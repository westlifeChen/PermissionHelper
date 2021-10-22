package com.vicker.permission.helper.aspect;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.vicker.permission.helper.TransparentActivity;
import com.vicker.permission.helper.annotation.Permission;
import com.vicker.permission.helper.annotation.PermissionCancel;
import com.vicker.permission.helper.annotation.PermissionDenied;
import com.vicker.permission.helper.core.IPermission;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


/**
 * @author: vicker
 * @date: 2021/10/19 0019 10:57
 * @desc:
 */
@Aspect
public class PermissionAspect {

    @Pointcut("execution(@com.vicker.permission.helper.annotation.Permission * *(..)) && @annotation(permission)")
    public void pointActionMethod(Permission permission) {

    }

    @Around("pointActionMethod(permission)")
    public void aProceedingJoinPoint(final ProceedingJoinPoint point, Permission permission) throws Throwable {
        Context context = null;
        final Object thisObject = point.getThis();

        if (thisObject instanceof Context) {
            context = (Context) thisObject;
        } else if (thisObject instanceof Fragment) {
            context = ((Fragment) thisObject).getActivity();
        } else if (thisObject instanceof android.view.View) {
            context = ((android.view.View) thisObject).getContext();
        }

        if (null == context && permission == null) {
            throw new IllegalAccessException("null == context || permission == null");
        }

        TransparentActivity.requestPermissionAction(context, permission.value(), permission.requestCode(), new IPermission() {
            @Override
            public void ganted() {
                try {
                    // 让使用@Permission修饰的函数正常执行
                    point.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public void denied() {
                invokeAnnotation(thisObject, PermissionDenied.class, permission.requestCode());
            }

            @Override
            public void cancel() {
                invokeAnnotation(thisObject, PermissionCancel.class, permission.requestCode());
            }
        });
    }

    private void invokeAnnotation(Object object, Class clazz, int requestCode) {
        //获取切面上下文的类型
        Class<?> clz = object.getClass();

        for (; ; ) {

            if (clz == null) {
                break;
            }

            String clazzName = clz.getCanonicalName();
            if (clazzName.startsWith("java.") || clazzName.startsWith("android.")) {
                break;
            }

            //获取类型中的方法
            Method[] methods = clz.getDeclaredMethods();
            if (methods == null) {
                continue;
            }

            for (Method method : methods) {
                //获取该方法是否有PermissionDenied注解
                boolean isHasAnnotation = method.isAnnotationPresent(clazz);
                Annotation annotation = method.getAnnotation(clazz);
                int annotationReqCode = -1;

                if (annotation instanceof PermissionDenied) {
                    PermissionDenied permissionDenied = (PermissionDenied) annotation;
                    annotationReqCode = permissionDenied.requestCode();
                } else if (annotation instanceof PermissionCancel) {
                    PermissionCancel permissionCancel = (PermissionCancel) annotation;
                    annotationReqCode = permissionCancel.requestCode();
                }
                if (isHasAnnotation) {
                    method.setAccessible(true);
                    try {
                        if (annotationReqCode == TransparentActivity.PARAM_REQUEST_CODE_DEFAULT || annotationReqCode == requestCode) {
                            // 如果code为默认值，就默认继续执行
                            // 如果回调的code与请求的code相同，就调用
                            method.invoke(object);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            clz = clz.getSuperclass();
        }
    }
}