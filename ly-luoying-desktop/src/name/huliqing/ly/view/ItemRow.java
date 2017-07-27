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

import name.huliqing.ly.constants.InterfaceConstants;
import name.huliqing.luoying.data.define.CountObject;
import name.huliqing.luoying.transfer.TransferData;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.ListView;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.tiles.ColumnBody;
import name.huliqing.luoying.ui.tiles.ColumnIcon;
import name.huliqing.luoying.ui.tiles.ColumnText;

/**
 * 显示物品的数据行
 * @author huliqing
 */
public class ItemRow extends Row<TransferData> {

    protected TransferData data;
    
    // 物品
    protected ColumnIcon icon;
    protected ColumnBody body;
    protected ColumnText num;
    
    private float[] columnsWidth;
    
    public ItemRow(ListView parentView) {
        super(parentView);
        this.setLayout(Layout.horizontal);
        icon = new ColumnIcon(height, height, InterfaceConstants.UI_MISS);
        body = new ColumnBody(height, height, "", "");
        num = new ColumnText(height, height, "");
        addView(icon);
        addView(body);
        addView(num);
        
        
        setBackground(UIFactory.getUIConfig().getBackground(), true);
        setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        setBackgroundVisible(false);
    }
    
    @Override
    public void updateViewChildren() {
        super.updateViewChildren();
        float iconSize = height;

        icon.setWidth(iconSize);
        icon.setHeight(iconSize);

        num.setWidth(iconSize);
        num.setHeight(iconSize);

        body.setWidth(width - iconSize * 2);
        body.setHeight(iconSize);
        
    }
    
    public float[] getColumnsWidth() {
        if (columnsWidth == null) {
            columnsWidth = new float[3];
        }
        columnsWidth[0] = icon.getWidth();
        columnsWidth[1] = body.getWidth();
        columnsWidth[2] = num.getWidth();
        return columnsWidth;
    }

    @Override
    public void displayRow(TransferData dd) {
        data = dd;
        icon.setIcon(data.getObjectData().getAsString("icon"));
        body.setNameText(ResourceManager.get(data.getObjectData().getId() + ".name"));
        body.setDesText(ResourceManager.getObjectDes(data.getObjectData().getId()));
        if (data instanceof CountObject) {
            num.setText(((CountObject)data).getTotal() + "");
        }
    }
    
    /**
     * 获取当前行所显示的ProtoData
     * @return 
     */
    public TransferData getData() {
        return data;
    }
    
    @Override
    protected void clickEffect(boolean isPress) {
        if (isPress) {
            this.setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        }
        setBackgroundVisible(isPress);
    }

    @Override
    public void onRelease() {
        setBackgroundVisible(false);
    }
    
}
