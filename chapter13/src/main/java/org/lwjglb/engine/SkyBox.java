package org.lwjglb.engine;

import org.joml.Vector3f;
import org.lwjglb.engine.graph.Mesh;

public class SkyBox extends GameItem {

    private Vector3f colour;
    
    public SkyBox(Vector3f colour, Mesh mesh) {
        super(mesh);
        this.colour = colour;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

}
