package bananaforscale.com.timechange.extensions

import android.graphics.Canvas
import android.graphics.Paint

fun Canvas.drawTextToWidth(
    text: String,
    width: Float,
    startY: Float,
    textSpacing: Float,
    paint: Paint
): Float {
    val words = text.split(" ").toTypedArray()
    var y = startY
    for (i in words.indices) {
        val word = words[i]
        paint.setTextSizeForWidth(width, word)
        val textHeight = paint.heightOfText(word)
        y += textHeight + textSpacing
        drawText(word, (this.width / 2).toFloat() - width / 2, y, paint)
    }
    return y
}