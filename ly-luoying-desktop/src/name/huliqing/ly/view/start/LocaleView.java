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
package name.huliqing.ly.view.start;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapFont.Align;
import java.util.Arrays;
import java.util.List;
import name.huliqing.luoying.manager.ResManager;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.ListView;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.Text;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.UI;
import name.huliqing.ly.LyConfig;
import name.huliqing.ly.view.start.StartState.Menu;

/**
 * @author huliqing
 */
public class LocaleView extends ListView<String> {
//    private final ConfigService configService = Factory.get(ConfigService.class);
    private final StartState startState;
    private final List<String> locales;
    
    /**
     * @param width
     * @param height
     * @param state
     */
    public LocaleView(float width, float height, StartState state) {
        super(width, height, "LocalePanel");
        this.startState = state;
        
        String[] localeAll = LyConfig.getLocaleSupported();
        locales = Arrays.asList(localeAll);
        this.pageSize = 3;
    }

    @Override
    public int getRowTotal() {
        return locales.size();
    }
    
    @Override
    protected Row createEmptyRow() {
        Row row = new LocaleRow();
        return row;
    }

    @Override
    public List getDatas() {
        return locales;
    }
    
    private class LocaleRow extends Row<String> {
        private Text text;
        private String locale; // zh_CN, en_US
        public LocaleRow() {
            // Row text
            this.text = new Text("");
            this.text.setBackground(UIFactory.getUIConfig().getBackground(), true);
            this.text.setBackgroundColor(UIFactory.getUIConfig().getButtonBgColor(), true);
            addView(this.text);
            
            addClickListener(new Listener() {
                @Override
                public void onClick(UI ui, boolean isPress) {
                    if (isPress) return;
                    LyConfig.setLocale(locale);
                    ResManager.setLocale(locale);
                    startState.refreshState(Menu.menu_settings);
                }
            });
        }

        @Override
        public void updateViewChildren() {
            super.updateViewChildren();
            float space = 20;
            text.setWidth(width);
            text.setHeight(height - space * 2);
            text.setMargin(0, space, 0, 0);
            text.setAlignment(Align.Center);
            text.setVerticalAlignment(BitmapFont.VAlign.Center);
        }
        
        @Override
        protected void clickEffect(boolean isPress) {
            if (isPress) {
                text.setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
            } else {
                text.setBackgroundColor(UIFactory.getUIConfig().getButtonBgColor(), true);
            }
        }
        
        @Override
        public void displayRow(String dd) {
            locale = dd;
            text.setText(ResourceManager.get("locale." + locale));
        }
        
    }
    
}
