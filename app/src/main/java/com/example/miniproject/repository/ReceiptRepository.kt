package com.example.miniproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReceiptRepository(private val firestore: FirebaseFirestore) {

    suspend fun triggerEmail(
        toEmail: String,
        customerName: String,
        itemsDescription: String, // Pass HTML rows here
        totalAmount: Double
    ): Result<Unit> {
        return try {
            val orderId = UUID.randomUUID().toString().take(8).uppercase()

            // 1. Build the HTML Body
            val htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #333;">Order Receipt</h2>
                    <p>Hi <strong>$customerName</strong>,</p>
                    <p>Thank you for your order! (Order ID: #$orderId)</p>
                    <hr>
                    <table style="width: 100%; border-collapse: collapse;">
                        <tr style="background-color: #f2f2f2;">
                            <th style="padding: 10px; text-align: left;">Item</th>
                            <th style="padding: 10px; text-align: right;">Price</th>
                        </tr>
                        $itemsDescription
                        <tr>
                            <td style="padding: 10px; font-weight: bold; border-top: 1px solid #333;">TOTAL</td>
                            <td style="padding: 10px; font-weight: bold; border-top: 1px solid #333; text-align: right;">RM ${String.format("%.2f", totalAmount)}</td>
                        </tr>
                    </table>
                </body>
                </html>
            """.trimIndent()

            // 2. Create the data object for the "Trigger Email" extension
            val emailData = hashMapOf(
                "to" to listOf(toEmail),
                "message" to hashMapOf(
                    "subject" to "Your Receipt: #$orderId",
                    "html" to htmlBody
                )
            )

            // 3. Write to the 'mail' collection (This is what triggers the email!)
            firestore.collection("mail").add(emailData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}