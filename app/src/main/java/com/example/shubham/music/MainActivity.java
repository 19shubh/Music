package com.example.shubham.music;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements OnPreparedListener, OnSeekBarChangeListener {
    private boolean background = false;
    private RelativeLayout buttonPannel;
    private TextView emptyTitleText;
    private FloatingActionButton fastForward;
    private boolean firstClick = true;
    private Handler handler = new Handler();
    private boolean isPaused = false;
    private Toolbar musicBar;
    private boolean musicBound = false;
    private MusicService musicSrv;
    private ServiceConnection musicConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            MainActivity.this.musicBound = false;
        }
    };
    private boolean result=false;
    private boolean paused = false;
    private Intent playIntent;
    private FloatingActionButton playNext;
    private FloatingActionButton playPause;
    private FloatingActionButton playPrevious;
    private boolean playbackPaused = false;
    private FloatingActionButton rewind;
    Runnable run = new Runnable() {
        public void run() {
            MainActivity.this.seekUpdation();
        }
    };
    private SeekBar seekBar;
    private boolean shuffle = false;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;
    public TextView songName;
    private ListView songView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        hide();
        result = checkPermission();
        if(result)
        {
            getSongList();
            this.seekBar.setOnSeekBarChangeListener(this);
            if (this.songList.isEmpty()) {
                this.emptyTitleText.setText(getString(R.string.nothing_to_show));
            } else {
                this.songView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        if (MainActivity.this.firstClick) {
                            MainActivity.this.show();
                            MainActivity.this.firstClick = false;
                        }
                        MainActivity.this.playPause.setImageResource(R.drawable.ic_action_pause);
                        MainActivity.this.musicSrv.setSong(position);
                        MainActivity.this.musicSrv.playSong();
                        MainActivity.this.songName.setText(MainActivity.this.musicSrv.currSongTitle());
                        MainActivity.this.seekBar.setMax(MainActivity.this.musicSrv.getSongDuration());
                        MainActivity.this.seekUpdation();
                        MainActivity.this.isPaused = false;
                        MainActivity.this.playbackPaused = false;
                    }
                });
                this.playPause.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (MainActivity.this.isPaused || !MainActivity.this.musicSrv.isPlaying()) {
                            MainActivity.this.playbackPaused = false;
                            MainActivity.this.musicSrv.go();
                            MainActivity.this.playPause.setImageResource(R.drawable.ic_action_pause);
                            MainActivity.this.isPaused = false;
                            return;
                        }
                        MainActivity.this.playbackPaused = true;
                        MainActivity.this.musicSrv.pausePlayer();
                        MainActivity.this.playPause.setImageResource(R.drawable.ic_action_play);
                        MainActivity.this.isPaused = true;
                    }
                });
                this.playPrevious.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.playPrev();
                        MainActivity.this.songView.setSelection(MainActivity.this.musicSrv.currSongID());
                        MainActivity.this.isPaused = false;
                        MainActivity.this.playbackPaused = false;
                        MainActivity.this.playPause.setImageResource(R.drawable.ic_action_pause);
                        MainActivity.this.songName.setText(MainActivity.this.musicSrv.currSongTitle());
                        MainActivity.this.seekBar.setMax(MainActivity.this.musicSrv.getSongDuration());
                        MainActivity.this.seekUpdation();
                    }
                });
                this.playNext.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.playNext();
                        MainActivity.this.songView.setSelection(MainActivity.this.musicSrv.currSongID());
                        MainActivity.this.isPaused = false;
                        MainActivity.this.playbackPaused = false;
                        MainActivity.this.playPause.setImageResource(R.drawable.ic_action_pause);
                        MainActivity.this.songName.setText(MainActivity.this.musicSrv.currSongTitle());
                        MainActivity.this.seekBar.setMax(MainActivity.this.musicSrv.getSongDuration());
                        MainActivity.this.seekUpdation();
                    }
                });
                this.rewind.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.musicSrv.seek(MainActivity.this.musicSrv.getPosition() - 5000);
                    }
                });
                this.fastForward.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MainActivity.this.musicSrv.seek(MainActivity.this.musicSrv.getPosition() + 5000);
                    }
                });
                Collections.sort(this.songList, new Comparator<Song>() {
                    public int compare(Song a, Song b) {
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });
                this.emptyTitleText.setVisibility(View.GONE);
            }
        }
    }

    private boolean checkPermission() {

        if (VERSION.SDK_INT >= 23 && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
            return false;
        }
        else
            return true;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.
                    getSongList();
                    this.songAdapter.notifyDataSetChanged();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    this.emptyTitleText.setText(getString(R.string.nothing_to_show));
                }
                return;
            }
        }
    }

    void init() {
        this.songName = (TextView) findViewById(R.id.song_name);
        this.handler = new Handler();
        this.seekBar = (SeekBar) findViewById(R.id.seekbar);
        this.buttonPannel = (RelativeLayout) findViewById(R.id.button_panel);
        this.musicBar = (Toolbar) findViewById(R.id.toolbar);
        this.playPause = (FloatingActionButton) findViewById(R.id.playPause);
        this.fastForward = (FloatingActionButton) findViewById(R.id.fast_forward);
        this.playNext = (FloatingActionButton) findViewById(R.id.next);
        this.rewind = (FloatingActionButton) findViewById(R.id.rewind);
        this.playPrevious = (FloatingActionButton) findViewById(R.id.previous);
        this.emptyTitleText = (TextView) findViewById(R.id.empty_title_text);
        this.songView = (ListView) findViewById(R.id.song_list);
        this.songList = new ArrayList();
        this.songAdapter = new SongAdapter(this, this.songList);
        this.songView.setAdapter(this.songAdapter);
    }

    public void seekUpdation() {
        this.seekBar.setProgress(this.musicSrv.getPosition());
        this.handler.postDelayed(this.run, 1000);
    }

    void hide() {
        this.songName.setVisibility(View.GONE);
        this.seekBar.setVisibility(View.GONE);
        this.buttonPannel.setVisibility(View.GONE);
        this.musicBar.setVisibility(View.GONE);
    }

    void show() {
        this.songName.setVisibility(View.VISIBLE);
        this.seekBar.setVisibility(View.VISIBLE);
        this.buttonPannel.setVisibility(View.VISIBLE);
        this.musicBar.setVisibility(View.VISIBLE);
    }

    private void playNext() {
        this.musicSrv.playNext();
        if (this.playbackPaused) {
            this.playbackPaused = false;
        }
    }

    private void playPrev() {
        this.musicSrv.playPrev();
        if (this.playbackPaused) {
            this.playbackPaused = false;
        }
    }

    protected void onPause() {
        super.onPause();
        this.background = true;
        this.paused = true;
    }

    protected void onResume() {
        super.onResume();
        if (this.background && result) {
            this.songName.setText(this.musicSrv.currSongTitle());
            this.seekBar.setMax(this.musicSrv.getSongDuration());
            seekUpdation();
        }
        if (this.paused && result) {
            this.paused = false;
        }
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onStart() {
        super.onStart();
        if (this.playIntent == null) {
            this.playIntent = new Intent(this,MusicService.class);
            bindService(this.playIntent, this.musicConnection, Context.BIND_AUTO_CREATE);
            startService(this.playIntent);
        }
    }

    protected void onDestroy() {
        if (this.musicBound) {
            unbindService(this.musicConnection);
        }
        stopService(this.playIntent);
        this.musicSrv = null;
        this.handler.removeCallbacks(this.run);
        super.onDestroy();
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onPrepared(MediaPlayer mp) {
        mp.start();
        this.isPaused = false;
        this.playbackPaused = false;
        this.songName.setText(this.musicSrv.currSongTitle());
        this.seekBar.setMax(this.musicSrv.getSongDuration());
        seekUpdation();
    }

    public void getSongList() {
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int thisDuration = musicCursor.getInt(durationColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist,thisDuration));
            } while (musicCursor.moveToNext());
            Log.w("Number Of Songs","NO="+songList.size());
            musicCursor.close();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                if (!this.shuffle)
                {
                    z = true;
                }
                this.shuffle = z;
                this.musicSrv.setShuffle();
                if (!this.shuffle) {
                    item.setIcon(R.drawable.shuffle_off);
                    break;
                }
                item.setIcon(R.drawable.shuffle_on);
                break;
            case R.id.action_end:
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
        try {
            if (this.musicSrv.isPlaying() || this.musicSrv != null) {
                if (fromUser) {
                    this.musicSrv.seek(progress);
                } else if (!fromUser && this.musicSrv.isCompleted()) {
                    this.songName.setText(this.musicSrv.currSongTitle());
                    this.musicSrv.setOnCompletion(false);
                    this.seekBar.setMax(this.musicSrv.getSongDuration());
                    seekUpdation();
                    this.songView.setSelection(this.musicSrv.currSongID());
                }
            } else if (this.musicSrv == null) {
                Toast.makeText(getApplicationContext(), "Media is not running", 0).show();
                this.seekBar.setProgress(0);
            }
        } catch (Exception e) {
            Log.e("seek bar", BuildConfig.FLAVOR + e);
            this.seekBar.setEnabled(false);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
