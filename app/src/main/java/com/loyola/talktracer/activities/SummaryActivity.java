package com.loyola.talktracer.activities;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
    private Boolean tutorialMode=false;
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
    @Override
    public void onClick(View view) {
        Log.d("aaa","HEY");
        if (tutorialMode==true){
        }
        mDrawerLayout.openDrawer(Gravity.START);

    }
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
    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }
    private ArrayList<ArrayList<String>> textViewer() {

        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        ArrayList<ArrayList<String>> timeHolder= new ArrayList<ArrayList<String>>();
        String[] fil;
        try {
            fil= assetManager.list("");

        for (String assets: fil)
        {
            if (assets.contains("ES2003a")&& assets.contains(".xml"))
            { Log.d("parser", assets);

                try {
                    inputStream = assetManager.open(assets);
                } catch (IOException e) {
                    Log.e("tag", e.getMessage());
                }

                String s = readTextFile(inputStream);
                String[] spli= s.split(" ");
                ArrayList<String> temp= new ArrayList<String>();
                String hold = "";
                for (String word :spli )
                {
                    if (word.contains("transcriber_start"))
                    {
                        temp.add("1s" + Double.parseDouble(word.replaceAll("[^\\d.]", "")));
                    }
                    if (word.contains("transcriber_end"))
                    {
                        temp.add("1e"+ Double.parseDouble(word.replaceAll("[^\\d.]", "")));
                    }
                }

                timeHolder.add(temp);


            }
        }
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        return timeHolder;
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.a);
        obj=this;
        DecimalFormat df = new DecimalFormat("#.#");
        tutorialMode= getIntent().getBooleanExtra("TUTORIAL",false);
        tipviews= new ArrayList<Tooltip.TooltipView>();
        //FloatingActionButton closeTutorial= (FloatingActionButton) findViewById(R.id.closeTutorial1);
        if (tutorialMode==true){
            //closeTutorial.setVisibility(View.VISIBLE);
            startTutorial();
        }


        segarray=new ArrayList<Segment>();
        Log.d("Tutorial", Boolean.toString(tutorialMode));
        Button menuSummary= (Button) findViewById(R.id.menuSummary);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerSummary_layout);
        buton=(Button)findViewById(R.id.menuSummary);
        buton.setOnClickListener(SummaryActivity.this);
        String isegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".i.seg";
        String ssegPathFileName=getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".s.seg";
        String pmssegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".pms.seg";
        //String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String hsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".h.seg";
        String dsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".d.seg";
        String adjsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".adj.seg";
        String gsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".g.seg";
        //String finalsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".seg";
        String rawPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        String wavPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".wav";
        String testWave= getCacheDir()+"/redd.wav";

        FileInputStream in;
        FileInputStream intest;
        FileInputStream pmstest;
        FileInputStream stest;
        FileInputStream ltest;
        FileInputStream htest;
        FileInputStream dtest;
        FileInputStream adjtest;
        FileInputStream gtest;
        File dirFiles =getFilesDir();
        for (String strFile : dirFiles.list())
        {
           Log.d("obj",strFile); // strFile is the file name
        }
       // FileInputStream finaltest;
        long rawFileSize=new File(getFilesDir() + "/" +"ES2003a.Mix-Headset.wav").length();
        Uri uri = Uri.parse(getFilesDir() + "/" +"ES2003a.Mix-Headset.wav");
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this,uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        //long rawFileSize = new File(rawPathFileName).length();
        mMeetingDurationInMilliseconds = rawFileSize
                * 1000
                / (AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ * 2);
        try {
            Log.i(TAG, "File size: " + rawFileSize);
            in = new FileInputStream(gsegPathFileName);
            //pmstest= new FileInputStream(pmssegPathFileName);
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
            Log.d("hey","ps");
            //speker.logSegStream(pmstest);
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
            mSpeakers = new SpeakersBuilder().parseSegStream(in).build();
            Log.i(TAG, "sp.size(): " + mSpeakers.size());
            Log.d("audiosource","bael");
        } catch (IOException e) {
            Log.d("audiosource","boo");
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + dsegPathFileName);
            Toast.makeText(this, "I could not open the segmentation file, quitting", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
        Comparator<Speaker> comp = new Comparator<Speaker>() {
            @Override
            public int compare(Speaker o1, Speaker o2) {
                if(o1.getStartTimes().get(0)>o2.getStartTimes().get(0))
                {
                    return -1;
                }
                else if(o1.getStartTimes().get(0)==o2.getStartTimes().get(0))
                {
                    return 0;
                }
                else{
                    return 1;
                }

            }
        };
        Collections.sort(mSpeakers,comp);
        for(int i =0;i<mSpeakers.size();i++)
        {
            mSpeakers.get(i).setName("s"+ (mSpeakers.size()-i));
        }
//        durationView.setText(Helper.timeToHMMSS(mMeetingDurationInMilliseconds));
        //HorizontalBarChart horizontalBarChart=(HorizontalBarChart) findViewById(R.id.horizBarChart);
        //horizontalBarChart.setMaxVisibleValueCount((int) Math.round(mMeetingDurationInMilliseconds/1000));
        //BarChart barChart = (BarChart) findViewById(R.id.barChart);
        ArrayList<BarEntry> barentry2=new ArrayList<BarEntry>();
        ArrayList<BarEntry> barEntries = new ArrayList<BarEntry>();

        ArrayList<String> labels= new ArrayList<String>();
        //PieGraph pg = (PieGraph) new PieGraph(this);
        GridLayout pianoGraph=(GridLayout) findViewById(R.id.piano_graph);
        GridLayout pianoGrid=(GridLayout)findViewById(R.id.piano_grid);
        //GridLayout piano_scale= new GridLayout(this);
        GridLayout piano_scale= new GridLayout(this);
        TextView total_time= (TextView) findViewById(R.id.total_time);
        piano_scale=piano_scale(mMeetingDurationInMilliseconds);
        TextView piano_scale1=new TextView(this);
        TextView empty= (TextView) findViewById(R.id.pianoGraphLabel);
        empty.setText("TIME IN SECONDS");
        empty.setTypeface(Typeface.DEFAULT_BOLD);
        total_time.setText("Total  "+speakerDuration(mMeetingDurationInMilliseconds,mMeetingDurationInMilliseconds));
        piano_scale1.setTypeface(Typeface.DEFAULT_BOLD);
        piano_scale1.setText(piano_scale1(mMeetingDurationInMilliseconds));
        PieSlice slice;
        //GridLayout speakerGrid = (GridLayout) findViewById(R.id.speaker_duration_grid);
        PieChart pieChart= (PieChart)findViewById(R.id.chart);
        List<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colorz= new ArrayList<Integer>();
        //GridLayout pielayout=(GridLayout) findViewById(R.id.pieGraph);
        //LinearLayout linlayout=(LinearLayout) findViewById(R.id.pieGraph);
        //GridView pieview=(GridView) findViewById(R.id.pieGraph);
       //ConstraintLayout constraintLayout=(ConstraintLayout) findViewById(R.id.pieGraph);
        ArrayList<ArrayList<String>> times = new ArrayList<ArrayList<String>>();
        final float scale = getResources().getDisplayMetrics().density;
        final float textscale = getResources().getDisplayMetrics().scaledDensity;
        int pixels = (int) (58 * scale + 0.5f);
        ArrayList<ArrayList<Object>> speakerlist=new ArrayList<ArrayList<Object>>();

        //LinearLayout timeGraph = (LinearLayout) findViewById(R.id.timeGraph);
        for (int i = 0; i < mSpeakers.size(); i++) {
            ArrayList<Object> temparrlist=new ArrayList<Object>();
            Speaker speaker = mSpeakers.get(i);

            labels.add(speaker.getName());
            colorz.add(speaker.getColor());
            Log.i(TAG, "onResume() speaker: " + speaker.getName() + " sp.size(): " + mSpeakers.size());
            slice = new PieSlice();
            slice.setColor(speaker.getColor());
            slice.setValue(speaker.getTotalDuration());
            //pg.addSlice(slice);
            entries.add(new PieEntry((speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)), speaker.getName()));
            barEntries.add(new BarEntry(i*10f,(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds))));
            TextView name = new TextView(this);
            name.setText(speaker.getName());
            name.setWidth(pixels);
            //speakerGrid.addView(name);
            temparrlist.add(name);
            Float spekertime=(float) Math.round(speaker.getTotalDuration());
            barentry2.add(new BarEntry((float) i, new float[] {spekertime}));
            float percentbar2=(float) (70.0*scale+0.5f);
            TextView colour = new TextView(this);
            float percentbar=(float) (78.0*scale+0.5f);
            GridLayout tempbar= new GridLayout(this);
            ArrayList<String> holder=new ArrayList<String>();
            for (int j=0;j<speaker.getStartTimes().size();j++)
            {
                int pianobarwidth= (int) Math.floor(percentbar2* speaker.getDurations().get(j)/10000.0);
                if(!speaker.getStartTimes().isEmpty()) {


                    holder.add("2s" +Double.toString(speaker.getStartTimes().get(j).doubleValue()/1000.0));
                    holder.add( "2e"+Double.toString(speaker.getStartTimes().get(j).doubleValue()/1000.0 + speaker.getDurations().get(j).doubleValue()/1000.0));
                }
                if (speaker.getStartTimes().get(j)<=0.0)
                {

                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText(Integer.toString((int) Math.floor(speaker.getDurations().get(j)/1000.0)));
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
            Log.d("audiosource","blay");
            pianoGraph.addView(tempbar);
            Log.d("speking","int percent" +Integer.toString(speakerPercentint(speaker.getTotalDuration( ),mMeetingDurationInMilliseconds)));
            Log.d("speking", "scale factor"+Float.toString((speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)/40)*38));
            int percentbar1= (int) Math.round(percentbar*(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)/100.0));
            Log.d("speking", "percent bar"+ Double.toString(percentbar1));
            colour.setText("");
            colour.setBackgroundColor(speaker.getColor());
            colour.setWidth(percentbar1);
            Log.d("spek", Integer.toString(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)));
            //speakerGrid.addView(colour);
            temparrlist.add(colour);
            TextView duration = new TextView(this);
            duration.setWidth((int) (60*scale+0.5f));
            duration.setText(speakerPercent(speaker.getTotalDuration(), mMeetingDurationInMilliseconds));
            //speakerGrid.addView(duration);
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
            times.add(holder);

            //timeGraph.addView(speakerTimeBar);



        }
        //pg.setInnerCircleRatio(150);
        //pg.setPadding(5);
        //pielayout.addView(pg);

       // BarDataSet set1=new BarDataSet(barentry2,"abc");
       // BarData data1= new BarData(set1);
       // horizontalBarChart.setData(data1);
       // horizontalBarChart.setFitBars(true);
        //horizontalBarChart.invalidate();
       // XAxis xAxis = barChart.getXAxis();
        //YAxis yAxis=barChart.getAxisLeft();
       // yAxis.setAxisMinimum(0f);
       // yAxis.setAxisMaximum(100f);
       // YAxis bottomYAxis=barChart.getAxisRight();
       // bottomYAxis.setEnabled(false);
       // bottomYAxis.setAxisMinimum(0f);
        //xAxis.setAxisMaximum(mSpeakers.size()*10);
        //xAxis.setAxisMinimum(-10f);
        //xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
       // xAxis.setDrawAxisLine(false);
       // xAxis.setDrawGridLines(false);
       // xAxis.setEnabled(false);

        ArrayList<ArrayList<String>> realtimes= textViewer();
        Collections.sort(realtimes, new Comparator<ArrayList<String>>() {
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                if(Double.parseDouble(o1.get(0).substring(2))>=(Double.parseDouble(o2.get(0).substring(2))))
                {
                    return 1;
                }
                else{
                    return -1;
                }
            }
        });
        Collections.reverse(times);
        ArrayList<ArrayList<String>> reimannrealtimes= new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> reimanntimes= new ArrayList<ArrayList<String>>();
        for (ArrayList<String> real : realtimes)
        {
            reimannrealtimes.add(reimann(real));
        }
        for (ArrayList<String> time: times)
        {
            reimanntimes.add(reimann(time));
        }
        Log.d("reimannrealtimes",reimannrealtimes.get(1).subList(0,200).toString());
        Log.d("reimanntimes",reimanntimes.get(0).subList(0,200).toString());
        Log.d("realtimes",realtimes.toString());
        Log.d("times",times.toString());

        for(int i =0; i<reimannrealtimes.size();i++)
        {
         reimannrealtimes.get(i).removeAll(reimanntimes.get(i));
        }
        Log.d("index",Integer.toString(reimannrealtimes.indexOf("y999.9")));
        Log.d("index",Integer.toString(reimannrealtimes.indexOf("n999.9")));
        try {
            Log.d("reimannrealtimes", "new times are /n" + reimannrealtimes.get(0).subList(3000, 3500).toString());
        }
        catch (Exception e)
        {

        }
        ArrayList<ArrayList<Double>> doublez= new ArrayList<ArrayList<Double>>();
        for (int i=0;i<reimannrealtimes.size();i++) {
            doublez.add(new ArrayList<Double>());
            for (int j = 0; j < reimannrealtimes.get(i).size(); j++) {
                try {
                    Double timing = Double.parseDouble(reimannrealtimes.get(i).get(j).replaceAll("[^\\d.]", ""));
                    doublez.get(i).add(timing);
                } catch (Exception e) {
                    Log.d("Dubs1", Integer.toString(i) + Integer.toString(j));
                }
            }

            // Log.d("Dubs",Integer.toString(i)+ reimannrealtimes.get(i).subList(0,20).toString());
        }
        for (int i=reimannrealtimes.size();i<reimanntimes.size();i++) {
          doublez.add(new ArrayList<Double>());
            for (int j = 0; j < reimanntimes.get(i).size(); j++) {
                try {
                    if(reimanntimes.get(i).get(j).contains("y")) {
                        Double timing = Double.parseDouble(reimanntimes.get(i).get(j).replaceAll("[^\\d.]", ""));
                        doublez.get(i).add(timing);
                        Log.d("Dubs7", "z " +Integer.toString(i) + "j " +Integer.toString(j));
                        Log.d("Dubs7","z " +reimanntimes.get(i).get(j).toString());

                    }
                }
                catch (Exception e) {
                    Log.d("Dubs8", "a " +Integer.toString(i) + "j " +Integer.toString(j));
                    Log.d("Dubs8","a " +reimanntimes.get(i).get(j).toString());
                }
            }



            Log.d("Dubs6", Integer.toString(doublez.size()));
        }
        ArrayList<Double> finaldoublez= new ArrayList<Double>();
        for (int i=0;i<doublez.size();i++)
        {
            finaldoublez.removeAll(doublez.get(i));
            finaldoublez.addAll(doublez.get(i));
        }
        double timeWronged=0.0;
        Collections.sort(finaldoublez);

        Log.d("finald",Integer.toString(finaldoublez.size()));
        Log.d("finald",finaldoublez.toString().substring(finaldoublez.size()-8));
        Log.d("finald",finaldoublez.toString().substring(0,500));
        Log.d("finald",finaldoublez.toString().substring(1000,1500));

        for(int i=0; i<finaldoublez.size()-1;i++)
        {
            if (finaldoublez.get(i+1)-finaldoublez.get(i)>.09 && finaldoublez.get(i+1)-finaldoublez.get(i)<.11)
            {
                timeWronged+=.1;
            }
        }
        Log.d("timeWronged",Double.toString(timeWronged));




        //Double timewronged= 0.0;
        //ArrayList<String> withoutStrings=new ArrayList<String>();
        //ArrayList<ArrayList<Double>> withDoubles= new ArrayList<ArrayList<Double>>();



       /* for (int i=0;i<reimannrealtimes.size();i++)
        {
            for (int j=0;j<reimannrealtimes.get(i).size();j++)
            {
                if (!withoutStrings.contains(reimannrealtimes.get(i).get(j).substring(1)))
                {
                    withoutStrings.add(reimannrealtimes.get(i).get(j).substring((1)));
                }
            }

        }
        Log.d("withoutStrings", withoutStrings.subList(0,500).toString());*/

        /*for(int i =0; i<reimannrealtimes.size();i++)
        {
           for(int j=0; j<reimannrealtimes.get(i).size();j++)
            {
                reimannrealtimes.get(i).set(j,reimannrealtimes.get(i).get(j).substring(1));
            }
            Log.d("onlyNumbers", Integer.toString(i) + reimannrealtimes.get(0).subList(0,500).toString());
        }
        Log.d("onlyNumbers",reimannrealtimes.get(0).subList(0,500).toString());
        for (int i =1;i<reimannrealtimes.size();i++)
        {
            reimannrealtimes.get(0).removeAll(reimannrealtimes.get(i));
            reimannrealtimes.get(0).addAll(reimannrealtimes.get(i));
        }
        Collections.sort(reimannrealtimes.get(0));
        Log.d("reimannrealtimesSubset",reimannrealtimes.get(0).subList(0,500).toString());
        ArrayList<String> timez= new ArrayList<String>();
        if(reimannrealtimes.size()<reimanntimes.size()) {
            for (int i =reimannrealtimes.size(); i<reimanntimes.size();i++)
            {
                for (int j= 0;j<reimanntimes.get(i).size();j++)
                {
                    if (reimanntimes.get(i).get(j).contains("y"))
                    {
                        if(j<30) {
                            Log.d("checking", reimanntimes.get(i).get(j));
                        }
                        timez.set(0, reimanntimes.get(i).get(j).substring(1));
                    }
                }
            }
        }*/

       // Log.d("timez",timez.toString());
        //reimannrealtimes.get(0).removeAll(timez);
       // reimannrealtimes.get(0).addAll(timez);
        //Log.d("lastCheck",reimannrealtimes.get(0).subList(0,1000).toString());


        /*   for (int i=0;i<realtimes.size();i++)
            {   Double start= Math.min(Double.parseDouble(realtimes.get(i).get(0).substring(2)),Double.parseDouble(times.get(i).get(0).substring(2)));
                int whichList;
                if(start==(Double.parseDouble(realtimes.get(i).get(0).substring(2))))
                {
                    //recursCompare(realtimes.get(i),times.get(i),0);
                }
                else if(start==Double.parseDouble(times.get(i).get(0).substring(2))){
                    //recursCompare(times.get(i),realtimes.get(i),0);
                }
                Log.d("concat",start.toString());

                if(i<times.size()) {
                    realtimes.get(i).addAll(times.get(i));
                    Collections.sort(realtimes.get(i), new Comparator<String>() {
                        public int compare(String o1, String o2) {
                            if(Double.parseDouble(o1.substring(2))>=(Double.parseDouble(o2.substring(2))))
                            {
                                return 1;
                            }
                            else{
                                return -1;
                            }
                        }
                    });
                    String first=realtimes.get(i).get(0);
                    String second=realtimes.get(i).get(1);
                    String third;
                    for(String j :realtimes.get(i).subList(2,realtimes.size()-1)) {
                        third = j;
                        if (first.substring(0, 1).equals("1s")) {
                            if (second.substring(0, 1).equals("2s")) {
                                timewronged += Double.parseDouble(second.substring(2)) - (Double.parseDouble(first.substring(2)));
                            } else if (second.substring(0, 1).equals("1e")) {
                                if (third.substring(0, 1).equals("2s")) {
                                    timewronged += Double.parseDouble(second.substring(2)) - (Double.parseDouble(first.substring(2)));
                                }
                            }

                        } else if (first.substring(0, 1).equals("2s")) {
                            if (second.substring(0, 1).equals("1s")) {
                                timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                            } else if (second.substring(0, 1).equals("2e")) {
                                if (third.substring(0, 1).equals("1s")) {
                                    timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                                }
                            }
                        } else if (first.substring(0, 1).equals("2e")) {
                            if (second.substring(0, 1).equals("1e")) {
                                timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                            } else if (second.substring(0, 1).equals("2s")) {
                                if (third.substring(0, 1).equals("1e")) {
                                    timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                                }
                            }
                        } else if (first.substring(0, 1).equals("1e")) {
                            if (second.substring(0, 1).equals("2e")) {
                                timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                            } else if (second.substring(0, 1).equals("1s")) {
                                if (third.substring(0, 1).equals("2e")) {
                                    timewronged += Double.parseDouble(second.substring(2)) - Double.parseDouble(first.substring(2));
                                }
                            }
                        }


                    }

                }
            }*/
      /*  ArrayList<String> totaltest=new ArrayList<String>();
        ArrayList<String> totalreal=new ArrayList<String>();
        for (ArrayList<String> timesz : realtimes){
            Log.d("actual",timesz.toString());
            totaltest.addAll(timesz);

        }
        for (ArrayList<String> timesz : times){
            Log.d("tested",timesz.toString());

        }

        for (int z= 0; z<realtimes.size();z++)
        {
           for (int l=0; l<realtimes.get(z).size();l++)
           {
               realtimes.get(z).set(l,z+"n"+realtimes.get(z).get(l));
           }
        }




        for (ArrayList<String> timesz : realtimes){
            Log.d("actual",timesz.toString());
            totalreal.addAll(timesz);

        }
        Collections.sort(totalreal, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if(Double.parseDouble(o1.substring(4))>=(Double.parseDouble(o2.substring(4))))
                {
                    return 1;
                }
                else{
                    return -1;
                }
            }
        });
        Collections.sort(totaltest, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if(Double.parseDouble(o1.substring(4))>=(Double.parseDouble(o2.substring(4))))
                {
                    return 1;
                }
                else{
                    return -1;
                }
            }
        });


      ArrayList<String> timesnew= stepconversion(times.get(0));
       ArrayList<String> realtimesnew= stepconversion(realtimes.get(0));


        Log.d("timesnew", timesnew.toString());
        Log.d("timesnew",times.get(0).toString());

        Log.d("realtimesnew",realtimesnew.subList(0,20).toString());
        realtimesnew.retainAll(timesnew);
        Log.d("realtimesnew",realtimesnew.subList(0,20).toString());
        Log.d("percent",Double.toString(millSecond/1000.0));*/
        //Log.d("percent",Double.toString(timeWronged(realtimesnew)/(millSecond/1000.0)));

        Description description= new Description();
        description.setText("Percentage spoken Pie Chart");
        Description description1= new Description();
        description1.setText("Percentage spoken Bar Chart");
        //barChart.setDescription(description1);
        pieChart.setDescription(description);
        BarDataSet barset=new BarDataSet(barEntries,"Bar");
        BarData barData= new BarData(barset);
        barData.setBarWidth(7f);
        PieDataSet set = new PieDataSet(entries, "Speaker names and colors");
        pieChart.setDrawSliceText(false);
        set.setSliceSpace(1);
        set.setValueTextSize(8);
        set.setColor(Color.WHITE);
        PieData data = new PieData(set);
        pieChart.setData(data);
       // barChart.setData(barData);
        barset.setColors(colorz);
        set.setColors(colorz);
        pieChart.invalidate(); // refresh
       // barChart.invalidate();

        pianoGraph.addView(piano_scale1);
        pianoGraph.addView(piano_scale);
        // mPlayerContainer = Parent view to add default player UI to.

        getSupportFragmentManager().beginTransaction()
                .add(R.id.soundWaveContainer, new CustomWaveformFragment())
                .commit();

    }


    public ArrayList<String> reimann(ArrayList<String> intervals)
    {
        ArrayList <String> summedList= new ArrayList<String>();
        for (double i =0.0; i< Double.parseDouble(intervals.get(0).substring(2));i+=.1)
        {
            summedList.add("n"+String.format("%1$,.1f", i));
        }
        for (int  i=0; i< intervals.size()-1;i++)
        {
            for (double j=roun(Double.parseDouble(intervals.get(i).substring(2)),1); j<Double.parseDouble(intervals.get(i+1).substring(2));j+=.1)
                    if (intervals.get(i).contains("s"))
                    {
                        summedList.add("y"+String.format("%1$,.1f", j));
                    }
                    else {
                        summedList.add("n"+String.format("%1$,.1f", j));
                    }
        }
        for (double i =roun(Double.parseDouble(intervals.get(0).substring(2)),1); i<=(double)mMeetingDurationInMilliseconds/1000.0;i+=.1)
        {
            summedList.add("n" + String.format("%1$,.1f", i));
        }
        return summedList;
    }




    private static double roun(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /*public double recursCompare(ArrayList<String> main, ArrayList<String> compare, int currentIndex)
    {
        Double wrong=0.0;
        if (main.isEmpty() && compare.isEmpty()) {
            return wrong;
        }
        else {
            if (Double.parseDouble(compare.get(0).substring(2)) <= Double.parseDouble(main.get(0).substring(2))) {
                if (Double.parseDouble(compare.get(1).substring(2)) <= Double.parseDouble(main.get(0).substring(2))) {
                    wrong += Double.parseDouble(compare.remove(1).substring(2)) - Double.parseDouble(compare.remove(0).substring(2));


                }
                else {
                    if (Double.parseDouble(compare.get(1).substring(2)) < Double.parseDouble(main.get(1).substring(2))) {
                        wrong += Double.parseDouble(main.get(0).substring(2)) - Double.parseDouble(compare.remove(0).substring(2));
                        compare.remove(0);
                    }
                    else {
                        wrong += Double.parseDouble(main.get(0).substring(2)) - Double.parseDouble(compare.remove(0).substring(2));
                        if (Double.parseDouble(main.get(2).substring(2)) < Double.parseDouble(compare.get(0).substring(2))) {
                            wrong += Double.parseDouble(main.get(2).substring(2)) - Double.parseDouble(main.remove(1).substring(2));
                            main.remove(0);
                            compare.add(0, main.get(0));
                        } else {
                            wrong += Double.parseDouble(compare.get(0).substring(2)) - Double.parseDouble(main.remove(1).substring(2));
                            main.remove(0);
                            compare.remove(0);

                        }

                    }

                }

            }
        }
    }*/

    public ArrayList<String> stepconversion( ArrayList<String> old)
    {ArrayList<String> newz= new ArrayList<String>();
        for (int i =0; i <old.size()-1; i+=2)
        {
            if (i==0) {
                for (Double j = 0.0; j < Math.floor(Double.parseDouble(old.get(i).substring(2))); j += .5) {
                    newz.add("n"+j);
                }
                for (Double j = Math.floor(Double.parseDouble(old.get(i).substring(2))); j < Math.ceil(Double.parseDouble(old.get(i + 1).substring(2))) + .5; j += 0.5) {
                newz.add("y"+j);
                }
            }
            else{
                    for (Double j = Math.floor(Double.parseDouble(old.get(i - 1).substring(2))); j < Math.ceil(Double.parseDouble(old.get(i).substring(2))) + .5; j += 0.5) {
                    newz.add("n"+j);
                    }


                for (Double j = Math.floor(Double.parseDouble(old.get(i).substring(2))); j < Math.ceil(Double.parseDouble(old.get(i + 1).substring(2))) + .5; j += 0.5) {
                    newz.add("y"+j);
                }
            }


            /*for (Double j=Math.floor(Double.parseDouble(old.get(i).substring(2)))-.5 ; j< Math.ceil(Double.parseDouble(old.get(i+1).substring(2)))+.5;j+=0.5)
            {
                newz.add(j);
            }*/

        }
        return newz;
    }

    public static class CustomWaveformFragment extends WaveformFragment {

        /**
         * Provide path to your audio file.
         *
         * @return
         */
        @Override
        protected String getFileName() {
            Log.d("obj", obj.getFilesDir().toString());
            Log.d("obj", obj.getCacheDir().toString());
            return obj.getFilesDir() + "/" + "ES2003a.Mix-Headset.wav";
            //return obj.getFilesDir()+"/"+  AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".wav";

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


    public void startTutorial(){
        FrameLayout soundwaveContainer= (FrameLayout)findViewById(R.id.soundWaveContainer);
        Button menuSummary= (Button)findViewById(R.id.menuSummary);
        PieChart pieChart= (PieChart) findViewById(R.id.chart);
        //BarChart barChart=(BarChart) findViewById(R.id.barChart);
        LinearLayout coord= (LinearLayout) findViewById(R.id.layout);
        /*Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(menuSummary, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(false, true),0)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("Theres a menu here too!")
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        ).show();*/
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
