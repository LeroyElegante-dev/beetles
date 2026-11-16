package com.example.beetles.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.beetles.MainActivity
import com.example.beetles.R
import com.example.beetles.data.GoldRateRepository
import com.example.beetles.data.GoldRateStore
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GoldRateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            showCached(context, appWidgetManager, id)
        }
        val pendingResult = goAsync()
        thread {
            try {
                val store = GoldRateStore(context)
                val repository = GoldRateRepository(store)
                runCatching { runBlocking { repository.refresh() } }
                appWidgetIds.forEach { id ->
                    showCached(context, appWidgetManager, id)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, GoldRateWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    private fun showCached(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val store = GoldRateStore(context)
        val rate = store.getRate()
        val date = store.getDate()
        val rateText = if (rate.isNaN() || rate <= 0.0) {
            context.getString(R.string.widget_gold_loading)
        } else {
            context.getString(R.string.widget_gold_rate_value, rate)
        }
        val dateText = if (date.isBlank()) {
            context.getString(R.string.widget_gold_subtitle)
        } else {
            context.getString(R.string.widget_gold_date, date)
        }

        val views = RemoteViews(context.packageName, R.layout.widget_gold_rate).apply {
            setTextViewText(R.id.tvWidgetGoldRate, rateText)
            setTextViewText(R.id.tvWidgetGoldDate, dateText)
            setOnClickPendingIntent(R.id.widgetRoot, openAppIntent(context))
            setOnClickPendingIntent(R.id.btnWidgetRefresh, refreshIntent(context))
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun refreshIntent(context: Context): PendingIntent {
        val intent = Intent(context, GoldRateWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.example.beetles.widget.REFRESH_GOLD"

        fun requestUpdate(context: Context) {
            val intent = Intent(context, GoldRateWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, GoldRateWidgetProvider::class.java))
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
