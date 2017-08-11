package com.twinsoft.convertigo.engine.admin.services.studio.database_objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.common.XMLVector;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.IStepSourceContainer;
import com.twinsoft.convertigo.engine.AuthenticatedSessionManager.Role;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.admin.services.XmlService;
import com.twinsoft.convertigo.engine.admin.services.at.ServiceDefinition;
import com.twinsoft.convertigo.engine.studio.CheStudio;
import com.twinsoft.convertigo.engine.studio.dnd.StepSourceWrap;
import com.twinsoft.convertigo.engine.studio.responses.SetPropertyResponse;
import com.twinsoft.convertigo.engine.studio.views.sourcepicker.SourcePickerViewWrap;

@ServiceDefinition(
        name = "SetStepSourceDefinition",
        roles = { Role.WEB_ADMIN, Role.PROJECT_DBO_CONFIG },
        parameters = {},
        returnValue = ""
    )
public class SetStepSourceDefinition extends XmlService {

    @Override
    protected void getServiceResult(HttpServletRequest request, Document document) throws Exception {
        HttpSession session = request.getSession();

        CheStudio cheStudio = CallAction.getStudio(session);
        Element root = document.getDocumentElement();
        if (cheStudio != null) {
            SourcePickerViewWrap spv = cheStudio.getSourcePickerView();
            if (spv != null) {
                // Retrieve the StepSource definition
                String qname = request.getParameter("qname");
                DatabaseObject dbo = Engine.theApp.databaseObjectsManager.getDatabaseObjectByQName(qname);
                StepSourceWrap ssw = (StepSourceWrap) spv.getDragData();

                // Update step source definition
                if (com.twinsoft.convertigo.engine.admin.services.studio.sourcepicker.CanCreateSource.canCreateSource(dbo, ssw)) {
                    XMLVector<String> sourceDefinition = new XMLVector<>(2);
                    sourceDefinition.add(ssw.getPriority());
                    sourceDefinition.add(ssw.getXpath());

                    IStepSourceContainer stepSourceContainer = (IStepSourceContainer) dbo;
                    stepSourceContainer.setSourceDefinition(sourceDefinition);

                    root.appendChild(new SetPropertyResponse("sourceDefinition").toXml(document, qname));
                }
            }
        }
    }
}
