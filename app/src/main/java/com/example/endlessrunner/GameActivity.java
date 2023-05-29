package com.example.endlessrunner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.Random;

public class GameActivity extends Activity {

    private FrameLayout gameLayout;
    private CubeCharacterView cubeCharacterView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameLayout = findViewById(R.id.gameLayout);

        cubeCharacterView = new CubeCharacterView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                100 // Set the height of the character as needed
        );
        params.leftMargin = -800; // Set the left margin as needed
        params.topMargin = 1710; // Set the top margin as needed
        gameLayout.addView(cubeCharacterView, params);
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

        final Handler handler = new Handler();
        final Handler obstacleHandler = new Handler(); // Add this line to initialize the obstacleHandler

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
                obstacleParams.topMargin = 1810 - obstacleHeights[obstacleType]; // Set the vertical position of the obstacle

                // Add the obstacle image view to the game layout
                gameLayout.addView(obstacleImageView, obstacleParams);

                // Move the obstacle from right to left
                Handler obstacleHandler = new Handler();
                Runnable obstacleMovementRunnable = new Runnable() {
                    @Override
                    public void run() {
                        obstacleParams.leftMargin += obstacleSpeed; // Adjust the movement speed as needed

                        // Check if the obstacle has gone off the screen
                        if (obstacleParams.leftMargin + obstacleWidths[obstacleType] < 0) {
                            // Remove the obstacle image view from the game layout
                            gameLayout.removeView(obstacleImageView);
                            return;
                        }

                        obstacleImageView.setLayoutParams(obstacleParams);

                        // Check for collision with character
                        if (isColliding(obstacleImageView)) {
                            // Check the color of the obstacle and character
                            if (obstacleType != obstacleTypeGreen || CubeCharacterView.getColor() != obstacleColors[obstacleType]) {
                                // Character collided with an obstacle of a different color
                                handleCollision();
                            }
                        }

                        // Call the runnable again after a certain delay (e.g., 16 milliseconds for smooth movement)
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
        handler.postDelayed(obstacleCreationRunnable, 2000); // Adjust the delay time as needed
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
        // Remove callbacks and messages from the obstacleHandler
        Handler obstacleHandler = new Handler();
        obstacleHandler.removeCallbacksAndMessages(null);

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
                // Handle retry button click to restart the game
                recreate(); // Restart the current activity
            }
        });
    }


}
