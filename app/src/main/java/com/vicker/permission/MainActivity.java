package com.vicker.permission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.vicker.permission.helper.annotation.Permission;
import com.vicker.permission.helper.annotation.PermissionCancel;
import com.vicker.permission.helper.annotation.PermissionDenied;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onRequestPermissionView(View view) {
        onRequestPermission();
    }

    @Permission(value = Manifest.permission.READ_EXTERNAL_STORAGE, requestCode = 200)
    public void onRequestPermission() {
        Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
    }

    @PermissionCancel(requestCode = 200)
    public void onPermissionCancel() {
        Log.w(TAG, "onPermissionCancel: ");
    }

    @PermissionDenied(requestCode = 200)
    public void onPermissionDenied() {
        Log.w(TAG, "onPermissionDenied: ");
    }
}