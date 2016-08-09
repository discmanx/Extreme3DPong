 /*
 * Created by: Ryan Baldwin
 */

package com.weakentroll.extreme3dpong;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Crosshair {
	float posX, posY, posZ;
	float worldBoxTopMinX = 0.0f, worldBoxTopMaxX = 0.0f,
			worldBoxTopMinY = 0.0f, worldBoxTopMaxY = 0.0f,
			worldBoxTopMinZ = 0.0f, worldBoxTopMaxZ = 0.0f;
	
	//float[] puckCubePos = new float[4];
	//float[] puckWorldCubePos = new float[4];
	//float[] mTempMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	final FloatBuffer mCubePositions;
	final FloatBuffer mCubeColors;
	final FloatBuffer mCubeNormals;
	final FloatBuffer mCubeTextureCoordinates;

	/** This will be used to pass in model texture coordinate information. */
	int mTextureCoordinateHandle;

	/** How many bytes per float. */
	final int mBytesPerFloat = 4;

	/** Size of the position data in elements. */
	final int mPositionDataSize = 3;

	/** Size of the color data in elements. */
	final int mColorDataSize = 4;

	/** Size of the normal data in elements. */
	final int mNormalDataSize = 3;
	
	/** This will be used to pass in model position information. */
	private int mPositionHandle;

	/** This will be used to pass in model color information. */
	private int mColorHandle;
	
	/** This will be used to pass in model normal information. */
	private int mNormalHandle;
	
	/** This is a handle to our cube shading program. */
	private int mProgramHandle;

	/** This is a handle to our light point program. */
	private int mPointProgramHandle;

	/** This is a handle to our texture data. */
	private int mTextureDataHandle;

	/** Size of the texture coordinate data in elements. */
	final int mTextureCoordinateDataSize = 2;

	public Crosshair() {
		posX = 0.0f;
		posY = 0.0f;
		posZ = 0.0f;

		// X, Y, Z
		final float[] cubePositionData = {
				// In OpenGL counter-clockwise winding is default. This means
				// that when we look at a triangle,
				// if the points are counter-clockwise we are looking at the
				// "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing
				// triangles are culled, since they
				// usually represent the backside of an object and aren't
				// visible anyways.

				// Front face
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f,
				1.0f,
				1.0f,
				1.0f,
				1.0f, };

		// R, G, B, A
		final float[] cubeColorData = {
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 1.0f, 0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f, };

		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.
		final float[] cubeNormalData = {
				// Front face
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f, };

		// S, T (or X, Y)
		// Texture coordinate data.
		// Because images have a Y axis pointing downward (values increase as
		// you move down the image) while
		// OpenGL has a Y axis pointing upward, we adjust for that here by
		// flipping the Y axis.
		// What's more is that the texture coordinates are the same for every
		// face.
		final float[] cubeTextureCoordinateData = {
				// Front face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				1.0f,
				1.0f,
				1.0f,
				0.0f, };

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

	public void setmPositionHandle(int mPositionHandle) {
		this.mPositionHandle = mPositionHandle;
	}
	
	public int getmColorHandle() {
		return mColorHandle;
	}

	public void setmColorHandle(int mColorHandle) {
		this.mColorHandle = mColorHandle;
	}
	
	public int getmNormalHandle() {
		return mNormalHandle;
	}

	public void setmNormalHandle(int mNormalHandle) {
		this.mNormalHandle = mNormalHandle;
	}
	
	public int getmProgramHandle() {
		return mProgramHandle;
	}

	public void setmProgramHandle(int mProgramHandle) {
		this.mProgramHandle = mProgramHandle;
	}

	public int getmPointProgramHandle() {
		return mPointProgramHandle;
	}

	public void setmPointProgramHandle(int mPointProgramHandle) {
		this.mPointProgramHandle = mPointProgramHandle;
	}

	public int getmTextureDataHandle() {
		return mTextureDataHandle;
	}

	public void setmTextureDataHandle(int mTextureDataHandle) {
		this.mTextureDataHandle = mTextureDataHandle;
	}
}
