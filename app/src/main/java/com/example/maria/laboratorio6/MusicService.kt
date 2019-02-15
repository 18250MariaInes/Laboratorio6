package com.example.maria.laboratorio6

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

import android.app.PendingIntent
import android.util.Log

import java.util.*

import android.media.AudioManager
import android.os.PowerManager
import java.nio.file.Files.size

import android.content.ContentUris
import android.os.Binder
import android.provider.MediaStore
import android.view.View

import com.example.maria.laboratorio6.MusicService.MusicBinder

import java.nio.file.Files.size
import java.util.Collections.shuffle


class MusicService : Service(), MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private var songs: ArrayList<Song> = ArrayList()
    private var songTitle = ""
    private val NOTIFY_ID = 1
    private var player:MediaPlayer=MediaPlayer()
    private var songPosn:Int = 0
    private val musicBind = MusicBinder()
    private var shuffle = false
    private lateinit var rand:Random

    override fun onCreate() {
        //create the service
        super.onCreate()
        //initialize position
        songPosn = 0
        //random
        rand = Random()
        //create player
        //player = MediaPlayer()
        //initialize
        initMusicPlayer()
    }

    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
            PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    fun setList(theSongs: ArrayList<Song>) {
        songs = theSongs
    }

    inner class MusicBinder : Binder() {
        fun getservice(): MusicService {
            return MusicService()
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun playSong() {
        //play
        player.reset()
        //get song
        val playSong = songs[songPosn]
        //get title
        songTitle = playSong.getTitle()
        //get id
        val currSong = playSong.getID()
        //set uri
        val trackUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong
        )
        //set the data source
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }



    override fun onPrepared(mp: MediaPlayer?) {
        //start playback
        mp!!.start()
        //notification
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(
            this, 0,
            notIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = Notification.Builder(this)

        builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(songTitle)
        val not = builder.build()
        startForeground(NOTIFY_ID, not)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.v("MUSIC PLAYER", "Playback Error")
        mp?.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(player.getCurrentPosition()>0){
            mp?.reset()
            playNext()
        }
    }

    //private val MediaPlayer player

    override fun onBind(arg0: Intent): IBinder? {
        return musicBind
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev() {
        songPosn--
        if (songPosn < 0) songPosn = songs.size - 1
        playSong()
    }
    //skip to next
    fun playNext() {
        if (shuffle) {
            var newSong = songPosn
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size)
            }
            songPosn = newSong
        } else {
            songPosn++
            if (songPosn >= songs.size) songPosn = 0
        }
        playSong()
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    fun setShuffle() {
        shuffle = !shuffle
    }
}