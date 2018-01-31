package yaakov.postcard

import android.content.Intent
import android.database.Cursor
import android.graphics.Matrix
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.cameraButton
import kotlinx.android.synthetic.main.activity_main.searchButton
import kotlinx.android.synthetic.main.activity_main.galleryButton
import kotlinx.android.synthetic.main.activity_main.nahButton
import kotlinx.android.synthetic.main.activity_main.goodButton
import kotlinx.android.synthetic.main.activity_main.foreground

class MainActivity : AppCompatActivity() {

    val RESULT_LOAD_IMG: Int = 1
    val RESULT_TAKE_PIC: Int = 2
    val RESULT_SEARCH_IMG: Int = 3

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    //private val matrix = Matrix()
    private lateinit var selectedImage: Uri
    //private lateinit var foregroundImg: ImageView

    //lateinit var galleryButton: ImageButton
    //lateinit var cameraButton: ImageButton
    //lateinit var searchButton: ImageButton
    //lateinit var goodButton: Button
    //lateinit var nahButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //foregroundImg = findViewById(R.id.foreground) as ImageView
        //galleryButton = findViewById(R.id.galleryButton) as ImageButton
        //cameraButton = findViewById(R.id.cameraButton) as ImageButton
        //searchButton = findViewById(R.id.searchButton) as ImageButton
        //goodButton = findViewById(R.id.goodButton) as Button
        //nahButton = findViewById(R.id.nahButton) as Button

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try{
            if (resultCode == RESULT_OK && null != data) {
                if(requestCode == RESULT_LOAD_IMG){
                    selectedImage = data.data
                    val filePathColumn = arrayOf<String>(MediaStore.Images.Media.DATA)

                    val cursor: Cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
                    cursor.moveToFirst()

                    //val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    //val imgDecodableString = cursor.getString(columnIndex)
                    cursor.close()
                    foreground.setImageURI(selectedImage)
                    removeMenuButtons()
                    addMenuButtons()
            }
                else if(requestCode == RESULT_TAKE_PIC) {
                    removeMenuButtons()
                    addMenuButtons()
                }
            } else {
                Toast.makeText(this, "No Image Selected", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception){
            Toast.makeText(this, "Something went wrong. :(", Toast.LENGTH_LONG).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener(){

        /*override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f))
            matrix.setScale(scaleFactor, scaleFactor)
            foregroundImg.imageMatrix = matrix
            Log.i("debug-matrix", matrix.toString())
            return true
        }*/
    }

    fun loadFromGallery(view: View){
        val photoIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(photoIntent, RESULT_LOAD_IMG)
    }

    fun loadFromCamera(){
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RESULT_TAKE_PIC)
    }

    fun startEditActivity(view: View){
        foreground.isDrawingCacheEnabled = true

        val i = Intent(this, EditActivity::class.java)
        i.putExtra("uri", selectedImage)
        startActivity(i)
    }

    fun goBack(view:View){
        //resets buttons to original values
        galleryButton.visibility = View.VISIBLE
        cameraButton.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE
        goodButton.visibility = View.INVISIBLE
        nahButton.visibility = View.INVISIBLE

        //clears imageview - null values scare me :(
        foreground.setImageBitmap(null)
    }

    fun removeMenuButtons(){
        galleryButton.visibility = View.INVISIBLE
        cameraButton.visibility = View.INVISIBLE
        searchButton.visibility = View.INVISIBLE
    }

    fun addMenuButtons(){
        goodButton.visibility = View.VISIBLE
        nahButton.visibility = View.VISIBLE
    }
}
