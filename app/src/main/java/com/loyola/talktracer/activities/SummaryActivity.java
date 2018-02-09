package com.loyola.talktracer.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
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
        PieSlice slice;
        GridLayout speakerGrid = (GridLayout) findViewById(R.id.speaker_duration_grid);
        ConstraintLayout constraintLayout=(ConstraintLayout) findViewById(R.id.pieGraph);
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (58 * scale + 0.5f);

        LinearLayout timeGraph = (LinearLayout) findViewById(R.id.timeGraph);
        for (int i = 0; i < mSpeakers.size(); i++) {
            Speaker speaker = mSpeakers.get(i);
            Log.i(TAG, "onResume() speaker: " + speaker.getName() + " sp.size(): " + mSpeakers.size());
            int percentbar=(int) (speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)*scale+0.5f);
            slice = new PieSlice();
            slice.setColor(speaker.getColor());
            slice.setValue(speaker.getTotalDuration());
            pg.addSlice(slice);


            TextView name = new TextView(this);
            name.setText(speaker.getName());
            name.setWidth(pixels);
            speakerGrid.addView(name);
            TextView duration = new TextView(this);

            duration.setWidth((int) (60*scale+0.5f));
            duration.setText(speakerPercent(speaker.getTotalDuration(), mMeetingDurationInMilliseconds));

            speakerGrid.addView(duration);
            TextView colour = new TextView(this);
            colour.setText("");
            colour.setBackgroundColor(speaker.getColor());
            colour.setWidth(percentbar);
            Log.d("spek", Integer.toString(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)));
            speakerGrid.addView(colour);



            TextView timehms=new TextView(this);
            timehms.setText(speakerDuration(speaker.getTotalDuration(),mMeetingDurationInMilliseconds));
            speakerGrid.addView(timehms);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) timehms.getLayoutParams();
            params.setGravity(Gravity.RIGHT);
            timehms.setLayoutParams(params);



            LinearLayout speakerTimeBar = new LinearLayout(this);
            speakerTimeBar.setOrientation(LinearLayout.HORIZONTAL);
            TimeBar bar = new TimeBar(this);
            bar.setVisible(true);
            bar.setColor(Color.RED);
            bar.setStartTime(0);
            bar.setFinishTime(100);
            speakerTimeBar.addView(bar);
            timeGraph.addView(speakerTimeBar);

        }
        pg.setInnerCircleRatio(150);
        pg.setPadding(5);
        constraintLayout.addView(pg);
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
