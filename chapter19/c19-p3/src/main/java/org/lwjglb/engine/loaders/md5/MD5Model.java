package org.lwjglb.engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import org.lwjglb.engine.Utils;

public class MD5Model {

    private MD5JointInfo jointInfo;

    private MD5ModelHeader header;

    private List<MD5Mesh> meshes;

    public MD5Model() {
        meshes = new ArrayList<>();
    }
    
    public MD5JointInfo getJointInfo() {
        return jointInfo;
    }

    public void setJointInfo(MD5JointInfo jointInfo) {
        this.jointInfo = jointInfo;
    }

    public MD5ModelHeader getHeader() {
        return header;
    }

    public void setHeader(MD5ModelHeader header) {
        this.header = header;
    }

    public List<MD5Mesh> getMeshes() {
        return meshes;
    }

    public void setMeshes(List<MD5Mesh> meshes) {
        this.meshes = meshes;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MD5MeshModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getJointInfo()).append(System.lineSeparator());
        
        for (MD5Mesh mesh : meshes) {
            str.append(mesh).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static MD5Model parse(String meshModelFile) throws Exception {
        List<String> lines = Utils.readAllLines(meshModelFile);

        MD5Model result = new MD5Model();

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
        MD5ModelHeader header = MD5ModelHeader.parse(headerBlock);
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

    private static void parseBlock(MD5Model model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "joints":
                MD5JointInfo jointInfo = MD5JointInfo.parse(blockBody);
                model.setJointInfo(jointInfo);
                break;
            case "mesh":
                MD5Mesh md5Mesh = MD5Mesh.parse(blockBody);
                model.getMeshes().add(md5Mesh);
                break;
            default:
                break;
        }
    }

}
