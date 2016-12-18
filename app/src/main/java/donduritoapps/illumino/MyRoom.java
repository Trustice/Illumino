package donduritoapps.illumino;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by User on 03.12.2015.
 */
public class MyRoom {
    private static final String DEBUG_TAG = "*** MyRoom Class";
    private Context context;
    private String name, ip, pattern, temperature_uuid, temperature, humidity_uuid, humidity;
    private JSONArray stripe_names;
    private int iconID, color1, color2, interval, stripes_num, room_state, brightness;
    private boolean dht, pir;
    private List<List<Integer>> colorLists = new ArrayList<List<Integer>>();
    private List<Integer> patternList = new ArrayList<Integer>();
    private List<Integer> intervalList = new ArrayList<>();
    private JSONObject jObj_room;

    public MyRoom(Context context, int roomIndex) {
        super();
        this.context = context;
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            this.jObj_room = new JSONObject(sharedPref.getString("ROOM_JSON_" + roomIndex, "{}"));
            this.name = jObj_room.getString("name");
            this.ip = jObj_room.getString("ip");
            this.iconID = jObj_room.getInt("icon_id");
            this.stripe_names = jObj_room.getJSONArray("stripes");
            JSONObject jObj_dht = jObj_room.getJSONObject("dht");
            this.temperature_uuid = jObj_dht.getString("t_uuid");
            this.humidity_uuid = jObj_dht.getString("h_uuid");
            this.pir = jObj_room.getBoolean("pir");
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "unexpected JSON exception", e);
        }

        this.stripes_num = this.stripe_names.length();
        this.brightness = 0;
        if (this.temperature_uuid.equals("nan"))
            this.dht = false;
        else
            this.dht = true;
        this.temperature = "nan";
        this.humidity = "nan";

        this.room_state = 0;

        List<Integer> colorList = new ArrayList<>();
        for (int j = 0; j <= 9; j++)
            colorList.add(j, 0);
        for (int i = 0; i < this.stripes_num; i++) {
            this.patternList.add(i, 0);
            this.colorLists.add(i, colorList);
            this.intervalList.add(i, 0);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getIp() {
        return this.ip;
    }

    public int getIconID() {
        return iconID;
    }

    public int getStripes_num() { return this.stripes_num; }

    //public String getStripeNames() {
//        return this.stripe_names;
//    }
    public String getStripeName(int index) {
        if (index < this.stripes_num) {
            try {
                return stripe_names.getString(index);
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "unexpected JSON exception", e);
            }
        }
        return "STRIPE NAME ERROR";
    }

    public void setRoomState(int room_state) {
        this.room_state = room_state;
    }

    public int getRoomState() { return this.room_state; }

    public void setPattern(int stripe_index, int pattern_code) {
        if (stripe_index >= this.stripes_num)
            return;
        this.patternList.set(stripe_index, pattern_code);
    }

    public int getPattern(int stripes_index) {
        if (stripes_index >= this.stripes_num)
            return 0;
        return this.patternList.get(stripes_index);
    }

    public void setColor(int stripes_index, int color_index, int color) {
        if (stripes_index >= this.stripes_num)
            return;
        List<Integer> colorList = this.colorLists.get(stripes_index);
        colorList.set(color_index, color);
        this.colorLists.set(stripes_index, colorList);
    }

    public int getColor(int stripes_index, int color_index) {
        List<Integer> colorList = this.colorLists.get(stripes_index);
        return colorList.get(color_index);
    }

    public void setInterval(int stripe_index, int value) {
        if (stripe_index >= this.stripes_num)
            return;
        this.intervalList.set(stripe_index, value);
    }

    public int getInterval(int stripe_index) {
        if (stripe_index >= this.stripes_num)
            return 0;
        return this.intervalList.get(stripe_index);
    }

    public boolean getDhtState() { return this.dht; }

    public boolean getPirState() { return this.pir; }

    public String getTemperatureUuid () { return this.temperature_uuid; }

    public String getHumidityUuid () { return this.humidity_uuid; }

    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getTemperature() { return this.temperature; }

    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getHumidity() { return this.humidity; }

    public void setBrightness(int value) {
        this.brightness = value;
    }

    public int getBrightness() { return this.brightness; }
}
