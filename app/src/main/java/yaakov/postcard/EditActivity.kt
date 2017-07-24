package yaakov.postcard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlinx.android.synthetic.main.activity_edit.foreground
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.widget.Toast


var selectedImage: Uri = Uri.EMPTY
var initCoords = FloatArray(2)
var finCoords = FloatArray(2)

class EditActivity : AppCompatActivity() {

    lateinit var foreground: DrawImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        foreground = findViewById(R.id.foreground) as DrawImageView

        if(!OpenCVLoader.initDebug()) {
            Log.d("Status", "Can't load OpenCV :(")
        } else {
            Log.d("Status", "OpenCV Loaded")
        }


        if(intent.hasExtra("uri")){
            selectedImage = intent.getParcelableExtra("uri")
            foreground.setImageURI(selectedImage)
            foreground.setOnTouchListener { v, event ->
                var debugBool = false
                val drawView = v as DrawImageView
                val inverse = Matrix()
                //foreground.imageMatrix.invert(inverse)
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
                    Log.d("Shit", initCoords[0].toString())
                    Log.d("Shit", initCoords[1].toString())
                    Log.d("Shit", finCoords[0].toString())
                    Log.d("Shit", finCoords[1].toString())
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

    /*override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                floatArrayOf(event.x, event.y)
            }

            MotionEvent.ACTION_UP -> {
                floatArrayOf(event.x, event.y)
            }

        }
        return super.onTouchEvent(event)
    }*/

    fun imageSegmentation(view: View){
        var imgMat: Mat = Mat()
        var mask = Mat()
        var bgModel = Mat()
        var fgModel = Mat()

        var selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
        Utils.bitmapToMat(selectedBitmap, imgMat)

        var rect = Rect(initCoords[0].toInt(), initCoords[1].toInt(), finCoords[0].toInt(), finCoords[1].toInt())
        var source = Mat(1, 1, CvType.CV_8U, Scalar(3.0))

        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.grabCut(imgMat, mask, rect, bgModel, fgModel, 1, 0)
        Core.compare(mask, source, mask, Core.CMP_EQ)
        var fg = Mat(imgMat.size(), CvType.CV_8UC1, Scalar(0.0, 0.0, 0.0))
        imgMat.copyTo(fg, mask)

        Utils.matToBitmap(fg, selectedBitmap)
        foreground.setImageBitmap(selectedBitmap)

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
