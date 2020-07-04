package com.thesocialfeed.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object BitmapUtils {

    fun byteToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun bitmapToByte(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}