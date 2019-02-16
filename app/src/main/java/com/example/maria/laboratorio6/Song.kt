package com.example.maria.laboratorio6

import android.view.GestureDetector
//creador de instancias de cancion las cuales seran reproducidas
class Song {
    //atributos
    private var id: Long = 0
    private var title: String = " "
    private var artist: String =" "
//constructor
    constructor (songID: Long, songTitle: String, songArtist: String){
        this.id = songID
        this.title = songTitle
        this.artist = songArtist
    }
//getters de ID, title y artist
    fun getID(): Long {
        return id
    }

    fun getTitle(): String {
        return title
    }

    fun getArtist(): String {
        return artist
    }
}