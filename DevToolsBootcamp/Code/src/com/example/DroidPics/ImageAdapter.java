// Copyright 2012 Google Inc. All Rights Reserved.

package com.example.DroidPics;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author trevorjohns@google.com (Trevor Johns)
 *
 */
public class ImageAdapter extends BaseAdapter {
  private Context mContext;
  private int mImageCount;
  
  private static final int[] IMAGES = {R.drawable.big_droid,
                                       R.drawable.blue_balloon,
                                       R.drawable.blue_bike,
                                       R.drawable.chrome_wheel,
                                       R.drawable.cupcake,
                                       R.drawable.donut,
                                       R.drawable.eclair,
                                       R.drawable.froyo,
                                       R.drawable.green_balloon,
                                       R.drawable.punk_droid,
                                       R.drawable.rainbow_bike,
                                       R.drawable.red_balloon,
                                       R.drawable.stargazer_droid};
  
  public ImageAdapter(Context c) {
      mContext = c;
  }

  public int getCount() {
      return mImageCount;
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
    int selectedImage = IMAGES[position % IMAGES.length];
    
    // Exercise 2: This code recycles views inside of our GridView as we
    //             scroll.
    //if (convertView == null) {  // if it's not recycled, initialize some attributes
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(new GridView.LayoutParams(285, 285));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);
    //} else {
    //    imageView = (ImageView) convertView;
    //}
    
    // Exercise 4.2: Only load new image if it's not already loaded
    // if (imageView.getTag() == null ||
    //      ((Integer) imageView.getTag() != selectedImage)) {
      // Exercise 4.2: Add metadata to images for background thread.
      // imageView.setImageDrawable(null);
      // imageView.setTag(selectedImage);
       
      imageView.setImageResource(selectedImage);
       // Exercise 4.1: Use the code below to scale bitmaps
       // BitmapFactory.Options options = new BitmapFactory.Options();
       // options.inSampleSize = 4;
       // Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), selectedImage, options);
       // imageView.setImageBitmap(b);

// Exercise 4.2: Uncomment this to load images on a background thread, once
// you realize that image scaling isn't enough. :)
// You'll need to also uncomment ImageLoaderTask below, and comment out
// calls to BitmapFactor and setImageBitmap() above. Also, see calls to
// setImageDrawable() and setTag() above.
//       new ImageLoaderTask(mContext).execute(imageView);
     //}
     return imageView;
  }

  public void addPhotos(int count) {
    mImageCount += count;
    notifyDataSetChanged();
  }

// Exercise 4.2: ImageLoaderTask implementation. (See above.)
//  private class ImageLoaderTask extends AsyncTask<ImageView, Void, Bitmap> {
//    private ImageView mView;
//    private int mSelectedImage;
//    private Context mCtx;
//
//    public ImageLoaderTask(Context context) {
//      mCtx = context;
//    }
//    
//    /* (non-Javadoc)
//     * @see android.os.AsyncTask#doInBackground(Params[])
//     */
//    @Override
//    protected Bitmap doInBackground(ImageView... argv) {
//      mView = argv[0];
//      mSelectedImage = (Integer) mView.getTag();
//      BitmapFactory.Options options = new BitmapFactory.Options();
//      options.inSampleSize = 4;
//      Bitmap image = BitmapFactory.decodeResource(mCtx.getResources(),
//                                                  mSelectedImage, options);
//      return image;
//    }
//    
//    @Override
//    protected
//    void onPostExecute(Bitmap result) {
//      // Check to make sure this view hasn't been recycled while the bitmap
//      // was loading
//      if (mSelectedImage == (Integer) mView.getTag()) {
//        mView.setImageBitmap(result);
//      }
//    }
//    
//    
//  }
}
