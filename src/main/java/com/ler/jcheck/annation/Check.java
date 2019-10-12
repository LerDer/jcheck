package com.ler.jcheck.annation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lww
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
//这个注解就是可以让一个注解同一个方法上标注多次
@Repeatable(CheckContainer.class)
public @interface Check {

	String ex() default "";

	String msg() default "";

}
