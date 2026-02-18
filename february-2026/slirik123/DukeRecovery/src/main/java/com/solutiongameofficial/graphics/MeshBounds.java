package com.solutiongameofficial.graphics;

public record MeshBounds(double minX, double minY, double minZ,
                         double maxX, double maxY, double maxZ) {

    public double centerX() {
        return (minX + maxX) * 0.5;
    }

    public double centerY() {
        return (minY + maxY) * 0.5;
    }

    public double centerZ() {
        return (minZ + maxZ) * 0.5;
    }

    public double sizeX() {
        return maxX - minX;
    }

    public double sizeY() {
        return maxY - minY;
    }

    public double sizeZ() {
        return maxZ - minZ;
    }

    public static MeshBounds from(WireMesh mesh) {
        if (mesh.vertexCount() == 0) {
            return new MeshBounds(0, 0, 0, 1, 1, 1);
        }

        Vector3 vectorZero = mesh.vertex(0);

        double minX = vectorZero.x(), minY = vectorZero.y(), minZ = vectorZero.z();
        double maxX = vectorZero.x(), maxY = vectorZero.y(), maxZ = vectorZero.z();

        for (int index = 1; index < mesh.vertexCount(); index++) {
            Vector3 vector = mesh.vertex(index);

            minX = Math.min(minX, vector.x());
            minY = Math.min(minY, vector.y());
            minZ = Math.min(minZ, vector.z());

            maxX = Math.max(maxX, vector.x());
            maxY = Math.max(maxY, vector.y());
            maxZ = Math.max(maxZ, vector.z());
        }

        return new MeshBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }
}