package com.xiaohang.project.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permission verification
 *
 * @author xiaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {


    /**
     * must have a role
     *
     * @return
     */
    String mustRole() default "";

}