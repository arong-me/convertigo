package com.twinsoft.convertigo.engine.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.twinsoft.convertigo.beans.core.UrlMapper;
import com.twinsoft.convertigo.beans.core.UrlMappingOperation;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EngineException;
import com.twinsoft.convertigo.engine.RestApiManager;
import com.twinsoft.convertigo.engine.enums.MimeType;
import com.twinsoft.convertigo.engine.enums.Parameter;
import com.twinsoft.convertigo.engine.requesters.Requester;
import com.twinsoft.convertigo.engine.util.HttpServletRequestTwsWrapper;
import com.twinsoft.convertigo.engine.util.HttpUtils;
import com.twinsoft.convertigo.engine.util.ServletUtils;
import com.twinsoft.convertigo.engine.util.SwaggerUtils;

public class RestApiServlet extends GenericServlet {

	private static final long serialVersionUID = 6926586430359873778L;

	private String buildSwaggerDefinition(String requestUrl, String projectName, boolean isYaml) throws EngineException, JsonProcessingException {
		String definition = null;
		
		// Build a given project definition
		if (projectName != null) {
			UrlMapper urlMapper = RestApiManager.getInstance().getUrlMapper(projectName);
			if (urlMapper != null) {
				definition = isYaml ? SwaggerUtils.getYamlDefinition(requestUrl, urlMapper): SwaggerUtils.getJsonDefinition(requestUrl, urlMapper);
			}
			else {
				Engine.logEngine.warn("Project \""+projectName+"\" does not contain any UrlMapper.");
				definition = isYaml ? SwaggerUtils.getYamlDefinition(requestUrl, projectName): SwaggerUtils.getJsonDefinition(requestUrl, projectName);
			}
		}
		// Build all project definitions
		else {
			Collection<UrlMapper> collection = RestApiManager.getInstance().getUrlMappers();
			definition = isYaml ? SwaggerUtils.getYamlDefinition(requestUrl, collection): SwaggerUtils.getJsonDefinition(requestUrl, collection);
		}
		return definition;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getCharacterEncoding() == null) {
			try {
				request.setCharacterEncoding("UTF-8"); // Set encoding if needed
    		}
    		catch(Exception e) {
    			throw new ServletException(e);
    		}
		}
		
		HttpServletRequestTwsWrapper wrapped_request = new HttpServletRequestTwsWrapper(request);
		request = wrapped_request;
		
		String encoded = request.getParameter(Parameter.RsaEncoded.getName());
		if (encoded != null) {
			String query = Engine.theApp.rsaManager.decrypt(encoded, request.getSession());
			wrapped_request.clearParameters();
			wrapped_request.addQuery(query);
		}
		
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		Engine.logEngine.debug("(RestApiServlet) Requested URI: "+ method + " " + uri);
		
		boolean isYaml = request.getParameter("YAML") != null;
		boolean isJson = request.getParameter("JSON") != null;
		
		if ("GET".equalsIgnoreCase(method) && uri.endsWith("/api") && (query == null || query.isEmpty())) {
			isJson = true;
		}
		
        // Generate YAML/JSON definition (swagger specific)
		if ("GET".equalsIgnoreCase(method) && (isYaml || isJson)) {
    		try {
    			String requestUrl = HttpUtils.originalRequestURL(request);
    			String output = buildSwaggerDefinition(requestUrl, request.getParameter("__project"), isYaml);
    			response.setCharacterEncoding("UTF-8");    			
    			response.setContentType((isYaml ? MimeType.Yaml : MimeType.Json).value());
                Writer writer = response.getWriter();
                writer.write(output);

                Engine.logEngine.debug("(RestApiServlet) Definition sent :\n"+ output);
    		}
    		catch(Exception e) {
    			throw new ServletException(e);
    		}
		}
		// Handle REST request
		else {
			try {
				Collection<UrlMapper> collection = RestApiManager.getInstance().getUrlMappers();
				
				if (collection.size() > 0) {
					// Found a matching operation
					UrlMappingOperation urlMappingOperation = null;
					for (UrlMapper urlMapper : collection) {
						urlMappingOperation = urlMapper.getMatchingOperation(request);
						if (urlMappingOperation != null) {
							break;
						}
					}
					
					// Handle request
					if (urlMappingOperation != null) {
						StringBuffer buf;
						
						// Request headers
						if (Engine.logEngine.isDebugEnabled()) {
							buf = new StringBuffer();
							buf.append("(RestApiServlet) Request headers:\n");
							Enumeration<String> headerNames = request.getHeaderNames();
							while (headerNames.hasMoreElements()) {
								String headerName = headerNames.nextElement();
								String headerValue = request.getHeader(headerName);
								buf.append(" " + headerName + "=" + headerValue + "\n");
							}
							Engine.logEngine.debug(buf.toString());

							Engine.logEngine.debug("(RestApiServlet) Request parameters: "+ 
													Collections.list(request.getParameterNames()));
						}
						
						// Handle request
		                String content = urlMappingOperation.handleRequest(request, response);
		                
		                // Set response status
		                ServletUtils.applyCustomStatus(request, response);
		                Engine.logEngine.debug("(RestApiServlet) Response status code: "+ response.getStatus());
		                
						// Set response headers
		                ServletUtils.applyCustomHeaders(request, response);
						if (Engine.logEngine.isDebugEnabled()) {
			    			buf = new StringBuffer();
			    			buf.append("(RestApiServlet) Response headers:\n");
			    			Collection<String> headerNames = response.getHeaderNames();
			    			for (String headerName: headerNames) {
			    				String headerValue = response.getHeader(headerName);
		    					buf.append(" " + headerName + "=" + headerValue + "\n");
			    			}
			    			Engine.logEngine.debug(buf.toString());
						}
						
						if (content != null) {
							Writer writer = response.getWriter();
				            writer.write(content);
						}
						
						Engine.logEngine.debug("(RestApiServlet) Request successfully handled");
					} else {
						Engine.logEngine.debug("(RestApiServlet) No matching operation for request");
						super.service(request, response);
					}
				} else {
					Engine.logEngine.debug("(RestApiServlet) No mapping defined");
					super.service(request, response);
				}
			} catch (Exception e) {
    			throw new ServletException(e);
    		} finally {
    			Requester requester = (Requester) request.getAttribute("convertigo.requester");
    			if (requester != null) {
	                processRequestEnd(request, requester);
	    			onFinally(request);
    			}
    		}
		}
	}

	@Override
	public Requester getRequester() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentExtension() {
		// TODO Auto-generated method stub
		return null;
	}
}
