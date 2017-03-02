package com.instamotor.kinect.operators

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import rx.Observable
import rx.Subscriber
import rx.subscriptions.Subscriptions

/*
* This is how pre 1.0 did it, which I enjoyed using much more than RxBroadcast https://github.com/cantrowitz/RxBroadcast.git
 */
class OperatorBroadcastRegister(private val context: Context, private val intentFilter: IntentFilter) : Observable.OnSubscribe<Intent> {

    override fun call(subscriber: Subscriber<in Intent>) {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                subscriber.onNext(intent)
            }
        }

        val subscription = Subscriptions.create { context.unregisterReceiver(broadcastReceiver) }

        subscriber.add(subscription)
        context.registerReceiver(broadcastReceiver, intentFilter)

    }
}