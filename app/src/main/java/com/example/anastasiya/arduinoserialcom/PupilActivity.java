package com.example.anastasiya.arduinoserialcom;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.anastasiya.arduinoserialcom.helpers.FileLogger;
import com.example.anastasiya.arduinoserialcom.routers.PupilHttpRequestTask;
import com.example.anastasiya.arduinoserialcom.routers.IAsyncResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PupilActivity extends AppCompatActivity {
    private FileLogger fileLogger;
    TextView tvName;
    TextView tvSurname;
    TextView tvPatronymic;
    TextView tvClass;
    TextView tvSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pupil);
        fileLogger = FileLogger.getInstance(this.getApplicationContext(), this);

        tvName = (TextView) findViewById(R.id.tvName);
        tvSurname = (TextView) findViewById(R.id.tvSurname);
        tvPatronymic = (TextView) findViewById(R.id.tvPatronymic);
        tvClass = (TextView) findViewById(R.id.tvClass);
        tvSubject = (TextView) findViewById(R.id.tvTSubject);

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        fileLogger.writeToLogFile(uid);

        PupilHttpRequestTask asyncTask = new PupilHttpRequestTask(new IAsyncResponse() {
            @Override
            public void processFinish(Object output) {
                try {
                    tvName.setText("Name: " + ((JSONObject) output).getJSONObject("data").getString("name"));
                    tvSurname.setText("Surname: " + ((JSONObject) output).getJSONObject("data").getString("surname"));
                    tvPatronymic.setText("Patronymic: " + ((JSONObject) output).getJSONObject("data").getString("patronymic"));
                    tvClass.setText("Class: " + ((JSONObject) output).getJSONObject("data").getString("classId"));
                    tvSubject.setText("Subject: Math");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, this.getApplicationContext());
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getPupilByUid", uid);
    }
}