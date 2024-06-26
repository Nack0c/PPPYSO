package com.ppp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java Class Loader 类型
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Builder {
    String Loader = "Loader";
    String MS = "MS";
    String RceEcho = "RceEcho";

    String value();
}
