package com.sa4108.draftca;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity{
    String url;
    private final int maxNumberOfImages = 20;
    public final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //ExecutorService provides a simple way to launch new threads, and manage concurrent tasks
    //In this case, a single-thread executor is created, it processes one task at a time (FIFO)
    private final Handler handler = new Handler(Looper.getMainLooper());
    //Handler object acts as channel to post tasks and messages back to a thread.
    //In this case, it is back to the Main Thread for UI updates
    private final ArrayList<String> imageList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView progressText;
    private int counter=0;

    private Future<?> futureTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText urlEditText = findViewById(R.id.urlEditText);
        CustomListAdapter customListAdapter = new CustomListAdapter(this, imageList,this);
        GridView gv = findViewById(R.id.imageGridView);
        if(gv!=null) {
            gv.setAdapter(customListAdapter);
        }

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        findViewById(R.id.fetchButton).setOnClickListener(v -> {
            if (futureTask != null && !futureTask.isDone()) {
                futureTask.cancel(true);  // attempt to interrupt if task is running
            }
            counter=0;
            imageList.clear();
            customListAdapter.imageCache.evictAll();
            url = urlEditText.getText().toString();


            downloadImages();
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        findViewById(R.id.imageGridView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO: Handle selection of images, keep count until six are chosen
            }
        });
    }

    private void downloadImages() {
        futureTask = executorService.submit(() -> { // Background process
            try{
                //Load the web page content
                URL url = new URL(this.url);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                int responseCode = httpConnection.getResponseCode();
                if(responseCode!=200){
                    throw new Exception("Non-200 HTTP Response received: " + responseCode);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder htmlContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line);
                }
                reader.close();
                //Extract the image URLs
                Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"](https?://[^'\"]+(?:\\.jpeg|\\.png|\\.jpg|\\.gif|\\.JPEG|\\.PNG|\\.JPG|\\.GIF))['\"][^>]*>");
                Matcher matcher = pattern.matcher(htmlContent.toString());
                while (matcher.find() && imageList.size() < maxNumberOfImages) {
                    String imageUrl = matcher.group(1);
                    imageList.add(imageUrl);
                }
                progressBar.setProgress(0);
                progressBar.setMax(imageList.size());

                handler.post(() -> { // Final UI operations on main thread
                    //Refresh the view
                    //.getAdapter returns an Adapter Object which is a superclass.
                    //Down casting it to CustomListAdapter gives us access to the method notifyDataSetChanged()
                    //This method asks the adapter to redraw the view
                    ((CustomListAdapter)((GridView)findViewById(R.id.imageGridView)).getAdapter()).notifyDataSetChanged();
                });
            }catch(IOException eio){
                eio.printStackTrace();
                //TODO handle gracefully
                progressText.setText("File not found. Please check that the URL is valid");
            }catch(Exception e){
                e.printStackTrace();
                //TODO handle gracefully
                progressText.setText("Error");
            }
        });
    }
    void updateProgressText(){
        runOnUiThread(()->{
            counter++;
            progressBar.setProgress(counter);
            progressText.setText(String.format(Locale.UK,"Downloaded %d of %d images...", counter ,imageList.size()));
            if (counter==imageList.size()){
                progressText.setText("Images downloaded");
            }
        });
    }

}
