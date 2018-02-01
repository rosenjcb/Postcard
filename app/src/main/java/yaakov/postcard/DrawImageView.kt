package yaakov.postcard

import android.content.Context
import android.graphics.*
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
    var currentPath: Path
    lateinit var maskBitmap: Bitmap
    lateinit var mCanvas: Canvas
    var mPaint: Paint
    var mPath: Path
    var inversePath: Path
    private var MOVEMENT_MINIMUM = 4.0f;
    var drawRect = false
    var drawPath = false
    var mX = 0.0f
    var mY = 0.0f
    var inverseX = 0.0f
    var inverseY = 0.0f
    var left = 0.0f
    var right = 0.0f
    var top = 0.0f
    var bottom = 0.0f

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mPaint = Paint()
        mPath = Path()
        inversePath = Path()
        mPaint.isDither = true
        mPaint.isAntiAlias = true
        mPaint.color = Color.GREEN // alpha.r.g.b
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 80.0f

        currentPaint = Paint()
        currentPath = Path()
        currentPaint.isAntiAlias = true
        currentPaint.color = Color.WHITE
        currentPaint.style = Paint.Style.STROKE
        currentPaint.strokeJoin = Paint.Join.ROUND
        currentPaint.strokeCap = Paint.Cap.ROUND
        currentPaint.strokeWidth = 80.0f
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawPath) {
            canvas.drawPath(mPath, mPaint)
            //mCanvas.drawPath(mPath, currentPaint)
            //canvas.drawPath(currentPath, currentPaint)
        }
        if (drawRect) {
            canvas.drawRect(left, top, right, bottom, mPaint)
        }
    }

    fun touchBegin(x: Float, y: Float, isMask: Boolean){
        //mPath.reset()
        if(isMask) {
            inversePath.moveTo(x, y)
            inverseX = x
            inverseY = y
        }

        else {
            mPath.moveTo(x, y)
            mX = x
            mY = y
        }
    }

    fun touchMove(x: Float, y: Float, isMask: Boolean){
        if(isMask) {
            var dx = Math.abs(x - inverseX)
            var dy = Math.abs(y - inverseY)

            if (dx >= MOVEMENT_MINIMUM || dy >= MOVEMENT_MINIMUM) {
                inversePath.quadTo(inverseX, inverseY, (x + inverseX) / 2, (y + inverseY) / 2)
                inverseX = x
                inverseY = y
            }
        }

        else {
            var dx = Math.abs(x - mX)
            var dy = Math.abs(y - mY)

            if (dx >= MOVEMENT_MINIMUM || dy >= MOVEMENT_MINIMUM) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y

                //currentPath.reset()
                //currentPath.addCircle(mX, mY, 30.0f, Path.Direction.CW)
            }
        }
    }

    fun touchEnd(isMask: Boolean) {
        if(isMask) {
            inversePath.lineTo(inverseX, inverseY)
        }

        else {
            mPath.lineTo(mX, mY)
            //mPath.reset()
        }
    }

    class GestureListener : GestureDetector.SimpleOnGestureListener(){
        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }
}