package com.wolf.jumpnoroot;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.wolf.jumpnoroot.ui.TitService;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        askForPermission();
    }

    /**
     * 请求用户给予悬浮窗的权限
     */
    public void askForPermission() {
        if (!checkAlertWindowsPermission(this)) {
            Toast.makeText(this, "当前无权限，请授权悬浮窗！", Toast.LENGTH_SHORT).show();

            String action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
            }
            Intent intent = new Intent(action,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            startService(new Intent(this, TitService.class));
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!checkAlertWindowsPermission(this)) {
                Toast.makeText(this, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限授予成功！", Toast.LENGTH_SHORT).show();
                //启动FxService
                startService(new Intent(this, TitService.class));
                finish();
            }

        }
    }

    public boolean checkAlertWindowsPermission(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

}
