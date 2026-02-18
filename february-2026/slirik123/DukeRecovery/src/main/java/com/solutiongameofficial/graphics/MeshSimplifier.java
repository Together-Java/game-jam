package com.solutiongameofficial.graphics;

import com.solutiongameofficial.phase.duke.LongHashSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshSimplifier {

    public static WireMesh quantizeAndRebuild(WireMesh inputMesh, double cellSize) {
        if (cellSize <= 0.0) {
            return inputMesh;
        }

        int inputVertexCount = inputMesh.vertexCount();

        Map<Long, Integer> quantizedKeyToNewVertexIndex = new HashMap<>(inputVertexCount * 2);
        List<Vector3> rebuiltVertices = new ArrayList<>(inputVertexCount / 2);

        int[] inputVertexIndexToRebuiltVertexIndex = new int[inputVertexCount];

        for (int inputVertexIndex = 0; inputVertexIndex < inputVertexCount; inputVertexIndex++) {
            Vector3 inputVertex = inputMesh.vertex(inputVertexIndex);

            int quantizedX = quantizeToCell(inputVertex.x(), cellSize);
            int quantizedY = quantizeToCell(inputVertex.y(), cellSize);
            int quantizedZ = quantizeToCell(inputVertex.z(), cellSize);

            long quantizedKey = pack3QuantizedInts(quantizedX, quantizedY, quantizedZ);

            Integer existingRebuiltVertexIndex = quantizedKeyToNewVertexIndex.get(quantizedKey);
            if (existingRebuiltVertexIndex == null) {
                int rebuiltVertexIndex = rebuiltVertices.size();
                quantizedKeyToNewVertexIndex.put(quantizedKey, rebuiltVertexIndex);

                // Snapped coordinates so it looks stylized
                rebuiltVertices.add(new Vector3(
                        quantizedX * cellSize,
                        quantizedY * cellSize,
                        quantizedZ * cellSize
                ));

                inputVertexIndexToRebuiltVertexIndex[inputVertexIndex] = rebuiltVertexIndex;
            } else {
                inputVertexIndexToRebuiltVertexIndex[inputVertexIndex] = existingRebuiltVertexIndex;
            }
        }

        // Rebuild edges with remapped indices, keep unique
        LongHashSet uniqueEdges = getLongHashSet(inputMesh, inputVertexIndexToRebuiltVertexIndex);

        int[] rebuiltEdgeA = new int[uniqueEdges.size()];
        int[] rebuiltEdgeB = new int[uniqueEdges.size()];

        int rebuiltEdgeIndex = 0;
        for (long packedEdgeKey : uniqueEdges.values()) {
            rebuiltEdgeA[rebuiltEdgeIndex] = (int) (packedEdgeKey >>> 32);
            rebuiltEdgeB[rebuiltEdgeIndex] = (int) (packedEdgeKey & 0xFFFFFFFFL);
            rebuiltEdgeIndex++;
        }

        return new WireMesh(rebuiltVertices, rebuiltEdgeA, rebuiltEdgeB);
    }

    private static LongHashSet getLongHashSet(WireMesh inputMesh, int[] inputVertexIndexToRebuiltVertexIndex) {
        LongHashSet uniqueEdges = new LongHashSet(inputMesh.edgeCount() * 2);
        for (int inputEdgeIndex = 0; inputEdgeIndex < inputMesh.edgeCount(); inputEdgeIndex++) {
            int rebuiltVertexA = inputVertexIndexToRebuiltVertexIndex[inputMesh.edgeA(inputEdgeIndex)];
            int rebuiltVertexB = inputVertexIndexToRebuiltVertexIndex[inputMesh.edgeB(inputEdgeIndex)];

            if (rebuiltVertexA == rebuiltVertexB) {
                continue;
            }

            int lowerVertexIndex = Math.min(rebuiltVertexA, rebuiltVertexB);
            int higherVertexIndex = Math.max(rebuiltVertexA, rebuiltVertexB);

            long packedEdgeKey = (((long) lowerVertexIndex) << 32) | (higherVertexIndex & 0xFFFFFFFFL);
            uniqueEdges.add(packedEdgeKey);
        }
        return uniqueEdges;
    }

    private static int quantizeToCell(double value, double cellSize) {
        return (int) Math.round(value / cellSize);
    }

    private static long pack3QuantizedInts(int quantizedX, int quantizedY, int quantizedZ) {
        // Pack 3 signed ints into a long with some bias
        // This is not cryptographic, just a stable hashable key
        long packedX = (quantizedX & 0x1FFFFF);
        long packedY = (quantizedY & 0x1FFFFF);
        long packedZ = (quantizedZ & 0x1FFFFF);
        return (packedX << 42) | (packedY << 21) | packedZ;
    }
}