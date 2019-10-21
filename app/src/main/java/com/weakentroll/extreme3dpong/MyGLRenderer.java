/***********************************
 * Description: Based on the learnopengles.net examples, the cube positions, colour, normal and texture fields
 * are manipulated to test collision detection, screen touch handling and 3D space manipulation
 *
 * Created by: Ryan Baldwin
 * github: www.github.com/discmanx/
 * website: http://www.weakentroll.com
 */

package com.weakentroll.extreme3dpong;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.weakentroll.extreme3dpong.GLText.GLText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// TODO: add 3 bars to pong life
//
/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 *
 * Created by: Ryan Baldwin
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
	/** Used for debug logs. */
	private static final String TAG = "ExtremePongRenderer";
	
	public enum ShapeTypes { puck, opponent, player, crosshair, skullnbones }
	ShapeTypes shapeTypes; // TODO: not needed yet

    ByteBuffer type;

    private final int frontFaceCoordsSize = 18; /*front face coords */

    // First we need to create the state machine.
    // Note that I'm using the abstract IStateMachine instead of a concrete class.
    public IStateMachine gameMachine = new GameMachine();

    // TODO: Add Splash Screen!

	Puck thePuck;
    Crosshair crosshair;
    Player player, opponent;
    float [] opponentLocation = { -5.0f, 5.0f, 20.0f };
    BottomScrollBar bottomScrollBar;
	FPSCounter fpsCounter = new FPSCounter();
    private GLText glText;                             // A GLText Instance

    int negativezcount = 0;
    int positivezcount = 0;
    long startTime, endTime, onDrawFrametime;

    private int opponentScore = 0;
    private int playerScore = 0;

    private float fieldWidth, fieldHeight, fW, fH, fWidthFar, fHeightFar;

    private float[] XOffset = new float[12];
    private float[] YOffset = new float[12];

    private float[] XOffsetIncr = new float[12];
    private float[] YOffsetIncr = new float[12];

    private double phi = 0.0f;//-(2*Math.PI);
    private double theta = 0.0f;//-(Math.PI);

    // Position the eye in front of the origin.
    private float eyeX = 0.0f;
    private float eyeY = 0.0f;
    private float eyeZ = 0.0f;

    // We are looking toward the distance
    private float lookX = 0.0f;
    private float lookY = 0.0f;
    private float lookZ = -5.0f;

    // Set our up vector. This is where our head would be pointing were we
    // holding the camera.
    private float upX = 0.0f;
    private float upY = 1.0f;
    private float upZ = 0.0f;

    SkeletalShapeBuilder skeletalShapeBuilder = new SkeletalShapeBuilder();;

    // Formatting the string representation value, not the actual numerical precision
    DecimalFormat form = new DecimalFormat("0.0000000");

	private int mWidth;
    private int mHeight;
    private float rawTouchY;
    public float touchedX, touchedY, touchedZ;
    float[] outputCoordObj = new float[4];
    
    private float worldPlayerMinX = 0.0f, worldPlayerMaxX = 0.0f, worldPlayerMinY = 0.0f, worldPlayerMaxY = -1.0f, worldPlayerMinZ = 0.0f , worldPlayerMaxZ = 0.0f;
    private float worldPuckMinX = 0.0f, worldPuckMaxX = 0.0f, worldPuckMinY = 1.0f, worldPuckMaxY = -1.0f, worldPuckMinZ = 0.0f , worldPuckMaxZ = 0.0f;
    private float worldPuckBottomMinX = 0.0f, worldPuckBottomMaxX = 0.0f, worldPuckBottomMinY = 0.0f, worldPuckBottomMaxY = 0.0f, worldPuckBottomMinZ = 0.0f , worldPuckBottomMaxZ = 0.0f;
    private float worldOpponentMinX = 0.0f, worldOpponentMaxX = 0.0f, worldOpponentMinY = 1.0f, worldOpponentMaxY = 0.0f, worldOpponentMinZ = 0.0f , worldOpponentMaxZ = 0.0f;
    private float worldBottomScrollBarMinX = 0.0f, worldBottomScrollBarMaxX = 0.0f, worldBottomScrollBarMinY = 1.0f, worldBottomScrollBarMaxY = -10.0f, worldBottomScrollBarMinZ = 0.0f , worldBottomScrollBarMaxZ = 0.0f;
    private float worldBottomScrollGreenHeight;
    private Context mActivityContext;

	/**
	 * Store the model matrix. This matrix is used to move models from object
	 * space (where each model can be thought of being located at the center of
	 * the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix
	 * transforms world space to eye space; it positions things relative to our
	 * eye.
	 */
	private float[] mViewMatrix = new float[16];

	/**
	 * Store the projection matrix. This is used to project the scene onto a 2D
	 * viewport.
	 */
	private float[] mProjectionMatrix = new float[16];

	/**
	 * Allocate storage for the final combined matrix. This will be passed into
	 * the shader program.
	 */
	private float[] mMVPMatrix = new float[16];

	/**
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;
	private final FloatBuffer mCubeTextureCoordinates;

	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;

	/** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;

	/** This will be used to pass in the light position. */
	private int mLightPosHandle;

	/** This will be used to pass in the texture. */
	private int mTextureUniformHandle;

	/** This will be used to pass in model position information. */
	private int mPositionHandle;

	/** This will be used to pass in model color information. */
	private int mColorHandle;

	/** This will be used to pass in model normal information. */
	private int mNormalHandle;

	/** This will be used to pass in model texture coordinate information. */
	private int mTextureCoordinateHandle;

	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;

	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;

	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;

	/** Size of the normal data in elements. */
	private final int mNormalDataSize = 3;

	/** Size of the texture coordinate data in elements. */
	private final int mTextureCoordinateDataSize = 2;

	/**
	 * Used to hold a light centered on the origin in model space. We need a 4th
	 * coordinate so we can get translations to work when we multiply this by
	 * our transformation matrices.
	 */
	private final float[] mLightPosInModelSpace = new float[] { 0.0f, 0.0f,
			0.0f, 1.0f };

	/**
	 * Used to hold the current position of the light in world space (after
	 * transformation via model matrix).
	 */
	private final float[] mLightPosInWorldSpace = new float[4];

	/**
	 * Used to hold the transformed position of the light in eye space (after
	 * transformation via modelview matrix)
	 */
	private final float[] mLightPosInEyeSpace = new float[4];

	/** This is a handle to our cube shading program. */
	private int mProgramHandle;

	/** This is a handle to our light point program. */
	private int mPointProgramHandle;

	/** This is a handle to our texture data. */
	private int mTextureDataHandle[] = new int[4];

    private float[] mRotationMatrix = new float[16];

    private float mAngle;

    /**
	 * Initialize the model data.
	 */
	public MyGLRenderer(Context activityContext) {
		mActivityContext = activityContext;

		touchedX = 0.0f;
		touchedY = 0.0f;
		touchedZ = 0.0f;

		thePuck = new Puck();
        crosshair = new Crosshair();
        bottomScrollBar = new BottomScrollBar();
		
		// X, Y, Z
		/*final float[] cubePositionData = {
				// In OpenGL counter-clockwise winding is default. This means
				// that when we look at a triangle,
				// if the points are counter-clockwise we are looking at the
				// "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing
				// triangles are culled, since they
				// usually represent the backside of an object and aren't
				// visible anyways.

				// Front face
				-1.0f, 1.0f, 1.0f,   -1.0f, -1.0f, 1.0f,   1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,   1.0f, -1.0f, 1.0f,   1.0f, 1.0f, 1.0f,

				// Right face
				1.0f, 1.0f, 1.0f,    1.0f, -1.0f, 1.0f,    1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,   1.0f, -1.0f, -1.0f,   1.0f, 1.0f, -1.0f,

				// Back face
				1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
				1.0f,
				-1.0f,

				// Left face
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
				1.0f,

				// Top face
				-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

				// Bottom face
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, };
*/
        float[] frontTopLeft = { -1.0f, 1.0f, 1.0f } ;
        float[] frontTopRight = { 1.0f, 1.0f, 1.0f } ;
        float[] frontBottomLeft = { -1.0f, -1.0f, 1.0f } ;
        float[] frontBottomRight = { 1.0f, -1.0f, 1.0f } ;
        float[] backTopLeft = { -1.0f, 1.0f, -1.0f } ;
        float[] backTopRight = { 1.0f, 1.0f, -1.0f } ;
        float[] backBottomLeft = { -1.0f, -1.0f, -1.0f } ;
        float[] backBottomRight = { 1.0f, -1.0f, -1.0f } ;
        float[] cubePositionData = skeletalShapeBuilder.generateCubeData(frontTopLeft, frontTopRight, frontBottomLeft, frontBottomRight ,
                                                                        backTopLeft, backTopRight,backBottomLeft , backBottomRight, 3);
        /* R, G, B, A
		final float[] cubeColorData = {
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f,     1.0f, 0.0f, 0.0f, 1.0f,     1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,     1.0f, 0.0f, 0.0f, 1.0f,     1.0f, 0.0f, 0.0f, 1.0f,

				// Right face (green)
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,

				// Back face (blue)
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,

				// Left face (yellow)
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f,
				1.0f,
				0.0f,
				1.0f,

				// Top face (cyan)
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				1.0f,

				// Bottom face (magenta)
				1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f };
*/
        float[] frontFaceRed = { 1.0f, 0.0f, 0.0f, 1.0f } ;
        float[] rightFaceGreen = { 0.0f, 1.0f, 0.0f, 1.0f } ;
        float[] backFaceBlue = { 0.0f, 0.0f, 1.0f, 1.0f } ;
        float[] LeftFaceYellow = { 1.0f, 1.0f, 0.0f, 1.0f } ;
        float[] topFaceCyan = { 0.0f, 1.0f, 1.0f, 1.0f } ;
        float[] bottomFaceMagenta = { 1.0f, 0.0f, 1.0f, 1.0f } ;

        float[] cubeColorData = skeletalShapeBuilder.generateCubeColors(frontFaceRed, rightFaceGreen, backFaceBlue, LeftFaceYellow ,
                topFaceCyan, bottomFaceMagenta, 4);

		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.
        /*
        // Front face - cube position data reference only
				-1.0f, 1.0f, 1.0f,   -1.0f, -1.0f, 1.0f,   1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,   1.0f, -1.0f, 1.0f,   1.0f, 1.0f, 1.0f,

				// Right face
				1.0f, 1.0f, 1.0f,    1.0f, -1.0f, 1.0f,    1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,   1.0f, -1.0f, -1.0f,   1.0f, 1.0f, -1.0f,

         */ /*
		final float[] cubeNormalData = {
				// Front face
				0.0f, 0.0f, 1.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f, 1.0f,

				// Right face
				1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f,

				// Back face
				0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,

				// Left face
				-1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,

				// Top face
				0.0f, 1.0f, 0.0f,   0.0f, 1.0f, 0.0f,   0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,   0.0f, 1.0f, 0.0f,   0.0f, 1.0f, 0.0f,

				// Bottom face
				0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f };
*/
        float[] cubeNormalData = skeletalShapeBuilder.generateCubeSurfaceNormals(cubePositionData /*frontTopLeft, frontTopRight, frontBottomLeft, frontBottomRight ,
                                                                          backTopLeft, backTopRight,backBottomLeft , backBottomRight, 3*/);
                // S, T (or X, Y)
		// Texture coordinate data.
		// Because images have a Y axis pointing downward (values increase as
		// you move down the image) while
		// OpenGL has a Y axis pointing upward, we adjust for that here by
		// flipping the Y axis.
		// What's more is that the texture coordinates are the same for every
		// face.

		/* final float[] cubeTextureCoordinateData = {
				// Front face
				0.0f, 0.0f,     0.0f, 1.0f,     1.0f, 0.0f,
                0.0f, 1.0f,     1.0f, 1.0f,     1.0f, 0.0f,

				// Right face
				0.0f, 0.0f,     0.0f, 1.0f,     1.0f, 0.0f,
                0.0f, 1.0f,     1.0f, 1.0f,     1.0f, 0.0f,

				// Back face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
				1.0f,
				1.0f,
				0.0f,

				// Left face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f,
				0.0f,

				// Top face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f,

				// Bottom face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f }; */
        float[] cubeFaceTextureCoords = {
                0.0f, 0.0f,     0.0f, 1.0f,     1.0f, 0.0f,
                0.0f, 1.0f,     1.0f, 1.0f,     1.0f, 0.0f, };

        final float[] cubeTextureCoordinateData = skeletalShapeBuilder.generateCubeTextureCoordinates(cubeFaceTextureCoords);

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

	protected String getVertexShader() {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext,
				R.raw.per_pixel_vertex_shader);
	}

	protected String getFragmentShader() {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext,
				R.raw.per_pixel_fragment_shader);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to white.
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        // Create the GLText
        glText = new GLText(mActivityContext.getAssets());

        // Load the font from file (set size + padding), creates the texture
        // NOTE: after a successful call to this the font is ready for rendering!
        glText.load( "Roboto-Regular.ttf", 10, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

        Random randomGenerator = new Random();

        for (int i = 0; i < 36; i++) {
            if ((i > 0) && ((i % 3) == 0)) {

                // nextInt is normally exclusive of the top value,
                // so add 1 to make it inclusive
                int max = 50;
                int min = -50;
                int randomNum = randomGenerator.nextInt((max - min) + 1) + min;
                //int randomInt = randomGenerator.nextInt(100);

                float randVel1X = randomNum / 10000.0f;
                randomNum = randomGenerator.nextInt((max - min) + 1) + min;
                float randVel1Y = randomNum / 10000.0f;

                XOffsetIncr[((i / 3) - 1)] = randVel1X;
                YOffsetIncr[((i / 3) - 1)] = randVel1Y;
            }
        }

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		// Use culling to remove back faces.
		//////GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing
		///////GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		///Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
		///		lookZ, upX, upY, upZ);

        //Setting the view matrix
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f);

		final String vertexShader = getVertexShader();
		final String fragmentShader = getFragmentShader();

		final int vertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle,
				fragmentShaderHandle, new String[] { "a_Position", "a_Color",
						"a_Normal", "a_TexCoordinate" });

		// Define a simple shader program for our point.
		final String pointVertexShader = RawResourceReader
				.readTextFileFromRawResource(mActivityContext,
						R.raw.point_vertex_shader);
		final String pointFragmentShader = RawResourceReader
				.readTextFileFromRawResource(mActivityContext,
						R.raw.point_fragment_shader);

		final int pointVertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
		mPointProgramHandle = ShaderHelper.createAndLinkProgram(
                pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[]{"a_Position"});

		// Load the Player texture
	    mTextureDataHandle[0] = TextureHelper.loadTexture(mActivityContext,
				R.drawable.skullnbones, ShapeTypes.player, touchedX, touchedY, touchedZ, worldPlayerMinX, worldPlayerMaxX, worldPlayerMinY, worldPlayerMaxY, worldPlayerMinZ, worldPlayerMaxZ);

        // Load the Puck texture
        mTextureDataHandle[1] = TextureHelper.loadTexture(mActivityContext,
                R.drawable.mars, ShapeTypes.puck, touchedX, touchedY, touchedZ, worldPlayerMinX, worldPlayerMaxX, worldPlayerMinY, worldPlayerMaxY, worldPlayerMinZ, worldPlayerMaxZ);

        // Load the Opponent texture
        mTextureDataHandle[2] = TextureHelper.loadTexture(mActivityContext,
                R.drawable.skullnbones, ShapeTypes.opponent, touchedX, touchedY, touchedZ, worldPlayerMinX, worldPlayerMaxX, worldPlayerMinY, worldPlayerMaxY, worldPlayerMinZ, worldPlayerMaxZ);

        // Load the Crosshair texture
        mTextureDataHandle[3] = TextureHelper.loadTexture(mActivityContext,
                R.drawable.square_crosshair, ShapeTypes.crosshair, touchedX, touchedY, touchedZ, worldPlayerMinX, worldPlayerMaxX, worldPlayerMinY, worldPlayerMaxY, worldPlayerMinZ, worldPlayerMaxZ);

    }

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {

        /* GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // Take into account device orientation
        if (width > height) {
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        }
        else {
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1/ratio, 1/ratio, 1, 10);
        }

        // Save width and height
        this.mWidth = width;                             // Save Current Width
        this.mHeight = height;                           // Save Current Height

        int useForOrtho = Math.min(width, height);

        Matrix.orthoM(mViewMatrix, 0,
                -useForOrtho/2,
                useForOrtho/2,
                -useForOrtho/2,
                useForOrtho/2, 0.1f, 100f);
    } */

		// Set the OpenGL viewport to the same size as the surface.
		//GLES20.glViewport(0, 0, width, height);
		
		mWidth = width;
        mHeight = height;

		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 0.1f;
		final float far = 100.0f;
        float fovY = 60;
        float zNear = 1.0f;
        float zFar = 1000.0f;
        float aspect = (float) width / height;


        //double pi = 3.1415926535897932384626433832795;


        //fH = tan( (fovY / 2) / 180 * pi ) * zNear;
        fieldHeight = (float)Math.tan( fovY / 360 * Math.PI ) * zNear;
        fieldWidth = fieldHeight * aspect;

        //glFrustum( -fW, fW, -fH, fH, zNear, zFar );

		//Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.frustumM(mProjectionMatrix, 0, -fieldWidth, fieldWidth, -fieldHeight, fieldHeight, zNear, zFar);

/*
        float ratio = (float) width / height;
        float near = 1.0f;
        float far = 1000.0f;
        float fov = 60; // degrees, try also 45, or different number if you like
        float top = (float) (Math.tan(fov * Math.PI / 360.0f) * near);
        float bottom = -top;
        float left = ratio * bottom;
        float right = ratio * top;
        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, ratio, near, far); */
        //Matrix.perpectiveM(mProjectionMatrix, 0, 45.0f, ratio, near, far)
	}

    @Override
    public void onDrawFrame(GL10 glUnused) {

        startTime = System.nanoTime();

        worldPlayerMinX = 1.0f; worldPlayerMaxX = -1.0f; worldPlayerMinY = 1.0f; worldPlayerMaxY = -1.0f; worldPlayerMinZ = 0.0f ; worldPlayerMaxZ = 0.0f;
        worldOpponentMinX = 1.0f; worldOpponentMaxX = -1.0f; worldOpponentMinY = 1.0f; worldOpponentMaxY = -1.0f; worldOpponentMinZ = 0.0f ; worldOpponentMaxZ = 0.0f;
        worldPuckMinX = 1.0f; worldPuckMaxX = -1.0f; worldPuckMinY = 1.0f; worldPuckMaxY = -1.0f; worldPuckMinZ = 0.0f ; worldPuckMaxZ = 0.0f;

        // Redraw background color
        int clearMask = GLES20.GL_COLOR_BUFFER_BIT;
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(clearMask);

        //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        fpsCounter.logFrame();

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle,
                "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle,
                "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle,
                "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,
                "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[0]);

        // Tell the texture uniform sampler to use this texture in the shader by
        // binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Calculate position of the light. Rotate and then push into the
        // distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        //Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        //Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        //Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0,
                mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
                mLightPosInWorldSpace, 0);

        //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw the player cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, /*outputCoordObj[0]*/ touchedX /* (fWProjected/ (fW / touchedX) ) */, -5.0f, -20.0f);

        float[] newCubePos = new float[4];
        float[] newWorldCubePos = new float[4];
        float[] mTempMVPMatrix = new float[16];

        for (int i = 0; i < frontFaceCoordsSize; i++) {
            if (i == 0)
                continue;

            newCubePos[i%3] = mCubePositions.get(i);
            Float zFloat = new Float(newCubePos[i%3]);

            if ( ((i % 3) == 0) ) { // skip the cube vertice points if the z value is negative
                newCubePos[3] = 1.0f;
                newWorldCubePos[3] = 1.0f;

                /*if (zFloat.compareTo(1.0f) == 0)
                    positivezcount++;
                else if (zFloat.compareTo(-1.0f) == 0)
                    negativezcount++;*/

                Matrix.multiplyMM(mTempMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
                Matrix.multiplyMM(mTempMVPMatrix, 0, mProjectionMatrix, 0, mTempMVPMatrix, 0);

                Matrix.multiplyMV(newWorldCubePos, 0, mTempMVPMatrix, 0, newCubePos, 0);

                newWorldCubePos[0] = newWorldCubePos[0] / newWorldCubePos[3];
                newWorldCubePos[1] = newWorldCubePos[1] / newWorldCubePos[3];
                newWorldCubePos[2] = newWorldCubePos[2] / newWorldCubePos[3];

                if (newWorldCubePos[0] < worldPlayerMinX) {
                    worldPlayerMinX = newWorldCubePos[0];
                }
                if (newWorldCubePos[0] > worldPlayerMaxX) {
                    worldPlayerMaxX = newWorldCubePos[0];
                }
                if (newWorldCubePos[1] < worldPlayerMinY) {
                    worldPlayerMinY = newWorldCubePos[1];
                }
                if (newWorldCubePos[1] > worldPlayerMaxY) {
                    worldPlayerMaxY = newWorldCubePos[1];
                }
                if (newWorldCubePos[2] < worldPlayerMinZ) {
                    worldPlayerMinZ = newWorldCubePos[2];
                }
                if (newWorldCubePos[2] > worldPlayerMaxZ) {
                    worldPlayerMaxZ = newWorldCubePos[2];
                }
            }
            //mModelMatrix * originalCubeVertices = newCubeVertices;
        }

        drawCube();

        // Draw the puck
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, thePuck.posX, thePuck.posY, -20.0f);

        // Need to verify the texture gen array stays at 1 length?
        //final int[] textureHandle = new int[1];
        //textureHandle[0] = mTextureDataHandle;
        //GLES20.glDeleteTextures(1, textureHandle, 0);

        // Reload the texture (this is not efficient use of memory)
        //mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
        //R.drawable.text_bubble_bg, shapeTypes.puck, touchedX, touchedY, touchedZ, worldPlayerMinX, worldPlayerMaxX, worldPlayerMinY, worldPlayerMaxY, worldPlayerMinZ, worldPlayerMaxZ);

        // TODO: This textures is already active? change it to the puck texture
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[1]);

        // Tell the texture uniform sampler to use this texture in the shader by
        // binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        float[] newPuckCubePos = new float[4];
        float[] newPuckWorldCubePos = new float[4];
        float[] mPuckTempMVPMatrix = new float[16];

        for (int i = 0; i < thePuck.mCubePositions.capacity(); i++) {
            if (i == 0)
                continue;

            newPuckCubePos[i%3] = thePuck.mCubePositions.get(i);

            if ( (i % 3) == 0) {
                newPuckCubePos[3] = 1.0f;
                newPuckWorldCubePos[3] = 1.0f;

                Matrix.multiplyMM(mPuckTempMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
                Matrix.multiplyMM(mPuckTempMVPMatrix, 0, mProjectionMatrix, 0, mPuckTempMVPMatrix, 0);

                Matrix.multiplyMV(newPuckWorldCubePos, 0, mPuckTempMVPMatrix, 0, newPuckCubePos, 0);

                newPuckWorldCubePos[0] = newPuckWorldCubePos[0] / newPuckWorldCubePos[3];
                newPuckWorldCubePos[1] = newPuckWorldCubePos[1] / newPuckWorldCubePos[3];
                newPuckWorldCubePos[2] = newPuckWorldCubePos[2] / newPuckWorldCubePos[3];

                if (newPuckWorldCubePos[0] < worldPuckMinX) {
                    worldPuckMinX = newPuckWorldCubePos[0];
                }
                if (newPuckWorldCubePos[0] > worldPuckMaxX) {
                    worldPuckMaxX = newPuckWorldCubePos[0];
                }
                if (newPuckWorldCubePos[1] < worldPuckMinY) {
                    worldPuckMinY = newPuckWorldCubePos[1];
                }
                if (newPuckWorldCubePos[1] > worldPuckMaxY) {
                    worldPuckMaxY = newPuckWorldCubePos[1];
                }
                if (newPuckWorldCubePos[2] < worldPuckMinZ) {
                    worldPuckMinZ = newPuckWorldCubePos[2];
                }
                if (newPuckWorldCubePos[2] > worldPuckMaxZ) {
                    worldPuckMaxZ = newPuckWorldCubePos[2];
                }
            }
            //mModelMatrix * originalCubeVertices = newCubeVertices;
        }

        drawPuck();

        // Draw the opponent cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, opponentLocation[0], -(opponentLocation[1])/*invert the opponent player y */,/*-5.0f, 5.0f,*/ -20.0f);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[2]);

        // Tell the texture uniform sampler to use this texture in the shader by
        // binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        float[] newOpponentCubePos = new float[4];
        float[] newOpponentWorldCubePos = new float[4];
        float[] mOpponentTempMVPMatrix = new float[16];

        for (int i = 0; i < frontFaceCoordsSize; i++) {
            if (i == 0)
                continue;

            newOpponentCubePos[i%3] = mCubePositions.get(i);

            if ( (i % 3) == 0) {
                newOpponentCubePos[3] = 1.0f;
                //newOpponentWorldCubePos[3] = 1.0f;+++++++++++++++++

                Matrix.multiplyMM(mOpponentTempMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
                Matrix.multiplyMM(mOpponentTempMVPMatrix, 0, mProjectionMatrix, 0, mOpponentTempMVPMatrix, 0);

                Matrix.multiplyMV(newOpponentWorldCubePos, 0, mOpponentTempMVPMatrix, 0, newOpponentCubePos, 0);

                newOpponentWorldCubePos[0] = newOpponentWorldCubePos[0] / newOpponentWorldCubePos[3];
                newOpponentWorldCubePos[1] = newOpponentWorldCubePos[1] / newOpponentWorldCubePos[3];
                newOpponentWorldCubePos[2] = newOpponentWorldCubePos[2] / newOpponentWorldCubePos[3];

                if (newOpponentWorldCubePos[0] < worldOpponentMinX) {
                    worldOpponentMinX = newOpponentWorldCubePos[0];
                }
                if (newOpponentWorldCubePos[0] > worldOpponentMaxX) {
                    worldOpponentMaxX = newOpponentWorldCubePos[0];
                }
                if (newOpponentWorldCubePos[1] < worldOpponentMinY) {
                    worldOpponentMinY = newOpponentWorldCubePos[1];
                }
                if (newOpponentWorldCubePos[1] > worldOpponentMaxY) {
                    worldOpponentMaxY = newOpponentWorldCubePos[1];
                }
                if (newOpponentWorldCubePos[2] < worldOpponentMinZ) {
                    worldOpponentMinZ = newOpponentWorldCubePos[2];
                }
                if (newOpponentWorldCubePos[2] > worldOpponentMaxZ) {
                    worldOpponentMaxZ = newOpponentWorldCubePos[2];
                }
            }
            //mModelMatrix * originalCubeVertices = newCubeVertices;
        }

        drawCube();


        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 0.0f, lookX, lookY,
                -5.0f, upX, upY, upZ);
        // Draw bottom scrollbar
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -8.0f, -20.0f);

        drawBottomScrollBar();

        float[] mTempProjectionMatrix;
        mTempProjectionMatrix = new float[16];

        final float ratio = (float) getmWidth() / getmHeight();
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 0.1f;
        final float far = 100.0f;
        float fovY = 60;
        float zNear = 15.0f;
        float zFar = 1000.0f;
        float aspect = (float) getmWidth() / getmHeight();

        float fW, fH;
        fH = (float)Math.tan( fovY / 360 * Math.PI ) * zNear;
        fW = fH * aspect;

        Matrix.frustumM(mTempProjectionMatrix, 0, -fW, fW, -fH, fH, zNear, zFar);

        float[] newbottomScrollBarCubePos = new float[4];
        float[] newbottomScrollBarWorldCubePos = new float[4];
        float[] mbottomScrollBarTempMVPMatrix = new float[16];

        for (int i = 0; i < bottomScrollBar.mCubePositions.capacity(); i++) {
            if (i == 0)
                continue;

            newbottomScrollBarCubePos[i%3] = bottomScrollBar.mCubePositions.get(i);

            if ( (i % 3) == 0) {
                newbottomScrollBarCubePos[3] = 1.0f;
                //newOpponentWorldCubePos[3] = 1.0f;+++++++++++++++++

                //Matrix.multiplyMM(newbottomScrollBarWorldCubePos, 0, mModelMatrix, 0, newbottomScrollBarCubePos , 0);
                // instead of multuiplying th object vertex by only the model...
                //Matrix.multiplyMM(mbottomScrollBarTempMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
                //Matrix.multiplyMM(mbottomScrollBarTempMVPMatrix, 0, /*mProjectionMatrix*/mTempProjectionMatrix, 0, mbottomScrollBarTempMVPMatrix, 0);
                //Matrix.multiplyMV(newbottomScrollBarWorldCubePos, 0, mbottomScrollBarTempMVPMatrix, 0, newbottomScrollBarCubePos, 0);

                newbottomScrollBarWorldCubePos[0] = newbottomScrollBarCubePos[0] + 0.55f;
                newbottomScrollBarWorldCubePos[1] = newbottomScrollBarCubePos[1] - 8.0f;
                newbottomScrollBarWorldCubePos[2] = newbottomScrollBarCubePos[2] - 20.0f;

                //newbottomScrollBarWorldCubePos[0] = newbottomScrollBarWorldCubePos[0] / newbottomScrollBarWorldCubePos[3];
                //newbottomScrollBarWorldCubePos[1] = newbottomScrollBarWorldCubePos[1] / newbottomScrollBarWorldCubePos[3];
                //newbottomScrollBarWorldCubePos[2] = newbottomScrollBarWorldCubePos[2] / newbottomScrollBarWorldCubePos[3];

                if (newbottomScrollBarWorldCubePos[0] < worldBottomScrollBarMinX) {
                    worldBottomScrollBarMinX = newbottomScrollBarWorldCubePos[0];//posX *fNearHieght//;
                }
                if (newbottomScrollBarWorldCubePos[0] > worldBottomScrollBarMaxX) {
                    worldBottomScrollBarMaxX = newbottomScrollBarWorldCubePos[0];
                }
                if (newbottomScrollBarWorldCubePos[1] < worldBottomScrollBarMinY) {
                    worldBottomScrollBarMinY = newbottomScrollBarWorldCubePos[1];
                }
                if (newbottomScrollBarWorldCubePos[1] > worldBottomScrollBarMaxY) {
                    worldBottomScrollBarMaxY = newbottomScrollBarWorldCubePos[1];
                }
                if (newbottomScrollBarWorldCubePos[2] < worldBottomScrollBarMinZ) {
                    worldBottomScrollBarMinZ = newbottomScrollBarWorldCubePos[2];
                }
                if (newbottomScrollBarWorldCubePos[2] > worldBottomScrollBarMaxZ) {
                    worldBottomScrollBarMaxZ = newbottomScrollBarWorldCubePos[2];
                }
            }
            //mModelMatrix * originalCubeVertices = newCubeVertices;
        }

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
                -20.0f, upX, upY, upZ);

    /*
        for (int i = 0; i < bottomScrollBar.mCubePositions.capacity(); i++) {
            if (i == 0)
                continue;

            newPuckCubePos[i % 3] = bottomScrollBar.mCubePositions.get(i);

            if ((i % 3) == 0) {
                newPuckCubePos[3] = 1.0f;
                newPuckWorldCubePos[3] = 1.0f;

                Matrix.multiplyMM(mPuckTempMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
                Matrix.multiplyMM(mPuckTempMVPMatrix, 0, mProjectionMatrix, 0, mPuckTempMVPMatrix, 0);

                Matrix.multiplyMV(newPuckWorldCubePos, 0, mPuckTempMVPMatrix, 0, newPuckCubePos, 0);

                newPuckWorldCubePos[0] = newPuckWorldCubePos[0] / newPuckWorldCubePos[3];
                newPuckWorldCubePos[1] = newPuckWorldCubePos[1] / newPuckWorldCubePos[3];
                newPuckWorldCubePos[2] = newPuckWorldCubePos[2] / newPuckWorldCubePos[3];


            }
        } */


        // Add crosshair to where the use has touched onto the screen
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, touchedX,/*outputCoordObj[0], outputCoordObj[1]*/touchedY, -20.0f/*touchedZ???*/);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[3]);

        // Tell the texture uniform sampler to use this texture in the shader by
        // binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        drawCrosshair();

        // correct way is to set the state in gamemachine class
        //gameMachine.Advance("PlayerWins");

        switch (gameMachine.CurrentState().GetName())
        {
            case "Entry":
                // TODO: Here is the where start menu will go
                break;
            case "Pong":
                checkPuckCollision();

                movePuck();//*/

                if (playerScore > 1) {
                    gameMachine.Advance("PlayerWins");
                }
                else if (opponentScore > 3) {
                    gameMachine.Advance("OpponentWins");
                }
                break;
                // Why wont this let me move indent?
            case "ActiveMatch":
                // send the player data to the server
                opponentLocation = opponent.getLocation();
                // get the checkpuckcollision and move puck data fom the server
                if (playerScore > 1) {
                    gameMachine.Advance("PlayerWins");
                }
                else if (opponentScore > 3) {
                    gameMachine.Advance("OpponentWins");
                }
                break;

            case "PlayerWins":
                // TODO: move the win condition handling to here
				/*if (mCubePositions.get(0) <= 20.5) {
					GLES20.glRotatef(15, 1, 0, 0);
					/*for (int i = 0; i < mCubePositions.capacity(); i++) {
                        if ( (i > 0) && (i % 3 == 0))
                            continue;
                        mCubePositions.put(i, mCubePositions.get(i) + .001f);
                    }
                    mCubePositions.put(0, mCubePositions.get(0) + .001f);
                    mCubePositions.put(1, mCubePositions.get(1) + .001f);
                    mCubePositions.put(2, mCubePositions.get(2) + .001f);
                    mCubePositions.put(3, mCubePositions.get(3) + .001f);
                    mCubePositions.put(4, mCubePositions.get(4) + .001f);
                    mCubePositions.put(5, mCubePositions.get(5) + .001f);
                    mCubePositions.put(6, mCubePositions.get(6) + .001f);
                    mCubePositions.put(7, mCubePositions.get(7) + .001f);
                    mCubePositions.put(8, mCubePositions.get(8) + .001f);
                    mCubePositions.put(99, mCubePositions.get(0) + .001f);
                    mCubePositions.put(100, mCubePositions.get(1) + .001f);
                    mCubePositions.put(101, mCubePositions.get(2) + .001f);
                    mCubePositions.put(102, mCubePositions.get(3) + .001f);
                    mCubePositions.put(103, mCubePositions.get(4) + .001f);
                    mCubePositions.put(104, mCubePositions.get(5) + .001f);
                    mCubePositions.put(105, mCubePositions.get(6) + .001f);
                    mCubePositions.put(106, mCubePositions.get(7) + .001f);
                    mCubePositions.put(107, mCubePositions.get(8) + .001f);

				}*/
                break;

            case "OpponentWins":
                // TODO: the camera will pan around the puck
                if (lookY >= 0.5) {
                    gameMachine.Advance("Pong");
                }
                // Zoom to the opponent in 5 seconds.
                //long time = SystemClock.uptimeMillis() % 5000L;
                //float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

                // Position the eye in front of the origin.
                //final float eyeX = 0.0f;
                //final float eyeY = 0.0f;
                if (eyeZ >= -1.5)
                    eyeZ -= 0.01;

                // We are looking toward the distance
                //final float lookX = 0.0f;
                if (eyeZ <= -1.5)
                    lookY += 0.001f;
                //final float lookZ = -5.0f;

                // Set our up vector. This is where our head would be pointing were we
                // holding the camera.
                //final float upX = 0.0f;
                //final float upY = 1.0f;
                //final float upZ = 0.0f;

                // Set the view matrix. This matrix can be said to represent the camera
                // position.
                // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
                // of a model and
                // view matrix. In OpenGL 2, we can keep track of these matrices
                // separately if we choose.
                Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
                        lookZ, upX, upY, upZ);

                // Display "You Lose! Retry?

                break;
            case "GameOver":
                break;
            default:
                break;

        }

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // TEST: render the entire font texture
        //glText.drawTexture( mWidth/2, mHeight/2, mMVPMatrix);            // Draw the Entire Texture
        // TEST: render some strings with the font
        glText.begin( 0.0f, 0.0f, 1.0f, 1.0f, mMVPMatrix );         // Begin Text Rendering
        //glText.drawC("Test String 3D!", 0f, 0f, 0f, 0, -30, 0);
        //glText.drawC( "Test String :)", 0, 0, 0 );          // Draw Test String
        //glText.draw( "Diagonal 1", 0.0f, 0.0f, -9.0f);                // Draw Test String
        //glText.draw( "Column 1", 100, 100, 90);              // Draw Test String
        if (gameMachine.CurrentState().GetName() == "ActiveMatch") {
            glText.draw( player.getUsername() + ": " + form.format(player.getScore())  , -120.0f, 50.0f, -450.0f, 0.0f, 0.0f, 0.0f);
            glText.draw( opponent.getUsername() + ": " + form.format(opponent.getScore())  , -120.0f, 60.0f, -450.0f, 0.0f, 0.0f, 0.0f);

        }
        glText.draw( "FPS: " + fpsCounter.fps, -120.0f, 150.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Touched X:" + form.format(touchedX) + ", Y: " + form.format(touchedY) + ", Z: " + form.format(touchedZ), -120.0f, 140.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        glText.draw( "OnDrawFrame Time: " + form.format(onDrawFrametime)  , -120.0f, 130.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        glText.draw( "Player TL: " + form.format(worldPlayerMinX) + ", " + form.format(worldPlayerMaxY) + ", BR: " + form.format(worldPlayerMaxX) + ", " + form.format(worldPlayerMinY), -120.0f, 120.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        glText.draw( "Opponent TL: " + form.format(worldOpponentMinX) + ", " + form.format(worldOpponentMaxY) + ", BR: " + form.format(worldOpponentMaxX) + ", " + form.format(worldOpponentMinY), -120.0f, 110.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Puck TopLeft: " + form.format(worldPuckMinX) + ", " + form.format(worldPuckMaxY), -120.0f, 100.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( ", BottomR: " + form.format(worldPuckMaxX) + ", " + form.format(worldPuckMinY), -120.0f, 90.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "SCORE", -120.0f, 90.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Player: " + playerScore , -120.0f, 80.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Opponent: " + opponentScore , -120.0f, 70.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "fW: " + form.format(this.fW)  + ", fH: " + form.format(this.fH),  -120.0f, 180.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "BottomScrollBar TL: " + form.format(worldBottomScrollBarMinX) + ", " + form.format(worldBottomScrollBarMaxY), -120.0f, 210.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "BottomScrollBar BR: " + form.format(worldBottomScrollBarMaxX) + ", " + form.format(worldBottomScrollBarMinY), -120.0f, 200.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "BottomScrollBar Selected Pct: " + form.format(bottomScrollBar.getSelectedPct()), -120.0f, 190.0f, -400.0f, 0.0f, 0.0f, 0.0f);

        //glText.draw( "BottomScrollBarHeightGreenCalc: " + form.format(worldBottomScrollGreenHeight) , -120.0f, 60.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "BottomScrollBarHeightActual: " + form.format(worldBottomScrollBarMaxY - worldBottomScrollBarMinY) , -120.0f, 50.0f, -400.0f, 0.0f, 0.0f, 0.0f);
        glText.draw( "World TouchX: " + form.format(touchedX) + ", TouchY: " + form.format(touchedY) , -120.0f, 160.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        glText.draw( "Raw Touch Y: " + form.format(rawTouchY)  , -120.0f, 170.0f, -450.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Server Ping: "   , -120.0f, 40.0f, -400.0f, 0.0f, 0.0f, 0.0f);

        //glText.draw( "XOffset[0]: " + XOffset[0] , -120.0f, 100.0f, -20.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "YOffset[0]: " + YOffset[0] , -120.0f, 90.0f, -20.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "XOffsetIncr[0]: " + XOffsetIncr[0] , -120.0f, 80.0f, -20.0ff, 0, 0.0.0f, 0.0f);
        //glText.draw( "YOffsetIncr[0]: " + YOffsetIncr[0] , -120.0f, 70.0f, -20.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "CPU Temp: " + fpsCounter.fps, -120.0f, 180.0f, -20.0f, 0.0f, 0.0f, 0.0f);
        //glText.draw( "Mem Usage: " + fpsCounter.fps, -120.0f, 180.0f, -20.0f, 0.0f, 0.0f, 0.0f);//glText.draw("Puck -,+Y: " + form.format(boxMinZ) + ", " + form.format(boxMaxZ), 20, 205, textPaint);*/
        glText.end();                                   // End Text Rendering

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();

        endTime = System.nanoTime();
        onDrawFrametime = (endTime - startTime)/1000000;
    }


        /**
         * Draws a cube.
         */
	private void drawCube() {
		// Pass in the position information
        if (gameMachine.CurrentState().GetName() == "PlayerWins") { // TODO: move player wins condition to the state switch statement in draw()

            mCubePositions.position(0);
            GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubePositions);

            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Pass in the color information
            mCubeColors.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubeColors);

            GLES20.glEnableVertexAttribArray(mColorHandle);

            // Pass in the normal information
            mCubeNormals.position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubeNormals);

            GLES20.glEnableVertexAttribArray(mNormalHandle);

            // Pass in the texture coordinate information
            mCubeTextureCoordinates.position(0);
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                    mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
                    mCubeTextureCoordinates);

            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            // This multiplies the view matrix by the model matrix, and stores the
            // result in the MVP matrix
            // (which currently contains model * view).

            // testpoint1
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

            // Pass in the modelview matrix.
            GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

            // This multiplies the modelview matrix by the projection matrix, and
            // stores the result in the MVP matrix
            // (which now contains model * view * projection).
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

            for (int i = 0; i < 36; i++) {
                if ( (i > 0) && ((i % 3) == 0) ) { // Algorithm to explode the paddles vertices
                    //TRANSLATION
                    float[] transMatrix = new float[16];
                    float[] scratch = new float[16];

                    XOffset[(i/3)-1] += XOffsetIncr[(i/3)-1];
                    YOffset[(i/3)-1] += YOffsetIncr[(i/3)-1];

                    Matrix.setIdentityM(mRotationMatrix, 0);

                    Matrix.translateM(mRotationMatrix, 0, XOffset[(i / 3) - 1], YOffset[(i / 3) - 1], 0);

                    // Use the following code to generate constant rotation.
                    // Leave this code out when using TouchEvents.
                    long time = SystemClock.uptimeMillis() % 4000L;
                    mAngle = 0.360f * ((int) time);

                    Matrix.rotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

                    Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

                    // Pass in the combined matrix.
                    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0);

                    // Pass in the light position in eye space.
                    GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
                            mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

                    // Draw one translated triangle at a time.
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, i, 3);
                }
            }
        }
        else {
            // draw the player or opponent normally
            mCubePositions.position(0);
            GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubePositions);

            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Pass in the color information
            mCubeColors.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubeColors);

            GLES20.glEnableVertexAttribArray(mColorHandle);

            // Pass in the normal information
            mCubeNormals.position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize,
                    GLES20.GL_FLOAT, false, 0, mCubeNormals);

            GLES20.glEnableVertexAttribArray(mNormalHandle);

            // Pass in the texture coordinate information
            mCubeTextureCoordinates.position(0);
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                    mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
                    mCubeTextureCoordinates);

            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            // This multiplies the view matrix by the model matrix, and stores the
            // result in the MVP matrix
            // (which currently contains model * view).
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

            // Pass in the modelview matrix.
            GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

            // This multiplies the modelview matrix by the projection matrix, and
            // stores the result in the MVP matrix
            // (which now contains model * view * projection).
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

            // Pass in the combined matrix.
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            // Pass in the light position in eye space.
            GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
                    mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

            getOpenGLActiveAttribs();

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        }
	}

	public void drawPuck() {
		// Pass in the position information
		thePuck.mCubePositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, thePuck.mPositionDataSize,
				GLES20.GL_FLOAT, false, 0, thePuck.mCubePositions);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		thePuck.mCubeColors.position(0);
		GLES20.glVertexAttribPointer(mColorHandle, thePuck.mColorDataSize,
				GLES20.GL_FLOAT, false, 0, thePuck.mCubeColors);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// Pass in the normal information
		thePuck.mCubeNormals.position(0);
		GLES20.glVertexAttribPointer(mNormalHandle, thePuck.mNormalDataSize,
				GLES20.GL_FLOAT, false, 0, thePuck.mCubeNormals);

		GLES20.glEnableVertexAttribArray(mNormalHandle);

		// Pass in the texture coordinate information
		thePuck.mCubeTextureCoordinates.position(0);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
				thePuck.mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				thePuck.mCubeTextureCoordinates);

		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// Pass in the modelview matrix.
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Pass in the light position in eye space.
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
				mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

		// Draw the puck using a triangle strip as per the puck sphere vertex setup arrangement.
		GLES20.glDrawArrays(/*GLES20.GL_TRIANGLES*/GLES20.GL_TRIANGLE_STRIP, 0, thePuck.mCubePositions.capacity()/3); // Careful with last argument for triangle_strip points!!
	}

    public void drawCrosshair() {
        // Pass in the position information
        crosshair.mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, crosshair.mPositionDataSize,
                GLES20.GL_FLOAT, false, 0, crosshair.mCubePositions);

        GLES20.glEnableVertexAttribArray(crosshair.getmPositionHandle());

        // Pass in the color information
        ////////crosshair.mCubeColors.position(0);
        ////////GLES20.glVertexAttribPointer(mColorHandle, crosshair.mColorDataSize,
        ////////        GLES20.GL_FLOAT, false, 0, crosshair.mCubeColors);

        /////GLES20.glEnableVertexAttribArray(crosshair.getmColorHandle());

        // Pass in the normal information
        crosshair.mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, crosshair.mNormalDataSize,
                GLES20.GL_FLOAT, false, 0, crosshair.mCubeNormals);

        GLES20.glEnableVertexAttribArray(crosshair.getmNormalHandle());

        // Pass in the texture coordinate information
        crosshair.mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
                crosshair.mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // This multiplies the view matrix by the model matrix, and stores the
        // result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and
        // stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the crosshair.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    /**
	 * Draws a point representing the position of the light.
	 */
	private void drawLight() {
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(
				mPointProgramHandle, "u_MVPMatrix");
		final int pointPositionHandle = GLES20.glGetAttribLocation(
				mPointProgramHandle, "a_Position");

		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0],
				mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for
		// this attribute.
		GLES20.glDisableVertexAttribArray(pointPositionHandle);

		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	public void setmContext(Context context) {
		// TODO Auto-generated method stub
		this.mActivityContext = context;
	}

	public float[] getmMVPMatrix() {
		return mMVPMatrix;
	}

	public float[] getmProjectionMatrix() {
		return mProjectionMatrix;
	}

    public float[] getmModelMatrix() {
        return mModelMatrix;
    }

	public float[] getmViewMatrix() {
		return mViewMatrix;
	}
	
	public int getmWidth() {
		return mWidth;
	}
	
	public int getmHeight() {
		return mHeight;
	}

    public void setTouchedPoint(float newX, float newY, float newZ, float[] outputCoordObj, float fWNear, float fHNear/*, float fWFar, float fHFar*/) {
        touchedX = newX;
        touchedY = newY;
        touchedZ = newZ;
        this.outputCoordObj[0] = outputCoordObj[0];
        this.outputCoordObj[1] = outputCoordObj[1];
        this.outputCoordObj[2] = outputCoordObj[2];
        this.fieldWidth = fWNear;
        this.fieldHeight = fHNear;
        //this.fWidthFar = fWFar;
        //this.fHeightFar = fHFar;

        float minY = -10.0f, maxY = -10.0f;
        //touchedY

        while ( (maxY/fieldHeight) < worldBottomScrollBarMaxY)
        {
            maxY = maxY + 0.000001f;
        }
        while ( (minY/fieldHeight) < worldBottomScrollBarMinY)
        {
            minY += 0.000001f;
        }

        worldBottomScrollGreenHeight = (minY/fieldHeight) - (maxY/fieldHeight);

        if ( (touchedX/fieldWidth > worldBottomScrollBarMinX) && (touchedX/fieldWidth < worldBottomScrollBarMaxX) ) {
           // if ((touchedY / fHeightNear > worldBottomScrollBarMinY) && (touchedY / fHeightNear < worldBottomScrollBarMaxY)) {
                //if ( (touchedY/fHeightNear > worldOpponentMinY) && (touchedY/fHeightNear < worldOpponentMaxY) ) {


                // R, G, B, A
                final float[] cubeColorData = {
                        // Front face (green)
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,};

                bottomScrollBar.mCubeColors.clear();
                bottomScrollBar.mCubeColors.put(cubeColorData).position(0);
            }
            //}
            else {
                // R, G, B, A
                final float[] cubeColorData = {
                        // Front face (red)
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,};

                bottomScrollBar.mCubeColors.clear();
                bottomScrollBar.mCubeColors.put(cubeColorData).position(0);

            }
        //}
    }


    /**
     * Calculates the transform from screen coordinate
     * system to world coordinate system coordinates
     * for a specific point, given a camera position.
     *
     * @param touch Vec2 point of screen touch, the
    actual position on physical screen (ej: 160, 240)

     * @return position in WCS.
     */
    public void SetWorldTouchCoords( float[] touch)
    {
        // Initialize auxiliary variables.
        float[] worldPos = new float[3];

        // SCREEN height & width (ej: 320 x 480)
        float screenW = getmWidth();
        float screenH = getmHeight();

        // Auxiliary matrix and vectors
        // to deal with ogl.
        float[] invertedMatrix, mTempMVPMatrix,mTempViewMatrix,  mTempModelMatrix, transformMatrix,
                mTempProjectionMatrix, normalizedInPoint, outPoint;
        invertedMatrix = new float[16];
        mTempMVPMatrix = new float[16];
        mTempModelMatrix = new float[16];
        mTempProjectionMatrix = new float[16];
        mTempViewMatrix = new float[16];
        transformMatrix = new float[16];
        normalizedInPoint = new float[4];
        outPoint = new float[4];

        ///Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
        ///		lookZ, upX, upY, upZ);

        //Setting the view matrix
        Matrix.setLookAtM(mTempViewMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f);


        final float ratio = (float) getmWidth() / getmHeight();
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 0.1f;
        final float far = 100.0f;
        float fovY = 60;
        float zNear = 19.0f;
        float zFar = 1000.0f;
        float aspect = (float) getmWidth() / getmHeight();

        float fW, fH;
        fH = (float)Math.tan( fovY / 360 * Math.PI ) * zNear;
        fW = fH * aspect;
        this.fW = fW;
        this.fH = fH;

        Matrix.frustumM(mTempProjectionMatrix, 0, -fW, fW, -fH, fH, zNear, zFar);

        // Invert y coordinate, as android uses
        // top-left, and ogl bottom-left.
        int oglTouchY = (int) (screenH - touch[1]);
        rawTouchY = touch[1];

        /* Transform the screen point to clip
        space in ogl (-1,1) */
        normalizedInPoint[0] =
         (float) ((touch[0]) * 2.0f / screenW - 1.0);
        normalizedInPoint[1] =
         (float) ((oglTouchY) * 2.0f / screenH - 1.0);
        normalizedInPoint[2] = - 1.0f;
        normalizedInPoint[3] = 1.0f;

        /* Obtain the transform matrix and
        then the inverse. */
        //Print("Proj", getCurrentProjection(gl));
        //Print("Model", getCurrentModelView(gl));
        Matrix.setIdentityM(mTempModelMatrix, 0);
        //Matrix.translateM(mTempModelMatrix, 0, 0.0f, 0.0f, -20.0f);

        Matrix.multiplyMM(mTempMVPMatrix, 0, mTempViewMatrix/*mViewMatrix*/, 0, mTempModelMatrix, 0);
        //Matrix.multiplyMM(mTempMVPMatrix, 0, mTempProjectionMatrix, 0, mTempMVPMatrix, 0);

        Matrix.multiplyMM(
            transformMatrix, 0,
                mTempProjectionMatrix/*getCurrentProjection(mRenderer.unused)*/, 0,
                mTempMVPMatrix/*getCurrentModelView(gl)*/, 0);
        Matrix.invertM(invertedMatrix, 0,
            transformMatrix, 0);

        /* Apply the inverse to the point
        in clip space */
        Matrix.multiplyMV(
            outPoint, 0,
            invertedMatrix, 0,
            normalizedInPoint, 0);

        /*if (outPoint[3] == 0.0)
        {
            // Avoid /0 error.
            //Log.e("World coords", "ERROR!");
            return worldPos;
        }*/

        // Divide by the 3rd component to find
        // out the real position.
        worldPos[0] = outPoint[0] / outPoint[3];
        worldPos[1] = outPoint[1] / outPoint[3];
        worldPos[2] = outPoint[2] / outPoint[3];

        touchedX = worldPos[0];//*fieldWidth;
        touchedY = worldPos[1];//*fieldHeight;
        touchedZ = worldPos[2];

        float minY = -10.0f, maxY = -10.0f;
        //touchedY

        while ( (maxY/fH) < worldBottomScrollBarMaxY)
        {
            maxY = maxY + 0.000001f;
        }
        while ( (minY/fH) < worldBottomScrollBarMinY)
        {
            minY += 0.000001f;
        }

        worldBottomScrollGreenHeight = (minY/fH) - (maxY/fH);


        if ( (touchedX > worldBottomScrollBarMinX) && (touchedX < worldBottomScrollBarMaxX)
                && ( touchedY > worldBottomScrollBarMinY) && ( touchedY < worldBottomScrollBarMaxY)) {

            float lengthScrollbar = worldBottomScrollBarMaxX -  worldBottomScrollBarMinX;// Math.abs(worldBottomScrollBarMinX) + Math.abs(worldBottomScrollBarMaxX);
            float leftOffset = touchedX - worldBottomScrollBarMinX;
            float pct = (leftOffset / lengthScrollbar) * 100.0f;
            bottomScrollBar.setSelectedPct( pct );

            //long time = SystemClock.uptimeMillis() % 4000L;
            // float angle = 0.010f * ((int) time);
            if (phi < (2*Math.PI) ) {
                phi += 0.01d;
            }
            else if (phi > (2*Math.PI ))
                phi = 0.0d;

            /*if (theta < Math.PI) {
                theta += 0.01d;
            }
            else if (theta > Math.PI)
                theta = -Math.PI;*/
            theta = (Math.PI) * (pct/100.0f);

            eyeX = (float) (0.0f +20.0f*Math.cos(theta));//*Math.sin(theta) );
            //eyeY = (float) (1.0f + 100.0f*Math.sin(phi)*Math.sin(theta) );
            eyeZ = (float) (-20.0f +20.0f*Math.sin(theta) ); ////

            //lookY = 1.0f;
            //lookZ = -20.0f;

            /////eyeX = (float) (pickObjX + /*1.0f* */ // Math.cos( i*2 )*Math.sin( i ));
            //eyeY = (float) (pickObjY + /*1.0f */ Math.sin( i )*Math.sin( i ));
            //eyeZ = (float) (pickObjZ + /*1.0f*/  Math.cos( i * 2 ) );
            //eyeY = (float) (lookY + 1.0f*Math.sin(pct)*Math.sin(pct));
            //eyeZ = (float) (lookZ + 20.0f* Math.cos(Math.PI*(100.0f/pct)));

            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
                    -20.0f, upX, upY, upZ);

            // Update the view
            //if (eyeZ >= -1.5)
            //    eyeZ -= 0.01;

            // We are looking toward the distance
            //final float lookX = 0.0f;
            //if (eyeZ <= -1.5)
            //    lookY += 0.001f;
            //final float lookZ = -5.0f;

            //eyeX = (float) (0.0f*Math.cos( 0.4f *  pct ) );
            //eyeY = (float) (1.0f*Math.sin( 0.4f * pct ) );

            float phi = (float) Math.PI*2;
            float theta = (float) Math.PI;
            float pickObjX = 0.0f;
            float pickObjY = 0.0f;
            float pickObjZ = 20.0f;

            /* for (float i = 0.0000001f; i < Math.PI; i += 0.0000001f)
            {

                //// eyeX = pickObjX + radius*cos(phi)*sin(theta);
                eyeY = pickObjY + radius*sin(phi)*sin(theta);
                eyeZ = pickObjZ + radius*cos(theta); ////

                /////eyeX = (float) (pickObjX + /*1.0f* */ // Math.cos( i*2 )*Math.sin( i ));
                //eyeY = (float) (pickObjY + /*1.0f */ Math.sin( i )*Math.sin( i ));
                //eyeZ = (float) (pickObjZ + /*1.0f*/  Math.cos( i * 2 ) );
                //eyeY = (float) (lookY + 1.0f*Math.sin(pct)*Math.sin(pct));
                //eyeZ = (float) (lookZ + 20.0f* Math.cos(Math.PI*(100.0f/pct)));

                //Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
                //        lookZ, upX, upY, upZ);
            //} */
            //eyeX = (float) (lookX + /*1.0f* */ Math.cos( Math.PI*(100.0f/pct) )*Math.sin( Math.PI*(100.0f/pct) ));
            //eyeY = (float) (lookY + 1.0f*Math.sin(pct)*Math.sin(pct));
            //eyeZ = (float) (lookZ + 20.0f* Math.cos(Math.PI*(100.0f/pct)));

            //Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
            //        lookZ, upX, upY, upZ);


             //if (( touchedY/fH < worldBottomScrollBarMaxY)  && (touchedY/fH > worldBottomScrollBarMinY)) {
            //if ( (touchedY/fHeightNear > worldOpponentMinY) && (touchedY/fHeightNear < worldOpponentMaxY) ) {


            // R, G, B, A
            final float[] cubeColorData = {
                    // Front face (green)
                    0.0f, 1.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 0.0f, 1.0f,};

            bottomScrollBar.mCubeColors.clear();
            bottomScrollBar.mCubeColors.put(cubeColorData).position(0);
        //}
        }
        else {
            // R, G, B, A
            final float[] cubeColorData = {
                    // Front face (red)
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,};

            bottomScrollBar.mCubeColors.clear();
            bottomScrollBar.mCubeColors.put(cubeColorData).position(0);

        }
    }

    /*
     * The collision detection is calculated by the objects screen position
     * TODO: make the winning score adjustable
     */
    public void checkPuckCollision() {

        float deltaPuckX = (worldPuckMaxX + worldPuckMinX)/2.0f;
        float deltaPuckY = worldPuckMaxY - worldPuckMinY;
        float deltaBoxX = (worldPlayerMaxX + worldPlayerMinX)/2.0f;
        float deltaBoxY = worldPlayerMaxY - worldPlayerMinY;
        float deltaOpponentX = (worldOpponentMaxX + worldOpponentMinX)/2.0f;

        if (worldPuckMinX <= -1.0) {
            thePuck.puckXDirection = 1;
        }
        else if (worldPuckMaxX >= 1.0) {
            thePuck.puckXDirection = -1;
        }

        if (worldPuckMinY >= 1.0) { // reset game
            playerScore++;
            //playerWinAnim();
            thePuck.puckYDirection = -1;
            thePuck.posX = 0;
            thePuck.posY = 0;

        }
        if (worldPuckMaxY <= -1.0) {
            opponentScore++;
            thePuck.puckYDirection = 1;
            thePuck.posX = 0;
            thePuck.posY = 0;

        }
        if (thePuck.puckYDirection == -1) {

            if (worldPuckMinY <= worldPlayerMaxY) {
                if ( (worldPuckMaxX >= worldPlayerMinX) && (worldPuckMinX <= worldPlayerMaxX) ) { //(   <= max) ) {
                    thePuck.puckYDirection = 1;
                    //if (deltaPuckX > deltaBoxX)
                    thePuck.puckSpeed = 0.1f+((deltaBoxX - deltaPuckX  )/3.0f);//worldPlayerMaxX - worldPuckMaxX);
                    //thePuck.posY += 0.05f;
                    //worldPuckMinX = 1.0f; worldPuckMaxX = -1.0f; worldPuckMinY = 1.0f; worldPuckMaxY = -1.0f; worldPuckMinZ = 0.0f ; worldPuckMaxZ = 0.0f;
                }
            }
        }
        else if (thePuck.puckYDirection == 1) {
            if (worldPuckMaxY >= worldOpponentMinY) {
                if ((worldPuckMaxX >= worldOpponentMinX) && (worldPuckMinX <= worldOpponentMaxX)) {//(   <= max) ) {
                    thePuck.puckYDirection = -1;
                    thePuck.puckSpeed = 0.10f+((deltaOpponentX - deltaPuckX) / 3.0f);//worldPlayerMaxX - worldPuckMaxX);
                    //thePuck.posY -= 0.05f;
                    //worldPuckMinX = 1.0f; worldPuckMaxX = -1.0f; worldPuckMinY = 1.0f; worldPuckMaxY = -1.0f; worldPuckMinZ = 0.0f ; worldPuckMaxZ = 0.0f;
                }
            }
        }
    }

    /*
     * TODO: Speed needs adjustment
     */
    public void movePuck() {
        if (thePuck.puckYDirection == 1) {
            thePuck.posY += 0.10f;
        }
        else if (thePuck.puckYDirection == -1) {
            thePuck.posY -= 0.10f;
        }
        if (thePuck.puckXDirection == 1) {
            thePuck.posX += thePuck.puckSpeed/10.0f;
        }
        else if (thePuck.puckXDirection == -1) {
            thePuck.posX -= thePuck.puckSpeed/10.0f;
        }
    }

    void getOpenGLActiveAttribs() {

        /*int i;
        java.nio.IntBuffer count = new IntBuffer();
        java.nio.IntBuffer size; // size of the variable

        final int bufSize = 16; // maximum name length
        String name;//[bufSize]; // variable name in GLSL
        java.nio.IntBuffer length; // name length


        GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_ACTIVE_ATTRIBUTES, count);
        //printf("Active Attributes: %d\n", count);

        for (i = 0; i < count.capacity(); i++)
        {
            GLES20.glGetActiveAttrib(mProgramHandle, (int)i, bufSize, length, size, type, name);

            printf("Attribute #%d Type: %u Name: %s\n", i, type, name);
        }
*/

    }

    private void drawBottomScrollBar() {
        // Pass in the position information
        bottomScrollBar.mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, bottomScrollBar.mPositionDataSize,
                GLES20.GL_FLOAT, false, 0, bottomScrollBar.mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle/*bottomScrollBar.getmPositionHandle()*/);

        // Pass in the color information
        bottomScrollBar.mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, bottomScrollBar.mColorDataSize,
               GLES20.GL_FLOAT, false, 0, bottomScrollBar.mCubeColors);

        GLES20.glEnableVertexAttribArray(mColorHandle/*bottomScrollBar.getmColorHandle()*/);

        // Pass in the normal information
        bottomScrollBar.mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, bottomScrollBar.mNormalDataSize,
                GLES20.GL_FLOAT, false, 0, bottomScrollBar.mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle/*bottomScrollBar.getmNormalHandle()*/);

        // Pass in the texture coordinate information
        bottomScrollBar.mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
                bottomScrollBar.mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // This multiplies the view matrix by the model matrix, and stores the
        // result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and
        // stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the crosshair.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);


    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }

    public void youWin() {
        // Scroll through a bunch of game stats with a compliment beside them at speed
    }

    public void youLose() {
        // Everything on screen explodes, YOU LOSE! featured prominently
    }
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}