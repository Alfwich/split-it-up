package com.arthurwut.splititup;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OCREvalService extends IntentService {

    public OCREvalService() {
        super("OCREvalService");
    }

    private static Map<String,Float> cachedValues = new HashMap<>();

    private ArrayList<Bitmap> generateImages( Bitmap bmp, int x, int y ) {

        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        int width = 200,
            height = 50,
            points = 2,
            delta = 10;


        for( int j = -(points/2); j < (points/2)+1; j++ ){

            bitmaps.add(BitmapCropper.cropBitmap(bmp, x, y + (j * delta), width, height));
        }

        return bitmaps;
    }

    private Float parseFloat( String v ) {
        String[] parts = v.split("\\.");

        if( parts.length > 1 ) {
            v = parts[0].replaceAll("[\\D]", "") + "." + parts[1].replaceAll("[\\D]", "");
            try {
                return Float.parseFloat(v);
            }catch( NumberFormatException x ) {
                Log.e( "@@@", "Could not extract float value from OCR result!" );
            }
        }

        return null;

    }

    private float evaluateImages( ArrayList<Bitmap> images ) {
        Map<Float,Integer> values = new HashMap<>();

        // Create the map of OCR-ed values based on their occurrence
        for( Bitmap b : images ) {
            TessBase.getInstance().setImage(b);
            String s = TessBase.getInstance().getUTF8Text();
            Float f = parseFloat(s);
            if( f != null ) {
                if( values.containsKey(f) ) {
                    values.put(f, values.get(f)+1);
                } else {
                    values.put(f, 1);
                }
            }
        }

        // Find the entry with the largest occurrence
        Map.Entry<Float,Integer> entry = null;
        for( Map.Entry<Float,Integer> i : values.entrySet() ) {
            Log.d( "@@@", i.getKey().toString() + " => " + i.getValue().toString() );
            if( entry == null || i.getValue() > entry.getValue() ) {
                entry = i;
            }
        }

        return (entry == null) ? 0.0f : entry.getKey();
    }

    private String processCood( int c ) {
        return Integer.toString( c / 50 );
    }

    private String generateCachedKey( String fileUrl, int x, int y ) {
        return processCood(x) + "." + processCood(y) + "@" + fileUrl;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        // Only execute the request if it was generated within 0.5s
        if (System.currentTimeMillis() - bundle.getLong("time") < 500L) {

            String fileUrl = bundle.getString("imageUrl");
            int x = bundle.getInt("x"), y = bundle.getInt("y");
            String key = generateCachedKey((fileUrl != null) ? fileUrl : "", x, y);
            Intent result = new Intent(Constants.BROADCAST_ACTION);

            if (cachedValues.containsKey(key)) {
                Log.d("@@@-2", "USING CACHED VALUE!");
                result.putExtra(Constants.DATA_OCR_FLOAT_VALUE, cachedValues.get(key));
            } else {
                Bitmap bmp;

                if (fileUrl != null && fileUrl.length() > 0) {
                    Log.d("@@@-2", "USING FILE URL: " + fileUrl);
                    bmp = BitmapFactory.decodeFile(fileUrl);

                } else {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                }

                float value = evaluateImages(generateImages(bmp, x, y));
                cachedValues.put(key, value);
                result.putExtra(Constants.DATA_OCR_FLOAT_VALUE, value);
                LocalBroadcastManager.getInstance(this).sendBroadcast(result);
            }
        } else {
            Log.d( "@@@-2", "EXIT INTENT HANDLER; TASK TOO OLD: " + Long.toString(System.currentTimeMillis() - bundle.getLong("time") ) + "ms" );
        }
    }
}
