package org.lwjglb.engine;

import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.items.GameItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.particles.IParticleEmitter;
import org.lwjglb.engine.graph.weather.Fog;

public class Scene {

    private final Map<Mesh, List<GameItem>> meshMap;

    private SkyBox skyBox;

    private SceneLight sceneLight;

    private Fog fog;

    private IParticleEmitter[] particleEmitters;

    public Scene() {
        meshMap = new HashMap();
        fog = Fog.NOFOG;
    }

    public Map<Mesh, List<GameItem>> getGameMeshes() {
        return meshMap;
    }

    public void setGameItems(GameItem[] gameItems) {
        // Create a map of meshes to speed up rendering
        int numGameItems = gameItems != null ? gameItems.length : 0;
        for (int i = 0; i < numGameItems; i++) {
            GameItem gameItem = gameItems[i];
            Mesh[] meshes = gameItem.getMeshes();
            for (Mesh mesh : meshes) {
                List<GameItem> list = meshMap.get(mesh);
                if (list == null) {
                    list = new ArrayList<>();
                    meshMap.put(mesh, list);
                }
                list.add(gameItem);
            }
        }
    }

    public void cleanup() {
        for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanUp();
        }
        for (IParticleEmitter particleEmitter : particleEmitters) {
            particleEmitter.cleanup();            
        }
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }

    /**
     * @return the fog
     */
    public Fog getFog() {
        return fog;
    }

    /**
     * @param fog the fog to set
     */
    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public IParticleEmitter[] getParticleEmitters() {
        return particleEmitters;
    }

    public void setParticleEmitters(IParticleEmitter[] particleEmitters) {
        this.particleEmitters = particleEmitters;
    }

}
