package com.sa4108.draftca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.LruCache;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity implements ImageDownloadCallback{
    private final Handler handler = new Handler();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //ExecutorService provides a simple way to launch new threads, and manage concurrent tasks
    //In this case, a single-thread executor is created, it processes one task at a time (FIFO)
    private final ArrayList<String> imageList = new ArrayList<>();
    //List of imageUrls scraped from website
    private ProgressBar progressBar;
    private TextView progressText;
    private final Map<String, Future<?>> futuresMap = new ConcurrentHashMap<>();
    //Keep track of tasks
    private final LruCache<String, Bitmap> imageCache = CacheManager.getInstance().getImageCache();
    private final ArrayList<String> selectedImages = new ArrayList<>();
    private GridView gv;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppAudioManager.startBackgroundAudio(this, R.raw.game_start);
        final EditText urlEditText = findViewById(R.id.urlEditText);

        CustomListAdapter customListAdapter = new CustomListAdapter(this,imageList);
        gv = findViewById(R.id.imageGridView);
        if(gv!=null) {
            gv.setAdapter(customListAdapter);
            gv.setOnItemClickListener((parent, v, position, id) -> {
                String selectedImage = imageList.get(position);
                boolean isSelected = toggleImageSelection(selectedImage);

                if (isSelected) {
                    v.setBackgroundResource(R.drawable.selected_overlay);
                } else {
                    v.setBackground(null);
                }

                if (selectedImages.size() == 6) {
                    Map<String, Bitmap> mapSnapshot = imageCache.snapshot();
                    for (String url : mapSnapshot.keySet()){
                        if (!selectedImages.contains(url)) {
                            imageCache.remove(url);
                        }
                    }

                    //TODO: change this to a popup window
                    //  or just give it a button to toggle?
                    //Intent intent = new Intent(this, MemoryGameActivity.class);
                    Intent intent = new Intent(this, MemoryGameMultiActivity.class);
                    AppAudioManager.incrementActiveActivityCount();
                    startActivity(intent);
                    this.finish();
                }
            });
        }

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        findViewById(R.id.fetchButton).setOnClickListener(v -> {
            //interrupt any past ongoing tasks
            for (Future<?> tasks: futuresMap.values()){
                tasks.cancel(true);
            }
            String newUrl = String.valueOf(urlEditText.getText());
            //reset
            selectedImages.clear();
            progressText.setText("Retrieving...");
            // Clear overlays for all items
            for (int i = 0; i < gv.getChildCount(); i++) {
                gv.getChildAt(i).setBackground(null);
            }


            //Queue new Task with newUrl input
            ImageDownloadTask task = new ImageDownloadTask(newUrl,this,this);
            Future<?> futureTask = executorService.submit(task);
            //Register the Task for future interruption if triggered
            futuresMap.put(newUrl, futureTask);
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });
    }
    protected void onDestroy(){
        super.onDestroy();
        imageCache.evictAll();
        executorService.shutdown();
        AppAudioManager.decrementActiveActivityCount();
        AppAudioManager.stopBackgroundAudio();
    }
    @Override
    protected void onPause() {
        super.onPause();
        AppAudioManager.pauseBackgroundAudio();
    }
    @Override
    protected void onResume() {
        super.onResume();
        AppAudioManager.resumeBackgroundAudio();
    }


    private boolean toggleImageSelection(String image) {
        if (selectedImages.contains(image)) {
            // Image is already selected, so remove it from the list
            selectedImages.remove(image); // Not selected anymore
            return false;
        } else {
            // Image is not selected, so add it to the list
            selectedImages.add(image); // Selected now
            return true;
        }
    }
    @Override
    public void onUrlRetrievalComplete(ArrayList<String> ImageUrlList){
        this.imageList.clear();
        imageCache.evictAll();
        progressBar.setMax(ImageUrlList.size());
        imageList.addAll(ImageUrlList);
        handler.post(() -> {
            ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            progressText.setText(String.format(Locale.UK,"Retrieved %d image URLs..downloading..",ImageUrlList.size()));
        });
    }

    @Override
    public void onEachImageDownloadComplete(int downloaded, int total) {
        // TODO Handle the downloaded image list
        // Update UI or perform any other necessary actions
        String message = String.format(Locale.UK,"Downloaded %d of %d images", downloaded, total);
        handler.post(() -> {
            ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            progressBar.setProgress(downloaded);
            progressText.setText(message);
        });
    }
    @Override
    public void onAllImageDownloadComplete(String url){
        futuresMap.remove(url);
    }

    @Override
    public void onImageDownloadError(String url,String errorMessage) {
        progressText.setText(errorMessage);
        futuresMap.remove(url);
    }

    @Override
    public void onTaskInterrupted(String url){
        //TODO
        progressBar.setProgress(0);
        imageList.clear();
        handler.post(() -> {
            ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            progressText.setText("Previous query stopped");
        });
        futuresMap.remove(url);
        imageCache.evictAll();
    }
}
