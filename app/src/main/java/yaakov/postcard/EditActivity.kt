package yaakov.postcard

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.widget.ImageView
import org.opencv.core.Mat
import org.opencv.core.Rect

class EditActivity : AppCompatActivity() {
    private lateinit var mGestureDetector: GestureDetectorCompat
    private lateinit var customGestureDetector: GestureListener
    private lateinit var mScaleDetector: ScaleGestureDetector
    private lateinit var customScaleDetector: ScaleListener
    private lateinit var foreground: DrawImageView
    private lateinit var background: ImageView
    private lateinit var layout: View
    private lateinit var selectedBitmap: Bitmap
    private var index = 0
    private var bDraw = true
    private var selectedImage: Uri = Uri.EMPTY
    private var initCords = FloatArray(2)
    private var finCords = FloatArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        customGestureDetector = GestureListener()
        mGestureDetector = GestureDetectorCompat(this, customGestureDetector)
        mGestureDetector.setOnDoubleTapListener(customGestureDetector)
        customScaleDetector = ScaleListener()
        mScaleDetector = ScaleGestureDetector(this, customScaleDetector)
        layout = findViewById(android.R.id.content)
        foreground = findViewById(R.id.foreground) as DrawImageView
        background = findViewById(R.id.background) as ImageView

        if(!OpenCVLoader.initDebug()) {
            Log.d("Status", "Can't load OpenCV :(")
        } else {
            Log.d("Status", "OpenCV Loaded")
        }


        if(intent.hasExtra("uri")){
            selectedImage = intent.getParcelableExtra("uri")
            selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            foreground.setImageURI(selectedImage)
            foreground.setOnTouchListener { v, event ->
                val drawView = v as DrawImageView
                //drawView.setMask(selectedBitmap)
                val inverse = Matrix()
                when (event.action){
                    MotionEvent.ACTION_DOWN -> {
                        if(bDraw) {
                            drawView.touchBegin(event.x, event.y, false)
                            initCords = floatArrayOf(event.x, event.y)
                            foreground.imageMatrix.invert(inverse)
                            inverse.postTranslate(foreground.scrollX.toFloat(), foreground.scrollY.toFloat())
                            inverse.mapPoints(initCords)
                            drawView.touchBegin(initCords[0], initCords[1], true)
                            drawView.invalidate()
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if(bDraw) {
                            drawView.touchMove(event.x, event.y, false)
                            val currentCords = floatArrayOf(event.x, event.y)
                            foreground.imageMatrix.invert(inverse)
                            inverse.postTranslate(foreground.scrollX.toFloat(), foreground.scrollY.toFloat())
                            inverse.mapPoints(currentCords)
                            drawView.invalidate()
                            drawView.touchMove(currentCords[0], currentCords[1], true)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if(bDraw) {
                            drawView.touchEnd(false)
                            finCords = floatArrayOf(event.x, event.y)
                            foreground.imageMatrix.invert(inverse)
                            inverse.postTranslate(foreground.scrollX.toFloat(), foreground.scrollY.toFloat())
                            inverse.mapPoints(finCords)
                            drawView.touchEnd(true)
                            drawView.invalidate()
                        }
                    }
                }

                mGestureDetector.onTouchEvent(event)
                mScaleDetector.onTouchEvent(event)
                drawView.invalidate()
                drawView.drawPath = true
                true
            }
        }
    }

    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }*/

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean{
            return true
        }

        override fun onDoubleTap(event: MotionEvent): Boolean {
            Log.d("yahoo", "yahoo")
            if(index == 2) { index = 0 }
            else { index++ }
            val images = assets.list("bgs")
            Log.d("uh", images.joinToString { it })
            val nextImage = assets.open("bgs/" + images[index])
            val nextImageBitmap = BitmapFactory.decodeStream(nextImage)
            nextImage.close()
            background.setImageBitmap(nextImageBitmap)
            return true
        }
    }

    inner class ScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))
            Log.d("scale", mScaleFactor.toString())
            foreground.scaleX = mScaleFactor
            foreground.scaleY = mScaleFactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    }

    fun imageSegmentation(view: View){
        //Ugly hack... I should make this functional and not a for-loop & if-else disaster
        fun convertToOpencvValues(mask: Mat) {
            var buffer = ByteArray(3)
            for (x in 0..mask.rows()) {
                for (y in 0..mask.cols()) {
                    mask.get(x, y, buffer);
                    var value = buffer[0];
                    if (value >= 0 && value < 64) {
                        buffer[0] = Imgproc.GC_BGD.toByte() // for sure background
                    } else if (value >= 64 && value < 128) {
                        buffer[0] = Imgproc.GC_PR_BGD.toByte() // probably background
                    } else if (value >= 128 && value < 192) {
                        buffer[0] = Imgproc.GC_PR_FGD.toByte() // probably foreground
                    } else {
                        buffer[0] = Imgproc.GC_FGD.toByte() // for sure foreground
                    }
                    mask.put(x, y, buffer)
                }
            }
        }

        fun convertToOriginalValues(mask: Mat) {
            var buffer = ByteArray(3)
            for (x in 0..mask.rows()) {
                for (y in 0..mask.cols()) {
                    mask.get(x, y, buffer)
                    var value = buffer[0]
                    if (value == Imgproc.GC_BGD.toByte()) {
                        buffer[0] = 0 // for sure background
                    } else if (value == Imgproc.GC_PR_BGD.toByte()) {
                        buffer[0] = 85 // probably background
                    } else if (value == Imgproc.GC_PR_FGD.toByte()) {
                        buffer[0] = 170.toByte() // probably foreground
                    } else {
                        buffer[0] = 255.toByte() // for sure foreground
                    }
                    mask.put(x, y, buffer)
                }
            }
        }

        val imgMat = Mat()
        val mask = Mat()
        val bgModel = Mat()
        val fgModel = Mat()

        val maskBitmap = Bitmap.createBitmap(selectedBitmap.width, selectedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(maskBitmap)
        canvas.drawColor(Color.BLACK)
        canvas.drawPath(foreground.inversePath, foreground.currentPaint)

        val scaledBitmap = selectedBitmap.resize()
        val scaledMask = maskBitmap.resize()

        val scaledX = floatArrayOf(initCords[0], finCords[0]).map { (it.toInt() * scaledBitmap.width) / selectedBitmap.width }
        val scaledY = floatArrayOf(initCords[1], finCords[1]).map { (it.toInt() * scaledBitmap.height) / selectedBitmap.width }

        foreground.mPath.reset()
        Utils.bitmapToMat(scaledBitmap, imgMat) 
        Utils.bitmapToMat(scaledMask, mask)

        foreground.mPath.reset()

        //val rect = Rect(10, 10, imgMat.cols() - 20, imgMat.rows() - 20)
        val realRect = Rect(scaledX[0], scaledY[0], scaledX[1], scaledY[1])
        //val rect = Rect(initCords[0].toInt(), initCords[1].toInt(), finCords[0].toInt(), finCords[1].toInt())
        val source = Mat(1, 1, CvType.CV_8UC4, Scalar(0.0, 0.0, 0.0))

        //This has to be reworked. It's not working as intended and it's incredibly slow.
        /*Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB)
        mask.convertTo(mask, CV_8U)
        Imgproc.cvtColor(imgMat, mask, Imgproc.COLOR_RGB2GRAY)
        convertToOpencvValues(mask)
        Imgproc.grabCut(imgMat, mask, Rect(), bgModel, fgModel, 1, Imgproc.GC_INIT_WITH_MASK) //mode = Imgproc.GC_INIT_WITH_MASK or 0 for both rect and mask
        convertToOriginalValues(mask)
        Imgproc.threshold(mask, mask, 120.0, 255.0, Imgproc.THRESH_TOZERO)
        Core.compare(mask, source, mask, Core.CMP_EQ)*/
        val fg = Mat(imgMat.size(), CvType.CV_8UC4, Scalar(0.0, 0.0, 0.0))
        imgMat.copyTo(fg, mask)

        val dst = Mat(scaledBitmap.width, scaledBitmap.height, CvType.CV_8UC4)
        val tmp = Mat(scaledBitmap.width, scaledBitmap.height, CvType.CV_8UC4)
        val alpha = Mat(scaledBitmap.width, scaledBitmap.height, CvType.CV_8UC4)

        Imgproc.cvtColor(fg, tmp, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(tmp, alpha, 0.0, 255.0, Imgproc.THRESH_BINARY)

        val rgb = ArrayList<Mat>(3)
        Core.split(fg, rgb)

        val rgba= ArrayList<Mat>(4)
        //maybe rgb.add(rgb[0])???
        rgba.add(rgb.get(0))
        rgba.add(rgb.get(1))
        rgba.add(rgb.get(2))
        rgba.add(alpha)
        Core.merge(rgba, dst)

        val output = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, output)

        val input = assets.open("bgs/eiffeltower.jpg")
        val backgroundBitmap = BitmapFactory.decodeStream(input)
        input.close()
        foreground.setImageBitmap(output)
        background.setImageBitmap(backgroundBitmap)
        bDraw = false

        /*val h = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == 0) {
                    foreground.setImageBitmap(selectedBitmap)
                    Toast.makeText(view.context, "Dude Nice :D", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(view.context, "Fuck", Toast.LENGTH_LONG).show()
                }
            }
        }

        val t = object : Thread() {
            var success = false
            override fun run(){
                Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB)
                Imgproc.grabCut(imgMat, mask, rect, bgModel, fgModel, 1, 0)
                Core.compare(mask, source, mask, Core.CMP_EQ)
                var fg = Mat(imgMat.size(), CvType.CV_8UC1, Scalar(0.0, 0.0, 0.0))
                imgMat.copyTo(fg, mask)

                Utils.matToBitmap(fg, selectedBitmap)
                success = true
                h.sendEmptyMessage(0)
            }
        }*/
    }
}
