/*
 * Copyright (c) 2001-2011 Convertigo SA.
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
 * $URL: svn://devus.twinsoft.fr/convertigo/CEMS_opensource/trunk/Studio/src/com/twinsoft/convertigo/beans/core/ReferenceBeanInfo.java $
 * $Author: nicolasa $
 * $Revision: 37168 $
 * $Date: 2014-05-19 17:31:38 +0200 (lun., 19 mai 2014) $
 */

package com.twinsoft.convertigo.beans.core;

import java.beans.PropertyDescriptor;

public class MobileObjectBeanInfo extends MySimpleBeanInfo {
    
	public MobileObjectBeanInfo() {
		try {
			beanClass = MobileObject.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.core.DatabaseObject.class;

			iconNameC16 = "/com/twinsoft/convertigo/beans/core/images/mobileobject_color_16x16.png";
			iconNameC32 = "/com/twinsoft/convertigo/beans/core/images/mobileobject_color_32x32.png";
			
			resourceBundle = getResourceBundle("res/MobileObject");
			
			displayName = resourceBundle.getString("display_name");
			shortDescription = resourceBundle.getString("short_description");
			
			properties = new PropertyDescriptor[0];
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}
