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

import com.twinsoft.convertigo.beans.mobile.components.dynamic.IonBean;
import com.twinsoft.convertigo.engine.EngineException;

public class UIDynamicMenu extends UIDynamicElement {

	private static final long serialVersionUID = 7671346079616209922L;

	public UIDynamicMenu() {
		super();
	}

	public UIDynamicMenu(String tagName) {
		super(tagName);
	}

	@Override
	public UIDynamicMenu clone() throws CloneNotSupportedException {
		UIDynamicMenu cloned = (UIDynamicMenu) super.clone();
		return cloned;
	}
	
	@Override
	protected void addUIComponent(UIComponent uiComponent, Long after) throws EngineException {
        if (isAutoMenu() && !(uiComponent instanceof UIAttribute)) {
            throw new EngineException("You cannot add component to this menu");
        }
        super.addUIComponent(uiComponent, after);
	}
	
	@Override
	protected StringBuilder initAttributes() {
		StringBuilder attributes = super.initAttributes();
		attributes.append(" id=\""+ getName() +"\" [content]=\"content\"");
		return attributes;
	}
	
	public boolean isAutoMenu() {
		IonBean ionBean = getIonBean();
		if (ionBean != null) {
			return ionBean.getName().equals("AutoMenu");
		}
		return false;
	}
	
	protected void markMenuAsDirty() throws EngineException {
		ApplicationComponent app = (ApplicationComponent) getParent();
		if (app != null) {
			app.markApplicationAsDirty();
		}
	}
}
