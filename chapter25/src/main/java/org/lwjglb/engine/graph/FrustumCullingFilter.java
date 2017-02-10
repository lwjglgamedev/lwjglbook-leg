package org.lwjglb.engine.graph;

import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjglb.engine.items.GameItem;

public class FrustumCullingFilter {

    private static final int NUM_PLANES = 6;

    private final Matrix4f prjViewMatrix;

    private final Vector4f[] frustumPlanes;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumPlanes = new Vector4f[NUM_PLANES];
        for (int i = 0; i < NUM_PLANES; i++) {
            frustumPlanes[i] = new Vector4f();
        }
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Get frustum planes
        for (int i = 0; i < NUM_PLANES; i++) {
            prjViewMatrix.frustumPlane(i, frustumPlanes[i]);
        }
    }

    public void filter(Map<? extends Mesh, List<GameItem>> mapMesh) {
        for(Map.Entry<? extends Mesh, List<GameItem>> entry : mapMesh.entrySet()) {
            List<GameItem> gameItems = entry.getValue();     
            filter(gameItems, entry.getKey().getBoundingRadius());
        }
    }
    
    public void filter(List<GameItem> gameItems, float meshBoundingRadious) {
        float boundingRadious;
        for(GameItem gameItem : gameItems) {
            boundingRadious = gameItem.getScale() * meshBoundingRadious;
            gameItem.setInsideFrustum(insideFrustum(gameItem, boundingRadious));
        }
    }
    
    public boolean insideFrustum(GameItem gameItem, float boundingRadious) {
        boolean result = true;
        for(int i=0; i<NUM_PLANES; i++) {
            Vector3f pos = gameItem.getPosition();
            Vector4f plane = frustumPlanes[i];
            if( plane.x * pos.x + plane.y * pos.y + plane.z * pos.z + plane.w <= -boundingRadious ) {
                result = false;
                return result;
            }
        }
        return result;
    }
}
