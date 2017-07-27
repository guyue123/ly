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

/**
 * 线性布局方式
 * @author huliqing
 */
public class LinearLayout extends UILayout {

    // 布局方式
    public enum Layout {
        vertical,
        horizontal
    }
    
    protected Layout layout = Layout.vertical;
    
    public LinearLayout() {
        super();
    }
    
    public LinearLayout(float width, float height) {
        super(width, height);
    }
    
    public Layout getLayout() {
        return layout;
    }
    
    public void setLayout(Layout layout) {
        this.layout = layout;
        setNeedUpdate();
    }

    @Override
    protected void updateViewChildren() {
        // for children
    }
    
    @Override
    protected void updateViewLayout() {
        if (childViews.isEmpty()) 
            return;
        
        // 1.更新子组件自身布局
        for (int i = 0; i < childViews.size(); i++) {
            childViews.get(i).updateView();
        }
        
        // 2.再更新子组件位置
        UI child;
        if (layout == Layout.vertical) {
            float x;
            float y = height - paddingTop;
            for (int i = 0; i < childViews.size(); i++) {
                child = childViews.get(i);
                if (!child.isVisible()) 
                    continue;
                
                x = paddingLeft + child.getMarginLeft();
                y -= child.getHeight();
                y -= child.getMarginTop();
                child.setPosition(x, y);
                y -= child.getMarginBottom();
            }
        } else if (layout == Layout.horizontal) {
            float x = paddingLeft;
            float y;
            for (int i = 0; i < childViews.size(); i++) {
                child = childViews.get(i);
                if (!child.isVisible()) 
                    continue;
                x += child.getMarginLeft();
                y = height - paddingTop - child.getHeight() - child.getMarginTop();
                child.setPosition(x, y);
                x += child.getWidth();
                x += child.getMarginRight();
            }
        }
    }

    @Override
    public void resize() {
        if (childViews.isEmpty()) {
            if (needUpdate) {
                updateView();
                needUpdate = false;
            }
            return;
        }
        
        updateView();
        
        for (UI child : childViews) {
            child.resize();
        }
        
        UI child;
        if (layout == Layout.vertical) {
            float maxWidth = 0;
            float sumHeight = paddingTop + paddingBottom;
            float tempWidth = 0;
            for (int i = 0; i < childViews.size(); i++) {
                child = childViews.get(i);
                if (!child.isVisible()) 
                    continue;
                sumHeight += child.getHeight() + child.getMarginTop() + child.getMarginBottom();
                tempWidth = child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                if (tempWidth > maxWidth) {
                    maxWidth = tempWidth;
                }
            }
            width = maxWidth + paddingLeft + paddingRight;
            height = sumHeight;
        } else if (layout == Layout.horizontal) {
            float sumWidth = paddingLeft + paddingRight;
            float maxHeight = 0;
            float tempHeight = 0;
            for (int i = 0; i < childViews.size(); i++) {
                child = childViews.get(i);
                if (!child.isVisible()) 
                    continue;
                sumWidth += child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                tempHeight = child.getHeight() + child.getMarginTop() + child.getMarginBottom();
                if (tempHeight > maxHeight) {
                    maxHeight = tempHeight;
                }
            }
            width = sumWidth;
            height = maxHeight + paddingTop + paddingBottom;
        }
        
        updateView();
        needUpdate = false;
        
    }
    
    
}
