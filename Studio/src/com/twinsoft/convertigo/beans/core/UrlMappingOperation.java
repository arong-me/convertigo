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
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.beans.core;

import java.util.LinkedList;
import java.util.List;

import com.twinsoft.convertigo.engine.EngineException;

public abstract class UrlMappingOperation extends DatabaseObject {

	private static final long serialVersionUID = -160544540810026807L;

	public UrlMappingOperation() {
		super();
		databaseType = "UrlMappingOperation";
	}
	
	@Override
	public UrlMappingOperation clone() throws CloneNotSupportedException {
		UrlMappingOperation clonedObject = (UrlMappingOperation)super.clone();
		clonedObject.parameters = new LinkedList<UrlMappingParameter>();
		return clonedObject;
	}

	@Override
	public List<DatabaseObject> getAllChildren() {	
		List<DatabaseObject> rep = super.getAllChildren();
		rep.addAll(getParameterList());
		return rep;
	}
	
	@Override
    public void add(DatabaseObject databaseObject) throws EngineException {
		if (databaseObject instanceof UrlMappingParameter) {
			addParameter((UrlMappingParameter) databaseObject);
		} else {
			throw new EngineException("You cannot add to an URL mapping operation a database object of type " + databaseObject.getClass().getName());
		}
    }

    @Override
    public void remove(DatabaseObject databaseObject) throws EngineException {
		if (databaseObject instanceof UrlMappingParameter) {
			removeParameter((UrlMappingParameter) databaseObject);
		} else {
			throw new EngineException("You cannot remove from an URL mapping operation a database object of type " + databaseObject.getClass().getName());
		}
		super.remove(databaseObject);
    }
    
	/**
	 * The list of available parameters for this operation.
	 */
	transient private List<UrlMappingParameter> parameters = new LinkedList<UrlMappingParameter>();
		
	protected void addParameter(UrlMappingParameter parameter) throws EngineException {
		checkSubLoaded();
		String newDatabaseObjectName = getChildBeanName(parameters, parameter.getName(), parameter.bNew);
		parameter.setName(newDatabaseObjectName);
		parameters.add(parameter);
		super.add(parameter);
	}

	public void removeParameter(UrlMappingParameter parameter) throws EngineException {
		checkSubLoaded();
		parameters.remove(parameter);
	}
	
	public List<UrlMappingParameter> getParameterList() {
		checkSubLoaded();
		return sort(parameters);
	}

	public UrlMappingParameter getParameterByName(String parameterName) throws EngineException {
		checkSubLoaded();
		for (UrlMappingParameter parameter : parameters)
			if (parameter.getName().equalsIgnoreCase(parameterName)) return parameter;
		throw new EngineException("There is no parameter named \"" + parameterName + "\" found into this operation.");
	}
}