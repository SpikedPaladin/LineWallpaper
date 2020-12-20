package me.paladin.linewallpaper;

public class MovingPoint {
    public float x, y, dx, dy;
    
    public void init(int width, int height, float minStep) {
        x = (float) ((width - 1) * Math.random());
        y = (float) ((height - 1) * Math.random());
        dx = (float) (Math.random() * minStep * 2) + 1;
        dy = (float) (Math.random() * minStep * 2) + 1;
    }
    
    public float adjDelta(float cur, float minStep, float maxStep) {
        cur += (Math.random() * minStep) - (minStep / 2);
        if (cur < 0 && cur > -minStep) cur = -minStep;
        if (cur >= 0 && cur < minStep) cur = minStep;
        if (cur > maxStep) cur = maxStep;
        if (cur < -maxStep) cur = -maxStep;
        return cur;
    }
    
    public void step(int width, int height, float minStep, float maxStep) {
        x += dx;
        if (x <= 0 || x >= (width - 1)) {
            if (x <= 0) x = 0;
            else if (x >= (width - 1)) x = width - 1;
            dx = adjDelta(-dx, minStep, maxStep);
        }
        y += dy;
        if (y <= 0 || y >= (height - 1)) {
            if (y <= 0) y = 0;
            else if (y >= (height - 1)) y = height - 1;
            dy = adjDelta(-dy, minStep, maxStep);
        }
    }
}
