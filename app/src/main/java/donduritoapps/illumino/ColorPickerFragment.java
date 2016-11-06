package donduritoapps.illumino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ColorPickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorPickerFragment extends Fragment {
    private static final String ROOM_IP = "ROOM_IP";
    private static final String COLOR_NR = "COLOR_NR";
    private static final String DEBUG_TAG = "*** ColorPicker";
    private View view;
    private String room_ip;
    private int color_number;


    public ColorPickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip Parameter 1.
     * @param color_nr Parameter 2.
     * @return A new instance of fragment ColorPickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ColorPickerFragment newInstance(String ip, int color_nr) {
        ColorPickerFragment fragment = new ColorPickerFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_color_picker, container, false);

        ImageView colorWheel = (ImageView) view.findViewById(R.id.imageView_color_wheel);

        if (colorWheel == null) return view;
        colorWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float eventX = event.getX();
                float eventY = event.getY();
                float[] eventXY = new float[] {eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView)v).getImageMatrix().invert(invertMatrix);
                invertMatrix.mapPoints(eventXY);
                int x = (int) eventXY[0];
                int y = (int) eventXY[1];

                Drawable imgDrawable = ((ImageView)v).getDrawable();
                Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();

                // Limit x, y, range within bitmap
                if (x < 0)
                    x = 0;
                else if (x > bitmap.getWidth())
                    x = bitmap.getWidth() - 1;

                if (y < 0)
                    y = 0;
                else if (y > bitmap.getHeight())
                    y = bitmap.getHeight() - 1;

                int touchedRGB = bitmap.getPixel(x, y);

                TextView textView_currentColor = (TextView) view.findViewById(R.id.textView_current_color);
                textView_currentColor.setBackgroundColor(touchedRGB);
                textView_currentColor.setTextColor(touchedRGB);
//                Log.d(DEBUG_TAG, String.format("%03d", Color.red(touchedRGB)));
//                Log.d(DEBUG_TAG, String.format("%03d", Color.green(touchedRGB)));
//                Log.d(DEBUG_TAG, String.format("%03d", Color.blue(touchedRGB)));
//                Log.d(DEBUG_TAG, String.format("%03d", Color.alpha(touchedRGB)));
                return false;
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

        startRequest(room_ip, String.format("C%1d_", color_number));

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
            Log.d(DEBUG_TAG, "Color changed?");
        }
    }

}
