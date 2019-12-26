package org.lwjglb.engine.graph;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import org.lwjglb.engine.items.GameItem;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedMesh extends Mesh {

    private static final int VECTOR4F_SIZE_BYTES = 4 * 4;

    private static final int MATRIX_SIZE_BYTES = 4 * InstancedMesh.VECTOR4F_SIZE_BYTES;

    private static final int MATRIX_SIZE_FLOATS = 4 * 4;

    private final int numInstances;

    private final int modelViewVBO;

    private final int modelLightViewVBO;

    private FloatBuffer modelViewBuffer;

    private FloatBuffer modelLightViewBuffer;

    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int numInstances) {
        super(positions, textCoords, normals, indices, Mesh.createEmptyIntArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0), Mesh.createEmptyFloatArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0));

        this.numInstances = numInstances;

        glBindVertexArray(vaoId);

        // Model View Matrix
        modelViewVBO = glGenBuffers();
        vboIdList.add(modelViewVBO);
        this.modelViewBuffer = MemoryUtil.memAllocFloat(numInstances * InstancedMesh.MATRIX_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        int start = 5;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, InstancedMesh.MATRIX_SIZE_BYTES, i * InstancedMesh.VECTOR4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        // Light view matrix
        modelLightViewVBO = glGenBuffers();
        vboIdList.add(modelLightViewVBO);
        this.modelLightViewBuffer = MemoryUtil.memAllocFloat(numInstances * InstancedMesh.MATRIX_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, InstancedMesh.MATRIX_SIZE_BYTES, i * InstancedMesh.VECTOR4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (this.modelViewBuffer != null) {
            MemoryUtil.memFree(this.modelViewBuffer);
            this.modelViewBuffer = null;
        }
        if (this.modelLightViewBuffer != null) {
            MemoryUtil.memFree(this.modelLightViewBuffer);
            this.modelLightViewBuffer = null;
        }
    }

    public void renderListInstanced(List<GameItem> gameItems, boolean depthMap, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        initRender();

        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            renderChunkInstanced(subList, depthMap, transformation, viewMatrix, lightViewMatrix);
        }

        endRender();
    }

    private void renderChunkInstanced(List<GameItem> gameItems, boolean depthMap, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        this.modelViewBuffer.clear();
        this.modelLightViewBuffer.clear();

        int i = 0;

        for (GameItem gameItem : gameItems) {
            Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
            if (!depthMap) {
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                modelViewMatrix.get(InstancedMesh.MATRIX_SIZE_FLOATS * i, modelViewBuffer);
            }
            Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
            modelLightViewMatrix.get(InstancedMesh.MATRIX_SIZE_FLOATS * i, this.modelLightViewBuffer);
            i++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelLightViewBuffer, GL_DYNAMIC_DRAW);

        glDrawElementsInstanced(
                GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
