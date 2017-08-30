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
import java.util.Random;

public class Puck {

    public class Vertex {
        float tu, tv;
        float nx, ny, nz;
        float vx, vy, vz;
        float r, g, b, a;

    }
    Vertex g_pSphereVertices[];

    Random randomGenerator;

    /* This is the direction: positive Y = up, positive X = right */
    int puckYDirection;
    int puckXDirection;

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

        int max = 1;
        int min = 0;
        randomGenerator = new Random();
        int randomNum = randomGenerator.nextInt((max - min) + 1) + min;
        if (randomNum > 0)
            puckYDirection = 1;
        else
            puckYDirection = -1;
        randomNum = randomGenerator.nextInt((max - min) + 1) + min;
        if (randomNum > 0)
            puckXDirection = 1;
        else
            puckXDirection = -1;

        createSphereGeometry(0.0f, 0.0f, 0.0f, 1.5f, 12);

        // Initialize the buffers.
        mCubePositions = ByteBuffer
                .allocateDirect(g_nNumSphereVertices * 3 * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < g_nNumSphereVertices; i++) {
            mCubePositions.put(g_pSphereVertices[i].vx);
            mCubePositions.put(g_pSphereVertices[i].vy);
            mCubePositions.put(g_pSphereVertices[i].vz);
        }

        mCubeColors = ByteBuffer
                .allocateDirect(g_nNumSphereVertices * 4 * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < g_nNumSphereVertices; i++) {
            mCubeColors.put(g_pSphereVertices[i].r);
            mCubeColors.put(g_pSphereVertices[i].g);
            mCubeColors.put(g_pSphereVertices[i].b);
            mCubeColors.put(g_pSphereVertices[i].a);
        }

        mCubeNormals = ByteBuffer
                .allocateDirect(g_nNumSphereVertices * 3 * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < g_nNumSphereVertices; i++) {
            mCubeNormals.put(g_pSphereVertices[i].nx);
            mCubeNormals.put(g_pSphereVertices[i].ny);
            mCubeNormals.put(g_pSphereVertices[i].nz);
        }
        mCubeTextureCoordinates = ByteBuffer
                .allocateDirect(g_nNumSphereVertices * 2 * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < g_nNumSphereVertices; i++) {
            mCubeTextureCoordinates.put(g_pSphereVertices[i].tu);
            mCubeTextureCoordinates.put(g_pSphereVertices[i].tv);
        }
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

    //-----------------------------------------------------------------------------
    // Name: createSphereGeometry()
    // Desc: Creates a sphere as an array of vertex data suitable to be fed into a
    //       OpenGL vertex array. The sphere will be centered at cy, cx, cz with
    //       radius r, and precision p. Based on a function Written by Paul Bourke.
    //       http://astronomy.swin.edu.au/~pbourke/opengl/sphere/
    //-----------------------------------------------------------------------------
    void createSphereGeometry( float cx, float cy, float cz, float r, int p )
    {
        final float PI = 3.14159265358979f;
        final float TWOPI = 6.28318530717958f;
        final float PIDIV2 = 1.57079632679489f;

        float theta1 = 0.0f;
        float theta2 = 0.0f;
        float theta3 = 0.0f;

        float ex = 0.0f;
        float ey = 0.0f;
        float ez = 0.0f;

        float px = 0.0f;
        float py = 0.0f;
        float pz = 0.0f;

        float tu  = 0.0f;
        float tv = 0.0f;

        g_nNumSphereVertices = (p/2) * ((p+1)*2);

        cubePositionData = new float[g_nNumSphereVertices*3+1];
        cubeNormalData = new float[g_nNumSphereVertices*3+1];
        cubeColorData = new float[g_nNumSphereVertices*4+1];
        cubeTextureCoordinateData = new float[g_nNumSphereVertices*/*3*/2+1];

        //-------------------------------------------------------------------------
        // If sphere precision is set to 4, then 20 verts will be needed to
        // hold the array of GL_TRIANGLE_STRIP(s) and so on...
        //
        // Example:
        //
        // total_verts = (p/2) * ((p+1)*2)
        // total_verts = (4/2) * (  5  *2)
        // total_verts =   2   *  10
        // total_verts =      20
        //-------------------------------------------------------------------------

        g_pSphereVertices = new Vertex[g_nNumSphereVertices + 1];

        for(int i = 0; i < g_pSphereVertices.length ; i++)
        {
            g_pSphereVertices[i] = new Vertex();
        }

        // Disallow a negative number for radius.
        if( r < 0 )
            r = -r;

        // Disallow a negative number for precision.
        if( p < 4 )
            p = 4;

        int k = -1;

        for( int i = 0; i < p/2; ++i )
        {
            theta1 = i * TWOPI / p - PIDIV2;
            theta2 = (i + 1) * TWOPI / p - PIDIV2;

            for( int j = 0; j <= p; ++j )
            {
                theta3 = j * TWOPI / p;

                ex = (float)(Math.cos(theta2) * Math.cos(theta3));
                ey = (float)Math.sin(theta2);
                ez = (float)(Math.cos(theta2) * Math.sin(theta3));
                px = cx + r * ex;
                py = cy + r * ey;
                pz = cz + r * ez;
                tu  = -(j/(float)p);
                tv  = 2*(i+1)/(float)p;

                ++k;
                setVertData( k, tu, tv, ex, ey, ez, px, py, pz, 1.0f, 1.0f, 1.0f, 1.0f );

                ex = (float)(Math.cos(theta1) * Math.cos(theta3));
                ey = (float)Math.sin(theta1);
                ez = (float)(Math.cos(theta1) * Math.sin(theta3));
                px = cx + r * ex;
                py = cy + r * ey;
                pz = cz + r * ez;
                tu  = -(j/(float)p);
                tv  = 2*i/(float)p;

                ++k;
                setVertData( k, tu, tv, ex, ey, ez, px, py, pz, 1.0f, 1.0f, 1.0f, 1.0f );
            }
        }
    }

    void setVertData( int index,
                      float tu, float tv,
                      float nx, float ny, float nz,
                      float vx, float vy, float vz,
                      float cr, float cg, float cb, float ca)
    {
        g_pSphereVertices[index].tu = tu;
        g_pSphereVertices[index].tv = tv;
        g_pSphereVertices[index].nx = nx;
        g_pSphereVertices[index].ny = ny;
        g_pSphereVertices[index].nz = nz;
        g_pSphereVertices[index].vx = vx;
        g_pSphereVertices[index].vy = vy;
        g_pSphereVertices[index].vz = vz;
        g_pSphereVertices[index].r = cr;
        g_pSphereVertices[index].g = cg;
        g_pSphereVertices[index].b = cb;
        g_pSphereVertices[index].a = ca;
    }
}
