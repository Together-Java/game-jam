package com.solutiongameofficial.game.hud;

import com.solutiongameofficial.graphics.AdditiveComposite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HudBloom {

    private static final Map<Integer, KernelPair> KERNEL_CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<BloomScratch> SCRATCH = ThreadLocal.withInitial(BloomScratch::new);

    public static BufferedImage buildBloomLayer(BufferedImage sourceImage, Color bloomTintColor) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        BloomScratch scratch = SCRATCH.get();
        scratch.ensureCapacity(sourceWidth, sourceHeight);

        extractBrightAlphaTintInto(
                sourceImage,
                bloomTintColor,
                scratch.brightTintedMask
        );

        // Clear accumulated
        Graphics2D accumulatedGraphics = scratch.accumulatedBloomImage.createGraphics();
        try {
            accumulatedGraphics.setComposite(AlphaComposite.Src);
            accumulatedGraphics.setColor(new Color(0, 0, 0, 0));
            accumulatedGraphics.fillRect(0, 0, sourceWidth, sourceHeight);

            accumulatedGraphics.setComposite(new AdditiveComposite(1));
            accumulatedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            BufferedImage currentMipImage = scratch.brightTintedMask;

            KernelPair kernels = getKernels();

            for (int bloomLevelIndex = 0; bloomLevelIndex < HudConfig.BLOOM_LEVELS; bloomLevelIndex++) {
                if (bloomLevelIndex > 0) {
                    currentMipImage = downscaleHalfByAveraging2x2Reusable(currentMipImage, scratch);
                }

                BufferedImage blurred = blurGaussianReusable(currentMipImage, scratch, kernels);

                // Upscale back to full res and add
                accumulatedGraphics.drawImage(blurred, 0, 0, sourceWidth, sourceHeight, null);
            }
        } finally {
            accumulatedGraphics.dispose();
        }

        BufferedImage out = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D outG = out.createGraphics();
        try {
            outG.setComposite(AlphaComposite.Src);
            outG.drawImage(scratch.accumulatedBloomImage, 0, 0, null);
        } finally {
            outG.dispose();
        }

        return scaleBloomIntensity(out, HudConfig.BLOOM_INTENSITY);
    }

    private static KernelPair getKernels() {
        if (HudConfig.BLOOM_BLUR_RADIUS <= 0) {
            return KernelPair.NOOP;
        }
        return KERNEL_CACHE.computeIfAbsent(HudConfig.BLOOM_BLUR_RADIUS, radius -> {
            float[] gaussianKernel1D = buildGaussianKernel1D(radius);
            Kernel horizontalKernel = new Kernel(gaussianKernel1D.length, 1, gaussianKernel1D);
            Kernel verticalKernel = new Kernel(1, gaussianKernel1D.length, gaussianKernel1D);

            // ConvolveOp allocation is expensive, so it's cached too
            ConvolveOp horizontalBlurOperation = new ConvolveOp(horizontalKernel, ConvolveOp.EDGE_NO_OP, null);
            ConvolveOp verticalBlurOperation = new ConvolveOp(verticalKernel, ConvolveOp.EDGE_NO_OP, null);

            return new KernelPair(horizontalBlurOperation, verticalBlurOperation);
        });
    }

    private static void extractBrightAlphaTintInto(
            BufferedImage sourceImage,
            Color tintColor,
            BufferedImage outImage
    ) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        int[] sourcePixelsArgb = sourceImage.getRGB(0, 0, sourceWidth, sourceHeight, null, 0, sourceWidth);
        int[] outputPixelsArgb = new int[sourcePixelsArgb.length];

        int tintRed = tintColor.getRed();
        int tintGreen = tintColor.getGreen();
        int tintBlue = tintColor.getBlue();

        for (int pixelIndex = 0; pixelIndex < sourcePixelsArgb.length; pixelIndex++) {
            int alpha = (sourcePixelsArgb[pixelIndex] >>> 24) & 0xFF;

            if (alpha <= HudConfig.BLOOM_THRESHOLD_ALPHA) {
                outputPixelsArgb[pixelIndex] = 0;
                continue;
            }

            outputPixelsArgb[pixelIndex] = (alpha << 24) | (tintRed << 16) | (tintGreen << 8) | tintBlue;
        }

        outImage.setRGB(0, 0, sourceWidth, sourceHeight, outputPixelsArgb, 0, sourceWidth);
    }

    private static BufferedImage downscaleHalfByAveraging2x2Reusable(BufferedImage sourceImage, BloomScratch scratch) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        int downscaledWidth = Math.max(1, sourceWidth / 2);
        int downscaledHeight = Math.max(1, sourceHeight / 2);

        BufferedImage target = scratch.acquireDownscaleTarget(downscaledWidth, downscaledHeight);

        int[] sourcePixelsArgb = sourceImage.getRGB(0, 0, sourceWidth, sourceHeight, null, 0, sourceWidth);
        int[] downscaledPixelsArgb = new int[downscaledWidth * downscaledHeight];

        for (int downscaledY = 0; downscaledY < downscaledHeight; downscaledY++) {
            int sourceY0 = downscaledY * 2;
            int sourceY1 = Math.min(sourceY0 + 1, sourceHeight - 1);

            for (int downscaledX = 0; downscaledX < downscaledWidth; downscaledX++) {
                int sourceX0 = downscaledX * 2;
                int sourceX1 = Math.min(sourceX0 + 1, sourceWidth - 1);

                int pixel00 = sourcePixelsArgb[sourceY0 * sourceWidth + sourceX0];
                int pixel10 = sourcePixelsArgb[sourceY0 * sourceWidth + sourceX1];
                int pixel01 = sourcePixelsArgb[sourceY1 * sourceWidth + sourceX0];
                int pixel11 = sourcePixelsArgb[sourceY1 * sourceWidth + sourceX1];

                int averagedAlpha = (
                        ((pixel00 >>> 24) & 0xFF) +
                                ((pixel10 >>> 24) & 0xFF) +
                                ((pixel01 >>> 24) & 0xFF) +
                                ((pixel11 >>> 24) & 0xFF)
                ) / 4;

                int averagedRed = (
                        ((pixel00 >>> 16) & 0xFF) +
                                ((pixel10 >>> 16) & 0xFF) +
                                ((pixel01 >>> 16) & 0xFF) +
                                ((pixel11 >>> 16) & 0xFF)
                ) / 4;

                int averagedGreen = (
                        ((pixel00 >>> 8) & 0xFF) +
                                ((pixel10 >>> 8) & 0xFF) +
                                ((pixel01 >>> 8) & 0xFF) +
                                ((pixel11 >>> 8) & 0xFF)
                ) / 4;

                int averagedBlue = (
                        (pixel00 & 0xFF) +
                                (pixel10 & 0xFF) +
                                (pixel01 & 0xFF) +
                                (pixel11 & 0xFF)
                ) / 4;

                downscaledPixelsArgb[downscaledY * downscaledWidth + downscaledX] =
                        (averagedAlpha << 24) |
                                (averagedRed << 16) |
                                (averagedGreen << 8) |
                                averagedBlue;
            }
        }

        target.setRGB(0, 0, downscaledWidth, downscaledHeight, downscaledPixelsArgb, 0, downscaledWidth);
        return target;
    }

    private static BufferedImage blurGaussianReusable(BufferedImage sourceImage, BloomScratch scratch, KernelPair kernels) {
        if (kernels == KernelPair.NOOP) {
            return sourceImage;
        }

        BufferedImage tmp = scratch.acquireBlurTmp(sourceImage.getWidth(), sourceImage.getHeight());
        BufferedImage out = scratch.acquireBlurOut(sourceImage.getWidth(), sourceImage.getHeight());

        kernels.horizontal.filter(sourceImage, tmp);
        kernels.vertical.filter(tmp, out);

        return out;
    }

    private static float[] buildGaussianKernel1D(int blurRadiusPixels) {
        int kernelSize = blurRadiusPixels * 2 + 1;
        float[] kernelWeights = new float[kernelSize];

        float sigma = Math.max(0.1f, blurRadiusPixels / 3f);
        float twoSigmaSquared = 2f * sigma * sigma;

        float weightSum = 0f;
        for (int kernelIndex = 0; kernelIndex < kernelSize; kernelIndex++) {
            int x = kernelIndex - blurRadiusPixels;
            float weight = (float) Math.exp(-(x * x) / twoSigmaSquared);
            kernelWeights[kernelIndex] = weight;
            weightSum += weight;
        }

        for (int kernelIndex = 0; kernelIndex < kernelSize; kernelIndex++) {
            kernelWeights[kernelIndex] /= weightSum;
        }

        return kernelWeights;
    }

    public static BufferedImage scaleBloomIntensity(BufferedImage sourceBloomImage, float intensityMultiplier) {
        if (intensityMultiplier <= 0f) {
            return new BufferedImage(sourceBloomImage.getWidth(), sourceBloomImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        if (Math.abs(intensityMultiplier - 1f) < 0.0001f) {
            return sourceBloomImage;
        }

        int bloomWidth = sourceBloomImage.getWidth();
        int bloomHeight = sourceBloomImage.getHeight();

        int[] bloomPixelsArgb = sourceBloomImage.getRGB(0, 0, bloomWidth, bloomHeight, null, 0, bloomWidth);
        for (int pixelIndex = 0; pixelIndex < bloomPixelsArgb.length; pixelIndex++) {
            int argb = bloomPixelsArgb[pixelIndex];

            int alpha = (argb >>> 24) & 0xFF;
            if (alpha == 0) {
                continue;
            }

            int red = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8) & 0xFF;
            int blue = argb & 0xFF;

            red = clamp255((int) (red * intensityMultiplier));
            green = clamp255((int) (green * intensityMultiplier));
            blue = clamp255((int) (blue * intensityMultiplier));

            bloomPixelsArgb[pixelIndex] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        sourceBloomImage.setRGB(0, 0, bloomWidth, bloomHeight, bloomPixelsArgb, 0, bloomWidth);
        return sourceBloomImage;
    }

    private static int clamp255(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 255);
    }

    private record KernelPair(ConvolveOp horizontal, ConvolveOp vertical) {
            static final KernelPair NOOP = new KernelPair(null, null);
    }

    private static final class BloomScratch {

        BufferedImage brightTintedMask;
        BufferedImage accumulatedBloomImage;

        // Small pool because sizes shrink as we mip down
        private final SoftReference<Map<Long, BufferedImage>> downscalePoolRef = new SoftReference<>(new ConcurrentHashMap<>());
        private final SoftReference<Map<Long, BufferedImage>> blurTmpPoolRef = new SoftReference<>(new ConcurrentHashMap<>());
        private final SoftReference<Map<Long, BufferedImage>> blurOutPoolRef = new SoftReference<>(new ConcurrentHashMap<>());

        void ensureCapacity(int width, int height) {
            if (brightTintedMask == null || brightTintedMask.getWidth() != width || brightTintedMask.getHeight() != height) {
                brightTintedMask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            if (accumulatedBloomImage == null || accumulatedBloomImage.getWidth() != width || accumulatedBloomImage.getHeight() != height) {
                accumulatedBloomImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }

        BufferedImage acquireDownscaleTarget(int width, int height) {
            return acquireFromPool(downscalePoolRef, width, height);
        }

        BufferedImage acquireBlurTmp(int width, int height) {
            return acquireFromPool(blurTmpPoolRef, width, height);
        }

        BufferedImage acquireBlurOut(int width, int height) {
            return acquireFromPool(blurOutPoolRef, width, height);
        }

        private static BufferedImage acquireFromPool(SoftReference<Map<Long, BufferedImage>> poolRef, int width, int height) {
            Map<Long, BufferedImage> pool = poolRef.get();
            if (pool == null) {
                pool = new ConcurrentHashMap<>();
                poolRef.clear();
            }

            long key = (((long) width) << 32) | (height & 0xFFFFFFFFL);

            BufferedImage existing = pool.get(key);
            if (existing != null) {
                return existing;
            }

            BufferedImage created = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            pool.put(key, created);
            return created;
        }
    }
}