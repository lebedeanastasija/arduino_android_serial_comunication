package com.example.anastasiya.arduinoserialcom.routers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.example.anastasiya.arduinoserialcom.helpers.FileLogger;
import com.example.anastasiya.arduinoserialcom.services.TeacherService;

public class TeacherHttpRequestTask extends AsyncTask<String, Object, Object>{
    private TeacherService teacherService;
    private FileLogger fileLogger;

    IAsyncResponse delegate = null;


    public TeacherHttpRequestTask(IAsyncResponse delegate, Context context, Activity activity) {
        this.delegate = delegate;
        teacherService = TeacherService.getInstance(context, activity);
        fileLogger = FileLogger.getInstance(context, activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(String... params) {
        Object response = null;
        String uid = null;
        String methodName = params[0];
        try {
            switch (methodName) {
                case "getLessonByUID":
                    uid = params[1];
                    response = teacherService.getLessonByUid(uid);
                    break;
                case "getTeacherByUID":
                    uid = params[1];
                    response = teacherService.getTeacherByUid(uid);
                    break;
            }

        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        fileLogger.writeToLogFile("Teacher http request task onPostExecute.");
        delegate.processFinish(result);
    }
}
