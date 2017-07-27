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

import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.constants.AssetConstants;

/**
 *
 * @author huliqing
 */
public class Checkbox extends AbstractUI {

    public interface ChangeListener {
        /**
         * 当checkbox的值发生变化时调用
         * @param ui 
         */
        void onChange(Checkbox ui);
    } 
    
    private boolean checked;
    
    private Icon flag;
    private String flagOff = AssetConstants.UI_CHECKBOX_OFF;
    private String flagOn = AssetConstants.UI_CHECKBOX_ON;
    private List<ChangeListener> listeners;

    public Checkbox() {
        this(false);
    }
    
    public Checkbox(boolean checked) {
        super(32, 32);
        this.flag = new Icon(flagOff);
        this.attachChild(flag);
        
        addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                setChecked(!isChecked());
            }
        });
        
        setChecked(checked);
    }
    
    public final boolean isChecked() {
        return checked;
    }
    
    public final void setChecked(boolean checked) {
        boolean changed = (this.checked != checked);
        this.checked = checked;
        updateFlag();
        // 触发侦听器
        if (changed && listeners != null) {
            for (ChangeListener cl : listeners) {
                cl.onChange(this);
            }
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ChangeListener>(1);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public boolean removeChangeListener(ChangeListener listener) {
        return listeners != null && listeners.remove(listener);
    }
    
    private void updateFlag() {
        flag.setImage(checked ? flagOn : flagOff);
        setNeedUpdate();
    }

    @Override
    public void updateView() {
        super.updateView();
        flag.setWidth(width);
        flag.setHeight(height);
        flag.updateView();
    }

    
}
