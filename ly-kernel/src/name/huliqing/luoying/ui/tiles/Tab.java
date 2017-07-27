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
package name.huliqing.luoying.ui.tiles;

import com.jme3.math.ColorRGBA;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.UIFactory;

/**
 *
 * @author huliqing
 */
public class Tab extends LinearLayout{

    private final Map<UI, UI> maps = new LinkedHashMap<UI, UI>();
    
    private LinearLayout tabPanel;
    private LinearLayout bodyPanel;
    
    // button区域的宽度或高度的权重。根据layout不同，会应用到宽度或高度上
    private float tabSizeWeight = 0.2f;
    // 当前显示的是哪一个面板
    private int showIndex;
    
    public Tab(float width, float height) {
        super(width, height);
        tabPanel = new LinearLayout(width, height);
        bodyPanel = new LinearLayout(width, height);
        addView(tabPanel);
        addView(bodyPanel);
    }

    @Override
    protected void updateViewChildren() {
        super.updateViewChildren();
        
        float tabWidth;
        float tabHeight;
        float btnWidth;
        float btnHeight;
        float bodyWidth;
        float bodyHeight;
        if (layout == Layout.vertical) {
            tabPanel.setLayout(Layout.horizontal);
            tabWidth = width;
            tabHeight = height * tabSizeWeight;
            btnWidth = maps.isEmpty() ? 0 : width / maps.size();
            btnHeight = tabHeight;
            bodyWidth = width;
            bodyHeight = height - tabHeight;
        } else {
            tabPanel.setLayout(Layout.vertical);
            tabWidth = width * tabSizeWeight;
            tabHeight = height;
            btnWidth = tabWidth;
            btnHeight = maps.isEmpty() ? 0 : height / maps.size();
            bodyWidth = width - tabWidth;
            bodyHeight = height;
        }
        
        tabPanel.setWidth(tabWidth);
        tabPanel.setHeight(tabHeight);
        bodyPanel.setWidth(bodyWidth);
        bodyPanel.setHeight(bodyHeight);
        for (Entry<UI, UI> en : maps.entrySet()) {
            en.getKey().setWidth(btnWidth);
            en.getKey().setHeight(btnHeight);
            en.getValue().setWidth(bodyWidth);
            en.getValue().setHeight(bodyHeight);
        }
        showTab(showIndex);
    }
    
    /**
     * button区域的宽度或高度的权重。根据layout不同，会应用到宽度或高度上
     * @param sizeWeight 取值 0.0~1.0
     */
    public void setTabSizeWeight(float sizeWeight) {
        tabSizeWeight = sizeWeight;
    }
    
    public final void addTab(final UI tabButton, final UI tabBody) {
        maps.put(tabButton, tabBody);
        tabPanel.addView(tabButton);
        bodyPanel.addView(tabBody);
        tabButton.setEffectEnabled(false);
        tabButton.addClickListener(new Listener() {
            @Override
            public void onClick(UI view, boolean isPressed) {
                if (isPressed) return;
                showTab(tabButton, tabBody);
            }
        });
        tabBody.setVisible(false);
    }
    
    private void showTab(UI tabButton, UI tabBody) {
        int index = 0;
        for (Entry<UI, UI> en : maps.entrySet()) {
            if (en.getKey() != tabButton) {
                en.getKey().setBackgroundColor(ColorRGBA.White, true);
            } else {
                showIndex = index;
            }
            if (en.getValue() != tabBody) {
                en.getValue().setVisible(false);
            }
            index++;
        }
        tabButton.setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        tabBody.setVisible(true);
    }
    
    public final void showTab(UI tabButton) {
        UI tabBody = maps.get(tabButton);
        if (tabBody != null) {
            showTab(tabButton, tabBody);
        }
    }
    
    public final void showTab(int index) {
        if (index >= maps.size()) 
            return;
        
        int i = 0;
        for (Entry<UI, UI> en : maps.entrySet()) {
            if (i == index) {
                showTab(en.getKey(), en.getValue());
                break;
            }
            i++;
        }
    }
    
}
