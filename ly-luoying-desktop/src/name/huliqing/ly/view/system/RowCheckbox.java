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
package name.huliqing.ly.view.system;

import com.jme3.font.BitmapFont;
import name.huliqing.luoying.ui.Checkbox;
import name.huliqing.luoying.ui.FrameLayout;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.Text;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.UI;

/**
 *
 * @author huliqing
 */
public class RowCheckbox extends Row<SystemData> {
    
    private LinearLayout bodyPanel;
    private Text rowName;
    private Text des;
    
    private FrameLayout checkPanel;
    private Checkbox checkbox;
    
    public RowCheckbox(String name, String des, boolean checked) {
        super();
        setLayout(Layout.horizontal);
        bodyPanel = new LinearLayout();
        checkPanel = new FrameLayout();
        addView(bodyPanel);
        addView(checkPanel);
        
        this.rowName = new Text(name);
        this.des = new Text(des);
        this.des.setFontColor(UIFactory.getUIConfig().getDesColor());
        this.des.setFontSize(UIFactory.getUIConfig().getDesSize());
        bodyPanel.addView(this.rowName);
        bodyPanel.addView(this.des);
        
        checkbox = new Checkbox(checked);
        checkPanel.addView(checkbox);
        
        addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                checkbox.setChecked(!checkbox.isChecked());
            }
        });
        
        setBackground(UIFactory.getUIConfig().getBackground(), true);
        setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        setBackgroundVisible(false);
    }
    
    @Override
    public void updateViewChildren() {
        super.updateViewChildren(); 
        float mg = 5;
        
        float bodyW = width * 0.8f;
        float bodyH = height;
        float checkW = width - bodyW;
        float checkH = height;
        float rowW = bodyW - mg;
        float rowH = height * 0.5f - mg;
        
        this.bodyPanel.setWidth(bodyW);
        this.bodyPanel.setHeight(bodyH);
        this.rowName.setWidth(rowW);
        this.rowName.setHeight(rowH);
        this.rowName.setMargin(mg, mg, 0, 0);
        this.rowName.setVerticalAlignment(BitmapFont.VAlign.Center);
        this.des.setWidth(rowW);
        this.des.setHeight(rowH);
        this.des.setMargin(mg, 0, 0, 0);
        this.des.setVerticalAlignment(BitmapFont.VAlign.Center);
        
        this.checkPanel.setWidth(checkW);
        this.checkPanel.setHeight(checkH);
        
    }

    @Override
    protected void updateViewLayout() {
        super.updateViewLayout(); //To change body of generated methods, choose Tools | Templates.
        checkbox.setToCorner(Corner.CC);
    }
    
    @Override
    protected void clickEffect(boolean isPress) {
        if (isPress) {
            setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        }
        setBackgroundVisible(isPress);
    }
    
    @Override
    public void onRelease() {
        setBackgroundVisible(false);
    }
    
    @Override
    public void displayRow(SystemData data) {
        // ignore
    }
    
    public void setChecked(boolean checked) {
        this.checkbox.setChecked(checked);
    }
    
    public boolean isChecked() {
        return checkbox.isChecked();
    }
}
