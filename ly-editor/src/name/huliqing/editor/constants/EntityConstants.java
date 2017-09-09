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
 */
public class EntityConstants {
    
	/**
	 * 直射光
	 */
    public final static String ENTITY_DIRECTIONAL_LIGHT = "entityDirectionalLight";
    
    /**
     * 环境光
     */
    public final static String ENTITY_AMBIENT_LIGHT = "entityAmbientLight";
    
    /**
     * 点光源
     */
    public final static String ENTITY_POINT_LIGHT = "entityPointLight";
    
    /**
     * 聚光源
     */
    public final static String ENTITY_SPOT_LIGHT = "entitySpotLight";
    
    /**
     * Bloom高光
     */
    public final static String ENTITY_BLOOM_FILTER = "entityBloomFilter";
    
    /**
     * 快速近似抗锯齿(Fast Approximate Anti-Aliasing)-
     */
    public final static String ENTITY_FXAA_FILTER = "entityFXAAFilter";
    
    /**
     * 雾化
     */
    public final static String ENTITY_FOG_FILTER = "entityFogFilter";
    
    /**
     * 光散射
     */
    public final static String ENTITY_LIGHTSCATTERING_FILTER = "entityLightScatteringFilter";
    
    /**
     * 屏幕空间环境光遮蔽（Screen-Space Ambient Occlusion）
     */
    public final static String ENTITY_SSAOFILTER = "entitySSAOFilter";
    
    /**
     * 阴影
     */
    public final static String ENTITY_DIRECTIONAL_LIGHTSHADOW_FILTER = "entityDirectionalLightShadowFilter";
    
    /**
     * 普通水体，可适用移动平台
     */
    public final static String ENTITY_SIMPLE_WATER = "entitySimpleWater";
    
    /**
     * 高级水体, 海水环境，不支持移动设备
     */
    public final static String ENTITY_ADVANCE_WATER = "entityAdvanceWater";
    
    /**
     * Instanced节点实体
     */
    public final static String ENTITY_INSTANCED = "entityInstanced";
    
    /**
     * BatchNode节点实体
     */
    public final static String ENTITY_BATCH = "entityBatch";
    
    /**
     * 模型
     */
    public final static String ENTITY_SIMPLE_MODEL = "entitySimpleModel";
    
    /**
     * 天空盒
     */
    public final static String ENTITY_SKYBOX = "entitySkyBox";
    
    /**
     * 地形
     */
    public final static String ENTITY_SIMPLE_TERRAIN = "entitySimpleTerrain";
    
    /**
     * 物理环境
     */
    public final static String ENTITY_PHYSICS = "entityPhysics";
    
    /**
     * 物理跟随相机
     */
    public final static String ENTITY_CHASEC_AMERA = "entityChaseCamera";
    
    /**
     * 声音
     */
    public final static String ENTITY_AUDIO = "entityAudio";
    
    /**
     * 特效: 默认的空效果实体，无任何特效，需要在程序中运行时设置效果
     */
    public final static String ENTITY_EFFECT = "entityEffect";
    
    /**
     * 寻路网格功能: 需要动态设置网格
     */
    public final static String ENTITY_NAVMESH = "entityNavMesh";
}
