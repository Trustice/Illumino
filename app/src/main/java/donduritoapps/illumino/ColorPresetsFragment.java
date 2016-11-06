package donduritoapps.illumino;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ColorPresetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorPresetsFragment extends Fragment {
    private static final String ROOM_IP = "ROOM_IP";
    private static final String COLOR_NR = "COLOR_NR";

    private static final String DEBUG_TAG = "*** ColorSlider";
    private View view;
    private String room_ip;
    private int color_number;

    public ColorPresetsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip Parameter 1.
     * @param color_nr Parameter 2.
     * @return A new instance of fragment ColorPresetsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ColorPresetsFragment newInstance(String ip, int color_nr) {
        ColorPresetsFragment fragment = new ColorPresetsFragment();
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
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        view = inflater.inflate(R.layout.fragment_color_presets, container, false);
        return view;
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
                        //processResponse(ip, message, response.replace("!\r\n",""));
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
}
