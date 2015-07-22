package com.arthurwut.splititup;

import android.graphics.Bitmap;

public class BitmapCropper {
    public static Bitmap cropBitmap( Bitmap src, int x, int y, int width, int height ) {
        x -= width/2;
        y -= height/2;

        if( x < 0 ) {
            x = 0;
        }

        if (y < 0) {
            y = 0;
        }

        if( width > src.getWidth() ) {
            width = src.getWidth();
        }

        if( height > src.getHeight() ) {
            height = src.getHeight();
        }

        if( x+width > src.getWidth() ) {
            x = src.getWidth()-width;
        }

        if( y+height > src.getHeight() ) {
            y = src.getHeight()-height;
        }

        return Bitmap.createBitmap( src, x, y, width, height );
    }
}
