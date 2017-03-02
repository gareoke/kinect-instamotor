package com.instamotor.kinect

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

import com.instamotor.kinect.operators.OperatorBroadcastRegister
import com.instamotor.kinect.utils.NetworkUtil
import org.jetbrains.anko.*

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.exceptions.OnErrorThrowable
import rx.schedulers.Schedulers
import timber.log.Timber

/*
Develop a function in Kotlin that transforms provided rx.Observable in such way so that subscription will occur only when device has Internet connection. If java.io.IOException occurred in the stream (let's treat it as there is no Internet connection), try again with the same requirements. To check connectivity status use android.net.ConnectivityManager.
Present a demo stand where is an UI element that indicates connectivity status and an UI element to see a data from the stream. For layouts use Anko. Let source to be some kind of infinity number emitter, for example Observable.interval(1, TimeUnit.SECONDS) Reviewer will manupulate connectivity status using airplane mode. Throw IOException when status is "no internet" for demonstration purpose.
*/

class MainActivity : AppCompatActivity() {
    val ID_CONN_STATUS = 1
    val ID_DATA_STREAM = 2

    internal var dataSubscription: Subscription? = null
    internal var broadcastSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            padding = dip(30)
            textView {
                id = ID_CONN_STATUS
                hint = "Connection Status"
            }.lparams {
                gravity = Gravity.CENTER
            }
            textView {
                id = ID_DATA_STREAM
                hint = "Data Stream"
            }.lparams {
                gravity = Gravity.CENTER
            }
        }

        solution(NetworkUtil.getConnectionStatus(this));
    }

    /*
    * solution to the provided problem
    * @param connected - the initial connection state
     */
    private fun solution(connected: Boolean) {
        find<TextView>(ID_CONN_STATUS).text = if (connected) resources.getString(R.string.conn_status_connected) else resources.getString(R.string.conn_status_disconnected)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        val isConnected = AtomicBoolean(connected)
        val counter = AtomicLong()

        val broadcastObservable = Observable.create(OperatorBroadcastRegister(this, filter))
        broadcastSubscription = broadcastObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("network change -- START");
                    val conn = NetworkUtil.getConnectionStatus(this@MainActivity)
                    find<TextView>(ID_CONN_STATUS).text = if (conn) resources.getString(R.string.conn_status_connected) else resources.getString(R.string.conn_status_disconnected)
                    isConnected.set(conn)
                    if (conn) {
                        if (dataSubscription == null)
                            dataSubscription = resumeDataStream(isConnected, counter)
                    } else {
                        if (dataSubscription != null)
                            dataSubscription!!.unsubscribe()
                        dataSubscription = null
                    }
                }
    }

    /*
    * solution to the provided problem
    * @param isConnected - the flag to pause and resume the counter
    * @param counter - the counter to increment only when we are have internet connection
     */
    internal fun resumeDataStream(isConnected: AtomicBoolean, counter: AtomicLong): Subscription {
        return Observable.interval(1, TimeUnit.SECONDS)
                .map {
                    if (isConnected.get()) {
                        counter.andIncrement
                    } else {
                        throw OnErrorThrowable.from(IOException())
                    }
                }
                .onErrorReturn {
                    Timber.e("onErrorReturn -- START");
                    counter.get()
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { aLong -> find<TextView>(ID_DATA_STREAM).text = aLong.toString() }
    }

    private val isCurrentlyOnMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    override fun onDestroy() {
        super.onDestroy()
        destoryAllSubscriptions()
    }

    private fun destoryAllSubscriptions() {
        if (dataSubscription != null) {
            dataSubscription!!.unsubscribe()
            dataSubscription = null
        }

        if (broadcastSubscription != null) {
            broadcastSubscription!!.unsubscribe()
            broadcastSubscription = null
        }
    }
}
