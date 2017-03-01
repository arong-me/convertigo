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

import java.util.Iterator;

public class UIElement extends UIComponent {

	private static final long serialVersionUID = -8671694717057158581L;

	public UIElement() {
		super();
	}

	protected UIElement(String tagName) {
		this();
		this.tagName = tagName;
	}
	
	@Override
	public UIElement clone() throws CloneNotSupportedException {
		UIElement cloned = (UIElement) super.clone();
		return cloned;
	}

	private String tagName = "tag";
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	protected String computeTemplate() {
		if (isEnabled()) {
			StringBuilder attributes = new StringBuilder();
			StringBuilder children = new StringBuilder();
			
			Iterator<UIComponent> it = getUIComponentList().iterator();
			while (it.hasNext()) {
				UIComponent component = (UIComponent)it.next();
				if (component instanceof UIAttribute)
					attributes.append(component.computeTemplate());
				else
					children.append(component.computeTemplate());
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("<").append(getTagName())
				.append(attributes.length()>0 ? attributes:"")
			  .append(">").append(System.getProperty("line.separator"))
				.append(children.length()>0 ? children:"")
			  .append("</").append(getTagName())
			  	.append(">").append(System.getProperty("line.separator"));
			
			return sb.toString();
		}
		else
			return "";
	}


}