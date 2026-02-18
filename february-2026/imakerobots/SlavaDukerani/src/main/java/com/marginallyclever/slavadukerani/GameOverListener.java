package com.marginallyclever.slavadukerani;

import java.util.EventListener;

/// GameOverListener is an interface for listening to game over events from SlavaDukerani.
public interface GameOverListener extends EventListener {
    void gameOver(boolean won);
}
