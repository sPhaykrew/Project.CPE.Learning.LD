package com.example.projectld.exercise3.nomal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectld.R;
import com.example.projectld.TTS;
import com.example.projectld.exercise3.segmentation;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


@SuppressLint("NewApi")
public class ex3_nomal_game extends AppCompatActivity {

    com.example.projectld.exercise3.segmentation segmentation;
    TTS tts;
    ImageView voice,next,back;

    int start = 0; //เช็คว่าเลากไปกี่ตัวแล้ว
    int finish = 0;

    ArrayList<String> wordset = new ArrayList<>();
    int count; // count array wordset for next and back
    SharedPreferences sharedPreferences; //เก็บค่า I ไม่ให้หาย
    Bundle arrayset; //รับค่า array จาก gridview ที่คิวรี่จากฐานข้อมูล

    int first = 0;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ex3_nomal_game);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        TextView Title = toolbar.findViewById(R.id.title);
        Title.setText("เแบบฝึกเรียงตัวอักษร");
        Title.setTextSize(16);

        ImageView back_toolbar = toolbar.findViewById(R.id.back);
        back_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        voice =  findViewById(R.id.voice_tts);
        next = findViewById(R.id.next);
        back = findViewById(R.id.back_this);
        arrayset = getIntent().getExtras();

        tts = new TTS(this);

        LoadInt();

        if (null != arrayset) {
            wordset = arrayset.getStringArrayList("wordset");
        }

        if (first == 0){
            count = arrayset.getInt("countarray");
            first++;
            checkFIrst(first);
        }

        /**
         * ตัดคำ
         */
        final String word = "วันนี้ฉันไปโรงเรียน";
        segmentation = new segmentation();
        ArrayList<String> sentence = segmentation.split(segmentation.Break(wordset.get(count))); //ระดับคำศัพท์
        //ArrayList<String> sentence = segmentation.substring(wordset.get(count)); //ระดับตัวอักษร

        /**
         * question set ex3_easy_game listeners
         */
        LinearLayout question = (LinearLayout) findViewById(R.id.question);
        for(int i=0 ; i < sentence.size() ; i++) {
            TextView valueQT = new TextView(this);
            valueQT.setText("__");
            valueQT.setId(i);
            valueQT.setTextSize(30);
            valueQT.setTag(sentence.get(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMarginStart(40);
            valueQT.setLayoutParams(params);
            valueQT.setOnDragListener(new ChoiceDragListener());
            valueQT.setGravity(Gravity.CENTER);
            question.addView(valueQT);

            start++; //เช็คว่าตอบคำถามครบหรือยัง
        }

        /**
         * answer views to ex3_easy_game
         */
        LinearLayout answer = (LinearLayout) findViewById(R.id.answer);
        int loop = sentence.size();
        ArrayList<String> sentenceRD = random(sentence,loop);

        for(int i=0 ; i < loop ; i++){
            TextView answerCH = new TextView(this);
            answerCH.setText(sentenceRD.get(i));
            answerCH.setTextSize(30);
            answerCH.setId(i);
            answerCH.setTag(answerCH.getText());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginStart(20);
            answerCH.setLayoutParams(params);
            answerCH.setOnTouchListener(new ChoiceTouchListener());
            answer.addView(answerCH);
        }

        /**
         * call TTS
         */
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(wordset.get(count));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if(count >= wordset.size()){
                    Toast.makeText(ex3_nomal_game.this,"ไม่พบคำถัดไป",Toast.LENGTH_SHORT).show();
                    count--;
                } else {
                    SaveInt(count);
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                if(count < 0){
                    Toast.makeText(ex3_nomal_game.this,"ไม่พบคำก่อนหน้า",Toast.LENGTH_SHORT).show();
                    count++;
                } else {
                    SaveInt(count);
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * ChoiceTouchListener will handle touch events on draggable views
     *
     */
    private final class ChoiceTouchListener implements OnTouchListener {
        @SuppressLint("NewApi")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                /*
                 * Drag details: we only need default behavior
                 * - clip data could be set to pass data as part of ex3_easy_game
                 * - shadow can be tailored
                 */
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //start dragging the item touched
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DragListener will handle dragged views being dropped on the drop area
     * - only the drop action will have processing added to it as we are not
     * - amending the default behavior for other parts of the ex3_easy_game process
     *
     */
    @SuppressLint("NewApi")
    private class ChoiceDragListener implements OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DROP:

                    //handle the dragged view being dropped over a drop view
                    View view = (View) event.getLocalState();
                    //view dragged item is being dropped on
                    TextView dropTarget = (TextView) v;
                    //view being dragged and dropped
                    TextView dropped = (TextView) view;

                    Log.i("droptarget", String.valueOf(dropTarget.getTag()));
                    Log.i("dropped", String.valueOf(dropped.getTag()));

                    //checking whether first character of dropTarget equals first character of dropped
                    if((dropTarget.getTag().equals(dropped.getTag())))
                    {
                        finish++; //เช็คว่าตอบคำถามครบหรือยัง
                        //stop displaying the view where it was before it was dragged
                        view.setVisibility(View.INVISIBLE);
                        //update the text in the target view to reflect the data being dropped
                        dropTarget.setText(dropped.getText().toString());
                        //make it bold to highlight the fact that an item has been dropped
                        dropTarget.setTypeface(Typeface.DEFAULT_BOLD);
                        //if an item has already been dropped here, there will be a tag
//                        Object tag = dropTarget.getTag();
                        //if there is already an item here, set it back visible in its original place

//                        if(tag!=null)
//                        {
//                            //the tag is the view id already dropped here
//                            int existingID = (Integer)tag;
//                            //set the original view visible again
//                            findViewById(existingID).setVisibility(View.VISIBLE);
//                        }

                        //set the tag in the target view being dropped on - to the ID of the view being dropped
                        dropTarget.setTag(dropped.getId());
                        //remove setOnDragListener by setting OnDragListener to null, so that no further ex3_easy_game & dropping on this TextView can be done
                        dropTarget.setOnDragListener(null);

                        if (finish == start){
                            Toast.makeText(ex3_nomal_game.this,"เสร็จสิ้น",Toast.LENGTH_LONG).show();

                            count++;
                            if(count >= wordset.size()){
                                Toast.makeText(ex3_nomal_game.this,"ไม่พบคำถัดไป",Toast.LENGTH_SHORT).show();
                                count--;
                            } else {
                                SaveInt(count);
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        }
                    }
                    else
                        //displays message if first character of dropTarget is not equal to first character of dropped
                        Toast.makeText(ex3_nomal_game.this,"ไม่ถูกต้อง", Toast.LENGTH_LONG).show();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //no action necessary
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public void reset(View view)
    {
//        option1.setVisibility(TextView.VISIBLE);
//        option2.setVisibility(TextView.VISIBLE);
//        option3.setVisibility(TextView.VISIBLE);
//
//        choice1.setText("A for ");
//        choice2.setText("O for ");
//        choice3.setText("B for ");
//
//        choice1.setTag(null);
//        choice2.setTag(null);
//        choice3.setTag(null);
//
//        choice1.setTypeface(Typeface.DEFAULT);
//        choice2.setTypeface(Typeface.DEFAULT);
//        choice3.setTypeface(Typeface.DEFAULT);
//
//        choice1.setOnDragListener(new ChoiceDragListener());
//        choice2.setOnDragListener(new ChoiceDragListener());
//        choice3.setOnDragListener(new ChoiceDragListener());
    }

    public ArrayList<String> random (ArrayList<String> result, int i){
        Random rd = new Random();
        ArrayList<String> list = new ArrayList<>();
        for(int j=0;j<i;j++){
            String test = result.get(rd.nextInt(result.size()));
            list.add(test);
            result.remove(test);
        }
        return list;
    }

    public void SaveInt(int value){ //เซฟค่า count
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("key", value);
        editor.commit();
    }
    public void LoadInt(){ // โหลดค่า count
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        count = sharedPreferences.getInt("key", 0);
        first = sharedPreferences.getInt("first",0);
    }

    public void checkFIrst(int first){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("first",first);
        editor.commit();
    }

}