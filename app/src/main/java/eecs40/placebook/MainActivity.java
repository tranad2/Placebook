package eecs40.placebook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    public static final String VIEW_ALL_KEY = "eecs40.placebook.EXTRA_VIEW_ALL";
    private static final int REQUEST_VIEW_ALL = 1005;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_SPEECH_INPUT = 1002;
    private static final int REQUEST_PLACE_PICKER = 1003;
    private ArrayList<PlacebookEntry> mPlacebookEntries = new ArrayList<>();
    private PlacebookEntry mPlacebookEntry;

    private GoogleApiClient mGoogleApiClient;
    private ListView mListView;
    private String mCurrentFilePath;
    private long entryId;

    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        entryId = 0;
        mPlacebookEntry = new PlacebookEntry(entryId);

        // Populate the history list view
        initGoogleApi();
        setContentView(R.layout.activity_main);

        ImageButton btnLocation = (ImageButton) findViewById(R.id.imageButton_location);
        ImageButton btnCamera = (ImageButton) findViewById(R.id.imageButton_camera);
        ImageButton btnMicrophone = (ImageButton) findViewById(R.id.imageButton_microphone);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchPlacePicker();
            }
        });

        btnMicrophone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchSpeechInputIntent();
            }
        });

        mListView = (ListView)findViewById(R.id.ListView_history);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, listItems);
        mListView.setAdapter(adapter);

    }

    //Call dispatchViewAllPlaces() when its menu command is selected .
    private void dispatchViewAllPlaces() {
        //TODO
        Intent intent = new Intent(VIEW_ALL_KEY);
        intent.putParcelableArrayListExtra(VIEW_ALL_KEY, mPlacebookEntries);
        try {
            startActivityForResult(intent, REQUEST_VIEW_ALL);
        } catch (ActivityNotFoundException a) {}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO
        if (resultCode == RESULT_OK && requestCode == REQUEST_VIEW_ALL && data != null) {
            // Check if any entry was deleted .
            // Check on change
            Log.v(TAG, mPlacebookEntries.toString());
            Toast.makeText(this, "View_ALL", Toast.LENGTH_SHORT).show();
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            // Save previously generated unique file path in current Placebook entry
            mPlacebookEntry.setPhotoPath(mCurrentFilePath);
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_SPEECH_INPUT && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // Append result.get(0) to the place name or description text view
            // according to which one had focus when voice recognizer was launched
            mPlacebookEntry.appendDescription(result.get(0));

        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_PLACE_PICKER && data != null) {
            Place place = PlacePicker.getPlace(data, this);
            //Set place name text view to place.getName()
            EditText view = (EditText) findViewById(R.id.editText_place);
            view.setText(place.getName());

        }

        if(resultCode == RESULT_OK && requestCode == REQUEST_RESOLVE_ERROR && data != null){
            mResolvingError = false;
            if(!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()){
                mGoogleApiClient.connect();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                             M E N U     B U T T O N S                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO
        Log.v(TAG,"Click");
        Log.v(TAG,"ID"+entryId);
        switch (item.getItemId()) {
            case R.id.action_new_place:
                Log.v(TAG,"Click New"+mPlacebookEntries);

                //Code to add a new place
                mPlacebookEntry = new PlacebookEntry(entryId);
                if(mPlacebookEntries == null){
                    Log.v(TAG,"NULL");
                }
                EditText desc = (EditText)findViewById(R.id.editText_description);
                EditText name = (EditText)findViewById(R.id.editText_place);
                mPlacebookEntry.setName(name.getText().toString());
                mPlacebookEntry.appendDescription(desc.getText().toString());
                mPlacebookEntry.setPhotoPath(mCurrentFilePath);
                mPlacebookEntries.add(mPlacebookEntry);
                entryId++;

                desc.setText("");
                name.setText("");
                mCurrentFilePath="";
                Log.v(TAG,"ENTRY: "+mPlacebookEntries);
                return true;
            case R.id.action_view_all:
                //Code to show all places
                //Inflate(?) ListView of places
                dispatchViewAllPlaces();
                return true;
            case R.id.action_edit_place:
                //Code to edit place
                return true;
            case R.id.action_delete_place:
                //Code to delete place
                return true;
            case R.id.action_settings:
                //Code to show settings
                return true;
            default:
                Log.v(TAG,"Click Default");
                return super.onOptionsItemSelected(item);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                   C A M E R A                                              //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Call dispatchTakePictureIntent() when the camera button is clicked .
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there ’s a camera activity to handle the intent
        File photoFile = null;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            CharSequence text = "No camera app available";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String fname = "Placebook_";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + fname;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentFilePath = image.getAbsolutePath();
        //mPlacebookEntry.setPhotoPath(image.getAbsolutePath());

        return image;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                           V O I C E     R E C O G N I T I O N                              //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Call dispatchSpeechInputIntent() when the speech-to-text button is clicked.
    void dispatchSpeechInputIntent() {
        //TODO
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQUEST_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            // Handle Exception
            a.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                           P L A C E     P I C K E R                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Call launchPlacePicker() when the Pick-A-Place button is clicked
    private void launchPlacePicker () {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Context context = getApplicationContext();
        try {
            startActivityForResult(builder.build(context), REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            //Handle exception - Display a Toast message
            e.printStackTrace();
            CharSequence text = "Google Play Services is not installed";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            return;
        } catch (GooglePlayServicesNotAvailableException e) {
            //Handle exception - Display a Toast message
            e.printStackTrace();
            CharSequence text = "Google Play Services is not available";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Call initGoogleApi() from MainActivity.onCreate()
    private void initGoogleApi() {
        //TODO
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(new ConnectionCallbacks() {
                    public void onConnected(Bundle connectionHint) {
                        //Get place
                    }

                    public void onConnectionSuspended(int cause) {
                        //disable UI components until onConnected() called
                    }

                })
                .addOnConnectionFailedListener(new OnConnectionFailedListener() {
                    public void onConnectionFailed(ConnectionResult result) {
                        if (mResolvingError) {
                            return;
                        } else if (result.hasResolution()) {
                            try {
                                mResolvingError = true;
                                result.startResolutionForResult(MainActivity.this, REQUEST_RESOLVE_ERROR);
                            } catch (IntentSender.SendIntentException e) {
                                mGoogleApiClient.connect();
                            }
                        } else{
                            mResolvingError = true;
                            CharSequence text = "ErrorDialog";
                            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .build();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        Intent intent = new Intent ();
        intent.putParcelableArrayListExtra(MainActivity.VIEW_ALL_KEY, mPlacebookEntries);
        setResult(Activity.RESULT_OK, intent);
    }

    public void addItems(View v){
        //listItems.add();
        adapter.notifyDataSetChanged();
    }
}
