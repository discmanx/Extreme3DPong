/************
 * Created by: Ryan Baldwin
 *
 * Special thanks to Stuart J Moore, and http://www.codesampler.com/oglsrc/oglsrc_9.htm for sphere code
 * https://gist.github.com/stuartjmoore/1076642
 */

package com.weakentroll.extreme3dpong;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Puck {

    /* This is the direction: positive Y = up, positive X = right */
    int puckYDirection = 1;
    int puckXDirection = 1;

    /* Speed of puck horizontally - 0.5 - 1.0 depending on if its center hits center of paddle in range 0.25 to 0.75 width */
    float puckSpeed = 0.0f;
    float posX, posY, posZ;
    float worldBoxTopMinX = 0.0f, worldBoxTopMaxX = 0.0f,
            worldBoxTopMinY = 0.0f, worldBoxTopMaxY = 0.0f,
            worldBoxTopMinZ = 0.0f, worldBoxTopMaxZ = 0.0f;

    int g_nNumSphereVertices;

    float[] cubePositionData;
    float[] cubeColorData;
    float[] cubeNormalData;
    float[] cubeTextureCoordinateData;

    /**
     * Store our model data in a float buffer.
     */
    final FloatBuffer mCubePositions;
    final FloatBuffer mCubeColors;
    final FloatBuffer mCubeNormals;
    final FloatBuffer mCubeTextureCoordinates;

    /**
     * How many bytes per float.
     */
    final int mBytesPerFloat = 4;

    /**
     * Size of the position data in elements.
     */
    final int mPositionDataSize = 3;

    /**
     * Size of the color data in elements.
     */
    final int mColorDataSize = 4;

    /**
     * Size of the normal data in elements.
     */
    final int mNormalDataSize = 3;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;

    /**
     * This will be used to pass in model normal information.
     */
    private int mNormalHandle;

    /**
     * This is a handle to our cube shading program.
     */
    private int mProgramHandle;

    /**
     * This is a handle to our light point program.
     */
    private int mPointProgramHandle;

    /**
     * This is a handle to our texture data.
     */
    private int mTextureDataHandle;

    /**
     * Size of the texture coordinate data in elements.
     */
    final int mTextureCoordinateDataSize = 2;

    public Puck() {
        posX = 0.0f;
        posY = 0.0f;
        posZ = 0.0f;

        initializeSphere(0.0f, 0.0f, 0.0f, 10.5f, 100);

        // Initialize the buffers.
        mCubePositions = ByteBuffer
                .allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer
                .allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mCubeNormals = ByteBuffer
                .allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeTextureCoordinates = ByteBuffer
                .allocateDirect(
                        cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

    }

    public int getmPositionHandle() {
        return mPositionHandle;
    }

    public int getmNormalHandle() {
        return mNormalHandle;
    }

    public int getmPointProgramHandle() {
        return mPointProgramHandle;
    }

    public void initializeSphere(float cx, float cy, float cz, float r, int p) {
        float PI = (float) Math.PI;
        float theta1 = 0.0f, theta2 = 0.0f, theta3 = 0.0f;
        float ex = 0.0f, ey = 0.0f, ez = 0.0f;
        float px = 0.0f, py = 0.0f, pz = 0.0f;
        float[] vertices = new float[p * 6 + 6], normals = new float[p * 6 + 6], colours = new float[p * 8 + 8], texCoords = new float[p * 4 + 4];

        float test_pi_divided_by_2 = PI / 2;

        if (r < 0)
            r = -r;

        if (p < 0)
            p = -p;

        for (int i = 0; i < p / 2; ++i) {
            theta1 = i * (PI * 2) / p - (PI / 2);
            theta2 = (i + 1) * (PI * 2) / p - (PI / 2);

            for (int j = 0; j <= p; ++j) {
                theta3 = j * (PI * 2) / p;

                ex = (float) (Math.cos(theta2) * Math.cos(theta3));
                ey = (float) (Math.sin(theta2));
                ez = (float) (Math.cos(theta2) * Math.sin(theta3));
                px = cx + r * ex;
                py = cy + r * ey;
                pz = cz + r * ez;

                vertices[(6 * j) + (0 % 6)] = px;
                vertices[(6 * j) + (1 % 6)] = py;
                vertices[(6 * j) + (2 % 6)] = pz;

                colours[(8 * j) + (0 % 8)] = 1.0f;
                colours[(8 * j) + (1 % 8)] = 1.0f;
                colours[(8 * j) + (2 % 8)] = 1.0f;
                colours[(8 * j) + (3 % 8)] = 1.0f;

                normals[(6 * j) + (0 % 6)] = ex;
                normals[(6 * j) + (1 % 6)] = ey;
                normals[(6 * j) + (2 % 6)] = ez;

                texCoords[(4 * j) + (0 % 4)] = -(j / (float) p);
                texCoords[(4 * j) + (1 % 4)] = 2 * (i + 1) / (float) p;


                ex = (float) (Math.cos(theta1) * Math.cos(theta3));
                ey = (float) (Math.sin(theta1));
                ez = (float) (Math.cos(theta1) * Math.sin(theta3));
                px = cx + r * ex;
                py = cy + r * ey;
                pz = cz + r * ez;

                vertices[(6 * j) + (3 % 6)] = px;
                vertices[(6 * j) + (4 % 6)] = py;
                vertices[(6 * j) + (5 % 6)] = pz;

                colours[(8 * j) + (4 % 8)] = 1.0f;
                colours[(8 * j) + (5 % 8)] = 1.0f;
                colours[(8 * j) + (6 % 8)] = 1.0f;
                colours[(8 * j) + (7 % 8)] = 1.0f;

                normals[(6 * j) + (3 % 6)] = ex;
                normals[(6 * j) + (4 % 6)] = ey;
                normals[(6 * j) + (5 % 6)] = ez;

                texCoords[(4 * j) + (2 % 4)] = -(j / (float) p);
                texCoords[(4 * j) + (3 % 4)] = 2 * i / (float) p;
            }
        }

        cubePositionData = vertices;
        cubeColorData = colours;
        cubeNormalData = normals;
        cubeTextureCoordinateData = texCoords;
    }

}