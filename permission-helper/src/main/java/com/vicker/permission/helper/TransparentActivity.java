package com.vicker.permission.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.vicker.permission.helper.core.IPermission;

/**
 * @author: vicker
 * @date: 2021/10/19 0019 10:24
 * @desc: 一个透明的activity，专门处理权限的activity
 */
public class TransparentActivity extends Activity {

    /**
     * 定义权限处理的标识
     */
    private static final String PARAM_PERMISSION = "param_permission";
    private static final String PARAM_REQUEST_CODE = "param_permission_code";
    public static final int PARAM_REQUEST_CODE_DEFAULT = -1;

    /**
     * 存储用户需要申请的权限
     */
    private String[] permissions;
    /**
     * 请求权限码
     */
    private int requestCode;
    private static IPermission mPermissionListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        // 接收用户传递过来的权限名称和权限码
        permissions = getIntent().getStringArrayExtra(PARAM_PERMISSION);
        requestCode = getIntent().getIntExtra(PARAM_REQUEST_CODE, PARAM_REQUEST_CODE_DEFAULT);

        if (permissions == null && requestCode < 0 && mPermissionListener == null) {
            this.finish();
            return;
        }

        boolean permissionRequest = PermissionUtils.hasSelfPermissions(this, permissions);
        if (permissionRequest) {
            mPermissionListener.ganted();
            this.finish();
            return;
        }

        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.requestPermissionSuccess(grantResults)) {
            mPermissionListener.ganted();
            this.finish();
            return;
        }

        // 如果用户点击了拒绝（不再提示打钩选项）等操作
        if (!PermissionUtils.shouldShowRequestPermissionRationale(this, permissions)) {
            mPermissionListener.denied();
            this.finish();
            return;
        }

        // 如果执行到这里来，就说明权限被取消了
        mPermissionListener.cancel();
        this.finish();
        return;
    }

    /**
     * 当前activity结束的时候，不需要有动画效果
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
        mPermissionListener = null;
    }

    public static void requestPermissionAction(Context context, String[] permissions, int requestCode, IPermission iPermission) {
        mPermissionListener = iPermission;

        Intent intent = new Intent(context, TransparentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putStringArray(PARAM_PERMISSION, permissions);
        bundle.putInt(PARAM_REQUEST_CODE, requestCode);

        intent.putExtras(bundle);

        //屏蔽进入动画
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
        context.startActivity(intent);
    }
}