package donduritoapps.illumino;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class AddRoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** RoomFragment";

    private Integer[] avatarIds = {
            R.drawable.ic_build_white_24dp,
            R.drawable.ic_weekend_white_24dp,
            R.drawable.ic_hot_tub_white_24dp,
            R.drawable.ic_restaurant_menu_white_24dp,
            R.drawable.ic_hotel_white_24dp
    };
    private int avatarSelectionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("New Room");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageButton button_getInfo = (ImageButton) findViewById(R.id.button_get_info);
        button_getInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText roomIP = (EditText) findViewById(R.id.editText_newRoomIP);
                startRequest(roomIP.getText().toString(),"");
            }
        });

        final GridView gridView = (GridView) findViewById(R.id.grid_room_icons);
        gridView.setAdapter(new GridAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ImageView avatarImage;
                for (int i = 0; i < avatarIds.length; i++) {
                    avatarImage = (ImageView) gridView.getChildAt(i);
                    if (i == position) {
                        avatarImage.setColorFilter(ContextCompat.getColor(AddRoomActivity.this, R.color.colorAccent));
                    }
                    else {
                        avatarImage.setColorFilter(Color.WHITE);
                    }
                }

                avatarSelectionIndex = position;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        if (id == R.id.action_save) {
            Intent returnIntent = new Intent();

            EditText roomName = (EditText) findViewById(R.id.editText_newRoomName);
            returnIntent.putExtra("ROOM_NAME",roomName.getText().toString());

            EditText roomIP = (EditText) findViewById(R.id.editText_newRoomIP);
            returnIntent.putExtra("ROOM_IP", roomIP.getText().toString());

            returnIntent.putExtra("ROOM_ICON", avatarIds[avatarSelectionIndex]);
            setResult(AddRoomActivity.RESULT_OK, returnIntent);

            EditText roomStripes = (EditText) findViewById(R.id.editText_newRoomStripes);
            returnIntent.putExtra("ROOM_STRIPE_NAMES", roomStripes.getText().toString());

            ImageView imageView_dht_state = (ImageView) findViewById(R.id.imageView_dhtState);
            if (imageView_dht_state.isEnabled()) { returnIntent.putExtra("ROOM_DHT", true); }
            else { returnIntent.putExtra("ROOM_DHT", false); }

            ImageView imageView_pir_state = (ImageView) findViewById(R.id.imageView_pirState);
            if (imageView_dht_state.isEnabled()) { returnIntent.putExtra("ROOM_PIR", true); }
            else { returnIntent.putExtra("ROOM_PIR", false); }

            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class GridAdapter extends BaseAdapter {
        private Context mContext;

        public GridAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return avatarIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(avatarIds[position]);

            return imageView;
        }
    }

    public void startRequest(final String ip, final String message) {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, "No network access", Toast.LENGTH_LONG).show();
            return;
        }

        final String url = "http://" + ip + "/" + message;
        Log.d(DEBUG_TAG, "Request: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(DEBUG_TAG, "Response PASS!!!" + url);
                        Log.d(DEBUG_TAG, "Response: " + response);
                        processResponse(ip, message, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(DEBUG_TAG, "Response FAIL!!!: " + url);
            }
        });

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MyWiFi.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void processResponse(String serverIP, String request, String response) {
        if (serverIP.equals("192.168.178.80")) {
            response = "VER:0.1\nNAME:Küche\nSTRIPES:Decke,Arbeitsfläche\nDHT:Y\nPIR:Y";
            Log.d(DEBUG_TAG, serverIP);
        }
        String version = response.split("\n")[0];

        String room_name = response.split("\n")[1].split(":")[1];
        EditText editText_room_name = (EditText) findViewById(R.id.editText_newRoomName);
        editText_room_name.setText(room_name);

        String room_stripes = response.split("\n")[2].split(":")[1];
        EditText editText_room_stripes = (EditText) findViewById(R.id.editText_newRoomStripes);
        editText_room_stripes.setText(room_stripes);

        String room_dht_available = response.split("\n")[3].split(":")[1];
        TextView textView_dht_state = (TextView) findViewById(R.id.textView_dht_state);
        if (room_dht_available.equals("Y")) {
            textView_dht_state.setEnabled(true);
        }
        else {
            textView_dht_state.setEnabled(false);
        }

        String room_pir_available = response.split("\n")[4].split(":")[1];
        TextView textView_pir_state = (TextView) findViewById(R.id.textView_pir_state);
        if (room_pir_available.equals("Y")) {
            textView_pir_state.setEnabled(true);
        }
        else {
            textView_pir_state.setEnabled(false);
        }
    }
}
