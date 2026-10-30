package com.github.libretube.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import android.widget.RemoteViews
import com.github.libretube.R
import com.github.libretube.db.DatabaseHolder
import com.github.libretube.ui.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** ABBADI glass home-screen widget: logo, continue watching, play/pause. */
class AbbadiWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val last = runCatching { DatabaseHolder.Database.watchHistoryDao().getAll().lastOrNull() }.getOrNull()
                ids.forEach { manager.updateAppWidget(it, build(context, last?.title, last?.uploader, last?.videoId)) }
            } finally { pending.finish() }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PLAY_PAUSE) {
            val am = context.getSystemService(AudioManager::class.java)
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
            return
        }
        super.onReceive(context, intent)
    }

    private fun build(c: Context, title: String?, sub: String?, videoId: String?): RemoteViews {
        val v = RemoteViews(c.packageName, R.layout.widget_abbadi)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        v.setTextViewText(R.id.widget_title, title ?: c.getString(R.string.abbadi_widget_empty))
        v.setTextViewText(R.id.widget_sub, sub ?: c.getString(R.string.abbadi_widget_continue))
        val open = if (videoId != null)
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")).setPackage(c.packageName)
        else Intent(c, MainActivity::class.java)
        v.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(c, 1, open, flags))
        v.setOnClickPendingIntent(R.id.widget_logo, PendingIntent.getActivity(c, 2, Intent(c, MainActivity::class.java), flags))
        v.setOnClickPendingIntent(R.id.widget_play,
            PendingIntent.getBroadcast(c, 3, Intent(ACTION_PLAY_PAUSE).setComponent(ComponentName(c, AbbadiWidget::class.java)), flags))
        return v
    }

    companion object { const val ACTION_PLAY_PAUSE = "com.abbadi.tube.widget.PLAY_PAUSE" }
}
