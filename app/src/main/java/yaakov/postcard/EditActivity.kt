package yaakov.postcard

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.core.Core.BORDER_DEFAULT
import org.opencv.core.CvType.CV_16S

var selectedImage: Uri = Uri.EMPTY


class EditActivity : AppCompatActivity() {

    private lateinit var foregroundImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)


        if(!OpenCVLoader.initDebug()) {
            Log.d("Shit", "Can't load OpenCV :(")
        } else {
            Log.d("Shit", "OpenCV Loaded")
        }

        foregroundImg = findViewById(R.id.foregroundImg) as ImageView

        if(intent.hasExtra("uri")){
            selectedImage = intent.getParcelableExtra("uri")
            foregroundImg.setImageURI(selectedImage)
        }

    }

    fun sobel(gray: Mat): Mat {

        //Imgcodecs.imread(selectedImage.toString())
        val edges: Mat = Mat(0,0,0)
        val scale: Double = 1.0
        val delta: Double = 0.0
        val ddepth = CV_16S
        val edges_x: Mat = Mat(0,0,0)
        val edges_y: Mat = Mat(0,0,0)
        val abs_edges_x: Mat = Mat(0,0,0)
        val abs_edges_y: Mat = Mat(0,0,0)
        Imgproc.Sobel(gray, edges_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT)
        Core.convertScaleAbs(edges_x, abs_edges_x)
        Imgproc.Sobel(gray, edges_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT)
        Core.convertScaleAbs(edges_y, abs_edges_y)
        Core.addWeighted(abs_edges_x, 0.5, abs_edges_y, 0.5, 0.0, edges)

        return edges
    }

}
