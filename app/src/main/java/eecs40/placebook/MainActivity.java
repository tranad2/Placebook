package eecs40.placebook;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    public static final String VIEW_ALL_KEY = "eecs40.placebook.EXTRA_VIEW_ALL";
    private static final int REQUEST_VIEW_ALL = 1005;
    private static final int REQUEST_SPEECH_INPUT = 1002;
    private static final int REQUEST_PLACE_PICKER = 1003;
    private ArrayList<PlacebookEntry> mPlacebookEntries;

    // Call dispatchViewAllPlaces() when its menu command is selected .
    private void dispatchViewAllPlaces() {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putParcelableArrayListExtra(VIEW_ALL_KEY, mPlacebookEntries);
        try {
            startActivityForResult(intent, REQUEST_VIEW_ALL);
        } catch (ActivityNotFoundException a) {}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_VIEW_ALL && data != null) {
            ArrayList<PlacebookEntry> placebookEntrys = data.getParcelableArrayListExtra(VIEW_ALL_KEY); // Check if any entry was deleted .
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            // Save previously generated unique file path in current Placebook entry
        }

        if ( resultCode == RESULT_OK && requestCode == REQUEST_SPEECH_INPUT && data != null ) {
            ArrayList<String>result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // Append result . get (0) to the place name or description text view
            // according to which one had focus when voice recognizer was launched
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_PLACE_PICKER && data != null) {
            Place place = PlacePicker.getPlace(data, this);
            //Set place name text view to place.getName()
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                /* * Code to show settings * */;
                return true ;
            case R.id.action_new_place:
                /* * Code to add a new place * */;
                return true ;
            case R.id.action_view_all:
                /* * Code to show all places * */;
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*CAMERA*/
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    //...
    // Call dispatchTakePictureIntent() when the camera button is clicked .
    private void dispatchTakePictureIntent () {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there ’s a camera activity to handle the intent
        if ( takePictureIntent . resolveActivity ( getPackageManager () ) != null ) {
            File photoFile = /* ** Create a unique file path ** */;
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile)) ;
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(/* ** Tell user there is no camera app installed . ** */).show();
        }
    }

    /*SPEECH INPUT*/
    //...
    // Call dispatchSpeechInputIntent() when the speech-to-text button is clicked.
    void dispatchSpeechInputIntent () {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQUEST_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            // Handle Exception
        }
    }


    // Call initGoogleApi() from MainActivity.onCreate()
    private void initGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).addConnectionCallbacks(new GoogleConnectionCallbacks()).addOnConnectionFailedListener(new GoogleApiOnConnectionFailedListener()).build();
    }
    // Call launchPlacePicker() when the Pick-A-Place button is clicked
    private void launchPlacePicker () {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Context context = getApplicationContext();
        try {
            startActivityForResult(builder.build(context), REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            //Handle exception - Display a Toast message
        } catch (GooglePlayServicesNotAvailableException e) {
            //Handle exception - Display a Toast message
        }
    }

    /*BUTTON CLICKS*/
    ImageButton btnSpeak = (ImageButton) findViewById(R.id.button_speak);
    btnSpeak.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Code to be executed when the button is clicked .
        }
    });

}
