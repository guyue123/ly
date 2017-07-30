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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import name.huliqing.editor.edit.Mode;
import name.huliqing.editor.edit.SimpleEditListener;
import name.huliqing.editor.edit.SimpleJmeEdit;
import name.huliqing.editor.edit.UndoRedo;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.events.Event;
import name.huliqing.editor.events.JmeEvent;
import name.huliqing.editor.tiles.AxisNode;
import name.huliqing.editor.tiles.RotationControlObj;
import name.huliqing.editor.toolbar.EditToolbar;
import name.huliqing.editor.tools.AbstractTool;
import name.huliqing.editor.tools.ToggleTool;
import name.huliqing.editor.utils.Picker;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.manager.PickManager;

/**
 * 旋转编辑工具
 * @author huliqing
 */
public class RotationTool extends AbstractTool implements SimpleEditListener, ToggleTool{
//    private static final Logger LOG = Logger.getLogger(RotationTool.class.getName());
    
    private final static String EVENT_ROTATION = "rotationEvent";
    private final static String EVENT_ROTATION_X = "rotationXEvent";
    private final static String EVENT_ROTATION_Y = "rotationYEvent";
    private final static String EVENT_ROTATION_Z = "rotationZEvent";
    
    private final static String EVENT_FREE_ROTATION_START = "freeRotationStartEvent";
    private final static String EVENT_FREE_ROTATION_CANCEL = "freeRotationCancelEvent";
    
    private final Ray ray = new Ray();
    private final Picker picker = new Picker();
    
    // 变换控制物体
    private final RotationControlObj controlObj = new RotationControlObj();
    // 当前操作的轴向
    private AxisNode rotationAxis;
    // 行为操作开始时编辑器中的被选择的物体，以及该物体的位置
    private ControlTile selectObj;
    
    // 开始变换时物体的位置(local)
    private Quaternion startRotate;
    private Quaternion startWorldRotate;
    // 经过旋转操作后的LocaleRotationl
    private Quaternion afterRotate;
    
    // 是否正在变换操作中
    private boolean transforming;
    // 自由旋转，相机视角
    private boolean freeRotation;
    // 按轴旋转
    private boolean axisRotation;
    
    // 2017/07/26 旋转轴不在物体中心时，旋转导致物体位置变化。物体的位置(local)
    private Vector3f startSpatialLoc;
    private Vector3f lastSpatialLoc;
    // 旋转中心
    private Vector3f rotateCenter;

    public RotationTool(String name, String tips, String icon) {
        super(name, tips, icon);
        // 绑定三个按键：x,y,z用于在自由旋转的时候转化到按轴旋转
        bindEvent(EVENT_ROTATION_X).bindKey(KeyInput.KEY_X, false);
        bindEvent(EVENT_ROTATION_Y).bindKey(KeyInput.KEY_Y, false);
        bindEvent(EVENT_ROTATION_Z).bindKey(KeyInput.KEY_Z, false);
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
        endRotation();
        controlObj.removeFromParent();
        edit.addListener(this);
        super.cleanup(); 
    }

    /**
     * 绑定一个按键用于打开旋转功能
     * @return 
     */
    public JmeEvent bindRotationEvent() {
        return bindEvent(EVENT_ROTATION);
    }
    
    /**
     * 绑定一个开始全局缩放的按键事件
     * @return 
     */
    public JmeEvent bindFreeRotationStartEvent() {
        return bindEvent(EVENT_FREE_ROTATION_START);
    }
    public JmeEvent bindFreeRotationCancelEvent() {
        return bindEvent(EVENT_FREE_ROTATION_CANCEL);
    }

    @Override
    protected void onToolEvent(Event e) {
        selectObj = edit.getSelected();
        if (selectObj == null || selectObj.getControlSpatial() == null) {
            endRotation();
            return;
        }
        
        if (e.getName().equals(EVENT_ROTATION)) {
            if (e.isMatch()) {
                
                // 重要优化：如果当前正在自由操作，则直接结束操作，不要启动新的按轴操作。
                // 因为这个操作可能是要结束自由操作，而不是希望启动按轴操作, 如果是这样则会多出一个多余的历史记录。
                if (freeRotation) {
                    endRotation();
                    return;
                }
                
                PickManager.getPickRay(editor.getCamera(), editor.getInputManager().getCursorPosition(), ray);
                AxisNode pickAxis = controlObj.getPickAxis(ray);
                if (pickAxis != null) {
                    startAxisRotation(pickAxis);
                    e.setConsumed(true);
                }
            } else {
                endRotation();
            }
        }
        
        // 开始旋转
        else if (EVENT_FREE_ROTATION_START.equals(e.getName())) {
            if (e.isMatch()) {
                startFreeRotation();
                e.setConsumed(true);
            }
        } 
        // 取消旋转
        else if (transforming && EVENT_FREE_ROTATION_CANCEL.equals(e.getName())) { 
            if (e.isMatch()) {
                cancelRotation();
                // 销毁后续事件，注意确保不要误销毁其它正常事件
                e.setConsumed(true);
            }
        }
        
        // 从自由旋转转换到按轴旋转
        else if (transforming && e.isMatch() && EVENT_ROTATION_X.equals(e.getName())) {
            cancelRotation();
            startAxisRotation(controlObj.getAxisX());
            e.setConsumed(true);
        }
        else if (transforming && e.isMatch() && EVENT_ROTATION_Y.equals(e.getName())) {
            cancelRotation();
            startAxisRotation(controlObj.getAxisY());
            e.setConsumed(true);
        }
        else if (transforming && e.isMatch() && EVENT_ROTATION_Z.equals(e.getName())) {
            cancelRotation();
            startAxisRotation(controlObj.getAxisZ());
            e.setConsumed(true);
        }
    }
    
    private void startFreeRotation() {
        if (axisRotation) {
            endRotation();
        }
        transforming = true;
        freeRotation = true;
        axisRotation = false;
        picker.startPick(selectObj.getControlSpatial(), Mode.CAMERA
                , editor.getCamera(), editor.getInputManager().getCursorPosition(), Picker.PLANE_XY);
        startRotate = selectedSpatial().getLocalRotation().clone();
        startWorldRotate = selectedSpatial().getWorldRotation().clone();
        controlObj.setAxisVisible(false);
        controlObj.setAxisLineVisible(false);
        
        startSpatialLoc = selectObj.getControlSpatial().getLocalTranslation().clone();
        lastSpatialLoc = new Vector3f(startSpatialLoc);
        
        rotateCenter = ModelManager.getInstance().getTranslation(selectObj.getControlSpatial());
    }

    private void startAxisRotation(AxisNode axis) {
        // 重要：这要避免自由操作和按轴操作时的冲突,因为自由操作按键可能和按轴操作按键冲突。
        // 比如：当自由操作通过按下“左键”来应用操作的时候，而这个按键又和按轴操作冲突，
        // 这时自由操作会直接变成按轴操作，而自由操作又来不及保存历史记录，
        // 这时候就导致“自由操作”丢失历史记录，而按轴操作又没有启动. 所以这里要FIX这个BUG。
        // 在按轴操作启动时要确保如果自由操作正在进行则需要保存历史记录再退出。
        if (freeRotation) {
            endRotation();
        }
        transforming = true;
        freeRotation = false;
        axisRotation = true;
        rotationAxis = axis;
        Quaternion planRotation = Picker.PLANE_XY;
        switch (axis.getType()) {
            case x:
                planRotation = Picker.PLANE_YZ;
                break;
            case y:
                planRotation = Picker.PLANE_XZ;
                break;
            case z:
                planRotation = Picker.PLANE_XY;
                break;
            default:
                throw new UnsupportedOperationException();
        }
       
        picker.startPick(selectObj.getControlSpatial(), edit.getMode()
                , editor.getCamera(), editor.getInputManager().getCursorPosition()
                ,planRotation);
        startRotate = selectedSpatial().getLocalRotation().clone();
        startWorldRotate = selectedSpatial().getWorldRotation().clone();
        controlObj.setAxisVisible(false);
        controlObj.setAxisLineVisible(false);
        axis.setAxisLineVisible(true);
        
        startSpatialLoc = selectObj.getControlSpatial().getLocalTranslation().clone();
        lastSpatialLoc = new Vector3f(startSpatialLoc);
        rotateCenter = ModelManager.getInstance().getTranslation(selectObj.getControlSpatial());
//            LOG.log(Level.INFO, "StartSpatialLoc={0}", startSpatialLoc);
    }
    
    private Spatial selectedSpatial() {
        Spatial cloneSelectObj = selectObj.getControlSpatial();
        //cloneSelectObj.setLocalTranslation(ModelManager.getInstance().getTranslation(cloneSelectObj));
        
        return cloneSelectObj;
    }

    private void endRotation() {
        if (transforming) {
            picker.endPick();
            edit.addUndoRedo(new RotationUndoRedo(selectObj, startRotate, afterRotate, startSpatialLoc, lastSpatialLoc));
            edit.setModified(true);
        }
        
        startSpatialLoc = null;
        lastSpatialLoc = null;
        transforming = false;
        freeRotation = false;
        axisRotation = false;
        rotationAxis = null;
        rotateCenter = null;
        controlObj.setAxisVisible(true);
        controlObj.setAxisLineVisible(false);
    }
    
    private void cancelRotation() {
        if (transforming) {
            picker.endPick();
            selectObj.setLocalRotation(startRotate);
            selectObj.setLocalTranslation(startSpatialLoc);
            transforming = false;
            // 更新一次位置,因为操作取消了
            updateMarkerState();
        }
        endRotation();
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
        
        float angle = picker.getAngle2f();
        System.out.println("-------------------------------");
        System.out.println("angle:" + (angle * 180 / FastMath.PI));
        if (FastMath.abs(angle * 180 / FastMath.PI) < 5) {
        	return;
        }
        
        Quaternion rotation = startRotate.mult(picker.getRotation(startWorldRotate.inverse()));
        controlObj.setLocalTranslation(rotateCenter);
        
        //Create the node to use as pivot
        Spatial cloneSpatial = selectObj.getControlSpatial().clone();
        // 新模型中心坐标
        Vector3f modelCenter = rotateCenter.subtract(cloneSpatial.getWorldTranslation());
        
        //Create the node to use as pivot
        Node newPivot = new Node();
        newPivot.setLocalTranslation(modelCenter);
        newPivot.attachChild(cloneSpatial);
        
        //Reverse the pivot to match the center of the mesh
        cloneSpatial.setLocalTranslation(modelCenter.negate());
        // 正向旋转
        newPivot.rotate(rotation);
        // 设置原中心为坐标
        newPivot.setLocalTranslation(rotateCenter);
        
/*        edit.getEditRoot().getParent().attachChild(newPivot);
        newPivot.setCullHint(CullHint.Dynamic);*/

        selectedSpatial().rotate(rotation);
        selectObj.setLocalRotation(selectedSpatial().getLocalRotation());
        // 转换成原世界坐标
        selectObj.setLocalTranslation(cloneSpatial.getWorldTranslation());
        
        afterRotate = rotation;
        // 2017/07/26 旋转轴不在中心
/*        TempVars tv = TempVars.get();
        Vector3f diff;
        Vector3f diff1;
        Vector3f diff3;
        if (freeRotation) {
            diff = picker.getTranslation();
            diff1 = picker.getTranslation();
            diff3 = picker.getTranslation();
        } else {
            diff = picker.getTranslation(rotationAxis.getDirection(tv.vect2));

            diff1 = picker.getTranslation(new Vector3f(1, 0, 0));
            diff3 = picker.getTranslation(new Vector3f(0, 0, 1));
        }
        
        Vector3f megediff = diff;//diff.add(diff1).add(diff3);
        
        Spatial parent = selectObj.getControlSpatial().getParent();
        if (parent != null) {
            tv.quat1.set(parent.getWorldRotation()).inverseLocal().mult(megediff, megediff);
            diff.divideLocal(parent.getWorldScale());
        }
        tv.release();
        
        Vector3f finalLocalPos = new Vector3f(startSpatialLoc).addLocal(megediff);
        selectObj.setLocalTranslation(finalLocalPos);
        controlObj.setLocalTranslation(ModelManager.getInstance().getTranslation(selectObj.getControlSpatial()));
        lastSpatialLoc.set(finalLocalPos);*/
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
        controlObj.setVisible(true);
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
    
    private class RotationUndoRedo implements UndoRedo {
        private final ControlTile selectObj;
        private final Quaternion before = new Quaternion();
        private final Quaternion after = new Quaternion();
        
        private final Vector3f locationBefore = new Vector3f();
        private final Vector3f locationAfter = new Vector3f();
        
        public RotationUndoRedo(ControlTile selectObj, Quaternion before, Quaternion after, Vector3f startPosition, Vector3f lastPosition) {
            this.selectObj = selectObj; 
            this.before.set(before);
            this.after.set(after);
            this.locationBefore.set(startPosition);
            this.locationAfter.set(lastPosition);
        }
        
        @Override
        public void undo() {
            selectObj.setLocalRotation(before);
            selectObj.setLocalTranslation(locationBefore);
            updateMarkerState();
        }

        @Override
        public void redo() {
            selectObj.setLocalRotation(after);
            selectObj.setLocalTranslation(locationAfter);
            updateMarkerState();
        }
        
        @Override
        public String toString() {
            return "RotationUndoRedo:" + selectObj + ", before=" + before + ", after=" + after;
        }
    }
}
