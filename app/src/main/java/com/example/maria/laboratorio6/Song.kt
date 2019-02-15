package com.example.maria.laboratorio6

import android.view.GestureDetector

class Song {
    private var id: Long = 0
    private var title: String = " "
    private var artist: String =" "

    constructor (songID: Long, songTitle: String, songArtist: String){
        this.id = songID
        this.title = songTitle
        this.artist = songArtist
    }

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