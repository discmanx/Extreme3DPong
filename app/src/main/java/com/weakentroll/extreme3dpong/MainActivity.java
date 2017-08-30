package com.weakentroll.extreme3dpong;

/***********************************************
 * Created by: Ryan Baldwin
 * The main android layout code will control the game menu and setup the opengl surface view
 *
 * Thanks to: learnopengles.net lessons for providing initial opengl rendering codebase.
 *
 *
 **********************************************/

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.weakentroll.extreme3dpong.R.id.dyn_layout;
import static com.weakentroll.extreme3dpong.R.id.multiPlayer;
import static com.weakentroll.extreme3dpong.R.id.singlePlayer;


public class MainActivity extends AppCompatActivity
{
	/** Hold a reference to our GLSurfaceView */
	private MyGLSurfaceView mGLSurfaceView;

    private static final int REQUEST_ENABLE_BT = 1;

	private Socket echoSocket;
	private static final String hostName = "www.weakentroll.com";
	private static final int portNumber = 9997;
	private String serverMsg;
    private static NetClient nc;


	LooperThread mLooperThread;

	private static class LooperThread extends Thread {

		public Handler mHandler;

		public void run() {

			System.out.println("reached thread run() 1");
			//try {
			nc = new NetClient(hostName, portNumber, null); //mac address maybe not for you

			Looper.prepare();
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					if(msg.what == 0) {
						doLongRunningOperation(msg);
					}
				}
			};
			Looper.loop();
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        // hideSystemUI();

		mLooperThread = new LooperThread();
		mLooperThread.start();

		/* TODO: enable bluetooth
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
        }*/

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
        ////final LinearLayout linearView = (LinearLayout) findViewById(R.id.my_linear_layout);
		//linearView.setOrientation(LinearLayout.);
		//linearView.setGravity(Gravity.CENTER);
/*
		final android.content.res.TypedArray styledAttributes = /*getContext().//  getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
		// this is for height of action toolbar

		// get height of quit button layout

		styledAttributes.recycle();

		final Button tv = (Button)findViewById(R.id.CloseProgram);
		int buttonHeight = tv.getHeight();
		ViewTreeObserver vto = tv.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//vto.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int width  = tv.getMeasuredWidth();
				int height = tv.getMeasuredHeight();

				ViewGroup.MarginLayoutParams vlp = (android.view.ViewGroup.MarginLayoutParams) tv.getLayoutParams();
				int btnsize =tv.getMeasuredHeight()+vlp.topMargin;

				int actualbuttonheight = tv.getHeight();

				ViewTreeObserver obs = tv.getViewTreeObserver();
				obs.removeOnGlobalLayoutListener(this);

			}
		});
*/
        //FrameLayout frameLayout = (FrameLayout) findViewById(R.id.container);
        //frameLayout.setContentView
        /////mGLSurfaceView = new MyGLSurfaceView(this);
		mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.glSurfaceViewID);
        ////GLSurfaceView s = new GLSurfaceView(this);
        //s.setRenderer(myGLRenderer);

        //to add the view with your own parameters
        ////linearView.addView(mGLSurfaceView,  new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		/*LinearLayout ll = new LinearLayout(this);
		Button newGame = new Button(this);
		newGame.setText("New Game");
		ll.addView(newGame);
		Button loadGame = new Button(this);
		loadGame.setText("Load Game");
		ll.addView(loadGame);
		Button highScores = new Button(this);
		highScores.setText("High Scores");
		ll.addView(highScores);
		Button exitGame = new Button(this);
		exitGame.setText("Exit Extreme 3D Pong");
		ll.addView(exitGame);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(/*Gravity.CENTER_HORIZONTAL |// / Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		this.addContentView(ll,
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
*/
		/*ViewTreeObserver vto2 = mGLSurfaceView.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//vto.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int width  = mGLSurfaceView.getMeasuredWidth();
				int height = mGLSurfaceView.getMeasuredHeight();

				ViewGroup.MarginLayoutParams vlp = (android.view.ViewGroup.MarginLayoutParams) mGLSurfaceView.getLayoutParams();
				int btnsize = mGLSurfaceView.getMeasuredHeight()+vlp.topMargin;

				int actualsurfaceviewheight = mGLSurfaceView.getHeight();

				ViewTreeObserver obs = mGLSurfaceView.getViewTreeObserver();
				obs.removeOnGlobalLayoutListener(this);

			}
		}); */

		/*ViewTreeObserver vto3 = linearView.getViewTreeObserver();
		vto3.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//vto.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int width  = linearView.getMeasuredWidth();
				int height = linearView.getMeasuredHeight();

				ViewGroup.MarginLayoutParams vlp = (android.view.ViewGroup.MarginLayoutParams) linearView.getLayoutParams();
				int btnsize = linearView.getMeasuredHeight()+vlp.topMargin;

				int actuallinearviewheight = linearView.getHeight();

				ViewTreeObserver obs = linearView.getViewTreeObserver();
				obs.removeOnGlobalLayoutListener(this);

			}
		}); */
        ////setContentView(mGLSurfaceView);

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);

		///new Thread(new ClientThread()).start();

	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.glSurfaceViewID);
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.glSurfaceViewID);
		mGLSurfaceView.onPause();
	}

    public void exitGame(View view) {
        finish();
        System.exit(0);
    }

	public void newGame(View view) {


        final Button singlePlayerButton = (Button)findViewById(R.id.singlePlayer);
        final Button multiPlayerButton = (Button)findViewById(R.id.multiPlayer);
        final Button newGameButton = (Button)findViewById(R.id.newGame);
        final Button loadGameButton = (Button)findViewById(R.id.loadGame);
        final Button highScoresButton = (Button)findViewById(R.id.highScores);
        final Button exitGameButton = (Button)findViewById(R.id.exitGame);

        final LinearLayout dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);

       // singlePlayerButton.setVisibility(View.INVISIBLE);
        //multiPlayerButton.setVisibility(View.INVISIBLE);
        //highScoresButton.setVisibility(View.INVISIBLE);
        dyn_layout.removeAllViews();

        dyn_layout.setOrientation(LinearLayout.HORIZONTAL);
        dyn_layout.setGravity(/*Gravity.CENTER |*/ Gravity.TOP );

        Button openMenuButton = new Button(this);
        openMenuButton.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        openMenuButton.setText("Menu");
        openMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //final LinearLayout dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);

                dyn_layout.removeAllViews();

                /*final Button singlePlayerButton = (Button)findViewById(R.id.singlePlayer);
                final Button multiPlayerButton = (Button)findViewById(R.id.multiPlayer);
                final Button newGameButton = (Button)findViewById(R.id.newGame);
                final Button loadGameButton = (Button)findViewById(R.id.loadGame);
                final Button highScoresButton = (Button)findViewById(R.id.highScores);
                final Button exitGameButton = (Button)findViewById(R.id.exitGame); */
                //exitGameButton.setVisibility(View.INVISIBLE);
                // TODO Auto-generated method stub
                dyn_layout.setOrientation(LinearLayout.VERTICAL);
                dyn_layout.setGravity(Gravity.CENTER_VERTICAL );

                dyn_layout.addView(newGameButton);
                dyn_layout.addView(loadGameButton);
                dyn_layout.addView(highScoresButton);
                dyn_layout.addView(exitGameButton);
            }
        });
        openMenuButton.setVisibility(View.VISIBLE);
        dyn_layout.addView(openMenuButton);

		mGLSurfaceView.getmRenderer().setPlayerScore(0);
		mGLSurfaceView.getmRenderer().setOpponentScore(0);
		mGLSurfaceView.getmRenderer().gameMachine.Advance("Pong");
	}

	public void singlePlayer(View view) {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;

        Log.i("*** Value :: ", "StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight);


        final Button singlePlayerButton = (Button)findViewById(R.id.singlePlayer);
        final Button multiPlayerButton = (Button)findViewById(R.id.multiPlayer);
        final Button newGameButton = (Button)findViewById(R.id.newGame);
        final Button loadGameButton = (Button)findViewById(R.id.loadGame);
        final Button highScoresButton = (Button)findViewById(R.id.highScores);
        final Button exitGameButton = (Button)findViewById(R.id.exitGame);
		final Button backMenuButton = (Button)findViewById(R.id.backMenu);

        final LinearLayout dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);

        dyn_layout.removeView(singlePlayerButton);
        dyn_layout.removeView(multiPlayerButton);

		newGameButton.setVisibility(View.VISIBLE);
		loadGameButton.setVisibility(View.VISIBLE);
		backMenuButton.setVisibility(View.VISIBLE);

		//dyn_layout.addView(newGameButton);
		//dyn_layout.addView(loadGameButton);
        //dyn_layout.addView(highScoresButton);
        //dyn_layout.addView(backMenuButton);

    }

    public void multiPlayer(View view) {


    }

	public void backMenu(View view) {

		final LinearLayout dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);

		final Button newGameButton = (Button)findViewById(R.id.newGame);
		final Button loadGameButton = (Button)findViewById(R.id.loadGame);
		final Button highScoresButton = (Button)findViewById(R.id.highScores);
		final Button exitGameButton = (Button)findViewById(R.id.exitGame);

		dyn_layout.removeAllViews();

		dyn_layout.addView(newGameButton);
		dyn_layout.addView(loadGameButton);
		dyn_layout.addView(highScoresButton);
		dyn_layout.addView(exitGameButton);

	}

	public void highScores(View view) {
		// send to www.weakentroll.com port 60001

		int testport = echoSocket.getLocalPort();

		try (
				// = new Socket(hostName, portNumber);
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
			String username = new String("discmanx");
			JSONObject gameData = new JSONObject();

			try {
				gameData.put("username", username);
				gameData.put("playerScore", mGLSurfaceView.getmRenderer().getPlayerScore());
				gameData.put("opponentScore", mGLSurfaceView.getmRenderer().getOpponentScore());

			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

			//while ((userInput = stdIn.readLine()) != null) {
			out.println(gameData);
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


    // TODO: The socket class cant be used in the main thread where the android UI resides, so it needs to move it into a new thread
	/** Called when the user clicks the Send button
	 *  Wait until the data reply packet is full before parsing it */
	public void saveGame(View view) {
		// send to www.weakentroll.com port 60001

		if (mLooperThread.mHandler != null) {
			Message msg = mLooperThread.mHandler.obtainMessage(0);

			String userInput;
			String username = new String("discmanx");
			JSONObject gameData = new JSONObject();

			try {
				gameData.put("username", username);
				gameData.put("playerScore", mGLSurfaceView.getmRenderer().getPlayerScore());
				gameData.put("opponentScore", mGLSurfaceView.getmRenderer().getOpponentScore());

			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

            msg.obj = gameData;
			mLooperThread.mHandler.sendMessage(msg);
		}

		/*
        int testport = echoSocket.getLocalPort();

		////try (
				// = new Socket(hostName, portNumber);
				PrintWriter out =
						new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(echoSocket.getInputStream()));
				BufferedReader stdIn =
						new BufferedReader(
								new InputStreamReader(System.in))
		) {////
			String userInput;
			String username = new String("discmanx");
			JSONObject gameData = new JSONObject();

			try {
				gameData.put("username", username);
				gameData.put("playerScore", mGLSurfaceView.getmRenderer().getPlayerScore());
				gameData.put("opponentScore", mGLSurfaceView.getmRenderer().getOpponentScore());

			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

			//while ((userInput = stdIn.readLine()) != null) {
			System.out.println("testpoint 1");
            nc.sendDataWithString(gameData.toString());
			//////out.println(gameData);
            System.out.println("testpoint 2");
            String r = nc.receiveDataFromServer();

            //////serverMsg = in.readLine(); // Print the reply readline size to test the max bytes each reply can send and if its enough to execute next code step
			System.out.println("testpoint 3 echo: " + serverMsg);
			//}
		/*} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
					hostName);
			System.exit(1);
		}*/
	}

	private static void doLongRunningOperation(Message msg) {
		// Add long running operation here.
		nc.sendDataWithString(msg.obj.toString());
		String r = nc.receiveDataFromServer();
	}

	class ClientThread implements Runnable {

		//System.out.println("reached thread run() 0");

		@Override
		public void run() {

			System.out.println("reached thread run() 1");
			//try {
                nc = new NetClient(hostName, portNumber, null); //mac address not used

                //InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
				///System.out.println("reached thread run() 2");
				///echoSocket = new Socket(hostName, portNumber);
				///System.out.println("reached thread run() 3");

			/*} catch (UnknownHostException e1) {
				System.out.println("reached thread run() 4");
				e1.printStackTrace();
				Log.e("YOUR_APP_LOG_TAG", "I got an UnknownHostException error", e1);
			} catch (IOException e1) {
				System.out.println("reached thread run() 5");
				e1.printStackTrace();
				Log.e("YOUR_APP_LOG_TAG", "I got an IOException error", e1);
			}*/
		}
	}

	protected void onDestroy() {
        super.onDestroy();
		mLooperThread.mHandler.getLooper().quit();
	}

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public String ping(String url) {
        String str = "";
        try {
            Process process = Runtime.getRuntime().exec(
                    "/system/bin/ping -c 8 " + url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0)
                output.append(buffer, 0, i);
            reader.close();

            // body.append(output.toString()+"\n");
            str = output.toString();
            // Log.d(TAG, str);
        } catch (IOException e) {
            // body.append("Error\n");
            e.printStackTrace();
        }
        return str;
    }
}