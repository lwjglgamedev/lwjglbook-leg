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

    private static final int FLOAT_SIZE = 4;

    private static final int VECTOR4F_SIZE = 4 * FLOAT_SIZE;

    private static final int MATRIX_SIZE = 4 * VECTOR4F_SIZE;

    private int numInstances;

    private int modelViewVBO;

    private int modelLightViewVBO;

    private int textOffsetsVBO;

    private FloatBuffer modelViewBuffer;

    private FloatBuffer modelLightViewBuffer;

    private FloatBuffer textOffsetsBuffer;

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

        // Texture offsets
        textOffsetsVBO = glGenBuffers();
        vboIdList.add(textOffsetsVBO);
        this.textOffsetsBuffer = BufferUtils.createFloatBuffer(numInstances * FLOAT_SIZE * 2);
        glBindBuffer(GL_ARRAY_BUFFER, textOffsetsVBO);
        glVertexAttribPointer(start, 2, GL_FLOAT, false, FLOAT_SIZE * 2, 0);
        glVertexAttribDivisor(start, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    protected void initRender() {
        super.initRender();

        int start = 5;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);
        }
    }

    @Override
    protected void endRender() {
        int start = 5;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);
        }

        super.endRender();
    }

    public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        renderListInstanced(gameItems, false, transformation, viewMatrix, lightViewMatrix);
    }

    public void renderListInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        initRender();

        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            renderChunkInstanced(subList, billBoard, transformation, viewMatrix, lightViewMatrix);
        }

        endRender();
    }

    private void renderChunkInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        this.modelViewBuffer.clear();
        this.modelLightViewBuffer.clear();
        this.textOffsetsBuffer.clear();

        int i = 0;

        Texture text = getMaterial().getTexture();
        for (GameItem gameItem : gameItems) {
            Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
            if (viewMatrix != null) {
                if (billBoard) {
                    viewMatrix.transpose3x3(modelMatrix);
                }
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                modelViewMatrix.get(16 * i, modelViewBuffer);
            }
            if (lightViewMatrix != null) {
                Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                modelLightViewMatrix.get(16 * i, this.modelLightViewBuffer);
            }
            if (text != null) {
                int col = gameItem.getTextPos() % text.getNumCols();
                int row = gameItem.getTextPos() / text.getNumCols();
                float textXOffset = (float) col / text.getNumCols();
                float textYOffset = (float) row / text.getNumRows();
                int buffPos = FLOAT_SIZE * i;
                this.textOffsetsBuffer.put(buffPos, textXOffset);
                this.textOffsetsBuffer.put(buffPos + 1, textYOffset);
            }

            i++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelLightViewBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, textOffsetsVBO);
        glBufferData(GL_ARRAY_BUFFER, textOffsetsBuffer, GL_DYNAMIC_DRAW);

        glDrawElementsInstanced(
                GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
