package org.lwjglb.engine;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        return new String(Files.readAllBytes(
                Paths.get(Utils.class.getResource(fileName).toURI())));
    }

}