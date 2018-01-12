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

package com.twinsoft.convertigo.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.twinsoft.convertigo.engine.enums.SessionAttribute;
import com.twinsoft.convertigo.engine.util.HttpUtils;
import com.twinsoft.convertigo.engine.util.Log4jHelper;
import com.twinsoft.convertigo.engine.util.Log4jHelper.mdcKeys;

public abstract class AbstractContext {

	/**
	 * The context unique identifier.
	 */
	public String contextID;
	
	/**
	 * The context number (it is incremented by 1 for each newly created context).
	 */
	public int contextNum = 0;
	
	/**
	 * The context creation time.
	 */
	public long creationTime;
	
	/**
	 * The last access time to this context.
	 */
	public long lastAccessTime;

	/**
	 * The HTTP servlet request object.
	 */
	public HttpServletRequest httpServletRequest;
	
	/**
	 * The HTTP session object.
	 */
	public HttpSession httpSession;
	
	/**
	 * The servlet path for the current request.
	 */
	public String servletPath;
	
	/**
	 * The user agent from the calling client.
	 */
	public String userAgent;
	
	/**
	 * The remote address from the calling client.
	 */
	public String remoteAddr;
	
	/**
	 * The name of the calling client (if reverse DNS has been enabled).
	 */
	public String remoteHost;

	/**
	 * The user reference passed in input of the request
	 */
	public String userReference = null;	
	/**
	 * Indicates whether the cache functionnality is enabled.
	 */
	public boolean isCacheEnabled = true; 
	
	/**
	 * Indicates whether the context is to be destroyed after the transaction handling.
	 */
	public boolean requireEndOfContext = false; 

	/**
	 * Indicates whether the context is to be kept in the pool even if it is not in the expected state (i.e. wrong screen class).
	 */
	public boolean lockPooledContext = false;
	
	/**
	 * Parent context of the current sub transaction or sub sequence, if any.
	 */
	public Context parentContext = null;
	
	public String getAuthenticatedUser() {
		if (tasUserName == null) {
			return SessionAttribute.authenticatedUser.string(httpSession);
		} else {
			return tasUserName;
		}
	}
	
	public void setAuthenticatedUser(String authenticatedUser){
		if (authenticatedUser != null) {
//			portalUserName = authenticatedUser;
			SessionAttribute.authenticatedUser.set(httpSession, authenticatedUser);
		}
		updateUserInLog();
	}
	
	public void removeAuthenticatedUser(){
//		portalUserName = null;
		tasUserName = null;
		SessionAttribute.authenticatedUser.remove(httpSession);
		updateUserInLog();
	}
	
	public void updateUserInLog() {
		String authenticatedUser = getAuthenticatedUser();
		Log4jHelper.mdcPut(mdcKeys.User, authenticatedUser == null ? "(anonymous)" : "'" + authenticatedUser + "'");
	}
	/**
	 * The portal user name.
	 */
//	public String portalUserName;
	
	// Carioca / VIC related properties

	/**
	 * The TAS (VIC or Carioca) session key.
	 */
	public String tasSessionKey;
	
	/**
	 * The TAS (VIC or Carioca) user name.
	 */
	public String tasUserName;
	
	/**
	 * The TAS (VIC or Carioca) user ^password.
	 */
	public String tasUserPassword;
	
	/**
	 * The TAS (VIC or Carioca) user group.
	 */
	public String tasUserGroup;
	
	/**
	 * The TAS (VIC or Carioca) service code.
	 */
	public String tasServiceCode;
	
	/**
	 * The TAS (VIC or Carioca) remote address for connection.
	 */
	public String tasDteAddress;
	
	/**
	 * The TAS (VIC or Carioca) communication device for establishing the connection.
	 */
	public String tasCommDevice;
	
	/**
	 * The TAS (VIC or Carioca) virtual server name.
	 */
	public String tasVirtualServerName;
	
	// Document generation related properties
	
	/**
	 * The input XML document generated by the requester.
	 */
	public Document inputDocument;
	
	/**
	 * The output XML document generated by the current transaction.
	 */
	public Document outputDocument;
	
	/**
	 * The steps objects, useful for asynchronous transaction.
	 */
	public List<String> steps;
	
	/**
	 * The lang object, useful for translate the target application
	 */
	public String lang;

	public AbstractContext() {
	}
	
	private Map<String, Object> internalTable = new HashMap<String, Object>(256);
	
	/**
	 * Gets a stored variable from the context.
	 * 
	 * @param key the requested key (i.e. the variable name).
	 * 
	 * @return the variable value.
	 */
	public Object get(String key) {
		return internalTable.get(key);
	}
	
	/**
	 * Stores a variable value into the context.
	 * 
	 * @param key the variable name.
	 * @param value the variable value.
	 */
	public void set(String key, Object value) {
		internalTable.put(key, value);
	}

	/**
	 * Removes a previously stored variable value from the context.
	 * 
	 * @param key the variable name.
	 */
	public void remove(String key) {
		internalTable.remove(key);
	}
	
	/**
	 * Returns an enumeration of stored variables in the context
	 * 
	 */
	public Set<String> keys() {
		return internalTable.keySet();
	}
	
	/**
	 * @return Getter for the absolute requested URL
	 */
	public String getAbsoluteRequestedUrl() {
		return HttpUtils.originalRequestURL(httpServletRequest);
	}
	
	/**
	 * @return Getter for the Convertigo URL
	 */
	public String getConvertigoUrl() {
		return HttpUtils.convertigoRequestURL(httpServletRequest) + '/';
	}
	/**
	 * @return Getter for the current project URL
	 */
	public String getProjectUrl () {
		return HttpUtils.projectRequestURL(httpServletRequest) + '/';
	}
	/**
	 * @Return the path to project directory
	 * 
	 */
	public abstract String getProjectDirectory();
	
	/**
	 * @Return the project name
	 * 
	 */
	public abstract String getProjectName();
	
	public abstract Properties loadPropertiesFromWebInf(String fileName);
	
	public abstract boolean savePropertiesToWebInf(String fileName, Properties properties);
	
	public abstract Properties loadPropertiesFromProject(String fileName);
	
	public abstract boolean savePropertiesToProject(String fileName, Properties properties);
	
	public abstract Object getTransactionProperty(String propertyName);
	
	public abstract boolean isSOAPRequest();
	
	public abstract void abortRequestable();
	
	/**
	 * Adds a text node into the output XML document.
	 * 
	 * @param parentNode the parent node into which the text node should be inserted.
	 * @param tagName the text node tag name.
	 * @param text the text to create.
	 * 
	 * @return the created node.
	 */
	public abstract Node addTextNode(Node parentNode, String tagName, String text);

	/**
	 * Adds a text node into the output XML document under the root element.
	 * 
	 * @param tagName the text node tag name.
	 * @param text the text to create.
	 * 
	 * @return the created node.
	 */
	public abstract Node addTextNodeUnderRoot(String tagName, String text);
	
	/**
	 * Adds a text node into the output XML document under the "blocks" node.
	 * 
	 * @param tagName the text node tag name.
	 * @param text the text to create.
	 * 
	 * @return the created node.
	 */
	public abstract Node addTextNodeUnderBlocks(String tagName, String text);

	/**
	 * <p><em>This method only concerns Minitel projects.</em></p>
	 * 
	 * Waits for a new page for the same screen class or a new screen class.
	 * 
 	 * The method wait for one of the screens described by the screen classes in the
 	 * project to arrive. We wait for all the screen classes except the current one. In the
 	 * case of a next page on the same screen class, waitNextPage() will monitor the cursor
 	 * position. the method will return when the cursor position returns to the same position
 	 * it was before calling waitNextPage().  
	 *  
	 * You can use waitNextPage() method to synchronize your handler before returning
	 * "redetect", "accumulate" or "skip".
	 * 
	 * @param timeout		the time (in ms) we have to wait for the screen class.
	 * @param hardDelay		a delay (in ms) added after the nextpage has arrived.
	 * 
	 * @return				true, if we the page did arrive, false otherwise.
	 */
	public abstract boolean waitNextPage(String action, int timeout, int hardDelay) throws EngineException;
	
	/**
	 * <p><em>This method only concerns Minitel projects.</em></p>
	 * 
	 * Waits for one of the screens described by the screen classes
	 * in the project to arrive. The method waits for all the screen classes
	 * except the current one. You can use waitAtScreenClass() method to synchronize
	 * your handler before returning "redetect", "accumulate" or "skip".
	 * 
	 * @param timeout		the time (in ms) we have to wait for the screen class.
	 * @param hardDelay		a delay (in ms) added after the screen class has arrived.
	 * 
	 * @return				true, if we the screen did arrive, false otherwise.
	 */
	public abstract boolean waitAtScreenClass(int timeout, int hardDelay) throws EngineException;

	/**
	 * Encrypts a string using the DES algorithm.
	 *
	 * @param s the string to encrypt.
	 *
	 * @returns the encrypted string; the script is of hexadecimal
	 * string format, i.e. it contains only hexadecimal (printable)
	 * characters, or <code>null</code> if any error occurs.
	 *
	 * @see #decodeFromHexString
	 */
	public abstract String encodeToHexString(String s);

	public abstract String encodeToHexString(String passphrase, String s);
	
	/**
	 * Decrypts a string using the DES algorithm.
	 *
	 * @param s the string to decrypt; this string must have been
	 * encoded by the <code>encodeToHexString()</code> function in
	 * order to stay meaningfull.
	 *
	 * @returns the decrypted string, or <code>null</code> if any
	 * error occurs.
	 *
	 * @see #encodeToHexString
	 */
	public abstract String decodeFromHexString(String s);
	
	public abstract String decodeFromHexString(String passphrase, String s);
	
	/**
	 * Get the context requested by the client.
	 * 
	 * Can be the current context or the last ancestor context, through many call steps.
	 * 
	 * @return the context requested by the client
	 */
	public abstract Context getRootContext();
	
	
	/**
	 * Set a header to the HTTP response.
	 * 
	 * @param name the header name to set
	 * @param value the header value to set for this name
	 */
	public abstract void setResponseHeader(String name, String value);

	/**
	 * Set a status to the HTTP response.
	 * 
	 * @param code the status code to set
	 * @param text the status message to set for this code
	 */
	public abstract void setResponseStatus(Integer code, String text);
}