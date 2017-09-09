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
package name.huliqing.editor.component;

import java.io.Serializable;

/**
 *
 * @author huliqing
 */
public class ComponentDefine implements Serializable {
    
    private String id;
    private String type;
    private String icon;
    private String converterClass;
    /**
     * 模型显示名称
     */
    private String name;
    
    /**
     * 分类ID
     */
    private String cid;
    
    /**
     * 分类名称
     */
    private String cname;
    
    /**
     * 模型介绍
     */
    private String desc;
    
    /**
     * 存储路径
     */
    private String path;
    
/*    *//**
     * 模型默认尺寸:长
     *//*
    private float length;
    
    *//**
     * 模型默认尺寸:宽
     *//*
    private float width;
    
    *//**
     * 模型默认尺寸:高
     *//*
    private float height;*/
    
    /**
     * 默认材质编码
     */
    private String tid;
    
    public ComponentDefine() {}
    
    public ComponentDefine(String id, String type, String icon, String converterClass, String name, String cid, String cname, String desc, String path, String tid) {
        this.id = id;
        this.type = type;
        this.icon = icon;
        this.converterClass = converterClass;
        
        this.name = name;
        this.cid = cid;
        this.cname = cname;
        this.desc = desc;
        this.path = path;
        this.tid = tid;
    }
    
    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String getType() {
        return type;
    }

    public String getConverterClass() {
        return converterClass;
    }

    @Override
    public String toString() {
        return "ComponentDefine{" + "id=" + id + ", type=" + type + ", icon=" + icon + ", converterClass=" + converterClass + '}';
    }

	public String getName() {
		if (name == null || "".equals(name.trim())) {
			return id;
		}
		return name;
	}

	public String getCid() {
		return cid;
	}

	public String getCname() {
		return cname;
	}

	public String getDesc() {
		return desc;
	}

	public String getPath() {
		return path;
	}

	public String getTid() {
		return tid;
	}
}
