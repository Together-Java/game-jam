package com.terminalvelocitycabbage.dukejump.rendernodes;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.ConfettiComponent;
import com.terminalvelocitycabbage.engine.client.renderer.elements.VertexFormat;
import com.terminalvelocitycabbage.engine.client.renderer.model.DataMesh;
import com.terminalvelocitycabbage.engine.client.renderer.model.Mesh;
import com.terminalvelocitycabbage.engine.client.renderer.model.Vertex;
import com.terminalvelocitycabbage.engine.client.renderer.shader.ShaderProgramConfig;
import com.terminalvelocitycabbage.engine.client.scene.Scene;
import com.terminalvelocitycabbage.engine.client.window.WindowProperties;
import com.terminalvelocitycabbage.engine.graph.RenderNode;
import com.terminalvelocitycabbage.engine.util.Color;
import com.terminalvelocitycabbage.engine.util.HeterogeneousMap;
import com.terminalvelocitycabbage.engine.util.Transformation;
import com.terminalvelocitycabbage.engine.util.touples.Triplet;
import com.terminalvelocitycabbage.templates.ecs.components.FixedOrthoCameraComponent;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawConfettiRenderNode extends RenderNode {

    int milestone = 0;
    //Transformation - initialVelocity (vertical, horizontal, rotational), color
    List<Triplet<Transformation, Vector3f, Color>> confettiData;
    Random random;
    Mesh mesh;

    public DrawConfettiRenderNode(ShaderProgramConfig shaderProgramConfig) {
        super(shaderProgramConfig);
        confettiData = new ArrayList<>(DukeGameClient.CONFETTI_COUNT);
        random = new Random();
        mesh = new Mesh(DukeGameClient.CONFETTI_FORMAT, new ConfettiMesh());
    }

    @Override
    public void execute(Scene scene, WindowProperties properties, HeterogeneousMap renderConfig, long deltaTime) {

        var client = DukeGameClient.getInstance();
        var player = client.getManager().getFirstEntityWith(FixedOrthoCameraComponent.class);
        var camera = player.getComponent(FixedOrthoCameraComponent.class);
        var transformation = player.getComponent(TransformationComponent.class).getTransformation();
        var shaderProgram = getShaderProgram();

        shaderProgram.bind();
        shaderProgram.getUniform("projectionMatrix").setUniform(camera.getProjectionMatrix());
        shaderProgram.getUniform("viewMatrix").setUniform(camera.getViewMatrix(transformation));

        client.getManager().getEntitiesWith(ConfettiComponent.class).forEach(confetti -> {
            shaderProgram.getUniform("modelMatrix").setUniform(confetti.getComponent(TransformationComponent.class).getTransformationMatrix());
            shaderProgram.getUniform("color").setUniform(confetti.getComponent(ConfettiComponent.class).getColor());
            mesh.render();
        });

        shaderProgram.unbind();
    }

    public static class ConfettiMesh extends DataMesh {

        @Override
        public Vertex[] getVertices(VertexFormat format) {
            return new Vertex[] {
                    new Vertex(format).setXYZPosition(-0.5f, 0.5f, 0f),
                    new Vertex(format).setXYZPosition(-0.5f, -0.5f, 0f),
                    new Vertex(format).setXYZPosition(0.5f, -0.5f, 0f),
                    new Vertex(format).setXYZPosition(0.5f, 0.5f, 0f)
            };
        }

        @Override
        public int[] getIndices() {
            return new int[] {0, 1, 3, 3, 1, 2};
        }
    }
}
