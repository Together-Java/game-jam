package com.solutiongameofficial.io;

import com.solutiongameofficial.graphics.Vector3;
import com.solutiongameofficial.graphics.WireMesh;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StlLoader {

    public static WireMesh loadFromResource(String resourcePath) {
        byte[] resourceBytes = ResourceLoader.readAllBytes(resourcePath);

        // binary STL has 80-byte header + 4-byte tri count, and expected size matches
        if (looksLikeBinaryStl(resourceBytes)) {
            return loadBinary(resourceBytes);
        }

        // Fallback: ASCII
        String asciiText = new String(resourceBytes, StandardCharsets.US_ASCII);
        return loadAscii(asciiText);
    }

    private static boolean looksLikeBinaryStl(byte[] stlBytes) {
        if (stlBytes.length < 84) {
            return false;
        }

        ByteBuffer stlByteBuffer = ByteBuffer.wrap(stlBytes).order(ByteOrder.LITTLE_ENDIAN);
        stlByteBuffer.position(80);

        long triangleCount = Integer.toUnsignedLong(stlByteBuffer.getInt());
        long expectedLength = 84L + triangleCount * 50L;
        if (expectedLength == stlBytes.length) {
            return true;
        }

        int startLength = 5;
        String startText = new String(stlBytes, 0, startLength, StandardCharsets.US_ASCII).toLowerCase();
        return !startText.startsWith("solid");
    }

    private static WireMesh loadBinary(byte[] stlBytes) {
        ByteBuffer stlByteBuffer = ByteBuffer.wrap(stlBytes).order(ByteOrder.LITTLE_ENDIAN);
        stlByteBuffer.position(80);

        int triangleCount = stlByteBuffer.getInt();

        List<Vector3> vertices = new ArrayList<>(triangleCount * 3);
        int[] triangleA = new int[triangleCount];
        int[] triangleB = new int[triangleCount];
        int[] triangleC = new int[triangleCount];

        // 80 header + 4 count
        stlByteBuffer.position(84);

        for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
            // normal (ignored)
            stlByteBuffer.getFloat();
            stlByteBuffer.getFloat();
            stlByteBuffer.getFloat();

            int vertexIndexA = vertices.size();
            vertices.add(new Vector3(stlByteBuffer.getFloat(), stlByteBuffer.getFloat(), stlByteBuffer.getFloat()));

            int vertexIndexB = vertices.size();
            vertices.add(new Vector3(stlByteBuffer.getFloat(), stlByteBuffer.getFloat(), stlByteBuffer.getFloat()));

            int vertexIndexC = vertices.size();
            vertices.add(new Vector3(stlByteBuffer.getFloat(), stlByteBuffer.getFloat(), stlByteBuffer.getFloat()));

            triangleA[triangleIndex] = vertexIndexA;
            triangleB[triangleIndex] = vertexIndexB;
            triangleC[triangleIndex] = vertexIndexC;

            // attribute byte count
            stlByteBuffer.getShort();
        }

        WireMesh.Triangles triangles = new WireMesh.Triangles(vertices, triangleA, triangleB, triangleC);
        return WireMesh.fromTriangles(triangles.vertices(), triangles.triangleA(), triangles.triangleB(), triangles.triangleC());
    }

    private static WireMesh loadAscii(String stlText) {
        String[] lines = stlText.split("\n");

        List<Vector3> vertices = new ArrayList<>();
        List<Integer> triangleA = new ArrayList<>();
        List<Integer> triangleB = new ArrayList<>();
        List<Integer> triangleC = new ArrayList<>();

        Vector3 pendingVertex0 = null;
        Vector3 pendingVertex1 = null;
        Vector3 pendingVertex2 = null;

        for (String rawLine : lines) {
            String trimmedLine = rawLine.trim();
            if (!trimmedLine.startsWith("vertex ")) {
                continue;
            }

            String[] parts = trimmedLine.split("\\s+");
            if (parts.length < 4) {
                continue;
            }

            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float z = Float.parseFloat(parts[3]);

            if (pendingVertex0 == null) {
                pendingVertex0 = new Vector3(x, y, z);
            } else if (pendingVertex1 == null) {
                pendingVertex1 = new Vector3(x, y, z);
            } else {
                pendingVertex2 = new Vector3(x, y, z);
            }

            if (pendingVertex1 != null && pendingVertex2 != null) {
                int vertexIndexA = vertices.size();
                vertices.add(pendingVertex0);

                int vertexIndexB = vertices.size();
                vertices.add(pendingVertex1);

                int vertexIndexC = vertices.size();
                vertices.add(pendingVertex2);

                triangleA.add(vertexIndexA);
                triangleB.add(vertexIndexB);
                triangleC.add(vertexIndexC);

                pendingVertex0 = null;
                pendingVertex1 = null;
                pendingVertex2 = null;
            }
        }

        int triangleCount = triangleA.size();
        int[] triangleAArray = new int[triangleCount];
        int[] triangleBArray = new int[triangleCount];
        int[] triangleCArray = new int[triangleCount];

        for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
            triangleAArray[triangleIndex] = triangleA.get(triangleIndex);
            triangleBArray[triangleIndex] = triangleB.get(triangleIndex);
            triangleCArray[triangleIndex] = triangleC.get(triangleIndex);
        }

        WireMesh.Triangles triangles = new WireMesh.Triangles(vertices, triangleAArray, triangleBArray, triangleCArray);
        return WireMesh.fromTriangles(triangles.vertices(), triangles.triangleA(), triangles.triangleB(), triangles.triangleC());
    }
}