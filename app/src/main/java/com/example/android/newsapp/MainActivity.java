package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<List<NewsItem>> {

    /** Constant value for the book loader ID */
    private static final int BOOK_LOADER_ID = 1;

    /** Loader for background thread */
    private static LoaderManager loaderManager;

    /** Initial Query which will be combined with the user's input */
    private static final String API_INITIAL_QUERY = "https://content.guardianapis.com/search?";

    /** Adapter for the List */
    private static NewsAdapter newsAdapter;

    /** Textview displaying messages to the user */
    TextView messageTextView;

    /** Spinner displaying progress to user */
    ProgressBar progressBar;

    /** Refresh Layout */
    SwipeRefreshLayout swipeRefreshLayout;

    /** Network info to check for internet connection*/
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.main_screen_title));

        // Initialize TextView and Spinner
        messageTextView = (TextView) findViewById(R.id.message_textView);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Initialize SwipeRefreshLayout and assign Listener
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Show message for fetching data
            messageTextView.setText(getString(R.string.message_fetching));

            // Initialize Loader and News Adapter
            initializeLoaderAndAdapter();

        } else {
            // Hide progressBar
            progressBar.setVisibility(View.GONE);

            // Display error
            messageTextView.setText(getString(R.string.message_no_internet));
        }
    }

    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {
        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get search query preference
        String searchQuery = sharedPreferences.getString(getString(R.string
                .settings_search_query_key), getString(R.string.settings_search_query_default));

        // Get order by preference
        String orderBy = sharedPreferences.getString(getString(R.string
                .settings_order_by_list_key), getString(R.string.settings_order_by_list_default));

        // Build the Uri based on the preferences
        Uri baseIri = Uri.parse(API_INITIAL_QUERY);
        Uri.Builder uriBuilder = baseIri.buildUpon();

        uriBuilder.appendQueryParameter("q", searchQuery);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", "test");
        Log.v("MainActivity", "Uri: " + uriBuilder);

        // Create a new loader with the supplied Url
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {
        // If there is a valid list of {@link BookItem}s, then add them to the adapter's
        if (newsItems != null && !newsItems.isEmpty()) {
            newsAdapter.addAll(newsItems);
            // Hide loading indicator because the data has been loaded
            progressBar.setVisibility(View.GONE);
            // Hide message text
            messageTextView.setText("");

        } else {
            // Hide loading indicator because the data has been loaded
            progressBar.setVisibility(View.GONE);
            // Set message text to display "No articles found!"
            messageTextView.setText(getString(R.string.message_no_articles));
        }
        Log.v("MainActivity","Loader completed operation!");
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        newsAdapter.clearAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.menu_refresh) {
            refreshRecyclerView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeLoaderAndAdapter() {
        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(BOOK_LOADER_ID, null, this);

        // Lookup the recyclerView in activity layout
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Create adapter passing the data
        newsAdapter = new NewsAdapter(this, new ArrayList<NewsItem>());
        // Attach the adapter to the recyclerView to populate items
        recyclerView.setAdapter(newsAdapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void refreshRecyclerView() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();
        Log.v("MainActivity", "networkInfo: " + networkInfo);

        if (networkInfo != null && networkInfo.isConnected()) {
            // Show message text
            messageTextView.setText(getString(R.string.message_refreshing));
            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);

            // Check if newsAdapter is not null (which will happen if on launch there was no
            // connection)
            if (newsAdapter != null) {
                // Clear the adapter
                newsAdapter.clearAll();
            }
            if (loaderManager != null) {
                // Restart Loader
                loaderManager.restartLoader(BOOK_LOADER_ID, null, this);
                // Inform SwipeRefreshLayout that loading is complete so it can hide its progress bar
                swipeRefreshLayout.setRefreshing(false);
            } else {
                initializeLoaderAndAdapter();
                // Inform SwipeRefreshLayout that loading is complete so it can hide its progress bar
                swipeRefreshLayout.setRefreshing(false);
            }

        } else {
            // Hide progressBar
            progressBar.setVisibility(View.GONE);

            // Check if newsAdapter is not null (which will happen if on launch there was no
            // connection)
            if (newsAdapter != null) {
                // Clear the adapter
                newsAdapter.clearAll();
            }

            // Display error
            messageTextView.setText(getString(R.string.message_no_internet));
            swipeRefreshLayout.setRefreshing(false);
        }

    }
}