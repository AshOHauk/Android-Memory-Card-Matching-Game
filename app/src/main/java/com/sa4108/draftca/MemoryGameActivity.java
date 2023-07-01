package com.sa4108.draftca;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MemoryGameActivity extends AppCompatActivity {
    private final int numOfMatches=0;
    private final String timeElapsed ="00:00";
    private final Handler handler = new Handler();
    private Runnable runnable;
    private TextView matchesTextView;
    private TextView timerTextview;
    private final LruCache<String, Bitmap> cache = CacheManager.getInstance().getCache();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);
        GridView gridView = (GridView) findViewById(R.id.memoryGameGrid);
        gridView.setAdapter(new CardImageAdapter(this));
    }


}
