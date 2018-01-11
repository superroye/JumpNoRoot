package com.wolf.jumpnoroot.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.wolf.jumpnoroot.R;
import com.wolf.jumpnoroot.net.JumpServer;
import com.wolf.jumpnoroot.util.Utils;

/**
 * Created by Roye on 2017/11/7.
 */

public class TitService extends Service {

    WindowManager wm;
    View mView;
    WindowManager.LayoutParams mWindowLayoutParams;
    TextView expandTv;

    int maxScreenHeight;
    boolean isMaxScreen;

    JumpServer jumpServer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        jumpServer = new JumpServer();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        // type和flags待确定
        mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int densityDpi = displayMetrics.densityDpi;
        float scale = densityDpi / 160;
        final int screenHeight = displayMetrics.heightPixels;
        final int screenWidth = displayMetrics.widthPixels;
        maxScreenHeight = screenHeight - (int) (120 * scale + 0.5f);
        mWindowLayoutParams.height = maxScreenHeight;
        mWindowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        mWindowLayoutParams.alpha = 0.7f;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = 0;
        mWindowLayoutParams.y = 0;

        mView = LayoutInflater.from(this).inflate(R.layout.layout_tit, null);
        //mView.setFocusableInTouchMode(true);
        wm.addView(mView, mWindowLayoutParams);

        isMaxScreen = true;
        final int jumpClickY = mWindowLayoutParams.height + 60;
        final TiTView tiTView = mView.findViewById(R.id.titview);
        tiTView.setClickCallback(new ClickCallback() {
            @Override
            public void doJump(final long jumpMs) {
                int[] coors = Utils.getRandomCoordinate(new int[]{200, jumpClickY}, new int[]{screenWidth - 200, screenHeight - 10});
                String command = String.format("adb shell input swipe %d %d %d %d %d", coors[0], coors[1], coors[0], coors[1], jumpMs);
                android.util.Log.d("www","command "+command);
                jumpServer.sendCommand(command);
            }

            @Override
            public void onBack() {
            }
        });
        expandTv = mView.findViewById(R.id.expandTv);
        expandTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMini();
            }
        });
        mView.findViewById(R.id.closeIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeViewImmediate(mView);
                jumpServer.close();
            }
        });

        TextView descTv = mView.findViewById(R.id.descTv);
        descTv.setText(jumpServer.getAddress());
        setMini();
        return super.onStartCommand(intent, flags, startId);
    }

    void setMini() {
        if (isMaxScreen) {
            mWindowLayoutParams.height = 200;
            expandTv.setText("跳一跳辅助 点击展开");
        } else {
            mWindowLayoutParams.height = maxScreenHeight;
            expandTv.setText("跳一跳辅助 点击折叠");
        }
        wm.updateViewLayout(mView, mWindowLayoutParams);
        isMaxScreen = !isMaxScreen;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jumpServer.close();
    }
}
