package com.loyola.talktracer.activities;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loyola.blabbertabber.R;
import com.loyola.talktracer.model.AudioRecord.AudioEventProcessor;
import com.loyola.talktracer.model.AudioRecord.RecordingService;
import com.loyola.talktracer.model.CheatSheet;
import com.loyola.talktracer.model.Helper;
import com.loyola.talktracer.model.Timer;
import com.loyola.talktracer.model.WavFile;
import com.venmo.view.TooltipView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;
import it.sephiroth.android.library.tooltip.Tooltip;


/**
 * Activity to record sound.
 */
public class RecordingActivity extends Activity implements View.OnClickListener {
    private boolean parseData=false;
    private boolean testingMode = true;
    private String testingFileName="redd.wav";
    private boolean tutorialMode = false;
    private int tutorialNumber;
    private DrawerLayout mDrawerLayout;
    private Button menu;
    private CheatSheet cheatsheet;
    public static final String SPHINX_CONFIG = "sphinx4_config.xml";
    private static final String TAG = "RecordingActivity";
    private static final String PREF_RECORDING = "com.blabbertabber.blabbertabber.pref_recording";
    private static final int REQUEST_RECORD_AUDIO = 51;
    private boolean mBound = false;
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            RecordingService.RecordingBinder recordingBinder = (RecordingService.RecordingBinder) binder;
            mBound = true;
            Log.v(TAG, "mServerConn.onServiceConnected()");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.v(TAG, "mServerConn.onServiceDisconnected()");
        }
    };

    private BroadcastReceiver mReceiver;
    private Timer mTimer = new Timer();
    private Thread mTimerDisplayThread;


    /**
     * Construct a new BroadcastReceiver that listens for Intent RECORD_RESULT and
     * Intent RECORD_STATUS.
     * Extracts the volumes and speaker id from the RECORD_RESULT messages.
     * Gracefully handles any RECORD_STATUS message as a failure.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        boolean first_Record = sharedPref.getBoolean("first_record", true);
        Log.d("aaa", Boolean.toString(first_Record));

        //this is the state of the tutorial when someone enters tutorial the number changes to tell certain methods like click record,
        // or summary button to prevent user clicking on other stuff that breaks the order of the tutorial
        tutorialNumber = 1;

        //handles the tutorial popup
        tutorialManager(first_Record, editor);

        //the rest of this stuff is setting up interface and little stuff
        super.onCreate(savedInstanceState);

        //when entering the app we set the timer to 0
        mTimer.reset();

        //recording layout setup
        setContentView(R.layout.activity_recording);
        resetNoView();

        //right hand corner menu
        menu = (Button) findViewById(R.id.menu);
        menu.setOnClickListener(RecordingActivity.this);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Log.v(TAG, "onReceive():  received Intent: " + intent);
                if (intent.getAction().equals(AudioEventProcessor.RECORD_RESULT)) {
                    displayTimer(mTimer);
                } else if (Objects.equals(intent.getAction(), AudioEventProcessor.RECORD_STATUS)) {
                    // If we start sending statuses other than MICROPHONE_UNAVAILABLE, add logic to check status message returned.
                    int status = intent.getIntExtra(AudioEventProcessor.RECORD_STATUS_MESSAGE, AudioEventProcessor.UNKNOWN_STATUS);
                    String statusMsg = "onReceive():  The microphone has a status of " + status;
                    Log.wtf(TAG, statusMsg);
                    String toastMessage;
                    switch (status) {
                        case AudioEventProcessor.MICROPHONE_UNAVAILABLE:
                            toastMessage = "Problem accessing microphone. Terminate app using microphone (e.g. Phone, Hangouts) and try again.";
                            break;
                        case AudioEventProcessor.CANT_WRITE_MEETING_FILE:
                            toastMessage = "Error recording the meeting to disk; make sure you've closed all BlabberTabber instances and try again.";
                            break;
                        case AudioEventProcessor.UNKNOWN_STATUS:
                        default:
                            toastMessage = "I have no idea what went wrong; restart BlabberTabber and see if that fixes it";
                    }
                    Toast.makeText(context, toastMessage,
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "onReceive() received an Intent with unknown action " + intent.getAction();
                    Log.wtf(TAG, errorMsg);
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        };

    }

    /**
     * this is the tutorial manager which on first opening of the app displays a popup for users to do the tutorial if they want.
     * If they do it calls startTutorial() which starts tutorial
     *
     * @param first_Record
     * @param editor
     */
    public void tutorialManager(Boolean first_Record, SharedPreferences.Editor editor) {
        if (first_Record == true) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            startTutorial();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };
            editor.putBoolean("first_record", false);
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hey its your first tiime. Do you want to view the tutorial? If not you can go to the menu to view it later").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

    }

    /**
     * Creates the tutorial interface the popups and the tooltips
     */
    public void startTutorial() {
        //FloatingActionButton floatingActionButton= (FloatingActionButton) findViewById(R.id.closeTutorial);
        ImageView play = (ImageView) findViewById(R.id.button_record);
        Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(play, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(false, true), 0)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("Click to record")
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        ).show();

        tutorialMode = true;
        AlertDialog.Builder tutorialMessage = new AlertDialog.Builder(this);
        tutorialMode = true;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        // Let's make sure we have android.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            registerRecordingServiceReceiver();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }


        //displaying the timer
        double meetingInSeconds = Helper.howLongWasMeetingInSeconds(new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME).length());
        mTimer = new Timer((long) (meetingInSeconds * 1000));
        mTimer.reset();
        displayTimer(mTimer);


        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(PREF_RECORDING, RecordingService.recording)) {
            // we're recording, not paused; maybe the screen was rotated
            clickRecord(null);
        } else {
            clickPause(null);
        }


        // mTimerDisplayThread sends Intents to update the Timer TextView
        mTimerDisplayThread = new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "run() starting " + Thread.currentThread().getId());
                try {
                    while (true) {
                        sleep(100);
                        Intent refreshTimerIntent = new Intent(AudioEventProcessor.RECORD_RESULT);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(refreshTimerIntent);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.i(TAG, "run() exiting " + Thread.currentThread().getId());
                }
            }
        };
        mTimerDisplayThread.start();
    }


    /**
     * note this is not for pause button click this is on pause for android studios
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        mTimerDisplayThread.interrupt();
        // unregister
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mServerConn != null && mBound) {
            unbindService(mServerConn);
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_RECORDING, RecordingService.recording);
        editor.apply();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // yes, call super first, even with onDestroy()
        Log.i(TAG, "onDestroy()");
    }

    private void registerRecordingServiceReceiver() {
        Intent serviceIntent = new Intent(this, RecordingService.class);
        // Start service first, then bind to it. Sounds redundant, but we have our reasons
        startService(serviceIntent);
        if (bindService(serviceIntent, mServerConn, BIND_AUTO_CREATE)) {
            Log.i(TAG, "bindService() succeeded, mBound: " + mBound);
        } else {
            Log.wtf(TAG, "bindService() failed, mBound: " + mBound);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(AudioEventProcessor.RECORD_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(AudioEventProcessor.RECORD_STATUS)
        );
    }

    public void clickRecord(View v) {
        // first part is for handling the click record button on the Tutorial
        Boolean tutorialStatus = clickRecordTutorial();
        if (tutorialStatus) {
            return;
        }
        // tutorial stuff end here


        // click record now start to record this is the stuff that occurs upon record
        record();
        findViewById(R.id.button_reset).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_finish).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_record).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_record_caption).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pause_caption).setVisibility(View.VISIBLE);
    }

    public Boolean clickRecordTutorial() {
        //
        if (tutorialMode == true && tutorialNumber != 1 && tutorialNumber != 6 && tutorialNumber != 4) {
            Toast.makeText(this, "FOLLOW INSTRUCTIONS GRRRR >:(",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        if (tutorialMode == true) {
            if (tutorialNumber == 1) {
                ImageView play = (ImageView) findViewById(R.id.button_record);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(play, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(true, false), 0)
                                .text("Talk for a little and when you are ready click pause button")
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;
                Log.d("Hello", Integer.toString(tutorialNumber));

            }
            if (tutorialNumber == 4) {
                ImageView play = (ImageView) findViewById(R.id.button_record);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(play, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(false, true), 0)
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .text("Talk for a little and when you are ready click pause button")
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;
            }
            if (tutorialNumber == 6) {
                ImageView play = (ImageView) findViewById(R.id.button_record);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(play, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(false, true), 0)
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .text("Talk for a little and when you are ready click pause button")
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;
            }


        }
        return false;
    }

    public void clickPause(View v) {
        if (pauseTutorial()) {
            return;
        }
        // was recording; need to pause
        pause();
        findViewById(R.id.button_reset).setVisibility(View.VISIBLE);
        findViewById(R.id.button_finish).setVisibility(View.VISIBLE);
        findViewById(R.id.button_record).setVisibility(View.VISIBLE);
        findViewById(R.id.button_record_caption).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_pause_caption).setVisibility(View.INVISIBLE);
    }
    public boolean pauseTutorial()
    {
        if (tutorialMode == true && tutorialNumber != 2 && tutorialNumber != 5 && tutorialNumber != 7) {
            Toast.makeText(this, "FOLLOW INSTRUCTIONS GRRRR >:(",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        if (tutorialMode == true) {

            if (tutorialNumber == 2) {
                Button buttonReset = (Button) findViewById(R.id.button_reset);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(buttonReset, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(false, true), 0)
                                .text("EWW I didnt like that recording :( click to clear it ")
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;

            }
            if (tutorialNumber == 5) {
                ImageView play = (ImageView) findViewById(R.id.button_record);
                Button buttonReset = (Button) findViewById(R.id.button_reset);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(play, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(false, true), 0)
                                .text("Did you know even after you pause you can record more? Click again and record a little more!")
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;

            }

            if (tutorialNumber == 7) {
                Button menuButton = (Button) findViewById(R.id.menu);
                Tooltip.make(this,
                        new Tooltip.Builder(101)
                                .anchor(menuButton, Tooltip.Gravity.TOP)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(false, true), 0)
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .text("Im gonna side track for a second. Click menu to find more fun :)")
                                .maxWidth(600)
                                .withArrow(true)
                                .withOverlay(true).build()
                ).show();
                tutorialNumber += 1;
            }

        }
        return false;
    }

    private void pause() {
        Log.i(TAG, "pause()");
        mTimer.stop();
        RecordingService.recording = false;
    }

    private void record() {
        Log.i(TAG, "record()");
        // Make sure we have all the necessary permissions, then begin recording:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "record() bailing out early, don't have permissions", Toast.LENGTH_LONG).show();
            Log.i(TAG, "record() bailing out early, don't have permissions");
            return;
        }
        mTimer.start();
        RecordingService.recording = true;
    }




    /**
     * used when the reset button is clicked
     * @param v
     */
    public void reset(View v) {
        if (resetTutorial())
        {
            return;
        }
        Log.i(TAG, "reset()");
        mTimer.reset();
        displayTimer(mTimer);
        RecordingService.reset = true;
        pause();
    }


/**
 *  used for on create before any view is initialized so we do not have to pass in a view parameter
 */
    public void resetNoView() {
       if (resetTutorial())
       {
           return;
       }
        Log.i(TAG, "reset()");
        mTimer.reset();
        displayTimer(mTimer);
        RecordingService.reset = true;
        pause();
    }

    /**
     * handles reset button if tutorial is activated. ensures reset only works if its time for it to work in tutorial
     * @return
     */
    public boolean resetTutorial()
    {
        if (tutorialMode == true && tutorialNumber != 3) {
            Toast.makeText(this, "FOLLOW INSTRUCTIONS GRRRR >:(",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        if (tutorialMode == true) {
            ImageView play = (ImageView) findViewById(R.id.button_record);
            Button buttonReset = (Button) findViewById(R.id.button_reset);
            Tooltip.make(this,
                    new Tooltip.Builder(101)
                            .anchor(play, Tooltip.Gravity.TOP)
                            .closePolicy(new Tooltip.ClosePolicy()
                                    .insidePolicy(true, false)
                                    .outsidePolicy(false, true), 0)
                            .text("Lets record for real this time!")
                            .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                            .maxWidth(600)
                            .withArrow(true)
                            .withOverlay(true).build()
            ).show();
            tutorialNumber += 1;

        }
        return false;
    }

    /**
     * when the finished button is clicked
     * @param v
     */
    public void finish(View v) {
        //checks if the tutorial is running and it is time to press finish button
        if (finishTutorial())
        {
            return;
        }
        mTimer.stop();
        pause(); // stop the recording
        diarizationProgress();
        final Context context = this;
        new Thread() {
            @Override
            public void run() {
                diarize();
                Intent intent = new Intent(context, SummaryActivity.class);
                intent.putExtra("TUTORIAL", tutorialMode);
                intent.putExtra("TESTING", testingMode);
                intent.putExtra("TESTINGFILE",testingFileName);
                intent.putExtra("PARSEDATA", parseData);
                startActivity(intent);
            }
        }.start();
    }

    public boolean finishTutorial()
    {
        if (tutorialMode == true && tutorialNumber != 9) {
            Toast.makeText(this, "FOLLOW INSTRUCTIONS GRRRR >:(",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        else{
            return false;
        }
    }

    private void copySphinxConfigFileIntoPlace() {
        String outputFilePathname = getFilesDir() + "/" + SPHINX_CONFIG;
        try {
            InputStream inputStream = getAssets().open(SPHINX_CONFIG, AssetManager.ACCESS_BUFFER);
            Helper.copyInputFileStreamToFilesystem(inputStream, outputFilePathname);
        } catch (IOException e) {
            Log.wtf(TAG, "copySphinxConfigFileIntoPlace() couldn't copy file");
            Toast.makeText(this, "Configuration didn't succeed, expect no results", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // log everything
        String argumentString = " " + requestCode + " [ ";
        for (String perm : permissions) {
            argumentString += perm + ", ";
        }
        argumentString += " ], [ ";
        for (int grantResult : grantResults) {
            argumentString += grantResult + ", ";
        }
        argumentString += " ]";
        Log.i(TAG, "onRequestPermissionsResult() " + argumentString);

        // boilerplate from http://developer.android.com/training/permissions/requesting.html
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission denied, message & exit gracefully
                    Toast.makeText(getApplicationContext(), "BlabberTabber exited because it's unable to access the microphone", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
        Log.wtf(TAG, "Oops, an unasked-for permission was granted/denied.");
    }

    /**
     * displays the time on the time
     *
     * @param t
     */
    private void displayTimer(Timer t) {
        ((TextView) findViewById(R.id.meeting_timer))
                .setText(Helper.timeToHMMSSMinuteMandatory(t.time()));
    }


    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                String outDir = getFilesDir().toString() ;

                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("failure", "Failed to copy asset file: " + filename, e);
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void diarize() {
        // Transform the raw file into a .wav file

        copyAssets();

        // opening the mixture model files however for some reason this does not work
        File genderModel= cacheDirFileOpener("gender.gmms");
        File sModel = cacheDirFileOpener("s.gmms");
        File smsModel = cacheDirFileOpener("sms.gmms");
        File ubmModel = cacheDirFileOpener("ubm.gmm");

        //This is the audio file name if we are not in data testing mode the file we use the AudioEventProcessor.RECORDER_RAW_FILENAME
        //If we are in testing mode we use the name of the file we want for testing
        String inputAudioFile;
        if (testingMode)
        {
            inputAudioFile= getFilesDir()+"/"+testingFileName;
        }
        else {
            inputAudioFile = getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME;
        }

        WavFile wavFile;
        try {
            Log.i(TAG, "summary()   AudioRecordWrapper.getRawFilePathName(): " + AudioEventProcessor.getRawFilePathName());
            wavFile = WavFile.of(this, new File(AudioEventProcessor.getRawFilePathName()));
        } catch (IOException e) {
            String errorTxt = "Whoops! couldn't convert " + AudioEventProcessor.getRawFilePathName()
                    + ": " + e.getMessage();
            Log.wtf(TAG, errorTxt);
            Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        // Copy Sphinx's config file into place
        copySphinxConfigFileIntoPlace();

        // Create config manager
        ConfigurationManager cm = new ConfigurationManager(getFilesDir() + "/" + SPHINX_CONFIG);

        FrontEnd frontEnd = (FrontEnd) cm.lookup("mfcFrontEnd");
        StreamDataSource audioSource = (StreamDataSource) cm.lookup("streamDataSource");



        try {
            audioSource.setInputStream(new FileInputStream(inputAudioFile), "audio");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LinkedList<float[]> allFeatures = new LinkedList<float[]>();
        int featureLength = -1;

        //get features from audio
        try {
            Data feature = frontEnd.getData();
            while (!(feature instanceof DataEndSignal)) {
                if (feature instanceof DoubleData) {
                    double[] featureData = ((DoubleData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        //logger.info("Feature length: " + featureLength);
                    }
                    float[] convertedData = new float[featureData.length];
                    for (int i = 0; i < featureData.length; i++) {
                        convertedData[i] = (float) featureData[i];
                    }
                    allFeatures.add(convertedData);
                } else if (feature instanceof FloatData) {
                    float[] featureData = ((FloatData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        //logger.info("Feature length: " + featureLength);
                    }
                    allFeatures.add(featureData);
                }
                feature = frontEnd.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //write the MFCC features to binary file
        DataOutputStream outStream = null;
        try {


                outStream = new DataOutputStream(new FileOutputStream(getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc"));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            outStream.writeInt(allFeatures.size() * featureLength);
        } catch (IOException | NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (float[] feature : allFeatures) {
            for (float val : feature) {
                try {
                    outStream.writeFloat(val);
                } catch (IOException | NullPointerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        try {
            outStream.close();
        } catch (IOException | NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //write initial segmentation file for LIUM_SpkDiarization
        String uemSegment = String.format(Locale.getDefault(), "BlabTab 1 0 %d U U U S0", allFeatures.size());
        try {
            FileWriter uemWriter = new FileWriter(getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".uem.seg");
            uemWriter.write(uemSegment);
            uemWriter.flush();
            uemWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String basePathName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION;
        try{
            MSegInit.main(new String[] {
                    "--trace",
                    "--help",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--sInputMask=" + basePathName + ".uem.seg",
                    "--sOutputMask=" + basePathName + ".i.seg",
                    AudioEventProcessor.RECORDER_RAW_FILENAME
            });
        }
        catch(Exception e){
            Toast.makeText(this, "Iseg exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

       /* try{
            MDecode.main(new String[] {
                "--trace"
                ,"--help",
                "--fInputMask=" + basePathName + ".mfc",
                "--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,0:0:0",
                "--sInputMask=" + basePathName + ".i.seg",
                "--sOutputMask=" + basePathName + ".pms.seg",
                AudioEventProcessor.RECORDER_RAW_FILENAME});
        }
        catch(Exception e){
            Log.d("hey","shit");
            Toast.makeText(this, "pmsseg exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/
        try {
            MSeg.main(new String[]{
                    "--trace",
                    "--help",
                    "--kind=FULL",
                    "--sMethod=GLR",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--sInputMask=" + basePathName + ".i.seg",
                    "--sOutputMask=" + basePathName + ".s.seg",
                    AudioEventProcessor.RECORDER_RAW_FILENAME
            });
        } catch (DiarizationException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "DiarizationException: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "Exception " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try {
            MClust.main(new String[] {
                    "--trace",
                    "--help",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--sInputMask=" + basePathName + ".s.seg",
                    "--sOutputMask=" + basePathName + ".l.seg",
                    "--cMethod=l",
                    "--cThr=2",
                    AudioEventProcessor.RECORDER_RAW_FILENAME
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            MClust.main(new String[] {
                    "--trace",
                    "--help",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--sInputMask=" + basePathName + ".l.seg",
                    "--sOutputMask=" + basePathName + ".h.seg",
                    "--cMethod=h",
                    "--cThr=2",
                    AudioEventProcessor.RECORDER_RAW_FILENAME
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try{
            MTrainInit.main(new String[]{ "--help", "--nbComp=8",
                    "--kind=DIAG",
                    "--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--fInputMask=" +  basePathName + ".mfc",
                    "--sInputMask=" + basePathName + ".h.seg",
                    "--tOutputMask=" + basePathName+".init.gmms",AudioEventProcessor.RECORDER_RAW_FILENAME });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try{
            MTrainEM.main(new String[]{ "--help", "--nbComp=8", "--kind=DIAG",
                    "--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--fInputMask=" +  basePathName + ".mfc",
                    "--sInputMask=" +  basePathName + ".h.seg",
                    "--tOutputMask=" +  basePathName+ ".gee.gmms",
                    "--tInputMask=" +  basePathName+".init.gmms",AudioEventProcessor.RECORDER_RAW_FILENAME });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
            MDecode.main(new String[] {
                    "--trace",
                    "--help",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--sInputMask=" + basePathName + ".h.seg",
                    "--sOutputMask=" + basePathName + ".d.seg",
                    "--dPenality=500",
                    "--tInputMask=" +  basePathName+ ".gee.gmms",
                    AudioEventProcessor.RECORDER_RAW_FILENAME
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try{
            SAdjSeg.main(new String[] { "--trace","--help",
                    "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--sInputMask=" + basePathName + ".d.seg",
                    "--sOutputMask=" + basePathName + ".adj.seg", AudioEventProcessor.RECORDER_RAW_FILENAME});
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try{
            SAdjSeg.main(new String[] { "--trace", "--help", "--sGender", "--sByCluster",
                    "--fInputDesc=sphinx,1:3:2:0:0:0,13,1:1:0",
                    "--fInputMask=" + basePathName + ".mfc",
                    "--sInputMask=" +basePathName + ".adj.seg",
                    "--sOutputMask=" + basePathName + ".g.seg",
                    "--tInputMask=" + genderModel.getAbsolutePath(),
                    AudioEventProcessor.RECORDER_RAW_FILENAME});
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*try{
            MClust.main(new String[] { "--trace","--help",
                "--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,1:1:300:4",
                "--fInputMask=" + basePathName + ".mfc",
                "--sInputMask=" + basePathName + ".g.seg",
                "--sOutputMask=" + basePathName + ".final.seg",
                "--cMethod=ce", "--cThr=1.7", "--emCtrl=1,5,0.01",
                "--sTop=5," + ubmModel.getAbsolutePath(),
                "--tInputMask=" + ubmModel.getAbsolutePath(),
                "--tOutputMask=" + basePathName + ".c.gmm", AudioEventProcessor.RECORDER_RAW_FILENAME });
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }

    public File cacheDirFileOpener(String filename)
    {
        File file = new File(getCacheDir()+"/"+ filename);

        if (!file.exists()) {
            try {

                InputStream is = getAssets().open(filename);
                byte[] buffer = new byte[1024];
                is.read(buffer);
                is.close();


                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private void diarizationProgress() {
        Log.i(TAG, "diarizationProgress()");
        final ProgressDialog diarizationProgress = new ProgressDialog(this);
        diarizationProgress.setMessage(getString(R.string.performing_diarization));
        diarizationProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        diarizationProgress.setIndeterminate(false);
        diarizationProgress.setProgress(0);
        diarizationProgress.show();

        File meetingFile = new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME);
        double diarizationDuration = Helper.howLongWillDiarizationTake(
                Helper.howLongWasMeetingInSeconds(meetingFile.length()),
                MainActivity.processorSpeed
        );
        // 1000 seconds/milliseconds, 100 ticks (each tick is 1% on the progressDialog)
        final long sleepInterval = (long) (1000.0 * diarizationDuration / 100.0);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        sleep(sleepInterval);
                        diarizationProgress.setProgress(i);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /*case R.id.closeTutorial:
                if (tutorialMode = true) {
                    tutorialMode = false;
                    clickPause(null);
                    reset(null);
                    //closeTutorial.setVisibility(View.GONE);
                    Toast.makeText(this, "Closing tutorial",
                            Toast.LENGTH_LONG).show();
                }
                break;*/
            case R.id.menu:
                if (tutorialMode == true && tutorialNumber!=8) {
                    Toast.makeText(this, "FOLLOW INSTRUCTIONS GRRRR >:(",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (tutorialMode == true) {
                    tutorialNumber += 1;
                    ImageView play= (ImageView) findViewById(R.id.play);
                    Tooltip.make(this,
                            new Tooltip.Builder(101)
                                    .anchor(mDrawerLayout, Tooltip.Gravity.RIGHT)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false),0)
                                    .text("In this menu you can access the tutorial again! Close the menu and click finish!")
                                    .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                    .maxWidth(600)
                                    .withArrow(true)
                                    .withOverlay(true).build()
                    ).show();
                    Button finish= (Button) findViewById(R.id.button_finish);
                    Tooltip.make(this,
                            new Tooltip.Builder(101)
                                    .anchor(finish, Tooltip.Gravity.RIGHT)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(false, false),0)
                                    .text("Close the menu and click here to finish!!!")
                                    .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                    .maxWidth(600)
                                    .withArrow(true)
                                    .withOverlay(true).build()
                    ).show();

                }
                mDrawerLayout.openDrawer(Gravity.LEFT);

                break;
            default:
                break;
        }

    }

    public void showCheatSheet(View view, TooltipView tooltipView, CharSequence text) {
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int x = locations[0];
        int y = locations[1];
        int minusx = view.getMeasuredWidth();
        int minusy = view.getMeasuredHeight();
        final int[] screenPos = new int[2]; // origin is device display
        final Rect displayFrame = new Rect(); // includes decorations (e.g. status bar)
        tooltipView.setVisibility(View.VISIBLE);
       /*view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);

        final Context context = view.getContext();
        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        final int viewCenterX = screenPos[0] + viewWidth / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        final int estimatedToastHeight = (int) (48
                * context.getResources().getDisplayMetrics().density);
                */





      /* Toast cheatSheet = Toast.makeText(context, text, Toast.LENGTH_LONG);
        boolean showBelow = screenPos[1] < estimatedToastHeight;
        if (showBelow) {
            // Show below
            // Offsets are after decorations (e.g. status bar) are factored in
            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    viewCenterX - screenWidth / 2,
                    screenPos[1] - displayFrame.top + viewHeight);

            tooltipView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            tooltipView.setX(0);
            tooltipView.setY(0);
            //tooltipView.getOffsetForPosition(viewCenterX - screenWidth / 2,screenPos[1] - displayFrame.top + viewHeight);


        } else {
            // Show above
            // Offsets are after decorations (e.g. status bar) are factored in
            // NOTE: We can't use Gravity.BOTTOM because when the keyboard is up
            // its height isn't factored in.
            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    viewCenterX - screenWidth / 2,
                    screenPos[1] - displayFrame.top + viewHeight);
            tooltipView.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);

            //tooltipView.getOffsetForPosition(viewCenterX - screenWidth / 2,screenPos[1] - displayFrame.top - estimatedToastHeight);

        }
        final int estimatedWith = (int) (tooltipView.getWidth()
                * context.getResources().getDisplayMetrics().density);
**/
        //tooltipView.setX(screenPos[0]+(viewWidth)/8);
        //tooltipView.setY(screenPos[1]-viewHeight/4);
        if (getResources().getResourceEntryName(view.getId()).equals("menu")) {
            tooltipView.setX(x - 3 * minusx);
            tooltipView.setY(y - minusy / 3);
            Log.d("Hello", getResources().getResourceEntryName(view.getId()));
            return;
        }
        if (view instanceof ImageView) {
            tooltipView.setX(x - minusx / 3);
            tooltipView.setY(y - 7 * minusy / 8);
        } else {
            tooltipView.setX(x + minusx / 6);
            tooltipView.setY(y - minusy / 3);
        }


        //cheatSheet.show();

    }
    public void showWaveFile(MenuItem menuItem) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            startTutorial();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to do the tutorial any stored recordings will be reset?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        Log.d("joe","aaa");

    }
    public void launchAboutActivity(MenuItem menuItem) {
        if (tutorialMode==true)
        {
            return;
        }
        Log.i(TAG, "launchAboutActivity()");
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}

