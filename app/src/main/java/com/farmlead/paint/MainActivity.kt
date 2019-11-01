package com.farmlead.paint


import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import java.util.*
import android.graphics.Bitmap
import android.text.TextUtils
import kotlinx.coroutines.*

/**
 * MainActivity
 * @author Swetha

 */
class MainActivity : AppCompatActivity() {

    /**
     * Number of columns on the canvas, i.e. the 'x' coordinate
     *  Note: Can be changed at compile time
     */
    private val columns = 6

    /**
     * Number of rows on the canvas, i.e. the 'y' coordinate
     *  Note: Can be changed at compile time
     */
    private val rows = 8

    /**
     * Bi-dimensional array representing the pixels on the canvas
     *  Each position in the canvas is a color in Integer format
     */
    private val canvas = Array(columns) { IntArray(rows) }

    /**
     * Different colors that each pixel on the canvas can be
     */
    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

    private lateinit var imgCanvas: ImageView
    private var bitmap: Bitmap? = null
    private lateinit var canvasView: Canvas
    private var COLUMN_WIDTH = 0
    private var ROW_HEIGHT = 0
    private lateinit var colorPicker: RadioGroup
    private lateinit var edtXCoordinate: EditText
    private lateinit var edtYCoordinate: EditText
    private lateinit var floodFill: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgCanvas = findViewById(R.id.img_canvas)
        colorPicker = findViewById(R.id.color_picker)
        edtXCoordinate = findViewById(R.id.x_coordinate)
        edtYCoordinate = findViewById(R.id.y_coordinate)
        floodFill = findViewById(R.id.flood_fill)

        Log.i("test:", "onCreate")

        imgCanvas.post {
            initializeCanvas()
            displayCanvas()
        }

        floodFill.setOnClickListener(View.OnClickListener {
            val color: Int
            when (colorPicker.checkedRadioButtonId) {
                R.id.green -> color = Color.GREEN
                R.id.blue -> color = Color.BLUE
                R.id.red -> color = Color.RED
                R.id.yellow -> color = Color.YELLOW
                else -> color = Color.WHITE
            }
            val x = edtXCoordinate.text.toString()
            val y = edtYCoordinate.text.toString()

            if (TextUtils.isEmpty(x) || TextUtils.isEmpty(y)) {
                Toast.makeText(this, "Please Enter Valid Input", Toast.LENGTH_LONG)
                    .show()
                return@OnClickListener
            } else if (x.toInt() > columns || y.toInt() > rows) {
                Toast.makeText(
                    this,
                    "Please enter x from 0 to $columns and y from 0 to $rows",
                    Toast.LENGTH_LONG
                )
                    .show()
                return@OnClickListener
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        floodFill(x.toInt(), y.toInt(), color)
                    }
                }
            }
        })
    }

    /**
     * Initializes the canvas by creating a random bi-dimensional array of colors
     */
    private fun initializeCanvas() {
        val random = Random()
        if (columns < 1 || rows < 1) {
            return
        }

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                // Choose a random number from the list of colors
                Log.i("test:", "initializeCanvas i = $i, j = $j")
                canvas[i][j] = colors[random.nextInt(colors.size)]
            }
        }
        bitmap = Bitmap.createBitmap(
            imgCanvas.measuredWidth,
            imgCanvas.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        COLUMN_WIDTH = bitmap!!.width / columns
        ROW_HEIGHT = bitmap!!.height / rows

        Log.i("test:", "initializeCanvas columns = $columns, rows = $rows")

        Log.i("test:", "initializeCanvas COLUMN_WIDTH = $COLUMN_WIDTH, ROW_HEIGHT = $ROW_HEIGHT")
        imgCanvas.setImageBitmap(bitmap)
        canvasView = Canvas(bitmap!!)


    }

    /**
     * Displays the canvas on the screen
     */
    private fun displayCanvas() {

        Log.d("Thread", "dispalyCanvas ${Thread.currentThread().name}")

        if (columns < 1 || rows < 1) {
            return
        }

        if (bitmap != null && !bitmap!!.isRecycled()) {
            bitmap!!.recycle()
            bitmap = null
        }

        bitmap = Bitmap.createBitmap(
            imgCanvas.measuredWidth,
            imgCanvas.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        canvasView = Canvas(bitmap!!)
        imgCanvas.setImageBitmap(bitmap)

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                val pixelRect = Rect()
                pixelRect.set(
                    i * COLUMN_WIDTH, j * ROW_HEIGHT,
                    (i + 1) * COLUMN_WIDTH, (j + 1) * ROW_HEIGHT
                )
                val pixelPaint = Paint()
                pixelPaint.color = canvas[i][j]
                Log.i("test:", "displayCanvas i = $i, j = $j ||  ${pixelPaint.color} ")
                canvasView.drawRect(pixelRect, pixelPaint)
            }
        }
        imgCanvas.requestFocus()
    }


    private fun floodFill(x: Int, y: Int, color: Int) {

        Log.d("Thread", "floodfill ${Thread.currentThread().name}")

        val inputPoint = Point()
        inputPoint.x = x
        inputPoint.y = y

        val targetColor = canvas[x][y]

        if (targetColor != color) {
            val inputList = LinkedList<Point>()
            val traversedPointsList = ArrayList<Point>()

            inputList.add(inputPoint)

            var currentPoint: Point
            while (!inputList.isEmpty()) {
                currentPoint = inputList.remove()
                Log.i(
                    "test:",
                    "floodFill currPoint.x = ${currentPoint.x}, currPoint.y = ${currentPoint.y}"
                )
                if (canvas[currentPoint.x][currentPoint.y] == targetColor) {
                    canvas[currentPoint.x][currentPoint.y] = color
                    if (currentPoint.x > 0 && !traversedPointsList.contains(
                            Point(
                                currentPoint.x - 1,
                                currentPoint.y
                            )
                        )
                    ) {
                        inputList.add(Point(currentPoint.x - 1, currentPoint.y))
                    }
                    if (currentPoint.x < columns - 1 && !traversedPointsList.contains(
                            Point(
                                currentPoint.x + 1,
                                currentPoint.y
                            )
                        )
                    ) {
                        inputList.add(Point(currentPoint.x + 1, currentPoint.y))
                    }
                    if (currentPoint.y > 0 && !traversedPointsList.contains(
                            Point(
                                currentPoint.x,
                                currentPoint.y - 1
                            )
                        )
                    ) {
                        inputList.add(Point(currentPoint.x, currentPoint.y - 1))
                    }
                    if (currentPoint.y < rows - 1 && !traversedPointsList.contains(
                            Point(
                                currentPoint.x,
                                currentPoint.y + 1
                            )
                        )
                    ) {
                        inputList.add(Point(currentPoint.x, currentPoint.y + 1))
                    }
                }
            }
            displayCanvas()
        }
    }
}