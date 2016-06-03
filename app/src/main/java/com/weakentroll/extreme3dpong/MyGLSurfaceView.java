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
		final float eyeX = 0.0f;
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
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		Matrix.setLookAtM(mTempViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
				lookZ, upX, upY, upZ);
		
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

		Matrix.frustumM(mTempProjectionMatrix, 0, left, right, bottom, top, near,
				far);
		
		newWorldCubePos[3] = 1.0f;
		
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -2.0f);
	
		Matrix.multiplyMM(mTempMVPMatrix, 0, mTempViewMatrix, 0, mModelMatrix, 0);
		//Matrix.multiplyMM(mTempMVPMatrix, 0, mTempProjectionMatrix, 0, mTempMVPMatrix, 0);

        float x = e.getX();
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
		

        	
        int status = GLU.gluUnProject(x, y, 0.0f, mTempMVPMatrix, 0, mRenderer.getmProjectionMatrix(), 
        			  0, convertedViewMatrix, 0, outputCoordObj, 0);
		
		
		
		// Initialize auxiliary variables.
		/*float[] worldPos = new float[4]; //Vec2 worldPos = new Vec2();

	       // SCREEN height & width (ej: 320 x 480)
	       float screenW = mRenderer.getmWidth();
	       float screenH = mRenderer.getmHeight();

	       // Auxiliary matrix and vectors
	       // to deal with ogl.
	       float[] invertedMatrix, transformMatrix,
	           normalizedInPoint, outPoint;
	       invertedMatrix = new float[16];
	       transformMatrix = new float[16];
	       normalizedInPoint = new float[4];
	       outPoint = new float[4];

	       // Invert y coordinate, as android uses
	       // top-left, and ogl bottom-left.
	       int oglTouchY = (int) (screenH - e.getY());

	       /* Transform the screen point to clip
	       space in ogl (-1,1)      
	       normalizedInPoint[0] =
	        (float) ((e.getX()) * 2.0f / screenW - 1.0);
	       normalizedInPoint[1] =
	        (float) ((oglTouchY) * 2.0f / screenH - 1.0);
	       normalizedInPoint[2] = - 1.0f;
	       normalizedInPoint[3] = 1.0f;

	       /* Obtain the transform matrix and
	       then the inverse. 
	       //Print("Proj", getCurrentProjection(gl));
	       //Print("Model", getCurrentModelView(gl));
	       Matrix.multiplyMM(transformMatrix, 0, mRenderer.getmProjectionMatrix(), 0, mTempMVPMatrix, 0);
	       Matrix.invertM(invertedMatrix, 0, transformMatrix, 0);       

	       /* Apply the inverse to the point
	       in clip space 
	       Matrix.multiplyMV(
	           outPoint, 0,
	           invertedMatrix, 0,
	           normalizedInPoint, 0);

	       if (outPoint[3] == 0.0)
	       {
	    	   int error1 = 1;
	    	   error1++;
	           // Avoid /0 error.
	           //Log.e("World coords", "ERROR!");
	           //return worldPos;
	       }

	       // Divide by the 3rd component to find
	       // out the real position.
	       worldPos[0] =  outPoint[0] / outPoint[3];
	       worldPos[1] =  outPoint[1] / outPoint[3];
	       worldPos[2] =  outPoint[2] / outPoint[3];

	       //return worldPos;    */
		
        
        int a1, b1;
        /*if (status == GL10.GL_TRUE)
        	a1 = 1;
        else if (status == GL10.GL_FALSE)
        	b1 = 1;*/
        
        // opengl prism color!
        
        // power of function derivative formula
        
        //outputCoordObj[0] /= ((float)mRenderer.getmWidth() / (float)mRenderer.getmHeight());
        float touchedX = (outputCoordObj[0] / outputCoordObj[3])/((float)mRenderer.getmWidth()/(float)mRenderer.getmHeight());//newWorldCubePos[0]; //
        //touchedX /= ((float)mRenderer.getmWidth() / (float)mRenderer.getmHeight());
        float touchedY = outputCoordObj[1] / outputCoordObj[3];//newWorldCubePos[1]; //
        float touchedZ = outputCoordObj[2] / outputCoordObj[3];//newWorldCubePos[2]; //
        
        //touchedX = touchedX * (mRenderer.getmWidth()/mRenderer.getmHeight());
        
        mRenderer.setTouchedPoint(touchedX, touchedY, touchedZ, outputCoordObj);

        System.out.printf("Touched screen coords->world coords are: %f, %f, %f", touchedX, touchedY, touchedZ);
        
        
        // add shit here
        /*mRenderer.setTextPosX(newX);
        mRenderer.setTextPosY(newY);
        mRenderer.setTextPosZ(newZ);
       
        
        if ( (newX > -0.5f) && (newX < 0.5f) ) {
        	if ( (newY > -0.5f) && (newY < 0.5f) ) {
        		mRenderer.getmSquare().color[1] = (float)Math.random();
        	}
        }*/
        
        
        
        
        
        
        /*if ( (x > ( getWidth()/2)-getWidth()/4) && ( x < ( getWidth()/2)+getWidth()/4) ) {
        	if ( (y > ( getHeight()/2)-getHeight()/4) && ( y < ( getHeight()/2)+getHeight()/4) ) {
        		mRenderer.getmSquare().color[1] = (float)Math.random();
        		sendPacket(this);
        	}
        }*/

        /****************8switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                
                // Calculus for firing a ball
                double c1, c2;
                
                // if magnetic sensor is closed, move the square to left..
                if (magSwitch == 1) {
                	// left square position = 20;
                	
                }
                else {
                	// square pos to right = 250;
                }
                

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                        ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                requestRender();
        }*/

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
    
    /**
     * Calculates the transform from screen coordinate
     * system to world coordinate system coordinates
     * for a specific point, given a camera position.
     *
     * @param touch Vec2 point of screen touch, the
       actual position on physical screen (ej: 160, 240)
     * @param cam camera object with x,y,z of the
       camera and screenWidth and screenHeight of
       the device.
     * @return position in WCS.
     */
    /*public Vec2 GetWorldCoords( Vec2 touch, Camera cam)
    {  
        // Initialize auxiliary variables.
        Vec2 worldPos = new Vec2();

        // SCREEN height & width (ej: 320 x 480)
        float screenW = cam.GetScreenWidth();
        float screenH = cam.GetScreenHeight();

        // Auxiliary matrix and vectors
        // to deal with ogl.
        float[] invertedMatrix, transformMatrix,
            normalizedInPoint, outPoint;
        invertedMatrix = new float[16];
        transformMatrix = new float[16];
        normalizedInPoint = new float[4];
        outPoint = new float[4];

        // Invert y coordinate, as android uses
        // top-left, and ogl bottom-left.
        int oglTouchY = (int) (screenH - touch.Y());

        /* Transform the screen point to clip
        space in ogl (-1,1) */       
        /*normalizedInPoint[0] =
         (float) ((touch.X()) * 2.0f / screenW - 1.0);
        normalizedInPoint[1] =
         (float) ((oglTouchY) * 2.0f / screenH - 1.0);
        normalizedInPoint[2] = - 1.0f;
        normalizedInPoint[3] = 1.0f;

        /* Obtain the transform matrix and
        then the inverse. */
        
        
        
        /*Print("Proj", getCurrentProjection(gl));
        Print("Model", getCurrentModelView(gl));
        Matrix.multiplyMM(
            transformMatrix, 0,
            getCurrentProjection(mRenderer.unused), 0,
            getCurrentModelView(gl), 0);
        Matrix.invertM(invertedMatrix, 0,
            transformMatrix, 0);       

        /* Apply the inverse to the point
        in clip space */
        /*Matrix.multiplyMV(
            outPoint, 0,
            invertedMatrix, 0,
            normalizedInPoint, 0);

        if (outPoint[3] == 0.0)
        {
            // Avoid /0 error.
            //Log.e("World coords", "ERROR!");
            return worldPos;
        }

        // Divide by the 3rd component to find
        // out the real position.
        worldPos.Set(
            outPoint[0] / outPoint[3],
            outPoint[1] / outPoint[3]);

        return worldPos;       
    }*/
    

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

    
}
