package com.sa4108.draftca;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.LruCache;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MemoryGameActivity extends AppCompatActivity {
    private TextView scoreDisplay;
    private TextView timerDisplay;
    private int score=0;
    private int secondsElapsed = 0;
    private Runnable timerRunnable;
    private final Handler handler = new Handler();
    private final LruCache<String, Bitmap> imageCache = CacheManager.getInstance().getImageCache();
    private int firstSelectedPosition = -1;
    private int secondSelectedPosition = -1;
    private CardImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        final GridView gridView = findViewById(R.id.memoryGameGrid);
        adapter = new CardImageAdapter(this);
        gridView.setAdapter(adapter);
        
        scoreDisplay = findViewById(R.id.score);
        timerDisplay = findViewById(R.id.timer);
        scoreDisplay.setText(String.format(Locale.UK, "%d /6",score));
        String timeElapsed = "00:00";
        timerDisplay.setText(timeElapsed);
        startTimer();

        AppAudioManager.playSoundEffect(this,R.raw.sound_complete);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.isImageRevealed(position)){
                return;
            }
            if (firstSelectedPosition == -1) {
                // First image selected
                firstSelectedPosition = position;
                adapter.revealImage(position);
            } else if (secondSelectedPosition == -1) {
                // Second image selected
                secondSelectedPosition = position;
                adapter.revealImage(position);
                // Compare the tag numbers of the first and second selected images
                compareImages();
            }
        });
    }
    private void compareImages() {
        ImageItem firstImage = adapter.getImageItem(firstSelectedPosition);
        ImageItem secondImage = adapter.getImageItem(secondSelectedPosition);

        if (firstImage.getTagNumber() == secondImage.getTagNumber()) {
            // Match found
            AppAudioManager.playSoundEffect(this,R.raw.sound_complete);
            score++;
            scoreDisplay.setText(String.format(Locale.UK,"%d / 6", score));
            // Execute successful match sequence
            firstSelectedPosition = -1;
            secondSelectedPosition = -1;
        } else {
            // No match
            VibrationManager.vibrate(this,200);
            // Un-Reveal the images after a certain delay
            // Reset firstSelected and secondSelected
            handler.postDelayed(() -> {
                adapter.unrevealImage(firstSelectedPosition);
                adapter.unrevealImage(secondSelectedPosition);
                firstSelectedPosition = -1;
                secondSelectedPosition = -1;
            }, 1000); // Delay in milliseconds
        }
        if(score==6){
            endGame();
        }
    }

    private void startTimer() {
        // Run the task every 1 second
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                updateTimerDisplay();
                handler.postDelayed(this, 1000); // Run the task every 1 second
            }
        };

        // Start the timer task
        handler.postDelayed(timerRunnable, 1000);
    }

    private void updateTimerDisplay() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        String time = String.format(Locale.UK,"%02d:%02d", minutes, seconds);
        timerDisplay.setText(time);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Game Exit");
        builder.setMessage("Are you sure you want to exit the game? All progress will be lost.");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirms to exit the game;
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User chooses not to exit the game. Do nothing
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void endGame(){
        // Play Sound Effect
        AppAudioManager.playSoundEffect(this,R.raw.sound_complete);
        // Stop Timer
        handler.removeCallbacks(timerRunnable);
        // Create a Dialog object
        Dialog popupDialog = new Dialog(this);
        // Set the custom layout for the popup
        popupDialog.setContentView(R.layout.popup_completion);
        // Force user to click the button
        popupDialog.setCancelable(false);
        // Fix the dialog window, match the full screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = popupDialog.getWindow();
        if (window != null) {
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }

        // Find the TextView for time in the popup layout
        TextView finalTimeTextView = popupDialog.findViewById(R.id.finalTime);
        // Set the time value as the text for the TextView
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        String time = String.format(Locale.UK,"%02d:%02d", minutes, seconds);
        finalTimeTextView.setText(time);

        // Find and set up any views or buttons within the popup layout
        Button closeButton = popupDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            popupDialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            AppAudioManager.incrementActiveActivityCount();
            startActivity(intent);
            this.finish();
        });

        // Show the popup
        popupDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCache.evictAll();
        handler.removeCallbacks(timerRunnable);
        AppAudioManager.decrementActiveActivityCount();
        AppAudioManager.stopBackgroundAudio();
        AppAudioManager.releaseSoundEffectPool();
        }

    @Override
    protected void onPause() {
        super.onPause();
        AppAudioManager.pauseBackgroundAudio();
        // TODO pause the timer
    }
    @Override
    protected void onResume() {
        super.onResume();
        AppAudioManager.resumeBackgroundAudio();
        // TODO resume the timer
    }


}
