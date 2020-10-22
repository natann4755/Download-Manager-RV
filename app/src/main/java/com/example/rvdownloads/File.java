package com.example.rvdownloads;

public class File {
    int index;
    String status;
    int progress;
    boolean isDownload = false;



    public File(int index, String status, int progress) {
        this.index = index;
        this.status = status;
        this.progress = progress;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
