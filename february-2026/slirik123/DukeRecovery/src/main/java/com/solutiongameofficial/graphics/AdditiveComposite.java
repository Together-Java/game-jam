package com.solutiongameofficial.graphics;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public record AdditiveComposite(int alpha) implements Composite {

    public AdditiveComposite {
        if (alpha < 0f) {
            alpha = 0;
        }
        if (alpha > 1) {
            alpha = 1;
        }
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new Ctx(alpha);
    }

    private record Ctx(int alpha255) implements CompositeContext {

            private Ctx {
                alpha255 = (int) (alpha255 * 255f + 0.5f);
            }

            @Override
            public void dispose() {
            }

            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                int width = Math.min(src.getWidth(), dstIn.getWidth());
                int height = Math.min(src.getHeight(), dstIn.getHeight());

                int[] source = new int[4];
                int[] destination = new int[4];

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        src.getPixel(x, y, source);
                        dstIn.getPixel(x, y, destination);

                        int sa = (source[3] * alpha255) / 255;
                        if (sa <= 0) {
                            dstOut.setPixel(x, y, destination);
                            continue;
                        }

                        destination[0] = clamp255(destination[0] + (source[0] * sa) / 255);
                        destination[1] = clamp255(destination[1] + (source[1] * sa) / 255);
                        destination[2] = clamp255(destination[2] + (source[2] * sa) / 255);
                        destination[3] = Math.max(destination[3], sa);

                        dstOut.setPixel(x, y, destination);
                    }
                }
            }

            private static int clamp255(int v) {
                if (v < 0) {
                    return 0;
                }
                return Math.min(v, 255);
            }
        }
}
