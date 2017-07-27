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

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;

/**
 * 按钮
 * @author huliqing
 */
public class Button extends AbstractUI {

    private Text text;
    private boolean disabled;
    
    public Button(String text) {
        super(UIFactory.getUIConfig().getButtonWidth(), UIFactory.getUIConfig().getButtonHeight());
        createText(text);
    }

    public Button(String text, float width, float height) {
        super(width, height);
        createText(text);
    }
    
    private void createText(String str) {
        this.text = new Text(str);
        this.text.setFontColor(UIFactory.getUIConfig().getButtonFontColor());
        this.text.setFontSize(UIFactory.getUIConfig().getButtonFontSize());
        
        setBackground(UIFactory.getUIConfig().getButtonBgFile(), true);
        setBackgroundColor(UIFactory.getUIConfig().getButtonBgColor(), true);
        attachChild(this.text);
        
        if (this.text.getWidth() > width) {
            this.width = this.text.getWidth();
        }
        if (this.text.getHeight() > height) {
            this.height = this.text.getHeight();
        }
    }
    
    public void setFontSize(float fontSize) {
        this.text.setFontSize(fontSize);
        setNeedUpdate();
    }
    
    public void setFontColor(ColorRGBA color) {
        this.text.setFontColor(color);
        setNeedUpdate();
    }
    
    @Override
    public void updateView() {
        super.updateView();
       
        text.setWidth(width);
        text.setHeight(height);
        text.setVerticalAlignment(BitmapFont.VAlign.Center);
        text.setAlignment(BitmapFont.Align.Center);
        
        text.updateView();
        
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean fireClick(boolean isPressed) {
        if (disabled) {
            return false;
        }
        return super.fireClick(isPressed); 
    }

    @Override
    public boolean fireDBClick(boolean isPressed) {
        if (disabled) {
            return false;
        }
        return super.fireDBClick(isPressed); 
    }

    @Override
    protected void clickEffect(boolean isPressed) {
        if (background != null) {
            background.setColor(isPressed ? 
                    UIFactory.getUIConfig().getActiveColor() : 
                    UIFactory.getUIConfig().getButtonBgColor());
        }
    }
    
}
