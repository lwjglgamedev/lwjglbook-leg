package org.lwjglb.engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import org.lwjglb.engine.Utils;

public class MD5AnimModel {

    private MD5AnimHeader header;

    private MD5Hierarchy hierarchy;

    private MD5BoundInfo boundInfo;

    private MD5BaseFrame baseFrame;

    private List<MD5Frame> frames;

    public MD5AnimModel() {
        frames = new ArrayList<>();
    }

    public MD5AnimHeader getHeader() {
        return header;
    }

    public void setHeader(MD5AnimHeader header) {
        this.header = header;
    }

    public MD5Hierarchy getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(MD5Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public MD5BoundInfo getBoundInfo() {
        return boundInfo;
    }

    public void setBoundInfo(MD5BoundInfo boundInfo) {
        this.boundInfo = boundInfo;
    }

    public MD5BaseFrame getBaseFrame() {
        return baseFrame;
    }

    public void setBaseFrame(MD5BaseFrame baseFrame) {
        this.baseFrame = baseFrame;
    }

    public List<MD5Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<MD5Frame> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MD5AnimModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getHierarchy()).append(System.lineSeparator());
        str.append(getBoundInfo()).append(System.lineSeparator());
        str.append(getBaseFrame()).append(System.lineSeparator());

        for (MD5Frame frame : frames) {
            str.append(frame).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static MD5AnimModel parse(String animFile) throws Exception {
        List<String> lines = Utils.readAllLines(animFile);

        MD5AnimModel result = new MD5AnimModel();

        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new Exception("Cannot parse empty file");
        }

        // Parse Header
        boolean headerEnd = false;
        int start = 0;
        for (int i = 0; i < numLines && !headerEnd; i++) {
            String line = lines.get(i);
            headerEnd = line.trim().endsWith("{");
            start = i;
        }
        if (!headerEnd) {
            throw new Exception("Cannot find header");
        }
        List<String> headerBlock = lines.subList(0, start);
        MD5AnimHeader header = MD5AnimHeader.parse(headerBlock);
        result.setHeader(header);

        // Parse the rest of block
        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < numLines; i++) {
            String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }

        return result;
    }

    private static void parseBlock(MD5AnimModel model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "hierarchy":
                MD5Hierarchy hierarchy = MD5Hierarchy.parse(blockBody);
                model.setHierarchy(hierarchy);
                break;
            case "bounds":
                MD5BoundInfo boundInfo = MD5BoundInfo.parse(blockBody);
                model.setBoundInfo(boundInfo);
                break;
            case "baseframe":
                MD5BaseFrame baseFrame = MD5BaseFrame.parse(blockBody);
                model.setBaseFrame(baseFrame);
                break;
            default:
                if (blockId.startsWith("frame ")) {
                    MD5Frame frame = MD5Frame.parse(blockId, blockBody);
                    model.getFrames().add(frame);
                }
                break;
        }
    }
}
