package org.lwjglb.engine.loaders.assimp;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

public class Node {

    private final List<Node> children;

    private final List<Matrix4f> transformations;

    private final String name;

    private final Node parent;

    public Node(String name, Node parent) {
        this.name = name;
        this.parent = parent;
        this.transformations = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public static Matrix4f getParentTransforms(Node node, int framePos) {
        if (node == null) {
            return new Matrix4f();
        } else {
            Matrix4f parentTransform = new Matrix4f(getParentTransforms(node.getParent(), framePos));
            List<Matrix4f> transformations = node.getTransformations();
            Matrix4f nodeTransform;
            int transfSize = transformations.size();
            if (framePos < transfSize) {
                nodeTransform = transformations.get(framePos);
            } else if ( transfSize > 0 ) {
                nodeTransform = transformations.get(transfSize - 1);
            } else {
                nodeTransform = new Matrix4f();
            }
            return parentTransform.mul(nodeTransform);
        }
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public void addTransformation(Matrix4f transformation) {
        transformations.add(transformation);
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
        int numFrames = this.transformations.size();
        for (Node child : children) {
            int childFrame = child.getAnimationFrames();
            numFrames = Math.max(numFrames, childFrame);
        }
        return numFrames;
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<Matrix4f> getTransformations() {
        return transformations;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }
}
