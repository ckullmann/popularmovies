package de.christiankullmann.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieOverviewFragment extends Fragment {
    private static final String LOG_TAG = MovieOverviewFragment.class.getSimpleName();

    private static final String IMAGEURL_BASE = "http://image.tmdb.org/t/p/";
    private static final String MOVIEDBURL_BASE = "https://api.themoviedb.org";
    private static final String POPULARMOVIES_BASE = "http://api.themoviedb.org/3/movie/popular?api_key=";
    private ArrayList<Movie> movies = new ArrayList<>();
    private MovieAdapter movieAdapter;

    public MovieOverviewFragment() {
//        movies = new String[]{"Movie 1", "Fight Club", "From Dusk till Dawn", "Deliverace", "Star Wars", "Hateful 8", "Leben und Sterben in Las Vegas"};
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        movieAdapter = new MovieAdapter(getActivity(), movies);
        gridView.setAdapter(movieAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_overview_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        try {
            movies = moviesTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private static final String SCHEME = "https";
        private static final String BASE = "api.themoviedb.org";///3/movie/popular?api_key=";
        private static final String SEARCH = "popular";
        private static final String APIKEY_QUERY_KEY = "api_key";


        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr;
            ArrayList<Movie> result = null;

            try {
                // Construct the URL for the MovieDB query
                // Possible parameters are avaiable at  API page, at
                // http://docs.themoviedb.apiary.io

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME);
                builder.authority(BASE);
                builder.appendPath("3");
                builder.appendPath("movie");
                builder.appendEncodedPath(SEARCH);
                builder.appendQueryParameter(APIKEY_QUERY_KEY, APPID.THEMOVIEDB_APPID);
                builder.build();
                String spec = builder.toString();
                Log.v(LOG_TAG, "Fetch Movies using [" + spec + "].");
                URL url = new URL(spec);

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG, moviesJsonStr);
                result = getMovieDataFromJsonString(moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } catch (Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                t.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
                return result;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            super.onPostExecute(result);
            if (result != null) {
                movieAdapter.clear();
                movieAdapter.addAll(result);
            }
        }

        private ArrayList<Movie> getMovieDataFromJsonString(String moviesJsonStr) throws JSONException {

            JSONObject main = new JSONObject(moviesJsonStr);
            JSONArray results = main.getJSONArray("results");
            ArrayList<Movie> result = new ArrayList<>();
            for (int index = 0; index < results.length(); index++) {
                JSONObject object = results.getJSONObject(index);
                Movie movie = new Movie(object.getString("poster_path"), object.getString("title"));
                result.add(movie);
            }
            return result;
        }
    }
}
