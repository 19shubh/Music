package com.example.shubham.music;

import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener {
    private static final int NOTIFY_ID = 1;
    private final IBinder musicBind = new MusicBinder();
    private boolean onCompletion = false;
    private MediaPlayer player;
    private Random rand;
    private boolean shuffle = false;
    private int songDuration;
    private int songPosition;
    private String songTitle = "";
    private ArrayList<Song> songs;

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        this.rand = new Random();
        this.songPosition = 0;
        this.player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        this.player.setWakeMode(getApplicationContext(), 1);
        this.player.setAudioStreamType(3);
        this.player.setOnPreparedListener(this);
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);
    }

    public void playSong() {
        this.player.reset();
        Song playSong = this.songs.get(this.songPosition);
        long currSong = playSong.getID();
        this.songTitle = playSong.getTitle();
        this.songDuration = playSong.getDuration();
        try {
            this.player.setDataSource(getApplicationContext(), ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, currSong));
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        this.player.prepareAsync();
    }

    public void setShuffle() {
        if (this.shuffle) {
            this.shuffle = false;
        } else {
            this.shuffle = true;
        }
    }

    public void setList(ArrayList<Song> theSongs) {
        this.songs = theSongs;
    }

    public void setSong(int songIndex) {
        this.songPosition = songIndex;
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return this.musicBind;
    }

    public void onCompletion(MediaPlayer mp) {
        if (this.player.getCurrentPosition() > 0) {
            mp.reset();
            this.onCompletion = true;
            playNext();
            this.songTitle = (this.songs.get(this.songPosition)).getTitle();
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public void onDestroy() {
        this.player.stop();
        this.player.release();
        stopForeground(true);
    }

    @RequiresApi(api = 16)
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(67108864);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, 134217728);
        Builder builder = new Builder(this);
        builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(this.songTitle).setOngoing(true).setContentTitle("Playing").setContentText(this.songTitle);
        startForeground(NOTIFY_ID, builder.build());
    }

    public void playPrev() {
        this.songPosition--;
        if (this.songPosition < 0) {
            this.songPosition = this.songs.size() - 1;
        }
        playSong();
    }

    public void playNext() {
        if (this.shuffle) {
            int newSong = this.songPosition;
            while (newSong == this.songPosition) {
                newSong = this.rand.nextInt(this.songs.size());
            }
            this.songPosition = newSong;
        } else {
            this.songPosition += NOTIFY_ID;
            if (this.songPosition >= this.songs.size()) {
                this.songPosition = 0;
            }
        }
        playSong();
    }

    public void setOnCompletion(boolean ans) {
        this.onCompletion = ans;
    }

    public boolean isCompleted() {
        return this.onCompletion;
    }

    public int currSongID() {
        return this.songPosition;
    }

    public int getSongDuration() {
        return this.songDuration;
    }

    public String currSongTitle() {
        return this.songTitle;
    }

    public int getPosition() {
        return this.player.getCurrentPosition();
    }

    public int getDuration() {
        return this.player.getDuration();
    }

    public boolean isPlaying() {
        return this.player.isPlaying();
    }

    public void pausePlayer() {
        this.player.pause();
    }

    public void seek(int position) {
        this.player.seekTo(position);
    }

    public void go() {
        this.player.start();
    }
}
