package com.weakentroll.extreme3dpong;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.weakentroll.extreme3dpong.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by discm on 2018-02-04.
 */

public class CustomListAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context; //context
    private ArrayList<MainActivity.MultiPlayer> items; //data source of the list adapter
    private Handler mHandler;
    private Player player;

    //public constructor
    public CustomListAdapter(Context context, Handler mHandler, ArrayList<MainActivity.MultiPlayer> items, Player player) {
        this.context = context;
        this.mHandler = mHandler;
        this.items = items;
        this.player = player;
    }

    @Override
    public int getCount() {
        return items.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return items.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        //String username = object.

        switch (v.getId())
        {
            case R.id.challengeMultiplayer: if (mHandler != null) {
                Message msg = mHandler.obtainMessage(0);

                String userInput;
                String username = new String("discmanx");
                JSONObject gameData = new JSONObject();

                try {
                    gameData.put("msgType", "challengyMultiPlayer");
                    // get objkect.usernamegameData.put("username", username);
                }
                catch(JSONException ex) {
                    ex.printStackTrace();
                }

                msg.obj = gameData;
                mHandler.sendMessage(msg);
                }
                break;
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.layout_list_view_row_items, parent, false);
        }

        // get current item to be displayed
        MainActivity.MultiPlayer currentItem = (MainActivity.MultiPlayer) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemUsername = (TextView)
                convertView.findViewById(R.id.text_view_item_username);
        TextView textViewItemScore = (TextView)
                convertView.findViewById(R.id.text_view_item_score);

        //sets the text for item name and item description from the current item object
        textViewItemUsername.setText(currentItem.getMultiUsername());
        textViewItemScore.setText(Integer.toString(currentItem.getMultiScore()));


         Button challengeMultiplayerButton = (Button) convertView.findViewById(R.id.challengeMultiplayer);
         challengeMultiplayerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });
         if ( currentItem.getMultiUsername().equals(player.getUsername()) ) {
             challengeMultiplayerButton.setVisibility(View.GONE);
         }
         else
             challengeMultiplayerButton.setVisibility(View.VISIBLE);


        // returns the view for the current row
        return convertView;
    }
}