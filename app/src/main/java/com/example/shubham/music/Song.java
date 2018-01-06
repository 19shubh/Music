package com.example.shubham.music;

public class Song {
    private String artist;
    private int duration;
    private long id;
    private String title;

    public Song(long songID, String songTitle, String songArtist, int time) {
        this.id = songID;
        this.title = songTitle;
        this.artist = songArtist;
        this.duration = time;
    }

    public long getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public int getDuration() {
        return this.duration;
    }
}
