package com.vicker.permission.helper.annotation;

import com.vicker.permission.helper.TransparentActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author: vicker
 * @date: 2021/10/19 0019 10:48
 * @desc: 权限被取消的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCancel {

    int requestCode() default TransparentActivity.PARAM_REQUEST_CODE_DEFAULT;
}