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
package name.huliqing.editor.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.ResConstants;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.manager.Manager;

/**
 *
 * @author huliqing
 */
public class ComponentZone extends VBox{
    
    private final TitledPane componentsPanel = new TitledPane();
    
    public ComponentZone() {
        super();
        
        ComponentsForm cv = new ComponentsForm();
        componentsPanel.setContent(cv);
        componentsPanel.setText(Manager.getRes(ResConstants.FORM_COMPONENTS_TITLE));
        
        getChildren().add(componentsPanel);
        getStyleClass().add(StyleConstants.CLASS_HVBOX);
        setPadding(Insets.EMPTY);
    }
    
}
