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
                initialViews.setImageViewResource(R.id.widget_background_img, R.drawable.widget_background)
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

                // Load cover and extract dominant palette if last game exists
                val coverData: CoverArtData? = if (lastGame != null && lastGame.coverUrl.isNotBlank()) {
                    loadCoverData(context, lastGame.coverUrl)
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

                    // Text color: highlight today with pure white, other days semi-transparent
                    val textColor = if (isToday) 0xFFFFFFFF.toInt() else 0xB3FFFFFF.toInt()
                    views.setTextColor(dayTextIds[i], textColor)
                }

                // 2. Update Left Section: Last Played Game & Dynamic Background
                if (lastGame != null) {
                    views.setTextViewText(R.id.widget_game_title, lastGame.title)
                    views.setTextViewText(R.id.widget_game_time, lastGame.formattedTotalTime)

                    if (coverData != null) {
                        views.setImageViewBitmap(R.id.widget_game_cover, coverData.coverBitmap)
                        val dynamicBg = createDynamicWidgetBackground(context, coverData.dominantColor)
                        views.setImageViewBitmap(R.id.widget_background_img, dynamicBg)
                    } else {
                        views.setImageViewResource(R.id.widget_game_cover, R.drawable.widget_cover_placeholder)
                        views.setImageViewResource(R.id.widget_background_img, R.drawable.widget_background)
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
                    views.setOnClickPendingIntent(R.id.widget_game_cover, gamePendingIntent)
                } else {
                    views.setTextViewText(R.id.widget_game_title, "Zatím žádná hra")
                    views.setTextViewText(R.id.widget_game_time, "0m")
                    views.setImageViewResource(R.id.widget_game_cover, R.drawable.widget_cover_placeholder)
                    views.setImageViewResource(R.id.widget_background_img, R.drawable.widget_background)
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
                views.setOnClickPendingIntent(R.id.widget_right_section, mainPendingIntent)
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

    private data class CoverArtData(
        val coverBitmap: Bitmap,
        val dominantColor: Int
    )

    private suspend fun loadCoverData(context: Context, url: String): CoverArtData? {
        return try {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // RemoteViews require software bitmap!
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val raw = (result as? BitmapDrawable)?.bitmap ?: return null
            val dominantColor = extractDominantColor(raw)
            val roundedCover = createRoundedPortraitBitmap(raw, 26f)
            CoverArtData(roundedCover, dominantColor)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDominantColor(bitmap: Bitmap): Int {
        return try {
            val smallBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
            val width = smallBitmap.width
            val height = smallBitmap.height
            val pixels = IntArray(width * height)
            smallBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            var bestColor = 0xFF8A1C14.toInt() // Default fallback crimson
            var maxScore = -1f
            val hsv = FloatArray(3)

            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                val saturation = hsv[1]
                val value = hsv[2]

                // Discard near-black shadows or washed out whites
                if (value < 0.15f || value > 0.95f || saturation < 0.22f) continue

                // Score favoring vibrant colors with moderate to high brightness
                val score = saturation * 2.2f + (1f - kotlin.math.abs(value - 0.65f))
                if (score > maxScore) {
                    maxScore = score
                    bestColor = pixel
                }
            }
            bestColor
        } catch (_: Exception) {
            0xFF8A1C14.toInt()
        }
    }

    private fun createDynamicWidgetBackground(
        context: Context,
        dominantColor: Int,
        widthPx: Int = 800,
        heightPx: Int = 400
    ): Bitmap {
        return try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(dominantColor, hsv)

            val hue = hsv[0]
            val sat = hsv[1].coerceIn(0.6f, 0.95f)

            // 1. Top-Left: Warm rich accent (vibrant, matches game art)
            val colorStart = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 0.55f))
            // 2. Center: Deep rich tone
            val colorMid = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat * 0.9f, 0.32f))
            // 3. Bottom-Right: Deep shadow / vignette
            val colorEnd = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat * 0.8f, 0.12f))

            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val density = context.resources.displayMetrics.density
            val gradient = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(colorStart, colorMid, colorEnd)
            ).apply {
                cornerRadius = 28f * density
                setStroke((1.5f * density).toInt(), 0x2AFFFFFF.toInt())
                setBounds(0, 0, widthPx, heightPx)
            }
            gradient.draw(canvas)
            bitmap
        } catch (_: Exception) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    private fun createRoundedPortraitBitmap(
        src: Bitmap,
        cornerRadiusPx: Float,
        destWidth: Int = 360,
        destHeight: Int = 500
    ): Bitmap {
        // Target aspect ratio for physical game box (approx 1 : 1.39)
        val targetRatio = destWidth.toFloat() / destHeight.toFloat()
        val srcRatio = src.width.toFloat() / src.height.toFloat()

        val srcCropRect = if (srcRatio > targetRatio) {
            // Source is wider than portrait (e.g. landscape 16:9) -> crop sides to keep center
            val cropWidth = (src.height * targetRatio).toInt()
            val left = (src.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, src.height)
        } else {
            // Source is taller than target portrait -> crop top/bottom
            val cropHeight = (src.width / targetRatio).toInt()
            val top = (src.height - cropHeight) / 2
            Rect(0, top, src.width, top + cropHeight)
        }

        val output = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw rounded rectangle mask
        val rectF = RectF(0f, 0f, destWidth.toFloat(), destHeight.toFloat())
        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)

        // Clip src into destination using SRC_IN
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val destRect = Rect(0, 0, destWidth, destHeight)
        canvas.drawBitmap(src, srcCropRect, destRect, paint)

        // Subtle physical collectible box border
        paint.xfermode = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = 0x3DFFFFFF.toInt()
        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)

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
