package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.model.GameEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class GameTrackerWidgetProvider : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                // Immediately provide initial view so launcher never encounters an empty or missing view
                val initialViews = RemoteViews(context.packageName, R.layout.widget_game_tracker)
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val mainPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                initialViews.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, initialViews)
            } catch (_: Exception) {}
            updateWidgetAsync(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == ACTION_FORCE_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GameTrackerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateWidgetAsync(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateWidgetAsync(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val gameDao = db.gameDao()
                val sessionDao = db.playSessionDao()

                val lastGame: GameEntity? = gameDao.getLastPlayedGame()

                // Calculate Monday start timestamp of the current week
                val mondayStartMillis = getMondayStartOfWeek()

                // Query sessions and played games of this week
                val sessionsThisWeek = sessionDao.getSessionsSince(mondayStartMillis)
                val gamesThisWeek = gameDao.getPlayedGamesDirect().filter {
                    (it.lastPlayedTimestamp ?: 0L) >= mondayStartMillis
                }

                // Map which days from Monday (0) to Sunday (6) had play activity
                val hasPlayedDay = BooleanArray(7)

                for (session in sessionsThisWeek) {
                    val cal = Calendar.getInstance(Locale.getDefault()).apply {
                        timeInMillis = session.endTime
                    }
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    val idx = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
                    if (idx in 0..6) {
                        hasPlayedDay[idx] = true
                    }
                }

                for (game in gamesThisWeek) {
                    val ts = game.lastPlayedTimestamp ?: continue
                    val cal = Calendar.getInstance(Locale.getDefault()).apply {
                        timeInMillis = ts
                    }
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    val idx = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
                    if (idx in 0..6) {
                        hasPlayedDay[idx] = true
                    }
                }

                // Determine today's day index (0 = Monday .. 6 = Sunday)
                val todayCal = Calendar.getInstance(Locale.getDefault())
                val todayDow = todayCal.get(Calendar.DAY_OF_WEEK)
                val todayIndex = if (todayDow == Calendar.SUNDAY) 6 else todayDow - Calendar.MONDAY

                // Load cover bitmap if last game exists
                val coverBitmap: Bitmap? = if (lastGame != null && lastGame.coverUrl.isNotBlank()) {
                    loadCoverBitmap(context, lastGame.coverUrl)
                } else null

                // Build RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_game_tracker)

                // 1. Update Weekly Dots
                val dotIds = intArrayOf(
                    R.id.widget_dot_mon,
                    R.id.widget_dot_tue,
                    R.id.widget_dot_wed,
                    R.id.widget_dot_thu,
                    R.id.widget_dot_fri,
                    R.id.widget_dot_sat,
                    R.id.widget_dot_sun
                )
                val dayTextIds = intArrayOf(
                    R.id.widget_day_mon,
                    R.id.widget_day_tue,
                    R.id.widget_day_wed,
                    R.id.widget_day_thu,
                    R.id.widget_day_fri,
                    R.id.widget_day_sat,
                    R.id.widget_day_sun
                )

                for (i in 0..6) {
                    val isToday = (i == todayIndex)
                    val isPlayed = hasPlayedDay[i]

                    val dotDrawable = when {
                        isToday && isPlayed -> R.drawable.widget_dot_today_played
                        isToday && !isPlayed -> R.drawable.widget_dot_today_unplayed
                        !isToday && isPlayed -> R.drawable.widget_dot_played
                        else -> R.drawable.widget_dot_unplayed
                    }
                    views.setImageViewResource(dotIds[i], dotDrawable)

                    // Text color: highlight today
                    val textColor = if (isToday) 0xFFFFFFFF.toInt() else 0xFF858D9D.toInt()
                    views.setTextColor(dayTextIds[i], textColor)
                }

                // Summary text and badge
                val playedCount = hasPlayedDay.count { it }
                views.setTextViewText(R.id.widget_week_badge, "• $playedCount/7")

                val summaryText = when {
                    playedCount == 0 -> "Zatím žádné hraní v tomto týdnu"
                    playedCount == 1 -> "1 aktivní den tento týden 🎮"
                    playedCount in 2..4 -> "$playedCount aktivní dny tento týden 🎮"
                    else -> "$playedCount aktivních dní! Jste ve formě 🔥"
                }
                views.setTextViewText(R.id.widget_status_subtext, summaryText)

                // 2. Update Right Section: Last Played Game
                if (lastGame != null) {
                    views.setTextViewText(R.id.widget_game_status_label, "NAPOSLEDY HRÁNO")
                    views.setTextViewText(R.id.widget_game_title, lastGame.title)
                    views.setTextViewText(R.id.widget_game_time, lastGame.formattedTotalTime)

                    if (coverBitmap != null) {
                        views.setImageViewBitmap(R.id.widget_game_cover, coverBitmap)
                    } else {
                        views.setImageViewResource(R.id.widget_game_cover, R.drawable.widget_cover_placeholder)
                    }

                    // Intent to open this game's detail directly
                    val gameDetailIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("GAME_ID", lastGame.id)
                    }
                    val gamePendingIntent = PendingIntent.getActivity(
                        context,
                        lastGame.id.toInt(),
                        gameDetailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_right_card, gamePendingIntent)
                } else {
                    views.setTextViewText(R.id.widget_game_status_label, "GAMEPAL")
                    views.setTextViewText(R.id.widget_game_title, "Žádná hra")
                    views.setTextViewText(R.id.widget_game_time, "0m")
                    views.setImageViewResource(R.id.widget_game_cover, R.drawable.widget_cover_placeholder)

                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        101,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_right_card, mainPendingIntent)
                }

                // Left side and Root click intent -> Open Main App
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val mainPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_left_section, mainPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

                // Commit widget update
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMondayStartOfWeek(): Long {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        return cal.timeInMillis
    }

    private suspend fun loadCoverBitmap(context: Context, url: String): Bitmap? {
        return try {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // RemoteViews require software bitmap!
                .size(160, 210)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val raw = (result as? BitmapDrawable)?.bitmap ?: return null
            createRoundedBitmap(raw, 24f)
        } catch (_: Exception) {
            null
        }
    }

    private fun createRoundedBitmap(src: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, src.width, src.height)
        val rectF = RectF(rect)
        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        return output
    }

    companion object {
        const val ACTION_FORCE_UPDATE = "com.example.action.WIDGET_FORCE_UPDATE"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, GameTrackerWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    val intent = Intent(context, GameTrackerWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
