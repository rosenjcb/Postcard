package yaakov.postcard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Created by Rosenzweig on 7/19/2017.
 */
class DrawImageView : AppCompatImageView {
    var currentPaint: Paint
    var drawRect = false
    var left = 0.0f
    var right = 0.0f
    var top = 0.0f
    var bottom = 0.0f

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        currentPaint = Paint()
        currentPaint.isDither = true
        currentPaint.color = Color.GREEN  // alpha.r.g.b
        currentPaint.style = Paint.Style.STROKE
        currentPaint.strokeJoin = Paint.Join.ROUND
        currentPaint.strokeCap = Paint.Cap.ROUND
        currentPaint.strokeWidth = 10.0f
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawRect) {
            canvas.drawRect(left, top, right, bottom, currentPaint)
        }
    }

    /*class GestureListener : GestureDetector.SimpleOnGestureListener(){
        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }*/
}