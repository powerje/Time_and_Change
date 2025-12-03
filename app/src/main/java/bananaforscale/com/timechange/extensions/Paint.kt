package bananaforscale.com.timechange.extensions

import android.graphics.Paint
import android.graphics.Rect

//http://stackoverflow.com/a/21895626/104527
/**
 * Sets the text size for a Paint object so a given string of text will be a
 * given width.
 *
 * @param desiredWidth
 * the desired width
 * @param text
 * the text that should be that width
 */
fun Paint.setTextSizeForWidth(
    desiredWidth: Float,
    text: String
) {

    // Pick a reasonably large value for the test. Larger values produce
    // more accurate results, but may cause problems with hardware
    // acceleration. But there are workarounds for that, too; refer to
    // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
    val testTextSize = 48f

    // Get the bounds of the text, using our testTextSize.
    textSize = testTextSize
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)

    // Calculate the desired size as a proportion of our testTextSize.
    val desiredTextSize = testTextSize * desiredWidth / bounds.width()

    // Set the paint for that size.
    textSize = desiredTextSize
}

fun Paint.heightOfText(text: String): Float {
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)
    return bounds.height().toFloat()
}