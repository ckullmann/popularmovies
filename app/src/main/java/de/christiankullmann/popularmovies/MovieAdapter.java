package de.christiankullmann.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Christian Kullmann on 18.07.16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, List<Movie> movies) {

        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie =  getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_layout, parent, false);
        }
        ImageView iconView = (ImageView) convertView.findViewById(R.id.grid_item_image);
//        iconView.setImageResource(R.mipmap.ic_launcher);
        Picasso.with(getContext()).load(movie.getImageUrl()).into(iconView);
        TextView textView = (TextView) convertView.findViewById(R.id.grid_item_text);
        textView.setText(movie.getTitle());
        return convertView;
    }
}
