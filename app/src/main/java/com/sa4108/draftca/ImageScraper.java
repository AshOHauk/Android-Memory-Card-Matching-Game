package com.sa4108.draftca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageScraper {
    private String url;
    private final int maxImages;

    public ImageScraper(String url, int maxImages) {
        this.url = url;
        this.maxImages = maxImages;
    }

    public ArrayList<String> scrape() throws IOException {
        ArrayList<String> imageList = new ArrayList<>();

        //Load the web page content
        URL url = new URL(this.url);
        //
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.28) Gecko/20120306 Firefox/3.6.28");

//        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        //
//        HttpURLConnection httpConnection = (HttpURLConnection) connection;
//        int responseCode = httpConnection.getResponseCode();

//        if(responseCode >= 300 && responseCode <= 307 && responseCode != 306 &&
//                responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
//            // The resource was redirected, follow the redirection
//            String redirectLocation = null;
//            // Get redirect URL from "location" header field
//            String newUrl = httpConnection.getHeaderField("Location");
//
//            // Open connection to the new URL and repeat the process
//            url = new URL(newUrl);
//            connection = url.openConnection();
//            httpConnection = (HttpURLConnection) connection;
//        } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
//            // Not Modified: The resource was not modified since the version specified by
//            //               the request headers If-Modified-Since or If-None-Match
//            // Handle accordingly
//        } else if (responseCode == HttpURLConnection.HTTP_OK) {
//            // The request is successful
//            // Do nothing or handle per your requirement...
//        } else {
//            // other type of https response code, throw exception
//            throw new IOException(httpConnection.getResponseMessage());
//        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        //
        String line;
        StringBuilder htmlContent = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            htmlContent.append(line);
        }
        reader.close();

        //Extract the image URLs
//        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"](https?:\\/\\/[^'\"]+)['\"][^>]*>");
        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"](https?:\\/\\/[^'\"]+(?:\\.jpeg|\\.png|\\.jpg|\\.gif|\\.JPEG|\\.PNG|\\.JPG|\\.GIF))['\"][^>]*>");


        Matcher matcher = pattern.matcher(htmlContent.toString());
        while (matcher.find() && imageList.size() < maxImages) {
            String imageUrl = matcher.group(1);
            imageList.add(imageUrl);
        }

        // Return the list of image URLs
        return imageList;
    }
}