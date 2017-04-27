/*
 * Copyright (c) 2001-2017 Convertigo. All Rights Reserved.
 *
 * The copyright to the computer  program(s) herein  is the property
 * of Convertigo.
 * The program(s) may  be used  and/or copied  only with the written
 *  permission  of  Convertigo  or in accordance  with  the terms and
 * conditions  stipulated  in the agreement/contract under which the
 * program(s) have been supplied.
 *
 * Convertigo makes  no  representations  or  warranties  about  the
 * suitability of the software, either express or implied, including
 * but  not  limited  to  the implied warranties of merchantability,
 * fitness for a particular purpose, or non-infringement. Convertigo
 * shall  not  be  liable for  any damage  suffered by licensee as a
 * result of using,  modifying or  distributing this software or its
 * derivatives.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
*/

package com.twinsoft.convertigo.engine.admin.services.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.twinsoft.api.Session;
import com.twinsoft.convertigo.engine.EnginePropertiesManager;
import com.twinsoft.convertigo.engine.EnginePropertiesManager.PropertyName;
import com.twinsoft.convertigo.engine.admin.services.XmlService;
import com.twinsoft.convertigo.engine.admin.services.at.ServiceDefinition;
import com.twinsoft.convertigo.engine.AuthenticatedSessionManager.Role;
import com.twinsoft.convertigo.engine.util.GenericUtils;
import com.twinsoft.tas.Key;
import com.twinsoft.tas.KeyManager;


@ServiceDefinition(
		name = "List",
		roles = { Role.WEB_ADMIN, Role.KEYS_CONFIG, Role.KEYS_VIEW },
		parameters = {},
		returnValue = "",
		cloud_forbidden = true
	)
public class List extends XmlService{

	protected void getServiceResult(HttpServletRequest request, Document document) throws Exception {
		Element rootElement = document.getDocumentElement();
        
        String tasRoot = EnginePropertiesManager.getProperty(PropertyName.CARIOCA_URL);
        
        KeyManager.init(tasRoot);
        
    	java.util.List<Key> keys = new ArrayList<Key>(GenericUtils.<Collection<Key>>cast(KeyManager.keys.values()));
    	Collections.sort(keys, new Comparator<Key>() {
			public int compare(Key o1, Key o2) {
				return o1.getQuickSortValue().toString().compareTo(o2.getQuickSortValue().toString());
			}
		});
    	
    	Iterator<Key> iKey = keys.iterator();
    	int nbValidKey = 0;
    	if (iKey.hasNext()) {
        	Key key = iKey.next();
        	while (key != null) {
            	int total = 0;
        		int emulatorID = key.emulatorID;
        		String emulatorName = KeyManager.getEmulatorName(emulatorID);

        		// Count number of valid keys
        		nbValidKey += KeyManager.hasExpired(emulatorID) ? 0 : (key.bDemo ? 0 : 1);

        		Element keysElement = document.createElement("keys");
        		
        		do {
       				total += key.cv;
        			
        			Element keyElement = document.createElement("key");
        			keyElement.setAttribute("text", key.sKey);
        			keyElement.setAttribute("value", Integer.toString(key.cv));
        			keyElement.setAttribute("evaluation",key.bDemo ? "true" : "false");
        			
        			/**
        			 * for demo keys, adjust expiry date to milliEvalDate (startDate + 30 days, see KeyManager)
        			 */
        			if (key.bDemo)
        				keyElement.setAttribute("expiration", ""+(KeyManager.getFirstStartDate()/(1000*3600*24)));
        			else
        				keyElement.setAttribute("expiration", Integer.toString(key.expiration));
        			
        			keyElement.setAttribute("expired", key.cv == 0 ? "true" : "false");
        			
        			NodeList keyList =  keysElement.getChildNodes();
        			
        			if (keyList.getLength() == 0)
        				keysElement.appendChild(keyElement);
        			else {
	        			for (int i=0; i<keyList.getLength(); i++) {
	        				Element el = ((Element)keyList.item(i));
	        				String expiration = el.getAttribute("expiration");
	        				
	        				if (expiration.equals("0") || expiration.compareTo(Integer.toString(key.expiration)) > 0) {
	        					keysElement.insertBefore(keyElement,  (Element)keyList.item(i));
	        					break;
	        				}

	        				if (i == keyList.getLength()-1)
	        					keysElement.appendChild(keyElement);
	        			}
        			}
        				
        			key = iKey.hasNext() ? iKey.next() : null;
        		} while (key != null && emulatorID == key.emulatorID);
        		
        		/*
        		 * parse final licence array to adjust sessions keys where the latest one overrides the previous
        		 */
        		NodeList keyList =  keysElement.getChildNodes();
        		if ((emulatorID == Session.EmulIDSE) && (keyList.getLength() > 0)) { 
        			// override computed total with value from last
        			total = Integer.parseInt(((Element)keyList.item(keyList.getLength()-1)).getAttribute("value"));
        			// sets all the others to 0
        			for (int i = 0; i < keyList.getLength() - 1; i++) {
        				((Element) keyList.item(i)).setAttribute("value", "0");
        			}
        		}
        		
        		Element emulatorNameElement = document.createElement("category");    	    	    	
        		emulatorNameElement.setAttribute("name", emulatorName);
        		emulatorNameElement.setAttribute("remaining", Integer.toString(KeyManager.getCV(emulatorID)));
        		emulatorNameElement.setAttribute("total", Integer.toString(total));
        		
        		if (emulatorID == Session.EmulIDSE) {        			
        			emulatorNameElement.setAttribute("overflow", KeyManager.isOverflow(emulatorID) ? "true" : "false");
        		}
        		
        		emulatorNameElement.appendChild(keysElement);
        		
        		rootElement.appendChild(emulatorNameElement);
        	}
    	}
    	
    	//We add the number of valid into the XML response
    	Element countValidKey = document.createElement("nb_valid_key");
    	countValidKey.setTextContent(nbValidKey + "");
		rootElement.appendChild(countValidKey);
		
    	Element firstStartDate = document.createElement("firstStartDate");
    	firstStartDate.setTextContent(KeyManager.getFirstStartDate() + "");
		rootElement.appendChild(firstStartDate);
	}
}
