package com.sa4108.draftca;

import java.util.ArrayList;

public interface ImageDownloadCallback {
    void onUrlRetrievalComplete(ArrayList<String> ImageUrlList);
    void onEachImageDownloadComplete(int downloaded,int total);
    void onAllImageDownloadComplete(String url);
    void onImageDownloadError(String url,String errorMessage);
    void onTaskInterrupted(String url);
}

