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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddRoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** AddRoomActivity";

    private JSONObject jObj_room;
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

            try {
                jObj_room.put("icon_id", avatarIds[avatarSelectionIndex]);
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "unexpected JSON exception", e);
            }
            Log.d(DEBUG_TAG, jObj_room.toString());
            returnIntent.putExtra("ROOM_JSON", jObj_room.toString());
            setResult(RESULT_OK, returnIntent);
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
        /* response - JSON Object:
            {
                "version":"v0.11",
                "date":"Dec 14 2016 21:28:23",
                "name":"TestRoom",
                "stripes":["D3","D4"],
                "dht":
                {
                "t_uuid":"17f94ff0-49ea-11e6-b180-3313562e4688",
                "h_uuid":"2c928490-49ea-11e6-a029-f5281b8b5d4d"
                },
                "pir":true
            }
         */
        if (serverIP.equals("192.168.178.80"))
            response = "{\"version\":\"v0.10\",\"date\":\"Dec 10 2016 21:30:54\",\"name\":\"Küche\",\"stripes\":[\"Decke\",\"Arbeitsfläche\"],\"dht\":{\"t_uuid\":\"1641b790-4799-11e6-9031-f75b35dde6e3\",\"h_uuid\":\"40fa9140-4799-11e6-9bab-5733c696a2e6\"},\"pir\":true}";

        try {
            jObj_room = new JSONObject(response);

            jObj_room.put("ip", serverIP);

            TextView textView_appVersion = (TextView) findViewById(R.id.textView_appVersion);
            textView_appVersion.setText(jObj_room.getString("version"));
            textView_appVersion.setVisibility(View.VISIBLE);

            TextView textView_compileDate = (TextView) findViewById(R.id.textView_compileDate);
            textView_compileDate.setText(jObj_room.getString("date"));
            textView_compileDate.setVisibility(View.VISIBLE);

            String room_name = jObj_room.getString("name").replace("Ã¼", "ü");
            EditText editText_room_name = (EditText) findViewById(R.id.editText_newRoomName);
            editText_room_name.setText(room_name);

            JSONArray stripes_arr = jObj_room.getJSONArray("stripes");
            String room_stripes = "";
            for (int i=0; i < stripes_arr.length(); i++) {
                room_stripes += stripes_arr.getString(i);
                if (i != stripes_arr.length() - 1)
                    room_stripes += ",";
            }
            EditText editText_room_stripes = (EditText) findViewById(R.id.editText_newRoomStripes);
            editText_room_stripes.setText(room_stripes);

            JSONObject jObj_dht = jObj_room.getJSONObject("dht");
            TextView textView_dht_state = (TextView) findViewById(R.id.textView_dht_state);
            textView_dht_state.setText("DHT");
            TextView textView_temp_uuid = (TextView) findViewById(R.id.editText_newRoomTempUUID);
            TextView textView_humid_uuid = (TextView) findViewById(R.id.editText_newRoomHumidUUID);

            String t_uuid = jObj_dht.getString("t_uuid");
            if (!t_uuid.equals("nan")) {
                textView_dht_state.setEnabled(true);
                textView_temp_uuid.setText(t_uuid);
                textView_temp_uuid.setVisibility(View.VISIBLE);

                String h_uuid = jObj_dht.getString("h_uuid");
                textView_humid_uuid.setText(h_uuid);
                textView_humid_uuid.setVisibility(View.VISIBLE);
            }
            else {
                textView_dht_state.setEnabled(false);
                textView_temp_uuid.setVisibility(View.INVISIBLE);
                textView_humid_uuid.setVisibility(View.INVISIBLE);
            }

            boolean room_pir_available = jObj_room.getBoolean("pir");
            TextView textView_pir_state = (TextView) findViewById(R.id.textView_pir_state);
            textView_pir_state.setText("PIR");
            textView_pir_state.setEnabled(room_pir_available);

        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "unexpected JSON exception", e);
        }
//        String version = response.split("\n")[0];

//        String room_name = response.split("\n")[1].split(":")[1];
//        room_name = room_name.replace("Ã¼", "ü");
//        EditText editText_room_name = (EditText) findViewById(R.id.editText_newRoomName);
//        editText_room_name.setText(room_name);

//        String room_stripes = response.split("\n")[2].split(":")[1];
//        EditText editText_room_stripes = (EditText) findViewById(R.id.editText_newRoomStripes);
//        editText_room_stripes.setText(room_stripes);
//
//        String room_dht_available = response.split("\n")[3].split(":")[1];
//        TextView textView_dht_state = (TextView) findViewById(R.id.textView_dht_state);
//        textView_dht_state.setText("DHT");
//        Log.d(DEBUG_TAG, room_dht_available.split(";")[0]);
//        if ( room_dht_available.contains(";") && room_dht_available.split(";")[0].equals("Y") ) {
//            textView_dht_state.setEnabled(true);
//
//            TextView textView_temp_uuid = (TextView) findViewById(R.id.editText_newRoomTempUUID);
//            textView_temp_uuid.setText(room_dht_available.split(";")[1].split("#")[1]);
//            textView_temp_uuid.setVisibility(View.VISIBLE);
//
//            TextView textView_humid_uuid = (TextView) findViewById(R.id.editText_newRoomHumidUUID);
//            textView_humid_uuid.setText(room_dht_available.split(";")[2].split("#")[1]);
//            textView_humid_uuid.setVisibility(View.VISIBLE);
//        } else {
//            textView_dht_state.setEnabled(false);
//        }
//
//        String room_pir_available = response.split("\n")[4].split(":")[1];
//        TextView textView_pir_state = (TextView) findViewById(R.id.textView_pir_state);
//        textView_pir_state.setText("PIR");
//        if (room_pir_available.equals("Y"))
//            textView_pir_state.setEnabled(true);
//        else
//            textView_pir_state.setEnabled(false);
    }
}
