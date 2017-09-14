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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.converter.SimpleFieldConverter;
import name.huliqing.luoying.xml.Converter;

/**
 *
 * @author huliqing
 */
public class DirectionalLightConverter extends SimpleFieldConverter{
    
    private final VBox layout = new VBox();
    
/*    private final HBox xLayout = new HBox();
    private final Label xLabel = new Label("X");
    private final TextField xField = new TextField("");
    
    private final HBox yLayout = new HBox();
    private final Label yLabel = new Label("Y");
    private final TextField yField = new TextField("");
    
    private final HBox zLayout = new HBox();
    private final Label zLabel = new Label("Z");
    private final TextField zField = new TextField("");*/
    
    // 照射参数
    private final HBox dlLayout = new HBox();
    // 照射方向
    private final Label directLabel = new Label("照射方向");
    private final ChoiceBox<String> directChoice = new ChoiceBox<String>(
            FXCollections.observableArrayList("东", "南", "西", "北", "东南", "东北", "西南", "西北"));
    
    // 照射角度
    private final Label angleLabel = new Label("照射角度");
    private final ChoiceBox<Float> angleChoice = new ChoiceBox<Float>(
            FXCollections.observableArrayList(angles));
    
    // 方位
    private final static Map<String, Vector3f> vector3fMap = new LinkedHashMap();
    // 度数
    private final static List<Float> angles = new ArrayList(Arrays.asList(0f, 5f, 10f, 15f, 30f, 45f, 60f, 75f, 90f));
    
    // 旋转轴
    private static Vector3f rotateAxis = new Vector3f(0, -1, 0);
    // 当前选择的角度
    private float resultAngle = FastMath.PI * 90 / 180;
    
    private String lastX = "";
    private String lastY = "";
    private String lastZ = "";
    private Vector3f lastValue;
    
    private boolean hasInited = false; 
    
    static {
        // 东(1, 0, 0), 南(0 , 0, 1), 西(-1, 0, 0), 北(0,0,-1)
        // 东南(1, 0, 1), 东北(1, 0, -1)， 西南(-1, 0, 1), 西北(-1, 0, -1)
    	vector3fMap.put("东", new Vector3f(-1, 0, 0));
    	vector3fMap.put("南", new Vector3f(0, 0, -1));
    	vector3fMap.put("西", new Vector3f(1, 0, 0));
    	vector3fMap.put("北", new Vector3f(0 , 0, 1));
    	vector3fMap.put("东南", new Vector3f(-1, 0, -1));
    	vector3fMap.put("东北", new Vector3f(-1, 0, 1));
    	vector3fMap.put("西南", new Vector3f(1, 0, -1));
    	vector3fMap.put("西北", new Vector3f(1, 0, 1));
    }
    
    private final ChangeListener<? super Number> changeListener = (ObservableValue<? extends Number> observable
            , Number oldValue, Number newValue) -> {
        if (newValue.intValue() == oldValue.intValue()) {
            return;
        }
       // updateAndSave();
    };
    
    private class ChoiceChangeListener<T> implements ChangeListener<T> {
    	private String source;
    	
    	public ChoiceChangeListener(String s) {
    		source = s;
    	}

		@Override
		public void changed(ObservableValue<? extends T> arg0, T oldValue, T newValue) {
			if (!(oldValue instanceof Number)) {
				return;
			}
			
			int newV = ((Number)newValue).intValue();
			int oldV = ((Number)oldValue).intValue();
	        if (newV == oldV) {
	            return;
	        }
	        
	        updateAndSave(source, oldV, newV);
		}

    	
    }
    
    public DirectionalLightConverter() {
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
        
        xField.focusedProperty().addListener(focusedListener);
        yField.focusedProperty().addListener(focusedListener);
        zField.focusedProperty().addListener(focusedListener);
        xField.setOnKeyPressed(keyHandler);
        yField.setOnKeyPressed(keyHandler);
        zField.setOnKeyPressed(keyHandler);
        
        xLabel.setMinWidth(15);
        yLabel.setMinWidth(15);
        zLabel.setMinWidth(15);*/
        
        directChoice.getSelectionModel().selectedIndexProperty().addListener(new ChoiceChangeListener("direct"));
        angleChoice.getSelectionModel().selectedIndexProperty().addListener(new ChoiceChangeListener("angle"));
        directLabel.setMinWidth(45);
        angleLabel.setMinWidth(45);
        
        dlLayout.getChildren().addAll(directLabel, directChoice, angleLabel, angleChoice);
        layout.getChildren().add(dlLayout);
        
        layout.setSpacing(3);
    }

    @Override
    protected Node createLayout() {
        return layout;
    }
    
    private void updateAndSave(String source, int oldV, int newV) {
        boolean changed = false;
        
        // 跟据方向和角度计算x, y, z
        String direct = "东";
        float angle = 60f;
        if ("direct".equals(source)) {
        	direct = directChoice.getItems().get(newV);
        	angle = angleChoice.getSelectionModel().getSelectedItem();
        } else  if("angle".equals(source)) {
        	direct = directChoice.getSelectionModel().getSelectedItem();
        	angle = angleChoice.getItems().get(newV);
        } else {
        	return;
        }
        
        if (!vector3fMap.containsKey(direct)) {
        	direct = caculateDirect();
        }
        
        // 东(1, 0, 0), 南(0 , 0, 1), 西(-1, 0, 0), 北(0,0,-1)
        // 东南(1, 0, 1), 东北(1, 0, -1)， 西南(-1, 0, 1), 西北(-1, 0, -1)
        // 旋转轴(0, -1, 0)
        Vector3f rotatedV3f = rotateV3f(direct, angle);
        if ("".equals(lastX) || "".equals(lastY) || "".equals(lastZ)) {
        	setLastVal(rotatedV3f);
        }
        
        if (FastMath.abs(rotatedV3f.getX() - Float.parseFloat(lastX)) > 0.001) {
        	lastX = String.valueOf(rotatedV3f.getX());
        	changed = true;
        }
        
        if (FastMath.abs(rotatedV3f.getY() - Float.parseFloat(lastY)) > 0.001) {
        	lastY = String.valueOf(rotatedV3f.getY());
        	changed = true;
        }
        
        if (FastMath.abs(rotatedV3f.getZ() - Float.parseFloat(lastZ)) > 0.001) {
        	lastZ = String.valueOf(rotatedV3f.getZ());
        	changed = true;
        }
        
/*        if (!xField.getText().equals(lastX)) {
            lastX = xField.getText();
            changed = true;
        }
        if (!yField.getText().equals(lastY)) {
            lastY = yField.getText();
            changed = true;
        }
        if (!zField.getText().equals(lastZ)) {
            lastZ = zField.getText();
            changed = true;
        }*/

        if (changed) {
            
            try {
                Vector3f newVec = new Vector3f(Float.parseFloat(lastX), Float.parseFloat(lastY),Float.parseFloat(lastZ));
                updateAttribute(newVec);
                addUndoRedo(lastValue, new Vector3f(newVec));
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

    @Override
    protected void updateUI() {
    	if (!hasInited) {
    		hasInited = true;
    		
            Object propertyValue = data.getAttribute(field);
            lastValue = Converter.getAsVector3f(propertyValue);
            if (lastValue != null) {
                /*xField.setText(lastValue.x + "");
                yField.setText(lastValue.y + "");
                zField.setText(lastValue.z + "");*/
            	
            	String direct = caculateDirect();
            	float matchAngle = caculateAngle(resultAngle);
            	
            	angleChoice.getSelectionModel().select(matchAngle);
            	directChoice.getSelectionModel().select(direct);
            	
            } else {
                /*xField.setText("");
                yField.setText("");
                zField.setText("");*/
            	angleChoice.getSelectionModel().select(60.0f);
            	directChoice.getSelectionModel().select("东");
            	// 东方角度60度
            	lastValue = new Vector3f(1, -1, 0).mult(new Vector3f(FastMath.cos(FastMath.PI * 60/180), FastMath.sin(FastMath.PI * 60/180),FastMath.cos(FastMath.PI * 60/180)));
            }
            setLastVal(lastValue);
    	}
    }

    /**
     * 设置历史值
     * @param lastValue
     */
	protected void setLastVal(Vector3f lastValue) {
		lastX = String.valueOf(lastValue.x);
        lastY = String.valueOf(lastValue.y);
        lastZ = String.valueOf(lastValue.z);
	}

    /**
     * 计算方向
     * @return
     */
	protected String caculateDirect() {
		// 通过向量判断方向,向量和八个方位之间的角度最小值为当前角度和方向
		String direct = "东";
		Iterator<String> it = vector3fMap.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			
			float rAngle = lastValue.angleBetween(vector3fMap.get(k));
			if (rAngle < resultAngle) {
				resultAngle = rAngle;
				direct = k;
			}
		}
		return direct;
	}

    /**
     * 计算当前选择的角度
     * @param resultAngle
     * @return
     */
	protected float caculateAngle(float resultAngle) {
		// 计算度数
		float angle = 180 * resultAngle/FastMath.PI;
		float diff = 180;
		float matchAngle = angle;
		for (float a : angles) {
			if (FastMath.abs(angle - a) < diff) {
				diff =  FastMath.abs(angle - a);
				matchAngle = a;
			}
		}
		return matchAngle;
	}
    
    
    private static Vector3f angle2V3f(float angle) {
    	float cos = FastMath.cos(-FastMath.PI * angle/180);
    	return new Vector3f(cos, cos, cos);
    }
    
    /**
     * 旋转角度
     * @param start
     * @param angle
     * @return
     */
    private static Vector3f rotateAngle(Vector3f start, float angle) {
    	return start.mult(angle2V3f(angle));
    }
    
    /**
     * 旋转向量:跟据方向和角度旋转
     * @param direct 方向
     * @param angle 角度
     * @return
     */
    private static Vector3f rotateV3f(String direct, float angle) {
    	float ag = FastMath.PI * angle/ 180;
    	return vector3fMap.get(direct).add(rotateAxis).mult(new Vector3f(FastMath.cos(ag), FastMath.sin(ag), FastMath.cos(ag)));
    }
    
    public static void main(String[] args) {
    	float angle = FastMath.PI * 90/ 180;
    	Vector3f newv3f = vector3fMap.get("东").add(rotateAxis).mult(new Vector3f(FastMath.cos(angle), FastMath.sin(angle), FastMath.cos(angle)));
    	System.out.println(newv3f);
    }
    
}
