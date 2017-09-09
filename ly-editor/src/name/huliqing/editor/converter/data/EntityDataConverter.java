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
package name.huliqing.editor.converter.data;

import java.text.DecimalFormat;
import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import name.huliqing.editor.component.ComponentDefine;
import name.huliqing.editor.constants.EntityConstants;
import name.huliqing.editor.converter.DataConverter;
import name.huliqing.editor.converter.FieldConverter;
import name.huliqing.editor.edit.Mode;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.edit.controls.entity.EntityControlTile;
import name.huliqing.editor.edit.controls.entity.EntityControlTileListener;
import name.huliqing.editor.edit.scene.JfxSceneEdit;
import name.huliqing.editor.edit.scene.JfxSceneEditListener;
import name.huliqing.editor.manager.ComponentManager;
import name.huliqing.editor.manager.ConverterManager;
import name.huliqing.fxswing.Jfx;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 场景实体转换器
 * @author huliqing
 */
public class EntityDataConverter extends DataConverter<JfxSceneEdit, EntityData> 
        implements JfxSceneEditListener<ControlTile>, EntityControlTileListener{
//    private static final Logger LOG = Logger.getLogger(EntityDataConverter.class.getName());

    private EntityControlTile selectObj;
    
    private Vector3f modelSize;
    
    public static DecimalFormat df =new DecimalFormat("#.00");
    
    @Override
    public void initialize() {
        super.initialize();
        this.jfxEdit.addListener(this);
    }

    @Override
    public void cleanup() {
        if (selectObj != null) {
            selectObj.removeListener(this);
        }
        jfxEdit.removeListener(this);
        super.cleanup(); 
    }
    
    @Override
    public void notifyChanged() {
        jfxEdit.reloadEntity(data);
        jfxEdit.setModified(true);
        super.notifyChanged();
    }

    @Override
    public void onModeChanged(Mode mode) {
        // ignore
    }

    @Override
    public void onSelectChanged(ControlTile newSelectObj) {
        if (!(newSelectObj instanceof EntityControlTile)) {
            return;
        }
        EntityControlTile newEso = (EntityControlTile) newSelectObj;
        if (newEso.getTarget().getData() != this.data) {
            return;
        }
        if (selectObj != null) {
            selectObj.removeListener(this);
        }
        selectObj = newEso;
        selectObj.addListener(this);
    }

    @Override
    public void onEntityAdded(EntityData entity) {
        // ignore
    }

    @Override
    public void onEntityRemoved(EntityData ed) {
        // ignore
    }

    @Override
    public void onPropertyChanged(EntityData data, String property, Object value) {
        if (this.data != data) {
            return;
        }
        Jfx.runOnJfx(() -> {
            FieldConverter pc = fieldConverters.get(property);
            if (pc != null) {
                pc.updateView();
//                LOG.log(Level.INFO, "onPropertyChanged, data={0}, property={1}, value={2}", new Object[] {data.getId(), property, value});
            }
        });
    }
    
    /**
     * 显示字段
     */
    @Override
	protected void displayFields() {
    	List<Entity> entities = jfxEdit.getJmeEdit().getScene().getEntities();
    	
    	for (Entity et : entities) {
    		if (et.getEntityId() == data.getUniqueId()) {
    			Spatial sp = et.getSpatial();
    			modelSize = ModelManager.getInstance().getSize(sp);
    			break;
    		}
    	}
    	
    	
		if (fieldDefines != null && !fieldDefines.isEmpty()) {
            
    		// 标题
    		ComponentDefine cd = ComponentManager.getComponentsById(data.getId());
    		// 模型名称
    		String name = cd.getName();
    		// 模型编号
    		String id = cd.getId();
    		Label label2 = new Label(id + " " + name);
    		label2.setBackground(null);
    		label2.setStyle("-fx-text-fill: white;-fx-background-color: #199ED8;-fx-background-insets: 3;-fx-background-radius: 5;-fx-padding: 10;");
    		fieldPanel.getChildren().add(label2);
			
            fieldDefines.values().forEach(t -> {
                FieldConverter pc = ConverterManager.createPropertyConverter(jfxEdit, data, t, this);
                pc.initialize();
                pc.updateView();
                
            	// 如果是entitySimpleModel
            	if (EntityConstants.ENTITY_SIMPLE_MODEL.equals(data.getTagName())) {
            		List<String> hideFields = featureHelper.getAsList(FEATURE_HIDE_FIELDS);
            		if (!hideFields.contains(t.getField())) {
	            		// 模型图片
            			
	            		// 模型尺寸
            			if ("scale".equals(t.getField()) && modelSize != null) {
            				HBox yLayout = new HBox();
            				Label labelX = new Label(" 长 ");
            				labelX.setStyle("-fx-padding: 4;");
            				
            				TextField xField = new TextField(String.valueOf(df.format(modelSize.getX() * data.getScale().getX())));
            				xField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
            				xField.setPromptText("长度(厘米)");
            				xField.setPrefWidth(90);
            				
            				Label labelY = new Label(" 宽 ");
            				labelY.setStyle("-fx-padding: 4;");
            				
            				TextField yField = new TextField(String.valueOf(df.format(modelSize.getY() * data.getScale().getY())));
            				yField.setPromptText("宽度(厘米)");
            				yField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
            				yField.setPrefWidth(90);
            				
            				Label labelZ = new Label(" 高 ");
            				labelZ.setStyle("-fx-padding: 4;");
            				
            				TextField zField = new TextField(String.valueOf(df.format(modelSize.getZ() * data.getScale().getZ())));
            				zField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
            				zField.setPromptText("高度(厘米)");
            				zField.setPrefWidth(90);
            				
            				yLayout.getChildren().addAll(labelX, xField, labelY, yField, labelZ, zField);
            				yLayout.setPadding(new Insets(10, 10, 10, 10));
            				//fieldPanel.getChildren().add(yLayout);
            				fieldPanel.getChildren().add(pc.getLayout());
            			} else {
            				fieldPanel.getChildren().add(pc.getLayout());
            			}
            			
	            		// fieldPanel.getChildren().add(pc.getLayout());
            		}
            	} else {
            		fieldPanel.getChildren().add(pc.getLayout());
            	}
                
                // 隐藏字段使用
                fieldConverters.put(t.getField(), pc);
            });
        }
	}
}
