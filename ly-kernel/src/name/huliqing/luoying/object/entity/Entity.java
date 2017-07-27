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
package name.huliqing.luoying.object.entity;

import com.jme3.scene.Spatial;
import java.util.List;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.object.attribute.Attribute;
import name.huliqing.luoying.object.attribute.AttributeManager;
import name.huliqing.luoying.object.module.Module;
import name.huliqing.luoying.object.scene.Scene;
import name.huliqing.luoying.xml.DataProcessor;
import name.huliqing.luoying.xml.ObjectData;

/**
 * Entity定义一种可以直接存放在Scene中的存在，这类<b>存在</b>包含任何可能性的有形物体或无形物体,
 * 如：水、天空、重力、阴影、模型、阳光、相机等...
 * @author huliqing
 */
public interface Entity extends DataProcessor<EntityData> {
    
    /**
     * 初始化实体，这个方法在实体载入的时候将被立即调用。<br>
     * 注：这个时候实体可能还没有被添加到场景中。
     * @see #onInitScene(Scene) 
     */
    void initialize();
        
    /**
     * 判断是否已经初始化
     * @return 
     */
    boolean isInitialized();

    /**
     * 设置是否开启Entity
     * @param enabled 
     */
    void setEnabled(boolean enabled);
    
    /**
     * 判断Entity是否开启
     * @return 
     */
    boolean isEnabled();
    
    /**
     * 这个方法会在物体被添加到场景的时候被调用, 如果一个物体没有被直接被放到场景中，则这个方法
     * 可能不会被调用。
     * @param scene 
     */
    void onInitScene(Scene scene);
    
    /**
     * 清理并释放资源
     */
    void cleanup();
    
    /**
     * 获取物体的唯一id, 通过这个id可以从scene中直接找到这个物体
     * @return 
     */
    long getEntityId();
    
    /**
     * 获取代表当前物体在场景(Scene)中的"空间存在"，这个存在代表了物体本身，一般也是Entity的根节点，
     * 一个Entity可以是有形的或是无形的存在，有形的如：花、草、树木、动物、生物、各种角色等拥有模型的物体。
     * 无形的Entity可以是如：物理环境、光照系统、水体环境、天空系统、包围盒、阴影及各种SceneProcessor或Filter等。
     * <br>
     * 1.对于有形的Entity，这个方法应该返回一个代表物体在场景中的实际存在，如对于角色一般应该返回角色的模型节点.<br>
     * 2.对于无形的Entity，这个方法可以返回一个空的Node,不要返回null.<br>
     * @return 
     * @see ModelEntity
     * @see NoneModelEntity
     */
    Spatial getSpatial();
    
    /**
     * 获得当前物体所在的场景,当一个Entity存在于场景中时这个方法应该返回对当前角色所在场景的引用，
     * 如果Entity已经脱离场景，则该返回值应该为null. 即可以通过这个方法来判断Entity是否正在场景中。
     * @return 
     */
    Scene getScene();
    
    /**
     * 让Entity从当前场景中脱离
     * @return 
     */
    boolean removeFromScene();
    
    /**
     * 添加新的属性，注：如果已经存在相同id或名称的属性，则旧的属性会被替换掉。
     * @param attribute 
     */
    void addAttribute(Attribute attribute);
    
    /**
     * 移除指定的属性。
     * @param attribute
     * @return 
     */
    boolean removeAttribute(Attribute attribute);
    
    /**
     * 获取角色当前的所有属性，注：返回的列表只能只读，否则报错。
     * @return 
     */
    List<Attribute>getAttributes();
    
    /**
     * 获取指定名称的属性,如果不存在则返回null.
     * @param <T>
     * @param attrName
     * @return 
     */
    <T extends Attribute> T getAttribute(String attrName);
    
    /**
     * 查找指定的属性，如果找不到或者指定的属性类型不匹配则返回null.
     * @param <T>
     * @param attrName 属性名称，<b>不</b>是属性ID(<b>Not</b>id)
     * @param type 如果不为null, 则找到的属性必须符合这个类型，否则返回null.
     * @return 
     * @see #getAttribute(java.lang.String) 
     */
    <T extends Attribute> T getAttribute(String attrName, Class<T> type);
    
    /**
     * 击中、设置当前Entity的指定的属性，属性必须存在，否则什么也不做。
     * @param attribute 属性名称
     * @param hitValue 属性值，<b>偿试</b>应用到指定属性上的值,应用后属性的值应该以属性内部获得的为准，因为一些
     * 属性类型可能会限制应用到属性上的值，比如限制取值范围的一些Number类型的值。
     * @param hitter 发起攻击的源，这个参数可以为null,如果击中源不存在。
     */
    void hitAttribute(String attribute, Object hitValue, Entity hitter);
    
    /**
     * 添加EntityListener,用于侦听Entity被hit，或者hit目标属性
     * @param listener 
     */
    void addHitAttributeListener(HitAttributeListener listener);
    
    /**
     * 移除EntityListener.
     * @param listener
     * @return 
     * @see #addListener(name.huliqing.luoying.object.entity.EntityListener) 
     */
    boolean removeHitAttributeListener(HitAttributeListener listener);
    
    /**
     * 获取属性管理器
     * @return 
     */
    AttributeManager getAttributeManager();
    
    /**
     * 给实体添加模块
     * @param module 
     */
    void addModule(Module module);
    
    /**
     * 移除指定的角色模块
     * @param module
     * @return 
     */
    boolean removeModule(Module module);
    
    /**
     * 获取实体的所有模块
     * @return 
     */
    List<Module> getModules();
    
    /**
     * 获取指定类型的模块,如果不存在指定类型的模块则返回null.
     * @param <T>
     * @param moduleType
     * @return 
     */
    <T extends Module> T getModule(Class<T> moduleType);
    
    /**
     * 向实体添加数据
     * @param data
     * @param amount 添加的数量
     * @return true 如果成功添加
     */
    boolean addObjectData(ObjectData data, int amount);
    
    /**
     * 从实体中移除数据
     * @param data
     * @param amount 移除的数量
     * @return true如果成功移除
     */
    boolean removeObjectData(ObjectData data, int amount);
    
    /**
     * 让实体使用一个数据.
     * @param data 
     * @return true如果成功使用
     */
    boolean useObjectData(ObjectData data);
    
    /**
     * 添加数据侦听器,用于侦听实体中数据的流转.
     * @param listener 
     */
    void addDataListener(DataListener listener);
    
    /**
     * 从实体中移除指定的数据侦听器。
     * @param listener
     * @return true如果成功移除。
     */
    boolean removeDataListener(DataListener listener);
}
