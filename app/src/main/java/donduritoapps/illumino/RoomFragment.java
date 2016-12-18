package donduritoapps.illumino;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

//import static donduritoapps.illumino.R.id.cast_notification_id;
//import static donduritoapps.illumino.R.id.coordinatorLayout;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomFragment extends Fragment {
    private static final String DEBUG_TAG = "*** RoomFragment";
    private static final String ARG_ROOM_INDEX = "0";


//    private static final String ARG_ROOM_NAME = "Room";
//    private static final String ARG_ROOM_IP = "255.255.255.255";

    private View fragment_view;
    private MyRoom room;
    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    private List<String> stripeList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int radioButtonSelection;
    private boolean pir_enable = true;
    private List<Boolean> send_enable = new ArrayList<>();
    private SwitchCompat switch_Toolbar;
    private Boolean send_main_switch_enable = true;

    public RoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomIndex Parameter 1.
     *
     * @return A new instance of fragment RoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoomFragment newInstance(Integer roomIndex) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ROOM_INDEX, roomIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        room = new MyRoom(getContext(), getArguments().getInt(ARG_ROOM_INDEX));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_room, menu);

        MenuItem item_switch_Toolbar = menu.findItem(R.id.action_switch);
        switch_Toolbar = (SwitchCompat) item_switch_Toolbar.getActionView().findViewById(R.id.switchForActionBar);
        switch_Toolbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (send_main_switch_enable) {
                    if (isChecked) {
                        startRequest(room.getIp(), "P" + (room.getStripes_num() * 100 + 1));
                    } else {
                        startRequest(room.getIp(), "P" + (room.getStripes_num() * 100));
                    }
                    refreshFragment();
                }
                send_main_switch_enable = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_brightness) {
            int brightness = room.getBrightness();

            final AppCompatDialog dialog = new AppCompatDialog(getActivity());
            dialog.setContentView(R.layout.dialog_template);

            TextView dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
            dialogTitle.setText("Helligkeit");

            LinearLayout ll_dialog = (LinearLayout) dialog.findViewById(R.id.linearLayout_dialog);

            TextInputLayout textInputLayout_brightness = new TextInputLayout(getActivity());
            textInputLayout_brightness.setHint("0 - 255");
            final TextInputEditText editText_brightness = new TextInputEditText(getActivity());
            textInputLayout_brightness.addView(editText_brightness);
            editText_brightness.setText(String.valueOf(brightness));
            ll_dialog.addView(textInputLayout_brightness);

//            final TextView textView_currentBrightness = new TextView(getContext());
//            textView_currentBrightness.setText(String.valueOf(brightness));
//            ll_dialog.addView(textView_currentBrightness);

            SeekBar seekBar_Brightness = new SeekBar(getActivity());
            seekBar_Brightness.setProgress(brightness / 5);
            seekBar_Brightness.setMax(51); // 51 * 5 = 255
            seekBar_Brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    editText_brightness.setText(String.valueOf(progress * 5));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    String message = "B" + progress * 5;
                    startRequest(room.getIp(), message);
                }
            });
            ll_dialog.addView(seekBar_Brightness);

            dialog.show();
        }

        if (id == R.id.action_exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_view = inflater.inflate(R.layout.fragment_room, container, false);

        ((MainActivity) getActivity()).setActionBarTitle(room.getName());

        for (int i = 0; i < room.getStripes_num(); i++) {
            stripeList.add(room.getStripeName(i));
            send_enable.add(i, true);
        }

        populateListView();

        swipeRefreshLayout = (SwipeRefreshLayout) fragment_view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshFragment();
                    }
                }
        );

        for (int i=0; i < room.getStripes_num(); i++) {
            startRequest(room.getIp(), "C" + ((i * 100)  + 10) + "_");
        }
        startRequest(room.getIp(), "B_");
        refreshFragment();
        return fragment_view;
    }

    private void populateListView() {
        ArrayAdapter<String> adapter = new MyListAdapter();
        ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
        if (list == null) return;
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<String> {
        public MyListAdapter() {
            super(getActivity(), R.layout.item_room, stripeList);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)

            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.item_room, parent, false);
            }

            // Find the room to work with.
//            final MyRoom currentRoom = roomList.get(position);
            // Stripe Name:
            String stripeName = stripeList.get(position);
            TextView nameText = (TextView) itemView.findViewById(R.id.textView_StripeName);
            nameText.setText(stripeName);

            handleOnOffSwitch(itemView, position);
            handleStripeAction(itemView, position);
            handleStripeInterval(itemView, position);

            return itemView;
        }

        private void handleOnOffSwitch(final View itemView, final int position) {
            //Switch Button for ON - OFF
            SwitchCompat switchOnOff = (SwitchCompat) itemView.findViewById(R.id.switch_on_off);
            switchOnOff.setEnabled(false);
            switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(DEBUG_TAG, "send_enable_" + position + ": " + send_enable.get(position));
                    if (send_enable.get(position)) {
                        int pattern = room.getPattern(position);
                        if (isChecked) {
                            if (pattern == 0)
                                startRequest(room.getIp(), "P" + (position * 100 + 1));
                        } else {
                            if (!(pattern == 0))
                                startRequest(room.getIp(), "P" + (position * 100));
                        }
                        refreshFragment();
                    }
                    send_enable.set(position, true);
                }
            });
        }

        private void handleStripeAction(final View itemView, final int position) {
            // Pattern Selection
            View stripeActionView = itemView.findViewById(R.id.stripeAction);
            stripeActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AppCompatDialog dialog = new AppCompatDialog(getActivity());
                    dialog.setContentView(R.layout.dialog_template);
                    dialog.setTitle(stripeList.get(position));

                    LinearLayout linearLayout_dialog = (LinearLayout) dialog.findViewById(R.id.linearLayout_dialog);

                    // GridView for setting Static Pattern
                    GridView gV_staticButtons = new GridView(getContext());
                    gV_staticButtons.setNumColumns(4);
                    gV_staticButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_staticButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "STATIC"));
                    gV_staticButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_staticButtons);

                    // GridView for setting Rainbow Pattern
                    GridView gV_rainbowButtons = new GridView(getContext());
                    gV_rainbowButtons.setNumColumns(4);
                    gV_rainbowButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_rainbowButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "RAINBOW"));
                    gV_rainbowButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, 20 + i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_rainbowButtons);

                    // GridView for setting Fire Pattern
                    GridView gV_fireButtons = new GridView(getContext());
                    gV_fireButtons.setNumColumns(4);
                    gV_fireButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_fireButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "FIRE"));
                    gV_fireButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, 30 + i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_fireButtons);

                    dialog.show();
                }
            });
        }

        private class GridAdapter_pattern extends BaseAdapter {
            private Context mContext;
            private int stripeNumber;
            private int patternMax;
            private String mode;

            public GridAdapter_pattern(Context c, int stripeNr, String patternMode) {
                this.stripeNumber = stripeNr;
                this.mode = patternMode;
                switch (mode) {
                    case "STATIC":
                    case "RAINBOW":
                        this.patternMax = 10;
                        break;
                    case "FIRE":
                        this.patternMax = 4;
                    default:
                        break;
                }
                mContext = c;
            }

            public int getCount() {
                return patternMax;
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

                switch (mode) {
                    case "STATIC":
                        imageView.setImageResource(R.drawable.ic_brightness_1_white_36dp);
                        imageView.setColorFilter(room.getColor(this.stripeNumber, position));
                        break;
                    case "RAINBOW":
                        imageView.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);
                        break;
                    case "FIRE":
                        imageView.setImageResource(R.drawable.ic_fire_white_24dp);
                    default:
                        break;
                }

                return imageView;
            }
        }

        private void handleStripeInterval(final View itemView, final int position) {
            final ImageButton imageButton_Interval = (ImageButton) itemView.findViewById(R.id.imageButton_interval);
            imageButton_Interval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int interval = room.getInterval(position);
                    final AppCompatDialog dialog = new AppCompatDialog(getActivity());
                    dialog.setContentView(R.layout.dialog_template);

                    TextView dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
                    dialogTitle.setText(stripeList.get(position) + " Interval");

                    LinearLayout ll_dialog = (LinearLayout) dialog.findViewById(R.id.linearLayout_dialog);


                    final TextInputLayout textInputLayout_interval = new TextInputLayout(getActivity());
                    textInputLayout_interval.setHint("Interval: 0 - 9999 [ms]");
                    final TextInputEditText editText_interval = new TextInputEditText(getActivity());
                    editText_interval.setText(String.valueOf(interval));
                    editText_interval.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText_interval.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                int interval =  Integer.parseInt(editText_interval.getText().toString());
                                String message = String.format("I%d%04d", position, interval);
                                startRequest(room.getIp(), message);
                                editText_interval.clearFocus();
                                // Check if no view has focus:
                                View view = getActivity().getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                    textInputLayout_interval.addView(editText_interval);
                    ll_dialog.addView(textInputLayout_interval);

                    SeekBar seekBar_Interval = new SeekBar(getActivity());
                    seekBar_Interval.setMax(500);
                    double progress = 54.28739969 * Math.log(interval);
                    seekBar_Interval.setProgress((int) progress);

                    seekBar_Interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int dimension = progress / 10;
                            int dim_index = progress % 10;
                            int interval_raw = (int) Math.round(Math.exp(0.01842048 * progress));
                            int interval;
                            if ( interval_raw > 9750)
                                interval = 9999;
                            else if ( interval_raw > 999 )
                                interval = (interval_raw / 250) * 250;
                            else if ( interval_raw > 99 )
                                interval = (interval_raw / 50) * 50;
                            else if ( interval_raw > 9 )
                                interval = (interval_raw / 5) * 5;
                            else
                                interval = interval_raw;

                            editText_interval.setText(String.valueOf(interval));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            int progress = seekBar.getProgress();
                            int interval =  Integer.parseInt(editText_interval.getText().toString());
                            String message = String.format("I%d%04d", position, interval);
                            startRequest(room.getIp(), message);
                        }
                    });
                    ll_dialog.addView(seekBar_Interval);

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface arg0) {
                            refreshFragment();
                        }
                    });
                    dialog.show();
                }
            });
        }

    }

    private void refreshFragment() {
        if (room.getPirState()) {
            startRequest(room.getIp(), "M_");
        }
        else {
            startRequest(room.getIp(), "P" + room.getStripes_num() + "_");
        }

        // turn off Spinner
        swipeRefreshLayout.setRefreshing(false);
    }


    public void startRequest(final String ip, final String message) {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(getActivity(), "No network access", Toast.LENGTH_LONG).show();
            return;
        }

        final String url = "http://" + ip + "/" + message;
        Log.d(DEBUG_TAG, "Request: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(DEBUG_TAG, "Response PASS!!!" + url + ": " + response.replace("!",""));
                        processResponse(ip, message, response.replace("!",""));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(DEBUG_TAG, "Response FAIL!!!: " + url);
            }
        });

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MyWiFi.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    // extract information from result of a WebRequest
    private void processResponse(String serverIP, String request, String response) {
        if (!room.getIp().equals(serverIP)) {
            return;
        }

        char state = response.charAt(0);
        String value = response.substring(1);

        switch (state) {
            case 'M':    // PIR
                switch (value) {
                    case "0":
                        pir_enable = false;
                        break;
                    case "10":
                        pir_enable = false;
                        break;
                    case "11":
                        pir_enable = true;
                        break;
                    default:
                        return;
                }
                startRequest(room.getIp(), "P" + room.getStripes_num() + "_");
                break;
            case 'P':   // process as Pattern
                processPattern(value);
                break;
            case 'C':   // Color
                processColor(value);
                break;
            case 'B':
                processBrightness(value);
                break;
            case 'I':
                processInterval(value);
                break;
            default:
//                Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
        }
    }

    private void processPattern(String value) {
        String pattern_string = value;
        if (value.contains(":")) {
            pattern_string = value.split(":")[0];
            String interval_string = value.split(":")[1];
            int interval = Integer.parseInt(interval_string.substring(1));
            room.setInterval(interval / 10000, interval % 10000);
        }
        int pattern_value_int = Integer.parseInt(pattern_string);
        int stripeNumber =  pattern_value_int / 100;
        int pattern = pattern_value_int % 100;


        if (stripeNumber == room.getStripes_num()) {

            if (pir_enable || pattern == 0) {
                if (switch_Toolbar.isChecked()) {
                    send_main_switch_enable = false;
                    switch_Toolbar.setChecked(false);
                }
            } else {
                if (!switch_Toolbar.isChecked()) {
                    send_main_switch_enable = false;
                    switch_Toolbar.setChecked(true);
                }
            }

            for (int i = 0; i < room.getStripes_num(); i++)
                startRequest(room.getIp(), "P" + i + "_");
        } else {
            ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
            View listChild = list.getChildAt(stripeNumber);

            SwitchCompat switchCompatOnOff = (SwitchCompat) listChild.findViewById(R.id.switch_on_off);
            switchCompatOnOff.setEnabled(true);

            // set Main Switch of the Stripe
            if ( (pattern == 0) && (switchCompatOnOff.isChecked()) ) {
                send_enable.set(stripeNumber, false);
                switchCompatOnOff.setChecked(false);
            } else if ( !(pattern == 0) && !(switchCompatOnOff.isChecked()) ) {
                send_enable.set(stripeNumber, false);
                switchCompatOnOff.setChecked(true);
            }

            // set Icon of the Stripe
            ImageView imageView_icon = (ImageView) listChild.findViewById(R.id.imageView_stripeIcon);
            if ( (pattern >= 0) && (pattern <= 9) ) {
                imageView_icon.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
                startRequest(room.getIp(), "C" + ((stripeNumber *100) + pattern) + "_");
            } else if ( (pattern >= 20) && (pattern <= 29) ) {
                imageView_icon.clearColorFilter();
                imageView_icon.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);
            } else if ( (pattern >= 30) && (pattern <= 33) ) {
                imageView_icon.setImageResource(R.drawable.ic_fire_white_24dp);
                imageView_icon.setColorFilter(Color.RED);
            }
            room.setPattern(stripeNumber, pattern);
        }
    }

    private void processColor(String value) {
        if (value.contains(":")) {
            int value_info = Integer.parseInt(value.split(":")[0]);
            int stripeNumber = value_info / 100;
            int colorNumber = value_info % 100;
            String color_string = value.split(":")[1];
            int color = 0;
            int red, green, blue;

            if ( color_string.contains(";")) {
                if (colorNumber == 10) {
                    for (int i = 0; i <= 9; i++) {
                        red = Integer.parseInt(color_string.split(";")[i].split(",")[0]);
                        green = Integer.parseInt(color_string.split(";")[i].split(",")[1]);
                        blue = Integer.parseInt(color_string.split(";")[i].split(",")[2]);
                        color = Color.rgb(red, green, blue);
                        room.setColor(stripeNumber, i, color);
                    }
                }
            } else if ( color_string.contains("[[") ) {
                if (colorNumber == 10) {
                    try {
                        JSONArray jArr_colors = new JSONArray(color_string);
                        for (int i = 0; i <= 9; i++) {
                            JSONArray jArr_color = jArr_colors.getJSONArray(i);
                            red = jArr_color.getInt(0);
                            green = jArr_color.getInt(1);
                            blue = jArr_color.getInt(2);
                            color = Color.rgb(red, green, blue);
                            room.setColor(stripeNumber, i, color);
                        }
                    } catch (JSONException e) {
                        Log.e(DEBUG_TAG, "unexpected JSON exception", e);
                    }
                }
            } else {
                if (color_string.contains("[") ) {
                    try {
                        JSONArray jArr_color = new JSONArray(color_string);
                        red = jArr_color.getInt(0);
                        green = jArr_color.getInt(1);
                        blue = jArr_color.getInt(2);
                        color = Color.rgb(red, green, blue);
                        room.setColor(stripeNumber, colorNumber, color);
                    } catch (JSONException e) {
                        Log.e(DEBUG_TAG, "unexpected JSON exception", e);
                    }
                } else {
                    red = Integer.parseInt(color_string.split(",")[0]);
                    green = Integer.parseInt(color_string.split(",")[1]);
                    blue = Integer.parseInt(color_string.split(",")[2]);

                    color = Color.rgb(red, green, blue);
                    room.setColor(stripeNumber, colorNumber, color);
                }

                // get color view from card to change tint
                ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
                View listChild = list.getChildAt(stripeNumber);
                ImageView imageView_icon = (ImageView) listChild.findViewById(R.id.imageView_stripeIcon);
                if (colorNumber == 0)
                    imageView_icon.setColorFilter(Color.DKGRAY);
                else
                    imageView_icon.setColorFilter(color);

            }
        }
    }

    private void processBrightness(String value) {
        int value_int = Integer.parseInt(value);
        if (value_int <= 255) {
            room.setBrightness(value_int);
        }
    }

    private void processInterval(String value) {
        int value_int = Integer.parseInt(value);
        int stripeNumber = value_int / 10000;
        int interval = value_int % 10000;
        room.setInterval(stripeNumber, interval);
    }

    private void getVolkszaehlerData() {
        String url = "http://192.168.178.27/middleware.php/data/1641b790-4799-11e6-9031-f75b35dde6e3.json?from=7+days+ago&tuples=14";
        Log.d(DEBUG_TAG, "Request: " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            long time_start = data.getLong("from");
                            long time_end = data.getLong("to");

                            JSONArray array_min = data.getJSONArray("min");
                            long time_min = array_min.getLong(0);
                            double min_value = array_min.getDouble(1);

                            JSONArray array_max = data.getJSONArray("max");
                            long time_max = array_max.getLong(0);
                            double max_value = array_max.getDouble(1);

                            double average_value = data.getDouble("average");
                            int rows = data.getInt("rows");

                            Log.d(DEBUG_TAG, String.valueOf(min_value));

                            JSONArray array_tuples = data.getJSONArray("tuples");
                            for (int i = 0; i < rows; i++) {
                                //array_tuples
                            }
                        } catch (JSONException e) {
                            // Oops
                        }
                        Log.d(DEBUG_TAG, "Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        // Access the RequestQueue through your singleton class.
        MyWiFi.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }
}
