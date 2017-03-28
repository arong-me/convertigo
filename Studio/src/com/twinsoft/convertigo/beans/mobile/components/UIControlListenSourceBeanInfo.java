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

public class UIControlListenSourceBeanInfo extends MySimpleBeanInfo {
	
	public UIControlListenSourceBeanInfo() {
		try {
			beanClass = UIControlListenSource.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.mobile.components.UIComponent.class;

			iconNameC16 = "/com/twinsoft/convertigo/beans/mobile/components/images/uicontrollistensource_color_16x16.png";
			iconNameC32 = "/com/twinsoft/convertigo/beans/mobile/components/images/uicontrollistensource_color_32x32.png";

			resourceBundle = getResourceBundle("res/UIControlListenSource");

			displayName = resourceBundle.getString("display_name");
			shortDescription = resourceBundle.getString("short_description");
			
			properties = new PropertyDescriptor[2];
			
			properties[0] = new PropertyDescriptor("target", beanClass, "getTarget", "setTarget");
			properties[0].setDisplayName(getExternalizedString("property.target.display_name"));
			properties[0].setShortDescription(getExternalizedString("property.target.short_description"));
			
			properties[1] = new PropertyDescriptor("marker", beanClass, "getMarker", "setMarker");
			properties[1].setDisplayName(getExternalizedString("property.marker.display_name"));
			properties[1].setShortDescription(getExternalizedString("property.marker.short_description"));
			
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}
