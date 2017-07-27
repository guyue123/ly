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
package name.huliqing.luoying.ui.state;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import name.huliqing.luoying.ui.UI;

/**
 * GUI点击监听事件
 * @author huliqing
 */
public class UIPickListener implements PickListener{
    
    private final Application app;
    private final Node guiNode;
    private final Ray ray = new Ray();
    private final CollisionResults collisionResult = new CollisionResults();
    
    public UIPickListener(Application app, Node guiNode) {
        this.app = app;
        this.guiNode = guiNode;
    }
    
    @Override
    public boolean pick(boolean isPressed, float tpf) {

        return isViewPicked(app.getInputManager(), guiNode, isPressed);
        
    }
    
    /**
     * 找出最接近的,可被选择的对象.如果最接近的对象不能被选择,则返回null.
     * @param inputManager (not null)
     * @param camera (not null)
     * @param guiRoot (not null)
     * @param results 存放结果
     */
    private boolean isViewPicked(InputManager inputManager, Spatial guiRoot, boolean isPressed) {
        TempVars tv = TempVars.get();
        Vector2f v2d = tv.vect2d.set(inputManager.getCursorPosition());
        Vector3f click3d = tv.vect1.set(v2d.x, v2d.y, 0);
        Vector3f dir = tv.vect2.set(click3d).setZ(1).subtractLocal(click3d).normalizeLocal();
 
        ray.setOrigin(click3d);
        ray.setDirection(dir);
        collisionResult.clear();
        guiRoot.collideWith(ray, collisionResult);
        
        boolean result = false;
        for (int i = collisionResult.size() - 1; i >= 0; i--) {
            CollisionResult cr = collisionResult.getCollision(i);
            UI target = pickView(cr.getGeometry());
            // 注意：一些透明的UI可能挡住实际要点击的UI
            if (target != null) {
                ClickManager.getInstance().fireEvent(target, isPressed);
                // 如果防止事件在不同层间穿透,则直接返回true退出.
                if (target.isPreventCross()) {
                    result = true;
                    break;
                }
            }
        }
        tv.release();
        return result;
    }
    
    private UI pickView(Spatial s) {
        if (s == null) {
            return null;
        }
        
        // View为可点击的对象
        if (s instanceof UI) {
            UI ui = (UI) s;
            
            // 可见和有事件才需要响应，通过是“否有事件响应”的判断可避免一些
            // 不可见或透明的UI遮住可点击UI的异常情况出现。
            // (目前的View层叠和ray的碰撞检测顺序还存在着一些问题)
            if (ui.isVisible() && ui.hasEvent()) {
                return ui;
            } 
            // ---- add20160612
            // 重要：使用PreventEvent以避免事件一直向父UI传递而导致同级的兄弟UI的事件被跳过。这发生在：若当前UI覆盖了另
            // 一个兄弟UI时，如果当前UI无事件，则会把事件传递到父UI事件中，这是错误的，因为可能兄弟UI存在事件。所以如果
            // 当前UI无事件，并打开了preventEvent时，则直接返回null,阻止事件继承向父UI传递,以避免被当前UI覆盖的兄弟UI的事件
            // 被跳过。
            else if (ui.isPreventEvent()) {
                return null;
            }
            
        } 
        
        return pickView(s.getParent());
    }
}
