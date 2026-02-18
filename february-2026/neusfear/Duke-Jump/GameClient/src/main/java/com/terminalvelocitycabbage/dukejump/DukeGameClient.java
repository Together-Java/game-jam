package com.terminalvelocitycabbage.dukejump;

import com.terminalvelocitycabbage.dukejump.components.*;
import com.terminalvelocitycabbage.dukejump.inputcontrollers.PauseGameController;
import com.terminalvelocitycabbage.dukejump.inputcontrollers.JumpController;
import com.terminalvelocitycabbage.dukejump.inputcontrollers.StompController;
import com.terminalvelocitycabbage.dukejump.rendernodes.DrawConfettiRenderNode;
import com.terminalvelocitycabbage.dukejump.rendernodes.DrawUIRenderNode;
import com.terminalvelocitycabbage.dukejump.rendernodes.DrawSceneRenderNode;
import com.terminalvelocitycabbage.dukejump.scenes.DefaultScene;
import com.terminalvelocitycabbage.dukejump.systems.*;
import com.terminalvelocitycabbage.engine.client.ClientBase;
import com.terminalvelocitycabbage.engine.client.input.control.*;
import com.terminalvelocitycabbage.engine.client.input.controller.ControlGroup;
import com.terminalvelocitycabbage.engine.client.input.types.KeyboardInput;
import com.terminalvelocitycabbage.engine.client.input.types.MouseInput;
import com.terminalvelocitycabbage.engine.client.renderer.Font;
import com.terminalvelocitycabbage.engine.client.renderer.RenderGraph;
import com.terminalvelocitycabbage.engine.client.renderer.elements.VertexAttribute;
import com.terminalvelocitycabbage.engine.client.renderer.elements.VertexFormat;
import com.terminalvelocitycabbage.engine.client.renderer.model.Mesh;
import com.terminalvelocitycabbage.engine.client.renderer.shader.Shader;
import com.terminalvelocitycabbage.engine.client.renderer.shader.ShaderProgramConfig;
import com.terminalvelocitycabbage.engine.client.renderer.shader.Uniform;
import com.terminalvelocitycabbage.engine.client.window.WindowProperties;
import com.terminalvelocitycabbage.engine.ecs.Entity;
import com.terminalvelocitycabbage.engine.filesystem.resources.ResourceCategory;
import com.terminalvelocitycabbage.engine.filesystem.resources.ResourceSource;
import com.terminalvelocitycabbage.engine.filesystem.sources.MainSource;
import com.terminalvelocitycabbage.engine.graph.Routine;
import com.terminalvelocitycabbage.engine.registry.Identifier;
import com.terminalvelocitycabbage.engine.util.Color;
import com.terminalvelocitycabbage.templates.ecs.components.*;
import com.terminalvelocitycabbage.templates.events.*;
import com.terminalvelocitycabbage.templates.inputcontrollers.UIClickController;
import com.terminalvelocitycabbage.templates.inputcontrollers.UIScrollController;
import com.terminalvelocitycabbage.templates.meshes.SquareDataMesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DukeGameClient extends ClientBase {

    //This client's identifier (namespace)
    public static final String ID = "dukejump";

    //Resource stuff
    public static Identifier CLIENT_RESOURCE_SOURCE;

    //Shader stuff
    public static Identifier DEFAULT_VERTEX_SHADER;
    public static Identifier DEFAULT_FRAGMENT_SHADER;
    public static ShaderProgramConfig DEFAULT_SHADER_PROGRAM_CONFIG;
    public static Identifier CONFETTI_VERTEX_SHADER;
    public static Identifier CONFETTI_FRAGMENT_SHADER;
    public static ShaderProgramConfig CONFETTI_SHADER_PROGRAM_CONFIG;

    //Atlases
    public static Identifier TEXTURE_ATLAS;

    //Textures
    public static Identifier DUKE_IDLE_0_TEXTURE;
    public static Identifier DUKE_IDLE_1_TEXTURE;
    public static Identifier DUKE_WALK_0_TEXTURE;
    public static Identifier DUKE_WALK_1_TEXTURE;
    public static Identifier DUKE_WALK_2_TEXTURE;
    public static Identifier DUKE_WALK_3_TEXTURE;
    public static Identifier DUKE_JUMP_TEXTURE;
    public static Identifier DUKE_DEAD_TEXTURE;
    public static Identifier GROUND_TEXTURE;
    public static Identifier BUG_0_TEXTURE;
    public static Identifier BUG_1_TEXTURE;
    public static Identifier FLY_0_TEXTURE;
    public static Identifier FLY_1_TEXTURE;
    public static Identifier BACKGROUND_TEXTURE;

    //Meshes and Models
    public static Identifier SPRITE_MESH;
    public static Identifier DUKE_IDLE_0_MODEL;
    public static Identifier DUKE_IDLE_1_MODEL;
    public static Identifier DUKE_WALK_0_MODEL;
    public static Identifier DUKE_WALK_1_MODEL;
    public static Identifier DUKE_WALK_2_MODEL;
    public static Identifier DUKE_WALK_3_MODEL;
    public static Identifier DUKE_JUMP_MODEL;
    public static Identifier DUKE_DEAD_MODEL;
    public static Identifier GROUND_MODEL;
    public static Identifier BUG_0_MODEL;
    public static Identifier BUG_1_MODEL;
    public static Identifier FLY_0_MODEL;
    public static Identifier FLY_1_MODEL;
    public static Identifier BACKGROUND_MODEL;

    //Sounds
    public static Identifier SOUND_JUMP_RESOURCE;
    public static Identifier SOUND_JUMP;
    public static Identifier SOUND_DEATH_RESOURCE;
    public static Identifier SOUND_DEATH;
    public static Identifier SOUND_SQUASH_RESOURCE;
    public static Identifier SOUND_SQUASH;
    public static Identifier SOUND_MILESTONE_RESOURCE;
    public static Identifier SOUND_MILESTONE;

    //Fonts
    public static Identifier PIXEL_FONT_RESOURCE;
    public static Identifier PIXEL_FONT;

    //Renderer configs
    public static final VertexFormat MESH_FORMAT = VertexFormat.builder()
            .addElement(VertexAttribute.XYZ_POSITION)
            .addElement(VertexAttribute.UV)
            .build();
    public static final VertexFormat CONFETTI_FORMAT = VertexFormat.builder()
            .addElement(VertexAttribute.XYZ_POSITION)
            .build();
    public static Routine DEFAULT_ROUTINE;
    public static Identifier RENDER_GRAPH;
    private static Identifier DEFAULT_SCENE;

    //Entity stuff
    public static Identifier DUKE_ENTITY;
    public static Identifier GROUND_ENTITY;
    public static Identifier BUG_ENTITY;
    public static Identifier FLY_ENTITY;
    public static Identifier BACKGROUND_ENTITY;
    public static Identifier PLAYER_CAMERA_ENTITY;
    public static Identifier CONFETTI_ENTITY;

    //STATES
    public static Identifier CURRENT_SCORE;
    public static Identifier GAME_STATE;
    public static Identifier PASSED_ENEMIES_THIS_ROUND;

    //Game Configuration
    public static final boolean USE_ADAPTIVE_JUMP = false;
    public static final float INITIAL_SPEED = -0.5f;
    public static float MOVEMENT_SPEED = INITIAL_SPEED;
    public static final float GRAVITY = 0.005f;
    public static final float JUMP_FORCE = USE_ADAPTIVE_JUMP ? 0.2f : 1.25f;
    public static final float JUMP_HOLD_FRICTION = .88f;
    public static final float STOMP_FORCE = 1.25f;
    public static final float SQUASH_UPFORCE = 0.75f;
    public static final float SCALE = 60f;
    public static final int GROUND_PARTS = 8;
    public static final int GROUND_Y = -100;
    public static final int PLAYER_POSITION_X = -300;
    public static final float SPEEDUP_MULTIPLIER = .000005f;
    //Enemies
    public static final int ENEMY = 500;
    public static final float BUG_SPEED_MULTIPLIER = 1.2f;
    public static final int BUG_FREQUENCY = 1000; //duration in ms between bug spawns
    public static final int BUG_FREQUENCY_VARIANCE = 600;
    //Flying Enemies
    public static final float FLY_CHANCE = .25f;
    public static final float FLY_SPEED_MULTIPLIER = 1.5f;
    public static final float FLY_HEIGHT_VARIANCE = 10f;
    public static final float FLY_HEIGHT_SPEED = 100f;
    public static final int FLY_WAIT = 10;
    public static final int FLY_BASE_HEIGHT = 55;
    //Background
    public static final float BACKGROUND_SPEED_MULTIPLIER = 0.2f;
    public static final int BACKGROUND_PARTS = 5;
    public static final float INTERSECTION_RADIUS = SCALE / 2f;
    public static final float SQUASH_OFFSET = INTERSECTION_RADIUS * 0.5f;
    //Confetti
    public static final int CONFETTI_COUNT = 100;
    public static final Vector3f CONFETTI_SPAWN_LOCATION = new Vector3f(0, 220, 5);
    public static final float CONFETTI_MAX_VERTICAL_VELOCITY = 1.0f;
    public static final float CONFETTI_MAX_HORIZONTAL_VELOCITY = 1.0f;
    public static final float CONFETTI_MAX_ROTATIONAL_VELOCITY = 1.0f;
    public static final float CONFETTI_SCALE = 8.0f;
    public static final Color[] confettiColors = {
            new Color(255, 0, 0, 255),
            new Color(0, 255, 0, 255),
            new Color(0, 0, 255, 255),
            new Color(255, 255, 0, 255),
            new Color(128, 0, 128, 255)
    };
    public static final float CONFETTI_SPAWN_DURATION = 500; //How long in ms it takes to spawn all the confetti

    //High Scores
    public static final List<Score> HIGH_SCORES = new ArrayList<>();

    public DukeGameClient(String namespace, int ticksPerSecond) {
        super(namespace, ticksPerSecond);
        Collections.sort(HIGH_SCORES);
        //Listen to events
        getEventDispatcher().listenToEvent(ResourceCategoryRegistrationEvent.EVENT, e -> {
            //Register engine defaults
            ResourceCategory.registerEngineDefaults(((ResourceCategoryRegistrationEvent) e).getRegistry(), ID);
        });
        getEventDispatcher().listenToEvent(ResourceSourceRegistrationEvent.EVENT, e -> {
            //Register and init filesystem things
            //Create resource sources for this client
            ResourceSource mainSource = new MainSource(getInstance());
            //Define roots for these resources based on default resoruce categories
            mainSource.registerDefaultSources(ID);
            //register this source
            CLIENT_RESOURCE_SOURCE = ((ResourceSourceRegistrationEvent) e).registerResourceSource(ID, "main", mainSource);
        });
        getEventDispatcher().listenToEvent(ResourceRegistrationEvent.getEventNameFromCategory(ResourceCategory.SHADER), e -> {
            ResourceRegistrationEvent event = (ResourceRegistrationEvent) e;
            //Register shader resources
            DEFAULT_VERTEX_SHADER = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SHADER, "default.vert").getIdentifier();
            DEFAULT_FRAGMENT_SHADER = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SHADER, "default.frag").getIdentifier();
            //Configure the shader program
            DEFAULT_SHADER_PROGRAM_CONFIG = ShaderProgramConfig.builder()
                    .vertexFormat(MESH_FORMAT)
                    .addShader(Shader.Type.VERTEX, DEFAULT_VERTEX_SHADER)
                    .addShader(Shader.Type.FRAGMENT, DEFAULT_FRAGMENT_SHADER)
                    .addUniform(new Uniform("textureSampler"))
                    .addUniform(new Uniform("projectionMatrix"))
                    .addUniform(new Uniform("viewMatrix"))
                    .addUniform(new Uniform("modelMatrix"))
                    .build();
            CONFETTI_VERTEX_SHADER = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SHADER, "confetti.vert").getIdentifier();
            CONFETTI_FRAGMENT_SHADER = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SHADER, "confetti.frag").getIdentifier();
            CONFETTI_SHADER_PROGRAM_CONFIG = ShaderProgramConfig.builder()
                    .vertexFormat(DukeGameClient.CONFETTI_FORMAT)
                    .addShader(Shader.Type.VERTEX, CONFETTI_VERTEX_SHADER)
                    .addShader(Shader.Type.FRAGMENT, CONFETTI_FRAGMENT_SHADER)
                    .addUniform(new Uniform("textureSampler"))
                    .addUniform(new Uniform("projectionMatrix"))
                    .addUniform(new Uniform("viewMatrix"))
                    .addUniform(new Uniform("modelMatrix"))
                    .addUniform(new Uniform("color"))
                    .build();
        });
        getEventDispatcher().listenToEvent(ResourceRegistrationEvent.getEventNameFromCategory(ResourceCategory.TEXTURE), e -> {
            ResourceRegistrationEvent event = (ResourceRegistrationEvent) e;
            //Register texture resources
            DUKE_IDLE_0_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_idle_0.png").getIdentifier();
            DUKE_IDLE_1_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_idle_1.png").getIdentifier();
            DUKE_WALK_0_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_walk_0.png").getIdentifier();
            DUKE_WALK_1_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_walk_1.png").getIdentifier();
            DUKE_WALK_2_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_walk_2.png").getIdentifier();
            DUKE_WALK_3_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_walk_3.png").getIdentifier();
            DUKE_JUMP_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_jump_0.png").getIdentifier();
            DUKE_DEAD_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "duke_dead.png").getIdentifier();
            GROUND_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "ground.png").getIdentifier();
            BUG_0_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "bug_0.png").getIdentifier();
            BUG_1_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "bug_1.png").getIdentifier();
            FLY_0_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "fly_0.png").getIdentifier();
            FLY_1_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "fly_1.png").getIdentifier();
            BACKGROUND_TEXTURE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.TEXTURE, "background.png").getIdentifier();
        });
        getEventDispatcher().listenToEvent(ResourceRegistrationEvent.getEventNameFromCategory(ResourceCategory.SOUND), e -> {
            ResourceRegistrationEvent event = (ResourceRegistrationEvent) e;
            SOUND_JUMP_RESOURCE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SOUND, "jump.ogg").getIdentifier();
            SOUND_DEATH_RESOURCE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SOUND, "death.ogg").getIdentifier();
            SOUND_SQUASH_RESOURCE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SOUND, "squash.ogg").getIdentifier();
            SOUND_MILESTONE_RESOURCE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.SOUND, "milestone.ogg").getIdentifier();
        });
        getEventDispatcher().listenToEvent(SoundRegistrationEvent.EVENT, e -> {
            SoundRegistrationEvent event = (SoundRegistrationEvent) e;
            SOUND_JUMP = event.registerSound(SOUND_JUMP_RESOURCE);
            SOUND_DEATH = event.registerSound(SOUND_DEATH_RESOURCE);
            SOUND_SQUASH = event.registerSound(SOUND_SQUASH_RESOURCE);
            SOUND_MILESTONE = event.registerSound(SOUND_MILESTONE_RESOURCE);
        });
        getEventDispatcher().listenToEvent(ResourceRegistrationEvent.getEventNameFromCategory(ResourceCategory.FONT), e -> {
            ResourceRegistrationEvent event = (ResourceRegistrationEvent) e;
            PIXEL_FONT_RESOURCE = event.registerResource(CLIENT_RESOURCE_SOURCE, ResourceCategory.FONT, "pixel_font.ttf").getIdentifier();
        });
        getEventDispatcher().listenToEvent(FontRegistrationEvent.EVENT, e -> {
            FontRegistrationEvent event = (FontRegistrationEvent) e;
            PIXEL_FONT = event.register(new Font(PIXEL_FONT_RESOURCE)).getIdentifier();
        });
        getEventDispatcher().listenToEvent(ConfigureTexturesEvent.EVENT, e -> {
            ConfigureTexturesEvent event = (ConfigureTexturesEvent) e;
            //Register a default atlas
            TEXTURE_ATLAS = event.registerAtlas(ID, "atlas");
            //Add textures to atlas
            event.addTexture(GROUND_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_IDLE_0_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_IDLE_1_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_WALK_0_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_WALK_1_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_WALK_2_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_WALK_3_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_JUMP_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(DUKE_DEAD_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(BUG_0_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(BUG_1_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(FLY_0_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(FLY_1_TEXTURE, TEXTURE_ATLAS);
            event.addTexture(BACKGROUND_TEXTURE, TEXTURE_ATLAS);
        });
        getEventDispatcher().listenToEvent(MeshRegistrationEvent.EVENT, e -> {
            MeshRegistrationEvent event = (MeshRegistrationEvent) e;
            SPRITE_MESH = event.registerMesh(ID, "sprite", new Mesh(MESH_FORMAT, new SquareDataMesh()));
        });
        getEventDispatcher().listenToEvent(ModelConfigRegistrationEvent.EVENT, e -> {
            ModelConfigRegistrationEvent event = (ModelConfigRegistrationEvent) e;
            DUKE_IDLE_0_MODEL = event.registerModel(ID, "duke_idle_0", SPRITE_MESH, DUKE_IDLE_0_TEXTURE);
            DUKE_IDLE_1_MODEL = event.registerModel(ID, "duke_idle_1", SPRITE_MESH, DUKE_IDLE_1_TEXTURE);
            DUKE_WALK_0_MODEL = event.registerModel(ID, "duke_walk_0", SPRITE_MESH, DUKE_WALK_0_TEXTURE);
            DUKE_WALK_1_MODEL = event.registerModel(ID, "duke_walk_1", SPRITE_MESH, DUKE_WALK_1_TEXTURE);
            DUKE_WALK_2_MODEL = event.registerModel(ID, "duke_walk_2", SPRITE_MESH, DUKE_WALK_2_TEXTURE);
            DUKE_WALK_3_MODEL = event.registerModel(ID, "duke_walk_3", SPRITE_MESH, DUKE_WALK_3_TEXTURE);
            DUKE_JUMP_MODEL = event.registerModel(ID, "duke_jump", SPRITE_MESH, DUKE_JUMP_TEXTURE);
            DUKE_DEAD_MODEL = event.registerModel(ID, "duke_dead", SPRITE_MESH, DUKE_DEAD_TEXTURE);
            GROUND_MODEL = event.registerModel(ID, "ground", SPRITE_MESH, GROUND_TEXTURE);
            BUG_0_MODEL = event.registerModel(ID, "bug_0", SPRITE_MESH, BUG_0_TEXTURE);
            BUG_1_MODEL = event.registerModel(ID, "bug_1", SPRITE_MESH, BUG_1_TEXTURE);
            FLY_0_MODEL = event.registerModel(ID, "fly_0", SPRITE_MESH, FLY_0_TEXTURE);
            FLY_1_MODEL = event.registerModel(ID, "fly_1", SPRITE_MESH, FLY_1_TEXTURE);
            BACKGROUND_MODEL = event.registerModel(ID, "background", SPRITE_MESH, BACKGROUND_TEXTURE);
        });
        getEventDispatcher().listenToEvent(EntityComponentRegistrationEvent.EVENT, e -> {
            EntityComponentRegistrationEvent event = (EntityComponentRegistrationEvent) e;
            event.registerComponent(ModelComponent.class);
            event.registerComponent(TransformationComponent.class);
            event.registerComponent(PositionComponent.class);
            event.registerComponent(FixedOrthoCameraComponent.class);
            event.registerComponent(VelocityComponent.class);
            event.registerComponent(GroundComponent.class);
            event.registerComponent(BugComponent.class);
            event.registerComponent(BackgroundComponent.class);
            event.registerComponent(SoundSourceComponent.class);
            event.registerComponent(SoundListenerComponent.class);
            event.registerComponent(SquashedComponent.class);
            event.registerComponent(PlayerComponent.class);
            event.registerComponent(AnimatedSpriteComponent.class);
            event.registerComponent(FlyComponent.class);
            event.registerComponent(EnemyComponent.class);
            event.registerComponent(ConfettiComponent.class);
        });
        getEventDispatcher().listenToEvent(EntitySystemRegistrationEvent.EVENT, e -> {
            EntitySystemRegistrationEvent event = (EntitySystemRegistrationEvent) e;
            event.createSystem(GravitySystem.class);
            event.createSystem(AccelerationSystem.class);
            event.createSystem(UpdateGroundPositionsSystem.class);
            event.createSystem(UpdateEnemyPositionSystem.class);
            event.createSystem(SpawnEnemySystem.class);
            event.createSystem(UpdateBackgroundPositionsSystem.class);
            event.createSystem(CheckForCollisionSystem.class);
            event.createSystem(CountPassedBugsSystem.class);
            event.createSystem(AnimateSpritesSystem.class);
            event.createSystem(UpdateConfettiSystem.class);
            event.createSystem(SpawnConfettiSystem.class);
            event.createSystem(MovementSpeedSystem.class);
        });
        getEventDispatcher().listenToEvent(EntityTemplateRegistrationEvent.EVENT, e -> {
            EntityTemplateRegistrationEvent event = (EntityTemplateRegistrationEvent) e;
            PLAYER_CAMERA_ENTITY = event.createEntityTemplate(ID, "player_camera", entity -> {
                entity.addComponent(TransformationComponent.class).setPosition(0, 0, -10);
                entity.addComponent(FixedOrthoCameraComponent.class);
            });
            DUKE_ENTITY = event.createEntityTemplate(ID, "duke", entity -> {
                entity.addComponent(ModelComponent.class).setModel(DUKE_IDLE_1_MODEL);
                entity.addComponent(AnimatedSpriteComponent.class)
                        .addStateAndStages("idle", 0.005f, DUKE_IDLE_0_MODEL, DUKE_IDLE_1_MODEL)
                        .addStateAndStages("walk", 0.01f, DUKE_WALK_0_MODEL, DUKE_WALK_1_MODEL, DUKE_WALK_2_MODEL, DUKE_WALK_3_MODEL)
                        .addStateAndStages("jump", 1f, DUKE_JUMP_MODEL)
                        .addStateAndStages("dead", 0.5f, DUKE_DEAD_MODEL)
                        .updateAnimation("idle", 0.1f);
                entity.addComponent(TransformationComponent.class).setPosition(PLAYER_POSITION_X, GROUND_Y, 0).setScale(SCALE);
                entity.addComponent(VelocityComponent.class);
                entity.addComponent(SoundSourceComponent.class);
                entity.addComponent(SoundListenerComponent.class);
                entity.addComponent(PlayerComponent.class);
            });
            BUG_ENTITY = event.createEntityTemplate(ID, "bug", entity -> {
                entity.addComponent(ModelComponent.class).setModel(BUG_0_MODEL);
                entity.addComponent(AnimatedSpriteComponent.class)
                        .addStateAndStages("any", 0.01f, BUG_0_MODEL, BUG_1_MODEL)
                        .updateAnimation("any", 0.1f);
                entity.addComponent(BugComponent.class);
                entity.addComponent(EnemyComponent.class);
                entity.addComponent(TransformationComponent.class).setPosition(ENEMY, GROUND_Y, 1).setScale(SCALE);
                entity.addComponent(SoundSourceComponent.class);
            });
            FLY_ENTITY = event.createEntityTemplate(ID, "fly", entity -> {
                entity.addComponent(ModelComponent.class).setModel(FLY_0_MODEL);
                entity.addComponent(AnimatedSpriteComponent.class)
                        .addStateAndStages("any", 0.5f, FLY_0_MODEL, FLY_1_MODEL)
                        .updateAnimation("any", 0.1f);
                entity.addComponent(FlyComponent.class);
                entity.addComponent(EnemyComponent.class);
                entity.addComponent(TransformationComponent.class).setPosition(ENEMY, GROUND_Y + FLY_BASE_HEIGHT, 1).setScale(SCALE);
                entity.addComponent(SoundSourceComponent.class);
            });
            GROUND_ENTITY = event.createEntityTemplate(ID, "ground", entity -> {
                entity.addComponent(ModelComponent.class).setModel(GROUND_MODEL);
                entity.addComponent(TransformationComponent.class).setPosition(-300, GROUND_Y - 150, -1).setScale(SCALE*4f);
                entity.addComponent(GroundComponent.class);
            });
            BACKGROUND_ENTITY = event.createEntityTemplate(ID, "background", entity -> {
                entity.addComponent(ModelComponent.class).setModel(BACKGROUND_MODEL);
                entity.addComponent(TransformationComponent.class).setPosition(-300, 80, -2).setScale((SCALE+1)*8f);
                entity.addComponent(BackgroundComponent.class);
            });
            CONFETTI_ENTITY = event.createEntityTemplate(ID, "confetti", entity -> {
                entity.addComponent(TransformationComponent.class).setPosition(CONFETTI_SPAWN_LOCATION).setScale(CONFETTI_SCALE);
                entity.addComponent(VelocityComponent.class);
                entity.addComponent(ConfettiComponent.class);
            });
        });
        getEventDispatcher().listenToEvent(RoutineRegistrationEvent.EVENT, e -> {
            RoutineRegistrationEvent event = (RoutineRegistrationEvent) e;
            DEFAULT_ROUTINE = event.registerRoutine(Routine.builder(ID, "update_duke_positions")
                    .addStep(event.registerStep(ID, "gravity"), GravitySystem.class)
                    .addStep(event.registerStep(ID, "acceleration"), AccelerationSystem.class)
                    .addStep(event.registerStep(ID, "update_ground_positions"), UpdateGroundPositionsSystem.class)
                    .addStep(event.registerStep(ID, "update_bug_positions"), UpdateEnemyPositionSystem.class)
                    .addStep(event.registerStep(ID, "spawn_bug"), SpawnEnemySystem.class)
                    .addStep(event.registerStep(ID, "update_background_positions"), UpdateBackgroundPositionsSystem.class)
                    .addStep(event.registerStep(ID, "check_for_collision"), CheckForCollisionSystem.class)
                    .addStep(event.registerStep(ID, "count_passed_bugs"), CountPassedBugsSystem.class)
                    .addStep(event.registerStep(ID, "animate_sprites"), AnimateSpritesSystem.class)
                    .addStep(event.registerStep(ID, "update_confetti_positions"), UpdateConfettiSystem.class)
                    .addStep(event.registerStep(ID, "spawn_confetti"), SpawnConfettiSystem.class)
                    .addStep(event.registerStep(ID, "movement_speed"), MovementSpeedSystem.class)
                    .build());
        });
        getEventDispatcher().listenToEvent(RendererRegistrationEvent.EVENT, e -> {
            RendererRegistrationEvent event = (RendererRegistrationEvent) e;
            RENDER_GRAPH = event.registerGraph(ID, "render_graph",
                    new RenderGraph(RenderGraph.RenderPath.builder()
                            .addRoutineNode(DEFAULT_ROUTINE)
                            .addRenderNode(event.registerNode(ID, "draw_scene"), DrawSceneRenderNode.class, DEFAULT_SHADER_PROGRAM_CONFIG)
                            .addRenderNode(event.registerNode(ID, "draw_confetti"), DrawConfettiRenderNode.class, CONFETTI_SHADER_PROGRAM_CONFIG)
                            .addRenderNode(event.registerNode(ID, "draw_ui"), DrawUIRenderNode.class, ShaderProgramConfig.EMPTY)
                    )
            );
        });
        getEventDispatcher().listenToEvent(SceneRegistrationEvent.EVENT, e -> {
            SceneRegistrationEvent event = (SceneRegistrationEvent) e;
            DEFAULT_SCENE = event.registerScene(ID, "scene", new DefaultScene(RENDER_GRAPH, List.of()));
        });
        getEventDispatcher().listenToEvent(InputHandlerRegistrationEvent.EVENT, e -> {
            InputHandlerRegistrationEvent event = (InputHandlerRegistrationEvent) e;

            var inputHandler = event.getInputHandler();
            //Register Controls
            Control exitControl = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.ESCAPE));
            Control spacebar = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.SPACE));
            Control w = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.W));
            Control up = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.UP));
            Control shift = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.LEFT_SHIFT));
            Control s = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.S));
            Control down = inputHandler.registerControlListener(new KeyboardKeyControl(KeyboardInput.Key.DOWN));
            Control leftClickControl = inputHandler.registerControlListener(new MouseButtonControl(MouseInput.Button.LEFT_CLICK));
            Control mouseScrollUpControl = inputHandler.registerControlListener(new MouseScrollControl(MouseInput.ScrollDirection.UP, 1f));
            Control mouseScrollDownControl = inputHandler.registerControlListener(new MouseScrollControl(MouseInput.ScrollDirection.DOWN, 1f));
            //Register Controllers
            inputHandler.registerController(ID, "exit_game", new PauseGameController(exitControl));
            inputHandler.registerController(ID, "jump", new JumpController(spacebar, w, up));
            inputHandler.registerController(ID, "stomp", new StompController(shift, s, down));
            inputHandler.registerController(ID, "ui_click", new UIClickController(MouseInput.Button.LEFT_CLICK, leftClickControl));
            inputHandler.registerController(ID, "scroll", new UIScrollController(
                    new ControlGroup(mouseScrollUpControl),
                    new ControlGroup(mouseScrollDownControl)
            ));
        });
        getEventDispatcher().listenToEvent(GameStateRegistrationEvent.EVENT, e -> {
            GameStateRegistrationEvent event = (GameStateRegistrationEvent) e;
            CURRENT_SCORE = event.registerState(ID, "score", 0);
            GAME_STATE = event.registerState(ID, "alive", GameState.MAIN_MENU);
            PASSED_ENEMIES_THIS_ROUND = event.registerState(ID, "passed_enemies", 0);
        });
    }

    public static void restart(boolean returnToMainMenu) {
        MOVEMENT_SPEED = INITIAL_SPEED;
        ClientBase.getInstance().getStateHandler().getState(DukeGameClient.PASSED_ENEMIES_THIS_ROUND).setValue(0);
        ClientBase.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).setValue(0);
        ClientBase.getInstance().getManager().getEntitiesWith(EnemyComponent.class).forEach(Entity::free);
        ClientBase.getInstance().getStateHandler().getState(DukeGameClient.GAME_STATE).setValue(returnToMainMenu ? GameState.MAIN_MENU : GameState.GAME_RUNNING);
    }

    public static void main(String[] args) {
        DukeGameClient client = new DukeGameClient(ID, 60);
        client.start();
    }

    @Override
    public void init() {
        super.init();

        //Create window properties
        WindowProperties windowProperties = new WindowProperties(800, 600, "Duke Jump Game", DEFAULT_SCENE);
        //Create window
        long window = getWindowManager().createNewWindow(windowProperties);
        //Focus window
        getWindowManager().focusWindow(window);
    }

    public enum GameState {
        MAIN_MENU,
        GAME_RUNNING,
        PAUSED,
        DEAD
    }

    public static boolean isAlive() {
        return DukeGameClient.getInstance().getStateHandler().getState(GAME_STATE).getValue().equals(GameState.GAME_RUNNING);
    }

    public static boolean isPaused() {
        return DukeGameClient.getInstance().getStateHandler().getState(GAME_STATE).getValue().equals(GameState.PAUSED);
    }

    public record Score(String scoreHolder, int score) implements Comparable<Score> {

        @Override
        public int compareTo(Score o) {
            return Integer.compare(o.score, score);
        }
    }
}
