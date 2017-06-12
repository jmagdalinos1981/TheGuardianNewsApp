package com.example.android.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Custom Adapter with ViewHolder supporting the use of the RecyclerView
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> mNews;
    private Context mContext;
    private String mURL;

    /** Tag for the log messages */
    public static final String LOG_TAG = NewsAdapter.class.getSimpleName();

    // Provide a direct reference to each of the views within a list_item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // The ViewHolder holds a variable for every View that will be used
        public TextView newsTitle;
        public TextView newsSection;
        public TextView newsPublicationDate;
        public TextView newsPublicationTime;
        private Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            // Store the context
            this.context = context;

            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this);

            newsTitle = (TextView) itemView.findViewById(R.id.news_title);
            newsSection = (TextView) itemView.findViewById(R.id.news_section);
            newsPublicationDate = (TextView) itemView.findViewById(R.id.news_publication_date);
            newsPublicationTime = (TextView) itemView.findViewById(R.id.news_publication_time);
        }

        // Handles the row being being clicked
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            NewsItem newsItem = mNews.get(position);

            // Get the Url from the current NewsItem
            mURL = newsItem.getWebURL();

            // Convert the String URL into a URI object (to pass into the Intent constructor)
            Uri newsURI = Uri.parse(mURL);
            // Create new intent to view the article's URL
            Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsURI);
            // Start the intent
            context.startActivity(websiteIntent);
        }
    }


    // Pass in the contact array into the constructor
    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        mContext = context;
        mNews = newsItems;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Involves inflating a layout from XML and returning the holder
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listView = inflater.inflate(R.layout.list_item, parent, false);

        // Return a new ViewHolder instance
        ViewHolder viewHolder = new ViewHolder(mContext, listView);
        return viewHolder;
    }

    // Involves populating data into the item through the holder
    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        NewsItem newsItem = mNews.get(position);

        // Set item views based on views and data model
        TextView newsTitleTextView = viewHolder.newsTitle;
        TextView newsSectionTextView = viewHolder.newsSection;
        TextView newsPublicationDateTextView = viewHolder.newsPublicationDate;
        TextView newsPublicationTimeTextView = viewHolder.newsPublicationTime;

        newsTitleTextView.setText(newsItem.getTitle());
        newsSectionTextView.setText(newsItem.getSectionName());
        newsPublicationDateTextView.setText(convertDateFormat(newsItem.getPublicationDate()));
        newsPublicationTimeTextView.setText(convertTimeFormat(newsItem.getPublicationDate()));
    }

    // Convert json DateTime to Date and Time
    public String convertDateFormat(String input){
        input = input.substring(0, input.length() - 1);
        String oldFormat = "yyyy-MM-dd'T'HH:mm:ss";
        String newFormat = "dd/MM/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(oldFormat);
        SimpleDateFormat outputFormat = new SimpleDateFormat(newFormat);
        Date date = null;
        String output = "";
        try {
            date = inputFormat.parse(input);
            output = outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "DateTime parse exception: " + e);
        }
        return output;
    }

    public String convertTimeFormat(String input){
        input = input.substring(0, input.length() - 1);
        String oldFormat = "yyyy-MM-dd'T'HH:mm:ss";
        String newFormat = "HH:mm";
        SimpleDateFormat inputFormat = new SimpleDateFormat(oldFormat);
        SimpleDateFormat outputFormat = new SimpleDateFormat(newFormat);
        Date date = null;
        String output = "";
        try {
            date = inputFormat.parse(input);
            output = outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "DateTime parse exception: " + e);
        }
        return output;
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mNews.size();
    }

    // Adds new items to mNews and refreshes the layout
    public void addAll(List<NewsItem> newsItemList) {
        mNews.clear();
        mNews.addAll(newsItemList);
        notifyDataSetChanged();
    }

    // Clears mNews
    public void clearAll() {
        mNews.clear();
        notifyDataSetChanged();
    }



}
