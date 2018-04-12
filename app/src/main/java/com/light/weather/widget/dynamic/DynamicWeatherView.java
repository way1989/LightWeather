package com.light.weather.widget.dynamic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.animation.AnimationUtils;

import java.lang.ref.WeakReference;


/**
 * Created by liyu on 2017/8/16.
 */

public class DynamicWeatherView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "DynamicWeatherView";
    private int mFromColor;
    private DrawThread mDrawThread;
    private BaseWeatherType mWeatherType;
    private int mViewWidth;
    private int mViewHeight;

    public DynamicWeatherView(Context context) {
        this(context, null);
    }

    public DynamicWeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWeatherType = new DefaultType(context.getResources());
        setSurfaceTextureListener(this);
    }

    public int getColor() {
        return mWeatherType.getWeatherColor();
    }

    public void setType(final BaseWeatherType type) {
        Log.i(TAG, "setType: mWeatherType = " + mWeatherType);
        if (mWeatherType != null) {
            mWeatherType.end();
            mWeatherType.endAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mFromColor = mWeatherType.getWeatherColor();
                    mWeatherType = type;
                    mDrawThread.setWeatherType(type);
                    if (mWeatherType != null) {
                        mWeatherType.onSizeChanged(mViewWidth, mViewHeight);
                        mWeatherType.startAnimation(mFromColor);
                    }
                }
            });
        } else {
            mFromColor = type.getWeatherColor();
            mWeatherType = type;
            mWeatherType.onSizeChanged(mViewWidth, mViewHeight);
            mWeatherType.startAnimation(mFromColor);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        if (mWeatherType != null) {
            mWeatherType.onSizeChanged(w, h);
        }
    }

    public void onResume() {
        if (mDrawThread != null) {
            mDrawThread.setSuspend(false);
        }
    }

    public void onPause() {
        if (mDrawThread != null) {
            mDrawThread.setSuspend(true);
        }
    }

    public void onDestroy() {
        mDrawThread.setRunning(false);
        setSurfaceTextureListener(null);
        if (mWeatherType != null) {
            mWeatherType.end();
            mWeatherType.endAnimation(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: ");
        mDrawThread = new DrawThread(this);
        mDrawThread.setWeatherType(mWeatherType);
        mDrawThread.setRunning(true);
        mDrawThread.start();
        mWeatherType.startAnimation(mWeatherType.getWeatherColor());
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged: ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed: ");
        mDrawThread.setRunning(false);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Log.i(TAG, "onSurfaceTextureUpdated: ");
    }

    private static class DrawThread extends Thread {
        private final Object mObject = new Object();
        private WeakReference<TextureView> mSurfaceHolderWeakReference;
        private WeakReference<BaseWeatherType> mWeatherTypeWeakReference;
        private boolean mIsRunning = false;
        private boolean mSuspended = false;

        DrawThread(TextureView holder) {
            mSurfaceHolderWeakReference = new WeakReference<>(holder);
        }

        void setWeatherType(BaseWeatherType weatherType) {
            if (mWeatherTypeWeakReference != null) {
                mWeatherTypeWeakReference.clear();
            }
            mWeatherTypeWeakReference = new WeakReference<>(weatherType);
        }

        void setRunning(boolean running) {
            Log.d(TAG, "setRunning: running = " + running);
            mIsRunning = running;
            if (!running) {
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        }

        void setSuspend(boolean suspend) {
            Log.d(TAG, "setSuspend: suspend = " + suspend);
            this.mSuspended = suspend;
            if (!suspend) {
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        }

        @Override
        public void run() {
            while (mIsRunning) {
                if (mSuspended) {
                    try {
                        synchronized (mObject) {
                            Log.d(TAG, "mObject: wait  start...");
                            mObject.wait();
                            Log.d(TAG, "mObject: wait  end...");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mIsRunning) {
                    return;
                }
                TextureView holder = mSurfaceHolderWeakReference.get();
                BaseWeatherType weatherType = mWeatherTypeWeakReference.get();
                if (holder == null || weatherType == null) {
                    continue;
                }
                final long startTime = AnimationUtils.currentAnimationTimeMillis();
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    weatherType.onDrawElements(canvas);
                    holder.unlockCanvasAndPost(canvas);
                    final long drawTime = AnimationUtils.currentAnimationTimeMillis() - startTime;
                    final long needSleepTime = 16 - drawTime;
                    if (needSleepTime > 0) {
                        SystemClock.sleep(needSleepTime);
                    }
                }

            }
        }

    }
}
