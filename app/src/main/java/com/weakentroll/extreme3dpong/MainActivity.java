package com.weakentroll.extreme3dpong;

/***********************************************
 * Created by: Ryan Baldwin
 * The main android layout code will control the game menu and setup the opengl surface view
 *
 * Thanks to: learnopengles.net lessons for providing initial opengl rendering codebase.
 *
 *
 **********************************************/

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;


public class MainActivity extends AppCompatActivity
{
	/** Hold a reference to our GLSurfaceView */
	private MyGLSurfaceView mGLSurfaceView;

    private static final int REQUEST_ENABLE_BT = 1;
	private String serverMsg;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        ArrayAdapter mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        /////mGLSurfaceView = new MyGLSurfaceView(this); // dont need this in main activity since the layout is now using multiple items

        /////setContentView(mGLSurfaceView);

		// Check if the system supports OpenGL ES 2.0.
		//final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		//final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		//final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		/*if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);

			// Set the renderer to our demo renderer, defined below.
			mGLSurfaceView.setRenderer(new MyGLRenderer(this));
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}*/

        setContentView(R.layout.activity_main);
        LinearLayout linearView = (LinearLayout) findViewById(R.id.my_linear_layout);
        //FrameLayout frameLayout = (FrameLayout) findViewById(R.id.container);
        //frameLayout.setContentView
        mGLSurfaceView = new MyGLSurfaceView(this);
        ////GLSurfaceView s = new GLSurfaceView(this);
        //s.setRenderer(myGLRenderer);

        //to add the view with your own parameters
        linearView.addView(mGLSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ////setContentView(mGLSurfaceView);

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);
	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

    public void quit(View view) {
        finish();
        System.exit(0);
    }

    // TODO: The socket class cant be used in the main thread where the android UI resides, so it needs to move it into a new thread
	/** Called when the user clicks the Send button
	 *  Wait until the data reply packet is full before parsing it */
	public void sendPacket(View view) {
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
	}
}