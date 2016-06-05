package org.lwjglb.engine;

public class Timer {

    private double lastLoopTime;
    
    public void init() {
        lastLoopTime = getTime();
    }

    public double getTime() {
        return System.nanoTime() / 1000_000_000.0;
    }

    public float getEllapsedTime() {
        double time = getTime();
        float ellapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return ellapsedTime;
    }

    public double getLastLoopTime() {
        return lastLoopTime;
    }
}