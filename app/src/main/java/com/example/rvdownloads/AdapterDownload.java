package com.example.rvdownloads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterDownload extends RecyclerView.Adapter<AdapterDownload.ViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<File> myDownloads;
    private Download mListener;



    public AdapterDownload(Context context, ArrayList<File> myDownloads) {
        this.mInflater = LayoutInflater.from(context);
        this.myDownloads = myDownloads;
        mListener = (Download) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_download, parent, false);
        return new AdapterDownload.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setHolder(position);
    }

    @Override
    public int getItemCount() {
        return myDownloads.size();
    }

    public void updateDownloadFinish(int position){
        myDownloads.get(position).setDownload(false);
        myDownloads.get(position).setProgress(0);
        myDownloads.get(position).setStatus("שתף");
        notifyDataSetChanged();
    }

    public void updateDownloadBar(int position,int progress){
        myDownloads.get(position).setProgress(progress);
        notifyItemChanged(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        int position;
        Button downloadButton;
        LinearLayout mLinearLayout;
        ProgressBar mProgressBar;
        TextView mTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.LinearLayout_downloadFile_LL);
            mProgressBar = itemView.findViewById(R.id.progressBar_downloadFile_PB);
            mTextView = itemView.findViewById(R.id.text_downloadFile_TV);
            downloadButton = itemView.findViewById(R.id.sharing_audio_file_BUT);
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (downloadButton.getText().equals("הורדה")){
                        downloadButton.setVisibility(View.GONE);
                        mLinearLayout.setVisibility(View.VISIBLE);
                        myDownloads.get(position).setDownload(true);
                        mListener.downloadClick(position);
                    }else {
//                        downloadButton.setText("הורדה");
//                        myDownloads.get(position).setStatus("הורדה");
                        mListener.shareClick(position);
                    }
                }
            });
        }

        public void setHolder(int position) {
            this.position = position;
            if (myDownloads.get(position).isDownload){
                downloadButton.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mTextView.setText(String.valueOf(myDownloads.get(position).getProgress()));
                mProgressBar.setProgress(myDownloads.get(position).getProgress());

            }else {
                mLinearLayout.setVisibility(View.GONE);
                downloadButton.setVisibility(View.VISIBLE);
                downloadButton.setText(myDownloads.get(position).getStatus());
            }
        }

    }
    public interface Download {
        void downloadClick(int position);
        void shareClick (int position);
    }
}
