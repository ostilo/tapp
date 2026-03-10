package com.tapp.spinwheel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Home screen widget entry point for the spin wheel experience.
 *
 * This lives in the reusable `android-widget` library so that any host
 * application can expose a consistent widget by:
 *
 * - Depending on this library module.
 * - Declaring a `<receiver>` in its manifest that references this class.
 *
 * The provider itself stays generic: it simply looks up the app's launch
 * intent and forwards taps on the widget card to that activity.
 */
class SpinWheelAppWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    appWidgetIds.forEach { appWidgetId ->
      val views = RemoteViews(context.packageName, R.layout.widget_spin_wheel)

      val launchIntent = context.packageManager
        .getLaunchIntentForPackage(context.packageName)
        ?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        ?: return@forEach

      val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    val manager = AppWidgetManager.getInstance(context)
    val ids = manager.getAppWidgetIds(ComponentName(context, SpinWheelAppWidgetProvider::class.java))
    if (ids.isNotEmpty()) {
      onUpdate(context, manager, ids)
    }
  }
}

