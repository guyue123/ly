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
package name.huliqing.luoying.object.attribute;

import name.huliqing.luoying.data.AttributeData;

/**
 * 可限制取值范围的属性,可以通过设置minValue,maxValue来直接限制当前值的取值范围，
 * 或者通过绑定其它属性来限制当前属性的取值范围。
 * @author huliqing
 */
public class LimitIntegerAttribute extends IntegerAttribute implements LimitAttribute, AttributeListener, ValueChangeListener{

    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;
    private String bindMinLimitAttribute;
    private String bindMaxLimitAttribute;
    private NumberAttribute minLimitAttribute;
    private NumberAttribute maxLimitAttribute;
    
    @Override
    public void setData(AttributeData data) {
        super.setData(data); 
        minValue = data.getAsInteger("minValue", minValue);
        maxValue = data.getAsInteger("maxValue", maxValue);
        bindMinLimitAttribute = data.getAsString("bindMinLimitAttribute");
        bindMaxLimitAttribute = data.getAsString("bindMaxLimitAttribute");
    }
    
    @Override
    public void updateDatas() {
        super.updateDatas();
        data.setAttribute("minValue", minValue);
        data.setAttribute("maxValue", maxValue);
        // bindMinLimitAttribute, bindMaxLimitAttribute不发生变化，所以不需要回设到data中去
//        data.setAttribute("bindMinLimitAttribute", bindMinLimitAttribute);
//        data.setAttribute("bindMaxLimitAttribute", bindMaxLimitAttribute);
    }

    @Override
    public float getMaxLimit() {
        return maxValue;
    }

    @Override
    public void setMax() {
        setValue(maxValue);
    }

    @Override
    public float getMinLimit() {
        return minValue;
    }

    @Override
    public void setMin() {
        setValue(minValue);
    }

    @Override
    public void initialize(AttributeManager module) {
        super.initialize(module);
        // 监听attributeModule，当module添加或减少属性时要重新获取绑定minLimitAttribute，maxLimitAttribute属性。
        // 注：只有在minLimitAttributeName或maxLimitAttributeName存在的情况下才需要这么做。
        // 并且这个侦听器也应该一直存在，因为模块可能在任何运行时增加或删除属性。
        if (bindMinLimitAttribute != null || bindMaxLimitAttribute != null) {
            module.addListener(this);
        }
        
        // 1.偿试绑定minLimitAttribute，注：这里有可能还找不到minLimitAttributeName指定的属性，因为属性的载入有先后，
        // 但没关系，如果这里没有载入，那么当minLimitAttributeName指定的属性在载入的时候一定会触发侦听器,
        // onAttributeAdded.这样也可以载入。
        // 2.如果minLimitAttributeName所指定的属性已经在当前属性之前已经载入，
        // 则触发器不会触发到（onAttributeAdded）, 所以这里也不能省略。
        if (bindMinLimitAttribute != null) {
            bindMinLimitAttribute(module.getAttribute(bindMinLimitAttribute, null));
        }
        
        // 同上
        if (bindMaxLimitAttribute != null) {
            bindMaxLimitAttribute(module.getAttribute(bindMaxLimitAttribute, null));
        }
        
        // 初始化值并触发一次侦听器
        setValue(intValue());
    }

    @Override
    public void cleanup() {
        if (minLimitAttribute != null) {
            minLimitAttribute.removeListener(this);
        }
        if (maxLimitAttribute != null) {
            maxLimitAttribute.removeListener(this);
        }
        super.cleanup(); 
    }

    @Override
    protected boolean doSetSimpleValue(Number newValue) {
        int valueSet = newValue.intValue();
        if (valueSet < minValue) {
            valueSet = minValue;
        }
        if (valueSet > maxValue) {
            valueSet = maxValue;
        }
        return super.doSetSimpleValue(valueSet);
    }
    
    // 值变侦听，当绑定的minLimitAttribute或maxLimitAttribute的值发生变化时，当前属性的值会受影响。
    @Override
    public void onValueChanged(Attribute attribute) {
        if (attribute == minLimitAttribute) {
            minValue = minLimitAttribute.intValue();
            setValue(intValue());
        }
        if (attribute == maxLimitAttribute) {
            maxValue = maxLimitAttribute.intValue();
            setValue(intValue());
        }
    }

    // 监听attributeModule是否添加了新的属性，如果新添加的属性刚好与当前属性需要用来限制值大小的属性名称一致，
    // 则需要重新绑定这些属性
    @Override
    public void onAttributeAdded(AttributeManager actor, Attribute attribute) {
        if (attribute == this)
            return;
        
        if (bindMinLimitAttribute != null && attribute.getName().equals(bindMinLimitAttribute)) {
            bindMinLimitAttribute(attribute);
        }
        if (bindMaxLimitAttribute != null && attribute.getName().equals(bindMaxLimitAttribute)) {
            bindMaxLimitAttribute(attribute);
        }
    }

    // 监听attributeModule是否移除了属性，如果移除的属性刚好是当前需要用来限制值大小的属性，
    // 则需要移除对这些属性的侦听,因为这些属性已经移除，则不能再用它们来限制当前属性的值大小。
    @Override
    public void onAttributeRemoved(AttributeManager actor, Attribute otherAttribute) {
        if (otherAttribute == this) {
            if (minLimitAttribute != null) {
                minLimitAttribute.removeListener(this);
            }
            if (maxLimitAttribute != null) {
                maxLimitAttribute.removeListener(this);
            }
        }
        if (otherAttribute == minLimitAttribute) {
            minLimitAttribute.removeListener(this);
            minLimitAttribute = null;
        }
        if (otherAttribute == maxLimitAttribute) {
            maxLimitAttribute.removeListener(this);
            maxLimitAttribute = null;
        }
    }

    private void bindMinLimitAttribute(Attribute otherAttribute) {
        if (otherAttribute == null)
            return;
                
        // 移除旧的侦听器
        if (minLimitAttribute != null) {
            minLimitAttribute.removeListener(this);
        }
        
        // 从store中找出需要绑定的用于限制最小值或最高值的attribute
        if (!(otherAttribute instanceof NumberAttribute)) {
            throw new IllegalArgumentException("\"minLimitAttributeName\" property of LimitIntegerAttribute"
                    + " only support NumberAttribute,"
                    + " but found other attribute, attribute=" + otherAttribute 
                    + ", attributeId=" + otherAttribute.getId() 
                    + ", attributeName=" + otherAttribute.getName());
        }
        minLimitAttribute = (NumberAttribute) otherAttribute;
        minLimitAttribute.addListener(this);
        if (minLimitAttribute.isInitialized()) {
            minValue = minLimitAttribute.intValue();
            setValue(intValue());
        }
    }
    
    private void bindMaxLimitAttribute(Attribute otherAttribute) {
        if (otherAttribute == null)
            return;
        
        // 移除旧的侦听器
        if (maxLimitAttribute != null) {
            maxLimitAttribute.removeListener(this);
        }
        
        // 从store中找出需要绑定的用于限制最小值或最高值的attribute
        if (!(otherAttribute instanceof NumberAttribute)) {
            throw new IllegalArgumentException("\"maxLimitAttributeName\" property of LimitIntegerAttribute"
                    + " only support NumberAttribute,"
                    + " but found other attribute, attribute=" + otherAttribute 
                    + ", attributeId=" + otherAttribute.getId() 
                    + ", attributeName=" + otherAttribute.getName());
        }
        maxLimitAttribute = (NumberAttribute) otherAttribute;
        maxLimitAttribute.addListener(this);
        if (maxLimitAttribute.isInitialized()) {
            maxValue = maxLimitAttribute.intValue();
            setValue(intValue());
        }
    }
}
