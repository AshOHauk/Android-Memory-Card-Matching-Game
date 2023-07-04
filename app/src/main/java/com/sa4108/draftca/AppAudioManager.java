package com.sa4108.draftca;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class AppAudioManager {
    private static MediaPlayer mediaPlayer;
    private static SoundPool soundEffectPool;
    private static int soundEffectId;
    private static int activeActivityCount = 1;

    public static void startBackgroundAudio(Context context, int backgroundMusicResourceId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, backgroundMusicResourceId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    public static void stopBackgroundAudio() {
        if (mediaPlayer != null && activeActivityCount <= 0) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public static void pauseBackgroundAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    public static void resumeBackgroundAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }


    public static void playSoundEffect(Context context, int soundEffectResourceId) {
        if (soundEffectPool == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundEffectPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        soundEffectId = soundEffectPool.load(context, soundEffectResourceId, 1);

        soundEffectPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                // loaded successfully
                soundEffectPool.play(soundEffectId, 1.0f, 1.0f, 0, 0, 1.0f);
            } else {
                // Failed to load the sample
                //TODO
            }
        });
    }

    public static void incrementActiveActivityCount() {
        activeActivityCount += 1;
    }

    public static void decrementActiveActivityCount() {
        activeActivityCount -= 1;
    }

    public static void releaseSoundEffectPool() {
        if (soundEffectPool != null && activeActivityCount <= 0) {
            soundEffectPool.release();
            soundEffectPool = null;
        }
    }
}
