package com.example.miniproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// 1. Update Data Class to include Image
data class ReceiptItem(
    val name: String,
    val variant: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val imageUrl: String
)

class ReceiptRepository(private val firestore: FirebaseFirestore) {

    suspend fun triggerEmail(
        toEmail: String,
        orderId: String,
        customerName: String,
        items: List<ReceiptItem>,
        subTotal: Double,
        deliveryFee: Double,
        discountAmount: Double
    ): Result<Unit> {
        return try {
            val finalTotal = subTotal + deliveryFee - discountAmount

            // 2. Generate Rows with Image Column
            val itemsRowsHtml = items.joinToString("") { item ->
                """
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; width: 60px;">
                        <img src="${item.imageUrl}" alt="Item" width="50" height="50" style="border-radius: 4px; object-fit: cover; display: block;">
                    </td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">
                        ${item.name} <br>
                        <span style="color: #888; font-size: 12px;">${item.variant}</span>
                    </td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: center;">${item.quantity}</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">RM ${String.format("%.2f", item.unitPrice)}</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">RM ${String.format("%.2f", item.totalPrice)}</td>
                </tr>
                """.trimIndent()
            }

            val htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; padding: 20px;">
                    <div style="max-width: 600px; border: 1px solid #eee; padding: 20px; border-radius: 8px; background-color: #ffffff;">
                        <h2 style="color: #4CAF50;">Order Receipt</h2>
                        <p>Hi <strong>$customerName</strong>,</p>
                        <p>Thank you for your order! <strong>#$orderId</strong></p>
                        <hr style="border: 0; border-top: 1px solid #eee;">
                        
                        <table style="width: 100%; border-collapse: collapse; margin-top: 15px;">
                            <tr style="background-color: #f8f9fa;">
                                <th style="padding: 10px; text-align: left; width: 60px;"></th> <th style="padding: 10px; text-align: left;">Item</th>
                                <th style="padding: 10px; text-align: center;">Qty</th>
                                <th style="padding: 10px; text-align: right;">Unit Price</th>
                                <th style="padding: 10px; text-align: right;">Total</th>
                            </tr>
                            $itemsRowsHtml
                        </table>
                        
                        <table style="width: 100%; margin-top: 20px; border-top: 2px solid #333;">
                             <tr>
                                <td style="padding: 5px; text-align: right;">Subtotal:</td>
                                <td style="padding: 5px; text-align: right; width: 120px;">RM ${String.format("%.2f", subTotal)}</td>
                            </tr>
                            <tr>
                                <td style="padding: 5px; text-align: right;">Delivery Fee:</td>
                                <td style="padding: 5px; text-align: right;">RM ${String.format("%.2f", deliveryFee)}</td>
                            </tr>
                            <tr>
                                <td style="padding: 5px; text-align: right;">Discount:</td>
                                <td style="padding: 5px; text-align: right; color: red;">- RM ${String.format("%.2f", discountAmount)}</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; text-align: right; font-weight: bold; font-size: 16px;">TOTAL:</td>
                                <td style="padding: 10px; text-align: right; font-weight: bold; font-size: 16px; color: #4CAF50;">RM ${String.format("%.2f", finalTotal)}</td>
                            </tr>
                        </table>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val emailData = hashMapOf(
                "to" to listOf(toEmail),
                "message" to hashMapOf(
                    "subject" to "Receipt for Order #$orderId",
                    "html" to htmlBody
                )
            )

            firestore.collection("mail").add(emailData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun sendStatusUpdateEmail(
        toEmail: String,
        orderId: String,
        newStatus: String,
        items: List<ReceiptItem>
    ): Result<Unit> {
        return try {
            val color = when (newStatus) {
                "Shipped" -> "#9C27B0"
                "Completed" -> "#4CAF50"
                else -> "#2196F3"
            }

            // Generate items row with image
            val itemsRowsHtml = items.joinToString("") { item ->
                """
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; width: 60px;">
                        <img src="${item.imageUrl}" alt="Item" width="50" height="50" style="border-radius: 4px; object-fit: cover; display: block;">
                    </td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">
                        ${item.name} <br>
                        <span style="color: #888; font-size: 12px;">${item.variant}</span>
                    </td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: center;">${item.quantity}</td>
                </tr>
                """.trimIndent()
            }

            val htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <div style="max-width: 500px; border: 1px solid #eee; padding: 20px; border-radius: 8px;">
                        <h2 style="color: $color;">Order Status Update</h2>
                        <p>Your order <strong>#$orderId</strong> has been updated.</p>
                        
                        <div style="background-color: #f9f9f9; padding: 15px; border-left: 5px solid $color; margin: 20px 0;">
                            <p style="margin: 0; font-size: 14px; color: #666;">New Status:</p>
                            <h3 style="margin: 5px 0 0 0; color: $color;">$newStatus</h3>
                        </div>

                        <p><strong>Items in this order:</strong></p>
                        <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
                             <tr style="background-color: #f8f9fa;">
                                <th style="padding: 10px; text-align: left; width: 60px;"></th>
                                <th style="padding: 10px; text-align: left;">Item</th>
                                <th style="padding: 10px; text-align: center;">Qty</th>
                            </tr>
                            $itemsRowsHtml
                        </table>

                        <p>Thank you for shopping with us!</p>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val emailData = hashMapOf(
                "to" to listOf(toEmail),
                "message" to hashMapOf(
                    "subject" to "Order #$orderId Update: $newStatus",
                    "html" to htmlBody
                )
            )

            firestore.collection("mail").add(emailData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}