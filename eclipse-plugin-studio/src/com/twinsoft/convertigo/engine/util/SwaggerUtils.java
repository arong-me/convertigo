package com.twinsoft.convertigo.engine.util;

import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twinsoft.convertigo.beans.core.IMappingRefModel;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.beans.core.UrlMapper;
import com.twinsoft.convertigo.beans.core.UrlMapping;
import com.twinsoft.convertigo.beans.core.UrlMappingOperation;
import com.twinsoft.convertigo.beans.core.UrlMappingParameter;
import com.twinsoft.convertigo.beans.core.UrlMappingParameter.DataContent;
import com.twinsoft.convertigo.beans.core.UrlMappingParameter.DataType;
import com.twinsoft.convertigo.beans.core.UrlMappingParameter.Type;
import com.twinsoft.convertigo.beans.core.UrlMappingResponse;
import com.twinsoft.convertigo.beans.rest.AbstractRestOperation;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EnginePropertiesManager;
import com.twinsoft.convertigo.engine.EnginePropertiesManager.PropertyName;
import com.twinsoft.convertigo.engine.enums.MimeType;

public class SwaggerUtils {
	private static Pattern parseRequestUrl = Pattern.compile("http(s)?://(.*?)(/.*?api)");

	public static Swagger read(String url) {
		return new SwaggerParser().read(url);
	}
	
	private static Swagger parseCommon(String requestUrl, Project project) {
		Swagger swagger = new Swagger();
		
		Contact contact = new Contact();
		/*contact.setName("Convertigo Support");
		contact.setEmail("support@convertigo.com");
		contact.setUrl("http://www.convertigo.com/#developers");*/
		
		Info info = new Info();
		info.setContact(contact);
		info.setTitle("Convertigo REST API");
		info.setDescription("Find here all deployed projects");
		if (project != null) {
			info.setDescription(project.getComment());
			info.setVersion(project.getVersion());			
		}

		List<Scheme> schemes = new ArrayList<Scheme>();
		String host;
		String basePath;
		
		Matcher matcher = parseRequestUrl.matcher(requestUrl);
		if (matcher.find()) {
			schemes.add(matcher.group(1) == null ? Scheme.HTTP : Scheme.HTTPS);
			host = matcher.group(2);
			basePath = matcher.group(3);
		} else {
			String webAppPath = EnginePropertiesManager.getProperty(PropertyName.APPLICATION_SERVER_CONVERTIGO_URL);
			int index = webAppPath.indexOf("://") + 3;
			host = webAppPath.substring(index, webAppPath.indexOf('/', index));
			basePath = webAppPath.substring(index + host.length()) + "/api";
			schemes.add(Scheme.HTTP);
			schemes.add(Scheme.HTTPS);
		}
		swagger.setInfo(info);
		swagger.setSchemes(schemes);
		swagger.setHost(host);
		swagger.setBasePath(basePath);
		
		swagger.setConsumes(Arrays.asList("multipart/form-data", MimeType.WwwForm.value(), MimeType.Json.value(), MimeType.Xml.value()));
		
		swagger.setProduces(Arrays.asList(MimeType.Json.value(), MimeType.Xml.value()));
		return swagger;
	}
	
	public static Swagger parse(String requestUrl, Collection<UrlMapper> collection) {
		Swagger swagger = parseCommon(requestUrl, null);
		
		List<Tag> tags = new ArrayList<Tag>();
		Map<String, Path> paths = new HashMap<String, Path>();
		Map<String, Model> models = new HashMap<String, Model>();
		for (UrlMapper urlMapper : collection) {
			if (urlMapper != null) {
				Swagger p_swagger = parse(requestUrl, urlMapper);
				if (p_swagger != null) {
					if (p_swagger != null) {
						tags.addAll(p_swagger.getTags());
						paths.putAll(p_swagger.getPaths());
						models.putAll(p_swagger.getDefinitions());
					}
				}
			}
		}
		swagger.setTags(tags);
		swagger.setPaths(paths);
		swagger.setDefinitions(models);
		
		return swagger;
	}
	
	public static Swagger parse(String requestUrl, String projectName) {
		Swagger swagger;
		
		Project project = null;
		try {
			project = Engine.theApp.databaseObjectsManager.getProjectByName(projectName);
			swagger = parseCommon(requestUrl, project);
			
			List<Tag> tags = new ArrayList<Tag>();
			Tag tag = new Tag();
			tag.setName(projectName);
			tag.setDescription(project.getComment());
			tags.add(tag);
			swagger.setTags(tags);
			
		} catch (Exception e) {
			e.printStackTrace();
			swagger = new Swagger();
		}
		
		return swagger;
	}
	
	public static Swagger parse(String requestUrl, UrlMapper urlMapper) {
		Project project = urlMapper.getProject();
		Swagger swagger = parseCommon(requestUrl, project);
		
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag();
		tag.setName(urlMapper.getProject().getName());
		tag.setDescription(urlMapper.getProject().getComment());
		tags.add(tag);
		swagger.setTags(tags);
		
		Map<String, Model> swagger_models = new HashMap<String, Model>();		
		try {
			String models = urlMapper.getModels();
			if (!models.isEmpty()) {
				ObjectMapper mapper = Json.mapper();
				JsonNode definitionNode = mapper.readTree(models);
				for (Iterator<Entry<String, JsonNode>> it = GenericUtils.cast(definitionNode.fields()); it.hasNext();) {
					Entry<String, JsonNode> entry = it.next();
					swagger_models.put(entry.getKey().toString(), mapper.convertValue(entry.getValue(), Model.class));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Engine.logEngine.warn("Unexpected exception while reading UrlMapper defined models", e);
		}
		swagger.setDefinitions(swagger_models);
		
		
		Map<String, Path> swagger_paths = new HashMap<String, Path>();
		try {
			for (UrlMapping urlMapping: urlMapper.getMappingList()) {
				Path swagger_path = new Path();
				for (UrlMappingOperation umo : urlMapping.getOperationList()) {
					Operation s_operation = new Operation();
					s_operation.setOperationId(umo.getName());
					s_operation.setDescription(umo.getComment());
					s_operation.setSummary(umo.getComment());
					
					// Operation produces
					if (umo instanceof AbstractRestOperation) {
						DataContent dataOutput = ((AbstractRestOperation)umo).getOutputContent();
						if (dataOutput.equals(DataContent.toJson)) {
							s_operation.setProduces(Arrays.asList(MimeType.Json.value()));
						}
						else if (dataOutput.equals(DataContent.toXml)) {
							s_operation.setProduces(Arrays.asList(MimeType.Xml.value()));
						}
						else {
							s_operation.setProduces(Arrays.asList(MimeType.Json.value(), MimeType.Xml.value()));
						}
					}
					
					// Operation tags
					List<String> list = Arrays.asList(""+ project.getName());
					s_operation.setTags(list);
					
					// Operation consumes
					List<String> consumes = new ArrayList<String>();
					
					// Operation parameters
					List<Parameter> s_parameters = new ArrayList<Parameter>();
					// 1 - add path parameters
					for (String pathVarName: urlMapping.getPathVariableNames()) {
						PathParameter s_parameter = new PathParameter();
						s_parameter.setName(pathVarName);
						s_parameter.setRequired(true);
						s_parameter.setType("string");
						
						// retrieve parameter description from bean
						UrlMappingParameter ump = null;
						try {
							ump = umo.getParameterByName(pathVarName);
						} catch (Exception e) {}
						if (ump != null && ump.getType() == Type.Path) {
							s_parameter.setDescription(ump.getComment());
							s_parameter.setType(ump.getInputType().toLowerCase());
						}
						
						s_parameters.add(s_parameter);
					}
					// 2 - add other parameters
					for (UrlMappingParameter ump: umo.getParameterList()) {
						Parameter s_parameter = null;
						if (ump.getType() == Type.Query) {
							s_parameter = new QueryParameter();
						}
						else if (ump.getType() == Type.Form) {
							s_parameter = new FormParameter();
						}
						else if (ump.getType() == Type.Body) {
							s_parameter = new BodyParameter();
							if (ump instanceof IMappingRefModel) {
								String modelreference = ((IMappingRefModel)ump).getModelReference();
								if (!modelreference.isEmpty()) {
									RefModel refModel = new RefModel(modelreference);
									((BodyParameter)s_parameter).setSchema(refModel);
								}
							}
						}
						else if (ump.getType() == Type.Header) {
							s_parameter = new HeaderParameter();
						}
						else if (ump.getType() == Type.Path) {
							// ignore : should have been treated before
						}
						
						if (s_parameter != null) {
							s_parameter.setName(ump.getName());
							s_parameter.setDescription(ump.getComment());
							s_parameter.setRequired(ump.isRequired());
							
							if (s_parameter instanceof SerializableParameter) {
								boolean isArray = ump.isMultiValued() || ump.isArray();
								String _type = isArray ? "array":ump.getDataType().name().toLowerCase();
								String _collectionFormat = ump.isMultiValued() ? "multi":(isArray ? "csv":null);
								Property _items = isArray ? getItems(ump.getDataType()):null;
								
								((SerializableParameter)s_parameter).setType(_type);
								((SerializableParameter)s_parameter).setCollectionFormat(_collectionFormat);
								((SerializableParameter) s_parameter).setItems(_items);
								
								/*String value = s_parameter.getValue();
								if (value != null) {
									String collection = s_parameter.getCollection();
									if (collection != null && collection.equals("multi")) {
										Property items = new StringProperty();
										items.setDefault(value);
										((SerializableParameter) s_parameter).setItems(items);
									}
									else {
										((AbstractSerializableParameter<?>)s_parameter).setDefaultValue(value);
									}
								}*/
								
							}
							
							DataContent dataInput = ump.getInputContent();
							if (dataInput.equals(DataContent.toJson)) {
								if (!consumes.contains(MimeType.Json.value())) {
									consumes.add(MimeType.Json.value());
								}
							}
							else if (dataInput.equals(DataContent.toXml)) {
								if (!consumes.contains(MimeType.Xml.value())) {
									consumes.add(MimeType.Xml.value());
								}
							}
							
							s_parameters.add(s_parameter);
						}
					}
					s_operation.setParameters(s_parameters);
					
					if (!consumes.isEmpty()) {
						s_operation.setConsumes(consumes);
					}
					
					// Set operation responses
					Map<String, Response> responses = new HashMap<String, Response>();
					for (UrlMappingResponse umr: umo.getResponseList()) {
						String statusCode = umr.getStatusCode();
						if (!statusCode.isEmpty()) {
							if (!responses.containsKey(statusCode)) {
								Response response = new Response();
								//response.setDescription(umr.getComment());
								response.setDescription(umr.getStatusText());
								if (umr instanceof IMappingRefModel) {
									String modelreference = ((IMappingRefModel)umr).getModelReference();
									if (!modelreference.isEmpty()) {
										RefProperty refProperty = new RefProperty(modelreference);
										response.setSchema(refProperty);
									}
								}
								responses.put(statusCode, response);
							}
						}
					}
					if (responses.isEmpty()) {
						Response resp200 = new Response();
						resp200.description("successful operation");
						responses.put("200", resp200);
					}
					s_operation.setResponses(responses);
					
					// Add operation to path
					String s_method = umo.getMethod().toLowerCase();
					swagger_path.set(s_method, s_operation);
				}
				swagger_paths.put(urlMapping.getPathWithPrefix(), swagger_path);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Engine.logEngine.error("Unexpected exception while parsing UrlMapper to generate definition", e);
		}
		
		swagger.setPaths(swagger_paths);
		
		return swagger;
	}
	
	public static Property getItems(DataType dataType) {
		if (DataType.String.equals(dataType))
			return new StringProperty();
		else if (DataType.Integer.equals(dataType))
			return new IntegerProperty();
		else if (DataType.Boolean.equals(dataType))
			return new BooleanProperty();
		else if (DataType.Number.equals(dataType))
			return new DoubleProperty();
		return null;
	}
	
	public static String getYamlDefinition(String requestUrl, Object object) throws JsonProcessingException {
		if (object instanceof String) {	// project name
			return prettyPrintYaml(parse(requestUrl, (String) object));
		}
		if (object instanceof UrlMapper) {	// urlmapper of project
			return prettyPrintYaml(parse(requestUrl, (UrlMapper) object));
		}
		if (object instanceof Collection<?>) { // all projects urlmapper
			Collection<UrlMapper> collection = GenericUtils.cast(object);
			return prettyPrintYaml(parse(requestUrl, collection));
		}
		return null;
	}

	public static String getJsonDefinition(String requestUrl, Object object) {
		if (object instanceof String) {	// project name
			return prettyPrintJson(parse(requestUrl, (String)object));
		}
		if (object instanceof UrlMapper) {
			return prettyPrintJson(parse(requestUrl, (UrlMapper)object));
		}
		if (object instanceof Collection<?>) {
			Collection<UrlMapper> collection = GenericUtils.cast(object);
			return prettyPrintJson(parse(requestUrl, collection));
		}
		return null;
	}
	
	public static String prettyPrintJson(Swagger swagger) {
		return Json.pretty(swagger);
	}
	
	public static String prettyPrintYaml(Swagger swagger) throws JsonProcessingException {
		return Yaml.pretty().writeValueAsString(swagger);
	}

	public static void testReadJson() {
		Swagger swagger = read("http://petstore.swagger.io/v2/swagger.json");
		if (swagger != null) {
			Json.prettyPrint(swagger);
			Yaml.prettyPrint(swagger);
		}		
	}
	
	public static void testReadYaml() {
		Swagger swagger = read("http://petstore.swagger.io/v2/swagger.yaml");
		if (swagger != null) {
			Json.prettyPrint(swagger);
			Yaml.prettyPrint(swagger);
		}		
	}
	
	public static void testReadPath() throws JsonProcessingException, IOException {
		String data = "{"
				+ "\"post\": { \"tags\": [\"pet\"], \"summary\": \"add a new pet to the store\", \"description\": \"\", \"operationid\": \"addpet\", \"consumes\": [\"application/json\", \"application/xml\"], \"produces\": [\"application/xml\", \"application/json\"], \"parameters\": [{ \"in\": \"body\", \"name\": \"body\", \"description\": \"pet object that needs to be added to the store\", \"required\": true, \"schema\": { \"$ref\": \"#/definitions/pet\" } }], \"responses\": { \"405\": { \"description\": \"invalid input\" } }, \"security\": [{ \"petstore_auth\": [\"write:pets\", \"read:pets\"] }] },"
				+ "\"put\": { \"tags\": [\"pet\"], \"summary\": \"update an existing pet\", \"description\": \"\", \"operationid\": \"updatepet\", \"consumes\": [\"application/json\", \"application/xml\"], \"produces\": [\"application/xml\", \"application/json\"], \"parameters\": [{ \"in\": \"body\", \"name\": \"body\", \"description\": \"pet object that needs to be added to the store\", \"required\": true, \"schema\": { \"$ref\": \"#/definitions/pet\" } }], \"responses\": { \"400\": { \"description\": \"invalid id supplied\" }, \"404\": { \"description\": \"pet not found\" }, \"405\": { \"description\": \"validation exception\" } }, \"security\": [{ \"petstore_auth\": [\"write:pets\", \"read:pets\"] }] }"
				+ "}";
		ObjectMapper mapper = Json.mapper();
		JsonNode pathNode = mapper.readTree(data);
		Path path = mapper.convertValue(pathNode, Path.class);
		Json.prettyPrint(path);
	}

/*
	public static void main(String[] args) {
		try {
			testReadJson();
			testReadYaml();
			testReadPath();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
*/
}
