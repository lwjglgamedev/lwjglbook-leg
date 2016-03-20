package org.lwjglb.engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.engine.items.GameItem;

public class MD5Loader {

    private static final String NORMAL_FILE_SUFFIX = "_normal";
    
    public static GameItem process(MD5Model md5Model, Vector3f defaultColour) throws Exception {
        List<MD5Mesh> md5MeshList = md5Model.getMeshes();
        int numMeshes = md5MeshList.size();
        Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            Mesh mesh = generateMesh(md5Model, md5MeshList.get(i), defaultColour);
            meshes[i] = mesh;
        }
        GameItem gameItem = new GameItem(meshes);            
        
        return gameItem;
    }

    private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh, Vector3f defaultColour) throws Exception {
        List<VertexInfo> vertexInfoList = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<MD5Mesh.MD5Vertex> vertices = md5Mesh.getVertices();
        List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();

        for (MD5Mesh.MD5Vertex vertex : vertices) {
            Vector3f vertexPos = new Vector3f();
            Vector2f vertexTextCoords = vertex.getTextCoords();
            textCoords.add(vertexTextCoords.x);
            textCoords.add(vertexTextCoords.y);

            int startWeight = vertex.getStartWeight();
            int numWeights = vertex.getWeightCount();

            for (int i = startWeight; i < startWeight + numWeights; i++) {
                MD5Mesh.MD5Weight weight = weights.get(i);
                MD5JointInfo.MD5JointData joint = joints.get(weight.getJointIndex());
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertexPos.add(acumPos);
            }

            vertexInfoList.add(new VertexInfo(vertexPos));
        }

        for (MD5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());

            // Normals
            VertexInfo v0 = vertexInfoList.get(tri.getVertex0());
            VertexInfo v1 = vertexInfoList.get(tri.getVertex1());
            VertexInfo v2 = vertexInfoList.get(tri.getVertex2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;

            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));
            normal.normalize();

            v0.normal.add(normal).normalize();
            v1.normal.add(normal).normalize();
            v2.normal.add(normal).normalize();
        }

        float[] positionsArr = VertexInfo.toPositionsArr(vertexInfoList);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = VertexInfo.toNormalArr(vertexInfoList);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        Mesh mesh = new Mesh(positionsArr, textCoordsArr, normalsArr, indicesArr);

        handleTexture(mesh, md5Mesh, defaultColour);

        return mesh;
    }

    private static void handleTexture(Mesh mesh, MD5Mesh md5Mesh, Vector3f defaultColour) throws Exception {
        String texturePath = md5Mesh.getTexture();
        if (texturePath != null && texturePath.length() > 0) {
            Texture texture = new Texture(texturePath);
            Material material = new Material(texture);

            // Handle normal Maps;
            int pos = texturePath.lastIndexOf(".");
            if (pos > 0) {
                String basePath = texturePath.substring(0, pos);
                String extension = texturePath.substring(pos, texturePath.length());
                String normalMapFileName = basePath + NORMAL_FILE_SUFFIX + extension;
                if (Utils.existsResourceFile(normalMapFileName)) {
                    Texture normalMap = new Texture(normalMapFileName);
                    material.setNormalMap(normalMap);
                }
            }
            mesh.setMaterial(material);
        } else {
            mesh.setMaterial(new Material(defaultColour, 1));
        }
    }

    private static class VertexInfo {

        public Vector3f position;

        public Vector3f normal;

        public VertexInfo(Vector3f position) {
            this.position = position;
            normal = new Vector3f(0, 0, 0);
        }

        public VertexInfo() {
            position = new Vector3f();
            normal = new Vector3f();
        }

        public static float[] toPositionsArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.position.x;
                result[i + 1] = v.position.y;
                result[i + 2] = v.position.z;
                i += 3;
            }
            return result;
        }

        public static float[] toNormalArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.normal.x;
                result[i + 1] = v.normal.y;
                result[i + 2] = v.normal.z;
                i += 3;
            }
            return result;
        }
    }
}
