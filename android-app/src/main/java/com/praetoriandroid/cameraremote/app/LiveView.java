package com.praetoriandroid.cameraremote.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.UiThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@EView
public class LiveView extends SurfaceView implements SurfaceHolder.Callback {

    private static class BitmapHolder {
        private Bitmap bitmap;

        public BitmapHolder(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    private static class LiveViewThread extends Thread {

        private static final BitmapHolder STOP_MARKER = new BitmapHolder(null);

        private final SurfaceHolder surfaceHolder;
        private final BlockingQueue<BitmapHolder> frameQueue = new LinkedBlockingQueue<BitmapHolder>(2);
        private volatile boolean run;
        private Rect viewRect = new Rect();
        private int canvasWidth = 1;
        private int canvasHeight = 1;

        public LiveViewThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void startShow() {
            synchronized (surfaceHolder) {
                run = true;
                start();
            }
        }

        public void stopShow() {
            synchronized (surfaceHolder) {
                frameQueue.clear();
                if (!frameQueue.offer(STOP_MARKER)) {
                    if (BuildConfig.DEBUG) {
                        throw new IllegalStateException();
                    }
                    interrupt();
                }
                run = false;
            }
            boolean retry = true;
            while (retry) {
                try {
                    join();
                    retry = false;
                } catch (InterruptedException ignored) {
                }
            }
        }

        public void putFrame(Bitmap frame) {
            synchronized (surfaceHolder) {
                frameQueue.offer(new BitmapHolder(frame));
            }
        }

        @Override
        public void run() {
            while (run) {
                Canvas canvas = null;
                try {
                    BitmapHolder frameHolder = frameQueue.take();
                    if (frameHolder == STOP_MARKER) {
                        return;
                    }
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        if (run) {
                            drawFrame(canvas, frameHolder.getBitmap());
                        }
                    }
                } catch (InterruptedException e) {
                    interrupt();
                    return;
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void drawFrame(Canvas c, Bitmap frame) {
            float frameWidth = frame.getWidth();
            float frameHeight = frame.getHeight();
            float scaleFactor = Math.min(canvasWidth / frameWidth, canvasHeight / frameHeight);
            int viewportWidth = Math.round(scaleFactor * frameWidth);
            int viewportHeight = Math.round(scaleFactor * frameHeight);

            viewRect.left = (canvasWidth - viewportWidth) / 2;
            viewRect.top = (canvasHeight - viewportHeight) / 2;
            viewRect.right = viewRect.left + viewportWidth;
            viewRect.bottom = viewRect.top + viewportHeight;

            c.drawBitmap(frame, null, viewRect, null);
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (surfaceHolder) {
                canvasWidth = width;
                canvasHeight = height;
            }
        }
    }

    private volatile LiveViewThread liveViewThread;

    public LiveView(Context context) {
        this(context, null, 0);
    }

    public LiveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        SurfaceHolder surfaceHolder = getHolder();
        assert surfaceHolder != null;
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        liveViewThread = new LiveViewThread(holder);
        liveViewThread.startShow();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        liveViewThread.stopShow();
        liveViewThread = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        liveViewThread.setSurfaceSize(width, height);
    }

    public void putFrame(Bitmap frame) {
        LiveViewThread thread = liveViewThread;
        if (thread != null) {
            thread.putFrame(frame);
        }
    }

}
