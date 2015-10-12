package org.lwjglb.engine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        return new String(Files.readAllBytes(
                Paths.get(Utils.class.getResource(fileName).toURI())));
    }
    
    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for(int i=0; i<size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

}