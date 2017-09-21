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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import name.huliqing.editor.component.ComponentDefine;
import name.huliqing.editor.constants.AssetConstants;
import name.huliqing.editor.constants.EntityConstants;
import name.huliqing.editor.constants.ResConstants;
import name.huliqing.editor.converter.DataConverter;
import name.huliqing.editor.converter.FieldConverter;
import name.huliqing.editor.edit.Mode;
import name.huliqing.editor.edit.UndoRedo;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.edit.controls.entity.EntityControlTile;
import name.huliqing.editor.edit.controls.entity.EntityControlTileListener;
import name.huliqing.editor.edit.scene.JfxSceneEdit;
import name.huliqing.editor.edit.scene.JfxSceneEditListener;
import name.huliqing.editor.edit.scene.SceneEdit;
import name.huliqing.editor.manager.ComponentManager;
import name.huliqing.editor.manager.ConverterManager;
import name.huliqing.editor.manager.Manager;
import name.huliqing.editor.ui.utils.JfxUtils;
import name.huliqing.fxswing.Jfx;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.scene.Scene;

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
    
    private static final String lableStyle = "-fx-text-fill: white;-fx-background-color: #199ED8;-fx-background-insets: 3;-fx-background-radius: 5;-fx-padding: 10;";
    
    private static final String btnStyle = "-fx-text-fill: white;-fx-background-color: #ea9d01;-fx-background-insets: 3;-fx-background-radius: 5;-fx-padding: 10;";
    
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
    	
    	fieldPanel.setPadding(new Insets(3));
    	dataScroll.setMargin(fieldPanel, new Insets(2, 0, 2, 0));
		if (fieldDefines != null && !fieldDefines.isEmpty()) {
    		// 标题
    		ComponentDefine cd = ComponentManager.getComponentsById(data.getId());
    		// 模型名称
    		String name = cd.getName();
    		// 模型编号
    		String id = cd.getId();
    		// 头部
    		fieldPanel.getChildren().add(initHeader(name, id));
            fieldDefines.values().forEach(t -> {
                FieldConverter pc = ConverterManager.createPropertyConverter(jfxEdit, data, t, this);
                pc.initialize();
                pc.updateView();
                
            	// 如果是entitySimpleModel
            	if (EntityConstants.ENTITY_SIMPLE_MODEL.equals(data.getTagName())) {
            		List<String> hideFields = featureHelper.getAsList(FEATURE_HIDE_FIELDS);
            		if (!hideFields.contains(t.getField())) {
	            		// 模型尺寸
            			fieldPanel.getChildren().add(pc.getLayout());
            		}
            	} else {
            		fieldPanel.getChildren().add(pc.getLayout());
            	}
                
                // 隐藏字段使用
                fieldConverters.put(t.getField(), pc);
            });
        }
	}

    /**
     * 生成模型头部
     * @param name 名字
     * @param id ID
     * @return
     */
	protected HBox initHeader(String name, String id) {
		// 显示，隐藏/显示
		HBox hBox = new HBox();
		
		// 实体模型头部
		Label label2 = new Label(name + " （编号：" + id + "）");
		label2.setBackground(null);
		label2.setStyle(lableStyle);
		
		if (data.isTypeof(EntityConstants.ENTITY_SIMPLE_MODEL)) {
			label2 = new Label(name + " （编号：" + id + "）");
			label2.setBackground(null);
			label2.setStyle(lableStyle);
			hBox.getChildren().addAll(label2);
		} else  if(data.isTypeof(EntityConstants.ENTITY_DIRECTIONAL_LIGHT)) {
			ComponentDefine cdDL = ComponentManager.getComponentsById(EntityConstants.ENTITY_DIRECTIONAL_LIGHT);
			label2 = new Label(name);
			label2.setGraphic(JfxUtils.createImage(cdDL.getIcon(), 20, 20));
			label2.setContentDisplay(ContentDisplay.RIGHT);
			label2.setGraphicTextGap(10);
			label2.setStyle(lableStyle);
			// 删除按钮
			Button removeBtn = removeAction();
			// 切换显示隐藏
			ToggleButton enableBtn = enableAction();
			hBox.getChildren().addAll(label2, removeBtn, enableBtn);
			
		} else  if(data.isTypeof(EntityConstants.ENTITY_POINT_LIGHT)) {
			ComponentDefine cdPL = ComponentManager.getComponentsById(EntityConstants.ENTITY_POINT_LIGHT);
			
			label2 = new Label(name);
			label2.setGraphic(JfxUtils.createImage(cdPL.getIcon(), 20, 20));
			label2.setContentDisplay(ContentDisplay.RIGHT);
			label2.setGraphicTextGap(10);
			label2.setStyle(lableStyle);
			
			// 选择灯光
			label2.setOnMouseClicked(t -> {
				jfxEdit.setSelected(data);
	        });
			
			// 删除按钮
			Button removeBtn = removeAction();
			// 切换显示隐藏
			ToggleButton enableBtn = enableAction();
			hBox.getChildren().addAll(label2, removeBtn, enableBtn);
		} else {
			hBox.getChildren().addAll(label2);
		}

		fieldPanel.setMargin(hBox, new Insets(0, 0, 2, 0));
		return hBox;
	}

	/**
	 * 隐藏/显示
	 * @return
	 */
	protected ToggleButton enableAction() {
		ToggleButton enableBtn = new ToggleButton("", JfxUtils.createIcon(AssetConstants.INTERFACE_ICON_EYE));
		enableBtn.setStyle(btnStyle);
		enableBtn.setTooltip(new Tooltip("开灯/关灯"));
        enableBtn.setSelected(data.getAsBoolean("enabled", true));
        enableBtn.setAlignment(Pos.CENTER_RIGHT);
        enableBtn.setPrefWidth(16);
        enableBtn.selectedProperty().addListener((ObservableValue<? extends Boolean> ob, Boolean oldValue, Boolean newValue) -> {
            Scene scene = jfxEdit.getJmeEdit().getScene();
            if (scene == null)
                return;
            Jfx.runOnJme(() -> {
                Entity entity = scene.getEntity(data.getUniqueId());
                if (entity != null) {
                    entity.setEnabled(newValue);
                }
            });
        });
		return enableBtn;
	}

	/**
	 * 删除动作
	 * @return
	 */
	protected Button removeAction() {
		Button removeBtn = new Button("", JfxUtils.createIcon(AssetConstants.INTERFACE_ICON_SUBTRACT));
		removeBtn.setStyle(btnStyle);
		removeBtn.setTooltip(new Tooltip("删除此灯光"));
		removeBtn.setOnAction(c -> {
      	  // 删除确认
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setHeaderText(Manager.getRes(ResConstants.MODEL_DELETE));
            alert.setContentText(Manager.getRes(ResConstants.MODEL_DELETE_CONFIRM));
            
            Optional<ButtonType> result = alert.showAndWait();
            ButtonType bt = result.get();
            // 取消关闭
            if (bt.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                return;
            }
      	  
            Scene scene = jfxEdit.getJmeEdit().getScene();
            if (scene == null)
                return;
            
            Jfx.runOnJme(() -> {
                List<EntityControlTile> ectsRemove = new ArrayList(1);
                SceneEdit se = jfxEdit.getJmeEdit();
                EntityControlTile ect = se.getEntityControlTile(data);
                if (ect != null) {
                    ectsRemove.add(ect);
                }
                ectsRemove.add(ect);
                EntityRemovedUndoRedo erur = new EntityRemovedUndoRedo(ectsRemove);
                erur.redo();
                se.addUndoRedo(erur);
                
                dataScroll.managedProperty().bind(dataScroll.visibleProperty());
                dataScroll.setVisible(false);
                dataScroll.getChildren().clear();
            });
        });
		return removeBtn;
	}
	
    
    private class EntityRemovedUndoRedo implements UndoRedo {
        private final List<EntityControlTile> ectRemoved = new ArrayList();
        public EntityRemovedUndoRedo(List<EntityControlTile> ectRemoved) {
            this.ectRemoved.addAll(ectRemoved);
        }
        @Override
        public void undo() {
            SceneEdit se = jfxEdit.getJmeEdit();
            for (int i = ectRemoved.size() - 1; i >= 0; i--) {
                se.addControlTile(ectRemoved.get(i));
            }
        }
        @Override
        public void redo() {
            SceneEdit se = jfxEdit.getJmeEdit();
            for (int i = 0; i < ectRemoved.size(); i++) {
                se.removeControlTile(ectRemoved.get(i));
            }
        }
    }
}
