package com.arthurwut.splititup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MainActivityFragment extends android.app.Fragment  {

    private File imageFile;
    private Bitmap bmp;
    private View view;
    private MainOCRServiceReceiver reciever;
    private Intent ocrIntent;

    private ImageView exampleImageView;
    private ImageView mainImageView;
    private AbsoluteLayout absLayoutView;
    private TextView ocrTextView;
    private TotalView totalView;

    public MainActivityFragment() {
    }

    public class MainOCRServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            float value = bundle.getFloat( Constants.DATA_OCR_FLOAT_VALUE );
            Log.d("@@@", "RESULT FROM SERVICE INTENT: " + Float.toString(value));

            ocrTextView.setText(Float.toString(value));
            totalView.setValue(value);
        }
    }

    private class PreviewImageTouchListener implements View.OnTouchListener {

        public float dX = 0.0f, dY = 0.0f;

        public void reloadExampleBitmap( MotionEvent event ) {
            if(  bmp != null ) {
                float factor = (float) bmp.getHeight() / mainImageView.getHeight();

                int x = Math.round((-absLayoutView.getX() + event.getRawX()) * factor),
                    y = Math.round((-absLayoutView.getY() + event.getRawY()) * factor),
                    width = 200,
                    height = 50;

                ocrIntent.putExtra( "x", x );
                ocrIntent.putExtra( "y", y );
                ocrIntent.putExtra("time", System.currentTimeMillis());
                getActivity().startService(ocrIntent);

                exampleImageView.setImageBitmap(BitmapCropper.cropBitmap(bmp, x, y, width, height));
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch( event.getActionMasked() ) {

                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    reloadExampleBitmap(event);
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

    private class ClickEvents implements View.OnClickListener {
        public void onClick( View v ) {

            switch( v.getId() ) {
                case R.id.mainTakePicture:
                    dispatchTakePictureIntent();
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
                bmp = BitmapFactory.decodeFile(imageFile.toString());
                ocrIntent.putExtra("imageUrl", ( imageFile != null ) ? imageFile.toString() : "" );
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
            totalView = (TotalView) view.findViewById(R.id.mainTotalView);

            Button btn = (Button) view.findViewById( R.id.mainTakePicture );
            btn.setOnClickListener(new ClickEvents());

            bmp = BitmapFactory.decodeResource( getResources(), R.drawable.test );

            reciever = new MainOCRServiceReceiver();
            ocrIntent = new Intent( getActivity(), OCREvalService.class );
            ocrIntent.putExtra("imageUrl", "" );

            IntentFilter filter = new IntentFilter( Constants.BROADCAST_ACTION );
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reciever, filter);
        }

        return view;
    }
}

