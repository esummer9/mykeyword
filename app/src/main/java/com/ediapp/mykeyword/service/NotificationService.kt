package com.ediapp.mykeyword.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.MainActivity
import com.ediapp.mykeyword.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationService : Service() {

    private val notificationId = 1
    private val channelId = "quick_memo_channel_v4"
    private val actionAddMemo = "com.ediapp.mykeyword.ADD_MEMO"
    private val actionAddTime = "com.ediapp.mykeyword.ADD_TIME"
    private val actionAddPos = "com.ediapp.mykeyword.ADD_POS"
    private val actionDuplicateLastMemo = "com.ediapp.mykeyword.DUPLICATE_LAST_MEMO"
    private val keyTextReply = "key_text_reply"

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        dbHelper = DatabaseHelper.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            actionAddMemo -> handleActionAddMemo(intent)
            actionAddTime -> handleActionAddTime(intent)
            actionAddPos -> handleActionAddPos(intent)
            actionDuplicateLastMemo -> handleActionDuplicateLastMemo()
        }

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(notificationId, createNotification())
        }
        return START_STICKY
    }

    private fun handleActionAddMemo(intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        if (results != null) {
            val memoText = results.getCharSequence(keyTextReply)?.toString()
            if (!memoText.isNullOrBlank()) {
                addMemoToDatabase(memoText)
                recreateNotification()
            }
        }
    }

    private fun handleActionAddTime(intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val memoText = results?.getCharSequence(keyTextReply)?.toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())

        val finalMemoText = if (!memoText.isNullOrBlank()) {
            "$memoText - $currentTime"
        } else {
            currentTime
        }
        addMemoToDatabase(finalMemoText)
        recreateNotification()
    }

    // TODO: Implement location logic
    private fun handleActionAddPos(intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        if (results != null) {
            val memoText = results.getCharSequence(keyTextReply)?.toString()
            if (!memoText.isNullOrBlank()) {
                // For now, just adds memo. Later, add location.
                addMemoToDatabase("$memoText - (위치 정보)")
                recreateNotification()
            }
        }
    }

    private fun handleActionDuplicateLastMemo() {
        val lastMemo = dbHelper.getLatestMemo("notey")
        if (lastMemo != null) {
            dbHelper.addMemo(
                title = lastMemo.title ?: "",
                mean = lastMemo.meaning,
                url = lastMemo.url,
                address = lastMemo.address,
                regDate = System.currentTimeMillis()
            )
            recreateNotification()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.quick_memo)
            val descriptionText = "빠른 메모 알림"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // Delete old channels just in case
            notificationManager.deleteNotificationChannel("quick_memo_channel")
            notificationManager.deleteNotificationChannel("quick_memo_channel_v2")
            notificationManager.deleteNotificationChannel("quick_memo_channel_v3")
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        // Memo Action
        val remoteInputMemo = RemoteInput.Builder(keyTextReply).run {
            setLabel(getString(R.string.quick_memo))
            build()
        }
        val replyIntent = Intent(this, NotificationService::class.java).apply {
            action = actionAddMemo
        }
        val replyPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
        val actionMemo = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "메모추가",
            replyPendingIntent
        ).addRemoteInput(remoteInputMemo).build()

        // Time Action
        val remoteInputTime = RemoteInput.Builder(keyTextReply).run {
            setLabel("메모 (시간 자동 추가)")
            build()
        }
        val timeIntent = Intent(this, NotificationService::class.java).apply {
            action = actionAddTime
        }
        val timePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 1, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 1, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
        val actionTime = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "시간추가",
            timePendingIntent
        ).addRemoteInput(remoteInputTime).build()

        // Position Action
        val remoteInputPos = RemoteInput.Builder(keyTextReply).run {
            setLabel("메모 (위치 자동 추가)")
            build()
        }
        val posIntent = Intent(this, NotificationService::class.java).apply {
            action = actionAddPos
        }
        val posPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 2, posIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 2, posIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
        val actionPos = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "위치추가",
            posPendingIntent
        ).addRemoteInput(remoteInputPos).build()

        // Duplicate Last Memo Action
        val duplicateIntent = Intent(this, NotificationService::class.java).apply {
            action = actionDuplicateLastMemo
        }
        val duplicatePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 4, duplicateIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 4, duplicateIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }

        val actionDuplicate = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "전메모 복제",
            duplicatePendingIntent
        ).build()


        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            3, // Changed request code to avoid collision
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.memo)
            .setContentTitle(getString(R.string.quick_memo))
//            .setContentText("여기에 메모를 추가하세요.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(actionMemo)
            .addAction(actionTime)
//            .addAction(actionPos)
            .addAction(actionDuplicate)
            .build()
    }

    private fun addMemoToDatabase(memoText: String) {
        dbHelper.addMemo(title=memoText,
            mean = null, url = null, address = null,
            regDate = System.currentTimeMillis())
    }

    private fun recreateNotification() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, createNotification())
    }
}
