/*
 * Copyright (c) 2001-2016 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.beans.mobile.components;

import java.beans.PropertyDescriptor;

import com.twinsoft.convertigo.beans.core.MySimpleBeanInfo;

public class UIFormCustomValidatorBeanInfo extends MySimpleBeanInfo {
	
	public UIFormCustomValidatorBeanInfo() {
		try {
			beanClass = UIFormCustomValidator.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.mobile.components.UIFormValidator.class;

			iconNameC16 = "/com/twinsoft/convertigo/beans/mobile/components/images/uiformvalidator_color_16x16.png";
			iconNameC32 = "/com/twinsoft/convertigo/beans/mobile/components/images/uiformvalidator_color_32x32.png";

			resourceBundle = getResourceBundle("res/UIFormCustomValidator");

			displayName = resourceBundle.getString("display_name");
			shortDescription = resourceBundle.getString("short_description");
			
			properties = new PropertyDescriptor[2];
			
			properties[0] = new PropertyDescriptor("validatorName", beanClass, "getValidatorName", "setValidatorName");
			properties[0].setDisplayName(getExternalizedString("property.validatorName.display_name"));
			properties[0].setShortDescription(getExternalizedString("property.validatorName.short_description"));
            properties[0].setExpert(true);
			
			properties[1] = new PropertyDescriptor("validatorValue", beanClass, "getValidatorValue", "setValidatorValue");
			properties[1].setDisplayName(getExternalizedString("property.validatorValue.display_name"));
			properties[1].setShortDescription(getExternalizedString("property.validatorValue.short_description"));
            properties[1].setHidden(true);
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}
