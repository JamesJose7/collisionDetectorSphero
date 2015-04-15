package com.orbotix.collisions;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Random;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.Robot;
import orbotix.robot.sensor.Acceleration;
import orbotix.sphero.CollisionListener;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class CollisionsActivity extends Activity {

    private TextView mAccelXValueLabel;
    private TextView mAccelYValueLabel;
    private TextView mAccelZValueLabel;
    private CheckBox mXAxisCheckBox;
    private CheckBox mYAxisCheckBox;
    private TextView mPowerXValueLabel;
    private TextView mPowerYValueLabel;
    private TextView mSpeedValueLabel;
    private TextView mTimestampLabel;

    private boolean isUp;
    private boolean isLeft;
    private boolean isDown;
    private boolean isRight;

    private int mCurrentDirection;

    private Sphero mRobot;

    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.back_layout).requestFocus();

        // initialize value labels
        mAccelXValueLabel = (TextView) findViewById(R.id.accel_x_value);
        mAccelXValueLabel.setText("0.0");

        mAccelYValueLabel = (TextView) findViewById(R.id.accel_y_value);
        mAccelYValueLabel.setText("0.0");

        mAccelZValueLabel = (TextView) findViewById(R.id.accel_z_value);
        mAccelZValueLabel.setText("0.0");

        mXAxisCheckBox = (CheckBox) findViewById(R.id.axis_x_checkbox);
        mXAxisCheckBox.setChecked(false);

        mYAxisCheckBox = (CheckBox) findViewById(R.id.axis_y_checkbox);
        mYAxisCheckBox.setChecked(false);

        mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
        mPowerXValueLabel.setText("0.0");

        mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
        mPowerYValueLabel.setText("0.0");

        mSpeedValueLabel = (TextView) findViewById(R.id.speed_value);
        mSpeedValueLabel.setText("0.0");

        mTimestampLabel = (TextView) findViewById(R.id.time_stamp_value);
        mTimestampLabel.setText(SystemClock.uptimeMillis() + " ms");

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                mRobot = (Sphero) robot;
                mRobot.getCollisionControl().addCollisionListener(mCollisionListener);
                mRobot.getCollisionControl().startDetection(45, 45, 100, 100, 100);
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

    private final CollisionListener mCollisionListener = new CollisionListener() {
        public void collisionDetected(CollisionDetectedAsyncData collisionData) {

            // Update the UI with the collision data
            Acceleration acceleration = collisionData.getImpactAcceleration();
            mAccelXValueLabel = (TextView) findViewById(R.id.accel_x_value);
            mAccelXValueLabel.setText("" + acceleration.x);

            mAccelYValueLabel = (TextView) findViewById(R.id.accel_y_value);
            mAccelYValueLabel.setText("" + acceleration.y);

            mAccelZValueLabel = (TextView) findViewById(R.id.accel_z_value);
            mAccelZValueLabel.setText("" + acceleration.z);

            mXAxisCheckBox = (CheckBox) findViewById(R.id.axis_x_checkbox);
            mXAxisCheckBox.setChecked(collisionData.hasImpactXAxis());

            mYAxisCheckBox = (CheckBox) findViewById(R.id.axis_y_checkbox);
            mYAxisCheckBox.setChecked(collisionData.hasImpactYAxis());

            CollisionPower power = collisionData.getImpactPower();
            mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
            mPowerXValueLabel.setText("" + power.x);

            mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
            mPowerYValueLabel.setText("" + power.y);

            mSpeedValueLabel = (TextView) findViewById(R.id.speed_value);
            mSpeedValueLabel.setText("" + collisionData.getImpactSpeed());

            mTimestampLabel = (TextView) findViewById(R.id.time_stamp_value);
            mTimestampLabel.setText(collisionData.getImpactTimeStamp() + " ms");


            if (power.x > 50 || power.y > 50) {
                changeDirection();
            }
        }
    };

    private void changeDirection() {

        Random randomGenerator = new Random();
        int randomNumber;
        float velocity = 1f;

        mRobot.stop();


        if (mCurrentDirection == 0) {
            //isUp
            float[] directions = {90f, 180f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], velocity);
            mCurrentDirection = (int) directions[randomNumber];
        } else if(mCurrentDirection == 180) {
            //isDown
            float[] directions = {0f, 90f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], velocity);
            mCurrentDirection = (int) directions[randomNumber];
        } else if (mCurrentDirection == 90) {
            //isRight
            float[] directions = {0f, 180f, 270f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], velocity);
            mCurrentDirection = (int) directions[randomNumber];
        } else if (mCurrentDirection == 270) {
            //isLeft
            float[] directions = {0f, 90f, 180f};
            randomNumber = randomGenerator.nextInt(directions.length);

            mRobot.drive(directions[randomNumber], velocity);
            mCurrentDirection = (int) directions[randomNumber];
        }


        /*float[] directions = {0f, 90f, 180f, 270f};
        Random randomGenerator = new Random();
        int randomNumber = randomGenerator.nextInt(directions.length);

        mRobot.drive(directions[randomNumber], 02f);*/

    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.up_button:
                mRobot.drive(0f, 0.2f);
                mCurrentDirection = 0;
                break;

            case R.id.right_button:
                mRobot.drive(90f, 0.2f);
                mCurrentDirection = 90;
                break;

            case R.id.down_button:
                mRobot.drive(180f, 0.2f);
                mCurrentDirection = 180;
                break;

            case R.id.left_button:
                mRobot.drive(270f, 0.2f);
                mCurrentDirection = 270;
                break;

            default:
                mRobot.stop();
                isUp = false;
                isDown = false;
                isLeft = false;
                isRight = false;
                break;
        }
    }
}