package com.solutiongameofficial.io;

import com.solutiongameofficial.game.GameAction;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class InputFacade {

    private final BlockingQueue<GameAction> queue = new LinkedBlockingQueue<>();

    public boolean publish(GameAction action) {
        return queue.offer(action);
    }

    public Optional<GameAction> poll() {
        return Optional.ofNullable(queue.poll());
    }

}
