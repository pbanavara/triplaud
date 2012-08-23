package com.example.DroidPics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.GridView;

import com.android.debug.hv.ViewServer;

public class MainActivity extends FragmentActivity {
    private GridView mGridView;
    private ImageAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGridView = (GridView) findViewById(R.id.gridView);
        mAdapter = new ImageAdapter(this);
        mGridView.setAdapter(mAdapter);
        ViewServer.get(this).addWindow(this);
    }
    
    public void addOneClicked(View v) {
      mAdapter.addPhotos(1);
    }
    
    public void addOneHundredClicked(View v) {
      mAdapter.addPhotos(100);
    }  
    
    @Override
    public void onDestroy() {
      super.onDestroy();
      ViewServer.get(this).removeWindow(this);
    }
  
    @Override
    public void onResume() {
      super.onResume();
      ViewServer.get(this).setFocusedWindow(this);
    }
}