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
    private EditText velocityInput;
    private int mVelocityPercentage;

    private Button mAlwaysMovingButton;

    private float mVelocityX;
    private float mVelocityY;
    private boolean isStill;
    private boolean startedCornerProof;

    private Sphero mRobot;

    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.back_layout).requestFocus();

        isStill = false;

        mAlwaysMovingButton = (Button) findViewById(R.id.isStillButton);

        mAlwaysMovingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStill) {
                    startCheckingCorners();
                    isStill = true;
                    Toast.makeText(CollisionsActivity.this, "Corner proof activated", Toast.LENGTH_LONG).show();

                    mAlwaysMovingButton.setBackgroundColor(Color.parseColor("#4000ff00"));
                }
                else if (isStill) {
                    isStill = false;
                    Toast.makeText(CollisionsActivity.this, "Corner proof deactivated", Toast.LENGTH_LONG).show();

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

    private LocatorListener mLocatorListener = new LocatorListener() {
        @Override
        public void onLocatorChanged(LocatorData locatorData) {

            mVelocityX = locatorData.getVelocityX();
            mVelocityY = locatorData.getVelocityY();

            mVelocityXLabel = (TextView) findViewById(R.id.velocityX_value);
            mVelocityXLabel.setText(mVelocityX + " cm/s");

            mVelocityYLabel = (TextView) findViewById(R.id.velocityY_value);
            mVelocityYLabel.setText(mVelocityY + " cm/s");

        }
    };

    private final CollisionListener mCollisionListener = new CollisionListener() {
        public void collisionDetected(CollisionDetectedAsyncData collisionData) {

            // Update the UI with the collision data
            CollisionPower power = collisionData.getImpactPower();
            mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
            mPowerXValueLabel.setText("" + power.x);

            mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
            mPowerYValueLabel.setText("" + power.y);

            if (power.x > 50 || power.y > 50) {
                changeDirection();
            }
        }
    };

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

    private void startCheckingCorners() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startedCornerProof = true;
                cornerProof();
                startCheckingCorners();
            }
        }, 2000);

    }

    private void cornerProof() {

        //Wait 4 seconds and change ball direction if it doesn't move
        mRobot.setColor(255, 0, 0);


        if (mVelocityX <= 2 && mVelocityY <= 2 && startedCornerProof && isStill) {
            startedCornerProof = false;

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

            /*case R.id.up_button:
                mRobot.drive(0f, mVelocity);
                mCurrentDirection = 0;
                break;

            case R.id.right_button:
                mRobot.drive(90f, mVelocity);
                mCurrentDirection = 90;
                break;

            case R.id.down_button:
                mRobot.drive(180f, mVelocity);
                mCurrentDirection = 180;
                break;

            case R.id.left_button:
                mRobot.drive(270f, mVelocity);
                mCurrentDirection = 270;
                break;*/

            case R.id.velocityButton:
                mVelocityPercentage = Integer.parseInt(velocityInput.getText().toString());
                mVelocity = mVelocityPercentage / 100;
                Toast.makeText(this, "velocity set to " + mVelocityPercentage + "%", Toast.LENGTH_LONG).show();
                break;


            default:
                mRobot.stop();
                break;
        }
    }
}