package com.solutiongameofficial.io;

import com.solutiongameofficial.graphics.Renderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public final class PngStreamRenderer implements Renderer {

    private final DataOutputStream outputStream;

    public PngStreamRenderer(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
    }

    @Override
    public synchronized void present(BufferedImage image) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(64 * 1024);
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();

            outputStream.writeInt(bytes.length);
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public void close() {
        try {
            outputStream.flush();
        } catch (IOException ignored) {}
    }
}
