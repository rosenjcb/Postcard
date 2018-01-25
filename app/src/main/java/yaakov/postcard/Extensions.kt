@file:JvmName("Extensions")

package yaakov.postcard

import android.graphics.Bitmap

/**
 * Created by Rosenzweig on 1/23/2018.
 */

fun Bitmap.resize(image: Bitmap = this, maxWidth: Int = 1920, maxHeight: Int = 1080): Bitmap {
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.getWidth()
            val height = image.getHeight()
            val ratioBitmap = width.toFloat() / height.toFloat()
            val  ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var  finalHeight = maxHeight

            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            val finalimage = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            return finalimage
        } else {
            return image
        }
    }