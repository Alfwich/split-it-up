package com.arthurwut.splititup;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessBase {
    private static TessBaseAPI instance;

    public static TessBaseAPI getInstance() {
        if( instance == null ) {
            instance = new TessBaseAPI();
            instance.init(Environment.getExternalStorageDirectory() + "/split-it", "eng");
        }

        return instance;
    }
}
