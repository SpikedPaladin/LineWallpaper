package me.paladin.linewallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class LineWallpaper extends WallpaperService {
    SharedPreferences pref;
    
    @Override
    public Engine onCreateEngine() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        return new LineWallpaperEngine();
    }
    
    public class LineWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawThread = this::drawFrame;
        
        private boolean visible;
        private int delay;
        float minStep;
        float maxStep;
        
        boolean initialized;
        MovingPoint point1 = new MovingPoint();
        MovingPoint point2 = new MovingPoint();
        
        static final int NUM_OLD = 100;
        int numOld = 0;
        float[] old = new float[NUM_OLD*4];
        int[] oldColor = new int[NUM_OLD];
        int brightLine = 0;
        
        MovingPoint color = new MovingPoint();
        
        Paint background = new Paint();
        Paint foreground = new Paint();
        
        public LineWallpaperEngine() {
            background.setColor(0xff000000);
            foreground.setColor(0xff00ffff);
            updateValues();
        }
        
        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawThread);
        }
        
        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            updateValues();
            if (visible) {
                drawFrame();
            } else {
                handler.removeCallbacks(drawThread);
            }
        }
        
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            drawFrame();
        }
        
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            
            drawFrame();
        }
        
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            
            visible = false;
            handler.removeCallbacks(drawThread);
        }
        
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            super.onOffsetsChanged(xOffset, yOffset, xStep, yStep, xPixels, yPixels);
            
            
            drawFrame();
        }
        
        private void updateValues() {
            delay = Integer.parseInt(pref.getString("delay", "0"));
            foreground.setStrokeWidth(Float.parseFloat(pref.getString("lineWidth", "1")));
            minStep = Float.parseFloat(pref.getString("minStep", "2"));
            maxStep = Float.parseFloat(pref.getString("maxStep", "6"));
            foreground.setAntiAlias(pref.getBoolean("antialiasing", false));
        }
        
        int makeGreen(int index) {
            int dist = Math.abs(brightLine - index);
            if (dist > 10) return 0;
            return (255 - (dist * (255 / 10))) << 8;
        }
        
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (!initialized) {
                        initialized = true;
                        point1.init(canvas.getWidth(), canvas.getHeight(), minStep);
                        point2.init(canvas.getWidth(), canvas.getHeight(), minStep);
                        color.init(127, 127, 1);
                    } else {
                        point1.step(canvas.getWidth(), canvas.getHeight(),
                                minStep, maxStep);
                        point2.step(canvas.getWidth(), canvas.getHeight(),
                                minStep, maxStep);
                        color.step(127, 127, 1, 3);
                    }
                    brightLine += 2;
                    if (brightLine > (NUM_OLD * 2)) {
                        brightLine = -2;
                    }
                    
                    canvas.drawColor(background.getColor());
                    
                    for (int i = numOld - 1; i >= 0; i--) {
                        foreground.setColor(oldColor[i] | makeGreen(i));
                        foreground.setAlpha(((NUM_OLD - i) * 255) / NUM_OLD);
                        int p = i * 4;
                        canvas.drawLine(old[p], old[p + 1], old[p + 2], old[p + 3], foreground);
                    }
                    
                    int red = (int) color.x + 128;
                    if (red > 255) red = 255;
                    int blue = (int) color.y + 128;
                    if (blue > 255) blue = 255;
                    int color = 0xff000000 | (red << 16) | blue;
                    foreground.setColor(color | makeGreen(-2));
                    canvas.drawLine(point1.x, point1.y, point2.x, point2.y, foreground);
                    
                    if (numOld > 1) {
                        System.arraycopy(old, 0, old, 4, (numOld - 1) * 4);
                        System.arraycopy(oldColor, 0, oldColor, 1, numOld - 1);
                    }
                    if (numOld < NUM_OLD) numOld++;
                    old[0] = point1.x;
                    old[1] = point1.y;
                    old[2] = point2.x;
                    old[3] = point2.y;
                    oldColor[0] = color;
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
            
            handler.removeCallbacks(drawThread);
            if (visible) {
                handler.postDelayed(drawThread, delay);
            }
        }
    }
}
