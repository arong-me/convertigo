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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.twinsoft.convertigo.beans.common.FormatedContent;
import com.twinsoft.convertigo.beans.common.XMLVector;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSourceType.Mode;
import com.twinsoft.convertigo.beans.mobile.components.UIControlDirective.AttrDirective;

public class UICustomAction extends UIComponent implements IAction {

	private static final long serialVersionUID = 4203444295012733219L;

	public UICustomAction() {
		super();
	}
	
	@Override
	public UICustomAction clone() throws CloneNotSupportedException {
		UICustomAction cloned = (UICustomAction) super.clone();
		return cloned;
	}
	
	public String getFunctionName() {
		return "ATS"+ this.priority;
	}
	
	public String getActionName() {
		return "CTS"+ this.priority;
	}

	/*
	 * The needed page imports
	 */
	private XMLVector<XMLVector<String>> page_ts_imports = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getPageTsImports() {
		return page_ts_imports;
	}
	
	public void setPageTsImports(XMLVector<XMLVector<String>> page_ts_imports) {
		this.page_ts_imports = page_ts_imports;
	}
	
	/*
	 * The needed module imports
	 */
	private XMLVector<XMLVector<String>> module_ts_imports = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getModuleTsImports() {
		return module_ts_imports;
	}
	
	public void setModuleTsImports(XMLVector<XMLVector<String>> module_ts_imports) {
		this.module_ts_imports = module_ts_imports;
	}

	/*
	 * The needed ngModule imports
	 */
	private XMLVector<XMLVector<String>> module_ng_imports = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getModuleNgImports() {
		return module_ng_imports;
	}
	
	public void setModuleNgImports(XMLVector<XMLVector<String>> module_ng_imports) {
		this.module_ng_imports = module_ng_imports;
	}

	/*
	 * The needed ngModule providers
	 */
	private XMLVector<XMLVector<String>> module_ng_providers = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getModuleNgProviders() {
		return module_ng_providers;
	}
	
	public void setModuleNgProviders(XMLVector<XMLVector<String>> module_ng_providers) {
		this.module_ng_providers = module_ng_providers;
	}

	/*
	 * The needed package dependencies
	 */
	private XMLVector<XMLVector<String>> package_dependencies = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getPackageDependencies() {
		return package_dependencies;
	}
	
	public void setPackageDependencies(XMLVector<XMLVector<String>> package_dependencies) {
		this.package_dependencies = package_dependencies;
	}

	/*
	 * The needed cordova plugins
	 */
	private XMLVector<XMLVector<String>> cordova_plugins = new XMLVector<XMLVector<String>>();
	
	public XMLVector<XMLVector<String>> getCordovaPlugins() {
		return cordova_plugins;
	}
	
	public void setCordovaPlugins(XMLVector<XMLVector<String>> cordova_plugins) {
		this.cordova_plugins = cordova_plugins;
	}

	/*
	 * The action value
	 */
	private FormatedContent actionValue = new FormatedContent("\t\tpage.c8o.log.debug(event ? event.toString():'no event');\n\t\tresolve();\n");
	
	public FormatedContent getActionValue() {
		return actionValue;
	}

	public void setActionValue(FormatedContent actionValue) {
		this.actionValue = actionValue;
	}

	protected int numberOfActions() {
		int num = 0;
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIDynamicAction || component instanceof UICustomAction) {
				if (component.isEnabled()) {
					num++;
				}
			}
		}
		return num;
	}
	
	protected String getScope() {
		String scope = "";
		DatabaseObject parent = getParent();
		while (parent != null && !(parent instanceof UIPageEvent)) {
			if (parent instanceof UIControlDirective) {
				UIControlDirective uicd = (UIControlDirective)parent;
				if (AttrDirective.ForEach.equals(AttrDirective.getDirective(uicd.getDirectiveName()))) {
					scope += !scope.isEmpty() ? ", ":"";
					scope += "item"+uicd.priority + ": "+ "item"+uicd.priority;
				}
			}
			parent = parent.getParent();
		}
		return scope;
	}
	
	@Override
	public String computeTemplate() {
		if (isEnabled()) {
			if (numberOfActions() > 0 || getParent() instanceof UIPageEvent) {
				String scope = getScope();
				return getFunctionName() + "({root: {scope:{"+scope+"}, in:{}, out:$event}})";
			} else {
				String inputs = computeActionInputs(true);
				int i = inputs.indexOf("props:")+"props:".length();
				int j = inputs.indexOf("vars:")+"vars:".length();
				String props = inputs.substring(i, inputs.indexOf('}',i)+1);
				String vars = inputs.substring(j, inputs.indexOf('}',j)+1);
				
				String actionName = getActionName();
				return ""+ actionName + "(this,"+ props + ","+ vars +")";
			}
		}
		return "";
	}

	protected String computeActionInputs(boolean forTemplate) {
		if (isEnabled()) {
			StringBuilder sbProps = new StringBuilder();
			
			StringBuilder sbVars = new StringBuilder();
			Iterator<UIComponent> it = getUIComponentList().iterator();
			while (it.hasNext()) {
				UIComponent component = (UIComponent)it.next();
				if (component instanceof UIControlVariable) {
					UIControlVariable uicv = (UIControlVariable)component;
					if (uicv.isEnabled()) {
						sbVars.append(sbVars.length() > 0 ? ", ":"");
						sbVars.append(uicv.getVarName()).append(": ");
						
						if (forTemplate) {
							sbVars.append(uicv.getVarValue());
						} else {
							MobileSmartSourceType msst = uicv.getVarSmartType();
							
							String smartValue = msst.getValue();
							if (Mode.PLAIN.equals(msst.getMode())) {
								smartValue = msst.escapeStringForTs("\'" + smartValue + "\'");
							} else {
								smartValue = msst.escapeStringForTs(smartValue);
							}
							
							if (Mode.SOURCE.equals(msst.getMode())) {
								MobileSmartSource mss = msst.getSmartSource();
								if (mss.getFilter().equals(MobileSmartSource.Filter.Iteration)) {
									smartValue = "scope."+ smartValue;
								}
								else {
									smartValue = "this."+ smartValue;
								}
							}
							smartValue = smartValue.replaceAll("\\?\\.", ".");
							smartValue = smartValue.replaceAll("this\\.", "c8oPage.");
							smartValue = "get(`"+smartValue+"`)";
							
							sbVars.append(smartValue);
						}
					}
				}
			}
			
			return "{props:{"+sbProps+"}, vars:{"+sbVars+"}}";
		}
		return "";
	}
	
	@Override
	public void computeScripts(JSONObject jsonScripts) {
		try {
			PageComponent page = getPage();
			
			String imports = jsonScripts.getString("imports");
			for (XMLVector<String> v : page_ts_imports) {
				String name = v.get(0).trim();
				String path = v.get(1).trim();
				if (page.addImport(name, path)) {
					imports += "import { "+name+" } from '"+path+"';" + System.lineSeparator();
				}
			}
			
			jsonScripts.put("imports", imports);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		DatabaseObject parent = getParent();
		if (parent != null && !(parent instanceof IAction)) {
			try {
				String function = computeActionFunction();
				
				String functions = jsonScripts.getString("functions") + System.lineSeparator() + function;
				jsonScripts.put("functions", functions);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String function = computeActionMain();
			
			String functions = jsonScripts.getString("functions") + System.lineSeparator() + function;
			jsonScripts.put("functions", functions);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		super.computeScripts(jsonScripts);
	}

	protected String computeActionFunction() {
		String computed = "";
		if (isEnabled() && (numberOfActions() > 0 || getParent() instanceof UIPageEvent)) {
			StringBuilder parameters = new StringBuilder();
			parameters.append("stack");
			
			StringBuilder cartridge = new StringBuilder();
			cartridge.append("\t/**").append(System.lineSeparator())
						.append("\t * Function "+ getFunctionName()).append(System.lineSeparator());
			for (String commentLine : getComment().split(System.lineSeparator())) {
				cartridge.append("\t *   ").append(commentLine).append(System.lineSeparator());
			}
			cartridge.append("\t * ").append(System.lineSeparator());
			cartridge.append("\t * @param stack , the object which holds actions stack").append(System.lineSeparator());
			cartridge.append("\t */").append(System.lineSeparator());
			
			String functionName = getFunctionName();
			
			computed += System.lineSeparator();
			computed += cartridge;
			computed += "\t"+ functionName + "("+ parameters +") {" + System.lineSeparator();
			computed += "\t\tlet c8oPage : C8oPage = this;" + System.lineSeparator();
			computed += "\t\tlet parent;" + System.lineSeparator();
			computed += "\t\tlet scope;" + System.lineSeparator();
			computed += "\t\tlet self;" + System.lineSeparator();
			computed += "\t\tlet out;" + System.lineSeparator();
			computed += "\t\t" + System.lineSeparator();
			computed += "\t\tlet get = function(key) {let val=undefined;try {val=eval(ts.transpile(key));}catch(e){c8oPage.c8o.log.warn(\"[MB] "+functionName+": \"+e.message)}return val;}" + System.lineSeparator();
			computed += "\t\t" + System.lineSeparator();
			computed += "\t\tparent = stack[\"root\"];" + System.lineSeparator();
			computed += "\t\tscope = stack[\"root\"].scope;" + System.lineSeparator();
			computed += "\t\t" + System.lineSeparator();
			computed += "\t\tthis.c8o.log.debug(\"[MB] "+functionName+": started\");" + System.lineSeparator();
			computed += "\t\t" + System.lineSeparator();
			computed += ""+ computeActionContent();
			computed += "\t\t.catch((error:any) => {return Promise.resolve(this.c8o.log.debug(\"[MB] "+functionName+": An error occured : \",error.message))})" + System.lineSeparator();
			computed += "\t\t.then((res:any) => {this.c8o.log.debug(\"[MB] "+functionName+": ended\")});" + System.lineSeparator();
			computed += "\t}";
		}
		return computed;
	}
	
	protected String computeActionContent() {
		int numThen = numberOfActions();
		String beanName = getName();
		String actionName = getActionName();
		String inputs = computeActionInputs(false);
		
		StringBuilder sbThen = new StringBuilder();  
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component.isEnabled()) {
				String s = "";
				if (component instanceof UIDynamicAction) {
					s = ((UIDynamicAction)component).computeActionContent();
				}
				if (component instanceof UICustomAction) {
					s = ((UICustomAction)component).computeActionContent();
				}
				if (!s.isEmpty()) {
					sbThen.append(sbThen.length()>0 && numThen > 1 ? "\t\t,"+ System.lineSeparator() :"")
					.append(s);
				}
			}
		}

		String tsCode = "";
		tsCode += "\t\tnew Promise((resolve, reject) => {"+ System.lineSeparator();
		tsCode += "\t\tself = stack[\""+ beanName +"\"] = {};"+ System.lineSeparator();
		tsCode += "\t\tself.in = "+ inputs +";"+ System.lineSeparator();
		
		tsCode +="\t\treturn this."+actionName+"(this, self.in.props, self.in.vars)"+ System.lineSeparator();
		
		tsCode += "\t\t.then((res:any) => {"+ System.lineSeparator();
		tsCode += "\t\tparent = self;"+ System.lineSeparator();
		tsCode += "\t\tparent.out = res;"+ System.lineSeparator();
		tsCode += "\t\tout = parent.out;"+ System.lineSeparator();
		if (sbThen.length() > 0) {
			if (numThen > 1) {
				tsCode += "\t\treturn Promise.all(["+ System.lineSeparator();
				tsCode += sbThen.toString();
				tsCode += "\t\t])"+ System.lineSeparator();
			} else {
				tsCode += "\t\treturn "+ sbThen.toString().replaceFirst("\t\t", "");
			}
		} else {
			tsCode += "";
		}
		tsCode += "\t\t}, (error: any) => {console.log(\"[MB] "+actionName+" : \", error.message);throw new Error(error);})"+ System.lineSeparator();
		tsCode += "\t\t.then((res:any) => {resolve(res)}).catch((error:any) => {reject(error)})"+ System.lineSeparator();
		tsCode += "\t\t})"+ System.lineSeparator();
		return tsCode;
	}
	
	protected String computeActionMain() {
		String computed = "";
		if (isEnabled()) {
			StringBuilder cartridge = new StringBuilder();
			cartridge.append("\t/**").append(System.lineSeparator())
						.append("\t * Function "+ getName()).append(System.lineSeparator());
			for (String commentLine : getComment().split(System.lineSeparator())) {
				cartridge.append("\t *   ").append(commentLine).append(System.lineSeparator());
			}
			cartridge.append("\t * ").append(System.lineSeparator());
			
			StringBuilder parameters = new StringBuilder();
			parameters.append("page: C8oPage, props, vars");
			cartridge.append("\t * @param page  , the current page").append(System.lineSeparator());
			cartridge.append("\t * @param props , the object which holds properties key-value pairs").append(System.lineSeparator());
			cartridge.append("\t * @param vars  , the object which holds variables key-value pairs").append(System.lineSeparator());
			cartridge.append("\t */").append(System.lineSeparator());
			
			String actionName = getActionName();
			
			computed += System.lineSeparator();
			computed += cartridge;
			computed += "\t"+ actionName + "("+ parameters +") : Promise<any> {" + System.lineSeparator();
			computed += "\t\treturn new Promise((resolve, reject) => {"+ System.lineSeparator();
			computed += "\t\t/*Begin_c8o_function:"+ actionName +"*/" + System.lineSeparator();
			computed += actionValue.getString();
			computed += "\t\t/*End_c8o_function:"+ actionName +"*/" + System.lineSeparator();
			computed += "\t\t});"+ System.lineSeparator();
			computed += "\t}" + System.lineSeparator();
		}
		return computed;
	}
	
	protected Contributor getContributor() {
		return new Contributor() {
			@Override
			public Map<String, String> getActionTsFunctions() {
				return new HashMap<String, String>();
			}

			@Override
			public Map<String, String> getActionTsImports() {
				return new HashMap<String, String>();
			}

			@Override
			public Map<String, String> getModuleTsImports() {
				Map<String, String> imports = new HashMap<String, String>();
				for (XMLVector<String> v : module_ts_imports) {
					String name = v.get(0).trim();
					String path = v.get(1).trim();
					if (!name.isEmpty() && !path.isEmpty()) {
						if (!imports.containsKey(name)) {
							imports.put(name, path);
						}
					}
				}
				return imports;
			}

			@Override
			public Set<String> getModuleNgImports() {
				Set<String> modules = new HashSet<String>();
				for (XMLVector<String> v : module_ng_imports) {
					String module = v.get(0).trim();
					if (!module.isEmpty()) {
						if (!modules.contains(module)) {
							modules.add(module);
						}
					}
				}
				return modules;
			}

			@Override
			public Set<String> getModuleNgProviders() {
				Set<String> providers = new HashSet<String>();
				for (XMLVector<String> v : module_ng_providers) {
					String provider = v.get(0).trim();
					if (!provider.isEmpty()) {
						if (!providers.contains(provider)) {
							providers.add(provider);
						}
					}
				}
				return providers;
			}

			@Override
			public Map<String, String> getPackageDependencies() {
				Map<String, String> dependencies = new HashMap<String, String>();
				for (XMLVector<String> v : package_dependencies) {
					String pckg = v.get(0).trim();
					String version = v.get(1).trim();
					if (!pckg.isEmpty() && !version.isEmpty()) {
						if (!dependencies.containsKey(pckg)) {
							dependencies.put(pckg, version);
						}
					}
				}
				return dependencies;
			}

			@Override
			public Map<String, String> getConfigPlugins() {
				Map<String, String> plugins = new HashMap<String, String>();
				for (XMLVector<String> v : cordova_plugins) {
					String plugin = v.get(0).trim();
					String version = v.get(1).trim();
					if (!plugin.isEmpty() && !version.isEmpty()) {
						if (!plugins.containsKey(plugin)) {
							plugins.put(plugin, version);
						}
					}
				}
				return plugins;
			}
			
		};
	}	
}
