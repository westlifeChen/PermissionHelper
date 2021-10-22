package com.vicker.permission.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.collection.SimpleArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vicker.permission.helper.menu.DefaultSettings;
import com.vicker.permission.helper.menu.IMenu;
import com.vicker.permission.helper.menu.OppoSettings;
import com.vicker.permission.helper.menu.VivoSettings;

import java.util.HashMap;

/**
 * @author: vicker
 * @date: 2021/10/19 0019 10:36
 * @desc:
 */
public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private static SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;

    static {
        MIN_SDK_PERMISSIONS = new SimpleArrayMap<>(8);
        MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", 14);
        MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", 20);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", 9);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.SYSTEM_ALERT_WINDOW", 23);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_SETTINGS", 23);
    }


    private static HashMap<String, Class<? extends IMenu>> permissionMenu = new HashMap<>();

    private static final String MANUFACTURER_DEFAULT = "Default";

    public static final String MANUFACTURER_HUAWEI = "huawei";
    public static final String MANUFACTURER_MEIZU = "meizu";
    public static final String MANUFACTURER_XIAOMI = "xiaomi";
    public static final String MANUFACTURER_OPPO = "oppo";
    public static final String MANUFACTURER_VIVO = "vivo";
    public static final String MANUFACTURER_SAMSUNG = "samsung";

    static {
        permissionMenu.put(MANUFACTURER_DEFAULT, DefaultSettings.class);
        permissionMenu.put(MANUFACTURER_OPPO, OppoSettings.class);
        permissionMenu.put(MANUFACTURER_VIVO, VivoSettings.class);
    }

    /**
     * 检查所有的权限是否被授权，被授予会返回0
     */
    public static boolean requestPermissionSuccess(int... grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static boolean permissionExists(String permission) {
        Integer minVersion = MIN_SDK_PERMISSIONS.get(permission);
        return minVersion == null || minVersion <= Build.VERSION.SDK_INT;
    }

    /**
     * 检查需要给予的权限是否需要显示理由
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            // 这个API主要用于给用户一个申请权限的解释，该方法只有在用户在上一次已经拒绝过你的这个权限申请。
            // 也就是说，用户已经拒绝一次了，你又弹个授权框，你需要给用户一个解释，为什么要授权，则使用该方法。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测 一些权限 是否都授权。 都授权则返回true，如果还未授权则返回false
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            //如果权限还未授权
            if (permissionExists(permission) && !hasSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSelfPermission(Context context, String permission) {
        try {
            // ContextCompat.checkSelfPermission，主要用于检测某个权限是否已经被授予。
            // 方法返回值为PackageManager.PERMISSION_DENIED或者PackageManager.PERMISSION_GRANTED
            // 当返回DENIED就需要进行申请授权了。
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException t) {
            return false;
        }
    }


    public static void setManuFacturer(String brand, Class<? extends IMenu> clazz) {
        permissionMenu.put(brand.toLowerCase(), clazz);
    }

    /**
     * 前往权限设置菜单的工具类
     * 如果被拒绝，可以弹一个对话框，询问是否要前往系统权限页面让用户自己手动开启。
     * 如果需要的话，可以通过以下代码前往（对话框自行解决，本框架不包含）
     */
    public static void goToMenu(Context context) {

        Class clazz = permissionMenu.get(Build.MANUFACTURER.toLowerCase());
        if (clazz == null) {
            clazz = permissionMenu.get(MANUFACTURER_DEFAULT);
        }

        try {
            IMenu iMenu = (IMenu) clazz.newInstance();

            Intent menuIntent = iMenu.getMenuIntent(context);
            if (menuIntent == null) {
                return;
            }
            context.startActivity(menuIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}