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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MainActivity extends ActionBarActivity implements ActionMode.Callback{
    public static final String TAG = "MainActivity";
    public static final String VIEW_ALL_KEY = "eecs40.placebook.EXTRA_VIEW_ALL";
    private static final int REQUEST_VIEW_ALL = 1005;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_SPEECH_INPUT = 1002;
    private static final int REQUEST_PLACE_PICKER = 1003;
    private static final int FOCUS_PLACE = 2001;
    private static final int FOCUS_DESC = 2002;
    private final ArrayList<PlacebookEntry> mPlacebookEntries = new ArrayList<>();
    private PlacebookEntry mPlacebookEntry;

    private GoogleApiClient mGoogleApiClient;
    private ListView mListView;
    protected Object mActionMode ;
    public int selectedItem = -1;
    private String mCurrentFilePath;
    private long entryId;
    private int focus = FOCUS_PLACE;

    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    ItemsAdapter adapter;


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
        EditText editPlace = (EditText) findViewById(R.id.editText_place);
        EditText editDesc = (EditText) findViewById(R.id.editText_description);

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

        editPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                focus = FOCUS_PLACE;
            }
        });

        editDesc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                focus = FOCUS_DESC;
            }
        });



        mListView = (ListView)findViewById(R.id.ListView_history);
        adapter = new ItemsAdapter(this, android.R.layout.simple_selectable_list_item, mPlacebookEntries);
        mListView.setAdapter(adapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                if (mActionMode != null)
                    return false;
                selectedItem = position;
                mActionMode = MainActivity.this.startActionMode(MainActivity.this);
                view.setSelected(true);
                return true;
            }
        });

    }

    //Call dispatchViewAllPlaces() when its menu command is selected .
    private void dispatchViewAllPlaces() {
        Intent intent = new Intent(VIEW_ALL_KEY);
        intent.putParcelableArrayListExtra(VIEW_ALL_KEY, mPlacebookEntries);
        try {
            startActivityForResult(intent, REQUEST_VIEW_ALL);
        } catch (ActivityNotFoundException a) {}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_VIEW_ALL && data != null) {
            // Check if any entry was deleted .
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
            //mPlacebookEntry.appendDescription(result.get(0));

            if (focus == FOCUS_PLACE) {
                EditText view = (EditText) findViewById(R.id.editText_place);
                view.setText(result.get(0));
            }
            else if (focus == FOCUS_DESC) {
                EditText view = (EditText) findViewById(R.id.editText_description);
                view.setText(result.get(0));
            }

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
        switch (item.getItemId()) {
            case R.id.action_new_place:
                //Code to add a new place
                mPlacebookEntry = new PlacebookEntry(entryId);

                EditText desc = (EditText)findViewById(R.id.editText_description);
                EditText name = (EditText)findViewById(R.id.editText_place);
                mPlacebookEntry.setName(name.getText().toString());
                mPlacebookEntry.appendDescription(desc.getText().toString());
                mPlacebookEntry.setPhotoPath(mCurrentFilePath);
                mPlacebookEntries.add(mPlacebookEntry);

                adapter.notifyDataSetChanged();

                desc.setText("");
                name.setText("");
                mCurrentFilePath=null;
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
                photoFile = null;
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
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (focus == FOCUS_PLACE) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt_place));
        }
        else if (focus == FOCUS_DESC) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt_desc));
        }
        try {
            startActivityForResult(intent, REQUEST_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            // Handle Exception
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateActionMode ( ActionMode mode , Menu menu ) {
        MenuInflater inflater = mode . getMenuInflater ();
        inflater.inflate(R.menu.rowselection,menu);
        return true ;
    }
    @Override
    public boolean onPrepareActionMode ( ActionMode mode , Menu menu ) {
        return false ;
    }
    @Override
    public boolean onActionItemClicked ( ActionMode mode , MenuItem item ) {
        switch ( item.getItemId()) {
            case R.id.action_delete_place :
                // Delete Item
                mPlacebookEntries.remove(selectedItem);
                adapter.notifyDataSetChanged();
                mode.finish ();
                return true ;
            default:
                return false ;
        }
    }
    @Override
    public void onDestroyActionMode ( ActionMode mode ) {
        mActionMode = null ;
        selectedItem = -1;
    }

    private class ItemsAdapter extends ArrayAdapter<PlacebookEntry> {

        private final ArrayList<PlacebookEntry> items;

        public ItemsAdapter(Context context, int textViewResourceId, ArrayList<PlacebookEntry> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.history_item, null);
            }

            PlacebookEntry it = items.get(position);
            if (it != null) {
                TextView hPlace = (TextView) v.findViewById(R.id.history_list_name);
                ImageView hImage = (ImageView) v.findViewById(R.id.history_list_image);
                TextView hDesc = (TextView) v.findViewById(R.id.history_list_desc);
                if (hPlace != null){
                    hPlace.setText(it.getName());
                }
                if (hImage != null) {
                    hImage.setImageBitmap(it.getImage());
                }
                if (hDesc != null) {
                    hDesc.setText(it.getDescription());
                }
            }

            return v;
        }
    }

}
