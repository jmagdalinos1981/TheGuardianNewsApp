package com.example.android.newsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news articles data from The Guardian API
 */

public class QueryUtils {

    /** Keys for JSON parsing */
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_RESULTS = "results";
    private static final String KEY_SECTION = "sectionName";
    private static final String KEY_DATE = "webPublicationDate";
    private static final String KEY_TITLE = "webTitle";
    private static final String KEY_WEB_URL = "webUrl";

    /** Tag for the log messages */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();


    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Returns {@link List<NewsItem>} from the given string URL.
     */
    public static List<NewsItem> fetchNewsData(String requestURL, Context context) {
        // Create Url object
        URL url = createURL(requestURL, context);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHTTPRequest(url, context);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Parse JSON string and create an {@ArrayList<BookItem>} object
        List<NewsItem> newsItems = extractNewsData(jsonResponse, context);

        return newsItems;
    }


    /**
     * Returns new URL object from the given string URL.
     */
    public static URL createURL(String stringURL, final Context context) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            //use a handler to create a toast on the UI thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Error Creating URL", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            Log.e(LOG_TAG, "Error Creating URL", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHTTPRequest(URL url, Context context) throws IOException {
        // If the url is empty, return early
        String jsonResponse = null;
        if (url == null) {
            return jsonResponse;
        }
        final Context mContext = context;
        HttpURLConnection urlConnection =null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200), then read the input stream and
            // parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            } else {
                Log.e(LOG_TAG,"Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            //use a handler to create a toast on the UI thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Problem retrieving the Guardian API JSON results", Toast
                            .LENGTH_SHORT)
                            .show();
                }
            });

            Log.e(LOG_TAG, "Problem retrieving the Guardian API JSON results", e);
        } finally {
            // Close connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            // Close stream
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Read input stream as received from API and convert to String.
     */
    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder streamOutput = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset
                    .forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                streamOutput.append(line);
                line = bufferedReader.readLine();
            }
        }
        return streamOutput.toString();
    }

    public static List<NewsItem> extractNewsData(String jsonResponse, Context context) {
        final Context mContext = context;
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty List<NewsItem>
        List<NewsItem> newsItems = new ArrayList<NewsItem>();

        // Try to parse the jsonResponse. If there's a problem with the way the JSON is
        // formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.

        try {
            // Build a list of News objects
            JSONObject baseJSONObject = new JSONObject(jsonResponse);
            JSONObject responseJSONObject = baseJSONObject.getJSONObject(KEY_RESPONSE);
            JSONArray newsResults = responseJSONObject.getJSONArray(KEY_RESULTS);

            // Variables for JSON parsing
            String section;
            String publicationDate;
            String title;
            String webUrl;

            for (int i = 0; i < newsResults.length(); i++ ) {
                JSONObject newsArticle = newsResults.getJSONObject(i);
                // Check if a sectionName exists
                if (newsArticle.has(KEY_SECTION)) {
                    section = newsArticle.getString(KEY_SECTION);
                } else section = null;

                // Check if a webPublicationDate exists
                if (newsArticle.has(KEY_DATE)) {
                    publicationDate = newsArticle.getString(KEY_DATE);
                } else publicationDate = null;

                // Check if a webTitle exists
                if (newsArticle.has(KEY_TITLE)) {
                    title = newsArticle.getString(KEY_TITLE);
                } else title = null;

                // Check if a sectionName exists
                if (newsArticle.has(KEY_WEB_URL)) {
                    webUrl = newsArticle.getString(KEY_WEB_URL);
                } else webUrl = null;

                // Create the NewsItem object and add it to the newsItems List.
                NewsItem newsItem = new NewsItem(title, section, webUrl, publicationDate);
                newsItems.add(newsItem);
            }

        } catch (JSONException e) {
            //use a handler to create a toast on the UI thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Problem parsing the Guardian JSON results", Toast
                            .LENGTH_SHORT)
                            .show();
                }
            });
            Log.e(LOG_TAG, "Problem parsing the Guardian JSON results", e);
        }
        return newsItems;
    }
}