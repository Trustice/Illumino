package donduritoapps.illumino;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RoomActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino RoomAct";
    private CoordinatorLayout coordinatorLayout;
    //private WebRequest webRequest;

    private MyRoom room;
    private int radioButtonSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        room = new MyRoom(intent.getStringExtra("ROOM_NAME"), intent.getStringExtra("ROOM_IP"), R.drawable.ic_build_white_24dp);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_room);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(room.getName());
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        //webRequest = new WebRequest();

        createOnOffSwitch();
        createAnimationSwitch();
        createColorButtons();
        createPatternSelection();

        refreshActivity();
    }

    public void refreshActivity() {
        startRequest(room.getIp(), "P_");
        startRequest(room.getIp(), "C1_");
        startRequest(room.getIp(), "C2_");
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
            refreshActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private class WebRequest {
        // Function calls AsyncTask.
        // Before attempting to fetch the URL, makes sure that there is a network connection.
        public void sendGetRequest(String stringUrl) {
            // Gets the URL from the UI's text field.
            //stringUrl = urlText.getText().toString();
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask().execute(stringUrl);
            } else {
                Snackbar.make(coordinatorLayout, "No network connection available.", Snackbar.LENGTH_LONG).show();
            }
        }

        // Uses AsyncTask to create a task away from the main UI thread. This task takes a
        // URL string and uses it to create an HttpUrlConnection. Once the connection
        // has been established, the AsyncTask downloads the contents of the webpage as
        // an InputStream. Finally, the InputStream is converted into a string, which is
        // displayed in the UI by the AsyncTask's onPostExecute method.
        private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls) {
                // params comes from the execute() call: params[0] is the url.
                try {
                    return downloadUrl(urls[0]);
                } catch (IOException e) {
                    Log.d(DEBUG_TAG, "IOException");
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                // check the result of the request for the specified format
                if (result.contains("&")) {
                    String[] splitResult = result.split("&");
                    String serverIP = splitResult[0];
                    String request = splitResult[1];
                    String response = splitResult[2];

                    //responseMsg.setText(String.valueOf(content.indexOf("\r\n")) + "\n" + String.valueOf(content.length()));
                    processResponse(serverIP, request, response);
                } else {
                    String message = "Communication Error:\n" + result;
                    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
                }
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as a string.
        private String downloadUrl(String myUrl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentRaw = readIt(is, len);
                int contentLen = contentRaw.indexOf("\r\n");
                // Termination characters \r\n should be there
                if (contentLen != -1) {
                    String[] urlSplit = myUrl.split("/");
                    String serverIP = urlSplit[2];
                    String request = urlSplit[3];
                    String content = contentRaw.substring(0, contentLen).replace("!", "");
                    Log.d(DEBUG_TAG, "The content is: " + serverIP + "&" + content);
                    return serverIP + "&" + request + "&" + content;
                } else {
                    return "invalid response content";
                }
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }

    public void startRequest(final String ip, final String message) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(RoomActivity.this, "No network access", Toast.LENGTH_LONG).show();
            return;
        }

        final String url = "http://" + ip + "/" + message;
        Log.d(DEBUG_TAG, "Request: " + url);
        //webRequest.sendGetRequest(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(DEBUG_TAG, "Response PASS!!!" + url);
                        Log.d(DEBUG_TAG, "Response: " + response.replace("\r\n",""));
                        processResponse(ip, message, response.replace("!\r\n",""));
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

    private void createPatternSelection() {
        // Pattern Selection
        View actionPattern = findViewById(R.id.action_Pattern);
        if (actionPattern != null) {
            actionPattern.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AppCompatDialog dialog = new AppCompatDialog(RoomActivity.this);
                    dialog.setContentView(R.layout.dialog_pattern);
                    dialog.setTitle("Pattern");
                    dialog.show();

                    RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                    if (radioGroup == null) { return; }
                    radioGroup.check(radioButtonSelection);
                    Button buttonOK = (Button) dialog.findViewById(R.id.button_okay);
                    if (buttonOK == null) { return; }
                    buttonOK.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                            int selectedId = radioGroup.getCheckedRadioButtonId();
                            radioButtonSelection = selectedId;
                            // find the radiobutton by returned id
                            RadioButton radioButton = (RadioButton) dialog.findViewById(selectedId);
                            TextView txtViewPattern = (TextView) findViewById(R.id.textView_Pattern);
                            String pattern = radioButton.getText().toString();
                            txtViewPattern.setText(pattern);
                            switch (pattern) {
                                case "Waves":
                                    startRequest(room.getIp(), "P2");
                                    break;
                                case "Rainbow Short":
                                    startRequest(room.getIp(), "P3");
                                    break;
                                case "Rainbow Long":
                                    startRequest(room.getIp(), "P4");
                                    break;
                                case "Theater":
                                    startRequest(room.getIp(), "P6");
                                    break;
                                case "Scanner":
                                    startRequest(room.getIp(), "P8");
                                    break;
                                default:
                                    break;
                            }
                            Snackbar.make(coordinatorLayout, radioButton.getText(), Snackbar.LENGTH_SHORT)
                                    .show();
                            dialog.dismiss();
                        }
                    });

                    Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
                    if (buttonCancel != null) {
                        buttonCancel.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });

        }
    }

    private void createOnOffSwitch() {
        //Switch Button for ON - OFF
        SwitchCompat switchOnOff = (SwitchCompat) findViewById(R.id.switch_on_off);
        switchOnOff.setEnabled(false);
        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String pattern = room.getPattern();
                if (isChecked) {
                    if (pattern.equals("99") || pattern.equals("97")) {
                        startRequest(room.getIp(), "P98");
                    }
                } else {
                    if (!pattern.equals("99") || !pattern.equals("97")) {
                        startRequest(room.getIp(), "P99");
                    }
                }
                refreshActivity();
            }
        });
    }

    private void createAnimationSwitch() {
        //Switch Button for Animation
        SwitchCompat switchAnimation = (SwitchCompat) findViewById(R.id.switch_animation);
        if (switchAnimation == null) { return; }
        switchAnimation.setEnabled(true);
        switchAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String pattern = room.getPattern();
                if (isChecked) {
                    switch (pattern) {
                        case "1":
                        case "5":
                        case "7":
                        case "98":
                            startRequest(room.getIp(), "P2");
                            break;
                        default:
                            break;
                    }
                } else { // not checked
                    switch (pattern) {
                        case "1":
                        case "5":
                        case "7":
                        case "98":
                            break;
                        default:
                            startRequest(room.getIp(), "P5");
                            break;
                    }
                }
            }
        });
    }

    private void createColorButtons() {
        // Button Color 1
        Button button_color1 = (Button) findViewById(R.id.button_color1);
        colorButtonListener(button_color1);
        Button button_color2 = (Button) findViewById(R.id.button_color2);
        colorButtonListener(button_color2);
    }

    private void colorButtonListener(final Button button_color) {
        button_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AppCompatDialog dialog = new AppCompatDialog(RoomActivity.this);
                dialog.setContentView(R.layout.dialog_color);
                dialog.setTitle(button_color.getText());
                dialog.show();

                ImageView colorWheel = (ImageView) dialog.findViewById(R.id.imageView_color_wheel);
                //colorWheel.setOnTouchListen;

                Button buttonOK = (Button) dialog.findViewById(R.id.button_ok);
                buttonOK.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        Snackbar.make(coordinatorLayout, "OK", Snackbar.LENGTH_SHORT).show();
                    }
                });

                Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    // extract information from result of a WebRequest
    private void processResponse(String serverIP, String request, String response) {
        // resend request send if failed
        if (!request.contains("_") && !response.equals(request)) {
            startRequest(serverIP, request);
        } else {
            if (!room.getIp().equals(serverIP)) {
                    return;
                }

            char state = response.charAt(0);
            String value = response.substring(1);
            switch (state) {
                case 'P':   // process as Pattern
                    room.setPattern(value);

                    // get button view from card to change tint

                    SwitchCompat switchCompatOnOff = (SwitchCompat) findViewById(R.id.switch_on_off);
                    switchCompatOnOff.setEnabled(true);
                    SwitchCompat switchCompatAnimation = (SwitchCompat) findViewById(R.id.switch_animation);
                    switchCompatAnimation.setEnabled(true);
                    switch (value) {
                        case "97":
                        case "99":
                            switchCompatOnOff.setChecked(false);
                            switchCompatAnimation.setChecked(false);
                            break;
                        case "1":
                        case "5":
                        case "7":
                        case "98":
                            switchCompatOnOff.setChecked(true);
                            switchCompatAnimation.setChecked(false);
                            break;
                        default:
                            switchCompatOnOff.setChecked(true);
                            switchCompatAnimation.setChecked(true);
                            break;
                    }
                    break;
                case 'C':   // Color
                    if (value.length() == 10) {
                        char colorNumber = value.charAt(0);
                        int red = Integer.parseInt(value.substring(1, 4));
                        int green = Integer.parseInt(value.substring(4, 7));
                        int blue = Integer.parseInt(value.substring(7, 10));

                        int color = Color.rgb(red, green, blue);


                        switch (colorNumber) {
                            case '1':
                                // get color view from card to change tint
                                Button button_color1 = (Button) findViewById(R.id.button_color1);
                                room.setColor1(color);
                                button_color1.setBackgroundColor(color);
                                break;
                            case '2':
                                // get color view from card to change tint
                                Button button_color2 = (Button) findViewById(R.id.button_color2);
                                room.setColor2(color);
                                button_color2.setBackgroundColor(color);
                                break;
                            default:
                                Snackbar.make(coordinatorLayout, "C_ERR_number: " + colorNumber, Snackbar.LENGTH_LONG).show();
                        }
                        //Color.rgb(red, green, blue);
                    } else {
                        Snackbar.make(coordinatorLayout, "C_ERR_value: " + value, Snackbar.LENGTH_LONG).show();
                    }
                    break;
                default:
                    Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}