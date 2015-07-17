package com.codepath.gridimagesearch.com.codepath.gridimagesearch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.codepath.gridimagesearch.com.codepath.gridimagesearch.com.codepath.gridimagesearch.adapters.ImageResultsAdapter;
import com.codepath.gridimagesearch.com.codepath.gridimagesearch.models.ImageResult;
import com.codepath.gridimagesearch.R;
import com.codepath.gridimagesearch.com.codepath.gridimagesearch.activities.InfiniteScrollListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity {
    private EditText etQuery;
    private GridView gvResults;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;

    /* width, height, tbUrl, title, url (important attributes)
    responseData -> results -> [x] -> tbUrl
    responseData -> results -> [x] -> title
    responseData -> results -> [x] -> url
    responseData -> results -> [x] -> width
    responseData -> results -> [x] -> height
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupViews();
        //Creates the data source
        imageResults = new ArrayList<ImageResult>();
        // Attaches the data source to an adapter
        aImageResults = new ImageResultsAdapter(this, imageResults);
        // Link the adapter to the adapter view (gridview)
        gvResults.setAdapter(aImageResults);
    }

    private void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        gvResults.setOnScrollListener(new InfiniteScrollListener() {

            @Override
            public  void onLoadMore(int page, int totalItemsCount) {
                customLoadMoreDataFromApi(false);
            }
        });
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Launch Image Display activity
                //Creating an intent
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                //Get the image result to display
                ImageResult result = imageResults.get(position);
                // Pass image result to the intent
                i.putExtra("result", result);
                //Laucnh the new activity
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    //This method will run anytime the "search button" is clicked. This is possible thanks to the onClick attribute in activity_search.xml
    public void onImageSearch(View v) {
        //Get the string from the EditText
        String query = etQuery.getText().toString();
        //Print the Text on the screen
        Toast.makeText(this, "Search for: " + query, Toast.LENGTH_SHORT).show();
        AsyncHttpClient client = new AsyncHttpClient();
        //http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=android&rsz=8
        String searchUrl = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + query + "&rsz=8";
        client.get(searchUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray imageResultsJson = null;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                    imageResults.clear(); //clear the existig images in case there is a new search
                    // When you make to the adapter, it does modify the underliying data auto
                    aImageResults.addAll(ImageResult.fromJSONAray(imageResultsJson));
                } catch (JSONException e) {
                    //TODO catch block
                    e.printStackTrace();
                }
                Log.i("INFO", imageResults.toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Creating intent
            Intent i = new Intent(SearchActivity.this, SettingsActivity.class);
            Toast.makeText(this, "About to start the filters activity", Toast.LENGTH_SHORT).show();
            String query = etQuery.getText().toString();
            String searchUrl = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + query + "&rsz=8";
            i.putExtra("searchUrl", searchUrl);
            //Launch the activity
            startActivity(i);
            Toast.makeText(this, "Starting it", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
