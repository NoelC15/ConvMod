package com.loyola.talktracer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.loyola.blabbertabber.R;
import com.loyola.talktracer.model.AudioRecord.AudioEventProcessor;
import com.loyola.talktracer.model.AudioRecord.RecordingService;
import com.loyola.talktracer.model.Helper;
import com.loyola.talktracer.model.Speaker;
import com.loyola.talktracer.model.SpeakersBuilder;
import com.loyola.talktracer.model.WavFile;
import com.loyola.talktracer.view.TimeBar;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import static java.security.AccessController.getContext;

/**
 * Shows a bar chart of speakers in decreasing order
 */
public class SummaryActivity extends Activity {
    private static final String TAG = "SummaryActivity";
    private long mMeetingDurationInMilliseconds;
    private ArrayList<Speaker> mSpeakers;

    public static String speakerPercent(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return (String.format(Locale.getDefault(), "(%2.0f%%)", speakerPercent));
    }
    public static int speakerPercentint(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return ((int)speakerPercent);
    }
    public static String speakerDuration(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return (String.format(Locale.getDefault(), " %s", Helper.timeToHMMSSFullFormat(speakerDurationInMilliseconds)));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.activity_summary);
        // Nav Drawer, http://stackoverflow.com/questions/26082467/android-on-drawer-closed-listener
//        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        NavigationView mNavigationView = (NavigationView) findViewById(R.id.left_drawer);
//        if (mDrawerLayout == null) {
//            Log.wtf(TAG, "onCreate() mDrawerLayout is NULL!");
//            return;
//        } else {
//            Log.i(TAG, "onCreate() mDrawerLayout is not null!");
//        }
//        ArrayList<Speaker> speakers = new ArrayList<Speaker>();

    }
    /**
     *Creates scale of piano roll
     *
     * @param totalSeconds Total time of meeting
     */
    public String piano_scale(long totalSeconds){
        String totalTime="";
        for (int i =0;i<=totalSeconds+1;i++)
        {
            totalTime+="| ";
        }
        return totalTime;

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_summary);

        String segPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String rawPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        FileInputStream in;
        long rawFileSize = new File(rawPathFileName).length();
        mMeetingDurationInMilliseconds = rawFileSize
                * 1000
                / (AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ * 2);
        try {
            Log.i(TAG, "File size: " + rawFileSize);
            in = new FileInputStream(segPathFileName);
            mSpeakers = new SpeakersBuilder().parseSegStream(in).build();
            Log.i(TAG, "sp.size(): " + mSpeakers.size());
        } catch (IOException e) {
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + segPathFileName);
            Toast.makeText(this, "I could not open the segmentation file, quitting", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
//        durationView.setText(Helper.timeToHMMSS(mMeetingDurationInMilliseconds));

        PieGraph pg = (PieGraph) new PieGraph(this);
        GridLayout pianoGraph=(GridLayout) findViewById(R.id.piano_graph);
        GridLayout pianoGrid=(GridLayout)findViewById(R.id.piano_grid);
        TextView piano_scale= new TextView(this);
        piano_scale.setTypeface(Typeface.DEFAULT_BOLD);
        piano_scale.setText(piano_scale(mMeetingDurationInMilliseconds));
        piano_scale.setTypeface(Typeface.DEFAULT_BOLD);
        pianoGraph.addView(piano_scale);
        PieSlice slice;
        GridLayout speakerGrid = (GridLayout) findViewById(R.id.speaker_duration_grid);
        GridLayout pielayout=(GridLayout) findViewById(R.id.pieGraph);
        //LinearLayout linlayout=(LinearLayout) findViewById(R.id.pieGraph);
        //GridView pieview=(GridView) findViewById(R.id.pieGraph);
       //ConstraintLayout constraintLayout=(ConstraintLayout) findViewById(R.id.pieGraph);
        final float scale = getResources().getDisplayMetrics().density;
        final float textscale = getResources().getDisplayMetrics().scaledDensity;
        int pixels = (int) (58 * scale + 0.5f);
        ArrayList<ArrayList<Object>> speakerlist=new ArrayList<ArrayList<Object>>();
        //LinearLayout timeGraph = (LinearLayout) findViewById(R.id.timeGraph);
        for (int i = 0; i < mSpeakers.size(); i++) {
            ArrayList<Object> temparrlist=new ArrayList<Object>();
            Speaker speaker = mSpeakers.get(i);
            Log.i(TAG, "onResume() speaker: " + speaker.getName() + " sp.size(): " + mSpeakers.size());

            slice = new PieSlice();
            slice.setColor(speaker.getColor());
            slice.setValue(speaker.getTotalDuration());
            pg.addSlice(slice);
            TextView name = new TextView(this);
            name.setText(speaker.getName());
            name.setWidth(pixels);
            speakerGrid.addView(name);
            temparrlist.add(name);



            TextView colour = new TextView(this);
            float percentbar=(float) (78.0*scale+0.5f);
            Log.d("speking","int percent" +Integer.toString(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)));
            Log.d("speking", "scale factor"+Float.toString((speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)/40)*38));
            int percentbar1= (int) Math.round(percentbar*(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)/100.0));
            Log.d("speking", "percent bar"+ Double.toString(percentbar1));
            colour.setText("");
            colour.setBackgroundColor(speaker.getColor());
            colour.setWidth(percentbar1);
            Log.d("spek", Integer.toString(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)));
            speakerGrid.addView(colour);
            temparrlist.add(colour);
            TextView duration = new TextView(this);
            duration.setWidth((int) (60*scale+0.5f));
            duration.setText(speakerPercent(speaker.getTotalDuration(), mMeetingDurationInMilliseconds));
            speakerGrid.addView(duration);
            temparrlist.add(duration);


           TextView timehms=new TextView(this);
            timehms.setTypeface(Typeface.DEFAULT_BOLD);
            timehms.setText(speaker.getName() + "  " +speakerDuration(speaker.getTotalDuration(),mMeetingDurationInMilliseconds));
            pianoGrid.addView(timehms);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) timehms.getLayoutParams();
            params.setGravity(Gravity.RIGHT);
            temparrlist.add(timehms);
            timehms.setLayoutParams(params);




            LinearLayout speakerTimeBar = new LinearLayout(this);
            speakerTimeBar.setOrientation(LinearLayout.HORIZONTAL);
            TimeBar bar = new TimeBar(this);
            bar.setVisible(true);
            bar.setColor(Color.RED);
            bar.setStartTime(0);
            bar.setFinishTime(1000);
            speakerTimeBar.addView(bar);
            //timeGraph.addView(speakerTimeBar);


        }
        pg.setInnerCircleRatio(150);
        pg.setPadding(5);
        pielayout.addView(pg);

    }

    /**
     * Replays the most recent meeting.
     * Called by the navigation drawer.
     *
     * @param menuItem Item selected in navigation drawer.  Unused within method.
     */
    public void replayMeeting(MenuItem menuItem) {
        Log.i(TAG, "replayMeeting()");
        String wavFilePath = WavFile.convertFilenameFromRawToWav(AudioEventProcessor.getRawFilePathName());
        File wavFile = new File(wavFilePath);
        Uri wavFileURI = Uri.fromFile(wavFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(wavFileURI, "audio/x-wav");
        if (wavFile.exists()) {
            Log.i(TAG, "replayMeeting(): wavFile " + wavFilePath + " exists, playing");
            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.v(TAG, "replayMeeting(): resolved activity");
                startActivity(intent);
            } else {
                Log.v(TAG, "replayMeeting(): couldn't resolve activity");
            }
        } else {
            Log.e(TAG, "replayMeeting(): wavFile " + wavFilePath + " doesn't exist");
            Log.wtf(TAG, "The raw file's path name is " + AudioEventProcessor.getRawFilePathName());
            Toast.makeText(getApplicationContext(), "Can't play meeting file " + wavFilePath + "; it doesn't exist.", Toast.LENGTH_LONG).show();
        }
    }

    public void launchMainActivity(MenuItem menuitem) {
        Log.i(TAG, "launchMainActivity()");
        MainActivity.resetFirstTime = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void newMeeting(View v) {
        // clear out the old, raw-PCM file
        AudioEventProcessor.newMeetingFile();
        Intent i = new Intent(this, RecordingActivity.class);
        //Added reset to see if it works with new meeting
        RecordingService.reset = true;
        startActivity(i);
    }

    public void share(View v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSpeakers.size(); i++) {
            Speaker speaker = mSpeakers.get(i);
            sb.append(speakerDuration(speaker.getTotalDuration(), mMeetingDurationInMilliseconds));
            sb.append(speaker.getName());
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "result");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void showRawFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        launchSpeakerStatsActivity(path);
    }

    public void showWavFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".wav";
        launchSpeakerStatsActivity(path);
    }

    public void showMfcFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc";
        launchSpeakerStatsActivity(path);
    }

    public void showUemSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".uem.seg";
        launchSpeakerStatsActivity(path);
    }

    public void showSSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".s.seg";
        launchSpeakerStatsActivity(path);
    }

    public void showLSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        launchSpeakerStatsActivity(path);
    }

    public void launchSpeakerStatsActivity(String path) {
        Log.i(TAG, "launchSpeakerStatsActivity()");
        Intent intent = new Intent(this, SpeakerStatsActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    public void launchAboutActivity(MenuItem menuItem) {
        Log.i(TAG, "launchAboutActivity()");
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
