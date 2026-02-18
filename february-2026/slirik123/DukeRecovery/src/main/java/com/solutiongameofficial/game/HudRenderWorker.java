package com.solutiongameofficial.game;

import com.solutiongameofficial.game.hud.HudRenderer;
import com.solutiongameofficial.game.hud.HudSnapshot;

import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public final class HudRenderWorker {

    private final BlockingQueue<HudSnapshot> queue = new ArrayBlockingQueue<>(1);
    private final AtomicReference<BufferedImage> latestHudImage = new AtomicReference<>();

    private final Thread thread;

    public HudRenderWorker() {
        this.thread = new Thread(this::run, "Hud-Renderer");
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void shutdown() {
        thread.interrupt();
        try {
            thread.join(200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void submit(HudSnapshot snapshot) {
        while (!queue.offer(snapshot)) {
            queue.poll();
        }
    }

    public BufferedImage latestImageOrFallback() {
        BufferedImage img = latestHudImage.get();
        if (img != null) {
            return img;
        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    private void run() {
        HudRenderer hudRenderer = new HudRenderer();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                HudSnapshot snapshot = queue.take();
                BufferedImage rendered = hudRenderer.render(snapshot);
                latestHudImage.set(rendered);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ignored) { }
        }
    }
}