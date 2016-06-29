package org.lwjglb.engine.graph;

import java.nio.FloatBuffer;
import java.util.List;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;
import org.lwjglb.engine.items.GameItem;

public class InstancedMesh extends Mesh {

    private static final int VECTOR4F_SIZE = 4 * 4;

    private static final int MATRIX_SIZE = 4 * VECTOR4F_SIZE;

    private int numInstances;

    private int modelViewVBO;

    private int modelLightViewVBO;

    private FloatBuffer modelViewBuffer;

    private FloatBuffer modelLightViewBuffer;

    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int instances) {
        this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0), instances);
    }

    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights, int numInstances) {
        super(positions, textCoords, normals, indices, jointIndices, weights);

        this.numInstances = numInstances;

        glBindVertexArray(vaoId);

        // Model View Matrix
        modelViewVBO = glGenBuffers();
        vboIdList.add(modelViewVBO);
        this.modelViewBuffer = BufferUtils.createFloatBuffer(numInstances * MATRIX_SIZE);
        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        int start = 5;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE, i * VECTOR4F_SIZE);
            glVertexAttribDivisor(start, 1);
            start++;
        }

        // Light view matrix
        modelLightViewVBO = glGenBuffers();
        vboIdList.add(modelLightViewVBO);
        this.modelLightViewBuffer = BufferUtils.createFloatBuffer(numInstances * MATRIX_SIZE);
        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE, i * VECTOR4F_SIZE);
            glVertexAttribDivisor(start, 1);
            start++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    protected void initRender() {
        super.initRender();

        int start = 5;
        int numElements = 4 * 2;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);
        }
    }

    @Override
    protected void endRender() {
        int start = 5;
        int numElements = 4 * 2;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);
        }

        super.endRender();
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
                modelViewMatrix.get(16 * i, modelViewBuffer);
            }
            Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
            modelLightViewMatrix.get(16 * i, this.modelLightViewBuffer);
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
