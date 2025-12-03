package bananaforscale.com.timechange

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import bananaforscale.com.timechange.extensions.drawTextToWidth
import bananaforscale.com.timechange.extensions.setTextSizeForWidth
import java.time.ZonedDateTime
import java.util.Locale

class BlackDigitalWatchService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = DigitalWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.SOFTWARE,
            defaultBackground = applicationContext.getColor(R.color.black_background)
        )
        return WatchFace(WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }
}

class DarkGrayDigitalWatchService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = DigitalWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.SOFTWARE,
            defaultBackground = applicationContext.getColor(R.color.gray_background)
        )
        return WatchFace(WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }
}

class LightGrayDigitalWatchService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = DigitalWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.SOFTWARE,
            defaultBackground = applicationContext.getColor(R.color.light_gray_background)
        )
        return WatchFace(WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }
}

class WhiteDigitalWatchService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = DigitalWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.SOFTWARE,
            defaultBackground = Color.WHITE
        )
        return WatchFace(WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }
}

// Default for how long each frame is displayed at expected frame rate.
// Since we only show hours:minutes (no seconds), update once per minute.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 60000L

class DigitalWatchFaceRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int,
    @ColorInt private val defaultBackground: Int,
) : Renderer.CanvasRenderer2<DigitalWatchFaceRenderer.DigitalSharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = true
)
{
    class DigitalSharedAssets : SharedAssets {
        private var ambientBlockO: Bitmap? = null
        private var blockO: Bitmap? = null

        fun blockO(drawMode: DrawMode, context: Context, maxWidth: Float): Bitmap {
            // Check if the required bitmap already exists, and return it if so
            val existingBitmap = when (drawMode) {
                DrawMode.AMBIENT -> ambientBlockO
                DrawMode.INTERACTIVE -> blockO
                DrawMode.LOW_BATTERY_INTERACTIVE -> ambientBlockO
                DrawMode.MUTE -> ambientBlockO
            }
            if (existingBitmap != null && !existingBitmap.isRecycled) {
                return existingBitmap
            }

            // If the bitmap doesn't exist or is recycled, create and store it
            val drawable = AppCompatResources.getDrawable(context, when (drawMode) {
                DrawMode.AMBIENT -> R.drawable.big_block_o_ambient
                DrawMode.INTERACTIVE -> R.drawable.big_block_o
                DrawMode.LOW_BATTERY_INTERACTIVE -> R.drawable.big_block_o_ambient
                DrawMode.MUTE -> R.drawable.big_block_o_ambient
            })!!

            val blockOHeight = maxWidth / drawable.intrinsicWidth * drawable.intrinsicHeight
            val newBitmap = drawable.toBitmap(maxWidth.toInt(), blockOHeight.toInt())

            when (drawMode) {
                DrawMode.AMBIENT -> ambientBlockO = newBitmap
                DrawMode.INTERACTIVE -> blockO = newBitmap
                DrawMode.LOW_BATTERY_INTERACTIVE -> ambientBlockO = newBitmap
                DrawMode.MUTE -> ambientBlockO = newBitmap
            }

            return newBitmap
        }

        // onDestroy for cleaning up resources
        override fun onDestroy() {
            ambientBlockO?.recycle()
            ambientBlockO = null
            blockO?.recycle()
            blockO = null
        }
    }

    override suspend fun createSharedAssets(): DigitalSharedAssets { return DigitalSharedAssets() }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: DigitalSharedAssets
    ) {
        val useAmbientStyle = renderParameters.drawMode != DrawMode.INTERACTIVE
        val backgroundColor = if (useAmbientStyle) Color.BLACK else defaultBackground
        canvas.drawColor(backgroundColor)

        val maxWidth = bounds.width() / widthMod
        val startingY = 14f
        val timeSpacing = 12f
        val textSpacing = 8f
        val blockOSpacing = 12f

        val hourString = convertTo12Hour(zonedDateTime.hour).toString()
        val minuteString = formatTwoDigitNumber(zonedDateTime.minute)
        val timeText = "$hourString:$minuteString"
        val paintForTime = if (useAmbientStyle) ambientTimePaint else timePaint
        paintForTime.setTextSizeForWidth(maxWidth, timeText)
        val x = (canvas.width / 2).toFloat()
        val y = startingY + paintForTime.textSize - timeSpacing
        canvas.drawText(timeText, x, y, paintForTime)

        val paintForText = if (useAmbientStyle) ambientTextPaint else textPaint
        val text = "AND MICHIGAN STILL SUCKS"
        val finalTextPosition = canvas.drawTextToWidth(
            text,
            maxWidth,
            y + textSpacing * 2,
            textSpacing,
            paintForText
        ) + textSpacing * 2
        val image = sharedAssets.blockO(renderParameters.drawMode, context, maxWidth)
        val imgX = (canvas.width - image.width) / 2.0f
        canvas.drawBitmap(image, imgX, finalTextPosition + blockOSpacing, null)
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: DigitalSharedAssets
    ) {
        // Not implemented as I never bothered supporting complications.
    }

    private val lightTypeface by lazy { ResourcesCompat.getFont(context, R.font.roboto_light)!! }
    private val lightCondensedTypeface by lazy { ResourcesCompat.getFont(context, R.font.roboto_condensed_light)!! }
    private val timePaint by lazy {
        Paint().apply {
            typeface = lightCondensedTypeface
            color = ContextCompat.getColor(context, R.color.time_text_light)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    private val ambientTimePaint by lazy {
        Paint().apply {
            typeface = lightCondensedTypeface
            color = ContextCompat.getColor(context, R.color.ambient_color)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    private val textPaint by lazy {
        Paint().apply {
            typeface = lightTypeface
            color = ContextCompat.getColor(context, R.color.gray_text)
            isAntiAlias = true
        }
    }
    private val ambientTextPaint by lazy {
        Paint().apply {
            typeface = lightTypeface
            color = ContextCompat.getColor(context, R.color.ambient_color)
            isAntiAlias = true
        }
    }

    private val widthMod by lazy {
        val configuration = context.resources.configuration
        var mod = 2.25f
        if (configuration.screenHeightDp < 200) { mod += 0.5f }
        if (configuration.isScreenRound) { mod += 0.35f }
        mod
    }

    private fun formatTwoDigitNumber(hour: Int): String {
        val locale = Locale.getDefault()
        return String.format(locale, "%02d", hour)
    }

    private fun convertTo12Hour(hour: Int): Int {
        val result = hour % 12
        return if (result == 0) 12 else result
    }

}