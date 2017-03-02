package com.instamotor.kinect.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.TextView

import com.instamotor.kinect.R

/**
 * Created by Gareoke on 2/28/17.
 */

object NetworkUtil {
    /*
    * Gets the current connection status
    * @return boolean true if online, otherwise false
     */
    fun getConnectionStatus(context: Context): Boolean {
        var connected = false
        try {
            val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            connected = networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            connected = false
        }
        return connected
    }
}
