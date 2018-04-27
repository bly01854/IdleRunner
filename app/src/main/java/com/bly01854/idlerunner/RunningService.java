package com.bly01854.idlerunner;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bly01854.idlerunner.database.AppDatabase;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by bly01854 on 4/13/2018.
 *
 * TODO
 * Fix multiple broadcast received
 */

public class RunningService extends Service implements SensorEventListener{


    SensorManager sensorManager;
    Sensor countSensor;
    int steps;

    int Ishoes;
    int Idrinks;
    int Ijoggers;
    int Ibreezes;

    int activeSteps;
    int cooldownSteps;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int addStep = calculateSteps();
        steps += addStep;
        System.out.println("step logged");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        steps = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        System.out.println("service started");

        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Ishoes = intent.getIntExtra("shoes", 0);
        Idrinks = intent.getIntExtra("drinks", 0);
        Ijoggers = intent.getIntExtra("joggers", 0);
        Ibreezes = intent.getIntExtra("breezes", 0);

        activeSteps = intent.getIntExtra("activeSteps", 0);
        cooldownSteps = intent.getIntExtra("cooldownSteps", 0);



        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "Steps taken: " + steps, Toast.LENGTH_LONG);
        sendBroadcast();
        System.out.println("Stopping service");
        sensorManager.unregisterListener(this, countSensor);
    }

    private int calculateSteps() {
        int shoes = Ishoes + 1;
        int joggers = ((Ijoggers + 1) * 10) / 10;
        int breezes = (Ibreezes + 1) * 20;
        int randomNum = ThreadLocalRandom.current().nextInt(0, 101);

        activeSteps++;
        cooldownSteps++;

        int step = (1 * shoes) * joggers;


        if (activeSteps <= Idrinks * 50) {
            step = step * 2;
        }

        if (randomNum <= 5) {
            step = step * breezes;
        }

        return step;
    }

    private void sendBroadcast () {
        Intent intent = new Intent ("message");
        intent.putExtra("steps", steps);
        intent.putExtra("cooldownSteps", cooldownSteps);
        intent.putExtra("activeSteps", activeSteps);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
