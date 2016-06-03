package com.weakentroll.extreme3dpong;


public class SkeletalShapeBuilder
{
	public float[] generateCubeData(float[] point1,
			float[] point2,
			float[] point3,
			float[] point4,
			float[] point5,
			float[] point6,
			float[] point7,
			float[] point8,
			int elementsPerPoint) // 3 elements?!
	{
		// Given a cube with the points defined as follows:
		// front left top, front right top, front left bottom, front right bottom,
		// back left top, back right top, back left bottom, back right bottom,		
		// return an array of 6 sides, /*2*/8 triangles per side, 3 vertices per triangle, and 4 floats per vertex.
		final int FRONT = 0;
		final int RIGHT = 1;
		final int BACK = 2;
		final int LEFT = 3;
		final int TOP = 4;
		final int BOTTOM = 5;

        final int size = elementsPerPoint * 6 * 6;
        final float[] cubeData = new float[size];

        for (int face = 0; face < 6; face ++)
        {
            // Relative to the side, p1 = top left, p2 = top right, p3 = bottom left, p4 = bottom right
            final float[] p1, p2, p3, p4;

            // Select the points for this face
            if (face == FRONT)
            {
                p1 = point1; p2 = point2; p3 = point3; p4 = point4;
            }
            else if (face == RIGHT)
            {
                p1 = point2; p2 = point6; p3 = point4; p4 = point8;
            }
            else if (face == BACK)
            {
                p1 = point6; p2 = point5; p3 = point8; p4 = point7;
            }
            else if (face == LEFT)
            {
                p1 = point5; p2 = point1; p3 = point7; p4 = point3;
            }
            else if (face == TOP)
            {
                p1 = point5; p2 = point6; p3 = point1; p4 = point2;
            }
            else // if (side == BOTTOM)
            {
                p1 = point8; p2 = point7; p3 = point4; p4 = point3;
            }

            // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.

            // Build the triangles
            //  1---3,6
            //  | / |
            // 2,4--5
            int offset = face * elementsPerPoint * 6;

            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p1[i]; }
            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p3[i]; }
            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p2[i]; }
            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p3[i]; }
            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p4[i]; }
            for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p2[i]; }
        }

        return cubeData;
    }

    // frontFaceRed, rightFaceGreen, backFaceBlue, LeftFaceYellow ,
    // topFaceCyan, bottomFaceMagenta
    public float[] generateCubeColors(float[] faceColor1,
                                    float[] faceColor2,
                                    float[] faceColor3,
                                    float[] faceColor4,
                                    float[] faceColor5,
                                    float[] faceColor6,
                                    int elementsPerPoint) // 4 elements?!
    {

        final int FRONT = 0;
        final int RIGHT = 1;
        final int BACK = 2;
        final int LEFT = 3;
        final int TOP = 4;
        final int BOTTOM = 5;

        final int size = elementsPerPoint * 6*/*24*/ 6;
        final float[] cubeData = new float[size];

        for (int face = 0; face < 6; face++)
        {
            int offset = face * elementsPerPoint * 6 ;//* 4;

            // Select the points for this face
            if (face == FRONT) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor1[i]; }
                //}
            }

            if (face == RIGHT) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor2[i]; }
                //}
            }

            if (face == BACK) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor3[i]; }
                //}
            }

            if (face == LEFT) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor4[i]; }
                //}
            }

            if (face == TOP) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor5[i]; }
                //}
            }

            if (face == BOTTOM) {
                //for (int q = 0; q < 4; q++) {
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }

                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }
                    for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = faceColor6[i]; }
                //}
            }
        }
        return cubeData;
    }

    public float[] generateCubeSurfaceNormals(float[] cubeData /*float[] point1,
                                    float[] point2,
                                    float[] point3,
                                    float[] point4,
                                    float[] point5,
                                    float[] point6,
                                    float[] point7,
                                    float[] point8,
                                    int elementsPerPoint */) // 3 elements?!
    {
        float[] cubeSurfaceNormals = new float[cubeData.length];

        for (int i = 0; i < cubeData.length; i++) {
            if ( ((i % 9) == 0) && (i > 0) ) {

               float[] cubeSurfaceNormal;

                float[] cubePositionDataPoint1 = new float[3];
                float[] cubePositionDataPoint2 = new float[3];
                float[] cubePositionDataPoint3 = new float[3];
                cubePositionDataPoint1[0] = cubeData[i - 9];
                cubePositionDataPoint1[1] = cubeData[i - 8];
                cubePositionDataPoint1[2] = cubeData[i - 7];
                cubePositionDataPoint2[0] = cubeData[i - 6];
                cubePositionDataPoint2[1] = cubeData[i - 5];
                cubePositionDataPoint2[2] = cubeData[i - 4];
                cubePositionDataPoint3[0] = cubeData[i - 3];
                cubePositionDataPoint3[1] = cubeData[i - 2];
                cubePositionDataPoint3[2] = cubeData[i - 1];

                cubeSurfaceNormal = calcNormal(cubePositionDataPoint1, cubePositionDataPoint2, cubePositionDataPoint3);

                //cubeSurfaceNormals[i ] = cubeSurfaceNormal[0];

                //for (int j = 1; j < 9; j++) {
                //    if ( (j % 3) == 0) {
                cubeSurfaceNormals[i - 9] = cubeSurfaceNormal[0];
                cubeSurfaceNormals[i - 8] = cubeSurfaceNormal[1];
                cubeSurfaceNormals[i - 7] = cubeSurfaceNormal[2];
                cubeSurfaceNormals[i - 6] = cubeSurfaceNormal[0];
                cubeSurfaceNormals[i - 5] = cubeSurfaceNormal[1];
                cubeSurfaceNormals[i - 4] = cubeSurfaceNormal[2];
                cubeSurfaceNormals[i - 3] = cubeSurfaceNormal[0];
                cubeSurfaceNormals[i - 2] = cubeSurfaceNormal[1];
                cubeSurfaceNormals[i - 1] = cubeSurfaceNormal[2];
                   // }
                //}
            }
        }
        return cubeSurfaceNormals;
    }

    float[] calcNormal( float[] p1, float[] p2, float[] p3 )
    {
        // TODO: Operations in 2D and 3D computer graphics are often performed using copies of vectors that have been normalized ie. converted to unit vectors. For example, the tutorial "RSL: Edge Effects" applies normalization before calculating the dot product of two vectors. Normalizing a vector involves two steps:
        //1   calculate its length, then,
        //    2   divide each of its (xy or xyz) components by its length.
        float[] U = new float[3];
        U[0] = (p2[0] - p1[0]);
        U[1] = (p2[1] - p1[1]);
        U[2] = (p2[2] - p1[2]);

        float[] V = new float[3];
        V[0] = (p3[0] - p1[0]);
        V[1] = (p3[1] - p1[1]);
        V[2] = (p3[2] - p1[2]);

        float[] surfaceNormal = new float[3];
        surfaceNormal[0] = (U[1]*V[2]) - (U[2]*V[1]);
        surfaceNormal[1] = (U[2]*V[0]) - (U[0]*V[2]);
        surfaceNormal[2] = (U[0]*V[1]) - (U[1]*V[0]);

        // Dont forget to normalize if needed
        return surfaceNormal;
    }

    float[] generateCubeTextureCoordinates( float[] cubeFaceTextureCoords )
    {
        float[] cubeTextureCoordinates = new float[cubeFaceTextureCoords.length * 6 * 4];

        for (int i = 0; i < (cubeFaceTextureCoords.length*6*4)-1; i++) {
            cubeTextureCoordinates[i] = cubeFaceTextureCoords[i%12];
        }
        return cubeTextureCoordinates;
    }
}
