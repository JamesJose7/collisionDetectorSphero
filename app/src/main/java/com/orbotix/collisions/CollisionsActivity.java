package com.orbotix.collisions;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.Random;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.Robot;
import orbotix.robot.sensor.LocatorData;
import orbotix.sphero.CollisionListener;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.LocatorListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class CollisionsActivity extends Activity {

    private TextView mPowerXValueLabel;
    private TextView mPowerYValueLabel;
    private TextView mVelocityXLabel;
    private TextView mVelocityYLabel;

    protected Handler mHandler = new Handler();

    private int mCurrentDirection;
    private float mVelocity;
    private float mVelocityX;
    private float mVelocityY;
    private boolean isAlwaysMoving;

    private EditText velocityInput;
    private Button mAlwaysMovingButton;

    private Sphero mRobot;

    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.back_layout).requestFocus();

        //Decides if the always move feature is activated
        isAlwaysMoving = false;

        mAlwaysMovingButton = (Button) findViewById(R.id.isStillButton);

        mAlwaysMovingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAlwaysMoving) {
                    checkSpheroMovement();
                    isAlwaysMoving = true;
                    Toast.makeText(CollisionsActivity.this, "Always moving activated", Toast.LENGTH_LONG).show();

                    mAlwaysMovingButton.setBackgroundColor(Color.parseColor("#4000ff00"));
                }
                else if (isAlwaysMoving) {
                    isAlwaysMoving = false;
                    Toast.makeText(CollisionsActivity.this, "Always moving deactivated", Toast.LENGTH_LONG).show();

                    mAlwaysMovingButton.setBackgroundColor(Color.parseColor("#40ff0000"));
                }
            }
        });

        velocityInput = (EditText) findViewById(R.id.velocityInput);

        // initialize value labels
        mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
        mPowerXValueLabel.setText("0.0");

        mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
        mPowerYValueLabel.setText("0.0");

        mVelocityXLabel = (TextView) findViewById(R.id.velocityX_value);
        mVelocityXLabel.setText("0 cm/s" );

        mVelocityYLabel = (TextView) findViewById(R.id.velocityY_value);
        mVelocityYLabel.setText("0 cm/s");

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                mRobot = (Sphero) robot;
                mRobot.getCollisionControl().addCollisionListener(mCollisionListener);
                mRobot.getCollisionControl().startDetection(45, 45, 100, 100, 100);

                mRobot.setColor(0, 0, 255);

                mRobot.getSensorControl().addLocatorListener(mLocatorListener);
                mRobot.getSensorControl().setRate(5);
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        });
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.startDiscovery();
    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        if (mRobot != null) {
            mRobot.getCollisionControl().stopDetection();
            // Remove async data listener
            mRobot.getCollisionControl().removeCollisionListener(mCollisionListener);
            // Disconnect Robot properly
            mRobot.disconnect();
        }
    }

    /** Sphero Locator is a firmware feature that provides real-time position and velocity information about the robot. */
    private LocatorListener mLocatorListener = new LocatorListener() {
        @Override
        public void onLocatorChanged(LocatorData locatorData) {

            //Save velocity on variables to check if the Sphero is moving
            mVelocityX = locatorData.getVelocityX();
            mVelocityY = locatorData.getVelocityY();

            mVelocityXLabel = (TextView) findViewById(R.id.velocityX_value);
            mVelocityXLabel.setText(mVelocityX + " cm/s");

            mVelocityYLabel = (TextView) findViewById(R.id.velocityY_value);
            mVelocityYLabel.setText(mVelocityY + " cm/s");

        }
    };

    /** Sphero collision detection is a firmware feature that generates a collision async message when an impact is detected. */
    private final CollisionListener mCollisionListener = new CollisionListener() {
        public void collisionDetected(CollisionDetectedAsyncData collisionData) {

            // Update the UI with the collision data
            CollisionPower power = collisionData.getImpactPower();
            mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
            mPowerXValueLabel.setText("" + power.x);

            mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
            mPowerYValueLabel.setText("" + power.y);

            //Check if Sphero hit a wall
            if (power.x > 60 || power.y > 60) {
                changeDirection();
            }
        }
    };

    /** Every collision it randomly chooses one of three opposite directions from the one it was currently heading */
    private void changeDirection() {

        Random randomGenerator = new Random();
        int randomNumber;

        mRobot.stop();

        if (mCurrentDirection == 0) {
            //isUp
            float[] directions = {90f, 180f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], mVelocity);
            mCurrentDirection = (int) directions[randomNumber];

            //Color for testing
            mRobot.setColor(236, 206, 31);
        } else if(mCurrentDirection == 180) {
            //isDown
            float[] directions = {0f, 90f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], mVelocity);
            mCurrentDirection = (int) directions[randomNumber];

            //Color for testing
            mRobot.setColor(236, 206, 31);
        } else if (mCurrentDirection == 90) {
            //isRight
            float[] directions = {0f, 180f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], mVelocity);
            mCurrentDirection = (int) directions[randomNumber];

            //Color for testing
            mRobot.setColor(236, 206, 31);
        } else if (mCurrentDirection == 270) {
            //isLeft
            float[] directions = {0f, 90f, 180f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], mVelocity);
            mCurrentDirection = (int) directions[randomNumber];

            //Color for testing
            mRobot.setColor(236, 206, 31);
        }
    }

    /** Check every 2 seconds if the Sphero is moving */
    private void checkSpheroMovement() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeSpheroDirection();
                checkSpheroMovement();
            }
        }, 2000);

    }

    /** Changes Sphero's direction if its not moving */
    private void changeSpheroDirection() {

        //Wait 2 seconds and change ball direction if it doesn't move
        mRobot.setColor(255, 0, 0);


        if (mVelocityX <= 2 && mVelocityY <= 2 && isAlwaysMoving) {

            float[] directions = {0f, 90f, 180f, 270f};

            Random randomGenerator = new Random();
            int randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.setColor(0, 255, 0);
            mRobot.drive(directions[randomNumber], mVelocity);
            mCurrentDirection = (int) directions[randomNumber];
        }


    }

    public void onClick(View v) {
        switch (v.getId()) {

            //Changes Sphero's velocity
            case R.id.velocityButton:
                int velocityPercentage = Integer.parseInt(velocityInput.getText().toString());
                mVelocity = (float) velocityPercentage / 100;
                Toast.makeText(this, "velocity set to " + velocityPercentage + "%", Toast.LENGTH_LONG).show();
                break;


            default:
                mRobot.stop();
                break;
        }
    }
}