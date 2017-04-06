package com.twinsoft.convertigo.engine.admin.services.studio.database_objects;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.core.BlockFactory;
import com.twinsoft.convertigo.beans.core.Criteria;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.ExtractionRule;
import com.twinsoft.convertigo.beans.core.IEnableAble;
import com.twinsoft.convertigo.beans.core.ScreenClass;
import com.twinsoft.convertigo.beans.core.Sheet;
import com.twinsoft.convertigo.beans.screenclasses.JavelinScreenClass;
import com.twinsoft.convertigo.engine.AuthenticatedSessionManager.Role;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.admin.services.XmlService;
import com.twinsoft.convertigo.engine.admin.services.at.ServiceDefinition;

@ServiceDefinition(
		name = "Get",
		roles = { Role.WEB_ADMIN, Role.PROJECT_DBO_CONFIG, Role.PROJECT_DBO_VIEW },
		parameters = {},
		returnValue = ""
	)
public class GetChildren extends XmlService {

	@Override
	protected void getServiceResult(HttpServletRequest request, Document document) throws Exception {
		String qname = request.getParameter("qname");
		
		final Element root = document.getDocumentElement();
		
		// Classic database objects
		if (qname != null) {
			getChildren(qname, root, 1);
		}
		// Project
		else {
			for (String qn: Engine.theApp.databaseObjectsManager.getAllProjectNamesList()) {
				getChildren(qn, root, 0);
			}
		}
	}
	
	private void getChildren(String qname, Element parent, int depth) throws Exception {
		DatabaseObject dbo = Engine.theApp.databaseObjectsManager.getDatabaseObjectByQName(qname);
		List<DatabaseObject> children = dbo.getDatabaseObjectChildren();
		
		// Get all children of the dbo
		Element elt = createDboElement(parent.getOwnerDocument(), dbo, !children.isEmpty());
		
		/*
		 *  In case of ScreenClass, we have to get Criteria, ExtractionRule and Sheets manually.
		 *  If fact, if the dbo is an inherited screen class, inherited Criteria, ExtractionRule and Sheets,
		 *  won't be retrieved by the method #getDatabaseObjectChildren.
		 */
		if (dbo instanceof ScreenClass) {
			ScreenClass sc = (ScreenClass) dbo;
			
			// Get all Criteria
			List<Criteria> criteria = sc.getCriterias();
			for (Criteria criterion : criteria) {
				children.remove(criterion);
				Element eltCriterion = createScreenClassChildElement(parent.getOwnerDocument(), criterion, dbo);
				elt.appendChild(eltCriterion);
			}
			
			// Get all Extraction Rules
			List<ExtractionRule> extractionRules = sc.getExtractionRules();
			for (ExtractionRule extractionRule : extractionRules) {
				children.remove(extractionRule);
				Element eltExtractionRule = createScreenClassChildElement(parent.getOwnerDocument(), extractionRule, dbo);
				elt.appendChild(eltExtractionRule);
			}
			
			// Get all Sheets
			List<Sheet> sheets = sc.getSheets();
			for (Sheet sheet : sheets) {
				children.remove(sheet);
				Element eltSheet = createScreenClassChildElement(parent.getOwnerDocument(), sheet, dbo);
				elt.appendChild(eltSheet);
			}
			
			// In case of JavelinScreenClass, we also have to get the block factory manually
			if (dbo instanceof JavelinScreenClass) {
				JavelinScreenClass jsc = (JavelinScreenClass) sc;
				BlockFactory blockFactory = jsc.getBlockFactory();
				children.remove(blockFactory);
				Element eltBlockFactory = createScreenClassChildElement(parent.getOwnerDocument(), blockFactory, dbo);
				elt.appendChild(eltBlockFactory);
			}
		}

		parent.appendChild(elt);
		if (depth > 0) {
			for (DatabaseObject child: children) {
				getChildren(child.getQName(), elt, depth - 1);
			}
		}
	}
	
	private Element createDboElement(Document document, DatabaseObject dbo, boolean hasChildren) throws DOMException, Exception {
		Element elt = document.createElement("dbo");
		elt.setAttribute("qname", dbo.getQName());
		elt.setAttribute("classname", dbo.getClass().getName());
		elt.setAttribute("name", dbo.toString());
		elt.setAttribute("category", dbo.getDatabaseType());
		elt.setAttribute("comment", dbo.getComment());
		elt.setAttribute("hasChildren", Boolean.toString(hasChildren));
		
		if (dbo instanceof IEnableAble) {
			elt.setAttribute("isEnabled", Boolean.toString(((IEnableAble) dbo).isEnabled()));
		}
		return elt;
	}
	
	private Element createScreenClassChildElement(Document document, DatabaseObject dbo, DatabaseObject dboParent) throws DOMException, Exception {
		Element elt = createDboElement(document, dbo, !dbo.getDatabaseObjectChildren().isEmpty());
		elt.setAttributeNode(createIsInheritedAttr(document, dbo, dboParent));
		return elt;
	}
	
	private Attr createIsInheritedAttr(Document document, DatabaseObject dbo, DatabaseObject dboParent) {
		Attr attr = document.createAttribute("isInherited");
		attr.setNodeValue(Boolean.toString(!dboParent.toString().equals(dbo.getParent().toString())));
		return attr;
	}

}
