package org.lwjglb.engine.graph.shadow;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjglb.engine.Window;
import org.lwjglb.engine.graph.Transformation;
import org.lwjglb.engine.graph.lights.DirectionalLight;

public class ShadowCascade {

    private static final int FRUSTUM_CORNERS = 8;

    private final Matrix4f projViewMatrix;

    private final Matrix4f orthoProjMatrix;

    private final Matrix4f lightViewMatrix;

    /**
     * Center of the view cuboid un world space coordinates.
     */
    private final Vector3f centroid;

    private final Vector3f[] frustumCorners;

    private final float zNear;

    private final float zFar;

    private final Vector4f tmpVec;
    
    public ShadowCascade(float zNear, float zFar) {
        this.zNear = zNear;
        this.zFar = zFar;
        this.projViewMatrix = new Matrix4f();
        this.orthoProjMatrix = new Matrix4f();
        this.centroid = new Vector3f();
        this.lightViewMatrix = new Matrix4f();
        this.frustumCorners = new Vector3f[FRUSTUM_CORNERS];
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            frustumCorners[i] = new Vector3f();
        }
        tmpVec = new Vector4f();
    }

    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }

    public Matrix4f getOrthoProjMatrix() {
        return orthoProjMatrix;
    }

    public void update(Window window, Matrix4f viewMatrix, DirectionalLight light) {
        // Build projection view matrix for this cascade
        float aspectRatio = (float) window.getWidth() / (float) window.getHeight();
        projViewMatrix.setPerspective(Window.FOV, aspectRatio, zNear, zFar);
        projViewMatrix.mul(viewMatrix);

        // Calculate frustum corners in world space
        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            corner.set(0, 0, 0);
            projViewMatrix.frustumCorner(i, corner);
            centroid.add(corner);
            centroid.div(8.0f);
            minZ = Math.min(minZ, corner.z);
            maxZ = Math.max(maxZ, corner.z);
        }

        // Go back from the centroid up to max.z - min.z in the direction of light
        Vector3f lightDirection = light.getDirection();
        Vector3f lightPosInc = new Vector3f().set(lightDirection);
        float distance = maxZ - minZ;
        lightPosInc.mul(distance);
        Vector3f lightPosition = new Vector3f();
        lightPosition.set(centroid);
        lightPosition.add(lightPosInc);

        updateLightViewMatrix(lightDirection, lightPosition);

        updateLightProjectionMatrix();
    }

    private void updateLightViewMatrix(Vector3f lightDirection, Vector3f lightPosition) {
        float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
        float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
        float lightAngleZ = 0;
        Transformation.updateGenericViewMatrix(lightPosition, new Vector3f(lightAngleX, lightAngleY, lightAngleZ), lightViewMatrix);
    }

    private void updateLightProjectionMatrix() {
        // Now calculate frustum dimensions in light space
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ =  Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            tmpVec.set(corner, 1);
            tmpVec.mul(lightViewMatrix);
            minX = Math.min(tmpVec.x, minX);
            maxX = Math.max(tmpVec.x, maxX);
            minY = Math.min(tmpVec.y, minY);
            maxY = Math.max(tmpVec.y, maxY);
            minZ = Math.min(tmpVec.z, minZ);
            maxZ = Math.max(tmpVec.z, maxZ);
        }
        float distz = maxZ - minZ;

        orthoProjMatrix.setOrtho(minX, maxX, minY, maxY, 0, distz);
    }

}
