package donduritoapps.illumino;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by User on 03.12.2015.
 */
public class MyRoom {
    //
    private String name, ip, pattern, stripe_names;
    private int iconID, color1, color2, interval, stripes_num;
    private boolean dht, pir;
    private List<List<Integer>> colorLists= new ArrayList<List<Integer>>();
    private List<Integer> patternList = new ArrayList<Integer>();

    public MyRoom(String name, String ip, int iconID, String stripeNames, boolean dhtState, boolean pirState) {
        super();
        this.name = name;
        this.iconID = iconID;
        this.ip = ip;

        this.stripe_names = stripeNames;
        this.stripes_num = stripeNames.split(",").length;
        this.dht = dhtState;
        this.pir = pirState;
        for (int i = 0; i < this.stripes_num; i++) {
            this.patternList.add(i, 0);
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

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String value) {
        if (!value.equals(this.pattern)) {
            this.pattern = value;
            Log.d("+++ Illumino", this.pattern);
        }
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int value) {
        this.interval = value;
    }

    public void setColor1(int color) {
        this.color1 = color;
    }

    public void setColor2(int color) {
        this.color2 = color;
    }

    public String getStripeNames() {
        return this.stripe_names;
    }

    public boolean getDhtState() { return this.dht; }

    public boolean getPirState() { return this.pir; }

    public int getStripes_num() { return this.stripes_num; }

    public void setPattern(int stripe_index, int pattern_code) {
        this.patternList.set(stripe_index, pattern_code);
    }

    public int getPattern(int stripes_index) {
        if (stripes_index >= this.stripes_num)
            return 0;
        return this.patternList.get(stripes_index);
    }
}
