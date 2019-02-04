package com.weakentroll.extreme3dpong;

/***********************************************
 * Created by: Ryan Baldwin
 * The main android layout code will control the game menu and setup the opengl surface view
 *
 * Thanks to: learnopengles.net lessons for providing initial opengl rendering codebase.
 *
 *
 **********************************************/

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.weakentroll.extreme3dpong.R.id.dyn_layout;
import static com.weakentroll.extreme3dpong.R.id.multiPlayer;
import static com.weakentroll.extreme3dpong.R.id.multiPlayerListView;
import static com.weakentroll.extreme3dpong.R.id.multiPlayer_layout;
import static com.weakentroll.extreme3dpong.R.id.singlePlayer;


public class MainActivity extends AppCompatActivity
{
	/** Hold a reference to our GLSurfaceView */
	private MyGLSurfaceView mGLSurfaceView;

    private static final int REQUEST_ENABLE_BT = 1;

	private Socket echoSocket;
	private static final String hostName = "www.weakentroll.com";
	private static final int portNumber = 60001;
	private String serverMsg;
    private static NetClient nc;
    private Player player;

	private CustomListAdapter customListAdapter;
	//static ListView multiPlayerListView = null;// = (ListView)findViewById(R.id.multiPlayerList);
	static ArrayList<MultiPlayer> multiPlayerList = new ArrayList<MultiPlayer>();

	// View variables
	LinearLayout dyn_layout; //= (LinearLayout)findViewById(R.id.dyn_layout);
	Button singlePlayerButton;// = (Button)findViewById(R.id.singlePlayer);
	Button newGameButton;// = (Button)findViewById(R.id.newGame);
	Button saveGameButton;// = (Button)findViewById(R.id.saveGame);
	Button loadGameButton;// = (Button)findViewById(R.id.loadGame);
	Button multiPlayerButton;// = (Button)findViewById(R.id.multiPlayer);
	ListView multiPlayerListView;// = (ListView)findViewById(R.id.multiPlayerListView);
	LinearLayout multiPlayer_layout;
	EditText multiplayer_username_input;
	EditText multiplayer_password_input;
	Button multiplayer_login_button;
	LinearLayout multiPlayerList_layout;// = (LinearLayout)findViewById(R.id.multiPlayer_layout);

	LinearLayout createMultiPlayerAccount_layout;
	EditText create_multiplayer_username_input;
	EditText create_multiplayer_password_input;
	EditText create_multiplayer_password_confirm_input;
	Button register_multiplayer_account_button;

	Button highScoresButton;// = (Button)findViewById(R.id.highScores);
	Button exitGameButton;// = (Button)findViewById(R.id.exitGame);
	Button backMenuButton;// = (Button)findViewById(R.id.backMenu);
	LinearLayout display_msg_layout;// = (LinearLayout)findViewById(R.id.display_msg_layout);
	TextView display_msg_textview;// = (TextView)findViewById(R.id.display_msg_textview);
	Button msg_button;// = (Button)findViewById(R.id.msg_button);
	TextView or_textview;
	Button createaccount_button;

	private Handler mHandler;

	LooperThread mLooperThread;



	private class LooperThread extends Thread {

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
					if (msg.what == 1) {
						handleTouchPoints();
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

		mHandler = new Handler(Looper.getMainLooper()) {
			/*
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
			@Override
			public void handleMessage(Message inputMessage) {

				// Gets the image task from the incoming Message object.
				//PhotoTask photoTask = (PhotoTask) inputMessage.obj;

				switch (inputMessage.what) {
					case 1: //ArrayAdapter<MultiPlayer> adapter = new ArrayAdapter<MultiPlayer>(MainActivity.this,
							//android.R.layout.simple_list_item_1, multiPlayerList);
						customListAdapter = new CustomListAdapter(getApplicationContext(), mLooperThread.mHandler, multiPlayerList, player);
						multiPlayerListView.setAdapter(customListAdapter);

						// Click on ListView Row:
						multiPlayerListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
							{
								MultiPlayer mp = (MultiPlayer)multiPlayerListView.getItemAtPosition(position);
								// Here i want have values of the clicked row,- like the name and/or id...,- but i get the following:
								// Output o.toString(): com.example.customlistview.MainActivity$Person@41252310
								Log.i("TEST", mp.toString());

								long viewId = arg1.getId();

								if (viewId == R.id.challengeMultiplayer) {
									System.out.println( "Button 1 clicked");

									// get current item to be displayed
									MainActivity.MultiPlayer currentItem = (MainActivity.MultiPlayer) customListAdapter.getItem(position);
									if (mLooperThread.mHandler != null) {
										Message msg = mLooperThread.mHandler.obtainMessage(0);

										String userInput;
										String fromUsername = player.getUsername();
										String toUsername = currentItem.multiUsername;

										JSONObject gameData = new JSONObject();

										try {
											gameData.put("msgType", "challengeMultiPlayer");
											gameData.put("fromUsername", fromUsername);
											gameData.put("toUsername", toUsername);

											// get objkect.usernamegameData.put("username", username);
										} catch (JSONException ex) {
											ex.printStackTrace();
										}

										msg.obj = gameData;
										mLooperThread.mHandler.sendMessage(msg);
									}
								} else {
									System.out.println( "Listview clicked");
								}

							}
						});

						for (int i = 0; i < dyn_layout.getChildCount(); i++) {
							View v = dyn_layout.getChildAt(i);
							v.setVisibility(View.GONE);
						}

						multiPlayerList_layout.setVisibility(View.VISIBLE);
						backMenuButton.setVisibility(View.VISIBLE);
						multiPlayerListView.setVisibility(View.VISIBLE);
						//mGLSurfaceView.getmRenderer().gameMachine.Advance("MultiPlayerMenu");
						break;
					case 2: displayMessage("Error connecting to server.", "Ok", "current");
						break;
					case 3: displayMessage("Invalid Password.", "Ok", "current");
						break;
					case 4: displayMessage("Username already created. Please choose another.", "Ok" , "current");
						break;
					case 5: displayMessage("Please confirm password.", "Ok", "current");
						break;
					default:
						// Otherwise, calls the super method
						super.handleMessage(inputMessage);
				}
			}
		};

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

		dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);
		singlePlayerButton = (Button)findViewById(R.id.singlePlayer);
		newGameButton = (Button)findViewById(R.id.newGame);
		saveGameButton = (Button)findViewById(R.id.saveGame);
		loadGameButton = (Button)findViewById(R.id.loadGame);
		multiPlayerButton = (Button)findViewById(R.id.multiPlayer);
		multiPlayerListView = (ListView)findViewById(R.id.multiPlayerListView);
		multiPlayer_layout = (LinearLayout)findViewById(R.id.multiPlayer_layout);
		multiplayer_username_input = (EditText)findViewById(R.id.multiplayer_username_input);
		multiplayer_login_button = (Button)findViewById(R.id.multiplayer_login_button);
		multiplayer_password_input = (EditText)findViewById(R.id.multiplayer_password_input);
		multiplayer_password_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					multiplayer_login_button.performClick();
					handled = true;
				}
				return handled;
			}
		});
		multiPlayerList_layout = (LinearLayout)findViewById(R.id.multiPlayerList_layout);
		highScoresButton = (Button)findViewById(R.id.highScores);
		exitGameButton = (Button)findViewById(R.id.exitGame);
		backMenuButton = (Button)findViewById(R.id.backMenu);
		display_msg_layout = (LinearLayout)findViewById(R.id.display_msg_layout);
		display_msg_textview = (TextView)findViewById(R.id.display_msg_textview);
		msg_button = (Button)findViewById(R.id.msg_button);
		or_textview = (TextView)findViewById(R.id.or__textview);
		createaccount_button = (Button)findViewById(R.id.createaccount_button);

		createMultiPlayerAccount_layout = (LinearLayout)findViewById(R.id.createMultiPlayerAccount_layout);
		create_multiplayer_username_input = (EditText)findViewById(R.id.create_multiplayer_username_input);
		create_multiplayer_password_input = (EditText)findViewById(R.id.create_multiplayer_password_input);
		create_multiplayer_password_confirm_input = (EditText)findViewById(R.id.create_multiplayer_password_confirm_input);
		register_multiplayer_account_button = (Button)findViewById(R.id.register_multiplayer_account_button);
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

		// might need to use static multiplayerlistview
		//////multiPlayerListView = (ListView)findViewById(R.id.multiPlayerList);




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
		for (int i = 0; i < dyn_layout.getChildCount(); i++) {
			View v = dyn_layout.getChildAt(i);
			v.setVisibility(View.GONE);
		}

        dyn_layout.setOrientation(LinearLayout.HORIZONTAL);
        dyn_layout.setGravity(/*Gravity.CENTER |*/ Gravity.TOP );

        Button openMenuButton = new Button(this);
        openMenuButton.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        openMenuButton.setText("Menu");
        openMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				for (int i = 0; i < dyn_layout.getChildCount(); i++) {
					v = dyn_layout.getChildAt(i);
					v.setVisibility(View.GONE);
				}
                dyn_layout.setOrientation(LinearLayout.VERTICAL);
                dyn_layout.setGravity(Gravity.CENTER_VERTICAL );

				newGameButton.setVisibility(View.VISIBLE);
				saveGameButton.setVisibility(View.VISIBLE);
				loadGameButton.setVisibility(View.VISIBLE);
				exitGameButton.setVisibility(View.VISIBLE);
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

		mGLSurfaceView.getmRenderer().gameMachine.Advance("SinglePlayerMenu");

		for (int i = 0; i < dyn_layout.getChildCount(); i++) {
			View v = dyn_layout.getChildAt(i);
			v.setVisibility(View.GONE);
		}
		newGameButton.setVisibility(View.VISIBLE);
		loadGameButton.setVisibility(View.VISIBLE);
		backMenuButton.setVisibility(View.VISIBLE);
    }

	public void multiPlayer(View view) {
		for (int i = 0; i < dyn_layout.getChildCount(); i++) {
			View v = dyn_layout.getChildAt(i);
			v.setVisibility(View.GONE);
		}

		multiPlayer_layout.setVisibility(View.VISIBLE);
		backMenuButton.setVisibility(View.VISIBLE);
		mGLSurfaceView.getmRenderer().gameMachine.Advance("MultiPlayerMenu");
	}

    public void multiPlayerLogin(View view) {

		if (mLooperThread.mHandler != null) {
			Message msg = mLooperThread.mHandler.obtainMessage(0);

			String userInput;
			String username = multiplayer_username_input.getText().toString(); //new String("discmanx");
			String password = multiplayer_password_input.getText().toString(); //new String("discmanx");

			JSONObject gameData = new JSONObject();

			try {
				gameData.put("msgType", "multiLogin");
				gameData.put("username", username);
				gameData.put("password", password);
			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

			msg.obj = gameData;
			mLooperThread.mHandler.sendMessage(msg);
		}

		JSONObject gameData = new JSONObject();


		AccountManager am = AccountManager.get(this);
		Bundle options = new Bundle();

		/*am.getAuthToken(
				myAccount_,                     // Account retrieved using getAccountsByType()
				"Manage your tasks",            // Auth scope
				options,                        // Authenticator-specific options
				this,                           // Your activity
				new OnTokenAcquired(),          // Callback called when a token is successfully acquired
				new Handler(new OnError()));    // Callback called if an error occurs */

		//dyn_layout.removeAllViews();



	}

	public void createAccount(View view) {
		for (int i = 0; i < dyn_layout.getChildCount(); i++) {
			View v = dyn_layout.getChildAt(i);
			v.setVisibility(View.GONE);
		}

		createMultiPlayerAccount_layout.setVisibility(View.VISIBLE);
		backMenuButton.setVisibility(View.VISIBLE);
		mGLSurfaceView.getmRenderer().gameMachine.Advance("RegisterMultiPlayerMenu");

	}

	public void registerNewAccount(View view) {
		if (mLooperThread.mHandler != null) {
			Message msg = mLooperThread.mHandler.obtainMessage(0);

			String username = create_multiplayer_username_input.getText().toString(); //new String("discmanx");
			String password = create_multiplayer_password_input.getText().toString(); //new String("discmanx");
			String confirm_password = create_multiplayer_password_confirm_input.getText().toString(); //new String("discmanx");

			JSONObject gameData = new JSONObject();

			try {
				gameData.put("msgType", "registerNewMultiAccount");
				gameData.put("username", username);
				gameData.put("password", password);
			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

			msg.obj = gameData;
			mLooperThread.mHandler.sendMessage(msg);
		}
	}

	public void backMenu(View view) {
		for (int i = 0; i < dyn_layout.getChildCount(); i++) {
			View v = dyn_layout.getChildAt(i);
			v.setVisibility(View.GONE);
		}
		// Go back from the current menu state
		switch (mGLSurfaceView.getmRenderer().gameMachine.CurrentState().GetName())
		{
			case "Entry":
				//  Here is the where start menu will go
				break;
			case "SinglePlayerMenu":
				singlePlayerButton.setVisibility(View.VISIBLE);
				multiPlayerButton.setVisibility(View.VISIBLE);
				highScoresButton.setVisibility(View.VISIBLE);
				exitGameButton.setVisibility(View.VISIBLE);
				mGLSurfaceView.getmRenderer().gameMachine.Advance("EntryMenu");
				break;
			case "MultiPlayerMenu":
				singlePlayerButton.setVisibility(View.VISIBLE);
				multiPlayerButton.setVisibility(View.VISIBLE);
				highScoresButton.setVisibility(View.VISIBLE);
				exitGameButton.setVisibility(View.VISIBLE);
				mGLSurfaceView.getmRenderer().gameMachine.Advance("EntryMenu");
				break;
			case "RegisterMultiPlayerMenu":
				multiPlayer_layout.setVisibility(View.VISIBLE);
				backMenuButton.setVisibility(View.VISIBLE);
				mGLSurfaceView.getmRenderer().gameMachine.Advance("MultiPlayerMenu");
				break;
			default:
				singlePlayerButton.setVisibility(View.VISIBLE);
				multiPlayerButton.setVisibility(View.VISIBLE);
				highScoresButton.setVisibility(View.VISIBLE);
				exitGameButton.setVisibility(View.VISIBLE);
				break;
		}
		//newGameButton.setVisibility(View.VISIBLE);
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

	public void challengeMultiplayer(View view) {
		System.out.println("challenge button clicked: ");

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
				gameData.put("msgType", "saveGame");
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

	/*private class UpdateMultiListView extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			return doSomeWork(url);
		}
	}*/

	private void doLongRunningOperation(Message msg) {
		// Add long running operation here.

		// If no connection to server
		if (nc.sendDataWithString(msg.obj.toString()) == false)
		{
			Message msg1 = new Message();
			msg1.what = 2;
			msg1.setTarget(mHandler);
			msg1.sendToTarget();
			//displayMessage("Error connecting to server.", "Ok", "main");
		}
		else {
			String r = nc.receiveDataFromServer();

			JSONObject serverData;
			try {
				serverData = new JSONObject(r);

				switch (serverData.getString("msgType")) {
					case "multiPlayerList":
						int length = serverData.getJSONArray("multiPlayers").length();

                        JSONObject json_player = serverData.getJSONObject("player");
                        player = new Player(json_player.getString("username"), null, json_player.getInt("id"), json_player.getInt("score"), 0.0f, 0.0f, 0.0f, 0);
                        player.setUsername(json_player.getString("username"));
                        player.setPlayerid(json_player.getInt("id"));
                        player.setScore(json_player.getInt("score"));
						JSONArray multiPlayers = serverData.getJSONArray("multiPlayers");

						for (int i = 0; i < multiPlayers.length(); i++) {
							JSONObject json_data = multiPlayers.getJSONObject(i);

							MultiPlayer mp = new MultiPlayer(json_data.getString("username"), json_data.getInt("id"), json_data.getInt("score"));
							multiPlayerList.add(mp);
						}
						//JSONArray player = /*(JSONObject)*/ multiPlayers.getJSONArray("testList");//   getString (0);

						String name = "test";//player.getString("username");
						//int score = player.getInt("score");
						//int id = player.getInt("id");
						// Display the multiplayer list layout
						Message msg1 = new Message();
						msg1.what = 1;
						msg1.setTarget(mHandler);
						msg1.sendToTarget();
						break;
					case "invalidPassword": Message msg2 = new Message();
						msg2.what = 3;
						msg2.setTarget(mHandler);
						msg2.sendToTarget();
						//displayMessage("Invalid Password.", "Ok", "main");
						break;
					case "invalidUsernameRegisterMultiAccount": Message msg3 = new Message();
						msg3.what = 4;
						msg3.setTarget(mHandler);
						msg3.sendToTarget();
						//displayMessage("Invalid Password.", "Ok", "main");
						break;
					default:
						break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
	/*
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
			}
		}
	} */

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

	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
		@Override
		public void run(AccountManagerFuture<Bundle> result) {
			// Get the result of the operation from the AccountManagerFuture.
			/*Bundle bundle = result.getResult();

			// The token is a named value in the bundle. The name of the value
			// is stored in the constant AccountManager.KEY_AUTHTOKEN.
			token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        ...*/
		}
	}

	static class MultiPlayer {
		String multiUsername;
		int multiId;
		int multiScore;

		MultiPlayer(String username, int id, int score) {
			multiUsername = username;
			multiId = id;
			multiScore = score;

		}

		public String getMultiUsername() {
			return multiUsername;
		}

		public int getMultiId() {
			return multiId;
		}

		public int getMultiScore() {
			return multiScore;
		}
	}

	public void displayMessage(String msgText, String msgButtonText, final String menuLevel) {
		display_msg_textview.setText(msgText);
		msg_button.setText(msgButtonText);

		msg_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// menu to navigate to
				switch (menuLevel) {

					case "current": display_msg_layout.setVisibility(View.GONE);
						return;
					case "back": display_msg_layout.setVisibility(View.GONE); //dyn_layout.removeView(display_msg_layout);
						backMenuButton.performClick();
						return;
				}
			}
		});
		display_msg_layout.setVisibility(View.VISIBLE);

	}

	public void handleTouchPoints() {

	}


}