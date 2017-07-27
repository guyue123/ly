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
package name.huliqing.luoying.object.define;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.data.DefineData;

/**
 * 装备各部位的定义
 * @author huliqing
 */
public class SkinPartDefine extends Define {
    private static final Logger LOG = Logger.getLogger(SkinPartDefine.class.getName());

    private final List<String> skinParts = new ArrayList<String>();
    
    @Override
    public void setData(DefineData data) {
        super.setData(data); 
        String[] tempParts = data.getAsArray("skinParts");
        if (tempParts != null) {
            skinParts.addAll(Arrays.asList(tempParts));
        } else {
            LOG.log(Level.WARNING, "skinParts not defined.");
        }
    }
    
    /**
     * @return 
     */
    public final int size() {
        return skinParts.size();
    }
    
    /**
     * 把装备各部分转换为二进制表示形式，返回的整形中每个二进制位(1)表示一个装备"部位"
     * @param parts
     * @return 
     */
    public long convert(String... parts) {
        if (parts == null) {
            return 0;
        }
        long result = 0L;
        int idx;
        for (String p : parts) {
            idx = skinParts.indexOf(p);
            if (idx != -1) {
                result |= 1L << idx;
            }
        }
        return result;
    }
    
    /**
     * 注册一个新的装备部位,这个注册必须放在系统初始化的时候进行。
     * @param skinPart 
     */
    public synchronized void registerSkinPart(String skinPart) {
        if (skinParts.contains(skinPart)) {
            LOG.log(Level.WARNING, "Could not register skin part,  skin part already exists! skinPart={0}", skinPart);
            return;
        }
        if (size() >= 64) {
            LOG.log(Level.WARNING
                    , "Could not register skin part, the size of skin part could not more than 64! skinPart={0}, current size={1}"
                    , new Object[] {skinPart, size()});
            return;
        }
        skinParts.add(skinPart);
    }
    
     /**
     * 清理所有装备"部位"的定义
     */
    public synchronized void clear() {
        skinParts.clear();
    }
    
    @Override
    public String toString() {
        return skinParts.toString();
    }
    
    /**
     * 查询parts所代表的各个装备部位的名称。
     * @param parts
     * @return 
     */
    public String toString(long parts) {
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < size(); i++) {
            if ((parts & 1 << i) != 0) {
                temp.add(skinParts.get(i));
            }
        }
        return temp.toString();
    }
}
