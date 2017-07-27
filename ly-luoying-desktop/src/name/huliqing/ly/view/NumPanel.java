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
package name.huliqing.ly.view;

import com.jme3.font.BitmapFont;
import name.huliqing.ly.constants.InterfaceConstants;
import name.huliqing.ly.constants.ResConstants;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.AbstractUI;
import name.huliqing.luoying.ui.Button;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.Text;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.UI.Listener;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.Window;
import name.huliqing.luoying.utils.MathUtils;

/**
 * 用于控制数量
 * @author huliqing
 */
public class NumPanel extends Window implements Listener {
    
    public interface NumConfirmListener {
        void onConfirm(NumPanel numPanel);
    }
    
    private final LinearLayout bodyPanel;
    private final IconPanel min;
    private final IconPanel sub;
    private final Text text;
    private final IconPanel add;
    private final IconPanel max;
    
    private final Button button;
    
    // 数值范围限制
    private int minLimit = 0;
    private int maxLimit = Integer.MAX_VALUE;
    private int value;
    private NumConfirmListener nclistener;

    public NumPanel(float width, float height) {
        super(width, height);
        setCloseable(true);
        
        float cw = getContentWidth();
        float ch = getContentHeight();
        float btnHeight = UIFactory.getUIConfig().getButtonHeight();
        float bodyWidth = cw;
        float bodyHeight = ch - btnHeight;
        float iconWidth = bodyWidth * 0.2f;
        
        min = new IconPanel(iconWidth, bodyHeight, InterfaceConstants.UI_ARROW_LEFT2, 0.3f);
        sub = new IconPanel(iconWidth, bodyHeight, InterfaceConstants.UI_ARROW_LEFT1, 0.3f);
        add = new IconPanel(iconWidth, bodyHeight, InterfaceConstants.UI_ARROW_RIGHT1, 0.3f);
        max = new IconPanel(iconWidth, bodyHeight, InterfaceConstants.UI_ARROW_RIGHT2, 0.3f);
        
        text = new Text(value + "");
        text.setWidth(iconWidth);
        text.setHeight(bodyHeight);
        text.setBackgroundColor(UIFactory.getUIConfig().getDesColor(), false);
        text.setVerticalAlignment(BitmapFont.VAlign.Center);
        text.setAlignment(BitmapFont.Align.Center);
        // 给text添加一个空和事件,防止点击text时发生事件穿透
        text.addClickListener(AbstractUI.EMPTY_LISTENER);
        text.setEffectEnabled(false);
        
        bodyPanel = new LinearLayout(bodyWidth, bodyHeight);
        bodyPanel.setLayout(Layout.horizontal);
        bodyPanel.addView(min);
        bodyPanel.addView(sub);
        bodyPanel.addView(text);
        bodyPanel.addView(add);
        bodyPanel.addView(max);
        
        button = new Button(ResourceManager.get(ResConstants.COMMON_CONFIRM));
        button.setWidth(cw);
        button.setHeight(btnHeight);
        button.setFontSize(UIFactory.getUIConfig().getButtonFontSize());
        
        min.addClickListener(this);
        sub.addClickListener(this);
        add.addClickListener(this);
        max.addClickListener(this);
        button.addClickListener(this);
        
        addView(bodyPanel);
        addView(button);
    }

    @Override
    protected void updateViewChildren() {
        super.updateViewChildren();
        
        String textValue = value + "";
        if (maxLimit != Integer.MAX_VALUE) {
            textValue += "/" + maxLimit;
        }
        text.setText(textValue);
    }

    @Override
    public void onClick(UI view, boolean isPressed) {
        if (isPressed) return;
        if (view == min) {
            min();
        } else if (view == sub) {
            sub();
        } else if (view == add) {
            add();
        } else if (view == max) {
            max();
        } else if (view == button) {
            // 触发确认侦听
            if (nclistener != null) {
                nclistener.onConfirm(this);
            }
        }
    }
    
    public void sub() {
        value -= 1;
        if (value < minLimit) {
            value = minLimit;
        }
        setNeedUpdate();
    }
    
    public void add() {
        value += 1;
        if (value > maxLimit) {
            value = maxLimit;
        }
        setNeedUpdate();
    }
    
    public void min() {
        // 如果没有设置过最小值限制则不处理
        if (minLimit == Integer.MIN_VALUE)
            return;
        
        value = minLimit;
        setNeedUpdate();
    }
    
    public void max() {
        if (maxLimit == Integer.MAX_VALUE)
            return;
        
        value = maxLimit;
        setNeedUpdate();
    }

    public void setMinLimit(int minLimit) {
        this.minLimit = minLimit;
        setNeedUpdate();
    }
    
    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
        setNeedUpdate();
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = MathUtils.clamp(value, minLimit, maxLimit);
    }
    
    public void setNumConfirmListener(NumConfirmListener ncListener) {
        this.nclistener = ncListener;
    }
}
