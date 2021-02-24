package org.lwjglb.engine.graph;

import java.util.ArrayList;

import org.lwjglb.engine.graph.lights.PointLight;
import org.lwjglb.engine.graph.lights.DirectionalLight;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.Scene;
import org.lwjglb.engine.SceneLight;
import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.Window;
import org.lwjglb.engine.graph.anim.AnimGameItem;
import org.lwjglb.engine.graph.anim.AnimatedFrame;
import org.lwjglb.engine.graph.particles.IParticleEmitter;
import org.lwjglb.engine.graph.shadow.ShadowCascade;
import org.lwjglb.engine.graph.shadow.ShadowRenderer;
import org.lwjglb.engine.loaders.assimp.StaticMeshesLoader;

public class Renderer {

    private final Transformation transformation;

    private final ShadowRenderer shadowRenderer;

    private ShaderProgram skyBoxShaderProgram;

    private ShaderProgram particlesShaderProgram;

    private ShaderProgram gBufferShaderProgram;

    private ShaderProgram dirLightShaderProgram;

    private ShaderProgram pointLightShaderProgram;

    private ShaderProgram fogShaderProgram;

    private final float specularPower;

    private final FrustumCullingFilter frustumFilter;

    private final List<GameItem> filteredItems;

    private GBuffer gBuffer;

    private SceneBuffer sceneBuffer;

    private Mesh bufferPassMesh;

    private Matrix4f bufferPassModelMatrix;

    private Vector4f tmpVec;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
        shadowRenderer = new ShadowRenderer();
        frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<>();
        tmpVec = new Vector4f();
    }

    public void init(Window window) throws Exception {
        shadowRenderer.init(window);
        gBuffer = new GBuffer(window);
        sceneBuffer = new SceneBuffer(window);
        setupSkyBoxShader();
        setupParticlesShader();
        setupGeometryShader();
        setupDirLightShader();
        setupPointLightShader();
        setupFogShader();

        bufferPassModelMatrix =  new Matrix4f();
        bufferPassMesh = StaticMeshesLoader.load("models/buffer_pass_mess.obj", "models")[0];
    }

    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        clear();

        if (window.getOptions().frustumCulling) {
            frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            frustumFilter.filter(scene.getGameMeshes());
            frustumFilter.filter(scene.getGameInstancedMeshes());
        }

        // Render depth map before view ports has been set up
        if (scene.isRenderShadows() && sceneChanged) {
            shadowRenderer.render(window, scene, camera, transformation, this);
        }

        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderGeometry(window, camera, scene);

        initLightRendering();
        renderPointLights(window, camera, scene);
        renderDirectionalLight(window, camera, scene);
        endLightRendering();

        renderFog(window, camera, scene);
        renderSkyBox(window, camera, scene);
        renderParticles(window, camera, scene);
    }

    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource("/shaders/particles_vertex.vs"));
        particlesShaderProgram.createFragmentShader(Utils.loadResource("/shaders/particles_fragment.fs"));
        particlesShaderProgram.link();

        particlesShaderProgram.createUniform("viewMatrix");
        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");

        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
    }

    private void setupSkyBoxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("/shaders/sb_vertex.vs"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("/shaders/sb_fragment.fs"));
        skyBoxShaderProgram.link();

        // Create uniforms for projection matrix
        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
        skyBoxShaderProgram.createUniform("colour");
        skyBoxShaderProgram.createUniform("hasTexture");

        skyBoxShaderProgram.createUniform("depthsText");
        skyBoxShaderProgram.createUniform("screenSize");
    }

    private void setupGeometryShader() throws Exception {
        gBufferShaderProgram = new ShaderProgram();
        gBufferShaderProgram.createVertexShader(Utils.loadResource("/shaders/gbuffer_vertex.vs"));
        gBufferShaderProgram.createFragmentShader(Utils.loadResource("/shaders/gbuffer_fragment.fs"));
        gBufferShaderProgram.link();

        gBufferShaderProgram.createUniform("projectionMatrix");
        gBufferShaderProgram.createUniform("viewMatrix");
        gBufferShaderProgram.createUniform("texture_sampler");
        gBufferShaderProgram.createUniform("normalMap");
        gBufferShaderProgram.createMaterialUniform("material");
        gBufferShaderProgram.createUniform("isInstanced");
        gBufferShaderProgram.createUniform("modelNonInstancedMatrix");
        gBufferShaderProgram.createUniform("selectedNonInstanced");
        gBufferShaderProgram.createUniform("jointsMatrix");
        gBufferShaderProgram.createUniform("numCols");
        gBufferShaderProgram.createUniform("numRows");

        // Create uniforms for shadow mapping
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShaderProgram.createUniform("shadowMap_" + i);
        }
        gBufferShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
        gBufferShaderProgram.createUniform("renderShadow");
    }

    private void setupDirLightShader() throws Exception {
        dirLightShaderProgram = new ShaderProgram();
        dirLightShaderProgram.createVertexShader(Utils.loadResource("/shaders/light_vertex.vs"));
        dirLightShaderProgram.createFragmentShader(Utils.loadResource("/shaders/dir_light_fragment.fs"));
        dirLightShaderProgram.link();

        dirLightShaderProgram.createUniform("modelMatrix");
        dirLightShaderProgram.createUniform("projectionMatrix");

        dirLightShaderProgram.createUniform("screenSize");
        dirLightShaderProgram.createUniform("positionsText");
        dirLightShaderProgram.createUniform("diffuseText");
        dirLightShaderProgram.createUniform("specularText");
        dirLightShaderProgram.createUniform("normalsText");
        dirLightShaderProgram.createUniform("shadowText");

        dirLightShaderProgram.createUniform("specularPower");
        dirLightShaderProgram.createUniform("ambientLight");
        dirLightShaderProgram.createDirectionalLightUniform("directionalLight");
    }

    private void setupPointLightShader() throws Exception {
        pointLightShaderProgram = new ShaderProgram();
        pointLightShaderProgram.createVertexShader(Utils.loadResource("/shaders/light_vertex.vs"));
        pointLightShaderProgram.createFragmentShader(Utils.loadResource("/shaders/point_light_fragment.fs"));
        pointLightShaderProgram.link();

        pointLightShaderProgram.createUniform("modelMatrix");
        pointLightShaderProgram.createUniform("projectionMatrix");

        pointLightShaderProgram.createUniform("screenSize");
        pointLightShaderProgram.createUniform("positionsText");
        pointLightShaderProgram.createUniform("diffuseText");
        pointLightShaderProgram.createUniform("specularText");
        pointLightShaderProgram.createUniform("normalsText");
        pointLightShaderProgram.createUniform("shadowText");

        pointLightShaderProgram.createUniform("specularPower");
        pointLightShaderProgram.createPointLightUniform("pointLight");
    }

    private void setupFogShader() throws Exception {
        fogShaderProgram = new ShaderProgram();
        fogShaderProgram.createVertexShader(Utils.loadResource("/shaders/light_vertex.vs"));
        fogShaderProgram.createFragmentShader(Utils.loadResource("/shaders/fog_fragment.fs"));
        fogShaderProgram.link();

        fogShaderProgram.createUniform("modelMatrix");
        fogShaderProgram.createUniform("viewMatrix");
        fogShaderProgram.createUniform("projectionMatrix");

        fogShaderProgram.createUniform("screenSize");
        fogShaderProgram.createUniform("positionsText");
        fogShaderProgram.createUniform("depthText");
        fogShaderProgram.createUniform("sceneText");

        fogShaderProgram.createFogUniform("fog");
        fogShaderProgram.createUniform("ambientLight");
        fogShaderProgram.createUniform("lightColour");
        fogShaderProgram.createUniform("lightIntensity");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private void renderGeometry(Window window, Camera camera, Scene scene) {
        // Render G-Buffer for writing
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());

        clear();

        glDisable(GL_BLEND);

        gBufferShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        gBufferShaderProgram.setUniform("viewMatrix", viewMatrix);
        gBufferShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        gBufferShaderProgram.setUniform("texture_sampler", 0);
        gBufferShaderProgram.setUniform("normalMap", 1);

        List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            gBufferShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            gBufferShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            gBufferShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
        shadowRenderer.bindTextures(GL_TEXTURE2);
        int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShaderProgram.setUniform("shadowMap_" + i, start + i);
        }
        gBufferShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene);

        renderInstancedMeshes(scene, viewMatrix);

        gBufferShaderProgram.unbind();

        glEnable(GL_BLEND);
    }

    private void initLightRendering() {
        // Bind scene buffer
        glBindFramebuffer(GL_FRAMEBUFFER, sceneBuffer.getBufferId());

        // Clear G-Buffer
        clear();

        // Disable depth testing to allow the drawing of multiple layers with the same depth
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        // Bind GBuffer for reading
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
    }

    private void endLightRendering() {
        // Bind screen for writing
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void renderPointLights(Window window, Camera camera, Scene scene) {
        pointLightShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        pointLightShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        pointLightShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Specular factor
        pointLightShaderProgram.setUniform("specularPower", specularPower);

        // Bind the G-Buffer textures
        int[] textureIds = this.gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        pointLightShaderProgram.setUniform("positionsText", 0);
        pointLightShaderProgram.setUniform("diffuseText", 1);
        pointLightShaderProgram.setUniform("specularText", 2);
        pointLightShaderProgram.setUniform("normalsText", 3);
        pointLightShaderProgram.setUniform("shadowText", 4);

        pointLightShaderProgram.setUniform("screenSize", (float) gBuffer.getWidth(), (float)gBuffer.getHeight());

        SceneLight sceneLight = scene.getSceneLight();
        PointLight[] pointLights = sceneLight.getPointLightList();
        int numPointLights = pointLights != null ? pointLights.length : 0;
        for(int i=0; i<numPointLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLights[i]);
            Vector3f lightPos = currPointLight.getPosition();
            tmpVec.set(lightPos, 1);
            tmpVec.mul(viewMatrix);
            lightPos.x = tmpVec.x;
            lightPos.y = tmpVec.y;
            lightPos.z = tmpVec.z;
            pointLightShaderProgram.setUniform("pointLight", currPointLight);

            bufferPassMesh.render();
        }

        pointLightShaderProgram.unbind();
    }

    private void renderDirectionalLight(Window window, Camera camera, Scene scene) {
        dirLightShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        dirLightShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        dirLightShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Specular factor
        dirLightShaderProgram.setUniform("specularPower", specularPower);

        // Bind the G-Buffer textures
        int[] textureIds = this.gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        dirLightShaderProgram.setUniform("positionsText", 0);
        dirLightShaderProgram.setUniform("diffuseText", 1);
        dirLightShaderProgram.setUniform("specularText", 2);
        dirLightShaderProgram.setUniform("normalsText", 3);
        dirLightShaderProgram.setUniform("shadowText", 4);

        dirLightShaderProgram.setUniform("screenSize", (float) gBuffer.getWidth(), (float)gBuffer.getHeight());

        // Ambient light
        SceneLight sceneLight = scene.getSceneLight();
        dirLightShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());

        // Directional light
        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        tmpVec.set(currDirLight.getDirection(), 0);
        tmpVec.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(tmpVec.x, tmpVec.y, tmpVec.z));
        dirLightShaderProgram.setUniform("directionalLight", currDirLight);

        bufferPassMesh.render();

        dirLightShaderProgram.unbind();
    }

    private void renderFog(Window window, Camera camera, Scene scene) {
        fogShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        fogShaderProgram.setUniform("modelMatrix", bufferPassModelMatrix);
        fogShaderProgram.setUniform("viewMatrix", viewMatrix);
        fogShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Bind the scene buffer texture and the the depth texture of the G-Buffer
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gBuffer.getPositionTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gBuffer.getDepthTexture());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, sceneBuffer.getTextureId());

        fogShaderProgram.setUniform("positionsText", 0);
        fogShaderProgram.setUniform("depthText", 1);
        fogShaderProgram.setUniform("sceneText", 2);

        fogShaderProgram.setUniform("screenSize", (float) window.getWidth(), (float)window.getHeight());

        fogShaderProgram.setUniform("fog", scene.getFog());
        SceneLight sceneLight = scene.getSceneLight();
        fogShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        DirectionalLight dirLight = sceneLight.getDirectionalLight();
        fogShaderProgram.setUniform("lightColour", dirLight.getColor());
        fogShaderProgram.setUniform("lightIntensity", dirLight.getIntensity());

        bufferPassMesh.render();

        fogShaderProgram.unbind();
    }

    private void renderParticles(Window window, Camera camera, Scene scene) {
        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        particlesShaderProgram.bind();

        Matrix4f viewMatrix = camera.getViewMatrix();
        particlesShaderProgram.setUniform("viewMatrix", viewMatrix);
        particlesShaderProgram.setUniform("texture_sampler", 0);
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        IParticleEmitter[] emitters = scene.getParticleEmitters();
        int numEmitters = emitters != null ? emitters.length : 0;

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (int i = 0; i < numEmitters; i++) {
            IParticleEmitter emitter = emitters[i];
            InstancedMesh mesh = (InstancedMesh) emitter.getBaseParticle().getMesh();

            Texture text = mesh.getMaterial().getTexture();
            particlesShaderProgram.setUniform("numCols", text.getNumCols());
            particlesShaderProgram.setUniform("numRows", text.getNumRows());

            mesh.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix);
        }

        glDisable(GL_BLEND);
        glDepthMask(true);

        particlesShaderProgram.unbind();
    }

    private void renderSkyBox(Window window, Camera camera, Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            skyBoxShaderProgram.bind();

            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            Matrix4f viewMatrix = camera.getViewMatrix();
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);

            Mesh mesh = skyBox.getMesh();
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
            skyBoxShaderProgram.setUniform("colour", mesh.getMaterial().getDiffuseColour());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, gBuffer.getDepthTexture());
            skyBoxShaderProgram.setUniform("screenSize", (float)window.getWidth(), (float)window.getHeight());
            skyBoxShaderProgram.setUniform("depthsText", 1);

            mesh.render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            skyBoxShaderProgram.unbind();
        }
    }

    private void renderNonInstancedMeshes(Scene scene) {
        gBufferShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            gBufferShaderProgram.setUniform("material", mesh.getMaterial());

            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                gBufferShaderProgram.setUniform("numCols", text.getNumCols());
                gBufferShaderProgram.setUniform("numRows", text.getNumRows());
            }

            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                gBufferShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                gBufferShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                if (gameItem instanceof AnimGameItem) {
                    AnimGameItem animGameItem = (AnimGameItem) gameItem;
                    AnimatedFrame frame = animGameItem.getCurrentAnimation().getCurrentFrame();
                    gBufferShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            }
            );
        }
    }

    private void renderInstancedMeshes(Scene scene, Matrix4f viewMatrix) {
        gBufferShaderProgram.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                gBufferShaderProgram.setUniform("numCols", text.getNumCols());
                gBufferShaderProgram.setUniform("numRows", text.getNumRows());
            }

            gBufferShaderProgram.setUniform("material", mesh.getMaterial());

            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
                if (gameItem.isInsideFrustum()) {
                    filteredItems.add(gameItem);
                }
            }

            mesh.renderListInstanced(filteredItems, transformation, viewMatrix);
        }
    }

    public void cleanup() {
        if (shadowRenderer != null) {
            shadowRenderer.cleanup();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanup();
        }
        if (particlesShaderProgram != null) {
            particlesShaderProgram.cleanup();
        }
        if (gBufferShaderProgram != null) {
            gBufferShaderProgram.cleanup();
        }
        if (dirLightShaderProgram != null) {
            dirLightShaderProgram.cleanup();
        }
        if (pointLightShaderProgram != null) {
            pointLightShaderProgram.cleanup();
        }
        if (gBuffer != null) {
            gBuffer.cleanUp();
        }
        if (bufferPassMesh != null) {
            bufferPassMesh.cleanUp();
        }
    }
}
