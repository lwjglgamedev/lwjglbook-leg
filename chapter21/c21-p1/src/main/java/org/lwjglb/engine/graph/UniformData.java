package org.lwjglb.engine.graph;

import java.nio.FloatBuffer;

public class UniformData {

    private final int uniformLocation;
    
    private FloatBuffer floatBuffer;
    
    public UniformData(int uniformLocation) {
        this.uniformLocation = uniformLocation;
    }

    public int getUniformLocation() {
        return uniformLocation;
    }

    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    public void setFloatBuffer(FloatBuffer floatBuffer) {
        this.floatBuffer = floatBuffer;
    }
}
