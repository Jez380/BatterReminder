package com.j380.alarm.presenter

import android.content.Context
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.TextView
import com.j380.alarm.R


class AlertViewPresenterImpl(val context: Context, val audioManager: AudioManager,
        val windowManager: WindowManager, val inflater: LayoutInflater) : AlertViewPresenter {

    private lateinit var player: MediaPlayer
    private val view = inflater.inflate(R.layout.layout_alert_notification, null)
    private val okButton = view.findViewById(R.id.okBtn) as Button
    private val remainingTv = view.findViewById(R.id.remainingTv) as TextView

    private val lParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_SYSTEM_ALERT,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

    override fun initView() {
        lParams.gravity = Gravity.CENTER
        okButton.setOnClickListener({
            hideAlert()
        })
    }

    override fun hideAlert() {
        windowManager.removeViewImmediate(view)
    }

    override fun showLowBatteryAlert(batteryLevel: Int) {
        remainingTv.text = String.format(context.getString(R.string.remain), batteryLevel);
        windowManager.addView(view, lParams)
        playAudio()
    }

    private fun setPlayerListeners() {
        player.setOnSeekCompleteListener({
            mediaPlayer: MediaPlayer ->
            mediaPlayer.stop()
        })


        player.setOnCompletionListener({
            mediaPlayer: MediaPlayer ->
            mediaPlayer.release()
        })
    }

    private fun playAudio() {
        prepareMediaPlayer()
        player.start()
    }

    private fun prepareMediaPlayer() {
        player = MediaPlayer.create(context, R.raw.low)
        setPlayerListeners()
        setVolumeToPlayer(player, getVolume())
    }

    private fun getVolume() = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

    private fun setVolumeToPlayer(player: MediaPlayer, volume: Int) {
        player.setVolume(volume.toFloat(), volume.toFloat())
    }
}