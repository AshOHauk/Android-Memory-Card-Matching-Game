package com.sa4108.draftca;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
    String url;
    private final int maxNumberOfImages = 20;
    public final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //ExecutorService provides a simple way to launch new threads, and manage concurrent tasks
    //In this case, a single-thread executor is created, it processes one task at a time (FIFO)
    public final ArrayList<String> imageList = new ArrayList<>();
    public ProgressBar progressBar;
    private TextView progressText;
    public final Map<String, Future> futuresMap = new ConcurrentHashMap<>();
    private LruCache<String, Bitmap> cache = CacheManager.getInstance().getCache();
    private ArrayList<String> selectedImages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText urlEditText = findViewById(R.id.urlEditText);
        CustomListAdapter customListAdapter = new CustomListAdapter(this,imageList);
        GridView gv = findViewById(R.id.imageGridView);
        if(gv!=null) {
            gv.setAdapter(customListAdapter);
        }

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        findViewById(R.id.fetchButton).setOnClickListener(v -> {
            String newUrl = String.valueOf(urlEditText.getText());
            Future previousTask = futuresMap.get(newUrl);
            if(previousTask != null && !previousTask.isDone()){
                previousTask.cancel(true);
            }
            progressText.setVisibility(View.GONE);
            progressBar.setProgress(0);
            imageList.clear();
            cache.evictAll();
            selectedImages.clear();
            ImageDownloadTask task = new ImageDownloadTask(newUrl,this);
            Future futureTask = executorService.submit(task);
            futuresMap.put(newUrl, futureTask);
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

//        findViewById(R.id.imageGridView).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // Hide the keyboard
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                return true;
//            }
//        });

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Get the selected image from the adapter
                String selectedImage = imageList.get(position);

                // TODO: Handle selection of images, keep count until six are chosen

                // Toggle the selection status of the clicked image
                boolean isSelected = toggleImageSelection(selectedImage);

                // Update the UI based on the selection status
                if (isSelected) {
                    // Apply an outline or any other visual indication to the selected image
                    v.setBackgroundResource(R.drawable.selected_overlay);
                } else {
                    // Remove the outline or any visual indication from the unselected image
                    v.setBackground(null);
                }
            }
        });
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

    void updateGridView(String message) {
        runOnUiThread(() -> {
            ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            progressText.setVisibility(View.VISIBLE);
            progressText.setText(message);
        });

    }

}
