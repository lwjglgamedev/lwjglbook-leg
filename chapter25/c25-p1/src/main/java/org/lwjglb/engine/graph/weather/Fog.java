package org.lwjglb.engine.graph.weather;

import org.joml.Vector3f;

public class Fog {

    private boolean active;

    private Vector3f colour;

    private float density;

    public static Fog NOFOG = new Fog();
    
    public Fog() {
        active = false;
        this.colour = new Vector3f(0, 0, 0);
        this.density = 0;
    }

    public Fog(boolean active, Vector3f colour, float density) {
        this.colour = colour;
        this.density = density;
        this.active = active;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the color
     */
    public Vector3f getColour() {
        return colour;
    }

    /**
     * @param colour the color to set
     */
    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    /**
     * @return the density
     */
    public float getDensity() {
        return density;
    }

    /**
     * @param density the density to set
     */
    public void setDensity(float density) {
        this.density = density;
    }
}
