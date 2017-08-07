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

import java.util.Iterator;

import org.codehaus.jettison.json.JSONObject;

import com.twinsoft.convertigo.beans.mobile.components.dynamic.IonBean;
import com.twinsoft.convertigo.engine.EngineException;

public class UIElement extends UIComponent implements IStyleGenerator {
	
	private static final long serialVersionUID = -8671694717057158581L;

	public UIElement() {
		super();
	}

	protected UIElement(String tagName) {
		this();
		this.tagName = tagName;
	}
	
	@Override
	public UIElement clone() throws CloneNotSupportedException {
		UIElement cloned = (UIElement) super.clone();
		return cloned;
	}

	/*
	 * The tagname
	 */
	protected String tagName = "tag";
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/*
	 * the self-closing state
	 */
	private boolean selfClose = false;
	
	public boolean isSelfClose() {
		return selfClose;
	}

	public void setSelfClose(boolean selfClose) {
		this.selfClose = selfClose;
	}
	
	
	@Override
	public void addUIComponent(UIComponent uiComponent) throws EngineException {
	    addUIComponent(uiComponent, null);
	}
	
	@Override
	protected void addUIComponent(UIComponent uiComponent, Long after) throws EngineException {
        if (isSelfClose() && !(uiComponent instanceof UIAttribute 
        						|| uiComponent instanceof UIStyle
        							|| uiComponent instanceof UIFormValidator)) {
            throw new EngineException("You cannot add component to this self-closing tag");
        } else if (uiComponent instanceof UIFormValidator) {
        	if (this instanceof UIForm) {
        		if (!(uiComponent instanceof UIFormCustomValidator)) {
        			throw new EngineException("You can only add a custom validator to this component");
        		} else {
	    			super.addUIComponent(uiComponent, after);
        		}
        	} else {
	    		String formControlName = getFormControlName();
	    		if (formControlName.isEmpty()) {
	    			throw new EngineException("You cannot add validator to this component: Missing \"formControlName\" property or attribute.");
	    		}
	    		super.addUIComponent(uiComponent, after);
        	}
        } else {
        	if (uiComponent instanceof UIDynamicElement && uiComponent.bNew) {
        		if (getUIForm() != null) {
					IonBean ionBean = ((UIDynamicElement)uiComponent).getIonBean();
					if (ionBean != null && ionBean.hasProperty("FormControlName")) {
						ionBean.setPropertyValue("FormControlName", new MobileSmartSourceType("var"+uiComponent.priority));
					}
        		}
        	}
        	super.addUIComponent(uiComponent, after);
        }
	}

	@Override
	public String toString() {
		String label = getTagName();
		return label.isEmpty() ? super.toString() : label;
	}

	public String getTagClass() {
		return "class"+priority;
	}
	
	protected String getFormControlName() {
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIAttribute) {
				UIAttribute attribute = (UIAttribute)component;
				if (attribute.getAttrName().equals("formControlName")) {
					return attribute.getAttrValue();
				}
			}
		}
		return "";
	}

	protected StringBuilder initAttributes() {
		return new StringBuilder();
	}
	
	protected String computeJsonModel() {
		if (isEnabled()) {
			String formControlVarName = getFormControlName();
			if (!formControlVarName.isEmpty()) {
				return "\"['"+formControlVarName+"']\":{\"dirty\":\"\",\"errors\":\"\",\"valid\":\"\",\"value\":\"\"}";
			} else {
				StringBuilder models = new StringBuilder();
				Iterator<UIComponent> it = getUIComponentList().iterator();
				while (it.hasNext()) {
					UIComponent component = (UIComponent)it.next();
					if (component instanceof UIElement ) {
						String model = ((UIElement)component).computeJsonModel();
						models.append(models.length() > 0 && !model.isEmpty() ? ",":"").append(model);
					}
				}
				return models.toString();
			}
		}
		return "";
	}
	
	protected String computeConstructor() {
		if (isEnabled()) {
			StringBuilder sb = new StringBuilder();
			
			String formControlVarName = getFormControlName();
			if (!formControlVarName.isEmpty()) {
				StringBuilder constructors = new StringBuilder();
				Iterator<UIComponent> it = getUIComponentList().iterator();
				while (it.hasNext()) {
					UIComponent component = (UIComponent)it.next();
					if (component instanceof UIFormValidator) {
						UIFormValidator validator = (UIFormValidator)component;
						String constructor = validator.computeConstructor();
						constructors.append(constructors.length() > 0 && !constructor.isEmpty() ? ",":"").append(constructor);
					}
				}
				sb.append(System.lineSeparator());
				sb.append("\t\t\t"+formControlVarName + " : new FormControl('', Validators.compose([");
				sb.append(constructors);
				sb.append("]))").append(",");
			}
			else {
				StringBuilder constructors = new StringBuilder();
				Iterator<UIComponent> it = getUIComponentList().iterator();
				while (it.hasNext()) {
					UIComponent component = (UIComponent)it.next();
					if (component instanceof UIElement ) {
						String constructor = ((UIElement)component).computeConstructor();
						constructors.append(constructor);
					}
				}
				sb.append(constructors);
			}
			return sb.toString().replaceAll(",+", ",");
		}
		return "";
	}
	
	protected String computeFunction() {
		if (isEnabled()) {
			StringBuilder sb = new StringBuilder();
			String formControlVarName = getFormControlName();
			if (!formControlVarName.isEmpty()) {
				Iterator<UIComponent> it = getUIComponentList().iterator();
				while (it.hasNext()) {
					UIComponent component = (UIComponent)it.next();
					if (component instanceof UIFormValidator) {
						sb.append(((UIFormValidator)component).computeFunction());
					}
				}
			}
			else {
				Iterator<UIComponent> it = getUIComponentList().iterator();
				while (it.hasNext()) {
					UIComponent component = (UIComponent)it.next();
					if (component instanceof UIElement ) {
						sb.append(((UIElement)component).computeFunction());
					}
				}
			}
			return sb.toString();
		}
		return "";
	}
	
	@Override
	public void computeScripts(JSONObject jsonScripts) {
		super.computeScripts(jsonScripts);
	}
		
	@Override
	public String computeTemplate() {
		if (isEnabled()) {
			StringBuilder attributes = initAttributes();
			StringBuilder attrclasses = new StringBuilder();
			StringBuilder children = new StringBuilder();
			
			Iterator<UIComponent> it = getUIComponentList().iterator();
			while (it.hasNext()) {
				UIComponent component = (UIComponent)it.next();
				if (component instanceof UIFormControlValidator) {
					;// ignore
				} else if (component instanceof UIStyle) {
					;// ignore
				} else if (component instanceof UIAttribute) {
					UIAttribute uiAttribute = (UIAttribute)component;
					if (uiAttribute.getAttrName().equals("class")) {
						attrclasses.append(attrclasses.length()>0 ? " ":"").append(uiAttribute.getAttrValue());
					} else {
						attributes.append(component.computeTemplate());
					}
				} else {
					children.append(component.computeTemplate());
				}
			}
			
			String tagClass = getTagClass();
			if (attrclasses.indexOf(tagClass) == -1) {
				attrclasses.append(attrclasses.length()>0 ? " ":"").append(tagClass);
			}
			String attrclass = attrclasses.length()>0 ? " class=\""+ attrclasses +"\"":"";
			
			StringBuilder sb = new StringBuilder();
			sb.append("<").append(getTagName())
				.append(attrclass)
				.append(attributes.length()>0 ? attributes:"");
			
			if (isSelfClose()) {
				sb.append("/>").append(System.getProperty("line.separator"));
			}
			else {
				sb.append(">").append(System.getProperty("line.separator"))
					.append(children.length()>0 ? children:"")
				  .append("</").append(getTagName())
				  	.append(">").append(System.getProperty("line.separator"));
			}
			
			return sb.toString();
		}
		return "";
	}

	@Override
	public String computeStyle() {
		StringBuilder styles = new StringBuilder();
		StringBuilder others = new StringBuilder();
		
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIStyle) {
				String tpl = component.computeTemplate();
				if (!tpl.isEmpty()) {
					styles.append(tpl).append(";").append(System.getProperty("line.separator"));
				}
			}
			else if (component instanceof UIElement) {
				String tpl = ((UIElement)component).computeStyle();
				if (!tpl.isEmpty()) {
					others.append(tpl);
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if (styles.length()>0) {
			sb.append("."+ getTagClass()).append(" {").append(System.getProperty("line.separator"));
			sb.append(styles);
			sb.append("}").append(System.getProperty("line.separator"));
		}
		if (others.length()>0) {
			sb.append(others);
		}
		return sb.toString();
	}

}
