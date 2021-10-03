package de.xorg.gsapp.wear;

import android.app.Activity;
import android.os.Bundle;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;


public class MainActivity extends Activity {

    WearableRecyclerView mRV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRV = findViewById(R.id.recyclerView);
        mRV.setEdgeItemsCenteringEnabled(true);
        mRV.setLayoutManager(
                new WearableLinearLayoutManager(this));
        mRV.setCircularScrollingGestureEnabled(true);
        mRV.setBezelFraction(0.5f);
        mRV.setScrollDegreesPerScreen(90);


    }
}
