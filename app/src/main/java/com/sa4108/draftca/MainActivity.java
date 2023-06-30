package com.sa4108.draftca;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity{
    String url;
    private final int maxNumberOfImages = 5;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //ExecutorService provides a simple way to launch new threads, and manage concurrent tasks
    //In this case, a single-thread executor is created, it processes one task at a time (FIFO)
    private final Handler handler = new Handler(Looper.getMainLooper());
    //Handler object acts as channel to post tasks and messages back to a thread.
    //In this case, it is back to the Main Thread for UI updates
    private final ArrayList<String> imageList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText urlEditText = findViewById(R.id.urlEditText);
        GridView gv = findViewById(R.id.imageGridView);
        CustomListAdapter customListAdapter = new CustomListAdapter(this, imageList);
        gv.setAdapter(customListAdapter);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(maxNumberOfImages);  //
        progressText = findViewById(R.id.progressText);

        findViewById(R.id.fetchButton).setOnClickListener(v -> {
            url = urlEditText.getText().toString();
            imageList.clear();
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            downloadImages();
        });


        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO: Handle selection of images, keep count until six are chosen
            }
        });
    }

    private void downloadImages() {
        executorService.execute(() -> { // Background process
//            try {
//                Document doc = Jsoup.connect(url).get();
//                Elements elements = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
//
//                for (Element e: elements) {
//                    imageList.add(e.attr("src"));
//                    if (imageList.size() == maxNumberOfImages) {
//                        break;
//                    }
//
//                    int finalSize = imageList.size();
//                    handler.post(() -> { // post to main thread to update UI
//                        progressBar.setProgress(finalSize);
//                        progressText.setText(String.format(Locale.UK,"Downloading %d of %d images...", finalSize,maxNumberOfImages));
//                    });
//                }
//            } catch (IOException e) {
//                //exception can occur if Jsoup cannot open a connection to the provided URL
//                //or if a network issue occurs during data transfer
//                e.printStackTrace();
//            }
            try{
                ImageScraper imageScraper = new ImageScraper(url,maxNumberOfImages);

                imageList.addAll(imageScraper.scrape());

            }catch(IOException eio){
                eio.printStackTrace();
            }

            handler.post(() -> { // Final UI operations on main thread
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                //Refresh the view
                //.getAdapter returns an Adapter Object which is a superclass.
                //Down casting it to CustomListAdapter gives us access to the method notifyDataSetChanged()
                //This method asks the adapter to redraw the view
                ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
            });
        });
    }
}
