package donduritoapps.illumino;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino RoomAct";
    private CoordinatorLayout coordinatorLayout;
//    private List<MyRoom> roomList = new ArrayList<MyRoom>();
//    private MyRoom room;
//    private int radioButtonSelection;
//    private View itemView;
//    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        RoomFragment roomFragment = new RoomFragment().newInstance(
                intent.getStringExtra("ROOM_NAME"),
                intent.getStringExtra("ROOM_IP"));

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, roomFragment).commit();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_room);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(intent.getStringExtra("ROOM_NAME"));
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

    }

//    public void refreshActivity() {
//        startRequest(room.getIp(), "P_");
//        startRequest(room.getIp(), "C1_");
//        startRequest(room.getIp(), "C2_");
//        startRequest(room.getIp(), "I_");
//        getVolkszaehlerData();
//
//        swipeRefreshLayout.setRefreshing(false);
//    }

    @Override
    public void onResume() {
        super.onResume();

        //populateListView();
        //registerClickCallback();

        //refreshActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room, menu);
        MenuItem item = menu.findItem(R.id.action_refresh);
        item.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
//            refreshActivity();
        }

        if (id == R.id.action_volkszaehler) {
            Intent intent = new Intent(RoomActivity.this, ChartActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


//    private void populateListView() {
//        ArrayAdapter<MyRoom> adapter = new MyListAdapter();
//        ListView list = (ListView) findViewById(R.id.roomListView);
//        if (list == null) return;
//        list.setAdapter(adapter);
//    }
//
//    private class MyListAdapter extends ArrayAdapter<MyRoom> {
//        public MyListAdapter() {
//            super(RoomActivity.this, R.layout.item_room, roomList);
//        }
//
//        @Override
//        public View getView(int position, View convertView, final ViewGroup parent) {
//            // Make sure we have a view to work with (may have been given null)
//            itemView = convertView;
//            if (itemView == null) {
//                itemView = getLayoutInflater().inflate(R.layout.item_room, parent, false);
//            }
//
//            // Find the room to work with.
//            final MyRoom currentRoom = roomList.get(position);
//
//            // Name:
//            //TextView nameText = (TextView) itemView.findViewById(R.id.item_txtName);
//            //nameText.setText(currentRoom.getName());
//            createOnOffSwitch();
//            createPatternSelection();
//            createAnimationSwitch();
//            createColorButtons();
//            createSliders();
//
//            return itemView;
//        }
//    }
//
//
//    public void startRequest(final String ip, final String message) {
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo == null || !networkInfo.isConnected()) {
//            Toast.makeText(RoomActivity.this, "No network access", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        final String url = "http://" + ip + "/" + message;
//        Log.d(DEBUG_TAG, "Request: " + url);
//
//        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d(DEBUG_TAG, "Response PASS!!!" + url);
//                        Log.d(DEBUG_TAG, "Response: " + response.replace("\r\n",""));
//                        processResponse(ip, message, response.replace("!\r\n",""));
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d(DEBUG_TAG, "Response FAIL!!!: " + url);
//            }
//        });
//
//        // Add a request (in this example, called stringRequest) to your RequestQueue.
//        MyWiFi.getInstance(this).addToRequestQueue(stringRequest);
//    }
//
//    private void createOnOffSwitch() {
//        //Switch Button for ON - OFF
//        SwitchCompat switchOnOff = (SwitchCompat) itemView.findViewById(R.id.switch_on_off);
//        switchOnOff.setEnabled(false);
//        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                String pattern = room.getPattern();
//                if (isChecked) {
//                    if (pattern.equals("99") || pattern.equals("97")) {
//                        startRequest(room.getIp(), "P98");
//                    }
//                } else {
//                    if (!pattern.equals("99") && !pattern.equals("97")) {
//                        startRequest(room.getIp(), "P99");
//                    }
//                }
//                refreshActivity();
//            }
//        });
//    }
//
//    private void createPatternSelection() {
//        // Pattern Selection
//        View actionPattern = itemView.findViewById(R.id.action_Pattern);
//        if (actionPattern != null) {
//            actionPattern.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    final AppCompatDialog dialog = new AppCompatDialog(RoomActivity.this);
//                    dialog.setContentView(R.layout.dialog_pattern);
//                    dialog.setTitle("Pattern");
//                    dialog.show();
//
//                    RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
//                    if (radioGroup == null) { return; }
//                    radioGroup.check(radioButtonSelection);
//                    Button buttonOK = (Button) dialog.findViewById(R.id.button_okay);
//                    if (buttonOK == null) { return; }
//                    buttonOK.setOnClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
//                            int selectedId = radioGroup.getCheckedRadioButtonId();
//                            radioButtonSelection = selectedId;
//                            // find the radiobutton by returned id
//                            RadioButton radioButton = (RadioButton) dialog.findViewById(selectedId);
//                            TextView txtViewPattern = (TextView) itemView.findViewById(R.id.textView_Pattern);
//                            String pattern = radioButton.getText().toString();
//                            txtViewPattern.setText(pattern);
//                            switch (pattern) {
//                                case "Waves":
//                                    startRequest(room.getIp(), "P2");
//                                    break;
//                                case "Rainbow Short":
//                                    startRequest(room.getIp(), "P3");
//                                    break;
//                                case "Rainbow Long":
//                                    startRequest(room.getIp(), "P4");
//                                    break;
//                                case "Theater":
//                                    startRequest(room.getIp(), "P6");
//                                    break;
//                                case "Scanner":
//                                    startRequest(room.getIp(), "P8");
//                                    break;
//                                default:
//                                    break;
//                            }
//                            Snackbar.make(coordinatorLayout, radioButton.getText(), Snackbar.LENGTH_SHORT)
//                                    .show();
//                            dialog.dismiss();
//                        }
//                    });
//
//                    Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
//                    if (buttonCancel != null) {
//                        buttonCancel.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        });
//                    }
//                }
//            });
//
//        }
//    }
//
//    private void createAnimationSwitch() {
//        //Switch Button for Animation
//        SwitchCompat switchAnimation = (SwitchCompat) itemView.findViewById(R.id.switch_animation);
//        if (switchAnimation == null) { return; }
//        switchAnimation.setEnabled(true);
//        switchAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                String pattern = room.getPattern();
//                if (isChecked) {
//                    switch (pattern) {
//                        case "1":
//                        case "5":
//                        case "7":
//                        case "98":
//                            startRequest(room.getIp(), "P2");
//                            break;
//                        default:
//                            break;
//                    }
//                } else { // not checked
//                    switch (pattern) {
//                        case "1":
//                        case "5":
//                        case "7":
//                        case "98":
//                        case "97":
//                        case "99":
//                            break;
//                        default:
//                            startRequest(room.getIp(), "P5");
//                            break;
//                    }
//                }
//            }
//        });
//    }
//
//    private void createColorButtons() {
//        // Button Color 1
//        Button button_color1 = (Button) itemView.findViewById(R.id.button_color1);
//        colorButtonListener(1, button_color1);
//        Button button_color2 = (Button) itemView.findViewById(R.id.button_color2);
//        colorButtonListener(2, button_color2);
//
//        Button button_colorSwap = (Button) itemView.findViewById(R.id.button_color_swap);
//        if (button_colorSwap == null) return;
//        button_colorSwap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startRequest(room.getIp(), "P7");
//            }
//        });
//    }
//
//    private void colorButtonListener(final int color_number, final Button button_color) {
//        button_color.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(RoomActivity.this, ColorActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void createSliders() {
//        SeekBar slider_interval = (SeekBar) itemView.findViewById(R.id.seekBar_Interval);
//        if (slider_interval == null) return;
//        slider_interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            TextView textView_interval = (TextView) itemView.findViewById(R.id.textView_interval);
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                double interval = Math.exp(0.08517 * progress);
//                textView_interval.setText(String.format("%.0fms", interval));
//                Log.d(DEBUG_TAG, String.valueOf(interval));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                int progress = seekBar.getProgress();
//                double interval = Math.exp(0.08517 * progress);
//                String message = String.format("I%.0f", interval);
//                startRequest(room.getIp(), message);
//            }
//        });
//    }
//
//    // extract information from result of a WebRequest
//    private void processResponse(String serverIP, String request, String response) {
//        // resend request send if failed
//        if (!request.contains("_") && !response.equals(request)) {
//            //startRequest(serverIP, request);
//            Snackbar.make(coordinatorLayout, "Request Error: " + request + "\nresponse: " + response, Snackbar.LENGTH_LONG).show();
//        } else {
//            if (!room.getIp().equals(serverIP)) {
//                    return;
//                }
//
//            char state = response.charAt(0);
//            String value = response.substring(1);
//            switch (state) {
//                case 'P':   // process as Pattern
//                    processPattern(value);
//                    break;
//                case 'C':   // Color
//                    processColor(value);
//                    break;
//                case 'I':
//                    double progress = 11.74096 * Math.log(Integer.parseInt(value));
//                    room.setInterval((int) progress);
//                    Log.d(DEBUG_TAG, "progress: " + progress);
//                    SeekBar slider_interval = (SeekBar) itemView.findViewById(R.id.seekBar_Interval);
//                    if (slider_interval == null) return;
//                    slider_interval.setProgress((int) progress);
//                    break;
//                default:
//                    Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    private void processPattern(String value) {
//        room.setPattern(value);
//
//        // get button view from card to change tint
//        SwitchCompat switchCompatOnOff = (SwitchCompat) itemView.findViewById(R.id.switch_on_off);
//        switchCompatOnOff.setEnabled(true);
//        SwitchCompat switchCompatAnimation = (SwitchCompat) itemView.findViewById(R.id.switch_animation);
//        switchCompatAnimation.setEnabled(true);
//        switch (value) {
//            case "97":
//            case "99":
//                switchCompatOnOff.setChecked(false);
//                switchCompatAnimation.setChecked(false);
//                break;
//            case "1":
//            case "5":
//            case "7":
//            case "98":
//                switchCompatOnOff.setChecked(true);
//                switchCompatAnimation.setChecked(false);
//                break;
//            default:
//                switchCompatOnOff.setChecked(true);
//                switchCompatAnimation.setChecked(true);
//                break;
//        }
//
//        TextView txtViewPattern = (TextView) itemView.findViewById(R.id.textView_Pattern);
//        String pattern;
//        switch (value) {
//            case "2":
//                pattern = "Waves";
//                break;
//            case "3":
//                pattern = "Rainbow Short";
//                break;
//            case "4":
//                pattern = "Rainbow Long";
//                break;
//            case "6":
//                pattern = "Theater";
//                break;
//            case "8":
//                pattern = "Scanner";
//                break;
//            default:
//                pattern = "-";
//                break;
//        }
//        txtViewPattern.setText(pattern);
//    }
//
//    private void processColor(String value) {
//        if (value.length() == 10) {
//            char colorNumber = value.charAt(0);
//            int red = Integer.parseInt(value.substring(1, 4));
//            int green = Integer.parseInt(value.substring(4, 7));
//            int blue = Integer.parseInt(value.substring(7, 10));
//
//            int color = Color.rgb(red, green, blue);
//
//
//            switch (colorNumber) {
//                case '1':
//                    // get color view from card to change tint
//                    Button button_color1 = (Button) itemView.findViewById(R.id.button_color1);
//                    room.setColor1(color);
//                    button_color1.setBackgroundColor(color);
//                    break;
//                case '2':
//                    // get color view from card to change tint
//                    Button button_color2 = (Button) itemView.findViewById(R.id.button_color2);
//                    room.setColor2(color);
//                    button_color2.setBackgroundColor(color);
//                    break;
//                default:
//                    Snackbar.make(coordinatorLayout, "C_ERR_number: " + colorNumber, Snackbar.LENGTH_LONG).show();
//            }
//        } else {
//            Snackbar.make(coordinatorLayout, "C_ERR_value: " + value, Snackbar.LENGTH_LONG).show();
//        }
//    }
//
//    private void getVolkszaehlerData() {
//        String url = "http://192.168.178.27/middleware.php/data/1641b790-4799-11e6-9031-f75b35dde6e3.json?from=7+days+ago&tuples=14";
//        Log.d(DEBUG_TAG, "Request: " + url);
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            JSONObject data = response.getJSONObject("data");
//                            long time_start = data.getLong("from");
//                            long time_end = data.getLong("to");
//
//                            JSONArray array_min = data.getJSONArray("min");
//                            long time_min = array_min.getLong(0);
//                            double min_value = array_min.getDouble(1);
//
//                            JSONArray array_max = data.getJSONArray("max");
//                            long time_max = array_max.getLong(0);
//                            double max_value = array_max.getDouble(1);
//
//                            double average_value = data.getDouble("average");
//                            int rows = data.getInt("rows");
//
//                            Log.d(DEBUG_TAG, String.valueOf(min_value));
//
//                            JSONArray array_tuples = data.getJSONArray("tuples");
//                            for (int i = 0; i < rows; i++) {
//                                //array_tuples
//                            }
//                        } catch (JSONException e) {
//                            // Oops
//                        }
//                        Log.d(DEBUG_TAG, "Response: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO Auto-generated method stub
//
//                    }
//                });
//
//        // Access the RequestQueue through your singleton class.
//        MyWiFi.getInstance(this).addToRequestQueue(jsObjRequest);
//    }
}
