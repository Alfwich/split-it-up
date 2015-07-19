package com.arthurwut.splititup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    public MainActivityFragment() {
    }

    public void onClick( View v ) {

        switch( v.getId() ) {
            case R.id.mainTakePicture:
                dispatchTakePictureIntent();
                break;

            case R.id.mainOCRImage:
                if( imageFile != null ) {
                    TessBaseAPI b = new TessBaseAPI();
                    b.init(Environment.getExternalStorageDirectory() + "/split-it", "eng");
                    Bitmap bmp = scaleDown(BitmapFactory.decodeFile(imageFile.toString()), 500.0f, true);
                    b.setImage(bmp);
                    TextView txt = (TextView) getView().findViewById(R.id.orcTextView);
                    txt.setText( b.getUTF8Text() );
                    Log.d("@@@", b.getUTF8Text());
                }
                break;

        }

    }

    File imageFile;
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

    static final int REQUEST_TAKE_PHOTO = 1;

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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if( imageFile != null ) {
                Bitmap bmp = scaleDown(BitmapFactory.decodeFile(imageFile.toString()), 500.0f, true);
                ImageView iView = (ImageView) getView().findViewById(R.id.mainImageView);
                iView.setImageBitmap(bmp);
                Log.d("@@@", "Returned to activity from picture-intent");
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button btn = (Button) view.findViewById( R.id.mainTakePicture );
        btn.setOnClickListener( this );

        btn = (Button) view.findViewById( R.id.mainOCRImage );
        btn.setOnClickListener( this );

        return view;
    }
}
