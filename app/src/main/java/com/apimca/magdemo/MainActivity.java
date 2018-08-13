package com.apimca.magdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnStartDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartDemo = (Button) findViewById(R.id.btnStartDemo);

        // Opening the DemoActivity on click
        btnStartDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDemo();
            }
        });
    }

    public void startDemo() {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }

}
