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
 * $URL: $
 * $Author: $
 * $Revision: $
 * $Date: $
 */
package com.twinsoft.convertigo.beans.transactions.couchdb;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.twinsoft.convertigo.beans.variables.RequestableVariable;
import com.twinsoft.convertigo.engine.util.GenericUtils;

public class UpdateDocumentTransaction extends AbstractDocumentTransaction {

	private static final long serialVersionUID = -7606732916561433014L;
	
	public UpdateDocumentTransaction() {
		super();
	}

	@Override
	public UpdateDocumentTransaction clone() throws CloneNotSupportedException {
		UpdateDocumentTransaction clonedObject =  (UpdateDocumentTransaction) super.clone();
		return clonedObject;
	}
	
	@Override
	public List<CouchDbParameter> getDeclaredParameters() {
		return Arrays.asList(new CouchDbParameter[] {var_database, var_id, var_data});
	}
		
	@Override
	protected Object invoke() throws Exception {
		JsonObject jsonDocument = new JsonObject();
		
		// add document members from variables
		for (RequestableVariable variable : getVariablesList()) {
			String variableName = variable.getName();
			JsonElement jsonv = toJson(getGson(), new JsonParser(), getParameterValue(variableName));
			if (jsonv != null) {
				if (jsonv instanceof JsonPrimitive) { // comes from a simple variable
					jsonDocument.add(variableName, jsonv);
				}
				else if (jsonv instanceof JsonObject) { // comes from a complex variable
					JsonObject jsonObject = jsonv.getAsJsonObject();
					Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
					for (Iterator<Entry<String, JsonElement>> it = GenericUtils.cast(set.iterator()); it.hasNext();) {
						Entry<String, JsonElement> entry = it.next();
						jsonDocument.add(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		
		addRevToDoc(jsonDocument);
		
		String jsonString = jsonDocument.toString();
		return getCouchDBDocument().update(encode(jsonString));
	}

	@Override
	public QName getComplexTypeAffectation() {
		return new QName(COUCHDB_XSD_NAMESPACE, "docUpdateType");
	}
}
