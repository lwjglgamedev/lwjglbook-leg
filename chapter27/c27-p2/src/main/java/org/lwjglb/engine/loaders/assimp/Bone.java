package org.lwjglb.engine.loaders.assimp;

import org.joml.Matrix4f;

public class Bone {

    private final int boneId;

    private final String boneName;

    private Matrix4f offsetMatrix;

    public Bone(int boneId, String boneName, Matrix4f offsetMatrix) {
        this.boneId = boneId;
        this.boneName = boneName;
        this.offsetMatrix = offsetMatrix;
    }

    public int getBoneId() {
        return boneId;
    }

    public String getBoneName() {
        return boneName;
    }

    public Matrix4f getOffsetMatrix() {
        return offsetMatrix;
    }

}
