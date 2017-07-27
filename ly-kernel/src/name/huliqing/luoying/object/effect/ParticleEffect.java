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
package name.huliqing.luoying.object.effect;

import com.jme3.effect.Particle;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import name.huliqing.luoying.data.EffectData;
import name.huliqing.luoying.data.EmitterData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.emitter.Emitter;

/**
 *
 * @author huliqing
 */
public class ParticleEffect extends Effect {
    private EmitterData emitterData;
    
    // 是否立即执行所有粒子发射
    private boolean emitAll;
    // 是否随机颜色
    private boolean randomColor;
    // 粒子是否使用世界坐标位置，默认true,与粒子的默认设置相同
    private boolean inWorldSpace = true;
    // 粒子材质的混合模式，默认AlphaAdditive,这意味着粒子图片中的黑色背
    // 景会被过滤掉。这会导致无法创建黑色烟雾的效果，这个时候可以使用
    // Alpha模式来代替。
    private BlendMode blendMode = BlendMode.AlphaAdditive;
    
    private Bucket bucket;
    
    // -- Inner
    private ParticleEmitter pe;
    private boolean emitted;

    @Override
    public void setData(EffectData data) {
        super.setData(data); 
        emitterData = data.getAsObjectData("emitter");
        
        emitAll = data.getAsBoolean("emitAll", emitAll);
        randomColor = data.getAsBoolean("randomColor", randomColor);
        inWorldSpace = data.getAsBoolean("inWorldSpace", inWorldSpace);
        String tempBlendMode = data.getAsString("blendMode");
        if (tempBlendMode != null) {
            blendMode = BlendMode.valueOf(tempBlendMode);
        }
        String tempBucket = data.getAsString("queueBucket");
        if (tempBucket != null) {
            bucket = Bucket.valueOf(tempBucket);
        }
    }
    
    @Override
    public void initialize() {
        super.initialize();
        Emitter em = Loader.load(emitterData);
        if (randomColor) {
            pe = em.getParticleEmitter(new RandomColorEmitter());
        } else {
            pe = em.getParticleEmitter();
        }
        // 这里必须从data中偿试获取Bucket设置，因为粒子有可能是在“世界”空间产生的，即粒子可能不是放在animNode
        // 下的，所以在父节点effect上的Bucket设置并不一定适应于这些粒子，这里必须直接把bucket的配置设置到粒子上。
        if (bucket != null) {
            pe.setQueueBucket(bucket);
        }
        pe.setInWorldSpace(inWorldSpace);
        pe.getMaterial().getAdditionalRenderState().setBlendMode(blendMode);
        animNode.attachChild(pe);
    }

    @Override
    protected void effectUpdate(float tpf) {
        super.effectUpdate(tpf);
        if (emitAll && !emitted) {
            pe.emitAllParticles();
            emitted = true;
        }
    }

    @Override
    public void cleanup() {
        emitted = false;
        if (pe != null) {
            pe.killAllParticles();
        }
        super.cleanup();
    }
    
    //------------- get and set
    
    // 主要是为了随机颜色。
    private class RandomColorEmitter extends ParticleEmitter {
        
        public RandomColorEmitter() {
            super("MyEmitter", ParticleMesh.Type.Triangle, 0);
        }
        
        @Override
        public void setParticleInfluencer(ParticleInfluencer particleInfluencer) {
            super.setParticleInfluencer(particleInfluencer); 
        }

        @Override
        public void emitAllParticles() {
            super.emitAllParticles();
            changeColor();
        }

        @Override
        public void updateFromControl(float tpf) {
            super.updateFromControl(tpf);
            changeColor();
        }
        
        private void changeColor() {
            if (this.isEnabled()) {
                Particle[] particles = this.getParticles();
                if (particles != null && particles.length > 0) {
                    for (Particle p : particles) {
                        if (p.life <= 0) { 
                            continue;
                        }
                        p.color.set(FastMath.nextRandomFloat(), FastMath.nextRandomFloat(), FastMath.nextRandomFloat(), 1);
                    }
                }                
            }
        }
    }
}
