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
package name.huliqing.editor.constants;

/**
 *
 * @author huliqing
 */
public class StyleConstants {
 
    /** HBox和VBox的通用设置 */
    public final static String CLASS_HVBOX = "hv-box";
    
    /** 左上和左下圆角化 */
    public final static String CLASS_CORNER_ROUND_LB = "corner-round-LB";
    
    /** 右上和右下圆角化 */
    public final static String CLASS_CORNER_ROUND_RB = "corner-round-RB";
    
    /**
     * 属性面板的ID
     */
    public final static String ID_PROPERTY_PANEL = "propertyPanel";
    
    /** 状态栏样式 */
    public final static String CLASS_STATUS_BAR = "statusBar";
    
    /** css 左边圆角化， 即左下和左上*/
    public final static String CSS_CORNER_ROUND_LEFT ="-fx-background-radius:7 0 0 7;";
    
    /** css 右边圆角化， 即右下和右上*/
    public final static String CSS_CORNER_ROUND_RIGHT ="-fx-background-radius:0 7 7 0;";
    
    /** css 上边圆角化，即左上和右上 */
    public final static String CSS_CORNER_ROUND_TOP ="-fx-background-radius:7 7 0 0;";
    
    /** css 下边圆角化，即左下和右下 */
    public final static String CSS_CORNER_ROUND_BOTTOM ="-fx-background-radius:0 0 7 7;";
}
