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
package name.huliqing.luoying.object.scene;

import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import java.util.List;
import name.huliqing.luoying.data.SceneData;
import name.huliqing.luoying.xml.DataProcessor;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 游戏场景，存取游戏场景中对象的容器。创场景场景之后需要通过{@link #getRoot() }获取场景的根节点，
 * 并把场景根节点放到游戏根节点下才能进行渲染和显示。使用示例：
 * <code>
 * <pre>
 *  // 创建一个场景实例 
 *  Scene scene = new SceneXxx();
 *  // 指定要让哪些ViewPort作为Processor的容器
 *  scene.setProcessorViewPorts(getViewPort());
 *  // 把场景根节点添加到游戏根节点
 *  rootNode.attachChild(scene.getRoot());
 *  // 初始化场景
 *  scene.initialize();
 * </pre>
 * </code>
 * @author huliqing
 */
public interface Scene extends DataProcessor<SceneData> {

    /**
     * 设置场景数据，一般不要手动去调用。
     * @param data 
     */
    @Override
    public void setData(SceneData data);

    /**
     * 获取场景data，这个data包含了当前场景中的状态信息，如果需要获得场景的<b>实时</b>状态，应该先调用
     * {@link #updateDatas() }来进行更新。
     * @return 
     */
    @Override
    public SceneData getData();

    /**
     * 更新当前场景中的所有物体的状态到data中.一般在需要对场景进行存档之前需要调用这个方法来更新状态到SceneData中去。
     * @see #getData() 
     */
    @Override
    public void updateDatas();
    
    /**
     * 初始化entity容器
     */
    void initialize();
    
    /**
     * 判断容器是否已经初始化完毕
     * @return 
     */
    boolean isInitialized();
    
    /**
     * 清理释放资源
     */
    void cleanup();
    
    /**
     * 获取场景根节点
     * @return 
     */
    Node getRoot();
    
    /**
     * 添加一个场景物体.
     * @param entity 
     */
    void addEntity(Entity entity);
    
    /**
     * 从容器中移除指定场景物体，如果成功则返回true,移除失败或者物体不存在则返回false.
     * @param entity 
     */
    void removeEntity(Entity entity);
    
    /**
     * 通过唯一id来查找场景物体,如果不存在则反回null.
     * @param entityId
     * @return 
     */
    Entity getEntity(long entityId);
    
    /**
     * 获取当前场景中的所有实体，返回的实体只能只读使用。不应该直接操作该列表，比如直接删除或添加元素是不允许的。
     * 删除实体或添加实体需要通过指定接口。
     * @return 
     * @see #removeEntity(name.huliqing.luoying.object.entity.Entity) 
     * @see #addEntity(name.huliqing.luoying.object.entity.Entity) 
     */
    List<Entity> getEntities();
    
    /**
     * 获取场景中指定类型的物体。
     * @param <T>
     * @param type
     * @param store,存放结果,如果为null则会自动创建
     * @return 
     */
    <T extends Entity> List<T> getEntities(Class<T> type, List<T> store);
    
    /**
     * 获取场景中指定位置范围内的物体。
     * @param <T>
     * @param type
     * @param location
     * @param radius
     * @param store
     * @return 
     */
    <T extends Entity> List<T> getEntities(Class<T> type, Vector3f location, float radius, List<T> store);
    
    /**
     * 添加容器侦听器
     * @param listener 
     */
    void addSceneListener(SceneListener listener);
    
    /**
     * 移除容器侦听器
     * @param listener 
     * @return  
     */
    boolean removeSceneListener(SceneListener listener);
    
    /**
     * 给场景添加Processor， 这些SceneProcessor会添加到{@link #setProcessorViewPorts(ViewPort...) }<br>
     * 所指定的所有ViewPort中。如果没有设置setProcessorViewPorts则调用该方法不会有任何意义。
     * @param sceneProcessor 
     * @see #setProcessorViewPorts(com.jme3.renderer.ViewPort...) 
     */
    void addProcessor(SceneProcessor sceneProcessor);
    
    /**
     * 从场景中移除Processor
     * @param sceneProcessor
     */
    void removeProcessor(SceneProcessor sceneProcessor);
    
    /**
     * 给场景添加一个Post Filter
     * @param filter 
     */
    void addFilter(Filter filter);

    /**
     * 从场景中移除一个指定的Post Filter.
     * @param filter
     */
    void removeFilter(Filter filter);
    
    /**
     * 设置一些ViewPort,这些ViewPort用于作为Processor的容器.
     * 当向Scene添加SceneProcessor时将添加到这些ViewPort中。<br>
     * 用于支持一些环境类的物体，如：高级水体、阴影效果等，这些物体需要向场景中添加Processor和Filter,
     * 如果没有给场景设置ViewPort则可能这些环境物体会无效,或者异常。<br>
     * 注：这个方法应该在场景初始化之前设置,一般在游戏开始时或切换游戏场景时设置, 然后才调用initialize初始化场景。
     * @param viewPorts
     */
    void setProcessorViewPorts(ViewPort... viewPorts);
    
    /**
     * 当实体发生了某些状态变化时，可以调用这个方法来通知场景，这样允许某些监听器确认是否需要重新处理该实体。
     * 例如：当某个模型实体的开关（Enabled）状态发生变化时，可以调用这个方法,以便场景中的物体实体可以知道，并确认是否
     * 需要重新处理该实体，例如移除模型的物理特性控制器之类。
     * @param entity 
     */
    void notifyEntityStateChanged(Entity entity);
}
