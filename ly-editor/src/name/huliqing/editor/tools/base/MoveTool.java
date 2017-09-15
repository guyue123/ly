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
package name.huliqing.editor.tools.base;

import com.jme3.input.KeyInput;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import name.huliqing.editor.utils.Picker;
import name.huliqing.editor.events.Event;
import name.huliqing.editor.events.JmeEvent;
import name.huliqing.editor.edit.Mode;
import name.huliqing.editor.edit.SimpleJmeEdit;
import name.huliqing.editor.tiles.AxisNode;
import name.huliqing.editor.tiles.LocationControlObj;
import name.huliqing.editor.edit.UndoRedo;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.manager.PickManager;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.toolbar.EditToolbar;
import name.huliqing.editor.edit.SimpleEditListener;
import name.huliqing.editor.tools.AbstractTool;
import name.huliqing.editor.tools.ToggleTool;

/**
 * 移动编辑工具
 * @author huliqing
 */
public class MoveTool extends AbstractTool implements SimpleEditListener, ToggleTool{
//    private static final Logger LOG = Logger.getLogger(MoveTool.class.getName());
    
    private final static String EVENT_MOVE = "moveEvent";
    private final static String EVENT_MOVE_X = "moveXEvent";
    private final static String EVENT_MOVE_Y = "moveYEvent";
    private final static String EVENT_MOVE_Z = "moveZEvent";
    
    private final static String EVENT_FREE_MOVE_START = "freeMoveStartEvent";
    private final static String EVENT_FREE_MOVE_CANCEL = "freeMoveCancelEvent";
    
    private final Ray ray = new Ray();
    private final Picker picker = new Picker();
    // 物体选择、操作标记（位置）
    private final LocationControlObj controlObj = new LocationControlObj();

    // 当前操作的轴向
    private AxisNode moveAxis;
    // 行为操作开始时编辑器中的被选择的物体，以及该物体的位置
    private ControlTile selectObj;
    
    // 开始移动时和结束移动时物体的位置(local)
    private Vector3f startSpatialLoc;
    private Vector3f lastSpatialLoc;
    
    // 自由移动
    private boolean transforming;
    private boolean freeMove;
    private boolean axisMove;
    
    public MoveTool(String name, String tips, String icon) {
        super(name, tips, icon);
        // 绑定三个按键：x,y,z用于在自由旋转的时候转化到按轴旋转,优先级高一些，避免和按x,y,z的按键冲突，例如：“删除”快捷键X
        bindEvent(EVENT_MOVE_X).bindKey(KeyInput.KEY_X, true).setPrior(1);
        bindEvent(EVENT_MOVE_Y).bindKey(KeyInput.KEY_Y, true).setPrior(1);
        bindEvent(EVENT_MOVE_Z).bindKey(KeyInput.KEY_Z, true).setPrior(1);
    }
    
    @Override
    public void initialize(SimpleJmeEdit jmeEdit, EditToolbar toolbar) {
        super.initialize(jmeEdit, toolbar);
        edit.getEditRoot().getParent().attachChild(controlObj);
        edit.addListener(this);
        updateMarkerState();
    }
    
    @Override
    public void cleanup() {
        endMove();
        edit.addListener(this);
        controlObj.removeFromParent();
        super.cleanup();
    }
    
    /**
     * 绑定移动按键
     * @return 
     */
    public JmeEvent bindMoveEvent() {
        return bindEvent(EVENT_MOVE);
    }
    
    public JmeEvent bindFreeMoveStartEvent() {
        return bindEvent(EVENT_FREE_MOVE_START);
    }
    public JmeEvent bindFreeMoveCancelEvent() {
        return bindEvent(EVENT_FREE_MOVE_CANCEL);
    }

    @Override
    protected void onToolEvent(Event e) {
        selectObj = edit.getSelected();
        if (selectObj == null || selectObj.getControlSpatial() == null) {
            endMove();
            return;
        }
        
        if (e.getName().equals(EVENT_MOVE)) {
            if (e.isMatch()) {
                // 重要优化：如果当前正在自由操作，则直接结束操作，不要启动新的按轴操作。
                // 因为这个操作可能是要结束自由操作，而不是希望启动按轴操作, 如果是这样则会多出一个多余的历史记录。
                if (freeMove) {
                    endMove();
                    return;
                }
                
                updateMarkerState();
                PickManager.getPickRay(editor.getCamera(), editor.getInputManager().getCursorPosition(), ray);
                AxisNode axis = controlObj.getPickAxis(ray);
                if (axis != null) {
                    startAxisMove(axis);
                    e.setConsumed(true);
                }
            } else {
                endMove();
            }
        }
        
        // 开始自由操作
        else if (EVENT_FREE_MOVE_START.equals(e.getName())) {
            if (e.isMatch()) {
                startFreeMove();
                e.setConsumed(true);
            }
        // 取消操作
        } else if (transforming && EVENT_FREE_MOVE_CANCEL.equals(e.getName())) { 
            if (e.isMatch()) {
                cancelMove();
                e.setConsumed(true);
            }
        }
        
        // 从自由操作转到约束操作
        else if (transforming && e.isMatch() && EVENT_MOVE_X.equals(e.getName())) {
            cancelMove();
            startAxisMove(controlObj.getAxisX());
            e.setConsumed(true);
        }
        else if (transforming && e.isMatch() && EVENT_MOVE_Y.equals(e.getName())) {
            cancelMove();
            startAxisMove(controlObj.getAxisY());
            e.setConsumed(true);
        }
        else if (transforming && e.isMatch() && EVENT_MOVE_Z.equals(e.getName())) {
            cancelMove();
            startAxisMove(controlObj.getAxisZ());
            e.setConsumed(true);
        }
    }
    
    /**
     * 自由移动操作
     */
    public void startFreeMove() {
        // 重要：这要避免自由操作和按轴操作时的冲突,因为自由操作按键可能和按轴操作按键冲突。
        // 比如：当自由操作通过按下“左键”来应用操作的时候，而这个按键又和按轴操作冲突，
        // 这时自由操作会直接变成按轴操作，而自由操作又来不及保存历史记录，
        // 这时候就导致“自由操作”丢失历史记录，而按轴操作又没有启动. 所以这里要FIX这个BUG。
        // 在按轴操作启动时要确保如果自由操作正在进行则需要保存历史记录再退出。
        if (axisMove) {
            endMove();
        }
        freeMove = true;
        axisMove = false;
        transforming = true;
        picker.startPick(selectObj.getControlSpatial(), Mode.CAMERA
                , editor.getCamera(), editor.getInputManager().getCursorPosition(), Picker.PLANE_XY);
        startSpatialLoc = selectObj.getControlSpatial().getLocalTranslation().clone();
        lastSpatialLoc = new Vector3f(startSpatialLoc);
        controlObj.setAxisVisible(false);
        controlObj.setAxisLineVisible(false);
    }

    private void startAxisMove(AxisNode axis) {
        // 重要：这要避免自由操作和按轴操作时的冲突,因为自由操作按键可能和按轴操作按键冲突。
        // 比如：当自由操作通过按下“左键”来应用操作的时候，而这个按键又和按轴操作冲突，
        // 这时自由操作会直接变成按轴操作，而自由操作又来不及保存历史记录，
        // 这时候就导致“自由操作”丢失历史记录，而按轴操作又没有启动. 所以这里要FIX这个BUG。
        // 在按轴操作启动时要确保如果自由操作正在进行则需要保存历史记录再退出。
        if (freeMove) {
            endMove();
        }
        axisMove = true;
        freeMove = false;
        moveAxis = axis;
        transforming = true;
        Quaternion planRotation = Picker.PLANE_XY;
        switch (moveAxis.getType()) {
            case x:
                planRotation = Picker.PLANE_XY;
                break;
            case y:
//                planRotation = Picker.PLANE_YZ;
                planRotation = Picker.PLANE_XY;
                break;
            case z:
                planRotation = Picker.PLANE_XZ;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        picker.startPick(selectObj.getControlSpatial(), edit.getMode()
                , editor.getCamera(), editor.getInputManager().getCursorPosition(), planRotation);
        startSpatialLoc = selectObj.getControlSpatial().getLocalTranslation().clone();
        lastSpatialLoc = new Vector3f(startSpatialLoc);
        controlObj.setAxisVisible(false);
        controlObj.setAxisLineVisible(false);
        moveAxis.setAxisLineVisible(true);
    }

    private void endMove() {
        if (transforming) {
            picker.endPick();
            MoveUndo undoRedo = new MoveUndo(selectObj, startSpatialLoc, lastSpatialLoc);
            edit.addUndoRedo(undoRedo);
            edit.setModified(true);
        }
        startSpatialLoc = null;
        lastSpatialLoc = null;
        transforming = false;
        freeMove = false;
        axisMove = false;
        moveAxis = null;
        controlObj.setAxisVisible(ControlTile.canBeSelectedMove(selectObj));
        controlObj.setAxisLineVisible(false);
    }
    
    private void cancelMove() {
        if (transforming) {
            picker.endPick();
            selectObj.setLocalTranslation(startSpatialLoc);
            transforming = false;
            // 更新一次位置,因为操作取消了
            updateMarkerState();
        }
        endMove();
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
        // 对于相机视角，Marker必须实时随着相机的移动旋转而更新
        if (edit.getMode() == Mode.CAMERA) {
            updateMarkerState();
        }
        
        if (!transforming)
            return;
        
        if (!picker.updatePick(editor.getCamera(), editor.getInputManager().getCursorPosition())) {
            return;
        }
        
        TempVars tv = TempVars.get();
        Vector3f diff;
        if (freeMove) {
            diff = picker.getTranslation();
        } else {
            diff = picker.getTranslation(moveAxis.getDirection(tv.vect2));
        }
        
        Spatial parent = selectObj.getControlSpatial().getParent();
        if (parent != null) {
            tv.quat1.set(parent.getWorldRotation()).inverseLocal().mult(diff, diff);
            diff.divideLocal(parent.getWorldScale());
        }
        tv.release();
        
        Vector3f finalLocalPos = new Vector3f(startSpatialLoc).addLocal(diff);
        selectObj.setLocalTranslation(finalLocalPos);
        controlObj.setLocalTranslation(ModelManager.getInstance().getTranslation(selectObj.getControlSpatial()));
        lastSpatialLoc.set(finalLocalPos);
    }

    @Override
    public void onModeChanged(Mode mode) {
        updateMarkerState();
    }

    @Override
    public void onSelected(ControlTile selectObj) {
        updateMarkerState();
    }
    
    private void updateMarkerState() {
        selectObj = edit.getSelected();
        if (selectObj == null || selectObj.getControlSpatial() == null) {
            controlObj.setVisible(false);
            return;
        }
        // controlObj.setVisible(true);
    	// 2017/09/10 只有实体模型和灯光可以选择移动
        boolean canMove = ControlTile.canBeSelectedMove(selectObj);
        if (!canMove) {
            controlObj.setAxisVisible(false);
            controlObj.setAxisLineVisible(false);
        	return;
        }
        controlObj.setAxisVisible(true);
        controlObj.setAxisLineVisible(false);
        controlObj.setVisible(canMove);
        
        controlObj.setLocalTranslation(ModelManager.getInstance().getTranslation(selectObj.getControlSpatial()));
        Mode mode = edit.getMode();
        switch (edit.getMode()) {
            case GLOBAL:
                controlObj.setLocalRotation(new Quaternion());
                break;
            case LOCAL:
                controlObj.setLocalRotation(edit.getSelected().getControlSpatial().getWorldRotation());
                break;
            case CAMERA:
                controlObj.setLocalRotation(editor.getCamera().getRotation());
                break;
            default:
                throw new IllegalArgumentException("Unknow mode type=" + mode);
        }
    }

    private class MoveUndo implements UndoRedo {

        private final ControlTile selectObj;
        private final Vector3f before = new Vector3f();
        private final Vector3f after = new Vector3f();
        
        public MoveUndo(ControlTile selectObj, Vector3f startPosition, Vector3f lastPosition) {
            this.selectObj = selectObj;
            this.before.set(startPosition);
            this.after.set(lastPosition);
        }
        
        @Override
        public void undo() {
            selectObj.setLocalTranslation(before);
            updateMarkerState();
        }

        @Override
        public void redo() {
            selectObj.setLocalTranslation(after);
            updateMarkerState();
        }

        @Override
        public String toString() {
            return "MoveUndo:" + selectObj + ", before=" + before + ", after=" + after;
        }
        
    }
}
