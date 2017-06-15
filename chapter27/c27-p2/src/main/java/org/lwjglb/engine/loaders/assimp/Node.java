package org.lwjglb.engine.loaders.assimp;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

public class Node {

    private List<Node> children;

    private List<FrameData> frameDataList;

    private String name;

    private Node parent;

    public Node(String name, Node parent) {
        this.name = name;
        this.parent = parent;
        this.frameDataList = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public static Matrix4f getParentTransform(Node node, int framePos) {
        if (node == null) {
            return new Matrix4f();
        } else {
            Matrix4f parentTransform = new Matrix4f(getParentTransform(node.getParent(), framePos));
            List<FrameData> frameDataList = node.getFrameDataList();
            Matrix4f nodeTransform;
            if (framePos < frameDataList.size()) {
                nodeTransform = frameDataList.get(framePos).transformation;
            } else {
                nodeTransform = new Matrix4f();
            }
            return parentTransform.mul(nodeTransform);
        }
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public void addFramedata(FrameData frameData) {
        frameDataList.add(frameData);
    }

    public Node findByName(String targetName) {
        Node result = null;
        if (this.name.equals(targetName)) {
            result = this;
        } else {
            for (Node child : children) {
                result = child.findByName(targetName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public int getAnimationFrames() {
        int numFrames = this.frameDataList.size();
        for (Node child : children) {
            int childFrame = child.getAnimationFrames();
            numFrames = Math.max(numFrames, childFrame);
        }
        return numFrames;
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<FrameData> getFrameDataList() {
        return frameDataList;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    public static class FrameData {

        private Matrix4f transformation;

        public FrameData(Matrix4f transformation) {
            this.transformation = transformation;
        }

        public Matrix4f getTransformation() {
            return transformation;
        }
    }
}
