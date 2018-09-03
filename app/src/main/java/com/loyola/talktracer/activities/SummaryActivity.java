package com.loyola.talktracer.activities;

import android.support.v4.app.Fragment;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.loyola.blabbertabber.R;
import com.loyola.talktracer.model.AudioRecord.AudioEventProcessor;
import com.loyola.talktracer.model.AudioRecord.RecordingService;
import com.loyola.talktracer.model.Helper;
import com.loyola.talktracer.model.Speaker;
import com.loyola.talktracer.model.SpeakersBuilder;
import com.loyola.talktracer.model.WavFile;
import com.loyola.talktracer.view.TimeBar;
import com.semantive.waveformandroid.waveform.Segment;
import com.semantive.waveformandroid.waveform.WaveformFragment;
import com.venmo.view.TooltipView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.sephiroth.android.library.tooltip.Tooltip;
import nl.changer.audiowife.AudioWife;

import static java.security.AccessController.getContext;

/**
 * Shows a bar chart of speakers in decreasing order
 */
public class SummaryActivity extends FragmentActivity implements View.OnClickListener{
    private static final String TAG = "SummaryActivity";
    /*these three variables for state of testing parse data is to see if we should parse through corpus transcription
    testing mode is to see if we are testing data
    and tutorial mode is for the tutorial- note turn off testing mode when running the tutorial
   */
    public Boolean parseData=false;
    private Boolean testingMode=false;
    private Boolean tutorialMode=false;
   public String testingFileName;

    //these are setups for the  app
    private DrawerLayout mDrawerLayout;
    private Button buton;
    private long mMeetingDurationInMilliseconds;
    private ArrayList<Speaker> mSpeakers;
    private ArrayList<Tooltip.TooltipView> tipviews;
    static SummaryActivity obj;
    static ArrayList<Segment> segarray;



    public static String speakerPercent(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return (String.format(Locale.getDefault(), "(%2.0f%%)", speakerPercent));

    }

    public static int speakerPercentint(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return ((int)speakerPercent);
    }

    public static String speakerDuration(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        return (String.format(Locale.getDefault(), " %s", Helper.timeToHMMSSFullFormat(speakerDurationInMilliseconds)));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.a);
    }


    @Override
    public void onClick(View view) {
        mDrawerLayout.openDrawer(Gravity.START);

    }


    //used for a calculation
    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public ArrayList<Speaker> speakerSegmentFiles()
    {
        ArrayList<Speaker> speakers ;
        // Opening the necessary segmentation files
        String isegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".i.seg";
        String ssegPathFileName=getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".s.seg";
        //String pmssegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".pms.seg";
        //String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String hsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".h.seg";
        String dsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".d.seg";
        String adjsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".adj.seg";
        String gsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".g.seg";
        //String finalsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".seg";


        FileInputStream in;
        FileInputStream intest;
        FileInputStream stest;
        FileInputStream ltest;
        FileInputStream htest;
        FileInputStream dtest;
        FileInputStream adjtest;
        FileInputStream gtest;
        // FileInputStream finaltest;

        try {

            in = new FileInputStream(gsegPathFileName);
            intest = new FileInputStream(isegPathFileName);
            stest=new FileInputStream(ssegPathFileName);
            ltest=new FileInputStream(lsegPathFileName);
            htest=new FileInputStream(hsegPathFileName);
            dtest=new FileInputStream(dsegPathFileName);
            adjtest=new FileInputStream(adjsegPathFileName);
            //  finaltest=new FileInputStream(finalsegPathFileName);
            gtest=new FileInputStream(gsegPathFileName);
            Log.d("hey","tostring"+ htest.toString());
            //inpmsseg=new FileInputStream(pmssegPathFileName);
            SpeakersBuilder speker= new SpeakersBuilder();
            Log.d("hey","i first");
            speker.logSegStream(intest);
            Log.d("hey","s second");
            speker.logSegStream(stest);
            Log.d("hey","l test third");
            speker.logSegStream(ltest);
            Log.d("hey","h test fourth");
            speker.logSegStream(htest);
            Log.d("hey","d test fifth");
            speker.logSegStream(dtest);
            Log.d("hey","adj test six");
            speker.logSegStream(adjtest);
            Log.d("hey","g test seve");
            speker.logSegStream(gtest);
            //Log.d("hey","final test eigth");
            //speker.logSegStream(finaltest);
            speakers = new SpeakersBuilder().parseSegStream(in).build();
            return speakers;

        } catch (IOException e) {
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + dsegPathFileName);
            Toast.makeText(this, "I could not open the segmentation file, quitting", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }

    }
    public void sortSpeakers(ArrayList<Speaker> speakers) {
        Comparator<Speaker> comp = new Comparator<Speaker>() {
            @Override
            public int compare(Speaker o1, Speaker o2) {
                if (o1.getStartTimes().get(0) > o2.getStartTimes().get(0)) {
                    return -1;
                } else if (o1.getStartTimes().get(0) == o2.getStartTimes().get(0)) {
                    return 0;
                } else {
                    return 1;
                }

            }
        };
        Collections.sort(speakers, comp);
    }

    /**
     * this initializes the interface and sets up the variables for use
     */
    public void initializeVariables()
    {
        obj=this;
        tipviews= new ArrayList<Tooltip.TooltipView>();
        segarray=new ArrayList<Segment>();
        Button menuSummary= (Button) findViewById(R.id.menuSummary);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerSummary_layout);
        buton=(Button)findViewById(R.id.menuSummary);
        buton.setOnClickListener(SummaryActivity.this);


        // state of app determined by tutorial, testing mode , or wheter you have data transcriptitons to parse
        tutorialMode= getIntent().getBooleanExtra("TUTORIAL",false);
        testingFileName= getIntent().getStringExtra("TESTINGFILENAME");
        testingMode=getIntent().getBooleanExtra("TESTING",false);
        parseData=getIntent().getBooleanExtra("PARSEDATA",false);

        //Raw file path and getting the time of the audio clip
        String rawPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        long rawFileSize = new File(rawPathFileName).length();
        mMeetingDurationInMilliseconds = rawFileSize * 1000 / (AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ * 2);

        mSpeakers=speakerSegmentFiles();
        sortSpeakers(mSpeakers);
        for(int i =0;i<mSpeakers.size();i++)
        {
            mSpeakers.get(i).setName("s"+ (mSpeakers.size()-i));
        }


    }
    @Override
    protected void onResume() {
        //setting up stuff for interface and stuff
        super.onResume();
        setContentView(R.layout.a);

        //this initializes varaibles and the interface
         initializeVariables();

        //run tutorial if true
        if (tutorialMode==true){
            startTutorial();
        }

        // THIS IS THE ARRAYLIST OF THE SPEAKERS EVERYTHING ABOUT THE SPEAKERS WHICH WILL BE USED TO DISPLAY ON THE GRAPHS IS HERE

        createPieGraph();
        createPianoRollBar();



        if(!testingMode || (testingMode && parseData)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.soundWaveContainer, new CustomWaveformFragment())
                    .commit();
        }


    }

    public void createPieGraph() {
        PieSlice slice;
        ArrayList<String> labels = new ArrayList<String>();
        PieChart pieChart = (PieChart) findViewById(R.id.chart);
        List<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colorz = new ArrayList<Integer>();

        for (int i = 0; i < mSpeakers.size(); i++) {


            ArrayList<Object> temparrlist = new ArrayList<Object>();
            Speaker speaker = mSpeakers.get(i);
            labels.add(speaker.getName());
            colorz.add(speaker.getColor());

            //adding a slice to the piegraph
            slice = new PieSlice();
            slice.setColor(speaker.getColor());
            slice.setValue(speaker.getTotalDuration());
            entries.add(new PieEntry((speakerPercentint(speaker.getTotalDuration(), mMeetingDurationInMilliseconds)), speaker.getName()));


            Description description = new Description();
            description.setText("Percentage spoken Pie Chart");
            Description description1 = new Description();
            description1.setText("Percentage spoken Bar Chart");

            pieChart.setDescription(description);

            PieDataSet set = new PieDataSet(entries, "Speaker names and colors");
            pieChart.setDrawSliceText(false);
            set.setSliceSpace(1);
            set.setValueTextSize(8);
            set.setColor(Color.WHITE);
            PieData data = new PieData(set);
            pieChart.setData(data);
            set.setColors(colorz);
            pieChart.invalidate(); // refresh

        }
    }


    public void createPianoRollBar()
    {


        GridLayout pianoGraph=(GridLayout) findViewById(R.id.piano_graph);
        GridLayout pianoGrid=(GridLayout)findViewById(R.id.piano_grid);
        GridLayout piano_scale= piano_scale(mMeetingDurationInMilliseconds);


        TextView total_time= (TextView) findViewById(R.id.total_time);
        TextView piano_scale1=new TextView(this);


        piano_scale1.setTypeface(Typeface.DEFAULT_BOLD);
        piano_scale1.setText(piano_scale1(mMeetingDurationInMilliseconds));


        TextView empty= (TextView) findViewById(R.id.pianoGraphLabel);
        empty.setText("TIME IN SECONDS");
        empty.setTypeface(Typeface.DEFAULT_BOLD);
        total_time.setText("Total  "+speakerDuration(mMeetingDurationInMilliseconds,mMeetingDurationInMilliseconds));


        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (58 * scale + 0.5f);


        for (int i = 0; i < mSpeakers.size(); i++) {


            ArrayList<Object> temparrlist=new ArrayList<Object>();
            Speaker speaker = mSpeakers.get(i);


            TextView name = new TextView(this);
            name.setText(speaker.getName());
            name.setWidth(pixels);

            temparrlist.add(name);




            GridLayout tempbar= new GridLayout(this);
            float percentbar2=(float) (70.0*scale+0.5f);
            for (int j=0;j<speaker.getStartTimes().size();j++)
            {int pianobarwidth= (int) Math.floor(percentbar2* speaker.getDurations().get(j)/10000.0);

                if (speaker.getStartTimes().get(j)<=0.0)
                {

                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText(Integer.toString((int) Math.floor(speaker.getDurations().get(j)/1000)));
                    pianoViewBar.setGravity(Gravity.CENTER);
                    pianoViewBar.setWidth(pianobarwidth);
                    pianoViewBar.setBackgroundColor(speaker.getColor());
                    tempbar.addView(pianoViewBar);

                }
                else if (j==0)
                {
                    int nonSpeakWdith=(int) Math.floor(percentbar2* speaker.getStartTimes().get(j)/10000.0);
                    TextView pianoViewBar1=new TextView(this);
                    pianoViewBar1.setText("");
                    pianoViewBar1.setWidth(nonSpeakWdith);
                    pianoViewBar1.setBackgroundColor(Color.parseColor("#00000000"));
                    tempbar.addView(pianoViewBar1);
                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText(Integer.toString((int) Math.floor(speaker.getDurations().get(j)/1000)));
                    pianoViewBar.setGravity(Gravity.CENTER);
                    pianoViewBar.setWidth(pianobarwidth);
                    pianoViewBar.setBackgroundColor(speaker.getColor());
                    tempbar.addView(pianoViewBar);
                }
                else{
                    Long nonSpeaktime= speaker.getStartTimes().get(j)-(speaker.getDurations().get(j-1)+speaker.getStartTimes().get(j-1));
                    int nonSpeakWdith=(int) Math.floor(percentbar2* (nonSpeaktime/10000.0));
                    TextView pianoViewBar1=new TextView(this);
                    pianoViewBar1.setText("");
                    pianoViewBar1.setWidth(nonSpeakWdith);
                    pianoViewBar1.setBackgroundColor(Color.parseColor("#00000000"));
                    tempbar.addView(pianoViewBar1);
                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText(Integer.toString((int) Math.floor(speaker.getDurations().get(j)/1000)));
                    pianoViewBar.setGravity(Gravity.CENTER);
                    pianoViewBar.setBackgroundColor(speaker.getColor());
                    pianoViewBar.setWidth(pianobarwidth);
                    tempbar.addView(pianoViewBar);
                }
                segarray.add(new Segment(round((double)Math.floor(speaker.getStartTimes().get(j)/1000) ,1),round((double)Math.floor(speaker.getStartTimes().get(j)/1000+speaker.getDurations().get(j)/1000),1),speaker.getColor()));
            }

            pianoGraph.addView(tempbar);


            //creating the color bar on the piano graph
            TextView colour = new TextView(this);
            float percentbar=(float) (78.0*scale+0.5f);
            int percentbar1= (int) Math.round(percentbar*(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)/100.0));
            colour.setText("");
            colour.setBackgroundColor(speaker.getColor());
            colour.setWidth(percentbar1);
            temparrlist.add(colour);


            TextView duration = new TextView(this);
            duration.setWidth((int) (60*scale+0.5f));
            duration.setText(speakerPercent(speaker.getTotalDuration(), mMeetingDurationInMilliseconds));
            temparrlist.add(duration);
            TextView timehms=new TextView(this);
            timehms.setTypeface(Typeface.DEFAULT_BOLD);
            timehms.setText(speaker.getName() + "   " +speakerDuration(speaker.getTotalDuration(),mMeetingDurationInMilliseconds));
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



        }



        pianoGraph.addView(piano_scale1);
        pianoGraph.addView(piano_scale);

    }



    public static class CustomWaveformFragment extends WaveformFragment {

        /**
         * Provide path to your audio file.
         *
         * @return
         */
        @Override
        protected String getFileName() {
            //return obj.getFilesDir() + "/"  + "redd.wav";
            return obj.getFilesDir()+"/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".wav";
        }

        /**
         * Optional - provide list of segments (start and stop values in seconds) and their corresponding colors
         *
         * @return
         */
        @Override
        protected List<Segment> getSegments() {
            return segarray;
        }
    }


    /**
     * for the summary activity the tutorial is self contained in a single method as the tutorial is not interactive.
     * This is the result
     */
    public void startTutorial(){
        FrameLayout soundwaveContainer= (FrameLayout)findViewById(R.id.soundWaveContainer);
        Button menuSummary= (Button)findViewById(R.id.menuSummary);
        PieChart pieChart= (PieChart) findViewById(R.id.chart);
        LinearLayout coord= (LinearLayout) findViewById(R.id.layout);
        GridLayout grid= (GridLayout) findViewById(R.id.piano_graph);
        CoordinatorLayout coord1= (CoordinatorLayout)findViewById(R.id.full);

        Tooltip.TooltipView tooltipView= Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(coord, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),3000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("The rest of this tutorial will play by its own sit back and relax.")
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView.show();

       Tooltip.TooltipView tooltipView1= Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(pieChart, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),6000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a pie chart of the percentage spoken")
                        .showDelay(3000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView1.show();
        Tooltip.TooltipView tooltipView2 = Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(grid, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),9000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a piano roll of time spoken per person")
                        .activateDelay(0)
                        .showDelay(6000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView2.show();

       Tooltip.TooltipView tooltipView3= Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(soundwaveContainer, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),12000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a audio playback to listen to the conversation")
                        .activateDelay(0)
                        .showDelay(9000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView3.show();
        Tooltip.TooltipView endtutorial= Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(coord, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),15000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This concludes our tutorial click new meeting button when you are ready.")
                        .showDelay(12000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView.show();
        tipviews.add(tooltipView);
        tipviews.add(tooltipView1);
        tipviews.add(tooltipView2);
        tipviews.add(tooltipView3);



    }


    public void newMeeting(View v) {
        if (tutorialMode){
            if (!tipviews.isEmpty())
            {
                for (Tooltip.TooltipView tipview: tipviews)
                {
                    if (tipview.isShown())
                    {
                        return;
                    }
                }

            }
            tutorialMode=false;

        }
        // clear out the old, raw-PCM file



        AudioEventProcessor.newMeetingFile();
        Intent i = new Intent(this, RecordingActivity.class);
        //Added reset to see if it works with new meeting
        RecordingService.reset = true;
        startActivity(i);
    }

    public void share(View v) {
        if (tutorialMode)
        {
            return;
        }
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
    /**
     *This and piano_scale1 are used to create the tick marks of the piano roll
     *
     * @param totalmSeconds Total time of meeting
     */
    public GridLayout piano_scale(long totalmSeconds){
        final float scale = getResources().getDisplayMetrics().density;
        float percentbar2=(float) (70.0*scale+0.5f);
        GridLayout tempbar= new GridLayout(this);

        DisplayMetrics displayMetrics = SummaryActivity.this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float maximum=dpWidth/80;
        int height =getResources().getDisplayMetrics().heightPixels;
        int width = getResources().getDisplayMetrics().widthPixels;
        double totalSeconds=totalmSeconds/1000.0;

        for (int i =0;(i<maximum||i<Math.floor(totalSeconds+25)/10);i++)
        {
            String itimes10=Integer.toString(i*10);
            itimes10 = itimes10.replaceAll("0", "⁰");
            itimes10 = itimes10.replaceAll("1", "¹");
            itimes10 = itimes10.replaceAll("2", "²");
            itimes10 = itimes10.replaceAll("3", "³");
            itimes10 = itimes10.replaceAll("4", "⁴");
            itimes10 = itimes10.replaceAll("5", "⁵");
            itimes10 = itimes10.replaceAll("6", "⁶");
            itimes10 = itimes10.replaceAll("7", "⁷");
            itimes10 = itimes10.replaceAll("8", "⁸");
            itimes10 = itimes10.replaceAll("9", "⁹");
            if (i==0)
            {
                TextView temptext = new TextView(this);
                temptext.setText(itimes10);
                int pianobarwidth = (int) Math.floor(percentbar2 * 9500 / 10000.0);
                temptext.setWidth(pianobarwidth);
                tempbar.addView(temptext);
            }
            else {
                TextView temptext = new TextView(this);
                temptext.setText(itimes10);
                int pianobarwidth1 = (int) Math.floor(percentbar2 * 10000 / 10000.0);
                temptext.setWidth(pianobarwidth1);
                tempbar.addView(temptext);
            }


        }

        return tempbar;

    }

    /**
     * this method and piano_scale are used to create the tick marks under the piano roll
     * @param totalmSeconds
     * @return
     */
    public String piano_scale1(long totalmSeconds){
        String totalTime="";
        DisplayMetrics displayMetrics = SummaryActivity.this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float maximum=dpWidth/8;
        int height =getResources().getDisplayMetrics().heightPixels;
        int width = getResources().getDisplayMetrics().widthPixels;
        double totalSeconds=totalmSeconds/1000.0;
        for (int i =0; i<maximum||i<totalSeconds+25;i++)
        {

            if (i==0) {
                totalTime += "|";
            }
            else if (i%10==0)
            {
                totalTime+=" |";
            }

            else {
                totalTime += " l";
            }
        }
        return totalTime;

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


    //EVERTYTHING BELOW HERE IS UNUSED

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
    public void replay() {
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


}
