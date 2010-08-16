/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

/**
 * Takes a path to an image, then displays it by filling all available space while
 * retaining w/h ratio. This is meant to be a (poor) replacement to the native 
 * image viewer intent on some devices. For example, the nexus-one gallery viewer
 * takes about 11 seconds to start up when using the following:
 * 
 *     Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 *     intent.setDataAndType(uri, "image/" + extension);
 *     startActivity(intent);
 *     
 * other devices might have their own issues.
 * 
 * We can support zooming/panning later on if it's important to users. 
 * 
 * No attempt is made to check the size of the input image, for now we're trusting
 * the foursquare api is keeping these images < 200kb.
 * 
 * @date July 28, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 * 
 */
public class FullSizeImageActivity extends Activity {
    private static final String TAG = "FullSizeImageActivity";

    public static final String INTENT_EXTRA_IMAGE_PATH = Foursquared.PACKAGE_NAME
        + ".FullSizeImageActivity.INTENT_EXTRA_IMAGE_PATH";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_size_image_activity);
        
        String imagePath = getIntent().getStringExtra(INTENT_EXTRA_IMAGE_PATH);
        if (!TextUtils.isEmpty(imagePath)) {
            
            try {
                Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                ImageView iv = (ImageView)this.findViewById(R.id.imageView);
                iv.setImageBitmap(bmp);
            } catch (Exception ex) {
                Log.e(TAG, "Couldn't load supplied image.", ex);
                finish();
            }
            
        } else {
            Log.e(TAG, "You must supply the path to the image as an intent extra.");
            finish();
        }
    }
}
