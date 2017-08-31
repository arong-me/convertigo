package com.twinsoft.convertigo.engine.admin.services.mobiles;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.twinsoft.convertigo.engine.AuthenticationException;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.AuthenticatedSessionManager.Role;
import com.twinsoft.convertigo.engine.admin.services.DownloadService;
import com.twinsoft.convertigo.engine.admin.services.ServiceException;
import com.twinsoft.convertigo.engine.admin.services.at.ServiceDefinition;
import com.twinsoft.convertigo.engine.admin.services.mobiles.MobileResourceHelper.Keys;
import com.twinsoft.convertigo.engine.enums.Accessibility;
import com.twinsoft.convertigo.engine.enums.HeaderName;
import com.twinsoft.convertigo.engine.enums.MimeType;

@ServiceDefinition(
		name = "GetSourcePackage",
		roles = { Role.ANONYMOUS },
		parameters = {},
		returnValue = ""
)
public class GetSourcePackage extends DownloadService {	
	
	@Override
	protected void writeResponseResult(HttpServletRequest request, HttpServletResponse response) throws  Exception {
		MobileResourceHelper mobileResourceHelper = new MobileResourceHelper(request, "mobile/www");
		
		if (mobileResourceHelper.mobileApplication == null) {
			throw new ServiceException("no such mobile application");
		} else {
			boolean bTpPrivateRole = Engine.authenticatedSessionManager.hasRole(request.getSession(), Role.TEST_PLATFORM_PRIVATE);
			if (!bTpPrivateRole && mobileResourceHelper.mobileApplication.getAccessibility() == Accessibility.Private) {
				throw new AuthenticationException("Authentication failure: user has not sufficient rights!");
			}
		}
		
		String project = Keys.project.value(request);
		String platform = Keys.platform.value(request);
		
		File mobileArchiveFile = mobileResourceHelper.makeZipPackage();
				
		FileInputStream archiveInputStream = new FileInputStream(mobileArchiveFile);		
		
		HeaderName.ContentDisposition.setHeader(response, "attachment; filename=\"" + project + "_" + platform + "_SourcePackage.zip\"");
		response.setContentType(MimeType.OctetStream.value());
		
		IOUtils.copy(archiveInputStream, response.getOutputStream());		
		
		archiveInputStream.close();
	}	

}
