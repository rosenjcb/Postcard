package yaakov.postcard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.GestureDetector
import android.widget.Toast
import org.opencv.core.Mat
import org.opencv.core.Rect

class EditActivity : AppCompatActivity() {
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var foreground: DrawImageView
    private lateinit var selectedBitmap: Bitmap
    private lateinit var maskBitmap: Bitmap
    private var selectedImage: Uri = Uri.EMPTY
    private var initCoords = FloatArray(2)
    private var finCoords = FloatArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        mDetector = GestureDetectorCompat(this, GestureListener())
        foreground = findViewById(R.id.foreground) as DrawImageView

        if(!OpenCVLoader.initDebug()) {
            Log.d("Status", "Can't load OpenCV :(")
        } else {
            Log.d("Status", "OpenCV Loaded")
        }


        if(intent.hasExtra("uri")){
            selectedImage = intent.getParcelableExtra("uri")
            selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            maskBitmap = Bitmap.createBitmap(selectedBitmap.width, selectedBitmap.height, Bitmap.Config.ARGB_8888)
            foreground.setImageURI(selectedImage)
            foreground.setOnTouchListener { v, event ->
                var debugBool = false
                val drawView = v as DrawImageView
                val inverse = Matrix()
                when (event.action){
                    MotionEvent.ACTION_DOWN -> {
                        drawView.left = event.x
                        drawView.top = event.y
                        initCoords = floatArrayOf(event.x, event.y)
                        foreground.imageMatrix.invert(inverse)
                        inverse.postTranslate(foreground.scrollX.toFloat(), foreground.scrollY.toFloat())
                        inverse.mapPoints(initCoords)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        drawView.right = event.x
                        drawView.bottom = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                        drawView.right = event.x
                        drawView.bottom = event.y
                        finCoords = floatArrayOf(event.x, event.y)
                        foreground.imageMatrix.invert(inverse)
                        inverse.postTranslate(foreground.scrollX.toFloat(), foreground.scrollY.toFloat())
                        inverse.mapPoints(finCoords)
                        debugBool = true
                    }
                }
                if(debugBool == true) {
                    Log.d("InitCoordX", initCoords[0].toString())
                    Log.d("InitCoordY", initCoords[1].toString())
                    Log.d("FinCoordX", finCoords[0].toString())
                    Log.d("FinCoordY", finCoords[1].toString())
                    debugBool = false
                }
                drawView.invalidate()
                drawView.drawRect = true
                /*inverse.postTranslate(drawView.x, drawView.y)
                inverse.mapPoints(initCoords)
                inverse.mapPoints(finCoords)*/
                true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(event: MotionEvent): Boolean{
            /*var bitmap = BitmapFactory.decodeFile("")
            var d: Drawable = BitmapDrawable(resources, bitmap)
            foreground.setBackgroundDrawable(d)*/
            return true
        }
    }

    fun imageSegmentation(view: View){
        var imgMat: Mat = Mat()
        var mask = Mat()
        var bgModel = Mat()
        var fgModel = Mat()

        Utils.bitmapToMat(selectedBitmap, imgMat)

        var rect = Rect(initCoords[0].toInt(), initCoords[1].toInt(), finCoords[0].toInt(), finCoords[1].toInt())
        var source = Mat(1, 1, CvType.CV_8UC4, Scalar(3.0))

        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.grabCut(imgMat, mask, rect, bgModel, fgModel, 3, 0)
        Core.compare(mask, source, mask, Core.CMP_EQ)
        var fg = Mat(imgMat.size(), CvType.CV_8UC4, Scalar(0.0, 0.0, 0.0))
        imgMat.copyTo(fg, mask)

        var dst = Mat(selectedBitmap.width, selectedBitmap.height, CvType.CV_8UC4)
        var tmp = Mat(selectedBitmap.width, selectedBitmap.height, CvType.CV_8UC4)
        var alpha = Mat(selectedBitmap.width, selectedBitmap.height, CvType.CV_8UC4)

        Imgproc.cvtColor(fg, tmp, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(tmp, alpha, 100.0, 255.0, Imgproc.THRESH_BINARY)

        val rgb = ArrayList<Mat>(3)
        Core.split(fg, rgb)

        val rgba= ArrayList<Mat>(4)
        rgba.add(rgb.get(0))
        rgba.add(rgb.get(1))
        rgba.add(rgb.get(2))
        rgba.add(alpha)
        Core.merge(rgba, dst)

        var output = Bitmap.createBitmap(selectedBitmap.width, selectedBitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, output)
        foreground.setImageBitmap(output)

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
