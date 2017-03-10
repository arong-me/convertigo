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

import com.twinsoft.convertigo.beans.core.MobileComponent;

public class RouteEventComponent extends MobileComponent {

	private static final long serialVersionUID = -5879576200562937068L;

	public RouteEventComponent() {
		super();
	}
	
	@Override
	public RouteEventComponent clone() throws CloneNotSupportedException {
		RouteEventComponent cloned = (RouteEventComponent)super.clone();
		return cloned;
	}

	@Override
	public RouteComponent getParent() {
		return (RouteComponent) super.getParent();
	}	

	@Override
	protected String computeTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * The event source
	 */
	protected String source = "*";

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	/*
	 * The marker for mobile client application
	 */
	private String marker = "";
	
	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}
	
	/*
	 * The 'requestable' used by mobile client application
	 */
	protected String getRequestableSource() {
		return getSource();
	}
	
	public String getRequestableString() {
		return getRequestableSource() + (marker.isEmpty() ? "" : "#" + marker);
	}

	protected String getSourceName() {
		String sourceName = getSource();
		if (!sourceName.isEmpty() && sourceName.startsWith(getProject().getName())) {
			try {
				sourceName = sourceName.substring(sourceName.lastIndexOf('.')+1);
			} catch (IndexOutOfBoundsException e) {}
		}
		return sourceName;
	}
	
	@Override
	public String toString() {
		String label = getSourceName();
		return "on " + (label.equals("") ? "?" : label);
	}
	
}