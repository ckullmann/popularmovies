package de.christiankullmann.popularmovies;

import android.net.Uri;

/**
 * Created by chris on 18.07.16.
 */
public final class Movie {

    private static final String BASEURL = "http://image.tmdb.org/t/p/";
    private static final String DEFAULT_SIZE = "w185";


    private String title;
    private String imageUrl;

    public Movie(String imageUrl, String title) {
        this.imageUrl = Uri.parse(BASEURL).buildUpon().appendEncodedPath(DEFAULT_SIZE).appendEncodedPath(imageUrl).build().toString();
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }
}
