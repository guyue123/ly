/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.editor.converter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jme3.export.Savable;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.converter.define.FieldDefine;
import name.huliqing.editor.edit.JfxAbstractEdit;
import name.huliqing.editor.manager.ConverterManager;
import name.huliqing.luoying.xml.ObjectData;

/**
 * @author huliqing
 * @param <E>
 * @param <T>
 */
public abstract class DataConverter<E extends JfxAbstractEdit, T extends ObjectData> extends AbstractConverter<E, T, FieldConverter> {
//    private static final Logger LOG = Logger.getLogger(AbstractDataConverter.class.getName());

    /**
     * 指定要隐藏的字段, 格式:"field1,field2,..."
     */
    public final static String FEATURE_HIDE_FIELDS = "hideFields";

    protected Map<String, FieldDefine> fieldDefines;

    protected final Map<String, FieldConverter> fieldConverters = new LinkedHashMap();

    protected final VBox dataScroll = new VBox();
    protected final VBox fieldPanel = new VBox();

    // 子面板，显示子联系的物体
    protected final VBox childPane = new VBox();
    
    // 显示光模型
    protected final Pane childLightPane = new VBox();
    
    // 显示阴影模型
    protected final Pane childShadowPane = new VBox();
    
    protected DataConverter childDataConverter;

    public DataConverter() {
        dataScroll.setId(StyleConstants.ID_PROPERTY_PANEL);
        //dataScroll.setContent(fieldPanel);
        dataScroll.getChildren().add(fieldPanel);
        childPane.managedProperty().bind(childPane.visibleProperty());
        // for debug
//        childPane.setStyle("-fx-border-style: solid inside;-fx-border-color:red;"); 
    }

    @Override
    public Region getLayout() {
        return dataScroll;
    }

    public void setFieldDefines(Map<String, FieldDefine> fieldDefines) {
        this.fieldDefines = fieldDefines;
    }

    public void updateAttribute(String property, Object value) {
        if (value == null) {
            data.setAttribute(property, (Savable) value); // 清除掉参数值. (Savable)用于调用特定的函数
            notifyChanged();
            return;
        }
        
        setAttribute(property, value);
        notifyChanged();
    }

    /**
     * 设置对象属性
     * @param property
     * @param value
     */
	protected void setAttribute(String property, Object value) {
        if (value == null) {
            data.setAttribute(property, (Savable) value); // 清除掉参数值. (Savable)用于调用特定的函数
            return;
        }
        
		if (value instanceof Byte) {
            data.setAttribute(property, (Byte) value);
            
        } else if (value instanceof Short) {
            data.setAttribute(property, (Short) value);
            
        } else if (value instanceof Integer) {
            data.setAttribute(property, (Integer) value);
            
        } else if (value instanceof Float) {
            data.setAttribute(property, (Float) value);
            
        } else if (value instanceof Long) {
            data.setAttribute(property, (Long) value);
            
        } else if (value instanceof Double) {
            data.setAttribute(property, (Double) value);
            
        } else if (value instanceof Boolean) {
            data.setAttribute(property, (Boolean) value);
            
        } else if (value instanceof byte[]) {
            data.setAttribute(property, (byte[]) value);
            
        } else if (value instanceof short[]) {
            data.setAttribute(property, (short[]) value);
            
        } else if (value instanceof int[]) {
            data.setAttribute(property, (int[]) value);
            
        } else if (value instanceof float[]) {
            data.setAttribute(property, (float[]) value);
            
        } else if (value instanceof long[]) {
            data.setAttribute(property, (long[]) value);
            
        } else if (value instanceof double[]) {
            data.setAttribute(property, (double[]) value);
            
        } else if (value instanceof boolean[]) {
            data.setAttribute(property, (boolean[]) value);
            
        } else if (value instanceof Savable) {
            data.setAttribute(property, (Savable) value);
            
        } else if (value instanceof String) {
            data.setAttribute(property, (String) value);
            
        } else if (value instanceof String[]) {
            data.setAttributeStringArray(property, (String[]) value);
            
        } else if (value instanceof List) {
            // 列表类型目前只支持 List<String>和List<Savable>
            List listValue = (List) value;
            if (listValue.isEmpty()) {
//                data.setAttribute(property, (Savable) null); // 清除
                data.setAttributeStringList(property, listValue);
            } else {
                Object itemObject = listValue.get(0);
                if (itemObject instanceof String) {
                    data.setAttributeStringList(property, listValue);
                } else if (itemObject instanceof Savable) {
                    data.setAttributeSavableList(property, listValue);
                } else {
                    throw new UnsupportedOperationException("Unsupport data type of the list! object=" + itemObject);
                }
            }
            
        } else {
            throw new UnsupportedOperationException("Unsupport data type=" + value.getClass());
        }
	}
    
    /**
     * 2017/09/02
     * 更新多个属性
     * @param attrs
     */
    public void updateMultAttributes(Map<String, Object> attrs) {
    	if (attrs == null || attrs.isEmpty()) {
    		return;
    	}
    	
    	Iterator<String> it = attrs.keySet().iterator();
    	while (it.hasNext()) {
    		String k = it.next();
    		setAttribute(k, attrs.get(k));
    	}
    	notifyChanged();
    }

    @Override
    public void initialize() {
        super.initialize();
        // 实体模型属性编辑面板
        jfxEdit.getEditPanel().getChildren().add(childPane);
        childPane.setVisible(false);
        
        // 光模型
        if (!jfxEdit.getLightPanel().getChildren().contains(childLightPane)) {
        	jfxEdit.getLightPanel().getChildren().add(childLightPane);
        }
        
        // 阴影模型
        // jfxEdit.getShadowPanel().getChildren().add(childShadowPane);
        // 显示字段
        displayFields();

        // 隐藏指定的字段
        hideFields();
       // dataScroll.setFitToWidth(true);
       // dataScroll.prefWidthProperty().bind();
    }

    /**
     * 显示字段
     */
	protected void displayFields() {
		if (fieldDefines != null && !fieldDefines.isEmpty()) {
            fieldDefines.values().forEach(t -> {
                FieldConverter pc = ConverterManager.createPropertyConverter(jfxEdit, data, t, this);
                pc.initialize();
                pc.updateView();
                fieldPanel.getChildren().add(pc.getLayout());
                fieldConverters.put(t.getField(), pc);
            });
        }
	}

    /**
     * 隐藏指定的字段
     */
	private void hideFields() {
		List<String> hideFields = featureHelper.getAsList(FEATURE_HIDE_FIELDS);
        if (hideFields != null) {
            hideFields.forEach(t -> {
                FieldConverter pc = fieldConverters.get(t);
                if (pc != null) {
                    // 隐藏的时候要同时把managed设置为false才不会占位
                    pc.getLayout().managedProperty().bind(pc.getLayout().visibleProperty());
                    pc.getLayout().setVisible(false);
                }
            });
        }
	}

    @Override
    public void cleanup() {
        if (childDataConverter != null) {
            if (childDataConverter.isInitialized()) {
                childDataConverter.cleanup();
            }
            childDataConverter = null;
        }
        
        fieldConverters.values().stream().filter(t -> t.isInitialized()).forEach(
                t -> {
                    t.cleanup();
                }
        );
        fieldConverters.clear();
        fieldPanel.getChildren().clear();

        jfxEdit.getEditPanel().getChildren().remove(childPane);
        //jfxEdit.getLightPanel().getChildren().clear();
        super.cleanup();
    }
    
    public void setChildLayout(String childTitle, DataConverter childConverter) {
        if (childDataConverter != null && childDataConverter != childConverter) {
            if (childDataConverter.isInitialized()) {
                childDataConverter.cleanup();
            }
        }
        childDataConverter = childConverter;
        //childPane.setText(childTitle);
        //childPane.setContent(childDataConverter.getLayout());
        childPane.getChildren().clear();
        childPane.getChildren().add(childDataConverter.getLayout());
        childPane.setVisible(true);
    }
    
    public void setLightPane(String childTitle, DataConverter childConverter) {
        // 子面板，显示子联系的物体
/*    	TitledPane lightChildPane = new TitledPane();
    	lightChildPane.setText(childTitle);
    	lightChildPane.setContent(childConverter.getLayout());
    	lightChildPane.setVisible(true);*/
        
    	childLightPane.getChildren().add(childConverter.getLayout());
    }
    
    public void setShadowPane(Pane shadowPane) {
    	jfxEdit.getShadowPanel().getChildren().add(shadowPane);
    }
    
}
