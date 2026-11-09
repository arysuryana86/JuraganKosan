package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaHelper {
    private const val TAG = "MediaHelper"

    /**
     * Copies a picked image from an external/content URI into the app's internal sandbox storage.
     * This ensures the URI remains readable indefinitely across app restarts without security exceptions.
     */
    fun savePickedImageToInternalStorage(
        context: Context,
        sourceUri: Uri,
        prefix: String = "photo"
    ): String? {
        return try {
            val mediaDir = File(context.filesDir, "media")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val filename = "${prefix}_${UUID.randomUUID()}.jpg"
            val destFile = File(mediaDir, filename)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyalin gambar ke internal storage", e)
            null
        }
    }

    /**
     * Generates a realistic mock receipt or inspection card bitmap for demo/testing purposes
     * when real camera/gallery input is bypassed.
     */
    fun createSampleReceiptOrDamageImage(
        context: Context,
        title: String,
        subtitle: String,
        type: String = "RECEIPT"
    ): String? {
        return try {
            val mediaDir = File(context.filesDir, "media")
            if (!mediaDir.exists()) mediaDir.mkdirs()

            val filename = "sample_${type.lowercase()}_${UUID.randomUUID()}.jpg"
            val destFile = File(mediaDir, filename)

            val width = 600
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val isReceipt = type.equals("RECEIPT", ignoreCase = true)
            val bgColor = if (isReceipt) Color.rgb(240, 244, 248) else Color.rgb(255, 235, 238)
            canvas.drawColor(bgColor)

            val borderPaint = Paint().apply {
                color = if (isReceipt) Color.rgb(21, 101, 192) else Color.rgb(198, 40, 40)
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(12f, 12f, width - 12f, height - 12f, borderPaint)

            val headerPaint = Paint().apply {
                color = if (isReceipt) Color.rgb(21, 101, 192) else Color.rgb(198, 40, 40)
                textSize = 34f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(title, 40f, 90f, headerPaint)

            val subPaint = Paint().apply {
                color = Color.rgb(66, 66, 66)
                textSize = 24f
                isAntiAlias = true
            }
            canvas.drawText(subtitle, 40f, 150f, subPaint)
            canvas.drawText("KosConnect Digital Inspection", 40f, 210f, subPaint)

            val stampPaint = Paint().apply {
                color = if (isReceipt) Color.rgb(46, 125, 50) else Color.rgb(230, 81, 0)
                textSize = 32f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val stampText = if (isReceipt) "[✓ BUKTI TRANSFER SAH]" else "[! DOKUMENTASI KERUSAKAN]"
            canvas.drawText(stampText, 40f, 320f, stampPaint)

            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuat sample gambar", e)
            null
        }
    }
}
