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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity{
    String url;
    private final int maxNumberOfImages = 40;
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
        CustomListAdapter customListAdapter = new CustomListAdapter(this, imageList);
        GridView gv = findViewById(R.id.imageGridView);
        if(gv!=null) {
            gv.setAdapter(customListAdapter);
        }

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
                    try{
                        Thread.sleep(50); //TODO remove after testing
                    }catch(Exception e_sleep){}
                    handler.post(() -> { // post to main thread to update UI
                        progressBar.setProgress(imageList.size());
                        progressText.setText(String.format(Locale.UK,"Downloading %d of %d images...", imageList.size(),maxNumberOfImages));
                    });
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
}
