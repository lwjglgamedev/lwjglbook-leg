package org.lwjglb.game;

import java.awt.Dimension;
import java.awt.Toolkit;
import org.lwjglb.engine.GameEngine;
import org.lwjglb.engine.IGameLogic;
 
public class Main {
 
    public static void main(String[] args) {
        try {
            IGameLogic gameLogic = new DummyGame();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            GameEngine gameEng = new GameEngine("GAME", (int)dim.getWidth() - 70, (int)dim.getHeight() -70, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}