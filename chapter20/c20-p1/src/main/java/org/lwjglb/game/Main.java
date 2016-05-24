package org.lwjglb.game;

import java.awt.Dimension;
import java.awt.Toolkit;
import org.lwjglb.engine.GameEngine;
import org.lwjglb.engine.IGameLogic;
import org.lwjglb.engine.Window;
 
public class Main {
 
    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            Window.WindowOptions opts = new Window.WindowOptions();
            GameEngine gameEng = new GameEngine("GAME", (int)dim.getWidth() - 70, (int)dim.getHeight() -70, vSync, opts, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}