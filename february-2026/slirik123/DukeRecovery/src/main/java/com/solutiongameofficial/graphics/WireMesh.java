package com.solutiongameofficial.graphics;

import com.solutiongameofficial.phase.duke.LongHashSet;

import java.util.List;

public record WireMesh(List<Vector3> vertices, int[] edgeA, int[] edgeB) {

    public int vertexCount() {
        return vertices.size();
    }

    public Vector3 vertex(int vertexIndex) {
        return vertices.get(vertexIndex);
    }

    public int edgeCount() {
        return edgeA.length;
    }

    public int edgeA(int edgeIndex) {
        return edgeA[edgeIndex];
    }

    public int edgeB(int edgeIndex) {
        return edgeB[edgeIndex];
    }

    public static WireMesh fromTriangles(List<Vector3> vertices, int[] triangleA, int[] triangleB, int[] triangleC) {
        // Convert triangles to unique edges.
        LongHashSet uniqueEdges = new LongHashSet(triangleA.length * 6);

        for (int triangleIndex = 0; triangleIndex < triangleA.length; triangleIndex++) {
            int vertexIndexA = triangleA[triangleIndex];
            int vertexIndexB = triangleB[triangleIndex];
            int vertexIndexC = triangleC[triangleIndex];

            addUndirectedEdge(uniqueEdges, vertexIndexA, vertexIndexB);
            addUndirectedEdge(uniqueEdges, vertexIndexB, vertexIndexC);
            addUndirectedEdge(uniqueEdges, vertexIndexC, vertexIndexA);
        }

        int edgeCount = uniqueEdges.size();
        int[] edgeVertexA = new int[edgeCount];
        int[] edgeVertexB = new int[edgeCount];

        int edgeWriteIndex = 0;
        for (long packedEdgeKey : uniqueEdges.values()) {
            edgeVertexA[edgeWriteIndex] = (int) (packedEdgeKey >>> 32);
            edgeVertexB[edgeWriteIndex] = (int) (packedEdgeKey & 0xFFFFFFFFL);
            edgeWriteIndex++;
        }

        return new WireMesh(vertices, edgeVertexA, edgeVertexB);
    }

    private static void addUndirectedEdge(LongHashSet uniqueEdges, int vertexIndexA, int vertexIndexB) {
        int lowerVertexIndex = Math.min(vertexIndexA, vertexIndexB);
        int higherVertexIndex = Math.max(vertexIndexA, vertexIndexB);

        long packedEdgeKey = (((long) lowerVertexIndex) << 32) | (higherVertexIndex & 0xFFFFFFFFL);
        uniqueEdges.add(packedEdgeKey);
    }

    public record Triangles(List<Vector3> vertices, int[] triangleA, int[] triangleB, int[] triangleC) {
    }
}