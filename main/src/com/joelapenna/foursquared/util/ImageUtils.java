/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;

import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 * @date July 24, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class ImageUtils {
    
    private ImageUtils() {
    }

    public static void resampleImageAndSaveToNewLocation(String pathInput, String pathOutput) 
        throws Exception 
    {
        Bitmap bmp = resampleImage(pathInput, 640);
        
        OutputStream out = new FileOutputStream(pathOutput);
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out); 
    }
    
    public static Bitmap resampleImage(String path, int maxDim) 
        throws Exception {
        
        BitmapFactory.Options bfo = new BitmapFactory.Options(); 
        bfo.inJustDecodeBounds = true; 
        BitmapFactory.decodeFile(path, bfo); 
    
        BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
        optsDownSample.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
    
        Bitmap bmpt = BitmapFactory.decodeFile(path, optsDownSample);
    
        Matrix m = new Matrix(); 
        
        if (bmpt.getWidth() > maxDim || bmpt.getHeight() > maxDim) {
            BitmapFactory.Options optsScale = getResampling(bmpt.getWidth(), bmpt.getHeight(), maxDim);
            m.postScale((float)optsScale.outWidth  / (float)bmpt.getWidth(), 
                        (float)optsScale.outHeight / (float)bmpt.getHeight()); 
        }
         
        int sdk = new Integer(Build.VERSION.SDK).intValue(); 
        if (sdk > 4) {
            int rotation = ExifUtils.getExifRotation(path);
            if (rotation != 0) { 
                m.postRotate(rotation); 
            }
        }
        
        return Bitmap.createBitmap(bmpt, 0, 0, bmpt.getWidth(), bmpt.getHeight(), m, true); 
    }
    
    private static BitmapFactory.Options getResampling(int cx, int cy, int max) {
        float scaleVal = 1.0f;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        if (cx > cy) {
            scaleVal = (float)max / (float)cx;
        }
        else if (cy > cx) {
            scaleVal = (float)max / (float)cy;
        }
        else {
            scaleVal = (float)max / (float)cx;
        }
        bfo.outWidth  = (int)(cx * scaleVal + 0.5f);
        bfo.outHeight = (int)(cy * scaleVal + 0.5f);
        return bfo;
    }
    
    private static int getClosestResampleSize(int cx, int cy, int maxDim) {
        int max = Math.max(cx, cy);
        
        int resample = 1;
        for (resample = 1; resample < Integer.MAX_VALUE; resample++) {
            if (resample * maxDim > max) {
                resample--;
                break;
            }
        }
        
        if (resample > 0) {
            return resample;
        }
        return 1;
    }
    
    public static BitmapFactory.Options getBitmapDims(String path) throws Exception {
        BitmapFactory.Options bfo = new BitmapFactory.Options(); 
        bfo.inJustDecodeBounds = true; 
        BitmapFactory.decodeFile(path, bfo); 
        return bfo;
    }
}