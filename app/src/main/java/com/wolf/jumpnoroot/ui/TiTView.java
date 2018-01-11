package com.wolf.jumpnoroot.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Roye on 2018/1/8.
 */

public class TiTView extends View {

    int action;
    ClickCallback mClickCallback;
    int screenWidth;
    ImagePosColor[] poss = new ImagePosColor[]{new ImagePosColor(), new ImagePosColor()};
    int count = 0;


    public TiTView(Context context) {
        super(context, null);
        setBackgroundColor(0x00000000);
    }

    public TiTView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x00000000);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        action = event.getAction();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            android.util.Log.d("www", "ACTION_UP");
            poss[count].x = (int) event.getX();
            poss[count].y = (int) event.getY();

            if (count == 1) {
                double _x = Math.abs(poss[0].x - poss[1].x);
                double _y = Math.abs(poss[0].y - poss[1].y);
                int jumpMs = (int) (Math.sqrt(_x * _x + _y * _y) * 1.45);
                if (mClickCallback != null) {
                    mClickCallback.doJump(jumpMs);
                }
                count = 0;
            } else {
                count++;
            }
        } else if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            android.util.Log.d("www", "ACTION_POINTER_DOWN");
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            android.util.Log.d("www", "ACTION_DOWN " + event.getX() + " " + event.getY());
        }

        return true;
    }

    public void setClickCallback(ClickCallback clickCallback) {
        this.mClickCallback = clickCallback;
    }

}
