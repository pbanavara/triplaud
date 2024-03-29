package com.triplaud.overlays;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.triplaud.common.Common;

/**
 * @author pradeep
 *
 * @param <Item>
 * Customization of ItemizedOerlay to display directions. The implementation has some known bugs.
 * Refer to this URL on how these are rectified.
 * http://developmentality.wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-workarounds/#comment-815
 */
public class DirectionsItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {
    private static final String TAG = "DirectionsItemizedOverlay";
    private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

    private Context context;


    public DirectionsItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
        // TODO Auto-generated constructor stub
    }

    public DirectionsItemizedOverlay(Drawable defaultMarker, Context context, MapView mapView) {
        super(boundCenterBottom(defaultMarker));
        this.context = context;


        // Workaround for bug that Google refuses to fix:
        // <a href="http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html">http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html</a>
        // <a href="http://code.google.com/p/android/issues/detail?id=2035">http://code.google.com/p/android/issues/detail?id=2035</a>
        populate();

    }



    public DirectionsItemizedOverlay(Drawable defaultMarker, Context context, View view) {
        super(boundCenterBottom(defaultMarker));
        this.context = context;
        // Workaround for bug that Google refuses to fix:
        // <a href="http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html">http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html</a>
        // <a href="http://code.google.com/p/android/issues/detail?id=2035">http://code.google.com/p/android/issues/detail?id=2035</a>
        populate();

    }

    @Override
    protected OverlayItem createItem(int i) {
        // TODO Auto-generated method stub
        return items.get(i);
    }


    @Override
    public int size() {
        // TODO Auto-generated method stub
        return items.size();
    }

    public void addOverlay(OverlayItem overlay) {
        Log.i(TAG, "Overlay called");
        items.add(overlay);
        setLastFocusedIndex(-1);
        populate();

    }

    @Override
    public boolean onTap(int index) {
        super.onTap(index);
        // TODO Auto-generated method stub
        OverlayItem item = items.get(index);
        Log.e(TAG, "Marker tapped");
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle(Common.DIALOG_TITLE);
        dialog.setMessage(item.getTitle());
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        dialog.show();
        //postMarkerData("maybe");
        return true;
    }

    public void clear() {
        items.clear();
        setLastFocusedIndex(-1);
        populate();
    }

}

