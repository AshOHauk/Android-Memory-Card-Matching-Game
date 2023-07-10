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

public class MemoryGameMultiActivity extends AppCompatActivity {

    private int playerTurn;
    private String matchResult;
    private TextView scoreDisplay_One;
    private TextView timerDisplay_One;
    private int score_One =0;
    private int secondsElapsed_One = 0;
    private TextView scoreDisplay_Two;
    private TextView timerDisplay_Two;
    private int score_Two =0;
    private int secondsElapsed_Two = 0;
    private Runnable timerRunnable_One;
    private Runnable timerRunnable_Two;
    private final Handler handler = new Handler();
    private final LruCache<String, Bitmap> imageCache = CacheManager.getInstance().getImageCache();
    private int firstSelectedPosition = -1;
    private int secondSelectedPosition = -1;
    private CardImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game_multi);

        final GridView gridView = findViewById(R.id.memoryGameGrid);
        adapter = new CardImageAdapter(this);
        gridView.setAdapter(adapter);

        playerTurn=1;
        String timeElapsed = "00:00";

        scoreDisplay_One = findViewById(R.id.score_One);
        timerDisplay_One = findViewById(R.id.timer_One);
        scoreDisplay_One.setText(String.format(Locale.UK, "%d /6", score_One));
        timerDisplay_One.setText(timeElapsed);
        timerRunnable_One = new Runnable() {
            @Override
            public void run() {
                secondsElapsed_One++;
                updateTimerDisplay();
                handler.postDelayed(this, 1000); // Run the task every 1 second
            }
        };

        scoreDisplay_Two = findViewById(R.id.score_Two);
        timerDisplay_Two = findViewById(R.id.timer_Two);
        scoreDisplay_Two.setText(String.format(Locale.UK, "%d /6", score_Two));
        timerDisplay_Two.setText(timeElapsed);
        timerRunnable_Two = new Runnable() {
            @Override
            public void run() {
                secondsElapsed_Two++;
                updateTimerDisplay();
                handler.postDelayed(this, 1000); // Run the task every 1 second
            }
        };

        //kick off p1's turn
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

            //award point to respective player
            if(playerTurn%2 == 0){
                score_Two++;
                scoreDisplay_Two.setText(String.format(Locale.UK,"%d / 6", score_Two));
            }
            else if (playerTurn%2 !=0){
                score_One++;
                scoreDisplay_One.setText(String.format(Locale.UK,"%d / 6", score_One));
            }
            // Execute successful match sequence
            firstSelectedPosition = -1;
            secondSelectedPosition = -1;
        } else {
            // No match
            AppAudioManager.playSoundEffect(this,R.raw.buzzer_or_wrong_answer_20582);
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

        stopTimer();
        playerTurn++;
        startTimer();

        if((score_One+score_Two) ==6){
            stopTimer();
            endGame();
        }
    }

    private void startTimer() {
        // Run the task every 1 second
        if(playerTurn%2 == 0){
            // timerRunnable used to be here
            handler.postDelayed(timerRunnable_Two, 1000);   //starts after 1s
        }
        else if (playerTurn%2 !=0){
            // timerRunnable used to be here
            handler.postDelayed(timerRunnable_One, 1000);   //starts after 1s
        }
    }

    private void updateTimerDisplay() {
        if(playerTurn%2 == 0){
            int minutes = secondsElapsed_Two / 60;
            int seconds = secondsElapsed_Two % 60;
            String time = String.format(Locale.UK,"%02d:%02d", minutes, seconds);
            timerDisplay_Two.setText(time);
        }
        else if (playerTurn%2 !=0){
            int minutes = secondsElapsed_One / 60;
            int seconds = secondsElapsed_One % 60;
            String time = String.format(Locale.UK,"%02d:%02d", minutes, seconds);
            timerDisplay_One.setText(time);
        }
    }

    private void stopTimer(){
        if(playerTurn%2 == 0){  //end of p2 turn
            handler.removeCallbacks(timerRunnable_Two);
        }
        else if (playerTurn%2 !=0){  //end of p1 turn
            handler.removeCallbacks(timerRunnable_One);
        }
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
        handler.removeCallbacks(timerRunnable_One);

        decideWinner();

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

        // Find the TextView for label in the popup layout
        TextView finalLabel = popupDialog.findViewById(R.id.popupLabel);
        // Find the TextView for value in the popup layout
        TextView finalTimeTextView = popupDialog.findViewById(R.id.finalTime);

        // Set the values for the popup window
        finalLabel.setText("Match Result");
        finalTimeTextView.setText(matchResult);

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

    public void decideWinner(){
        if(score_Two > score_One)
            matchResult = getString(R.string.p2_win);
        else if(score_Two < score_One)
            matchResult = getString(R.string.p1_win);
        else{
            if(secondsElapsed_Two > secondsElapsed_One)
                matchResult = getString(R.string.p1_win);
            else if(secondsElapsed_Two < secondsElapsed_One)
                matchResult = getString(R.string.p2_win);
            else
                matchResult = getString(R.string.draw);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCache.evictAll();
        handler.removeCallbacks(timerRunnable_One);
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
