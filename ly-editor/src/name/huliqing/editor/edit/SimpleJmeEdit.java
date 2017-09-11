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
package name.huliqing.editor.edit;

import com.jme3.scene.Node;
import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.toolbar.BaseEditToolbar;
import name.huliqing.editor.toolbar.Toolbar;

/**
 * 3D模型编辑器窗口
 * @author huliqing 
 * @param <T> 
 */
public abstract class SimpleJmeEdit<T extends ControlTile> extends JmeAbstractEdit {
    
    // 侦听器
    protected final List<SimpleEditListener> editFormListeners = new ArrayList<>();
    
    // 变换模式
    protected Mode mode = Mode.GLOBAL;
    
    // 当前选择的物体
    protected final SafeArrayList<ControlTile> controlTiles = new SafeArrayList<>(ControlTile.class);
    protected T selectObj;

    @Override
    protected Toolbar createToolbar() {
        return new BaseEditToolbar(this);
    }

    /**
     * 默认情况下SimpleJmeEdit不会创建扩展工具栏，子类可以根据实际情况来创建并返回一个扩展工具栏列表
     * @return 
     */
    @Override
    protected List<Toolbar> createExtToolbars() {
        return null;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        boolean changed = this.mode != mode;
        this.mode = mode;
        if (changed) {
            editFormListeners.forEach(l -> {l.onModeChanged(mode);});
        }
    }
    
    public T getSelected() {
        return selectObj;
    }
    
    /**
     * 把一个物体设置为当前的选择的主物体
     * @param selectObj 
     */
    public void setSelected(T selectObj) {
        this.selectObj = selectObj;
        editFormListeners.forEach(l -> {l.onSelected(selectObj);});
    }
    
    /**
     * 获取编辑窗口根节点。
     * @return 
     */
    public Node getEditRoot() {
        return editRoot;
    }
    
    public void addListener(SimpleEditListener listener) {
        if (!editFormListeners.contains(listener)) {
            editFormListeners.add(listener);
        }
    }
    
    public boolean removeListener(SimpleEditListener listener) {
        return editFormListeners.remove(listener);
    }
    
    public void addControlTile(T ct) {
        if (!controlTiles.contains(ct)) {
            controlTiles.add(ct);
        }
        if (!ct.isInitialized()) {
            ct.initialize(this);
        }
    }
    
    public boolean removeControlTile(T ct) {
        if (ct.isInitialized()) {
            ct.cleanup();
        }
        return controlTiles.remove(ct);
    }
    
    public <T extends ControlTile> SafeArrayList<T> getControlTiles() {
        return (SafeArrayList<T>) controlTiles;
    }
}
