package donduritoapps.illumino;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrefRoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino PrefRoom";
    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    private List<String> jObj_roomList = new ArrayList<>();
    ArrayAdapter<MyRoom> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrefRoomActivity.this, AddRoomActivity.class);
                startActivityForResult(intent, 1);
            }
        });



        populateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pref_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            Intent homeIntent= new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        if (id == R.id.action_delete_list) {
            roomList.clear();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();
            adapter.notifyDataSetChanged();
        }

        if (id == R.id.action_save) {
            // Save Preferences
//            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putInt("ROOM_COUNT", roomList.size());
            for (int i = 0; i < roomList.size(); i++) {
                editor.putString("ROOM_JSON_" + i, jObj_roomList.get(i));
            }
            editor.commit();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(DEBUG_TAG, String.valueOf(requestCode));
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(DEBUG_TAG, "new room added");
                    // Add new room to preferences
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    int newRoomNumber = roomList.size();
                    editor.remove("ROOM_COUNT");
                    editor.putInt("ROOM_COUNT", roomList.size()+1);

                    String json_string = data.getStringExtra("ROOM_JSON");
                    Log.d(DEBUG_TAG, json_string);
                    editor.putString("ROOM_JSON_" + newRoomNumber, json_string);
                    editor.commit();


                    jObj_roomList.add(newRoomNumber, json_string);
                    roomList.add(new MyRoom(this, newRoomNumber));
                    adapter.notifyDataSetChanged();
                } else
                    Log.d(DEBUG_TAG, "resultCode Error!!!");
                break;
            }
        }
    }

    private void populateListView() {
        // Load from Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            roomList.add(new MyRoom(this, i));
            jObj_roomList.add(sharedPref.getString("ROOM_JSON_" + i, "{ }"));
        }
        adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.listViewRoomSettings);
        list.setAdapter(adapter);
    }



    private class MyListAdapter extends ArrayAdapter<MyRoom> {
        public MyListAdapter() {
            super(PrefRoomActivity.this, R.layout.pref_item_view, roomList);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.pref_item_view, parent, false);
            }

            // Find the room to work with.
            final MyRoom room = roomList.get(position);

            // Fill the view
            // Icon:
            ImageView imageView = (ImageView)itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(room.getIconID());

            // Name:
            TextView nameText = (TextView) itemView.findViewById(R.id.item_txtName);
            nameText.setText(room.getName());

            // IP:
            TextView ipText = (TextView) itemView.findViewById(R.id.item_textIP);
            ipText.setText(room.getIp());

            // Stripe Names
            TextView stripeNamesText = (TextView) itemView.findViewById(R.id.item_textStripes);
            String stripeNames = "";
            for (int i=0; i < room.getStripes_num(); i++) {
                stripeNames += room.getStripeName(i);
                if (i < room.getStripes_num() - 1)
                    stripeNames += ", ";
            }
            stripeNamesText.setText(stripeNames);

            // DHT:
            TextView dhtText = (TextView) itemView.findViewById(R.id.item_textDHT);
            dhtText.setText("DHT");
            if (room.getDhtState()) {
                dhtText.setEnabled(true);

                TextView textView_tempUuid = (TextView) itemView.findViewById(R.id.item_textTempUUID);
                textView_tempUuid.setText(room.getTemperatureUuid());
                textView_tempUuid.setVisibility(View.VISIBLE);

                TextView textView_humidUuid = (TextView) itemView.findViewById(R.id.item_textHumidUUID);
                textView_humidUuid.setText(room.getHumidityUuid());
                textView_humidUuid.setVisibility(View.VISIBLE);
            }
            else { dhtText.setEnabled(false); }

            // PIR:
            TextView pirText = (TextView) itemView.findViewById(R.id.item_textPIR);
            pirText.setText("PIR");
            if (room.getPirState()) { pirText.setEnabled(true); }
            else { pirText.setEnabled(false); }

            // Button Up:
            ImageButton btnUpAction = (ImageButton) itemView.findViewById(R.id.btn_list_up);
            btnUpAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swapListItems(position, -1);
                }
            });

            // Button Down:
            ImageButton btnDownAction = (ImageButton) itemView.findViewById(R.id.btn_list_down);
            btnDownAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swapListItems(position, 1);
                }
            });

            // Button Trash:
            ImageButton btnDeleteAction = (ImageButton) itemView.findViewById(R.id.btn_delete);
            btnDeleteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    roomList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });

            return itemView;
        }

        void swapListItems(int position, int delta) {
            if ((position + delta < 0) || (position + delta >= roomList.size())) {
                Toast.makeText(PrefRoomActivity.this, "am Ende der Liste...Maaaaan", Toast.LENGTH_LONG).show();
                return;
            }

            // move down
            if (delta == 1) {
                roomList.add(position, roomList.get(position + 1));
                roomList.remove(position + 2);

                jObj_roomList.add(position, jObj_roomList.get(position + 1));
                jObj_roomList.remove(position + 2);
            }
            else {
                roomList.add(position - 1, roomList.get(position));
                roomList.remove(position + 1);

                jObj_roomList.add(position - 1, jObj_roomList.get(position));
                jObj_roomList.remove(position + 1);
            }
            adapter.notifyDataSetChanged();
        }
    }

}

