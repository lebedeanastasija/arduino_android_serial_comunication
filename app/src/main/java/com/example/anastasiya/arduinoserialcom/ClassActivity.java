package com.example.anastasiya.arduinoserialcom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.anastasiya.arduinoserialcom.helpers.FileLogger;
import com.example.anastasiya.arduinoserialcom.routers.IAsyncResponse;
import com.example.anastasiya.arduinoserialcom.routers.PupilHttpRequestTask;
import com.example.anastasiya.arduinoserialcom.routers.TeacherHttpRequestTask;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] pupils = new String[0];
    private String[] avatarIds = new String[0];
    private Context context;
    private Activity activity;
    private FileLogger fileLogger;
    private String teacher_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        Intent intent = getIntent();
        teacher_uid = intent.getStringExtra("teacher_uid");
        context = this.getApplicationContext();
        activity = this;

        fileLogger = FileLogger.getInstance(context, activity);
        mRecyclerView = (RecyclerView) findViewById(R.id.rvClass);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getPupils();
    }

    @Override
    public void onStart() {
        super.onStart();
        getPupils();
    }

    private void getPupils() {
        TeacherHttpRequestTask asyncTask = new TeacherHttpRequestTask(new IAsyncResponse() {
            @Override
            public void processFinish(Object output) {
                try {
                    final String classId = ((JSONObject) output).getJSONObject("data").getJSONObject("class").getString("id");
                    PupilHttpRequestTask asyncTask2 = new PupilHttpRequestTask(new IAsyncResponse() {
                        @Override
                        public void processFinish(Object output1) {
                            try {
                                JSONArray jsonArray = ((JSONObject) output1).getJSONObject("data").getJSONArray("pupils");
                                pupils = new String[jsonArray.length()];
                                avatarIds = new String[jsonArray.length()];
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    pupils[i] = jsonArray.getJSONObject(i).getString("surname") + " " +
                                            jsonArray.getJSONObject(i).getString("name") + " " +
                                            jsonArray.getJSONObject(i).getString("patronymic");
                                    if(!jsonArray.getJSONObject(i).getString("avatarId").equals("null")) {
                                        avatarIds[i] = jsonArray.getJSONObject(i).getString("avatarId");
                                    } else {
                                        avatarIds[i] = "1";
                                    }
                                }
                                mAdapter = new PupilsListAdapter(pupils, avatarIds, context, activity);
                                mRecyclerView.setAdapter(mAdapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, context);
                    asyncTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getPupilsByClassId", classId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, context, activity);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getTeacherByUID", teacher_uid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.class_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_add_pupil:
                Intent add_pupil_intent = new Intent(this, AddPupilActivity.class);
                add_pupil_intent.putExtra("teacher_uid", teacher_uid);
                startActivity(add_pupil_intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
