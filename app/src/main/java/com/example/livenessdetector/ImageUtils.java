package com.example.livenessdetector;

import java.nio.ByteBuffer;


public class ImageUtils {
    public static void convertYUV420ToARGB8888(
            ByteBuffer yBuffer, ByteBuffer uBuffer, ByteBuffer vBuffer,
            int width, int height, int yRowStride, int uvRowStride, int uvPixelStride,
            int[] out) {
        for (int y = 0; y < height; y++) {
            int pY = yRowStride * y;
            int uvRowStart = uvRowStride * (y >> 1);

            for (int x = 0; x < width; x++) {
                int uvOffset = uvRowStart + (x >> 1) * uvPixelStride;

                out[y * width + x] = YUV2RGB(
                        yBuffer.get(pY + x) & 0xFF,
                        uBuffer.get(uvOffset) & 0xFF,
                        vBuffer.get(uvOffset) & 0xFF);
            }
        }
    }

    private static int YUV2RGB(int y, int u, int v) {
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        r = r > 262143 ? 262143 : r < 0 ? 0 : r;
        g = g > 262143 ? 262143 : g < 0 ? 0 : g;
        b = b > 262143 ? 262143 : b < 0 ? 0 : b;

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }
}
