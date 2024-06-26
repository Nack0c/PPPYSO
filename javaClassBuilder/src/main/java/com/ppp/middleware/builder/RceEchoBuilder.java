package com.ppp.middleware.builder;

import com.ppp.JavaClassHelper;
import com.ppp.annotation.Builder;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

/**
 * @author Whoopsunix
 */
@Builder(Builder.RceEcho)
public class RceEchoBuilder {
    public byte[] build(Class cls, JavaClassHelper javaClassHelper) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(cls));
//        classPool.importPackage("javax.servlet.http");
        classPool.importPackage("java.util");
        classPool.importPackage("java.lang.reflect");

        CtClass ctClass = classPool.getCtClass(cls.getName());

        JavaClassModifier.javaClassHelperInit(javaClassHelper);

        JavaClassModifier.ctClassBuilderNew(cls, ctClass, javaClassHelper);

        return JavaClassModifier.toBytes(ctClass);


//        // 字段信息修改
//        JavaClassModifier.fieldChange(cls, ctClass, javaClassHelper);
//
//        return JavaClassModifier.ctClassBuilderExt(ctClass, javaClassHelper);
    }
}
