package com.terminalvelocitycabbage.dukejump.rendernodes;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.BugComponent;
import com.terminalvelocitycabbage.engine.client.ClientBase;
import com.terminalvelocitycabbage.engine.client.renderer.shader.ShaderProgramConfig;
import com.terminalvelocitycabbage.engine.client.ui.UIRenderNode;
import com.terminalvelocitycabbage.engine.debug.Log;
import com.terminalvelocitycabbage.engine.ecs.Entity;
import com.terminalvelocitycabbage.engine.registry.Identifier;
import com.terminalvelocitycabbage.templates.events.UICharInputEvent;
import com.terminalvelocitycabbage.templates.events.UIClickEvent;
import com.terminalvelocitycabbage.templates.events.UIScrollEvent;

import java.util.Collections;

public class DrawUIRenderNode extends UIRenderNode {

    public static boolean highScore = false;

    public DrawUIRenderNode(ShaderProgramConfig shaderProgramConfig) {
        super(shaderProgramConfig);
    }

    @Override
    protected Identifier[] getInterestedEvents() {
        return new Identifier[]{UIClickEvent.EVENT, UIScrollEvent.EVENT, UICharInputEvent.EVENT};
    }

    @Override
    protected void declareUI() {

        DukeGameClient.GameState state = (DukeGameClient.GameState) DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.GAME_STATE).getValue();
        boolean alive = DukeGameClient.isAlive();
        boolean paused = state.equals(DukeGameClient.GameState.PAUSED);
        boolean dead = state.equals(DukeGameClient.GameState.DEAD);
        boolean mainMenu = state.equals(DukeGameClient.GameState.MAIN_MENU);

        if (!alive) {
            var currentScore = DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue();
            container("grow-y grow-x px-[140] py-[30]", () -> {
                container("grow-x grow-y layout-y-[ttb]", () -> {
                    titleText();
                    if (dead) {
                        container("layout-y-[ttb] grow-x h-[300px] p-[10] gap-[5]", () -> {
                            container("layout-y-[ttb] grow-x align-x-[center]", () -> {
                                text("YOU DIED.", "grow-x text-size-[60] text-color-[1,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
                            });
                            container("layout-y-[ttb] pb-[2] grow-x align-x-[center]", () -> {
                                text("Score: " + currentScore, "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
                            });
                            container("layout-y-[ttb] grow-x", () -> {
                                nameInput();
                            });
                            retryButton();
                            mainMenuButton();
                        });
                    }
                    if (mainMenu) {
                        if (DukeGameClient.HIGH_SCORES.isEmpty()) {
                            tutorialText();
                        } else {
                            highScores();
                        }
                    }
                    if (paused) {
                        container("layout-y-[ttb] grow-x align-x-[center]", () -> {
                            text("PAUSED", "grow-x text-size-[60] text-color-[1,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
                        });
                    }
                    container("grow-x h-[80] py-[10] gap-[10]", () -> {
                        if (!dead) {
                            playButton(DukeGameClient.isPaused());
                            quitButton(DukeGameClient.isPaused());
                        }
                    });
                });
            });
        } else {
            container("float-root z-[10] align-x-[center] align-y-[center] w-[100] h-[30] attach-[top] to-[top] float-offset-y-[30] p-[10]", () -> {
                text(DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue().toString(),
                        "text-size-[40] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            });
            highScoreTextArea();
        }
    }

    private void highScoreTextArea() {
        if (!DukeGameClient.HIGH_SCORES.isEmpty()) {
            if (((int) ClientBase.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue()) > DukeGameClient.HIGH_SCORES.getFirst().score()) {
                container("float-root z-[10] align-x-[center] align-y-[center] w-[100] h-[30] attach-[top] to-[top] float-offset-y-[80] p-[10]", () -> {
                    text("NEW HIGH SCORE", "text-size-[20] text-color-[1,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
                });
            } else {
                container("float-root z-[10] align-x-[center] align-y-[center] w-[100] h-[30] attach-[top] to-[top] float-offset-y-[80] p-[10]", () -> {
                    text("SCORE TO BEAT: " + DukeGameClient.HIGH_SCORES.getFirst().score(),
                            "text-size-[14] text-color-[1,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
                });
            }
        }
    }

    private void lineBreak() {
        container("h-[20]", () -> {});
    }

    private void defaultText(String text) {
        text(text, "text-size-[20] text-wrap-words text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
    }

    private void tutorialText() {
        container("grow layout-y-[ttb] gap-[5] bg-[1,1,1,1] p-[12]", () -> {
            text("HOW TO PLAY:", "text-size-[30] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            lineBreak();
            defaultText("The goal of the game is to help Duke avoid and squash bugs");
            lineBreak();
            defaultText("- Press spacebar/W/up to jump and avoid bugs");
            lineBreak();
            defaultText("- Land on bugs' heads to squash them for extra points - harder enemies earn more points!");
            lineBreak();
            defaultText("- Save and share your high scores with friends!");
        });
    }

    private void titleText() {
        container("grow-x px-[40] py-[5] align-x-[center]", () -> {
            text("DUKE JUMP", "text-size-[40] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
    }

    private void retryButton() {

        int buttonID = id("retryMenuButton");

        container(buttonID, "grow-x mt-[10] h-[40px] p-[5] align-x-[center] align-y-[center] border-width-[3] border-color-[0,0,0,1] " + (isHovered(buttonID) ? "bg-[.95,.95,.95,1]" : "bg-[1,1,1,1]"), () -> {
            text("RETRY WITHOUT SAVING SCORE", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });

        if (heardEvent(buttonID, UIClickEvent.EVENT) instanceof UIClickEvent) {
            DukeGameClient.restart(false);
        }
    }

    private void mainMenuButton() {

        int buttonID = id("mainMenuButton");

        container(buttonID, "grow-x mt-[10] h-[40px] p-[5] align-x-[center] align-y-[center] border-width-[3] border-color-[0,0,0,1] " + (isHovered(buttonID) ? "bg-[.95,.95,.95,1]" : "bg-[1,1,1,1]"), () -> {
            text("RETURN TO MAIN MENU", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });

        if (heardEvent(buttonID, UIClickEvent.EVENT) instanceof UIClickEvent) {
            DukeGameClient.restart(true);
        }
    }

    private void nameInput() {

        int inputId = id("nameInput");
        int buttonID = id("submitNameButton");
        var textState = useState(inputId + "_text", "");

        container("grow-x layout-y-[ttb]", () -> {
            text("Save High Score:", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            input(inputId, "bg-[1,1,1,1] grow-x h-[40px] p-[5] layout-x-[rtr] align-y-[center] gap-[2] border-color-[0,0,0,1] border-width-[3] clip",
                    "bg-[1,0,0,1] w-[2px] h-[20px]",
                    "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]"
            );

            container(buttonID, "grow-x mt-[10] h-[40px] py-[5] align-x-[center] align-y-[center] border-width-[3] border-color-[0,0,0,1] " + (isHovered(buttonID) ? "bg-[.95,.95,.95,1]" : "bg-[1,1,1,1]"), () -> {
                text("SUBMIT", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            });
        });

        if (heardEvent(buttonID, UIClickEvent.EVENT) instanceof UIClickEvent) {
            addHighScoreAndReturnToMainMenu(textState.getValue(), (Integer) ClientBase.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue());
        }

        if (heardEvent(inputId, UICharInputEvent.EVENT) instanceof UICharInputEvent event) {
            if (event.isSpecialInput() && event.isEnter()) {
                addHighScoreAndReturnToMainMenu(textState.getValue(), (Integer) ClientBase.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue());
            }
        }
    }

    private void addHighScoreAndReturnToMainMenu(String name, int score) {
        DukeGameClient.HIGH_SCORES.add(new DukeGameClient.Score(name, score));
        Collections.sort(DukeGameClient.HIGH_SCORES);
        DukeGameClient.restart(true);
    }

    private void listItem(String first, String middle, String last, boolean compared) {
        container("grow-x layout-x-[ltr] gap-[5] bg-[.9,.9,.9,1] p-[4]" + (compared ? "px-[30]" : ""), () -> {
            container("bg-[1,1,1,1] p-[4]", () -> text(first, "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]"));
            text(middle, "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            container("grow-x", () -> {
            });
            text(last, "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
    }

    private void highScores() {
        container("grow-x px-[40] py-[5] align-x-[center]", () -> {
            text("High Scores", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
        container("grow-x layout-x-[ltr] gap-[5] px-[8] pt-[10] bg-[1,1,1,1]", () -> {
            text("Place", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            container("grow-x", () -> {
            });
            text("Player", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
            container("grow-x", () -> {
            });
            text("Score", "text-size-[20] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
        scrollableContainer(
                "bg-[1,16,1,1] layout-y-[ttb] grow-x h-[300px] p-[10] gap-[5]",
                "bg-[1,0,0,1] w-[10px] fit-y", () -> {
                    for (int i = 0; i < DukeGameClient.HIGH_SCORES.size(); i++) {
                        DukeGameClient.Score score = DukeGameClient.HIGH_SCORES.get(i);
                        listItem(String.valueOf(i + 1), score.scoreHolder(), String.valueOf(score.score()), false);
                    }
                    container("h-[20]", () -> {
                    });
                });
    }

    private void playButton(boolean isPaused) {

        int buttonID = id("playButton");

        if (heardEvent(buttonID, UIClickEvent.EVENT) instanceof UIClickEvent) {
            if (isPaused) {
                DukeGameClient.getInstance().getStateHandler().updateState(DukeGameClient.GAME_STATE, DukeGameClient.GameState.GAME_RUNNING);
            } else {
                DukeGameClient.restart(false);
            }
        }

        container(buttonID, "grow py-[5] align-x-[center] align-y-[center] border-width-[3] border-color-[0,0,0,1] " + (isHovered(buttonID) ? "bg-[.95,.95,.95,1]" : "bg-[1,1,1,1]"), () -> {
            text("PLAY", "text-size-[40] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
    }

    private void quitButton(boolean returnToMainMenuInstead) {

        int buttonID = id("quitButton");

        if (heardEvent(buttonID, UIClickEvent.EVENT) instanceof UIClickEvent) {
            if (returnToMainMenuInstead) {
                DukeGameClient.restart(true);
            } else {
                DukeGameClient.getInstance().getWindowManager().closeFocusedWindow();
            }
        }

        container(buttonID, "grow py-[5] align-x-[center] align-y-[center] border-width-[3] border-color-[0,0,0,1] " + (isHovered(buttonID) ? "bg-[.95,.95,.95,1]" : "bg-[1,1,1,1]"), () -> {
            text(returnToMainMenuInstead ? "MAIN MENU" : "QUIT GAME", "text-size-[40] text-color-[0,0,0,1] font-[" + DukeGameClient.PIXEL_FONT + "]");
        });
    }
}
