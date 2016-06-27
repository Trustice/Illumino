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

import java.util.ArrayList;
import java.util.List;

public class PrefRoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino PrefRoom";
    private List<MyRoom> roomList = new ArrayList<MyRoom>();
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
                //roomList.add(new MyRoom("Test", "192.168.178.34", R.drawable.avatar_test_40dp));
                //adapter.notifyDataSetChanged();
//                final AppCompatDialog dialog = new AppCompatDialog(PrefRoomActivity.this);
//                dialog.setContentView(R.layout.dialog_add_room);
//                dialog.setTitle("Add Room");
//
//
//                Button buttonOK = (Button) dialog.findViewById(R.id.buttonOK);
//                buttonOK.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        EditText edit = (EditText) dialog.findViewById(R.id.editText_name);
//                        String text = edit.getText().toString();
//                        dialog.dismiss();
//                        Toast.makeText(PrefRoomActivity.this, text, Toast.LENGTH_LONG).show();
//                    }
//                });
//
//                Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
//                buttonCancel.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//
//                dialog.show();
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
            adapter.notifyDataSetChanged();
        }

        if (id == R.id.action_save) {
            // Save Preferences
//            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putInt("ROOM_COUNT", roomList.size());
            Log.d(DEBUG_TAG, String.valueOf(roomList.size()));
            for (int i = 0; i < roomList.size(); i++) {
                String roomNumber = String.valueOf(i);
                MyRoom room = roomList.get(i);
                editor.putString("ROOM_NAME_" + roomNumber, room.getName());
                editor.putString("ROOM_IP_" + roomNumber, room.getIp());
                editor.putInt("ROOM_ICON_" + roomNumber, room.getIconID());
            }
            editor.commit();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    String newRoomName = data.getStringExtra("ROOM_NAME");
                    String newRoomIP = data.getStringExtra("ROOM_IP");
                    int newRoomIcon = data.getIntExtra("ROOM_ICON", R.drawable.ic_build_white_24dp);
//                    Toast.makeText(PrefRoomActivity.this, newRoomName, Toast.LENGTH_LONG).show();
//                    Toast.makeText(PrefRoomActivity.this, newRoomIP, Toast.LENGTH_LONG).show();
                    roomList.add(new MyRoom(newRoomName, newRoomIP, newRoomIcon));
                    adapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    private void populateListView() {
        // Load from Preferences
//        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            String roomNumber = String.valueOf(i);
            String roomName = sharedPref.getString("ROOM_NAME_" + roomNumber, "Error");
            String roomIP = sharedPref.getString("ROOM_IP_" + roomNumber, "Error");
            int roomIcon = sharedPref.getInt("ROOM_ICON_" + roomNumber, 0);
            roomList.add(new MyRoom(roomName, roomIP, roomIcon));
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
            final MyRoom currentRoom = roomList.get(position);

            // Fill the view
            // Icon:
            ImageView imageView = (ImageView)itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(currentRoom.getIconID());

            // Name:
            TextView nameText = (TextView) itemView.findViewById(R.id.item_txtName);
            nameText.setText(currentRoom.getName());

            // IP:
            TextView ipText = (TextView) itemView.findViewById(R.id.item_textIP);
            ipText.setText(currentRoom.getIp());

            // Button Up:
            ImageButton btnUpAction = (ImageButton) itemView.findViewById(R.id.btn_list_up);
            btnUpAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toast.makeText(PrefRoomActivity.this, "OKKKKK", Toast.LENGTH_LONG).show();
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
            }
            else {
                roomList.add(position - 1, roomList.get(position));
                roomList.remove(position + 1);
            }
            adapter.notifyDataSetChanged();
        }
    }

}

