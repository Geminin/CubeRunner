package com.example.endlessrunner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Random;

public class GameActivity extends Activity implements SensorEventListener {

    private static final float GRAVITY = 100;
    private FrameLayout gameLayout;
    private CubeCharacterView cubeCharacterView;
    private boolean isRedColor = true;
    private Handler handler;

    private boolean isJumping;
    private long jumpStartTime;
    private float initialJumpY;

    private static final int JUMP_DURATION = 1000; // Adjust the jump duration as needed
    private static final float JUMP_HEIGHT = 300; // Adjust the jump height as needed

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean shakeDetected;
    private static final float SHAKE_THRESHOLD = 15.0f; // Adjust the shake threshold as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gameLayout = findViewById(R.id.gameLayout);

        cubeCharacterView = new CubeCharacterView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                100, // Set the width of the character as needed
                100 // Set the height of the character as needed
        );
        params.leftMargin = 100; // Set the left margin as needed
        params.topMargin = 1810; // Set the top margin as needed
        gameLayout.addView(cubeCharacterView, params);

        handler = new Handler(); // Initialize the handler
        cubeCharacterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isJumping) {
                    jump();
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            createObstacle();
        }
    }

    private void createObstacle() {
        final int obstacleSpacing = 1500; // Set the spacing between obstacles
        final int obstacleSpeed = -6; // Set the speed of the obstacles as needed

        final int numObstacleTypes = 3; // Number of obstacle types
        final int obstacleTypeGreen = 0; // Index of the green obstacle type
        final int[] obstacleColors = {Color.GREEN, Color.RED, Color.BLUE}; // Colors for each obstacle type
        final int[] obstacleWidths = {80, 20, 20}; // Widths for each obstacle type
        final int[] obstacleHeights = {80, gameLayout.getHeight(), gameLayout.getHeight()}; // Heights for each obstacle type
        final boolean[] collisionEnabled = {false};
        final Paint obstaclePaint = new Paint();

        Runnable obstacleCreationRunnable = new Runnable() {
            @Override
            public void run() {
                // Generate a random obstacle type
                int obstacleType = new Random().nextInt(numObstacleTypes);

                // Create a bitmap for the obstacle
                Bitmap obstacleBitmap = Bitmap.createBitmap(obstacleWidths[obstacleType], obstacleHeights[obstacleType], Bitmap.Config.ARGB_8888);

                // Create a canvas from the bitmap
                Canvas obstacleCanvas = new Canvas(obstacleBitmap);

                // Set the color of the obstacle
                obstaclePaint.setColor(obstacleColors[obstacleType]);

                // Draw the obstacle on the canvas
                obstacleCanvas.drawRect(0, 0, obstacleWidths[obstacleType], obstacleHeights[obstacleType], obstaclePaint);

                // Create an image view to display the obstacle
                ImageView obstacleImageView = new ImageView(GameActivity.this);
                obstacleImageView.setImageBitmap(obstacleBitmap);

                // Set the position of the obstacle image view
                FrameLayout.LayoutParams obstacleParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
                );
                obstacleParams.leftMargin = gameLayout.getWidth(); // Set the initial horizontal position of the obstacle
                obstacleParams.topMargin = 1910 - obstacleHeights[obstacleType]; // Set the vertical position of the obstacle

                // Add the obstacle image view to the game layout
                gameLayout.addView(obstacleImageView, obstacleParams);

                // Move the obstacle from right to left
                final Handler obstacleHandler = new Handler();
                Runnable obstacleMovementRunnable = new Runnable() {
                    @Override
                    public void run() {
                        obstacleParams.leftMargin += obstacleSpeed;

                        // Check if the obstacle has gone off the screen
                        if (obstacleParams.leftMargin + obstacleWidths[obstacleType] < 0) {
                            gameLayout.removeView(obstacleImageView);
                            return;
                        }

                        obstacleImageView.setLayoutParams(obstacleParams);

                        // Check for collision with character
                        if (isColliding(obstacleImageView)) {
                            int characterColor = cubeCharacterView.getColor();
                            int obstacleColor = obstacleColors[obstacleType];

                            // Check if the character and obstacle have the same color
                            if (characterColor != obstacleColor) {
                                handleCollision();
                                return;
                            }
                        }

                        obstacleHandler.postDelayed(this, 16);
                    }
                };

                // Start the obstacle movement
                obstacleHandler.postDelayed(obstacleMovementRunnable, 0);

                // Call the runnable again after the obstacle spacing
                handler.postDelayed(this, obstacleSpacing);
            }
        };

        // Delay the start of the obstacle creation loop
        handler.postDelayed(obstacleCreationRunnable, 1000); // Adjust the delay time as needed
    }

    private boolean isColliding(@NonNull View obstacleView) {
        int[] charLocation = new int[2];
        cubeCharacterView.getLocationOnScreen(charLocation);
        int charLeft = charLocation[0];
        int charRight = charLeft + cubeCharacterView.getWidth();
        int charTop = charLocation[1];
        int charBottom = charTop + cubeCharacterView.getHeight();

        int[] obstacleLocation = new int[2];
        obstacleView.getLocationOnScreen(obstacleLocation);
        int obstacleLeft = obstacleLocation[0];
        int obstacleRight = obstacleLeft + obstacleView.getWidth();
        int obstacleTop = obstacleLocation[1];
        int obstacleBottom = obstacleTop + obstacleView.getHeight();

        return charLeft < obstacleRight && charRight > obstacleLeft &&
                charTop < obstacleBottom && charBottom > obstacleTop;
    }

    private void handleCollision() {
        // Remove callbacks and messages from the handler
        handler.removeCallbacksAndMessages(null);

        // Add your collision handling logic here

        // Show "GAME OVER" screen and buttons
        setContentView(R.layout.activity_game_over);
        Button menuButton = findViewById(R.id.backButton);
        Button retryButton = findViewById(R.id.retryButton);

        // Set click listeners for the buttons
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle menu button click to go back to the menu screen
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void jump() {
        if (!isJumping) {
            isJumping = true;
            jumpStartTime = System.currentTimeMillis();
            initialJumpY = cubeCharacterView.getY();
            jumpAnimation();
        }
    }

    private void jumpAnimation() {
        final long elapsedTime = System.currentTimeMillis() - jumpStartTime;
        if (elapsedTime <= JUMP_DURATION) {
            float jumpProgress = (float) elapsedTime / JUMP_DURATION;
            float jumpOffset = JUMP_HEIGHT * (1 - (float) Math.pow(jumpProgress - 1, 2));

            // Apply gravity effect for smooth fall
            float gravity = GRAVITY * elapsedTime / JUMP_DURATION;
            float fallOffset = gravity * (float) Math.pow(jumpProgress, 2);

            float totalOffset = jumpOffset - fallOffset;
            cubeCharacterView.setY(initialJumpY - totalOffset);
            handler.postDelayed(this::jumpAnimation, 16);
        } else {
            cubeCharacterView.setY(initialJumpY);
            isJumping = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);



    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            Log.d("a", String.valueOf(acceleration));


            if (acceleration > SHAKE_THRESHOLD) {
                // Shake detected, perform your desired action here
                handleShake();
            }
        }

        // Check if the acceleration exceeds the shake threshold

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not used
    }

    private void handleShake() {

        //setContentView(R.layout.activity_game_over);




        CubeCharacterView characterView = cubeCharacterView.get();
        if (characterView != null) {
            if (isRedColor) {
                characterView.setColor(Color.BLUE);
            } else {
                characterView.setColor(Color.RED);
            }
            isRedColor = !isRedColor;
            characterView.invalidate(); // Force the view to redraw
        }
    }
}
