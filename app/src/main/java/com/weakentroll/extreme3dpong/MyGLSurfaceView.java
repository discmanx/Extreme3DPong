package com.weakentroll.extreme3dpong;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 *
 * Created by: Ryan Baldwin
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private GLU glU;
    
    private String serverMsg;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        //mRenderer.setmContext(getContext());
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        //mRenderer.setmContext(getContext());
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

    	float[] newCubePos = new float[4];
    	float[] newWorldCubePos = new float[4];
        float[] outputCoordObj = new float[4];
		float[] mTempViewMatrix = new float[16];
		float[] mTempProjectionMatrix = new float[16];
		float[] mTempMVPMatrix = new float[16];

		float[] mModelMatrix = new float[16];
		
		// Position the eye in front of the origin.
		/*final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 0.0f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f; */

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		////Matrix.setLookAtM(mTempViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //Setting the view matrix
        Matrix.setLookAtM(mTempViewMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f);


        float testWidth = mRenderer.getmWidth();
        float testHeight = getHeight();
		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) mRenderer.getmWidth() / mRenderer.getmHeight();
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 0.1f;
		final float far = 100.0f;
		float fovY = 60;
		float zNear = 20.0f;
		float zFar = 1000.0f;
		float aspect = (float) mRenderer.getmWidth() / mRenderer.getmHeight();


		//double pi = 3.1415926535897932384626433832795;
		float fW, fH;

		//fH = tan( (fovY / 2) / 180 * pi ) * zNear;
		fH = (float)Math.tan( fovY / 360 * Math.PI ) * zNear;
		fW = fH * aspect;

		//glFrustum( -fW, fW, -fH, fH, zNear, zFar );

		//Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
		Matrix.frustumM(mTempProjectionMatrix, 0, -fW, fW, -fH, fH, zNear, zFar);


		newWorldCubePos[3] = 1.0f;
		
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -20.0f);
	
		Matrix.multiplyMM(mTempMVPMatrix, 0, mTempViewMatrix, 0, mModelMatrix, 0);
		//Matrix.multiplyMM(mTempMVPMatrix, 0, mTempProjectionMatrix, 0, mTempMVPMatrix, 0);



        float x = e.getX();//0.0f; //e.getX();
        // TODO: subtract the height of the action toolbar and quit button layout from the total screen size to get .5 y touch screen value
        float y = (mRenderer.getmHeight() - e.getY()); //float y = e.getY();


        newCubePos[0] = x;
        newCubePos[1] = y;
        newCubePos[2] = 0.0f;
        newCubePos[3] = 1.0f;
        
        int[] convertedViewMatrix  = { 0, 0, mRenderer.getmWidth(), mRenderer.getmHeight() };
        
        /*for (int i = 0; i < mRenderer.getmViewMatrix().length-1; i++) {
        	convertedViewMatrix[i] = (int)mRenderer.getmViewMatrix()[i];
        }*/
        
        //GLint viewport[4];                  // Where The Viewport Values Will Be Stored
        //GLES20.glGetIntegerv(GL11.GL_VIEWPORT, convertedViewMatrix, 0);           // Retrieves The Viewport Values (X, Y, Width, Height)
        	
        Matrix.multiplyMV(newWorldCubePos, 0, mTempMVPMatrix, 0, newCubePos, 0);
		
		newWorldCubePos[0] = newWorldCubePos[0] / newWorldCubePos[3];
		newWorldCubePos[1] = newWorldCubePos[1] / newWorldCubePos[3];
		newWorldCubePos[2] = newWorldCubePos[2] / newWorldCubePos[3];

        int status = GLU.gluUnProject(x, y, 0.0f, mTempMVPMatrix, 0, mTempProjectionMatrix /* change back!!!@ mRenderer.getmProjectionMatrix() */,
         			  0, convertedViewMatrix, 0, outputCoordObj, 0);

        //outputCoordObj[0] /= ((float)mRenderer.getmWidth() / (float)mRenderer.getmHeight());
        float touchedX = (outputCoordObj[0] / outputCoordObj[3]);///((float)mRenderer.getmWidth()/(float)mRenderer.getmHeight());//newWorldCubePos[0]; //
        //touchedX /= ((float)mRenderer.getmWidth() / (float)mRenderer.getmHeight());
        float touchedY = outputCoordObj[1] / outputCoordObj[3];//newWorldCubePos[1]; //
        float touchedZ = outputCoordObj[2] / outputCoordObj[3];//newWorldCubePos[2]; //



        System.out.printf("Touched screen coords->world coords are: %f, %f, %f", touchedX, touchedY, touchedZ);

        mPreviousX = x;
        mPreviousY = y;
    /* not needed anymore to get the far clip plane
        status = GLU.gluUnProject(x, y, 0.020f, mTempMVPMatrix, 0, mRenderer.getmProjectionMatrix(),
                0, convertedViewMatrix, 0, outputCoordObj, 0);

        float touchedX2 = (outputCoordObj[0] / outputCoordObj[3]);///((float)mRenderer.getmWidth()/(float)mRenderer.getmHeight());//newWorldCubePos[0]; //
        //touchedX /= ((float)mRenderer.getmWidth() / (float)mRenderer.getmHeight());
        float touchedY2 = outputCoordObj[1] / outputCoordObj[3];//newWorldCubePos[1]; //
        float touchedZ2 = outputCoordObj[2] / outputCoordObj[3];//newWorldCubePos[2]; //
    */
        //mRenderer.setTouchedPoint(touchedX, touchedY, touchedZ, outputCoordObj, fW, fH /*,  fW, fH */);

        float[] touchedCoords = new float[2];
        touchedCoords[0] = e.getX();
        touchedCoords[1] = e.getY();

        mRenderer.SetWorldTouchCoords(touchedCoords);
        return true;
    }
    


    

    /** Called when the user clicks the Send button
     *  Wait until the data reply packet is full before parsing it */
	/*public void sendPacket(View view) {
		// send to www.weakentroll.com port 60001

	
	String hostName = "www.weakentroll.com";
    int portNumber = 60001;

    try (
        Socket echoSocket = new Socket(hostName, portNumber);
        PrintWriter out =
            new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(echoSocket.getInputStream()));
        BufferedReader stdIn =
            new BufferedReader(
                new InputStreamReader(System.in))
    ) {
        String userInput;
        //while ((userInput = stdIn.readLine()) != null) {
            //out.println(userInput);
        serverMsg = in.readLine(); // Print the reply readline size to test the max bytes each reply can send and if its enough to execute next code step
            System.out.println("echo: " + serverMsg);
        //}
    } catch (UnknownHostException e) {
        System.err.println("Don't know about host " + hostName);
        System.exit(1);
    } catch (IOException e) {
        System.err.println("Couldn't get I/O for the connection to " +
            hostName);
        System.exit(1);
    } 
	
	}*/

    public MyGLRenderer getmRenderer() {
        return mRenderer;
    }
}
