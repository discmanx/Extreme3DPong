package com.weakentroll.extreme3dpong;

/***********************************************
 * Created by: Ryan Baldwin
 * The main android layout code will control the game menu and setup the opengl surface view
 *
 * Thanks to: learnopengles.net lessons for providing initial opengl rendering codebase.
 *
 *
 **********************************************/

import android.Manifest;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.weakentroll.extreme3dpong.R.id.dyn_layout;
import static com.weakentroll.extreme3dpong.R.id.glSurfaceViewID;
import static com.weakentroll.extreme3dpong.R.id.multiPlayer;
import static com.weakentroll.extreme3dpong.R.id.multiPlayerListView;
import static com.weakentroll.extreme3dpong.R.id.multiPlayer_layout;
import static com.weakentroll.extreme3dpong.R.id.my_relative_layout;
import static com.weakentroll.extreme3dpong.R.id.singlePlayer;
import static com.weakentroll.extreme3dpong.R.id.weakentroll_splashview;


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
    private Thread ncThread;
    private Player player;
    private Player opponent;
	private final static Semaphore available = new Semaphore(1, true);

	// Media
    private MediaPlayer mediaplayer = null;
    private MediaRecorder mediarecorder = null;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mediafileName = null;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private CustomListAdapter customListAdapter;
	//static ListView multiPlayerListView = null;// = (ListView)findViewById(R.id.multiPlayerList);
	static ArrayList<MultiPlayer> multiPlayerList = new ArrayList<MultiPlayer>();
	boolean activeMatch = false;
	boolean debugFlag = false;

	// View variables
	RelativeLayout parentRelativeLayout;
	LinearLayout dyn_layout; //= (LinearLayout)findViewById(R.id.dyn_layout);
	ImageView weakentroll_splash;
	Button singlePlayerButton;// = (Button)findViewById(R.id.singlePlayer);
	Button newGameButton;// = (Button)findViewById(R.id.newGame);
	Button saveGameButton;// = (Button)findViewById(R.id.saveGame);
	Button loadGameButton;// = (Button)findViewById(R.id.loadGame);
	Button multiPlayerButton;// = (Button)findViewById(R.id.multiPlayer);
	Button debugButton;
	Button sendPlayerMsgButton;
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
	LinearLayout display_challenge_layout;
	Button msg_button;// = (Button)findViewById(R.id.msg_button);
	Button challenge_yes_button;
	Button challenge_no_button;
	TextView display_challenge_textview;
	TextView or_textview;
    TextView countdown_textview;

    Button createaccount_button;

	public static Handler mHandler;

	LooperThread mLooperThread;

	private class LooperThread extends Thread {

		public Handler mHandler;

		public void run() {

			System.out.println("reached thread run() 1");
			//try {
			nc = new NetClient(hostName, portNumber, null, available); //mac address maybe not for you
            ncThread = new Thread(nc);
            ncThread.start();

			Looper.prepare();
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					if(msg.what == 0) { // this is where we send msgs to the server
						doLongRunningOperation(msg);
					}
					if (msg.what == 1) {
						float[] data = (float[])msg.obj;
						player.setLocation(data[0], -5.0f, -20.0f);
					}
					if (msg.what == 2) {
						/* deprecated
						if (nc.connectWithServer() == false)
						{
							Message msg1 = new Message();
							msg1.what = 2;
							msg1.setTarget(mHandler);
							msg1.sendToTarget();
							return;
							//displayMessage("Error connecting to server.", "Ok", "main");
						}
						*/
					}
					if(msg.what == 3) { // this is where we send msgs to the server
						nc.disConnectWithServer();
					}
					/*if(msg.what == 6) { // this is where we first receive msg from the server - it is checked repeatedly if theres one waiting
						System.out.println(" msg.what == 6");
						try {
							available.acquire();
							System.out.println("mainactivity dolongrunningoperation() available.acquire()");

							System.out.println("r: " + msg.obj);
							available.release();
							System.out.println("mainactivity dolongrunningoperation() available.release()");

							JSONObject serverData = new JSONObject(msg.obj.toString());

							switch (serverData.getString("msgType")) {
								case "multiPlayerList":
									int length = serverData.getJSONArray("multiPlayers").length();

									JSONObject json_player = serverData.getJSONObject("player");
									player = new Player(json_player.getString("username"), null, json_player.getInt("id"), json_player.getInt("score"), 0.0f, -5.0f, -20.0f, 0);
									/*player.setUsername(json_player.getString("username")); NOT CALLED!!!!
									player.setPlayerid(json_player.getInt("id"));
									player.setScore(json_player.getInt("score")); /*/
									/*JSONArray multiPlayers = serverData.getJSONArray("multiPlayers");

									multiPlayerList.clear();

									for (int i = 0; i < multiPlayers.length(); i++) {
										JSONObject json_data = multiPlayers.getJSONObject(i);

										MultiPlayer mp = new MultiPlayer(json_data.getString("username"), json_data.getInt("id"), json_data.getInt("score"));
										multiPlayerList.add(mp);
									}
									//JSONArray player = /*(JSONObject)*/ /*multiPlayers.getJSONArray("testList");//   getString (0);

									String name = "test";//player.getString("username");
									//int score = player.getInt("score");
									//int id = player.getInt("id");
									// Display the multiplayer list layout
									Message msg1 = new Message();
									msg1.what = 1;
									msg1.setTarget(mHandler);
									msg1.sendToTarget();
									break;
								case "invalidPassword":
									Message msg2 = new Message();
									msg2.what = 3;
									msg2.setTarget(mHandler);
									msg2.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "invalidUsername":
									Message msg9 = new Message();
									msg9.what = 9;
									msg9.setTarget(mHandler);
									msg9.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "invalidUsernameRegisterMultiAccount":
									Message msg3 = new Message();
									msg3.what = 4;
									msg3.setTarget(mHandler);
									msg3.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "challengeMultiplayer":
									Message msg4 = new Message();
									msg4.what = 8;
									msg4.obj = new JSONObject(msg.obj.toString());
									msg4.setTarget(mHandler);
									msg4.sendToTarget();

									/*for (int i = 0; i < multiPlayerList.size(); i++) {
										if (serverData.getInt("toPlayerId") == multiPlayerList.get(i).multiId) {

										}
								}*/ /*
									break;
								case "challengeMultiplayerAccepted":
									Message msg5 = new Message();
									msg5.what = 9;
									msg5.obj = new JSONObject(msg.obj.toString());
									msg5.setTarget(mHandler);
									msg5.sendToTarget();
								default:
									break;
							}
						//available.release();
					} catch (JSONException e) {
						System.out.println("mainactivity dolongrunningoperation() catch jsonbexcepetion called");
						e.printStackTrace();
					} catch (InterruptedException e) {
						System.out.println("mainactivity dolongrunningoperation() catch interruptedexcepetion called");
						e.printStackTrace();
					}
					}*/
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
		// Record to the external cache directory for visibility
		mediafileName = getExternalCacheDir().getAbsolutePath();
		mediafileName += "/audiorecordtest.3gp";
		ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);



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

										String fromUsername = player.getUsername();
										String toUsername = currentItem.multiUsername;
										int fromUserId = player.getPlayerid();
										int toUserId = currentItem.multiId;

										JSONObject gameData = new JSONObject();

										try {
											gameData.put("msgType", "challengeMultiPlayer");
											gameData.put("fromPlayername", fromUsername);
											gameData.put("toPlayername", toUsername);
											gameData.put("fromPlayerId", fromUserId);
											gameData.put("toPlayerId", toUserId);
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
						Message msgA = new Message();
						msgA.what = 3;
						mLooperThread.mHandler.sendMessage(msgA);
						break;
					case 4: displayMessage("Username already created. Please choose another.", "Ok" , "current");
						msgA = new Message();
						msgA.what = 3;
						mLooperThread.mHandler.sendMessage(msgA);

						break;
					case 5: displayMessage("Please confirm password.", "Ok", "current");
						break;
					case 6: System.out.println(" msg.what == 6");

						try {
							available.acquire();
							System.out.println("mainactivity dolongrunningoperation() available.acquire()");

							System.out.println("r: " + inputMessage.obj);
							available.release();
							System.out.println("mainactivity dolongrunningoperation() available.release()");


							JSONObject serverData;

							serverData = new JSONObject(inputMessage.obj.toString());
							Message msg;

							switch (serverData.getString("msgType")) {
								case "multiPlayerList":
									int length = serverData.getJSONArray("multiPlayers").length();

									JSONObject json_player = serverData.getJSONObject("player");
									player = new Player(json_player.getString("username"), null, json_player.getInt("id"), json_player.getInt("score"), 0.0f, 0.0f, 0.0f, 0);
									mGLSurfaceView.getmRenderer().setPlayer(player);
									JSONArray multiPlayers = serverData.getJSONArray("multiPlayers");

									multiPlayerList.clear();

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

									mGLSurfaceView.getmRenderer().gameMachine.Advance("MultiPlayerListMenu");

									msg = new Message();
									msg.what = 1;
									msg.setTarget(mHandler);
									msg.sendToTarget();
									break;
								case "invalidPassword":
									msg = new Message();
									msg.what = 3;
									msg.setTarget(mHandler);
									msg.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "invalidUsername":
									msg = new Message();
									msg.what = 11;
									msg.setTarget(mHandler);
									msg.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "invalidUsernameRegisterMultiAccount":
									msg = new Message();
									msg.what = 4;
									msg.setTarget(mHandler);
									msg.sendToTarget();
									//displayMessage("Invalid Password.", "Ok", "main");
									break;
								case "challengeMultiplayer":
									msg = new Message();
									msg.what = 8;
									msg.obj = new JSONObject(inputMessage.obj.toString());
									msg.setTarget(mHandler);
									msg.sendToTarget();
									break;
                                case "startingMatchCountdown":
                                    msg = new Message();
                                    msg.what = 9;
									JSONObject newServerData = new JSONObject(inputMessage.obj.toString());
                                    msg.obj = newServerData;
									msg.setTarget(mHandler);
									msg.sendToTarget();
                                    break;
								case "launchingMatch":
									player.setSessionId(serverData.getInt("sessionId"));
									opponent.setUsername(serverData.getString("username"));
									opponent.setPlayerid(serverData.getInt("playerId"));
									mGLSurfaceView.getmRenderer().setOpponent(opponent);

									activeMatch = true;
									mGLSurfaceView.getmRenderer().gameMachine.Advance("ActiveMatch");
									msg = new Message();
									msg.what = 10;
									msg.setTarget(mHandler);
									msg.sendToTarget();
									break;
								case "activeMatchSendData":
									float locationX = (float)serverData.getDouble("locationX");
									float locationY = (float)serverData.getDouble("locationY");
									float locationZ = (float)serverData.getDouble("locationZ");

									opponent.setLocation(locationX,locationY,locationZ); // needed? find where used
									mGLSurfaceView.getmRenderer().opponent.setLocation(locationX, locationY, locationZ);

									float posX = (float)serverData.getDouble("puck.posX");
									float posY = (float)serverData.getDouble("puck.posY");
									mGLSurfaceView.getmRenderer().setPuck(posX, posY);

									JSONObject activeMatchSendData = new JSONObject();
									activeMatchSendData.put("msgType", "activeMatchSendData");
									float[] playerLocation = player.getLocation();
									activeMatchSendData.put( "locationX", mGLSurfaceView.getmRenderer().touchedX);
									activeMatchSendData.put( "locationY", -5.0f);
									activeMatchSendData.put( "locationZ", -20.0f);
									activeMatchSendData.put( "playerId", player.getPlayerid());
									activeMatchSendData.put( "sessionId", player.getSessionId());

									msg = new Message();
									msg.what = 0;
									msg.obj = activeMatchSendData;
									mLooperThread.mHandler.sendMessage(msg);
									break;
                                case "updateScore":
                                    msg = new Message();
                                    mGLSurfaceView.getmRenderer().player.setScore(serverData.getInt("playerScore"));
                                    mGLSurfaceView.getmRenderer().opponent.setScore(serverData.getInt("opponentScore"));
                                    break;
								case "playmedia": // need to review
									//msg.
									//serverData.getJSONObject()
									break;
								default:
									break;
							}
							//available.release();
						} catch (JSONException e) {
							System.out.println("mainactivity dolongrunningoperation() catch jsonbexcepetion called");
							e.printStackTrace();
						} catch (InterruptedException e) {
							System.out.println("mainactivity dolongrunningoperation() catch interruptedexcepetion called");
							e.printStackTrace();
						}
						break;
					case 7: parentRelativeLayout.removeView((ImageView)findViewById(R.id.weakentroll_splashview));
						break;
					case 8:
						try {
							final JSONObject serverData = new JSONObject(inputMessage.obj.toString());
							display_challenge_textview.setText("You are being challenged by " + serverData.getString("fromPlayername"));
							display_challenge_layout.setVisibility(View.VISIBLE);

							challenge_yes_button.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									if (mLooperThread.mHandler != null) {
										try {
											serverData.remove("msgType");
											serverData.put("msgType", "challengeMultiPlayerAccepted");

											Message msg = new Message();
											msg.what = 0;
											msg.obj = serverData;
											mLooperThread.mHandler.sendMessage(msg);

                                            display_challenge_layout.setVisibility(View.GONE);
                                        } catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}
							});
                            challenge_no_button.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if (mLooperThread.mHandler != null) {
                                        try {
                                            serverData.remove("msgType");
                                            serverData.put("msgType", "multiPlayerChallengeDeclined");

                                            Message msg = new Message();
                                            msg.what = 0;
                                            msg.obj = serverData;
                                            mLooperThread.mHandler.sendMessage(msg);

                                            display_challenge_layout.setVisibility(View.GONE);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					case 9:
						try {
							final JSONObject serverData = new JSONObject(inputMessage.obj.toString());
							int countdownTimer = serverData.getInt("countdownTimer");
							if (countdownTimer == 10)
								countdown_textview.setVisibility(View.VISIBLE);
							else if (countdownTimer < 0) {
								countdown_textview.setVisibility(View.GONE);
								//activeMatch = true;
								break;
							}
							countdown_textview.setText("Starting match in " + countdownTimer + "...");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					case 10:
						multiPlayerList_layout.setVisibility(View.GONE);
						backMenuButton.setVisibility(View.GONE);
						multiPlayerListView.setVisibility(View.GONE);
						break;
					/*case 10: countdownTimer--;
						countdown_textview.setText("Starting match in " + countdownTimer + "...");
						COUNTDOWN = 10;

						if (countdownTimer < 0) {
							countdown_textview.setVisibility(View.GONE);
							countdownTimer = 10;
							return;
						}
						mHandler.sendEmptyMessageDelayed(COUNTDOWN, 1000);
						break;*/
					case 11: displayMessage("Invalid Username.", "Ok", "current");
						Message msg = new Message();
						msg.what = 3;
						mLooperThread.mHandler.sendMessage(msg);
						break;
					default:
						// Otherwise, calls the super method
						super.handleMessage(inputMessage);
						//break;
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
		parentRelativeLayout = (RelativeLayout)findViewById(R.id.my_relative_layout);
		dyn_layout = (LinearLayout)findViewById(R.id.dyn_layout);
		weakentroll_splash = (ImageView)findViewById(R.id.weakentroll_splashview);
		singlePlayerButton = (Button)findViewById(R.id.singlePlayer);
		newGameButton = (Button)findViewById(R.id.newGame);
		saveGameButton = (Button)findViewById(R.id.saveGame);
		loadGameButton = (Button)findViewById(R.id.loadGame);
		multiPlayerButton = (Button)findViewById(R.id.multiPlayer);
		debugButton = (Button)findViewById(R.id.debug);
		sendPlayerMsgButton = (Button)findViewById(R.id.sendPlayerMsg);


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
		display_challenge_layout = (LinearLayout)findViewById(R.id.display_challenge_layout);
		display_msg_textview = (TextView)findViewById(R.id.display_msg_textview);
		msg_button = (Button)findViewById(R.id.msg_button);
		display_challenge_textview = (TextView)findViewById(R.id.display_challenge_textview);
		challenge_yes_button = (Button)findViewById(R.id.challenge_yes_button);
		challenge_no_button = (Button)findViewById(R.id.challenge_no_button);

		or_textview = (TextView)findViewById(R.id.or__textview);
        countdown_textview = (TextView)findViewById(R.id.countdown_textview);

        createaccount_button = (Button)findViewById(R.id.createaccount_button);

		createMultiPlayerAccount_layout = (LinearLayout)findViewById(R.id.createMultiPlayerAccount_layout);
		create_multiplayer_username_input = (EditText)findViewById(R.id.create_multiplayer_username_input);
		create_multiplayer_password_input = (EditText)findViewById(R.id.create_multiplayer_password_input);
		create_multiplayer_password_confirm_input = (EditText)findViewById(R.id.create_multiplayer_password_confirm_input);
		register_multiplayer_account_button = (Button)findViewById(R.id.register_multiplayer_account_button);

		player = new Player(null, null, 0,
				0, -5.0f, 5.0f, -20.0f, 0);
		opponent = new Player(null, null, 0,
                0, -5.0f, 5.0f, -20.0f, 0);

		mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.glSurfaceViewID);

		Glide.with(this)
				.load(R.drawable.weakentroll_splash)
				.into((ImageView)findViewById(R.id.weakentroll_splashview));

		//this will post a message to the mHandler, which mHandler will get after 5 seconds
		int FINISH_SPLASH = 7;
		mHandler.sendEmptyMessageDelayed(FINISH_SPLASH, 2000);
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

	public void debug(View view) {
		if (debugFlag == true) {
			debugFlag = false;
			mGLSurfaceView.getmRenderer().debugFlag = false;
		}
		else {
			debugFlag = true;
			mGLSurfaceView.getmRenderer().debugFlag = true;
		}
		}

	public void sendPlayerMsg(View view) {
		/*
		type text or send 5 second audio msg

		 */
        mediarecorder = new MediaRecorder();
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediarecorder.setOutputFile(mediafileName);
        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediarecorder.prepare();
        } catch (IOException e) {
            //Log.e(TAG, "prepare() failed");
        }

        mediarecorder.start();


        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
				mediarecorder.stop();
				mediarecorder.release();
				mediarecorder = null;

				// send mediafilename to opponent
				if (mLooperThread.mHandler != null) {
					Message msg = mLooperThread.mHandler.obtainMessage(0);

					JSONObject gameData = new JSONObject();

					try {
						gameData.put("msgType", "playmedia");
					}
					catch(JSONException ex) {
						ex.printStackTrace();
					}

					msg.obj = gameData;

					Bundle b = new Bundle();
					byte[] mybytearray = new byte[(int) mediafileName.length()];
					FileInputStream fis = null;

					try {
						fis = new FileInputStream(mediafileName);
						fis.read(mybytearray);
						fis.close();

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					b.putByteArray("mediafile", mybytearray );
					msg.setData(b);
					mLooperThread.mHandler.sendMessage(msg);
				}

            }
        }, 0, 5000);
		//record



		//send the data
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
                dyn_layout.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL );

				newGameButton.setVisibility(View.VISIBLE);
				saveGameButton.setVisibility(View.VISIBLE);
				loadGameButton.setVisibility(View.VISIBLE);
				exitGameButton.setVisibility(View.VISIBLE);
            }
        });
        openMenuButton.setVisibility(View.VISIBLE);
        dyn_layout.addView(openMenuButton);

        sendPlayerMsgButton.setVisibility(View.VISIBLE);

		mGLSurfaceView.getmRenderer().player.setScore(0);
		mGLSurfaceView.getmRenderer().opponent.setScore(0);
		mGLSurfaceView.getmRenderer().gameMachine.Advance("Pong");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mGLSurfaceView.getmRenderer().moveComputer();                    }
        }, 0, 200);
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
		// try to connect to server - not in use right now
		// Message msgConnect = mLooperThread.mHandler.obtainMessage(2);
		// mLooperThread.mHandler.sendMessage(msgConnect);

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
			case "MultiPlayerListMenu":
				// only disconnect when the player exits multiplayer lobby
				if (mLooperThread.mHandler != null) {
					Message msg = mLooperThread.mHandler.obtainMessage(0);

					String username = multiplayer_username_input.getText().toString(); //new String("discmanx");
					String password = multiplayer_password_input.getText().toString(); //new String("discmanx");

					JSONObject gameData = new JSONObject();

					try {
						gameData.put("msgType", "multiLogout");
						gameData.put("username", username);
					}
					catch(JSONException ex) {
						ex.printStackTrace();
					}
					msg.obj = gameData;
					mLooperThread.mHandler.sendMessage(msg);
				}



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
				gameData.put("playerScore", mGLSurfaceView.getmRenderer().player.getScore());
				gameData.put("opponentScore", mGLSurfaceView.getmRenderer().opponent.getScore());

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
				gameData.put("msgType", "saveGame");
				gameData.put("username", username);
				gameData.put("playerScore", mGLSurfaceView.getmRenderer().player.getScore());
				gameData.put("opponentScore", mGLSurfaceView.getmRenderer().opponent.getScore());
			}
			catch(JSONException ex) {
				ex.printStackTrace();
			}

            msg.obj = gameData;
			mLooperThread.mHandler.sendMessage(msg);
		}
	}

	private void doLongRunningOperation(Message msg) {
		// If no connection to server
		if (nc.connectWithServer() == false)
		{
			Message msg1 = new Message();
			msg1.what = 2;
			msg1.setTarget(mHandler);
			msg1.sendToTarget();
			return;
		}
		nc.sendDataWithString(msg.obj.toString());
		System.out.println(msg.obj.toString());
	}

	protected void onDestroy() {
        super.onDestroy();
		mLooperThread.mHandler.getLooper().quit();
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			hideSystemUI();
		}
	}

	private void hideSystemUI() {
		// Enables regular immersive mode.
		// For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
		// Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_IMMERSIVE
						// Set the content to appear under the system bars so that the
						// content doesn't resize when the system bars hide and show.
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						// Hide the nav bar and status bar
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	// Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
	private void showSystemUI() {
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mediarecorder != null) {
			mediarecorder.release();
			mediarecorder = null;
		}

		if (mediaplayer != null) {
			mediaplayer.release();
			mediaplayer = null;
		}
	}

}