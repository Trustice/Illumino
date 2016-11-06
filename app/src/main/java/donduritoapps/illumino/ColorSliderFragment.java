package donduritoapps.illumino;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ColorSliderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorSliderFragment extends Fragment {
    private static final String ROOM_IP = "ROOM_IP";
    private static final String COLOR_NR = "COLOR_NR";

    private static final String DEBUG_TAG = "*** ColorSlider";
    private View view;
    private String room_ip;
    private int color_number;

    public ColorSliderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip Parameter 1.
     * @param color_nr Parameter 2.
     * @return A new instance of fragment ColorSliderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ColorSliderFragment newInstance(String ip, int color_nr) {
        ColorSliderFragment fragment = new ColorSliderFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_IP, ip);
        args.putInt(COLOR_NR, color_nr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            room_ip = getArguments().getString(ROOM_IP);
            color_number = getArguments().getInt(COLOR_NR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_color_slider, container, false);
        final int current_color;
        final TextView textView_currentColor = (TextView) view.findViewById(R.id.textView_current_color);

        SeekBar seekBar_red = (SeekBar) view.findViewById(R.id.seekBar_red);
        seekBar_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView textView_redValue = (TextView) view.findViewById(R.id.editText_redValue);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = 2.55 * progress;
                textView_redValue.setText(String.format("%.0f", value));

                ColorDrawable cd = (ColorDrawable) textView_currentColor.getBackground();
                int color_pre = cd.getColor();
                textView_currentColor.setBackgroundColor(
                        Color.rgb(
                                (int) value,
                                Color.green(color_pre),
                                Color.blue(color_pre)
                        ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar seekBar_green = (SeekBar) view.findViewById(R.id.seekBar_green);
        seekBar_green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView textView_greenValue = (TextView) view.findViewById(R.id.editText_greenValue);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = 2.55 * progress;
                textView_greenValue.setText(String.format("%.0f", value));

                ColorDrawable cd = (ColorDrawable) textView_currentColor.getBackground();
                int color_pre = cd.getColor();
                textView_currentColor.setBackgroundColor(
                        Color.rgb(
                                Color.red(color_pre),
                                (int) value,
                                Color.blue(color_pre)
                        ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar seekBar_blue = (SeekBar) view.findViewById(R.id.seekBar_blue);
        seekBar_blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView textView_blueValue = (TextView) view.findViewById(R.id.editText_blueValue);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = 2.55 * progress;
                textView_blueValue.setText(String.format("%.0f", value));

                ColorDrawable cd = (ColorDrawable) textView_currentColor.getBackground();
                int color_pre = cd.getColor();
                textView_currentColor.setBackgroundColor(
                        Color.rgb(
                                Color.red(color_pre),
                                Color.green(color_pre),
                                (int) value
                        ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fab_view) {
                TextView textView_currentColor = (TextView) view.findViewById(R.id.textView_current_color);
                ColorDrawable cd = (ColorDrawable) textView_currentColor.getBackground();
                int selectedColor = cd.getColor();
                String message = String.format("C%1d%03d%03d%03d",
                        color_number,
                        Color.red(selectedColor),
                        Color.green(selectedColor),
                        Color.blue(selectedColor));
                startRequest(room_ip, message);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startRequest(room_ip, String.format("C%1d_", color_number));
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
        MyWiFi.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }


    private void processResponse(String serverIP, String request, String response) {
        // resend request send if failed
        if (!request.contains("_") && !response.equals(request)) {
            //startRequest(serverIP, request);
            //Snackbar.make(fragment_view, "Request Error: " + request + "\nresponse: " + response, Snackbar.LENGTH_LONG).show();
            return;
        } else {
            if (!room_ip.equals(serverIP)) {
                return;
            }

            char state = response.charAt(0);
            String value = response.substring(1);
            Log.d(DEBUG_TAG, value);
            switch (state) {
                case 'C':   // Color
                    processColor(value);
                    break;
                default:
                    //Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void processColor(String value) {
        Log.d(DEBUG_TAG, value);
        if (value.length() == 10) {
            int red = Integer.parseInt(value.substring(1, 4));
            int green = Integer.parseInt(value.substring(4, 7));
            int blue = Integer.parseInt(value.substring(7, 10));

            int color = Color.rgb(red, green, blue);
            TextView textView_color = (TextView) view.findViewById(R.id.textView_current_color);
            textView_color.setBackgroundColor(color);

            SeekBar seekBar_red = (SeekBar) view.findViewById(R.id.seekBar_red);
            seekBar_red.setProgress((int) (red / 2.55));
            SeekBar seekBar_green = (SeekBar) view.findViewById(R.id.seekBar_green);
            seekBar_green.setProgress((int) (green / 2.55));
            SeekBar seekBar_blue = (SeekBar) view.findViewById(R.id.seekBar_blue);
            seekBar_blue.setProgress((int) (blue / 2.55));
        }
    }

}
