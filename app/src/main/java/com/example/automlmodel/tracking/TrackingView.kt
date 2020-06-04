package com.example.automlmodel.tracking

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.View

class TrackingView : View {
    var trackBounds: List<RectF>
    val fillTrack = Paint().apply {
        color = Color.WHITE
        strokeWidth = 10f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL_AND_STROKE
        alpha = 25

    }
    val strokeTrack = Paint().apply {
        color = Color.WHITE
        strokeWidth = 10f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        alpha = 50

    }

    constructor(context: Context) : super(context) {
        trackBounds = listOf<RectF>()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        trackBounds = listOf<RectF>()
    }

    fun calculateSize(rect: Rect): RectF {
        // tinh scale giua anh va view
        val scaleX = viewSize.width / 480f
        val scaleY = viewSize.height / 640f
    //  val scale = Math.max(scaleX, scaleY) // chon scale max

        // val scaledSize = Size(Math.ceil(rect.width() * scale).toInt(), Math.ceil(rect.height() * scale).toInt()) // tinh do dai chinh xac


        // Calculate offset (we need to center the overlay on the target)
        val offsetX = (rect.width() * scaleX - viewSize.width) / 2f
        //   val offsetY = (scaledSize.height - viewSize.height) / 2f

        // Map bounding box
        val mappedBoundingBox = RectF().apply {
            left = rect.right * scaleX
            top = rect.top * scaleY
            right = rect.left * scaleX
            bottom = rect.bottom * scaleY
        }

        return mappedBoundingBox
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        trackBounds.forEach {
            canvas?.drawRoundRect(it, 25f, 25f, fillTrack)
            canvas?.drawRoundRect(it, 25f, 25f, strokeTrack)
        }


    }

    lateinit var viewSize: Size
    fun updateViewSize(size2: Size) {
        viewSize = size2
        postInvalidate()
    }

    fun updateBound(newBounds: List<Rect>) {

        trackBounds = arrayListOf<RectF>().apply {
            newBounds.forEach {
                this.add(calculateSize(it))
            }
        }


        postInvalidate()
    }
}