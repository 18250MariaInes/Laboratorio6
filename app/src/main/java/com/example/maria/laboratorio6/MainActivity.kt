package com.example.maria.laboratorio6

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.net.Uri
import android.view.View
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import java.util.*
import com.example.maria.laboratorio6.MusicService.MusicBinder
import android.os.IBinder
import android.content.Context.BIND_AUTO_CREATE
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Menu
import android.view.MenuItem



//import android.widget.MediaController.MediaPlayerControl

//main de programa
//VERSION FINAL
class MainActivity : AppCompatActivity(), MediaPlayerControl {


    private var songList: ArrayList<Song> = ArrayList()
    private var songView: ListView? = null
    private var controller: MusicController? = null
    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false
    private var paused = false
    private var playbackPaused= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songView = findViewById(R.id.song_list)

        //songList = ArrayList()
        getSongList()
        Collections.sort(songList, object : Comparator<Song> {
            override fun compare(a: Song, b: Song): Int {
                return a.getTitle().compareTo(b.getTitle())
            }
        })
        val songAdt = SongAdapter(this, songList)
        songView!!.setAdapter(songAdt)
        setController()

    }

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
            musicSrv!!.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    fun getSongList() {
        //query external audio
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)
        //iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext())
        }
    }

    private fun setController() {
        controller = MusicController(this)
        //set previous and next button listeners
        controller!!.setPrevNextListeners(
            { playNext() },
            { playPrev() })
        //set and show
        controller!!.setMediaPlayer(this)
        controller!!.setAnchorView(findViewById(R.id.song_list))
        controller!!.isEnabled = true
    }

    private fun playNext() {
        musicSrv?.playNext()
        controller?.show(0)
    }

    private fun playPrev() {
        musicSrv?.playPrev()
        /*if (playbackPaused) {
            setController()
            playbackPaused = false
        }*/
        controller!!.show(0)
    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    fun songPicked(view: View) {

        musicSrv?.setSong(Integer.parseInt(view.tag.toString()))
        musicSrv?.playSong()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        //musicSrv!!.go()
        controller!!.show(0)

    }


    override fun pause() {
        //playbackPaused=true
        musicSrv!!.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun seekTo(pos: Int) {
        musicSrv?.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        return if(musicSrv!=null && musicBound && musicSrv!!.isPng())
            musicSrv!!.getPosn()
        else 0
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun start() {
        musicSrv?.go()
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        return if(musicSrv!=null && musicBound && musicSrv!!.isPng())
            musicSrv!!.getDur()
        else 0
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound)
            return musicSrv!!.isPng()
        return false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller?.hide()
        super.onStop()
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }
}
