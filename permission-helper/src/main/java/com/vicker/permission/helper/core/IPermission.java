package com.vicker.permission.helper.core;

/**
 * @author: vicker
 * @date: 2021/10/19 0019 10:28
 * @desc:
 */
public interface IPermission {

    /**
     * 权限已经授权信
     */
    void ganted();

    /**
     * 拒绝授权
     */
    void denied();

    /**
     * 取消授权
     */
    void cancel();
}