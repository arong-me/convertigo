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
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSourceType.Mode;

public class UIControlVariable extends UIComponent implements ITagsProperty {

	private static final long serialVersionUID = 413397469582687729L;

	public UIControlVariable() {
		super();
	}

	@Override
	public UIControlVariable clone() throws CloneNotSupportedException {
		UIControlVariable cloned = (UIControlVariable) super.clone();
		return cloned;
	}
	
	public String getVarName() {
		return getName();//varName;
	}
	
	private MobileSmartSourceType varValue = new MobileSmartSourceType("value");
	
	public MobileSmartSourceType getVarSmartType() {
		return varValue;
	}

	public void setVarSmartType(MobileSmartSourceType varValue) {
		this.varValue = varValue;
	}
	
	protected String getVarValue() {
		String value = varValue.getValue();
		if (Mode.PLAIN.equals(varValue.getMode())) {
			value = "'" + value + "'";
		}
		return value;
	}
	
	protected String getVarLabel() {
		String label = varValue.getLabel();
		if (Mode.PLAIN.equals(varValue.getMode())) {
			label = "'" + label + "'";
		}
		return label;
	}

	@Override
	public String computeTemplate() {
		if (isEnabled()) {
        	return getVarName() + ": " + getVarValue();
		}
		return "";
	}

	@Override
	public String toString() {
		String label = getVarName();
		label = label + (label.isEmpty() ? "":"=") + getVarLabel();
		return label;
	}

	@Override
	public String[] getTagsForProperty(String propertyName) {
		if (propertyName.equals("varValue")) {
			return new String[] {""};
		}
		return new String[0];
	}
	
	@Override
	public void updateSmartSource(long oldPriority, long newPriority) {
		String smartValue = varValue.getSmartValue();
		String oldString = String.valueOf(oldPriority);
		if (smartValue.indexOf(oldString) != -1) {
			String newString = String.valueOf(newPriority);
			varValue.setSmartValue(smartValue.replaceAll(oldString, newString));
			this.hasChanged = true;
		}
		super.updateSmartSource(oldPriority, newPriority);
	}
}
