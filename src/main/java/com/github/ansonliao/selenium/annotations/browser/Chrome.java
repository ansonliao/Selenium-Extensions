package com.github.ansonliao.selenium.annotations.browser;

import com.github.ansonliao.selenium.internal.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Chrome {
    String value() default "Chrome";
    String type() default Constants.BROWSER_ANNOTATION_TYPE_PROPERTY;
}
