package com.example.maria.laboratorio6

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

import android.util.Log

import java.util.*

import android.media.AudioManager
import android.os.PowerManager
import java.nio.file.Files.size

import android.content.ContentUris
import android.content.Context
import android.os.Binder
import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi


//clase que brinda el servicio del manejo y reproduccion de la musica
@Suppress("DEPRECATION")
class MusicService : Service(), MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    //variables a utilizar en el service
    lateinit var songs: ArrayList<Song>
    private var songTitle = ""
    private var songArtist = ""
    private val NOTIFY_ID = 1
    private var player:MediaPlayer=MediaPlayer()
    private var songPosn:Int = 0
    private val musicBind = MusicBinder()

    override fun onCreate() {
        //creador de servicio
        super.onCreate()
        initMusicPlayer()
    }

    fun initMusicPlayer() {
        //settear propiedades del player
        player.setWakeMode(
            applicationContext,
            PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //set listeners
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }
//settea las canciones guardadas en el telefono y un arraylist para mostrarla
    fun setList(theSongs: ArrayList<Song>) {
        songs = theSongs!!
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun playSong() {
        //reprodice la cancion
        player.reset()
        //consigue la cancion que desea al usuario
        val playSong = songs[songPosn]
        //get title
        songTitle = playSong.getTitle()
        //obtiene el artista
        songArtist=playSong.getArtist()
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



    @RequiresApi(Build.VERSION_CODES.O)//ya que usamos un diferente APK y necesita uno mas reciente
    override fun onPrepared(mp: MediaPlayer?) {
        //start playback
        mp!!.start()
        //notification, instancia de intent que al seleccionarlo manda a la app
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(
            this, 0,
            notIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        //notificacion
        val builder = Notification.Builder(this)
        builder.setContentIntent(pendInt)
        builder.setSmallIcon(R.drawable.play)
        builder.setTicker(songTitle)
        builder.setOngoing(true)
        builder.setContentTitle("Is playing "+songTitle+". Artist: "+songTitle)
        //se agrega un notificaton manager al ejemplo de Canvas
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sdl_notification_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, "SmartDeviceLink", importance)
        notificationChannel.enableLights(false)
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
        builder.setChannelId(channelId)
        val not = builder.build()
        startForeground(NOTIFY_ID,not)
    }
//catch de algun error en la reproduccion
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.v("MUSIC PLAYER", "Playback Error")
        mp!!.reset()
        return false
    }
//cuando termina la cancion, se reproduce la siguiente
    override fun onCompletion(mp: MediaPlayer?) {
        if(player.getCurrentPosition()>0){
            mp?.reset()
            playNext()
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return musicBind
    }
//obtiene posicion del player
    fun getPosn(): Int {
        return player.currentPosition
    }
//obtiene duracion de la cancion
    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }
//cuando le dan pause
    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun go() {
        player.start()

    }
//reproduce la cancion anterior
    fun playPrev() {
        songPosn--
        if (songPosn < 0) songPosn = songs.size - 1
        playSong()
    }
    //reproduce la siguiente cancion
    fun playNext() {
        songPosn++
        if(songPosn>=songs.size) songPosn=0
        playSong()
    }

    override fun onDestroy() {
        stopForeground(true)
    }
}