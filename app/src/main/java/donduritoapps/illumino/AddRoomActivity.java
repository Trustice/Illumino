package donduritoapps.illumino;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class AddRoomActivity extends AppCompatActivity {

    private Integer[] avatarIds = {
            R.drawable.ic_build_white_24dp,
            R.drawable.ic_weekend_white_24dp,
            R.drawable.ic_hot_tub_white_24dp,
            R.drawable.ic_restaurant_menu_white_24dp,
            R.drawable.ic_hotel_white_24dp
    };
    private int avatarSelectionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("New Room");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final GridView gridView = (GridView) findViewById(R.id.grid_room_icons);
        gridView.setAdapter(new GridAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ImageView avatarImage;
                for (int i = 0; i < avatarIds.length; i++) {
                    avatarImage = (ImageView) gridView.getChildAt(i);
                    if (i == position) {
                        avatarImage.setColorFilter(ContextCompat.getColor(AddRoomActivity.this, R.color.colorAccent));
                    }
                    else {
                        avatarImage.setColorFilter(Color.WHITE);
                    }
                }

                avatarSelectionIndex = position;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        if (id == R.id.action_save) {
            Intent returnIntent = new Intent();

            EditText roomName = (EditText) findViewById(R.id.editText_newRoomName);
            returnIntent.putExtra("ROOM_NAME",roomName.getText().toString());

            EditText roomIP = (EditText) findViewById(R.id.editText_newRoomIP);
            returnIntent.putExtra("ROOM_IP", roomIP.getText().toString());

            returnIntent.putExtra("ROOM_ICON", avatarIds[avatarSelectionIndex]);
            setResult(AddRoomActivity.RESULT_OK, returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class GridAdapter extends BaseAdapter {
        private Context mContext;

        public GridAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return avatarIds.length;
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

            imageView.setImageResource(avatarIds[position]);

            return imageView;
        }

    }
}
