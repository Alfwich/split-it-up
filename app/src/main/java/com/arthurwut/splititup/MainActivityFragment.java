package com.arthurwut.splititup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends android.app.Fragment {

    public MainActivityFragment() {
    }

    private TotalSummer sum;

    TessBaseAPI tessBase;
    private Bitmap nBmp;
    private File imageFile;
    private Bitmap bmp;
    private View view;

    private ImageView exampleImageView;
    private ImageView mainImageView;
    private AbsoluteLayout absLayoutView;
    private TextView ocrTextView;
    private TextView totalText;

    private class PreviewImageTouchListener implements View.OnTouchListener {

        public float dX = 0.0f, dY = 0.0f;

        private Bitmap cropBitmap( Bitmap src, int x, int y, int width, int height ) {
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

        public void reloadExampleBitmap( MotionEvent event ) {
            if(  bmp != null ) {
                float factor = (float) bmp.getHeight() / mainImageView.getHeight();
                nBmp = cropBitmap(
                        bmp,
                        Math.round(( -absLayoutView.getX() + event.getRawX()) * factor),
                        Math.round(( -absLayoutView.getY() + event.getRawY()) * factor),
                        200,
                        50
                );

                exampleImageView.setImageBitmap(nBmp);
                tessBase.setImage(nBmp);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch( event.getActionMasked() ) {

                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    reloadExampleBitmap( event );
                    break;

                case MotionEvent.ACTION_MOVE:
                    v.animate()
                            .x( event.getRawX() + dX )
                            .y( event.getRawY() + dY )
                            .setDuration( 0L )
                            .start();
                    break;

            }
            return true;
        }
    }

    private class TotalSummer {
        private float total = 0.0f;

        public void addValue( float v ) {
            total += v;
            totalText.setText(String.format("%.2f", total));
        }

        public void addValue( String v ) {
            String[] parts = v.split("\\.");

            if( parts.length > 1 ) {
                v = parts[0] + "." + parts[1];
                try {
                    addValue(Float.parseFloat(v));
                }catch( NumberFormatException x ) {
                    Log.e( "@@@", "Could not extract float value from OCR result!" );
                }
            }

        }

    }

    private class ClickEvents implements View.OnClickListener {
        public void onClick( View v ) {

            switch( v.getId() ) {
                case R.id.mainTakePicture:
                    dispatchTakePictureIntent();
                    break;

                case R.id.mainExampleImageView:
                    String val = tessBase.getUTF8Text();
                    ocrTextView.setText(val);
                    sum.addValue(val);
                    break;
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return imageFile;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if( imageFile != null ) {
                //Bitmap bmp = scaleDown(BitmapFactory.decodeFile(imageFile.toString()), 500.0f, true);
                bmp = BitmapFactory.decodeFile(imageFile.toString());
                AbsoluteLayout iLay = (AbsoluteLayout) getView().findViewById( R.id.mainImageViewContainer);
                iLay.setX(0.0f);
                iLay.setY(0.0f);
                mainImageView.setImageBitmap(bmp);
                mainImageView.getLayoutParams().height = iLay.getLayoutParams().height = bmp.getHeight()*2;
                mainImageView.getLayoutParams().width = iLay.getLayoutParams().width = bmp.getWidth()*2;
                Log.d("@@@", "Returned to activity from picture-intent");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Only create the view once
        if( view == null ) {
            setRetainInstance(true);
            Log.d("@@@", "INIT VIEW");
            view = inflater.inflate(R.layout.fragment_main, container, false);

            absLayoutView = (AbsoluteLayout) view.findViewById( R.id.mainImageViewContainer );
            absLayoutView.setOnTouchListener(new PreviewImageTouchListener());

            exampleImageView = (ImageView) view.findViewById( R.id.mainExampleImageView );
            exampleImageView.setOnClickListener(new ClickEvents());

            mainImageView = (ImageView) view.findViewById(R.id.mainImageView);
            ocrTextView = (TextView) view.findViewById(R.id.mainOCRTextView);
            totalText = (TextView) view.findViewById(R.id.mainSumTotal);

            Button btn = (Button) view.findViewById( R.id.mainTakePicture );
            btn.setOnClickListener(new ClickEvents());

            bmp = BitmapFactory.decodeResource( getResources(), R.drawable.test );

            tessBase = new TessBaseAPI();
            tessBase.init(Environment.getExternalStorageDirectory() + "/split-it", "eng");

            sum = new TotalSummer();
        }

        return view;
    }
}

