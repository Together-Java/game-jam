package com.terminalvelocitycabbage.dukejump.scenes;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.engine.client.renderer.model.MeshCache;
import com.terminalvelocitycabbage.engine.client.scene.Scene;
import com.terminalvelocitycabbage.engine.graph.Routine;
import com.terminalvelocitycabbage.engine.registry.Identifier;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

import java.util.List;

public class DefaultScene extends Scene {

    public DefaultScene(Identifier renderGraph, List<Routine> routines) {
        super(renderGraph, routines);
    }

    @Override
    public void init() {
        var client = DukeGameClient.getInstance();
        var manager = client.getManager();

        client.getTextureCache().generateAtlas(DukeGameClient.TEXTURE_ATLAS);
        setMeshCache(new MeshCache(client.getModelRegistry(), client.getMeshRegistry(), client.getTextureCache()));

        for (int i = 0; i < DukeGameClient.BACKGROUND_PARTS; i++) {
            manager.createEntityFromTemplate(DukeGameClient.BACKGROUND_ENTITY).getComponent(TransformationComponent.class).translate(DukeGameClient.SCALE * 8 * i, 0, 0);
        }
        manager.createEntityFromTemplate(DukeGameClient.DUKE_ENTITY);
        manager.createEntityFromTemplate(DukeGameClient.PLAYER_CAMERA_ENTITY);
        for (int i = 0; i < DukeGameClient.GROUND_PARTS; i++) {
            manager.createEntityFromTemplate(DukeGameClient.GROUND_ENTITY).getComponent(TransformationComponent.class).translate(DukeGameClient.SCALE * 4 * i, 0, 0);
        }
    }

    @Override
    public void cleanup() {
        var client = DukeGameClient.getInstance();
        client.getTextureCache().cleanupAtlas(DukeGameClient.TEXTURE_ATLAS);
        getMeshCache().cleanup();
    }
}
