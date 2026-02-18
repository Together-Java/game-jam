package com.terminalvelocitycabbage.dukejump.rendernodes;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.engine.client.renderer.shader.ShaderProgramConfig;
import com.terminalvelocitycabbage.engine.client.scene.Scene;
import com.terminalvelocitycabbage.engine.client.window.WindowProperties;
import com.terminalvelocitycabbage.engine.ecs.Entity;
import com.terminalvelocitycabbage.engine.graph.RenderNode;
import com.terminalvelocitycabbage.engine.util.HeterogeneousMap;
import com.terminalvelocitycabbage.templates.ecs.components.FixedOrthoCameraComponent;
import com.terminalvelocitycabbage.templates.ecs.components.ModelComponent;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

import static org.lwjgl.opengl.GL11C.glClearColor;

public class DrawSceneRenderNode extends RenderNode {

    public DrawSceneRenderNode(ShaderProgramConfig shaderProgramConfig) {
        super(shaderProgramConfig);
    }

    @Override
    public void execute(Scene scene, WindowProperties properties, HeterogeneousMap renderConfig, long deltaTime) {

        var client = DukeGameClient.getInstance();
        var player = client.getManager().getFirstEntityWith(FixedOrthoCameraComponent.class);
        var camera = player.getComponent(FixedOrthoCameraComponent.class);
        var transformation = player.getComponent(TransformationComponent.class).getTransformation();
        var shaderProgram = getShaderProgram();

        if (properties.isResized()) {
            camera.updateProjectionMatrix(properties.getWidth(), properties.getHeight());
        }

        shaderProgram.bind();
        shaderProgram.getUniform("textureSampler").setUniform(0);
        shaderProgram.getUniform("projectionMatrix").setUniform(camera.getProjectionMatrix());
        shaderProgram.getUniform("viewMatrix").setUniform(camera.getViewMatrix(transformation));

        var entities = client.getManager().getEntitiesWith(ModelComponent.class, TransformationComponent.class);

        //Render entities
        for (Entity entity : entities) {
            var modelIdentifier = entity.getComponent(ModelComponent.class).getModel();
            var model = client.getModelRegistry().get(modelIdentifier);
            var mesh = scene.getMeshCache().getMesh(modelIdentifier);
            var texture = client.getTextureCache().getTexture(model.getTextureIdentifier());
            var transformationComponent = entity.getComponent(TransformationComponent.class);

            texture.bind();
            shaderProgram.getUniform("modelMatrix").setUniform(transformationComponent.getTransformationMatrix());
            if (mesh.getFormat().equals(shaderProgram.getConfig().getVertexFormat())) mesh.render();
        }

        shaderProgram.unbind();
    }
}
