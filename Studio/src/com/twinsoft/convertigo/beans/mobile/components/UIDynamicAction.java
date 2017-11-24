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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSourceType.Mode;
import com.twinsoft.convertigo.beans.mobile.components.UIControlDirective.AttrDirective;
import com.twinsoft.convertigo.beans.mobile.components.dynamic.ComponentManager;
import com.twinsoft.convertigo.beans.mobile.components.dynamic.IonBean;
import com.twinsoft.convertigo.beans.mobile.components.dynamic.IonProperty;

public class UIDynamicAction extends UIDynamicElement implements IAction {

	private static final long serialVersionUID = 5988583131428053374L;

	public UIDynamicAction() {
		super();
	}

	public UIDynamicAction(String tagName) {
		super(tagName);
	}

	@Override
	public UIDynamicAction clone() throws CloneNotSupportedException {
		UIDynamicAction cloned = (UIDynamicAction) super.clone();
		return cloned;
	}

	@Override
	protected StringBuilder initAttributes() {
		return new StringBuilder();
	}

	/*public String getInputId() {
		return "_"+ this.priority;
	}*/
	
	public String getFunctionName() {
		return "ATS"+ this.priority;
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
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					String inputs = computeActionInputs(true);
					String actionName = ionBean.getName();
					int i = inputs.indexOf("props:")+"props:".length();
					int j = inputs.indexOf("vars:")+"vars:".length();
					String props = inputs.substring(i, inputs.indexOf('}',i)+1);
					String vars = inputs.substring(j, inputs.indexOf('}',j)+1);
					return "actionBeans."+ actionName + "(this,"+ props + ","+ vars +")";
				}
			}
		}
		return "";
	}

	protected String computeActionInputs(boolean forTemplate) {
		if (isEnabled()) {
			IonBean ionBean = getIonBean();
			if (ionBean != null) {
				StringBuilder sbProps = new StringBuilder();
				for (IonProperty property : ionBean.getProperties().values()) {
					String p_name = property.getName();
					Object p_value = property.getValue();
					
					sbProps.append(sbProps.length() > 0 ? ", ":"");
					sbProps.append(p_name).append(": ");
					// case value is set
					if (!p_value.equals(false)) {
						MobileSmartSourceType msst = property.getSmartType();
						String smartValue = msst.getValue();
						
						if (Mode.PLAIN.equals(msst.getMode())) {
							if (property.getType().equalsIgnoreCase("string")) {
								smartValue = forTemplate ?
										"\'" + MobileSmartSourceType.escapeStringForTpl(smartValue) + "\'":
											"\'" + MobileSmartSourceType.escapeStringForTs(smartValue) + "\'";
							}
						}
						
						if (forTemplate) {
							smartValue = ""+smartValue;
						} else {
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
						}
						
						sbProps.append(smartValue);
					}
					// case value is not set
					else {
						sbProps.append("null");
					}
				}
				
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
									smartValue = "\'" + MobileSmartSourceType.escapeStringForTs(smartValue) + "\'";
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
		}
		return "";
	}
	
	@Override
	public void computeScripts(JSONObject jsonScripts) {
		try {
			String imports = jsonScripts.getString("imports");
			
			String search = "import * as ts from 'typescript';";
			if (imports.indexOf(search) == -1) {
				imports += search + System.lineSeparator();
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
		IonBean ionBean = getIonBean();
		if (ionBean != null) {
			int numThen = numberOfActions();
			String actionName = ionBean.getName();
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
			tsCode += "\t\tself = stack[\""+ getName() +"\"] = {};"+ System.lineSeparator();
			tsCode += "\t\tself.in = "+ inputs +";"+ System.lineSeparator();
			
			tsCode +="\t\treturn this.actionBeans."+actionName+"(this, self.in.props, self.in.vars)"+ System.lineSeparator();
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
		return "";
	}

	@Override
	protected Contributor getContributor() {
		return new Contributor() {
			@Override
			public Map<String, String> getActionTsFunctions() {
				Map<String, String> functions = new HashMap<String, String>();
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					String actionName = ionBean.getName();
					functions.put(actionName, ComponentManager.getActionTsCode(actionName));
				}
				return functions;
			}

			@Override
			public Map<String, String> getActionTsImports() {
				Map<String, String> imports = new HashMap<String, String>();
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					Map<String, List<String>> map = ionBean.getConfig().getActionTsImports();
					if (map.size() > 0) {
						for (String from : map.keySet()) {
							for (String component: map.get(from)) {
								imports.put(component.trim(), from);
							}
						}
					}
				}
				return imports;
			}

			@Override
			public Map<String, String> getModuleTsImports() {
				Map<String, String> imports = new HashMap<String, String>();
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					Map<String, List<String>> map = ionBean.getConfig().getModuleTsImports();
					if (map.size() > 0) {
						for (String from : map.keySet()) {
							for (String component: map.get(from)) {
								imports.put(component.trim(), from);
							}
						}
					}
				}
				return imports;
			}

			@Override
			public Set<String> getModuleNgImports() {
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					return ionBean.getConfig().getModuleNgImports();
				}
				return new HashSet<String>();
			}

			@Override
			public Set<String> getModuleNgProviders() {
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					return ionBean.getConfig().getModuleNgProviders();
				}
				return new HashSet<String>();
			}

			@Override
			public Map<String, String> getPackageDependencies() {
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					return ionBean.getConfig().getPackageDependencies();
				}
				return new HashMap<String, String>();
			}

			@Override
			public Map<String, String> getConfigPlugins() {
				IonBean ionBean = getIonBean();
				if (ionBean != null) {
					return ionBean.getConfig().getConfigPlugins();
				}
				return new HashMap<String, String>();
			}
			
		};
	}

	@Override
	protected void addInfos(Map<String, Set<String>> infoMap) {
		IonBean ionBean = getIonBean();
		if (ionBean != null) {
			String beanName = ionBean.getName(); 
			if (ionBean.hasProperty("marker")) {
				JSONObject json = new JSONObject();
				String key = null;
				
				for (IonProperty property : ionBean.getProperties().values()) {
					MobileSmartSourceType msst = property.getSmartType();
					String p_name = property.getName();
					Object p_value = property.getValue();
					
					if (!p_value.equals(false)) {
						if (beanName.equals("FullSyncViewAction")) {
							if (p_name.equals("fsview")) {
								key = p_value.toString() + ".view";
							}
						} else if (beanName.equals("CallSequenceAction") || beanName.equals("CallFullSyncAction")) {
							if (p_name.equals("requestable")) {
								key = p_value.toString();
							}
						}
					}
					
					try {
						if (p_name.equals("marker")) {
							json.put(p_name, !p_value.equals(false) ? msst.getValue():"");
						}
						if (p_name.equals("include_docs")) {
							json.put(p_name, !p_value.equals(false) ? msst.getValue():"false");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				if (key != null && !key.isEmpty()) {
					Set<String> infos = infoMap.get(key);
					if (infos == null) {
						infos = new HashSet<String>();
					}
					String info = json.toString();
					if (!info.isEmpty()) {
						infos.add(info);
					}
					infoMap.put(key, infos);
				}
			}
		}
		
		super.addInfos(infoMap);
	}	
}
