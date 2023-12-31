package com.ppp;

import com.ppp.annotation.*;
import com.ppp.utils.Reflections;
import com.ppp.utils.maker.ClassUtils;

import java.util.List;

/**
 * @author Whoopsunix
 * 内存马生成
 */

public class JavaClassBuilder {
    private static String schedulerPackageName = "com.ppp.scheduler";

    public static byte[] build(JavaClassHelper javaClassHelper) throws Exception {
        String javaClassHelperType = javaClassHelper.getJavaClassHelperType();

        Class builderClass = null;

        List<Class<?>> schedulerClasses = ClassUtils.getClasses(schedulerPackageName);
        for (Class<?> clazz : schedulerClasses) {
            JavaClassHelperType javaClassHelperTypeAnnotation = clazz.getAnnotation(JavaClassHelperType.class);
            if (javaClassHelperTypeAnnotation == null) continue;

            if (javaClassHelperTypeAnnotation.value().equals(javaClassHelperType)) {
                builderClass = clazz;
                break;
            }
        }

        return (byte[]) Reflections.invokeMethod(builderClass.newInstance(), "build", javaClassHelper);

    }

}