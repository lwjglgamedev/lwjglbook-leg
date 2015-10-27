package org.lwjglb.engine;

public class Scene {

    private GameItem[] gameItems;
    
    private SkyBox skyBox;
    
    private SceneLight sceneLight;

    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void setGameItems(GameItem[] gameItems) {
        this.gameItems = gameItems;
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
    
}
