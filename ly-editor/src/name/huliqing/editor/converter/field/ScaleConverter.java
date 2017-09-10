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
package name.huliqing.editor.converter.field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.DataFormatConstants;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.converter.SimpleFieldConverter;
import name.huliqing.editor.edit.scene.JfxSceneEdit;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.xml.Converter;

/**
 *
 * @author huliqing
 */
public class ScaleConverter extends SimpleFieldConverter{
    
    private final VBox layout = new VBox();
    private final HBox sizeLayout = new HBox();
    private final HBox controlLayout = new HBox();
    
    private final HBox xLayout = new HBox();
    private final Label xLabel = new Label("长");
    private final TextField xField = new TextField("");
    
    private final HBox yLayout = new HBox();
    private final Label yLabel = new Label("高");
    private final TextField yField = new TextField("");
    
    private final HBox zLayout = new HBox();
    private final Label zLabel = new Label("宽");
    private final TextField zField = new TextField("");
    
    private String lastX = "";
    private String lastY = "";
    private String lastZ = "";
    private Vector3f lastValue;
    
    private final CheckBox sameScale = new CheckBox("等比缩放");
    
    private final ChangeListener<Boolean> focusedListener = (ObservableValue<? extends Boolean> observable
            , Boolean oldValue, Boolean newValue) -> {
        if (newValue) {
            return;
        }
        updateAndSave();
    };
    private final EventHandler<KeyEvent> keyHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if (event.getCode() == KeyCode.ENTER) {
                updateAndSave();
            }
        }
    };
    
    public ScaleConverter() {
        layout.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        /*xLayout.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        yLayout.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        zLayout.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        
        layout.getChildren().addAll(xLayout, yLayout, zLayout);
        xLayout.getChildren().addAll(xLabel, xField);
        yLayout.getChildren().addAll(yLabel, yField);
        zLayout.getChildren().addAll(zLabel, zField);
        
        xLayout.setAlignment(Pos.CENTER_LEFT);
        yLayout.setAlignment(Pos.CENTER_LEFT);
        zLayout.setAlignment(Pos.CENTER_LEFT);
        */
        xField.focusedProperty().addListener(focusedListener);
        yField.focusedProperty().addListener(focusedListener);
        zField.focusedProperty().addListener(focusedListener);
        xField.setOnKeyPressed(keyHandler);
        yField.setOnKeyPressed(keyHandler);
        zField.setOnKeyPressed(keyHandler);
        
        xLabel.setMinWidth(25);
        yLabel.setMinWidth(25);
        zLabel.setMinWidth(25);
        
        sizeLayout.getChildren().addAll(xLabel, xField, yLabel, yField, zLabel, zField);
        controlLayout.getChildren().add(sameScale);
        
        layout.getChildren().addAll(sizeLayout, controlLayout);
        
        sizeLayout.setPadding(new Insets(10, 10, 10, 10));
        controlLayout.setPadding(new Insets(10, 10, 10, 20));
        layout.setPadding(new Insets(10, 10, 10, 10));
        
        setLayoutStyle();
    }
    
    private void setLayoutStyle() {
    	xLabel.setStyle("-fx-padding: 4;");
		xField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
		xField.setPromptText("长度(厘米)");
		xField.setPrefWidth(90);
		
		yLabel.setStyle("-fx-padding: 4;");
		yField.setPromptText("宽度(厘米)");
		yField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
		yField.setPrefWidth(90);
		
		zLabel.setStyle("-fx-padding: 4;");
		zField.setStyle("-fx-border-color: blue;-fx-border-width: 1px;-fx-background-radius: 5;");
		zField.setPromptText("高度(厘米)");
		zField.setPrefWidth(90);
		
		sameScale.setSelected(true);
    }

    @Override
    protected Node createLayout() {
        return layout;
    }
    
    private void updateAndSave() {
    	// 数字检查
    	if (!isNum(xField.getText())) {
    		xField.setText(lastX);
    		return;
    	}
    	
    	if (!isNum(yField.getText())) {
    		xField.setText(lastY);
    		return;
    	}
    	
    	if (!isNum(zField.getText())) {
    		xField.setText(lastZ);
    		return;
    	}
    	
        boolean changed = false;
        // 缩放比例
        float ratioX = 1;
        float ratioY = 1;
        float ratioZ = 1;
        
        float oldX = Float.parseFloat(lastX);
        float oldY = Float.parseFloat(lastY);
        float oldZ = Float.parseFloat(lastZ);
        
        if (!xField.getText().equals(lastX)) {
        	ratioX = Float.parseFloat(xField.getText()) / Float.parseFloat(lastX);
            lastX = xField.getText();
            changed = true;
        }
        if (!yField.getText().equals(lastY)) {
        	ratioY = Float.parseFloat(yField.getText()) / Float.parseFloat(lastY);
            lastY = yField.getText();
            changed = true;
        }
        if (!zField.getText().equals(lastZ)) {
        	ratioZ = Float.parseFloat(zField.getText()) / Float.parseFloat(lastZ);
            lastZ = zField.getText();
            changed = true;
        }
        
        // 是否等比例缩放，计算缩放尺寸
        if (sameScale.isSelected()) {
        	if (ratioX != 1) {
        		ratioY = ratioX;
        		ratioZ = ratioX;
        		ratioX = 1;
        	} else if (ratioY != 1) {
        		ratioX = ratioY;
        		ratioZ = ratioY;
        		ratioY = 1;
        	} else if (ratioZ != 1) {
        		ratioY = ratioZ;
        		ratioX = ratioZ;
        		ratioZ = 1;
        	}
        	lastX = DataFormatConstants.DECIMAL_FORMAT_2.format(ratioX * Float.parseFloat(lastX));
        	lastY = DataFormatConstants.DECIMAL_FORMAT_2.format(ratioY * Float.parseFloat(lastY));
        	lastZ = DataFormatConstants.DECIMAL_FORMAT_2.format(ratioZ * Float.parseFloat(lastZ));
        	xField.setText(String.valueOf(lastX));
        	yField.setText(String.valueOf(lastY));
        	zField.setText(String.valueOf(lastZ));
        }
        
        if (changed) {
            
            try {
                Vector3f newVec = new Vector3f(Float.parseFloat(lastX), Float.parseFloat(lastY),Float.parseFloat(lastZ));
                Map<String, Object> updateAttrs = new HashMap<>();
                Vector3f modelSize = getModelSize();
                // 读取的是厘米，转换成比例
                updateAttrs.put(field, newVec.divide(modelSize));

                // 模型中心位置
                Vector3f modelCenter = getModelCenter();
                
                // 模型的旋转角度
                Vector3f modelRotation = getModelRotation();
                
                // 保持模型位置
            	Vector3f location = getEntityLocation();
            	
            	// X,Z轴跟据比例计算缩放后的新的中心位置
            	float newX = modelCenter.getX() * (newVec.getX() / oldX - 1);
            	// 保持Y轴
            	//float newY = location.getY();
            	float newZ = modelCenter.getZ() * (newVec.getZ() / oldZ - 1);
            	
            	// 水平旋转角度，以模型为中心旋转模型角度
            	Vector2f rotate2 = new Vector2f(newX, newZ);
            	rotate2.rotateAroundOrigin(modelRotation.getY(), true);

            	// 水平偏移量
            	Vector3f globalLoc3 = new Vector3f(rotate2.getX(), 0, rotate2.getY());
            	
            	// 通过方位和水平位移移动3D模型
            	Vector3f newLocation = location.subtract(globalLoc3);
        
            	updateAttrs.put("location", newLocation);
                super.updateMultAttibutes(updateAttrs);
                // 重做/撤销
                undoRedo(newVec, modelSize, location, newLocation);
                lastValue = new Vector3f(newVec);
            } catch (NumberFormatException e) {
                // 如果全为空则清除该参数
                if (lastX.equals("") && lastY.equals("") && lastZ.equals("")) {
                    updateAttribute(null);
                    addUndoRedo(lastValue, null);
                    lastValue = null;
                }
            }
        }
    }

    /**
     * 重做/撤回
     * @param newVec
     * @param modelSize
     * @param location
     * @param newLocation
     */
	protected void undoRedo(Vector3f newVec, Vector3f modelSize, Vector3f location, Vector3f newLocation) {
		// 存储原始数据
		List<Map<String, Object>> props = new ArrayList<>();
		// 缩放比例
		Map<String, Object> p1 = new HashMap<>();
		p1.put("property", field);
		p1.put("before", lastValue.divide(modelSize));
		p1.put("after", new Vector3f(newVec).divide(modelSize));
		// 坐标
		Map<String, Object> p2 = new HashMap<>();
		p2.put("property", "location");
		p2.put("before", location);
		p2.put("after", newLocation);
		props.add(p1);
		props.add(p2);
		
		addUndoRedo(props);
	}
	
	/**
	 * 检查输入是否为正确的数字
	 * @param val
	 * @return
	 */
	private boolean isNum(String val) {
		try {
			float v = Float.parseFloat(val);
			DataFormatConstants.DECIMAL_FORMAT_2.format(v);
			return true;
		} catch (NumberFormatException e) {
		}
		
		return false;
	}

    @Override
    protected void updateUI() {
        Object propertyValue = data.getAttribute(field);
        // 存储的是比例，转换成尺寸厘米
        lastValue = Converter.getAsVector3f(propertyValue).mult(getModelSize());
        if (lastValue == null) {
        	lastValue = getModelSize();
        }
        
        if (lastValue != null) {
            xField.setText(DataFormatConstants.DECIMAL_FORMAT_2.format(lastValue.x) + "");
            yField.setText(DataFormatConstants.DECIMAL_FORMAT_2.format(lastValue.y) + "");
            zField.setText(DataFormatConstants.DECIMAL_FORMAT_2.format(lastValue.z) + "");
        } else {
            xField.setText("");
            yField.setText("");
            zField.setText("");
        }
        lastX = xField.getText();
        lastY = yField.getText();
        lastZ = zField.getText();
    }
    
    /**
     * 获得当前模型的尺寸
     * @return
     */
    private  Vector3f getModelSize() {
    	List<Entity> entities = ((JfxSceneEdit)jfxEdit).getJmeEdit().getScene().getEntities();
    	 Vector3f modelSize = null;
    	for (Entity et : entities) {
    		if (et.getEntityId() == data.getUniqueId()) {
    			Spatial sp = et.getSpatial();
    			modelSize = ModelManager.getInstance().getSize(sp);
    			return modelSize;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * 获得模型的中心位置
     * @return
     */
    private Vector3f getModelCenter() {
    	List<Entity> entities = ((JfxSceneEdit)jfxEdit).getJmeEdit().getScene().getEntities();
	   	Vector3f modelCenter = null;
	   	for (Entity et : entities) {
	   		if (et.getEntityId() == data.getUniqueId()) {
	   			Spatial sp = et.getSpatial();
	   			modelCenter = ModelManager.getInstance().getModelCenter(sp);
	   			return modelCenter;
	   		}
	   	}
	   	
	   	return null;
    }
    
    /**
     * 获得模型的旋转角度
     * @return
     */
    private Vector3f getModelRotation() {
    	Quaternion quaternion = ((EntityData)data).getRotation();
    	if (quaternion == null) {
    		return new Vector3f();
    	}
    	
    	 float[] temp = new float[3];
    	quaternion.toAngles(temp);
    	
    	return new Vector3f(temp[0], temp[1], temp[2]);
    }
    
    /**
     * 获得实体位置信息
     * @return
     */
    private Vector3f getEntityLocation() {
    	return ((EntityData)data).getLocation();
    }
}
