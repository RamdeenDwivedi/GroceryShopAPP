package com.example.groceryshopapp.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.groceryshopapp.ui.theme.CartItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptManager {

    // 1. SHARE VIA WHATSAPP / SMS / EMAIL
    fun shareReceipt(context: Context, cart: List<CartItem>, total: Double) {
        val sb = StringBuilder()
        sb.append("🧾 *GROCERY STORE RECEIPT*\n")
        sb.append("Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("----------------------------\n")
        cart.forEach {
            sb.append("${it.item.name}\n")
            sb.append("  ${it.quantity} x $${"%.2f".format(it.item.price)} = $${"%.2f".format(it.totalPrice)}\n")
        }
        sb.append("----------------------------\n")
        sb.append("*GRAND TOTAL: $${"%.2f".format(total)}*\n\n")
        sb.append("Thank you for shopping with us! 🙏")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Share Receipt via"))
    }

    // 2. PRINT RECEIPT (Thermal / Wi-Fi Printers via Android Print API)
    fun printReceipt(context: Context, cart: List<CartItem>, total: Double) {
        val htmlContent = """
            
            
                
            
            
                GROCERY STORE RECEIPT
                Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}
                
                <table>
                    ${cart.joinToString("") { 
                        "<tr><td>${it.item.name} x${it.quantity}</td><td class='price'>$${"%.2f".format(it.totalPrice)}</td></tr>"
                    }}
                </table>
                <div class="total">GRAND TOTAL: $${"%.2f".format(total)}</div>
                Thank you! Visit Again.
            
            
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Grocery_Receipt")
                printManager.print("Grocery Receipt", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}