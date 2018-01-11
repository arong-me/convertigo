/*
* Copyright (c) 2009-2014 Convertigo. All Rights Reserved.
*
* The copyright to the computer  program(s) herein  is the property
* of Convertigo.
* The program(s) may  be used  and/or copied  only with the written
* permission  of  Convertigo  or in accordance  with  the terms and
* conditions  stipulated  in the agreement/contract under which the
* program(s) have been supplied.
*
* Convertigo makes  no  representations  or  warranties  about  the
* suitability of the software, either express or implied, including
* but  not  limited  to  the implied warranties of merchantability,
* fitness for a particular purpose, or non-infringement. Convertigo
* shall  not  be  liable for  any damage  suffered by licensee as a
* result of using,  modifying or  distributing this software or its
* derivatives.
*/

/*
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.engine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.connectors.CicsConnector;
import com.twinsoft.convertigo.beans.connectors.HtmlConnector;
import com.twinsoft.convertigo.beans.connectors.HttpConnector;
import com.twinsoft.convertigo.beans.connectors.JavelinConnector;
import com.twinsoft.convertigo.beans.connectors.SiteClipperConnector;
import com.twinsoft.convertigo.beans.connectors.SqlConnector;
import com.twinsoft.convertigo.beans.core.Connector;
import com.twinsoft.convertigo.beans.core.Criteria;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.DatabaseObject.ExportOption;
import com.twinsoft.convertigo.beans.core.ExtractionRule;
import com.twinsoft.convertigo.beans.core.Pool;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.beans.core.Reference;
import com.twinsoft.convertigo.beans.core.ScreenClass;
import com.twinsoft.convertigo.beans.core.Sequence;
import com.twinsoft.convertigo.beans.core.Sheet;
import com.twinsoft.convertigo.beans.core.Statement;
import com.twinsoft.convertigo.beans.core.StatementWithExpressions;
import com.twinsoft.convertigo.beans.core.Step;
import com.twinsoft.convertigo.beans.core.TestCase;
import com.twinsoft.convertigo.beans.core.Transaction;
import com.twinsoft.convertigo.beans.core.TransactionWithVariables;
import com.twinsoft.convertigo.beans.core.Variable;
import com.twinsoft.convertigo.beans.references.ProjectSchemaReference;
import com.twinsoft.convertigo.beans.screenclasses.JavelinScreenClass;
import com.twinsoft.convertigo.beans.screenclasses.SiteClipperScreenClass;
import com.twinsoft.convertigo.beans.statements.HandlerStatement;
import com.twinsoft.convertigo.beans.steps.SimpleStep;
import com.twinsoft.convertigo.beans.transactions.HtmlTransaction;
import com.twinsoft.convertigo.beans.transactions.HttpTransaction;
import com.twinsoft.convertigo.beans.transactions.JavelinTransaction;
import com.twinsoft.convertigo.beans.transactions.JsonHttpTransaction;
import com.twinsoft.convertigo.beans.transactions.SiteClipperTransaction;
import com.twinsoft.convertigo.beans.transactions.SqlTransaction;
import com.twinsoft.convertigo.beans.transactions.XmlHttpTransaction;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EngineException;
import com.twinsoft.convertigo.engine.helpers.WalkHelper;
import com.twinsoft.convertigo.engine.proxy.translated.ProxyTransaction;

public class ProjectUtils {

	public static void copyIndexFile(String projectName) throws Exception {
    	String projectRoot = Engine.PROJECTS_PATH + '/' +  projectName;
    	String templateBase = Engine.TEMPLATES_PATH + "/base";
    	File indexPage = new File(projectRoot + "/index.html");
    	if (!indexPage.exists()) {
    		if (new File(projectRoot + "/sna.xsl").exists()) { /** webization javelin */
        		if (new File(projectRoot + "/templates/status.xsl").exists()) { /** not DKU / DKU */
        			FileUtils.copyFile(new File(templateBase + "/index_javelin.html"), indexPage);
        		} else {
        			FileUtils.copyFile(new File(templateBase + "/index_javelinDKU.html"), indexPage);
        		}
        	} else {
        		FileFilter fileFilterNoSVN = new FileFilter() {
    				public boolean accept(File pathname) {
    					String name = pathname.getName();
    					return !name.equals(".svn") || !name.equals("CVS") || !name.equals("node_modules");
    				}
    			};
        		FileUtils.copyFile(new File(templateBase + "/index.html"), indexPage);
        		FileUtils.copyDirectory(new File(templateBase + "/js"), new File(projectRoot + "/js"), fileFilterNoSVN);
        		FileUtils.copyDirectory(new File(templateBase + "/css"), new File(projectRoot + "/css"), fileFilterNoSVN);
        	}
    	}
	}

	public static void renameProjectFile(String projectsDir, String sourceProjectName, String targetProjectName) throws Exception {
		String oldPath = projectsDir + "/" + targetProjectName + "/" + sourceProjectName + ".xml";
		File oldFile = new File(oldPath);
		if (oldFile.exists()) {
			String newPath = projectsDir + "/" + targetProjectName + "/" + targetProjectName + ".xml";
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				if (oldFile.renameTo(newFile)) {
					List<Replacement> replacements = new ArrayList<Replacement>();
					if (isPreviousXmlFileFormat(newPath)) {
						// replace project's bean name
						replacements.add(new Replacement("value=\""+sourceProjectName+"\"", "value=\""+targetProjectName+"\""));
						makeReplacementsInFile(replacements, newPath);
					}
					else {
						// replace project's bean name
						replacements.add(new Replacement("<!--<Project : " + sourceProjectName + ">", "<!--<Project : " + targetProjectName + ">"));
						replacements.add(new Replacement("value=\""+sourceProjectName+"\"", "value=\""+targetProjectName+"\"", "<!--<Project"));
						replacements.add(new Replacement("<!--</Project : " + sourceProjectName + ">", "<!--</Project : " + targetProjectName + ">"));
						makeReplacementsInFile(replacements, newPath);
					}
				}
				else {
					throw new Exception("Unable to rename \""+oldPath+"\" to \""+newPath+"\"");
				}
			}
			else {
				throw new Exception("File \""+newPath+"\" already exists");
			}
		}
		else {
			throw new Exception("File \""+oldPath+"\" does not exist");
		}
	}

	public static boolean isPreviousXmlFileFormat(String filePath) throws Exception { 
		boolean isPreviousFormat = false;
		String line= null;
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		while ((line = br.readLine()) != null) {
			if (line.indexOf("<project classname=\"com.twinsoft.convertigo.beans.core.Project\"")!=-1) {
				isPreviousFormat = !line.trim().startsWith("<!--<Project");
				break;
			}
		}
		br.close();
		return isPreviousFormat;
	}
	
	public static void renameXmlProject(String projectsDir, String sourceProjectName, String targetProjectName) throws Exception {
		String oldPath = projectsDir + "/" + targetProjectName + "/" + sourceProjectName + ".xml";
		File oldFile = new File(oldPath);
		if (oldFile.exists()) {
			String newPath = projectsDir + "/" + targetProjectName + "/" + targetProjectName + ".xml";
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				if (oldFile.renameTo(newFile)) {
					List<Replacement> replacements = new ArrayList<Replacement>();
					if (isPreviousXmlFileFormat(newPath)) {
						// replace project's bean name
						replacements.add(new Replacement("value=\""+sourceProjectName+"\"", "value=\""+targetProjectName+"\""));
						// replace project's name references
						replacements.add(new Replacement("value=\""+sourceProjectName+"\\.", "value=\""+targetProjectName+"\\."));
						makeReplacementsInFile(replacements, newPath);
					}
					else {
						// replace project's bean name
						replacements.add(new Replacement("<!--<Project : " + sourceProjectName + ">", "<!--<Project : " + targetProjectName + ">"));
						replacements.add(new Replacement("value=\""+sourceProjectName+"\"", "value=\""+targetProjectName+"\"", "<!--<Project"));
						replacements.add(new Replacement("<!--</Project : " + sourceProjectName + ">", "<!--</Project : " + targetProjectName + ">"));
						// replace project's name references
						replacements.add(new Replacement("value=\""+sourceProjectName+"\\.", "value=\""+targetProjectName+"\\."));
						makeReplacementsInFile(replacements, newPath);
					}
				}
				else {
					throw new Exception("Unable to rename \""+oldPath+"\" to \""+newPath+"\"");
				}
			}
			else {
				throw new Exception("File \""+newPath+"\" already exists");
			}
		}
		else {
			throw new Exception("File \""+oldPath+"\" does not exist");
		}
	}
	
	
	
	public static void renameXsdFile(String projectsDir, String sourceProjectName, String targetProjectName) throws Exception {
		String oldPath = projectsDir + "/" + targetProjectName + "/" + sourceProjectName + ".xsd";
		File oldFile = new File(oldPath);
		if (oldFile.exists()) {
			String newPath = projectsDir + "/" + targetProjectName + "/" + targetProjectName + ".xsd";
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				if (oldFile.renameTo(newFile)) {
					List<Replacement> replacements = new ArrayList<Replacement>();
					replacements.add(new Replacement("/"+sourceProjectName, "/"+targetProjectName));
					replacements.add(new Replacement(sourceProjectName+"_ns", targetProjectName+"_ns"));
					makeReplacementsInFile(replacements, newPath);
				}
				else {
					throw new Exception("Unable to rename \""+oldPath+"\" to \""+newPath+"\"");
				}
			}
			else {
				throw new Exception("File \""+newPath+"\" already exists");
			}
		}
	}
	
	public static void renameWsdlFile(String projectsDir, String sourceProjectName, String targetProjectName) throws Exception {
		String oldPath = projectsDir + "/" + targetProjectName + "/" + sourceProjectName + ".wsdl";
		File oldFile = new File(oldPath);
		if (oldFile.exists()) {
			String newPath = projectsDir + "/" + targetProjectName + "/" + targetProjectName + ".wsdl";
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				if (oldFile.renameTo(newFile)) {
					List<Replacement> replacements = new ArrayList<Replacement>();
					replacements.add(new Replacement("/"+sourceProjectName, "/"+targetProjectName));
					replacements.add(new Replacement(sourceProjectName+"_ns", targetProjectName+"_ns"));
					replacements.add(new Replacement(sourceProjectName+".xsd", targetProjectName+".xsd"));
					replacements.add(new Replacement(sourceProjectName+"Port", targetProjectName+"Port"));
					replacements.add(new Replacement(sourceProjectName+"SOAP", targetProjectName+"SOAP"));
					replacements.add(new Replacement("soapAction=\""+sourceProjectName+"\\?", "soapAction=\""+targetProjectName+"\\?"));
					replacements.add(new Replacement("definitions name=\""+sourceProjectName+"\"", "definitions name=\""+targetProjectName+"\""));
					replacements.add(new Replacement("service name=\""+sourceProjectName+"\"", "service name=\""+targetProjectName+"\""));
					makeReplacementsInFile(replacements, newPath);
				}
				else {
					throw new Exception("Unable to rename \""+oldPath+"\" to \""+newPath+"\"");
				}
			}
			else {
				throw new Exception("File \""+newPath+"\" already exists");
			}
		}
	}
	
	public static void renameConnector(String filePath, String oldName, String newName) throws Exception {
		if (filePath.endsWith(".wsdl") || filePath.endsWith(".xsd")) {
			List<Replacement> replacements = new ArrayList<Replacement>();
			replacements.add(new Replacement(oldName+"__", newName+"__"));
			makeReplacementsInFile(replacements, filePath);
		}
	}
	
	public static void makeReplacementsInFile(List<Replacement> replacements, String filePath) throws Exception {
		File file = new File(filePath);
		if (file.exists()) {
			String line;
			StringBuffer sb = new StringBuffer();
			
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while((line = br.readLine()) != null) {
				for (Replacement replacement: replacements) {
					String lineBegin = replacement.getStartsWith();
					if ((lineBegin == null) || (line.trim().startsWith(lineBegin))) {
						line = line.replaceAll(replacement.getSource(), replacement.getTarget());
					}
				}
				sb.append(line+"\n");
			}
			br.close();
			
			BufferedWriter out= new BufferedWriter(new FileWriter(filePath));
			out.write(sb.toString());
			out.close();
		}
		else {
			throw new Exception("File \""+filePath+"\" does not exist");
		}
	}

	public static void xsdRenameProject(String filePath, String sourceProjectName, String targetProjectName) throws Exception {
		if (filePath.endsWith(".xsd")) {
			List<Replacement> replacements = new ArrayList<Replacement>();
			replacements.add(new Replacement("/"+sourceProjectName, "/"+targetProjectName));
			replacements.add(new Replacement(sourceProjectName+"_ns", targetProjectName+"_ns"));
			makeReplacementsInFile(replacements, filePath);
		}
	}
	
	public static void xsdRenameConnector(String filePath, String oldName, String newName) throws Exception {
		if (filePath.endsWith(".xsd")) {
			List<Replacement> replacements = new ArrayList<Replacement>();
			replacements.add(new Replacement(oldName+"__", newName+"__"));
			makeReplacementsInFile(replacements, filePath);
		}
	}

	public static void getFullProjectDOM(Document document,String projectName, StreamSource xslFilter) throws TransformerFactoryConfigurationError, EngineException, TransformerException{
		Element root = document.getDocumentElement();
		getFullProjectDOM(document,projectName);
		
		// transformation du dom
		Transformer xslt = TransformerFactory.newInstance().newTransformer(xslFilter);				
		Element xsl = document.createElement("xsl");
		xslt.transform(new DOMSource(document), new DOMResult(xsl));
		root.replaceChild(xsl.getFirstChild(), root.getFirstChild());
	}
	
	public static void getFullProjectDOM(Document document, String projectName, ExportOption... exportOptions) throws TransformerFactoryConfigurationError, EngineException, TransformerException{
		Element root = document.getDocumentElement();
				
		Project project = Engine.theApp.databaseObjectsManager.getOriginalProjectByName(projectName);
		Element projectTag = project.toXml(document, exportOptions);
		projectTag.setAttribute("qname", project.getQName());
		root.appendChild(projectTag);

		constructDom(document, projectTag, project);
	}
	
	private static void constructDom(Document document, Element root, DatabaseObject father, ExportOption... exportOptions) throws EngineException {		
		if (father instanceof HtmlTransaction) {
			Collection<Statement> statements = ((HtmlTransaction) father).getStatements();
			addElement(statements, document, root, exportOptions);			
		} else {
			if (father instanceof StatementWithExpressions) {
				Collection<Statement> statements = ((StatementWithExpressions) father).getStatements();
				addElement(statements, document, root, exportOptions);
			} 
		}
		List<DatabaseObject> dbos = father.getAllChildren();
		addElement(dbos, document, root, exportOptions);
	}

	private static <E extends DatabaseObject> void addElement(Collection<E> collection, Document document, Element root, ExportOption... exportOptions) throws EngineException {
		for (E dbo : collection) {
			Element tag = dbo.toXml(document, exportOptions);
			tag.setAttribute("qname", dbo.getQName());
			root.appendChild(tag);
			constructDom(document, tag, dbo, exportOptions);
		}
	}
	
	public static boolean existProjectSchemaReference(Project project, String projectName) {
		if (projectName.equals(project.getName()))
			return true;
		for (Reference reference : project.getReferenceList()) {
			if (reference instanceof ProjectSchemaReference) {
				if (((ProjectSchemaReference)reference).getProjectName().equals(projectName))
					return true;
			}
		}
		return false;
	}
	
	public static Map<String,String> getStatByProject(Project project) throws Exception {
		final Map<String,String> result = new HashMap<String,String>();
        try {
    		if (project != null) {
    			try {

					new WalkHelper() {
						String displayString = "";

						@SuppressWarnings("unused")
						int depth = 0;
						int sequenceJavascriptLines;
						int sequenceJavascriptFunction;
						
    					int connectorCount = 0;    					
						int httpConnectorCount = 0;
						int httpsConnectorCount = 0;
						int htmlConnectorCount = 0;
						int cicsConnectorCount = 0;
						int siteClipperConnectorCount = 0;
						int sqlConnectorCount = 0;
						int javelinConnectorCount = 0;
    					
    					int htmlScreenclassCount = 0;
    					int htmlCriteriaCount = 0;
    					int siteClipperScreenclassCount = 0;
    					int siteClipperCriteriaCount = 0;
    					int htmlExtractionRuleCount = 0;
    					int htmlTransactionVariableCount = 0;
    					
    					int sqlTransactionVariableCount = 0;
    					int javelinTransactionVariableCount = 0;
    					int javelinScreenclassCount = 0;
    					int javelinCriteriaCount = 0;
    					int javelinExtractionRuleCount = 0;
    					int javelinEntryHandlerCount = 0;
    					int javelinExitHandlerCount = 0;
    					int javelinHandlerCount = 0;
    					int javelinJavascriptLines = 0;
    					int statementCount = 0;
    					int poolCount = 0;
    					int handlerstatementCount = 0;
						@SuppressWarnings("unused")
    					int reqVariableCount = 0;
    					int sequenceVariableCount = 0;
						@SuppressWarnings("unused")
    					int transactionVariableCount = 0;
    					int testcaseVariableCount = 0;
    					int testcaseCount = 0;
    					int sequenceCount = 0;
    					int stepCount = 0;
    					int sheetCount = 0;
    					int referenceCount = 0;
    					int selectInQueryCount = 0;

    					/*
    					 * transaction counters
    					 */
						@SuppressWarnings("unused")
    					int transactionCount = 0;
						@SuppressWarnings("unused")
    					int transactionWithVariablesCount = 0;
    					
    					int htmltransactionCount = 0;
    					int httpTransactionCount = 0;
    					int httpsTransactionCount = 0;
    					int xmlHttpTransactionCount = 0;
    					int xmlHttpsTransactionCount = 0;
    					int jsonHttpTransactionCount = 0;
    					int jsonHttpsTransactionCount = 0;
    					int proxyTransactionCount = 0;
    					int siteClipperTransactionCount = 0;
    					int javelinTransactionCount = 0;
    					int sqlTransactionCount = 0;
    					int totalC8oObjects = 0;
    					
    					public void go(DatabaseObject project) {
    						try {
    		                	String projectName = project.getName();                
    							
								init(project);
								
								connectorCount = htmlConnectorCount + httpConnectorCount + httpsConnectorCount + cicsConnectorCount + siteClipperConnectorCount + sqlConnectorCount + javelinConnectorCount;
								
								totalC8oObjects = 1  
										+ connectorCount	// connectors
										+ htmlScreenclassCount
										+ htmlCriteriaCount
										+ htmlExtractionRuleCount
										+ htmlTransactionVariableCount
										+ handlerstatementCount 
										+ statementCount
										+ javelinScreenclassCount
										+ javelinCriteriaCount
										+ javelinExtractionRuleCount
										+ javelinTransactionCount
										+ javelinEntryHandlerCount
										+ javelinExitHandlerCount
										+ javelinHandlerCount
										+ javelinTransactionVariableCount
										+ sqlTransactionCount
										+ sqlTransactionVariableCount
										+ sheetCount
										+ jsonHttpTransactionCount
										+ jsonHttpsTransactionCount
										+ xmlHttpTransactionCount
										+ xmlHttpsTransactionCount
										+ httpTransactionCount
										+ httpsTransactionCount
										+ proxyTransactionCount
										+ siteClipperTransactionCount
										+ siteClipperScreenclassCount
										+ siteClipperCriteriaCount
										+ sequenceCount
										+ stepCount
										+ sequenceVariableCount
										+ sequenceJavascriptFunction
										+ poolCount
										+ referenceCount
										+ testcaseCount
										+ testcaseVariableCount;								
							
								displayString = totalC8oObjects + " object(s)<br/>"														// ok
										+ connectorCount +" connector(s)";															// ok
								
								result.put(projectName, displayString);
								
								/*
								 * html connector
								 */
								
								if (htmltransactionCount > 0) {
									
									displayString = 
										(htmlScreenclassCount>0 ? "&nbsp;screenclassCount = " + htmlScreenclassCount + "<br/>" : "")											// ok
										+ (htmlCriteriaCount>0 ? "&nbsp;criteriaCount = " + htmlCriteriaCount + "<br/>" : "")
										+ (htmlExtractionRuleCount>0 ? "&nbsp;extractionRuleCount = " + htmlExtractionRuleCount + "<br/>" : "")
										+ "&nbsp;transactionCount = " + htmltransactionCount + "<br/>"											// ok
										+ (htmlTransactionVariableCount>0 ? "&nbsp;transactionVariableCount = " + htmlTransactionVariableCount + "<br/>" : "")
										+ "&nbsp;statementCount (handlers=" + handlerstatementCount + ", statements=" + statementCount +  ", total=" + (int)(handlerstatementCount + statementCount) + ")";
									
									result.put("HTML connector", displayString);
								}						

								/*
								 * javelin connector
								 */
								if (javelinScreenclassCount > 0) {
									
									displayString = 
										"&nbsp;screenclassCount = " + javelinScreenclassCount + "<br/>"											// ok
										+ (javelinCriteriaCount>0 ? "&nbsp;criteriaCount = " + javelinCriteriaCount + "<br/>" : "")
										+ (javelinExtractionRuleCount>0 ? "&nbsp;extractionRuleCount = " + javelinExtractionRuleCount + "<br/>" : "")
										+ (javelinTransactionCount>0 ? "&nbsp;transactionCount = " + javelinTransactionCount + "<br/>" : "")											// ok
										+ "&nbsp;handlerCount (Entry = " + javelinEntryHandlerCount + ", Exit = " + javelinExitHandlerCount + ", Screenclass = " + javelinHandlerCount + "), total = " 
										+ (int)(javelinEntryHandlerCount + javelinExitHandlerCount + javelinHandlerCount) + " in " + javelinJavascriptLines + " lines<br/>"										
										+ (javelinTransactionVariableCount>0 ? "&nbsp;variableCount = " + javelinTransactionVariableCount : "");
									
									result.put("Javelin connector", displayString);
								}						
								
								/*
								 * SQL connector
								 */
								if (sqlTransactionCount > 0) {
									
									displayString = 
										"&nbsp;sqltransactionCount = " + sqlTransactionCount + "<br/>"											// ok
										+ (selectInQueryCount>0 ? "&nbsp;selectInQueryCount = " + selectInQueryCount + "<br/>" : "")											// ok
										+ (sqlTransactionVariableCount>0 ? "&nbsp;transactionVariableCount = " + sqlTransactionVariableCount : "");

									if (sheetCount > 0) {
										displayString += 
												"<br/>Sheets<br/>" 
												+ "&nbsp;sheetCount = " + sheetCount;
									}

									result.put("SQL connector", displayString);
								}

								/*
								 * Http connector
								 */
								if(httpConnectorCount>0) {
									displayString = 
										"&nbsp;connectorCount = " + httpConnectorCount + "<br/>";
								}
								if (jsonHttpTransactionCount > 0) {
									
									displayString +=
										"&nbsp;JSONTransactionCount = " + jsonHttpTransactionCount + "<br/>"									// ok
										+ (xmlHttpTransactionCount>0 ? "&nbsp;XmlTransactionCount = " + xmlHttpTransactionCount + "<br/>" : "")	// ok
										+ (httpTransactionCount>0 ? "&nbsp;HTTPtransactionCount = " + httpTransactionCount : "");

									result.put("HTTP connector", displayString);
								}						

								/*
								 * Https connector
								 */
								if (httpsConnectorCount > 0) {
									displayString = 
										"&nbsp;connectorCount = " + httpsConnectorCount + "<br/>"
										+ (jsonHttpsTransactionCount>0 ? "&nbsp;JSONTransactionCount = " + jsonHttpsTransactionCount + "<br/>" :"")		// ok
										+ (xmlHttpsTransactionCount>0 ? "&nbsp;XmlTransactionCount = " + xmlHttpsTransactionCount + "<br/>"	: "")		// ok
										+ (httpsTransactionCount>0 ? "&nbsp;HTTPStransactionCount = " + httpsTransactionCount : "");														// ok
									
									result.put("HTTPS connector", displayString);
								}			

								/*
								 * Proxy connector
								 */
								if (proxyTransactionCount > 0) {
									
									displayString = 
										"&nbsp;TransactionCount = " + proxyTransactionCount;

									result.put("Proxy connector", displayString);
								}						
								
								/*
								 * Siteclipper connector
								 */
								if (siteClipperTransactionCount > 0) {
									
									displayString = 
										"&nbsp;TransactionCount = " + siteClipperTransactionCount + "<br/>"											// ok
										+ (siteClipperScreenclassCount>0 ? "&nbsp;screenclassCount = " + siteClipperScreenclassCount + "<br/>" :"")	// ok
										+ (siteClipperCriteriaCount>0 ? "&nbsp;criteriaCount = " + siteClipperCriteriaCount : "");
									
									result.put("SiteClipper connector", displayString);
								}						

								/*
								 * Sequencer
								 */
								if (sequenceCount > 0) {
									
									displayString = 
										"&nbsp;sequenceCount = " + sequenceCount + "<br/>"														// ok
										+ (stepCount>0 ? "&nbsp;stepCount = " + stepCount + "<br/>"	: "")														// ok
										+ (sequenceVariableCount>0 ? "&nbsp;variableCount = " + sequenceVariableCount + "<br/>" : "")
										+ "&nbsp;javascriptCode = " + sequenceJavascriptFunction + " functions in " + sequenceJavascriptLines + " lines"
										+  ((boolean)(sequenceJavascriptFunction == 0) ? " (declarations or so)":"");
									
									result.put("Sequencer", displayString);
								}
								
// 								displayString += " reqVariableCount = " + reqVariableCount + "\r\n";

								if (poolCount > 0) {
									
									displayString =
										"&nbsp;poolCount = " + poolCount;
									
									result.put("Pools", displayString);
								}
								
								if (referenceCount > 0) {
									
									displayString =
										"&nbsp;referenceCount = " + referenceCount;
									
									result.put("References", displayString);
								}
								
								if (testcaseCount > 0) {
									
									displayString =
										"&nbsp;testcaseCount = " + testcaseCount + "<br/>"
										+ (testcaseVariableCount>0 ? "&nbsp;testcaseVariableCount = " + testcaseVariableCount : "");
									
									result.put("Test cases", displayString);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
    					}
    					
						@Override
						protected void walk(DatabaseObject databaseObject) throws Exception {
							depth++;
							
							// String name = databaseObject.getName();
							
							// deal with connectors
							if (databaseObject instanceof Connector) {
								if (databaseObject instanceof HtmlConnector) {
									htmlConnectorCount++;
								}
								else
								if (databaseObject instanceof HttpConnector) {
									if (((HttpConnector)databaseObject).isHttps()) 
										httpsConnectorCount++;
									else
										httpConnectorCount++;
								}
								else
								if (databaseObject instanceof CicsConnector) {
									cicsConnectorCount++;
								}
								else
								if (databaseObject instanceof SiteClipperConnector) {
									siteClipperConnectorCount++;
								}
								else
								if (databaseObject instanceof SqlConnector) {
									sqlConnectorCount++;
								}
								else
								if (databaseObject instanceof JavelinConnector) {
									javelinConnectorCount++;
								}
							}					
/*							
							else
							if (databaseObject instanceof Reference) {    								
								referenceCount++;
							}
*/														
							else // deal with screenclasses
							if (databaseObject instanceof ScreenClass) {
								if (databaseObject instanceof JavelinScreenClass) {	// deal with javelinScreenClasses    								
									javelinScreenclassCount++;
								}
								else 
								if (databaseObject instanceof SiteClipperScreenClass) {	// deal with siteClipperScreenClasses    								
									siteClipperScreenclassCount++;
								}
								else {												// deal with html ScreenClasses
									htmlScreenclassCount++;
								}
							}
							else 
							if (databaseObject instanceof Criteria) {
								if (databaseObject.getParent() instanceof JavelinScreenClass) {																
									javelinCriteriaCount++;
								}
								else
								if (databaseObject.getParent() instanceof SiteClipperScreenClass) {																
									siteClipperCriteriaCount++;
								}
								else {
									htmlCriteriaCount++;
								}
							}
							else
							if (databaseObject instanceof ExtractionRule) {
								if (databaseObject.getParent() instanceof JavelinScreenClass) {																
									javelinExtractionRuleCount++;
								}
								else {
									htmlExtractionRuleCount++;
								}
							}
							else
							if (databaseObject instanceof Transaction) {
								if (databaseObject instanceof TransactionWithVariables) {
									if (databaseObject instanceof HtmlTransaction) {
										htmltransactionCount++;
									}
									else
									if (databaseObject instanceof JsonHttpTransaction) {
										if (((HttpConnector)databaseObject.getParent()).isHttps())
											jsonHttpsTransactionCount++;
										else
											jsonHttpTransactionCount++;
									}
									else
									if (databaseObject instanceof HttpTransaction) {
										if (((HttpConnector)databaseObject.getParent()).isHttps())
											httpsTransactionCount++;
										else
											httpTransactionCount++;
									}
									else
									if (databaseObject instanceof XmlHttpTransaction) {
										if (((HttpConnector)databaseObject.getParent()).isHttps())
											xmlHttpsTransactionCount++;
										else
											xmlHttpTransactionCount++;
									}								
									else
									if (databaseObject instanceof ProxyTransaction) {
										proxyTransactionCount++;
									}
									else
									if (databaseObject instanceof SiteClipperTransaction) {
										siteClipperTransactionCount++;
									}
									else
									if (databaseObject instanceof JavelinTransaction) {
										JavelinTransaction javelinTransaction = (JavelinTransaction)databaseObject;

										// Functions
										String line;
										int lineNumber = 0;
										BufferedReader br = new BufferedReader(new StringReader(javelinTransaction.handlers));

										while ((line = br.readLine()) != null) {
											line = line.trim();
											lineNumber++;
											if (line.startsWith("function ")) {
												try {
													String functionName = line.substring(9, line.indexOf(')') + 1);
													
													if (functionName.endsWith(JavelinTransaction.EVENT_ENTRY_HANDLER + "()")) {
														// TYPE_FUNCTION_SCREEN_CLASS_ENTRY
														javelinEntryHandlerCount++;
													} else if (functionName.endsWith(JavelinTransaction.EVENT_EXIT_HANDLER + "()")) {
														// TYPE_FUNCTION_SCREEN_CLASS_EXIT
														javelinExitHandlerCount++;
													} else {
														// TYPE_OTHER
														javelinHandlerCount++;
													}
												} catch(StringIndexOutOfBoundsException e) {
													// Ignore
												}
											}
										}

										// compute total number of lines of javascript
										javelinJavascriptLines += lineNumber;
										
										javelinTransactionCount++;
									}
									else
									if (databaseObject instanceof SqlTransaction) {
										SqlTransaction sqlTransaction = (SqlTransaction)databaseObject;
										/*
										 * count the number of SELECT
										 */
										String query = sqlTransaction.getSqlQuery();
										if (query != null) {
											query = query.toLowerCase();
											String pattern = "select";
											int lastIndex = 0;

											while(lastIndex != -1) {
												lastIndex = query.indexOf(pattern, lastIndex);
											    if (lastIndex != -1) {
											    	selectInQueryCount++;
											    	lastIndex += pattern.length();
											    }
											}
										}
										
										sqlTransactionCount++;
									}
									
									transactionWithVariablesCount++;
								}
								else { // transaction with no variables
									transactionCount++;
								}
							}
							else // deal with statements
							if (databaseObject instanceof Statement) {
								// System.out.println(databaseObject.getClass().getName() + "\r\n");
								if (databaseObject instanceof HandlerStatement) {
									handlerstatementCount++;									
								}
								else { 				
									statementCount++;
								}
							}
							else // deal with variables
							if (databaseObject instanceof Variable) {
								if (databaseObject.getParent() instanceof Transaction) {
									if (databaseObject.getParent() instanceof JavelinTransaction) {
										javelinTransactionVariableCount++;
									}
									else
									if (databaseObject.getParent() instanceof HtmlTransaction) {
										htmlTransactionVariableCount++;
									}
									else
									if (databaseObject.getParent() instanceof SqlTransaction) {
										sqlTransactionVariableCount++;
									}
									else { // should be zero
										transactionVariableCount++;
									}
								}
								else
								if (databaseObject.getParent() instanceof Sequence) { 
    								sequenceVariableCount++;
								}
								else
								if (databaseObject.getParent() instanceof TestCase) { 
    								testcaseVariableCount++;
								}
							}
							else
							if (databaseObject instanceof TestCase) {    
								testcaseCount++;
							}
							else
							if (databaseObject instanceof Sequence) {    
								sequenceCount++;
							}
							else
							if (databaseObject instanceof Step) {
								if (databaseObject instanceof SimpleStep) {
									SimpleStep simpleStep = (SimpleStep)databaseObject;
									
									// Functions
									String line;
									int lineNumber = 0;
									BufferedReader br = new BufferedReader(new StringReader(simpleStep.getExpression()));

									while ((line = br.readLine()) != null) {
										line = line.trim();
										lineNumber++;
										if (line.startsWith("function ")) {
											try {
												sequenceJavascriptFunction++;
											} catch(StringIndexOutOfBoundsException e) {
												// Ignore
											}
										}
									}

									sequenceJavascriptLines += lineNumber;
									stepCount++;
								}
								else
									stepCount++;
							}
							else
							if (databaseObject instanceof Sheet) {    
								sheetCount++;
							}
							else
							if (databaseObject instanceof Pool) {    
								poolCount++;
							}
							
							super.walk(databaseObject);
						}				
						
					}.go(project);
    			} catch (Exception e) {
    				// Just ignore, should never happen
    			}
    		}
        }
        catch (Throwable e) {
        	throw new Exception("Unable to compute statistics of the project!: \n"+e.getMessage());
        }
        finally {
        }
		return result;        
    }
}
