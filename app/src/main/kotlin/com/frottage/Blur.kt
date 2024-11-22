package com.frottage


import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val rs = RenderScript.create(context)
    val inputAllocation = Allocation.createFromBitmap(rs, bitmap)
    val outputAllocation = Allocation.createFromBitmap(rs, outputBitmap)
    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    blurScript.setRadius(radius)
    blurScript.setInput(inputAllocation)
    blurScript.forEach(outputAllocation)
    outputAllocation.copyTo(outputBitmap)
    rs.destroy()
    return outputBitmap
}
