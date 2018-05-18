package com.loyola.talktracer.activities;

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
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.venmo.view.TooltipView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.sephiroth.android.library.tooltip.Tooltip;

import static java.security.AccessController.getContext;

/**
 * Shows a bar chart of speakers in decreasing order
 */
public class SummaryActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "SummaryActivity";
    private Boolean tutorialMode=false;
    private DrawerLayout mDrawerLayout;
    private Button buton;
    private long mMeetingDurationInMilliseconds;
    private ArrayList<Speaker> mSpeakers;
    private ArrayList<Tooltip.TooltipView> tipviews;


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
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.a);
        tutorialMode= getIntent().getBooleanExtra("TUTORIAL",false);
        tipviews= new ArrayList<Tooltip.TooltipView>();
        //FloatingActionButton closeTutorial= (FloatingActionButton) findViewById(R.id.closeTutorial1);
        if (tutorialMode==true){
            //closeTutorial.setVisibility(View.VISIBLE);
            startTutorial();
        }
        Log.d("Tutorial", Boolean.toString(tutorialMode));
        Button menuSummary= (Button) findViewById(R.id.menuSummary);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerSummary_layout);
        buton=(Button)findViewById(R.id.menuSummary);
        buton.setOnClickListener(SummaryActivity.this);
        String isegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".i.seg";
        String checkgmmsinit = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".init.gmms";
        String checkgmms = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".g.gmms";
        String ssegPathFileName=getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".s.seg";
        //String pmssegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".pms.seg";
        //String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String lsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String hsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".h.seg";
        String dsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".d.seg";
        String adjsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".adj.seg";
        String gsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".g.seg";
        //String fsegPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".final.seg";
        String rawPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        FileInputStream in;
        FileInputStream ginitgmmstest;
        FileInputStream ggmmstest;
        FileInputStream intest;
        FileInputStream stest;
        FileInputStream ltest;
        FileInputStream htest;
        FileInputStream gtest;
        FileInputStream dtest;
        FileInputStream adjtest;
       // FileInputStream finaltest;

        long rawFileSize = new File(rawPathFileName).length();
        mMeetingDurationInMilliseconds = rawFileSize
                * 1000
                / (AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ * 2);
        /*try{
            ginitgmmstest=new FileInputStream(checkgmmsinit);
            ggmmstest=new FileInputStream(checkgmms);
            Log.d("hey","yay");

        }
        catch (Exception e){
            Log.d("hey","bad");
        }*/
        try {
            Log.i(TAG, "File size: " + rawFileSize);
            in = new FileInputStream(hsegPathFileName);
            intest = new FileInputStream(isegPathFileName);
            stest=new FileInputStream(ssegPathFileName);
            ltest=new FileInputStream(lsegPathFileName);
            htest=new FileInputStream(hsegPathFileName);
           /* dtest=new FileInputStream(dsegPathFileName);
            gtest=new FileInputStream(gsegPathFileName);
            adjtest=new FileInputStream(adjsegPathFileName);*/
            //finaltest=new FileInputStream(fsegPathFileName);
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
            /*Log.d("hey","d test fif");
            speker.logSegStream(dtest);
            Log.d("hey","adj test six");
            speker.logSegStream(adjtest);
            Log.d("hey","g test seven");
            speker.logSegStream(gtest);
            Log.d("hey","final test eight");*/
           // speker.logSegStream(finaltest);
            mSpeakers = new SpeakersBuilder().parseSegStream(in).build();
            Log.i(TAG, "sp.size(): " + mSpeakers.size());
        } catch (IOException e) {
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + hsegPathFileName);
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
        BarChart barChart = (BarChart) findViewById(R.id.barChart);
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
            Log.d("hey", "start times are");
            for (int j=0;j<speaker.getStartTimes().size();j++)
            {   Log.d("hey", Long.toString(speaker.getStartTimes().get(j)));
                Log.d("hey", "Duration"+Long.toString(speaker.getDurations().get(j)));

                int pianobarwidth= (int) Math.floor(percentbar2* speaker.getDurations().get(j)/10000.0);

                if (speaker.getStartTimes().get(j)<=0.0)
                {
                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText("");
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

                    tempbar.addView(pianoViewBar1);
                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText("");
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

                    tempbar.addView(pianoViewBar1);
                    TextView pianoViewBar=new TextView(this);
                    pianoViewBar.setText("");
                    pianoViewBar.setWidth(pianobarwidth);
                    pianoViewBar.setBackgroundColor(speaker.getColor());
                    tempbar.addView(pianoViewBar);
                }




            }

            pianoGraph.addView(tempbar);
            Log.d("speking","int percent" +Integer.toString(speakerPercentint(speaker.getTotalDuration(),mMeetingDurationInMilliseconds)));
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
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis=barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        YAxis bottomYAxis=barChart.getAxisRight();
        bottomYAxis.setEnabled(false);
        bottomYAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(mSpeakers.size()*10);
        xAxis.setAxisMinimum(-10f);
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(false);
        Description description= new Description();
        description.setText("Percentage spoken Pie Chart");
        Description description1= new Description();
        description1.setText("Percentage spoken Bar Chart");
        barChart.setDescription(description1);
        pieChart.setDescription(description);
        BarDataSet barset=new BarDataSet(barEntries,"Bar");
        BarData barData= new BarData(barset);
        barData.setBarWidth(7f);
        PieDataSet set = new PieDataSet(entries, "Speaker names and colors");
        set.setSliceSpace(1);
        set.setValueTextSize(8);
        PieData data = new PieData(set);
        pieChart.setData(data);
        barChart.setData(barData);
        barset.setColors(colorz);
        set.setColors(colorz);
        pieChart.invalidate(); // refresh
        barChart.invalidate();

        pianoGraph.addView(piano_scale1);
        pianoGraph.addView(piano_scale);

    }


    public void startTutorial(){
        Button menuSummary= (Button)findViewById(R.id.menuSummary);
        PieChart pieChart= (PieChart) findViewById(R.id.chart);
        BarChart barChart=(BarChart) findViewById(R.id.barChart);
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
                        .anchor(barChart, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),6000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a bar chart of the percentage spoken")
                        .showDelay(3000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView1.show();
        Tooltip.TooltipView tooltipView2 = Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(pieChart, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),9000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a pie chart of the percentage spoken")
                        .activateDelay(0)
                        .showDelay(6000)
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(true).build()
        );
        tooltipView2.show();

       Tooltip.TooltipView tooltipView3= Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(grid, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(false, false)
                                .outsidePolicy(false,false),12000)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .text("This here is a piano roll of time spoken per person spoken")
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
