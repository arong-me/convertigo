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

import com.twinsoft.convertigo.beans.core.ITagsProperty;
import com.twinsoft.convertigo.engine.util.EnumUtils;

public class UIControlDirective extends UIElement implements IControl, ITagsProperty {
	
	private static final long serialVersionUID = 2750008565134796761L;

	public enum AttrDirective {
		ForEach("*ngFor"),
		If("*ngIf"),
		Switch("[ngSwitch]"),
		SwitchCase("*ngSwitchCase"),
		SwitchDefault("*ngSwitchDefault"),
		;
		
		String directive;
		AttrDirective(String directive) {
			this.directive = directive;
		}
		
		String directive() {
			return directive;
		}
		
		public static AttrDirective getDirective(String directiveName) {
			AttrDirective bindDirective = null;
			try {
				bindDirective = AttrDirective.valueOf(directiveName);
			} catch (Exception e) {};
			return bindDirective;
		}
		
		public static String getDirectiveAttr(String directiveName) {
			AttrDirective bindDirective = getDirective(directiveName);
			return bindDirective != null ? bindDirective.directive():directiveName;
		}
		
	}
	
	public UIControlDirective() {
		super("ng-container");
	}

	@Override
	public UIControlDirective clone() throws CloneNotSupportedException {
		UIControlDirective cloned = (UIControlDirective) super.clone();
		return cloned;
	}
	
	/*
	 * The directive to bind
	 */
	private String directiveName = AttrDirective.If.name();

	public String getDirectiveName() {
		return directiveName;
	}

	public void setDirectiveName(String directiveName) {
		this.directiveName = directiveName;
	}

	/*
	 * The directive value
	 */
	private String directiveExpression = "";

	public String getDirectiveExpression() {
		return directiveExpression;
	}

	public void setDirectiveExpression(String directiveExpression) {
		this.directiveExpression = directiveExpression;
	}
	
	/*
	 * The directive source
	 */
	private MobileSmartSourceType directiveSource = new MobileSmartSourceType();
	
	public MobileSmartSourceType getSourceSmartType() {
		return directiveSource;
	}

	public void setSourceSmartType(MobileSmartSourceType directiveSource) {
		this.directiveSource = directiveSource;
	}

	protected String getComputedValue() {
		StringBuilder sbListen = new StringBuilder();
		sbListen.append(directiveSource.getValue());
		
		StringBuilder children = new StringBuilder();
		if (sbListen.length() > 0) {
			AttrDirective attrDirective = AttrDirective.getDirective(getDirectiveName());
			if (AttrDirective.ForEach.equals(attrDirective)) {
				String item = "item"+ this.priority;
				children.append("let "+ item).append(" of ").append(sbListen);
			}
			else {
				children.append(sbListen);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(children).append(directiveExpression);
		
		return sb.toString();
	}

	protected String getDirectiveTemplate() {
		if (isEnabled()) {
			String directiveTpl = "";
			String value = getComputedValue().replaceAll("\"", "'");
			String attr = AttrDirective.getDirectiveAttr(getDirectiveName());
			if (!attr.isEmpty()) {
				directiveTpl = " "+ attr + "=" + "\""+ value +"\"";
			}
			return directiveTpl;
		}
		return "";
	}
	
	@Override
	public String[] getTagsForProperty(String propertyName) {
		if (propertyName.equals("directiveName")) {
			return EnumUtils.toNames(AttrDirective.class);
		}
		return new String[0];
	}

	@Override
	public String toString() {
		String label = getDirectiveName();
		return label = (label.isEmpty() ? "?":label) + " " 
							+ directiveSource.getLabel()
							+ directiveExpression;
	}

	@Override
	protected StringBuilder initAttributes() {
		StringBuilder attributes = super.initAttributes();
		attributes.append(getDirectiveTemplate());
		return attributes;
	}

	@Override
	public void updateSmartSource(long oldPriority, long newPriority) {
		String smartValue = directiveSource.getSmartValue();
		String oldString = String.valueOf(oldPriority);
		if (smartValue.indexOf(oldString) != -1) {
			String newString = String.valueOf(newPriority);
			directiveSource.setSmartValue(smartValue.replaceAll(oldString, newString));
			this.hasChanged = true;
		}
		super.updateSmartSource(oldPriority, newPriority);
	}
}
