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
package name.huliqing.luoying.ui;

import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapFont.VAlign;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 *
 * @author huliqing
 */
public class Text extends AbstractUI {
    
    protected BitmapText bitmapText;
    // 是否已经设置了Box?
    private boolean boxSet;
    // 是否需要重新设置Box
    private boolean needToSetBox;
    
    private Align align;
    private VAlign valign;
    
    public Text(String text) {
        this(text, null);
    }
    
    public Text(String text, ColorRGBA color) {
        bitmapText = new BitmapText(UIFactory.getUIConfig().getFont());
        bitmapText.setSize(UIFactory.getUIConfig().getBodyFontSize());
        bitmapText.setText(text);
        if (color != null) {
            bitmapText.setColor(color);
        }
        this.width = bitmapText.getLineWidth();
        this.height = bitmapText.getHeight();
        attachChild(bitmapText);
        setNeedUpdate();
    }
    
    public String getText() {
        return bitmapText.getText();
    }
    
    public void setText(String text) {
        bitmapText.setText(text);
        setNeedUpdate();
    }
    
    public void setFontSize(float size) {
        bitmapText.setSize(size);
        setNeedUpdate();
    }
    
    public void setFontColor(ColorRGBA color) {
        if (color != null) {
            bitmapText.setColor(color);
            setNeedUpdate();
        }
    }

    /**
     * 设置子字符的颜色
     * @param color 
     * @param regexp 正则表达式 
     */
    public void setSubColor(ColorRGBA color, String regexp) {
        bitmapText.setColor(regexp, color);
        setNeedUpdate();
    }
    
    @Override
    public void resize() {
        this.width = bitmapText.getLineWidth();
        this.height = bitmapText.getHeight();
    }
    
    public void setAlignment(Align align) {
        this.align = align;
        this.needToSetBox = true;
        setNeedUpdate();
    }
    
    public void setVerticalAlignment(VAlign valign) {
        this.valign = valign;
        this.needToSetBox = true;
        setNeedUpdate();
    }

    @Override
    public float getWidth() {
        checkUpdate();
        return width;
    }
    
    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        needToSetBox = true;
    }
    
    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        needToSetBox = true;
    }

    @Override
    public float getHeight() {
        checkUpdate();
        return height;
    }
    
    /**
     * @param lineWrapMode 
     */
    public void setLineWrapMode(LineWrapMode lineWrapMode) {
        bitmapText.setLineWrapMode(lineWrapMode);
        setNeedUpdate();
    }
    
    /**
     * 设置透明度
     * @param alpha 
     */
    public void setAlpha(float alpha) {
        bitmapText.setAlpha(alpha);
        setNeedUpdate();
    }
    
    protected void checkUpdate() {
        if (needUpdate) {
            updateView();
            needUpdate = false;
        }
    }
    
    @Override
    public void updateView() {
        if (needToSetBox) {
            bitmapText.setBox(new Rectangle(0, 0, width, height));
            boxSet = true;
            needToSetBox = false;
        }
        
        if (align != null) {
            bitmapText.setAlignment(align);
            align = null;
        }
        if (valign != null) {
            bitmapText.setVerticalAlignment(valign);
            valign = null;
        }
        
        bitmapText.updateLogicalState(0.16f);

        Vector3f loc = bitmapText.getLocalTranslation();
        bitmapText.setLocalTranslation(loc.set(0, height, 0));
        
        super.updateView();
    }
}
