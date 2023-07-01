package com.sa4108.draftca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity{
    public final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //ExecutorService provides a simple way to launch new threads, and manage concurrent tasks
    //In this case, a single-thread executor is created, it processes one task at a time (FIFO)
    public final ArrayList<String> imageList = new ArrayList<>();
    //List of imageUrls scraped from website
    public ProgressBar progressBar;
    private TextView progressText;
    public final Map<String, Future<?>> futuresMap = new ConcurrentHashMap<>();
    //Keep track of tasks
    private final LruCache<String, Bitmap> cache = CacheManager.getInstance().getCache();
    private final ArrayList<String> selectedImages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText urlEditText = findViewById(R.id.urlEditText);

        CustomListAdapter customListAdapter = new CustomListAdapter(this,imageList);
        GridView gv = findViewById(R.id.imageGridView);
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
                    Map<String, Bitmap> mapSnapshot = cache.snapshot();
                    for (String url : mapSnapshot.keySet()){
                        if (!selectedImages.contains(url)) {
                            cache.remove(url);
                        }
                    }
                    Intent intent = new Intent(this, MemoryGameActivity.class);
                    startActivity(intent);
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
            progressText.setText("Retrieving...");
            progressBar.setProgress(0);
            imageList.clear();
            cache.evictAll();
            selectedImages.clear();
            //Queue new Task with newUrl input
            ImageDownloadTask task = new ImageDownloadTask(newUrl,this);
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
        executorService.shutdown();
    }

    private boolean toggleImageSelection(String image) {
        boolean isSelected = selectedImages.contains(image);
        if (isSelected) {
            // Image is already selected, so remove it from the list
            selectedImages.remove(image);
        } else {
            // Image is not selected, so add it to the list
            selectedImages.add(image);
        }
        return !isSelected; // Return the updated selection status
    }
    //Render Image GridView, triggered by ImageDownloadTask per downloaded image
    void updateGridView(String message) {
        runOnUiThread(() -> {
            ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            progressText.setVisibility(View.VISIBLE);
            progressText.setText(message);
        });
    }
}
