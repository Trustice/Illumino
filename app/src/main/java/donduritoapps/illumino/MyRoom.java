package donduritoapps.illumino;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * Created by User on 03.12.2015.
 */
public class MyRoom {
    //
    private String name, ip, pattern;
    private int iconID, color1, color2;

    public MyRoom(String name, String ip, int iconID) {
        super();
        this.name = name;
        this.iconID = iconID;
        this.ip = ip;


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

    public void setColor1(int color) {
        this.color1 = color;
    }

    public void setColor2(int color) {
        this.color2 = color;
    }

//    public String getColor1() {
//        return this.color1;
//    }
//
//    public String getColor2() {
//        return this.color2;
//    }
}
