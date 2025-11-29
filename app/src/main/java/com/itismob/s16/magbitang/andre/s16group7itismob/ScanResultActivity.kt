package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class ScanResultActivity : AppCompatActivity() {

    private var detectedAmount: Double = 0.0
    private var detectedNotes: String = ""
    private var detectedCategory: String = "Uncategorized"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val tvResult = findViewById<TextView>(R.id.tvOCRText)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmScan)

        // Scenario 1: QR Code (Real-time scan)
        if (intent.hasExtra("QR_DATA")) {
            val qrData = intent.getStringExtra("QR_DATA") ?: ""
            tvResult.text = "QR Code Detected:\n$qrData"
            parseQRData(qrData)
        }
        // Scenario 2: Receipt Image (Captured photo)
        else if (intent.hasExtra("IMAGE_URI")) {
            val imageUriString = intent.getStringExtra("IMAGE_URI")
            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                processReceiptImage(imageUri, tvResult)
            }
        }

        btnConfirm.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("AMOUNT_FROM_SCAN", detectedAmount)
            intent.putExtra("NOTES_FROM_SCAN", detectedNotes)
            intent.putExtra("CATEGORY_FROM_SCAN", detectedCategory)
            startActivity(intent)
            finish()
        }
    }

    // --- RECEIPT PARSING ---
    private fun processReceiptImage(uri: Uri, textView: TextView) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    textView.text = "Scanned Receipt:\n$rawText"

                    // Use Smart Parse instead of just finding the largest number
                    detectedAmount = smartParseReceipt(visionText)
                    detectedNotes = "Scanned Receipt"

                    if (detectedAmount > 0.0) {
                        Toast.makeText(this, "Total Detected: ₱$detectedAmount", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Could not find 'Total'. Please enter manually.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    textView.text = "Failed to scan: ${e.message}"
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Smart Parsing Logic:
     * 1. Looks for lines containing "Total", "Amount Due", etc.
     * 2. Ignores lines containing "Cash", "Change", "Tax", "VAT".
     * 3. Tries to find the price on the same line or the line immediately after.
     */
    private fun smartParseReceipt(visionText: Text): Double {
        val lines = visionText.text.split("\n")
        val numberRegex = Regex("[0-9]+(\\.[0-9]{2})") // Matches 100.00

        var bestAmount = 0.0

        // Strategy 1: Look for "Total" keyword
        for (i in lines.indices) {
            val line = lines[i].lowercase()

            // Skip if it is Cash or Change (the problem you mentioned)
            if (line.contains("cash") || line.contains("change") || line.contains("tender")) {
                continue
            }

            // If we find "Total" or "Amount Due"
            if (line.contains("total") || line.contains("amount due") || line.contains("subtotal")) {

                // A. Check if number is on THIS line (e.g., "Total: 150.00")
                val match = numberRegex.find(lines[i])
                if (match != null) {
                    return match.value.toDouble()
                }

                // B. Check the NEXT line (often Total is a label, price is below)
                if (i + 1 < lines.size) {
                    val nextLineMatch = numberRegex.find(lines[i + 1])
                    if (nextLineMatch != null) {
                        return nextLineMatch.value.toDouble()
                    }
                }
            }
        }

        // Strategy 2: If "Total" keyword failed, fallback to finding the largest valid price
        // BUT exclude integers (years) and absurdly large numbers
        for (line in lines) {
            // Skip Cash/Change lines again to be safe
            if (line.lowercase().contains("cash") || line.lowercase().contains("change")) continue

            val matches = numberRegex.findAll(line)
            for (match in matches) {
                val num = match.value.toDouble()
                // Filter: Must be < 50,000 and not look like a year (2024/2025)
                if (num > bestAmount && num < 50000 && num != 2024.0 && num != 2025.0) {
                    bestAmount = num
                }
            }
        }

        return bestAmount
    }

    // --- QR PARSING ---
    private fun parseQRData(data: String) {
        val parts = data.split(",")
        if (parts.isNotEmpty()) detectedAmount = parts[0].toDoubleOrNull() ?: 0.0
        if (parts.size > 1) detectedCategory = parts[1].trim()
        if (parts.size > 2) detectedNotes = parts[2].trim()
        else detectedNotes = "Scanned QR"
    }
}