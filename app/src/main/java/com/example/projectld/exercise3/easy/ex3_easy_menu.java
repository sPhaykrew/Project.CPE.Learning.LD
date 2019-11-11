package com.example.projectld.exercise3.easy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projectld.DatabaseHelper;
import com.example.projectld.exercise3.GridviewAdapter;
import com.example.projectld.R;

import java.util.ArrayList;

public class ex3_easy_menu extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ArrayList<String> words,st;
    GridView gridView,gridView_admin;
    GridviewAdapter gridviewAdapter,gridviewAdapter_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ex3_easy_menu);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView Title = toolbar.findViewById(R.id.title);
        Title.setText("เลือกคำศัพท์");
        Title.setTextSize(20);

        ImageView back = toolbar.findViewById(R.id.back);
        ImageView show_menu = toolbar.findViewById(R.id.show_menu);
        show_menu.setVisibility(View.GONE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dbHelper = new DatabaseHelper(getApplicationContext());

        words = dbHelper.queryword("word");
        gridView = findViewById(R.id.gridView);

        ArrayList<String> numbers = new ArrayList<>();
        for (int i=1;i<=words.size();i++){
            numbers.add(String.valueOf(i));
        }

        //create Girdview
        gridviewAdapter = new GridviewAdapter(numbers,this,"easy",R.drawable.radius_button_color1,words);
        gridView.setAdapter(gridviewAdapter);

        //gridview admin setting
        gridView_admin = findViewById(R.id.gridView_admin);
        st = dbHelper.GetGroupname("Setting_ex3_easy","st_ex3_easy_id");
        gridviewAdapter_admin = new GridviewAdapter(st,this,"ex3_easy_game_st",R.drawable.radius_button_color1,null);
        gridView_admin.setAdapter(gridviewAdapter_admin);

        TextView textHide = findViewById(R.id.noEX);

        if (st.size() != 0){
            textHide.setVisibility(View.INVISIBLE);
        }
    }
}
