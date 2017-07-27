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
package name.huliqing.editor.tools.entity;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.editor.edit.SimpleJmeEdit;
import name.huliqing.editor.edit.UndoRedo;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.edit.scene.SceneEdit;
import name.huliqing.editor.toolbar.EntityBrushToolbar;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.entity.TerrainEntity;

/**
 * 实体擦除工具
 * @author huliqing
 */
public class EraseTool extends AbstractAdjustEntityBrushTool {
    
    private final List<ControlTile> ectRemoveList = new ArrayList();

    public EraseTool(String name, String tips, String icon) {
        super(name, tips, icon);
    }

    @Override
    public void initialize(SimpleJmeEdit edit, EntityBrushToolbar toolbar) {
        super.initialize(edit, toolbar);
        controlObj.getMaterial().setColor("Color", ColorRGBA.Red);
    }

    @Override
    protected void doAction() {
        float radius = controlObj.getWorldScale().x;
        int density = densityTool.getValue().intValue();
        if (radius <= 0 || density <= 0) {
            return;
        }
        TerrainEntity terrain = getSelectTerrain();
        if (terrain == null) 
            return;
        
        List<EntityData> sourceList = toolbar.getSourceTool().getSources();
        if (sourceList == null || sourceList.isEmpty())
            return;
        
        if (!(edit instanceof SceneEdit)) 
            return;
        
        erase((SceneEdit) edit, sourceList, controlObj.getWorldTranslation(), radius, density);
    }
    
    private void erase(SceneEdit se, List<EntityData> sourceList, Vector3f eraseWorldPoint, float radius, int density) {
        SafeArrayList<ControlTile> cts = se.getControlTiles();
        Entity en;
        float radiusSquared = FastMath.pow(radius, 2);
        int eraseCount = 0;
        EntityData tempEd;
        for (ControlTile ct : cts.getArray()) {
            if (eraseCount >= density) {
                break;
            }
            if (!(ct.getTarget() instanceof Entity)) {
                continue;
            }
            en = (Entity) ct.getTarget();
            if (en.getSpatial().getWorldTranslation().distanceSquared(eraseWorldPoint) > radiusSquared) {
                continue;
            }
            // 通ID匹配来擦除
            for (int i = 0; i < sourceList.size(); i++) {
                tempEd = sourceList.get(i);
                if (tempEd.getId().equals(en.getData().getId())) {
                    se.removeControlTile(ct);
                    ectRemoveList.add(ct);
                    eraseCount++;
                    break;
                }
            }
        }
    }

    @Override
    protected void doEndAction() {
        if (ectRemoveList.isEmpty())
            return;
        EraseUndoRedo eur = new EraseUndoRedo(ectRemoveList);
        edit.addUndoRedo(eur);
        ectRemoveList.clear();
    }

    private class EraseUndoRedo implements UndoRedo {
        
        private final List<ControlTile> ectRemoved = new ArrayList();
        
        public EraseUndoRedo(List<ControlTile> ectRemoved) {
            this.ectRemoved.addAll(ectRemoved);
        }
        
        @Override
        public void undo() {
            SceneEdit sEdit = (SceneEdit) edit;
            for (int i = ectRemoved.size() - 1; i >= 0; i--) {
                sEdit.addControlTile(ectRemoved.get(i));
            }
        }

        @Override
        public void redo() {
            SceneEdit sEdit = (SceneEdit) edit;
            for (int i = 0; i < ectRemoved.size(); i++) {
                sEdit.removeControlTile(ectRemoved.get(i));
            }
        }
    }
}
