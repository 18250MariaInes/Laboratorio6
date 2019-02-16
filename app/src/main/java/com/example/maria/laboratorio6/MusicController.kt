package com.example.maria.laboratorio6


import android.content.Context
import android.widget.MediaController


//controlador de musica que se instancia en MainActivity

class MusicController(c: Context) : MediaController(c) {

    override fun hide() {}

}