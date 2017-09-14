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

import java.util.logging.Logger;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import name.huliqing.editor.converter.SimpleFieldConverter;
import name.huliqing.luoying.xml.Converter;

/**
 *
 * @author huliqing
 */
public class ColorConverter extends SimpleFieldConverter {

    private static final Logger LOG = Logger.getLogger(ColorConverter.class.getName());

    // 容器
    private final HBox colorLayout = new HBox();
    
    private final Label colorLabel = new Label("颜色");
    private final ColorPicker colorPicker = new ColorPicker();
    
    private final Label strengthLabel = new Label("强度");
    private final ChoiceBox<Float> strengthChoice = new ChoiceBox<Float>(
            FXCollections.observableArrayList(0.01f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f, 1.2f, 1.5f, 1.8f, 2f, 3f, 3.5f, 4f, 5f, 6f, 10f));
    
    private Color lastColorSaved;
    private float lastStrenth;
    
    public ColorConverter() {
        // 宽度要减少一些，不然会导致宽度BUG。
        colorPicker.prefWidthProperty().bind(root.widthProperty().multiply(0.4f));
        
        // 强度
        strengthChoice.prefHeightProperty().bind(root.widthProperty().multiply(0.4f));
        strengthChoice.setScaleShape(false);
        strengthChoice.setMaxHeight(20);
        
        // 颜色改变时持续更新，但不保存，只有在明确点击后(OnAction)的时候才保存
        // 这可避免用户在颜色面板上滑动改变颜色的时候产生很多历史记录存档。
        colorPicker.valueProperty().addListener((ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
            updateColor(newValue);
        });
        
        strengthChoice.valueProperty().addListener((ObservableValue<? extends Float> observable, Float oldValue, Float newValue) -> {
            updateStrenth(newValue);
        });
        
        // 当在color palette面板上点击选择了颜色及Custom面板上点击“保存”"确定“之后才会确发这个方法
        // 这里要明确保存并记录历史。
        colorPicker.setOnAction((ActionEvent event) -> {
            updateColorAndSave(colorPicker.getValue());
        });
        
        // 当ColorPicker界面失去焦点后检查并保存颜色设置
        // 这是因为可能用户在自定义颜色面板（滑动选择）颜色后直接关闭了面板，即无选择保存和取消行为，
        // 所在这里要特别保存一下.(另外用户可能在自定义颜色面板滑动选择颜色后直接跳转到3D场景，切换选择，这需要在cleanup中处理保存)
        colorPicker.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//             LOG.log(Level.INFO, "ColorPicker, focused listener, oldValue={0}, newValue={1}", new Object[] {oldValue, newValue});
            if (!newValue) {
                updateColorAndSave(colorPicker.getValue());
            }
        });
        
        colorLayout.getChildren().addAll(colorLabel, colorPicker, strengthLabel, strengthChoice);
        
        colorLayout.setSpacing(3);
    }
    
    @Override
    protected Node createLayout() {
        return colorLayout;
    }

    @Override
    public void initialize() {
        super.initialize(); 
    }

    @Override
    public void cleanup() {
        // 用户可能在调节了颜色之后，切换选择了其它物体，这时颜色选择窗口可能还没有保存颜色即被关闭.
        // 这里需要判断颜色是否发生了变化，并在清理的时候保存一次。(注：颜色选择器中的自定义颜色面板虽然是模式的
        // , 但是因为整合了JME（Swing)窗口，所以JFX的模式窗口无法阻止JME窗口的操作。)
        updateColorAndSave(colorPicker.getValue());
        super.cleanup();
    }
    
    private void updateColor(Color color) {
        updateAttribute(toJmeColor(color));
    }
    
    private void updateColorAndSave(Color color) {
        // 颜色没有变化则不需要更新和保存
        if (!checkColorDiff(color, lastColorSaved)) {
            return;
        }
        
        float strenth = 1f;
        if (strengthChoice.getSelectionModel().getSelectedItem() != null) {
        	strenth = strengthChoice.getValue();
        }
        
        // 更新属性
        ColorRGBA newColor = toJmeColor(color).mult(strenth);
        updateAttribute(newColor);
        // 保存历史记录
        ColorRGBA before = toJmeColor(lastColorSaved);
        ColorRGBA after = new ColorRGBA(newColor);
        addUndoRedo(toJmeColor(lastColorSaved), after);
//        LOG.log(Level.INFO, "颜色变化(ColorPicker)，保存颜色修改, before={0}, after={1}", new Object[] {before, after});
        // 注：尽量让lastColorUsed不引用到一个特定的实例
        lastColorSaved = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
    }
    
    /**
     * 更新颜色强度
     * @param color
     */
    private void updateStrenth(float strenth) {
    	Color color = colorPicker.getValue();
        // 更新属性
        ColorRGBA newColor = toJmeColor(color).mult(strenth);
        updateAttribute(newColor);
        // 保存历史记录
        ColorRGBA before = toJmeColor(lastColorSaved);
        ColorRGBA after = new ColorRGBA(newColor);
        addUndoRedo(toJmeColor(lastColorSaved), after);
//        LOG.log(Level.INFO, "颜色变化(ColorPicker)，保存颜色修改, before={0}, after={1}", new Object[] {before, after});
        // 注：尽量让lastColorUsed不引用到一个特定的实例
        lastColorSaved = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
    }
    
    @Override
    protected void updateUI() {
        Object propertyValue = data.getAttribute(field);
        ColorRGBA jmeColor = Converter.getAsColor(propertyValue);
        Color jfxColor = toJfxColor(jmeColor);
        colorPicker.setValue(jfxColor);
        lastColorSaved = new Color(jfxColor.getRed(), jfxColor.getGreen(), jfxColor.getBlue(), jfxColor.getOpacity());
        
        // 默认选择强度
        strengthChoice.getSelectionModel().select(1f);
    }
    
    private boolean checkColorDiff(Color color1, Color color2) {
        if (color1 == color2) {
            return false;
        }
        if (color1 == null && color2 != null) {
            return true;
        }
        if (color1 != null && color2 == null) {
            return true;
        }
        return Double.compare(color1.getRed(), color2.getRed()) != 0 
                || Double.compare(color1.getGreen(), color2.getGreen()) != 0 
                || Double.compare(color1.getBlue(), color2.getBlue()) != 0 
                || Double.compare(color1.getOpacity(), color2.getOpacity()) != 0;
    }
    
    private Color toJfxColor(ColorRGBA jmeColor) {
        if (jmeColor == null) {
            return new Color(1,1,1,1);
        }
        
        return new Color(FastMath.clamp(jmeColor.r, 0, 1)
                , FastMath.clamp(jmeColor.g, 0, 1)
                , FastMath.clamp(jmeColor.b, 0, 1)
                , FastMath.clamp(jmeColor.a, 0, 1));
    }
    
    private ColorRGBA toJmeColor(Color color) {
        if (color == null) {
            return null;
        }
        return new ColorRGBA((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
    }
}
