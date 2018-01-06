package com.example.shubham.music;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<Song> {
    private int mintues;
    private int seconds;
    private ArrayList<Song> songs;
    private int timeInSeconds;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        super(c, 0, theSongs);
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.songs, parent, false);
        }
        TextView artistView = (TextView) listItemView.findViewById(R.id.song_artist);
        TextView durationView = (TextView) listItemView.findViewById(R.id.duration);
        Song currSong = (Song) getItem(position);
        ((TextView) listItemView.findViewById(R.id.song_title)).setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        durationView.setText(convertTime(currSong.getDuration()));
        ((GradientDrawable) durationView.getBackground()).setColor(getMagnitudeColor());
        listItemView.setTag(Integer.valueOf(position));
        return listItemView;
    }

    public int getMagnitudeColor() {
        int magnitudeColorResourceId;
        switch (this.mintues) {
            case R.styleable.View_android_theme /*0*/:
                magnitudeColorResourceId = R.color.less_than_1;
                break;
            case R.styleable.View_android_focusable /*1*/:
                magnitudeColorResourceId = R.color.less_than_2;
                break;
            case R.styleable.View_paddingStart /*2*/:
                magnitudeColorResourceId = R.color.less_than_3;
                break;
            case R.styleable.View_paddingEnd /*3*/:
                magnitudeColorResourceId = R.color.less_than_4;
                break;
            case R.styleable.View_theme /*4*/:
                magnitudeColorResourceId = R.color.less_than_5;
                break;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                magnitudeColorResourceId = R.color.less_than_6;
                break;
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                magnitudeColorResourceId = R.color.less_than_7;
                break;
            case R.styleable.Toolbar_contentInsetLeft /*7*/:
                magnitudeColorResourceId = R.color.less_than_8;
                break;
            default:
                magnitudeColorResourceId = R.color.more_than_8;
                break;
        }
        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
    }

    String convertTime(int timeInMillisecond) {
        this.timeInSeconds = timeInMillisecond / 1000;
        this.mintues = this.timeInSeconds / 60;
        this.seconds = this.timeInSeconds % 60;
        return Integer.toString(this.mintues) + ":" + String.format("%02d", new Object[]{Integer.valueOf(this.seconds)});
    }
}
