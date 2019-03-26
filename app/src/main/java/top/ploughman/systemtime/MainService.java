package top.ploughman.systemtime;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class MainService extends Service {
    //要引用的布局文件.
    ConstraintLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;

    ImageButton imageButton1;

    Integer number = 0;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MainService Created");
        //OnCreate中来生成悬浮窗.
        createToucher();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 300;
        params.height = 300;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.main, null);
        //添加toucherlayout
        windowManager.addView(toucherLayout, params);

        Log.i(TAG, "toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG, "toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG, "toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG, "toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG, "状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);

        // 图片按钮设置了连续点击两下退出悬浮窗。关闭Service使用stopSelf()方法。
        imageButton1.setOnClickListener(new View.OnClickListener() {
            long[] hints = new long[2];

            @Override
            public void onClick(View v) {
                Log.i(TAG, "点击了");
                System.arraycopy(hints, 1, hints, 0, hints.length - 1);
                hints[hints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - hints[0] >= 700) {
                    Log.i(TAG, "要执行");
                    Toast.makeText(MainService.this, "连续点击两次以退出", Toast.LENGTH_SHORT).show();
//                    synchronized (number) {
//                        number += 1;
//                        if (number % 2 == 1) {
//                            setTime1();
//                        } else {
//                            setTime2();
//                        }
//
//                    }

                } else {
                    Log.i(TAG, "即将关闭");
                    stopSelf();
                }
            }
        });

        // 给图片按钮设置OnTouchListener，让悬浮窗可以拖动。

        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //ImageButton我放在了布局中心，布局一共300dp
                params.x = (int) event.getRawX() - 150;
                //这就是状态栏偏移量用的地方
                params.y = (int) event.getRawY() - 150 - statusBarHeight;
                windowManager.updateViewLayout(toucherLayout, params);
                return false;
            }
        });

        //其他代码...
    }

    /**
     * OnDestroy时销毁WindowManager
     */
    @Override
    public void onDestroy() {
        //用imageButton检查悬浮窗还在不在，这里可以不要。优化悬浮窗时要用到。
        if (imageButton1 != null) {
            windowManager.removeView(toucherLayout);
        }
        super.onDestroy();
    }

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void setTime1() {
        Calendar c = Calendar.getInstance();
        c.set(2019, 1, 1, 12, 00, 00);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setTime(c.getTimeInMillis());
    }

    private void setTime2() {
        Calendar c = Calendar.getInstance();
        c.set(2020, 1, 1, 12, 00, 00);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setTime(c.getTimeInMillis());
    }
}
