package donduritoapps.illumino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ColorPickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorPickerFragment extends Fragment {
    private static final String DEBUG_TAG = "*** ColorPicker";
    private View view;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ColorPickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ColorPickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ColorPickerFragment newInstance(String param1, String param2) {
        ColorPickerFragment fragment = new ColorPickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
                if (textView_currentColor != null) {
                    textView_currentColor.setBackgroundColor(touchedRGB);
                    textView_currentColor.setTextColor(touchedRGB);
                }
                Log.d(DEBUG_TAG, String.format("%03d", Color.red(touchedRGB)));
                Log.d(DEBUG_TAG, String.format("%03d", Color.green(touchedRGB)));
                Log.d(DEBUG_TAG, String.format("%03d", Color.blue(touchedRGB)));
                Log.d(DEBUG_TAG, String.format("%03d", Color.alpha(touchedRGB)));
                return false;
            }
        });

        /*
        Button buttonOK = (Button) view.findViewById(R.id.button_ok);
        if (buttonOK == null) return view;
        buttonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int selectedColor = 0;
                TextView textView_currentColor = (TextView) view.findViewById(R.id.textView_current_color);
                if (textView_currentColor != null) {
                    selectedColor = textView_currentColor.getCurrentTextColor();
                }

                String message = String.format("C%1d%03d%03d%03d",
                        color_number,
                        Color.red(selectedColor),
                        Color.green(selectedColor),
                        Color.blue(selectedColor));

                startRequest(room.getIp(), message);
                Log.d(DEBUG_TAG, message);
                Snackbar.make(coordinatorLayout, "OK", Snackbar.LENGTH_SHORT).show();
            }
        });
        */

        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dialog.dismiss();
            }
        });


        return view;
    }

}
