/*
* Copyright (c) 2001-2014 Convertigo. All Rights Reserved.
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
 * $URL: http://sourceus.twinsoft.fr/svn/convertigo/CEMS_opensource/trunk/Studio/src/com/twinsoft/convertigo/eclipse/editors/completion/CtfCompletionProposalsComputer.java $
 * $Author: jmc $
 * $Revision: 37416 $
 * $Date: 2014-06-24 15:45:16 +0200 (Tue, 24 Jun 2014) $
 */

package com.twinsoft.convertigo.beans.steps;

import java.beans.PropertyDescriptor;

import com.twinsoft.convertigo.beans.core.MySimpleBeanInfo;

public class SetAuthenticatedUserStepBeanInfo extends MySimpleBeanInfo {
    
	public SetAuthenticatedUserStepBeanInfo() {
		try {
			beanClass = SetAuthenticatedUserStep.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.core.Step.class;

			iconNameC16 = "/com/twinsoft/convertigo/beans/steps/images/setAuthenticatedUser_16x16.png";
			iconNameC32 = "/com/twinsoft/convertigo/beans/steps/images/setAuthenticatedUser_32x32.png";
			
			resourceBundle = getResourceBundle("res/SetAuthenticatedUserStep");
			
			displayName = resourceBundle.getString("display_name");
			shortDescription = resourceBundle.getString("short_description");
			
			properties = new PropertyDescriptor[1];

            properties[0] = new PropertyDescriptor("userid", beanClass, "getUserId", "setUserId");
            properties[0].setDisplayName(getExternalizedString("property.userid.display_name"));
            properties[0].setShortDescription(getExternalizedString("property.userid.short_description"));
            properties[0].setPropertyEditorClass(getEditorClass("SmartTypeCellEditor"));
            
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}
