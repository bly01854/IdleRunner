package com.bly01854.idlerunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bly01854.idlerunner.database.AppDatabase;
import com.bly01854.idlerunner.entity.User;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;
/*
Bailey Henson
Term Project
CS 475
Working name: Idle Runner
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor countSensor;

    TextView tv_steps;
    boolean running = false;

    private CreateUserTask createUser;
    private RetrieveTask getSteps;
    private UpdateTask updateSteps;

    User user;



    // Upgrade Views

    TextView tv_shoes;
    TextView tv_drink;
    TextView tv_joggers;
    TextView tv_breeze;
    Button bt_shoes;
    Button bt_drink;
    Button bt_joggers;
    Button bt_breeze;

    // Energy Drink Variables
    Button bt_activateEnergy;
    boolean energyDrinkActive;
    int activeSteps;
    int cooldownSteps;

    // Formatting
    DecimalFormat format = new DecimalFormat("0.#");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUser = new CreateUserTask();
        createUser.execute();
        setContentView(R.layout.activity_main);

        tv_steps = findViewById(R.id.tv_steps);

        // Initialize Upgrade views
        tv_shoes = findViewById(R.id.runningShoes);
        tv_drink = findViewById(R.id.energyDrink);
        tv_joggers = findViewById(R.id.joggers);
        tv_breeze = findViewById(R.id.breeze);
        bt_shoes = findViewById(R.id.running_shoes_button);
        bt_drink = findViewById(R.id.energy_drink_button);
        bt_joggers = findViewById(R.id.joggers_button);
        bt_breeze = findViewById(R.id.breeze_button);

        bt_activateEnergy = findViewById(R.id.activate_energy);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }


    // Start animation
    AnimationDrawable runningAnimation;

    @Override
    protected void onStart(){
        super.onStart();
        // Animation must be placed in onStart
        ImageView runningMan = findViewById(R.id.ivRunningMan);
        runningMan.setBackgroundResource(R.drawable.running);
        runningAnimation = (AnimationDrawable) runningMan.getBackground();
        runningAnimation.start();
    }

    // Initialize counting
    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Intent intent = new Intent(this, RunningService.class);
        this.stopService(intent);
        // Receive service steps
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("message"));
        getSteps = new RetrieveTask();
        getSteps.execute();

        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
        // Unregister activity listener
        sensorManager.unregisterListener(this, countSensor);
        // Start service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        updateSteps = new UpdateTask();
        updateSteps.execute();
        Intent intent = new Intent(this, RunningService.class);
        intent.putExtra("energyActive", energyDrinkActive);
        intent.putExtra("activeSteps", activeSteps);
        intent.putExtra("cooldownSteps", cooldownSteps);
        intent.putExtra("energyDrinks", user.getEnergyDrink());
        intent.putExtra("shoes", user.getRunningShoes());
        intent.putExtra("joggers", user.getJoggers());
        intent.putExtra("breezes", user.getCoolBreeze());
        this.startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, RunningService.class);
        this.stopService(intent);
        // Receive service steps
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("message"));
        updateSteps = new UpdateTask();
        updateSteps.execute();
    }


    // Calculate steps
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (running) {
            stepCalculator();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    // To retrieve user steps
    public class RetrieveTask extends AsyncTask<Void, Void, Void> {

        RetrieveTask(){}

        @Override
        protected Void doInBackground(Void... voids) {
            retrieveSteps(AppDatabase.getAppDatabase(MainActivity.this));
            System.out.println("Processing background");
            return null;
        }

        private void retrieveSteps(AppDatabase db) {
            user = db.userDao().getUser();
            System.out.println("Value of steps: " + user.getSteps());
        }

        @Override
        protected void onPostExecute(Void result){
            tv_steps.setText(String.valueOf(user.getSteps()));
            // Update Views
            tv_shoes.setText("Running Shoes  x" + user.getRunningShoes());
            bt_shoes.setText(format.format(((user.getRunningShoes() + 1) * 30) * (double)(1 + (user.getRunningShoes() / 5))) + "");
            tv_drink.setText("Energy Drink  x" + user.getEnergyDrink());
            bt_drink.setText(format.format(((user.getEnergyDrink() + 1) * 150) * (double)(1 + (user.getEnergyDrink() / 4))) + "");
            tv_joggers.setText("Joggers  x" + user.getJoggers());
            bt_joggers.setText(format.format(((user.getJoggers() + 1) * 300) * (double)(1 + (user.getJoggers() / 4))) + "");
            tv_breeze.setText("Cool Breeze  x" + user.getCoolBreeze());
            bt_breeze.setText(format.format(((user.getCoolBreeze() + 1) * 500) * (double)(1 + (user.getCoolBreeze() / 3))) + "");

            if (user.getEnergyDrink() < 1)
                bt_activateEnergy.setVisibility(View.INVISIBLE);
            else {
                bt_activateEnergy.setVisibility(View.VISIBLE);
            }
            System.out.println("Task done");
        }


    }

    // To store user steps
    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        UpdateTask(){}

        @Override
        protected Void doInBackground(Void... voids) {
            updateSteps(AppDatabase.getAppDatabase(MainActivity.this));
            return null;
        }

        private void updateSteps(AppDatabase db) {
            db.userDao().updateUser(user);
        }

    }

    // To create initial user object
    public class CreateUserTask extends AsyncTask<Void, Void, Void>{

        CreateUserTask() {}

        @Override
        protected Void doInBackground(Void... voids) {
            User user = null;
            user = existTest(AppDatabase.getAppDatabase(MainActivity.this));
            //deleteUser(AppDatabase.getAppDatabase(MainActivity.this), user);
            if (user == null) {
                createUser(AppDatabase.getAppDatabase(MainActivity.this));
            }
            return null;
        }

        private void createUser(AppDatabase db) {
            User user = new User(1, 0, 0, 0, 0, 0);
            db.userDao().addUser(user);
        }

        // Check if user exists
        private User existTest(AppDatabase db) {
            User user = null;
            user = db.userDao().getUser();
            return user;
        }
        // Delete user data if necessary
        private void deleteUser(AppDatabase db, User user) {
            db.userDao().deleteUser(user);
        }
    }

    // To receive information from service
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int serviceSteps = intent.getIntExtra("steps", 0);
            cooldownSteps += intent.getIntExtra("cooldownSteps", 0);
            activeSteps += intent.getIntExtra("activeSteps", 0);
            if (cooldownSteps > 800)
                bt_activateEnergy.setEnabled(true);

            user.setSteps(user.getSteps() + serviceSteps);
            updateSteps = new UpdateTask();
            updateSteps.execute();
            System.out.println("Broadcast sent");
        }
    };

    // UPGRADE STEP CALCULATION METHOD
    public void stepCalculator () {
        int shoes = user.getRunningShoes() + 1;
        int joggers = ((user.getJoggers() + 1) * 10) / 10;
        int breezes = (user.getCoolBreeze()) * 20;
        int randomNum = ThreadLocalRandom.current().nextInt(0, 101);

        activeSteps++;
        cooldownSteps++;

        int step = (1 * shoes) * joggers;


        if (energyDrinkActive == true) {
            step = step * 2;
        }

        if (randomNum <= 5 && user.getCoolBreeze()>0) {
            step = step * breezes;
        }

        user.setSteps(user.getSteps() + step);

        tv_steps.setText(String.valueOf(user.getSteps()));
    }


    // Energy drink activation method
    public void activateEnergyDrink (View view) {
        activatedEnergy();
    }

    // To run while energy drink buff is active
    public void activatedEnergy() {
        bt_activateEnergy.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                activeSteps = 0;
                System.out.println("Running active!");

                while (activeSteps < user.getEnergyDrink() * 50) {

                }
                System.out.println("Running inactive!");
                energyDrinkActive = false;
                cooldownEnergy();
            }
        }).start();

    }

    // To run while energy drink active is on cooldown for 800 steps
    public void cooldownEnergy () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                cooldownSteps = 0;
                while (cooldownSteps <800) {

                }

                bt_activateEnergy.setEnabled(true);
            }
        }).start();

    }


    // ***UPGRADES ONCLICK METHODS***

    public void upgradeShoes (View view) {
        if(user.getSteps() >= ((user.getRunningShoes() + 1) * 30) * (double)(1 + (user.getRunningShoes() / 5))){
            user.setSteps((int)(user.getSteps() - ((user.getRunningShoes() + 1) * 30) * (double)(1 + (user.getRunningShoes() / 5))));
            user.setRunningShoes(user.getRunningShoes() + 1);
            tv_steps.setText(String.valueOf(user.getSteps()));
            tv_shoes.setText("Running Shoes  x" + user.getRunningShoes());
            bt_shoes.setText(format.format(((user.getRunningShoes() + 1) * 30) * (double)(1 + (user.getRunningShoes() / 5))) + "");
        }

    }
    public void upgradeEnergyDrink (View view) {
        if(user.getSteps() >= ((user.getEnergyDrink() + 1) * 150) * (double)(1 + (user.getEnergyDrink() / 4))){
            user.setSteps((int)(user.getSteps() - ((user.getEnergyDrink() + 1) * 150) * (double)(1 + (user.getEnergyDrink() / 4))));
            user.setEnergyDrink(user.getEnergyDrink() + 1);
            tv_steps.setText(String.valueOf(user.getSteps()));
            tv_drink.setText("Energy Drink  x" + user.getEnergyDrink());
            bt_drink.setText(format.format(((user.getEnergyDrink() + 1) * 150) * (double)(1 + (user.getEnergyDrink() / 4))) + "");
            bt_activateEnergy.setVisibility(View.VISIBLE);
            bt_activateEnergy.setEnabled(true);
        }
    }
    public void upgradeJoggers (View view) {
        if(user.getSteps() >= ((user.getJoggers() + 1) * 300) * (double)(1 + (user.getJoggers() / 4))){
            user.setSteps((int)(user.getSteps() - ((user.getJoggers() + 1) * 300) * (double)(1 + (user.getJoggers() / 4))));
            user.setJoggers(user.getJoggers() + 1);
            tv_steps.setText(String.valueOf(user.getSteps()));
            tv_joggers.setText("Joggers  x" + user.getJoggers());
            bt_joggers.setText(format.format(((user.getJoggers() + 1) * 300) * (double)(1 + (user.getJoggers() / 4))) + "");
        }
    }
    public void upgradeCoolBreeze (View view) {
        if(user.getSteps() >= ((user.getCoolBreeze() + 1) * 500) * (double)(1 + (user.getCoolBreeze() / 3))){
            user.setSteps((int)(user.getSteps() - ((user.getCoolBreeze() + 1) * 500) * (double)(1 + (user.getCoolBreeze() / 3))));
            user.setCoolBreeze(user.getCoolBreeze() + 1);
            tv_steps.setText(String.valueOf(user.getSteps()));
            tv_breeze.setText("Cool Breeze  x" + user.getCoolBreeze());
            bt_breeze.setText(format.format(((user.getCoolBreeze() + 1) * 500) * (double)(1 + (user.getCoolBreeze() / 3))) + "");
        }
    }


    /*
    public void registerSteps() {
        // Step detector
        sensorManager.registerListener(new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                steps++;
                tv_steps.setText(String.valueOf(steps));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_GAME);
    }
    */
}
