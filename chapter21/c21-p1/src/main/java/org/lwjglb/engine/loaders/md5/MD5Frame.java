package org.lwjglb.engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import org.lwjglb.engine.Utils;

public class MD5Frame {

    private int id;

    private float[] frameData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getFrameData() {
        return frameData;
    }

    public void setFrameData(float[] frameData) {
        this.frameData = frameData;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("frame " + id + " [data: " + System.lineSeparator());
        for (float frameData : frameData) {
            str.append(frameData).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static MD5Frame parse(String blockId, List<String> blockBody) throws Exception {
        MD5Frame result = new MD5Frame();
        String[] tokens = blockId.trim().split("\\s+");
        if (tokens != null && tokens.length >= 2) {
            result.setId(Integer.parseInt(tokens[1]));
        } else {
            throw new Exception("Wrong frame definition: " + blockId);
        }

        List<Float> data = new ArrayList<>();
        for (String line : blockBody) {
            List<Float> lineData = parseLine(line);
            if (lineData != null) {
                data.addAll(lineData);
            }
        }
        float[] dataArr = Utils.listToArray(data);
        result.setFrameData(dataArr);

        return result;
    }

    private static List<Float> parseLine(String line) {
        String[] tokens = line.trim().split("\\s+");
        List<Float> data = new ArrayList<>();
        for (String token : tokens) {
            data.add(Float.parseFloat(token));
        }
        return data;
    }
}