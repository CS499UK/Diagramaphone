package edu.uky.cs.www.diagramaphone;

import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.CameraBridgeViewBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    // Something from openCV to prove that it imported correctly
    private CameraBridgeViewBase mOpenCvCameraView;

    // Result Strings for optical character recognition
    private static int RESULT_LOAD_IMG = 1;
    String recognizedText;
    String imgDecodableString;
	
	//TessbaseAPI object used for OCR 
	TessBaseAPI baseApi;
	
	// Bitmap of image being processed
    Bitmap bitmap;

    Pixa words;

    int left,top,width,height;

    // Should get initialized to the main activity's EditText
    protected EditText _field;

    // Our package name and path
    public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/Diagramaphone/";


    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    public static final String lang = "eng";

    // A TAG for log files
    private static final String TAG = "SimpleAndroidOCR.java";

    // The text to speech object used by this application
    TextToSpeech ttobj;


    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 400; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();

    public final static String MY_ORIGIN_STRING = "com.example.app";


    /**
     * onResume()
     * Purpose:
     *  This is a method that classes that extend ActionBarActivity (or any Activity)
     *  must override.  Defines how the activity should resume if it has already been
     *  created once.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Use a new thread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
            }
        });
        thread.start();
    }

    /**
     * genTone()
     * Purpose:
     *   A simple helper function that will create a tone.
     */
    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples/20; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    /**
     * playSound()
     * Purpose:
     *  Plays a short souund.
     */
    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

    /**
     * onPause()
     * Purpose:
     *  The text to speech object has a habit of crashing often.
     *  This is one of the ActionBarActivity methods that this extends class overrides.
     *  Defines how the activity should behave when paused.
     * Preconditions:
     *  The Text to Speech object may be initialized already or not.
     * Post-conditions:
     *  The text to speech object should stop correctly.
     */
    @Override
    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

    /**
     * speakText()
     * Purpose:
     *  Helper function that simply gets the recognized text and speaks it out loud.
     * Preconditions:
     *   The recognizedText String should exist to get anything audible.
     * Post-conditions:
     *   An auditory reading of the recognized text from the OCR.
     */
    public void speakText(){
        String toSpeak = recognizedText;//colorRGB.getTextColors().toString(); //write.getText().toString();

        //Uncomment to show text of TTS

        //Toast.makeText(getApplicationContext(), toSpeak,
        //        Toast.LENGTH_SHORT).show();
        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }


    /**
     * colorCheck()
     * Purpose:
     *   Helper function that determines the equality of two colors.
     * @param oldColor
     * @param newColor
     * @return
     */
    public boolean colorCheck(String oldColor, String newColor){
        return !oldColor.equals(newColor);
    }


    /**
     * initTTS()
     * Purpose:
     *  Initializes a text to speech object.
     * Preconditions:
     *  An empty method variable called ttobj of type TextToSpeech.
     * Post-conditions:
     *  Initializes ttobj to the US English Text to Speech object.
     */
    public void initTTS(){
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.US);
                        }
                    }
                });

    }

    /**
     * onCreate
     * Purpose:
     *   Another override from the Activity class.
     *   This method defines what this Activity should do the first time it is loaded.
     * Preconditions:
     *  @param savedInstanceState there are certain conditions when the application might need
     *                            to remember the details of an Activity that was closed.
     *                            Details at developer.android.com/training/basics/activity-lifecycle/recreating.html
     * Post-conditions:
     *   Copies the lang.traineddata over to the Android device for its local use the first
     *   time that this activity is created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) { //Handle if the directory cannot be created.
                    //NOTE: Remove in final release version.
                    //Helpful in debugging versions.
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {    //If the directory doesn't exist and can be created.
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();

                //Verifies that the tessdata folder exists, and there exists a .traineddata file.
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    /**
     * loadImagefromGallery()
     * Purpose:
     *   Gets an image from the Android device's picture Gallery
     * Preconditions:
     *   @param view
     * Post-conditions:
     *   An image is loaded.
     */
    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    /**
     * onActivityResult()
     * Purpose:
     *  Loads in an image for analysis.
     * Preconditions:
     * @param requestCode
     * @param resultCode
     * @param data
     * Post-conditions:
     *   The image is displayed in the image view.
     *   Or, an error message when something went wrong.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                imgView.setOnTouchListener(imgSourceOnTouchListener);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) { //Handles when the activity can't get the image.
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        //scanForText(imgDecodableString);
		initBitmap();
		initTessBaseAPI();
        initTTS();
        // NOTE: remove before release.  Useful for debugging.
        Log.v(TAG, imgDecodableString);
        //speakText();
    }

/*
    View.OnTouchListener imgSourceOnTouchListener
            = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Log.v(TAG, "test listener");
            speakText();
            return true;
        }
    };
*/

	View.OnTouchListener imgSourceOnTouchListener
        = new View.OnTouchListener() {

		@Override
        public boolean onTouch(View view, MotionEvent event) {


            Log.i("Test audio", "test");
            float eventX = event.getX();
            float eventY = event.getY();
            float[] eventXY = new float[]{eventX, eventY};

            Matrix invertMatrix = new Matrix();
            ((ImageView) view).getImageMatrix().invert(invertMatrix);

            invertMatrix.mapPoints(eventXY);
            int x = Integer.valueOf((int) eventXY[0]);
            int y = Integer.valueOf((int) eventXY[1]);

		/*	touchedXY.setText(
					"touched position: "
					+ String.valueOf(eventX) + " / "
					+ String.valueOf(eventY));
		    invertedXY.setText(
					"touched position: "
					+ String.valueOf(x) + " / "
					+ String.valueOf(y));
		*/
            //Drawable imgDrawable = ((ImageView) view).getDrawable();
            //Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();

			/*			imgSize.setText(
					"drawable size: "
					+ String.valueOf(bitmap.getWidth()) + " / "
					+ String.valueOf(bitmap.getHeight()));
			*/
            //Limit x, y range within bitmap
            if (x < 0) {
                x = 0;
            } else if (x > bitmap.getWidth() - 1) {
                x = bitmap.getWidth() - 1;
            }

            if (y < 0) {
                y = 0;
            } else if (y > bitmap.getHeight() - 1) {
                y = bitmap.getHeight() - 1;
            }

            int touchedRGB = bitmap.getPixel(x, y);

            //Continent Map

            /*if(touchedRGB == Color.WHITE){
                //colorName.setText("white");

                blockText = "Ocean";
               /* colorRGB.setText("touched color: WHITE");
                colorRGB.setTextColor(Color.BLACK);
                *//*
            }*/

            left = x - 32;
            top = y - 32;
            width = 64;
            height = 64;

            baseApi.setRectangle(left, top, width, height);
            scanForText();
            //Log.v(TAG, "Text Read: " + recognizedText);
            Log.v(TAG, "test listener");
            speakText();
            return true;
        }
    };

	protected  void initBitmap() {
        
		Log.v(TAG, "Intitializing Bitmap object");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        bitmap = BitmapFactory.decodeFile(imgDecodableString, options);

		// Code for rotating an improperly aligned image.  Might be useful for later, not
        // right now though since we aren't using the camera anymore.
        /*
        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }*/
		
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

    }

	protected  void initTessBaseAPI() {

        Log.v(TAG, "Intitializing TessBaseAPI object");

		// Create the tesseract API object here
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
		// Initialize with the traineddata
        baseApi.init(DATA_PATH, lang);
		// Set the image scanned by the TessBaseAPI object. In this case it it the bitmap initialized in initBitmap
        baseApi.setImage(bitmap);



    }


    /**
     * scanForText()
     * Preconditions:
     * Member variable bitmap has been initialized.
     * Member variable baseApi has been initialized.
     * Post-conditions:
     *  A String object containing the text in the analyzed region of the image.
     */
    protected void scanForText() {

		// Empty recognizedText
		recognizedText = "";

        words = baseApi.getWords();

        int X = words.getBox(0).getX();
        int Y = words.getBox(0).getY();
        int Width = words.getBox(0).getWidth();
        int Height = words.getBox(0).getHeight();


        String s = String.format("Bounded Word : X: %d,  Y: %d, Width: %d, Height: %d ", X,Y, Width, Height);
        Log.v(TAG, s);

        String s2 = String.format("Original Rect : X: %d,  Y: %d, Width: %d, Height: %d ", left,top, width, height);
        Log.v(TAG, s2);

        // Get the recognized text from the image.
        recognizedText = baseApi.getUTF8Text();
		
		// Originally the entire image was scanned for text at once prior to displaying the image.
		// It has since changed to scan around a location touched. 
		// Since the TessBaseAPI object will scan throughout execution it can no longer be stoped with TessBaseAPI.end() here.
		//baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        // NOTE: remove before release version.  Useful for debugging.
        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        //Finally, trim off text
        recognizedText = recognizedText.trim();

		// Place the recognized text in the _field for viewing purposes
        _field = (EditText) findViewById(R.id.ocrEditText);
        if ( recognizedText.length() != 0 ) {
            _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
            _field.setSelection(_field.getText().toString().length());
            _field.setText(recognizedText);
        }

        // Cycle done.
    }

}

