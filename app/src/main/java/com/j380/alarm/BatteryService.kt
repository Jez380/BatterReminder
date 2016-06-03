package com.j380.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import com.j380.alarm.view.AlertView

class BatteryService : Service() {

    private val LOW_BATTERY_LEVEL = 25f

    private lateinit var alertView: AlertView

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        setAlarm()
        alertView = AlertView(this)
        alertView.initView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkBattery()
        return START_STICKY
    }

    private fun setAlarm() {
        val alarmManager = applicationContext.getSystemService(
                Context.ALARM_SERVICE) as AlarmManager;
        val lIntent = Intent(getString(R.string.checkBatteryIntentAction));
        val lPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, lIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, lPendingIntent);
    }

    private fun checkBattery() {
        val batteryStatus = getBatteryStatusIntent()

        if(isNotPluggedIn(batteryStatus)) {
            val batteryLevel = convertBatteryLevelToPercent(getRawBatteryLevel(batteryStatus),
                    getBatteryScale(batteryStatus))
            showViewIfBatteryLevelIsLow(batteryLevel)
        }
    }

    private fun getBatteryStatusIntent(): Intent {
        val lFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return applicationContext.registerReceiver(null, lFilter)
    }

    private fun getRawBatteryLevel(batteryStatus: Intent) = batteryStatus.getIntExtra(
            BatteryManager.EXTRA_LEVEL, -1)

    private fun getBatteryScale(batteryStatus: Intent) = batteryStatus.getIntExtra(
            BatteryManager.EXTRA_SCALE, -1)

    private fun convertBatteryLevelToPercent(rawLevel: Int, scale: Int): Float {
        return rawLevel / scale.toFloat() * 100;
    }

    private fun getTypeOfChargePlug(batteryStatus: Intent) = batteryStatus.getIntExtra(
            BatteryManager.EXTRA_PLUGGED, -1)

    private fun isNotPluggedIn(batteryStatus: Intent) = !isPluggedInUsbCharger(batteryStatus) &&
            !isPluggedInAcCharger(batteryStatus)

    private fun isPluggedInUsbCharger(batteryStatus: Intent): Boolean {
        return getTypeOfChargePlug(batteryStatus) == BatteryManager.BATTERY_PLUGGED_USB
    }

    private fun isPluggedInAcCharger(batteryStatus: Intent): Boolean {
        return getTypeOfChargePlug(batteryStatus) == BatteryManager.BATTERY_PLUGGED_AC
    }

    private fun showViewIfBatteryLevelIsLow(batteryLevel: Float) {
        if (batteryLevel <= LOW_BATTERY_LEVEL ) {
            alertView.show(batteryLevel);
        }
    }
}