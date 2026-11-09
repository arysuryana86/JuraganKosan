package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import com.example.data.repository.KosRepository
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportService {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getReceiptsDir(context: Context): File {
        val dir = File(context.cacheDir, "receipts")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getAgreementsDir(context: Context): File {
        val dir = File(context.cacheDir, "agreements")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Generates a formal A4 PDF Financial Report.
     */
    fun generateFinancialReportPdf(
        context: Context,
        report: ProfitSharingReport,
        invoices: List<Invoice>,
        expenses: List<Expense>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72 DPI
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Banner (KosConnect Blue)
        paint.color = Color.rgb(21, 101, 192) // #1565C0
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KosConnect Residence", 36f, 42f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("LAPORAN KEUANGAN & ARUS KAS BULANAN", 36f, 62f, paint)
        canvas.drawText("Periode: ${report.periodeBulan} • Jl. Merdeka No. 45, Jakarta Selatan", 36f, 78f, paint)

        // 2. Summary Boxes (3 Columns)
        val boxWidth = 160f
        val boxHeight = 56f
        val boxY = 112f

        // Box 1: Pemasukan (Light Blue)
        paint.color = Color.rgb(227, 242, 253)
        canvas.drawRoundRect(RectF(36f, boxY, 36f + boxWidth, boxY + boxHeight), 8f, 8f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("TOTAL PEMASUKAN", 48f, boxY + 20f, paint)
        paint.textSize = 13f
        canvas.drawText(KosRepository.formatRupiah(report.totalPemasukan), 48f, boxY + 40f, paint)

        // Box 2: Pengeluaran (Light Red/Orange)
        paint.color = Color.rgb(255, 235, 238)
        canvas.drawRoundRect(RectF(212f, boxY, 212f + boxWidth, boxY + boxHeight), 8f, 8f, paint)
        paint.color = Color.rgb(198, 40, 40)
        paint.textSize = 9f
        canvas.drawText("TOTAL PENGELUARAN", 224f, boxY + 20f, paint)
        paint.textSize = 13f
        canvas.drawText(KosRepository.formatRupiah(report.totalPengeluaran), 224f, boxY + 40f, paint)

        // Box 3: Laba Bersih (Light Green)
        paint.color = Color.rgb(232, 245, 233)
        canvas.drawRoundRect(RectF(388f, boxY, 388f + boxWidth, boxY + boxHeight), 8f, 8f, paint)
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 9f
        canvas.drawText("LABA BERSIH", 400f, boxY + 20f, paint)
        paint.textSize = 13f
        canvas.drawText(KosRepository.formatRupiah(report.labaBersih), 400f, boxY + 40f, paint)

        // 3. Section: Rincian Penerimaan Sewa Kamar
        var currentY = 195f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("1. Rincian Tagihan & Penerimaan Sewa", 36f, currentY, paint)

        // Table Header
        currentY += 14f
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(36f, currentY, 559f, currentY + 20f, paint)
        paint.color = Color.rgb(66, 66, 66)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Kamar", 42f, currentY + 14f, paint)
        canvas.drawText("Nama Penghuni", 95f, currentY + 14f, paint)
        canvas.drawText("Sewa Kamar", 230f, currentY + 14f, paint)
        canvas.drawText("Listrik/Denda", 320f, currentY + 14f, paint)
        canvas.drawText("Total", 410f, currentY + 14f, paint)
        canvas.drawText("Status", 495f, currentY + 14f, paint)

        currentY += 20f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f

        val invoiceRows = invoices.take(8) // Fits cleanly on 1 page
        invoiceRows.forEachIndexed { idx, inv ->
            if (idx % 2 == 1) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(36f, currentY, 559f, currentY + 16f, paint)
            }
            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText("Kamar ${inv.roomNomor}", 42f, currentY + 12f, paint)
            canvas.drawText(inv.penghuniNama.take(18), 95f, currentY + 12f, paint)
            canvas.drawText(KosRepository.formatRupiah(inv.nominalKamar), 230f, currentY + 12f, paint)
            canvas.drawText(KosRepository.formatRupiah(inv.tambahanListrik + inv.denda), 320f, currentY + 12f, paint)
            canvas.drawText(KosRepository.formatRupiah(inv.totalTagihan), 410f, currentY + 12f, paint)

            if (inv.statusPembayaran == PaymentStatus.LUNAS) {
                paint.color = Color.rgb(46, 125, 50)
                canvas.drawText("LUNAS", 495f, currentY + 12f, paint)
            } else {
                paint.color = Color.rgb(198, 40, 40)
                canvas.drawText("BELUM", 495f, currentY + 12f, paint)
            }
            currentY += 16f
        }

        // 4. Section: Rincian Biaya Operasional
        currentY += 16f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("2. Rincian Pengeluaran Operasional", 36f, currentY, paint)

        currentY += 14f
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(36f, currentY, 559f, currentY + 20f, paint)
        paint.color = Color.rgb(66, 66, 66)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Kategori", 42f, currentY + 14f, paint)
        canvas.drawText("Deskripsi Keterangan", 160f, currentY + 14f, paint)
        canvas.drawText("Tanggal", 400f, currentY + 14f, paint)
        canvas.drawText("Jumlah", 490f, currentY + 14f, paint)

        currentY += 20f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f

        expenses.take(5).forEachIndexed { idx, exp ->
            if (idx % 2 == 1) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(36f, currentY, 559f, currentY + 16f, paint)
            }
            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText(exp.kategori.label, 42f, currentY + 12f, paint)
            canvas.drawText(exp.deskripsi.take(34), 160f, currentY + 12f, paint)
            canvas.drawText(exp.tanggalPengeluaran, 400f, currentY + 12f, paint)
            paint.color = Color.rgb(198, 40, 40)
            canvas.drawText(KosRepository.formatRupiah(exp.nominal), 490f, currentY + 12f, paint)
            currentY += 16f
        }

        // 5. Section: Alokasi Bagi Hasil Investor
        currentY += 16f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("3. Alokasi Dividen Bagi Hasil Investor", 36f, currentY, paint)

        currentY += 14f
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(36f, currentY, 559f, currentY + 20f, paint)
        paint.color = Color.rgb(66, 66, 66)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Nama Investor / Pemegang Saham", 42f, currentY + 14f, paint)
        canvas.drawText("Porsi Saham", 260f, currentY + 14f, paint)
        canvas.drawText("Nominal Dividen", 360f, currentY + 14f, paint)
        canvas.drawText("Status Penyaluran", 470f, currentY + 14f, paint)

        currentY += 20f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f

        report.payouts.forEachIndexed { idx, po ->
            if (idx % 2 == 1) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(36f, currentY, 559f, currentY + 16f, paint)
            }
            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText(po.investorNama, 42f, currentY + 12f, paint)
            canvas.drawText("${po.sahamPersen}%", 260f, currentY + 12f, paint)
            paint.color = Color.rgb(21, 101, 192)
            canvas.drawText(KosRepository.formatRupiah(po.nominalBagian), 360f, currentY + 12f, paint)

            if (po.status == PayoutStatus.DITRANSFER) {
                paint.color = Color.rgb(46, 125, 50)
                canvas.drawText("DITRANSFER", 470f, currentY + 12f, paint)
            } else {
                paint.color = Color.rgb(230, 81, 0)
                canvas.drawText("MENUNGGU", 470f, currentY + 12f, paint)
            }
            currentY += 16f
        }

        // 6. Footer / Signatures
        currentY = 740f
        paint.color = Color.rgb(180, 180, 180)
        canvas.drawLine(36f, currentY, 559f, currentY, paint)

        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 8f
        val timeStamp = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
        canvas.drawText("Dokumen digital ini digenerate secara otomatis oleh sistem KosConnect pada $timeStamp.", 36f, currentY + 16f, paint)
        canvas.drawText("Kerahasiaan dokumen ini dilindungi dan diverifikasi oleh sistem cloud KosConnect.", 36f, currentY + 28f, paint)

        // Stamp Signature
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Pengelola KosConnect", 430f, currentY + 44f, paint)
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(120, 120, 120)
        canvas.drawText("[Verified Digital Seal]", 430f, currentY + 58f, paint)

        document.finishPage(page)

        val file = File(getReportsDir(context), "Laporan_Keuangan_KosConnect_${System.currentTimeMillis()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    /**
     * Generates a personal Investor Profit Sharing statement (A4 PDF).
     */
    fun generateInvestorReportPdf(
        context: Context,
        investorName: String,
        sharePercent: Double,
        dividendAmount: Long,
        report: ProfitSharingReport,
        pastWithdrawals: List<WithdrawalRecord>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Banner
        paint.color = Color.rgb(21, 101, 192)
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KosConnect Residence", 36f, 42f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("SURAT LAPORAN BAGI HASIL INVESTOR (DIVIDEN)", 36f, 64f, paint)
        canvas.drawText("Periode: ${report.periodeBulan} • Dokumen Resmi Investor", 36f, 82f, paint)

        // Investor Info Card
        val cardY = 120f
        paint.color = Color.rgb(245, 247, 250)
        canvas.drawRoundRect(RectF(36f, cardY, 559f, cardY + 90f), 10f, 10f, paint)

        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 13f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Profil Pemegang Saham", 52f, cardY + 26f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.rgb(80, 80, 80)
        canvas.drawText("Nama Investor:", 52f, cardY + 48f, paint)
        canvas.drawText("Porsi Kepemilikan Saham:", 52f, cardY + 68f, paint)

        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.rgb(21, 101, 192)
        canvas.drawText(investorName, 210f, cardY + 48f, paint)
        canvas.drawText("$sharePercent% dari total keuntungan bersih kos", 210f, cardY + 68f, paint)

        // Dividend Highlight Card
        val divY = cardY + 105f
        paint.color = Color.rgb(232, 245, 233)
        canvas.drawRoundRect(RectF(36f, divY, 559f, divY + 80f), 10f, 10f, paint)

        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("TOTAL ESTIMASI DIVIDEN BULAN INI", 52f, divY + 28f, paint)

        paint.textSize = 24f
        canvas.drawText(KosRepository.formatRupiah(dividendAmount), 52f, divY + 58f, paint)

        // Calculation Breakdown
        var currentY = divY + 110f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Kalkulasi Bagi Hasil Bersih", 36f, currentY, paint)

        currentY += 15f
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(36f, currentY, 559f, currentY + 20f, paint)
        paint.color = Color.rgb(66, 66, 66)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Komponen Finansial", 48f, currentY + 14f, paint)
        canvas.drawText("Nominal (Rp)", 440f, currentY + 14f, paint)

        currentY += 20f
        fun drawCalcRow(label: String, value: String, isBold: Boolean = false, isPositive: Boolean = true) {
            paint.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.color = if (isBold) Color.rgb(21, 101, 192) else Color.rgb(50, 50, 50)
            canvas.drawText(label, 48f, currentY + 12f, paint)
            canvas.drawText(value, 440f, currentY + 12f, paint)
            currentY += 18f
        }

        drawCalcRow("Total Penerimaan Sewa Kamar", KosRepository.formatRupiah(report.totalPemasukan))
        drawCalcRow("Total Beban Operasional & Listrik", "- ${KosRepository.formatRupiah(report.totalPengeluaran)}")
        drawCalcRow("Laba Bersih Kosan", KosRepository.formatRupiah(report.labaBersih), isBold = true)
        drawCalcRow("Porsi Bagi Hasil Investor ($sharePercent%)", KosRepository.formatRupiah(dividendAmount), isBold = true)

        // Past Withdrawals Table
        currentY += 15f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Riwayat Pencairan & Transfer Terakhir", 36f, currentY, paint)

        currentY += 15f
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(36f, currentY, 559f, currentY + 20f, paint)
        paint.color = Color.rgb(66, 66, 66)
        paint.textSize = 9f
        canvas.drawText("Periode", 48f, currentY + 14f, paint)
        canvas.drawText("Tanggal Transfer", 160f, currentY + 14f, paint)
        canvas.drawText("Nomor Rekening", 300f, currentY + 14f, paint)
        canvas.drawText("Nominal", 460f, currentY + 14f, paint)

        currentY += 20f
        pastWithdrawals.take(4).forEachIndexed { idx, w ->
            if (idx % 2 == 1) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(36f, currentY, 559f, currentY + 16f, paint)
            }
            paint.color = Color.rgb(33, 33, 33)
            paint.textSize = 8.5f
            canvas.drawText(w.periodeBulan, 48f, currentY + 12f, paint)
            canvas.drawText(w.tanggalTransfer, 160f, currentY + 12f, paint)
            canvas.drawText(w.nomorRekening, 300f, currentY + 12f, paint)
            paint.color = Color.rgb(46, 125, 50)
            canvas.drawText(KosRepository.formatRupiah(w.nominal), 460f, currentY + 12f, paint)
            currentY += 16f
        }

        // Footer
        currentY = 740f
        paint.color = Color.rgb(180, 180, 180)
        canvas.drawLine(36f, currentY, 559f, currentY, paint)

        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 8f
        val timeStamp = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
        canvas.drawText("Laporan bagi hasil resmi diterbitkan oleh PT KosConnect Investama pada $timeStamp.", 36f, currentY + 16f, paint)
        canvas.drawText("Dana dividen ditransfer langsung ke rekening terdaftar sesuai jadwal akhir bulan.", 36f, currentY + 28f, paint)

        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Direktur Keuangan KosConnect", 400f, currentY + 44f, paint)
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawText("SEAL: VERIFIED AUDITED", 400f, currentY + 58f, paint)

        document.finishPage(page)

        val file = File(getReportsDir(context), "Laporan_Dividen_${investorName.replace(" ", "_")}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    /**
     * Generates an official payment receipt PDF for a tenant.
     */
    fun generateReceiptPdf(context: Context, invoice: Invoice): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(420, 595, 1).create() // Compact A5 Portrait
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Receipt Card Container
        paint.color = Color.rgb(21, 101, 192)
        canvas.drawRect(0f, 0f, 420f, 75f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("KosConnect Residence", 28f, 36f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("KUITANSI PEMBAYARAN SEWA RESMI", 28f, 54f, paint)

        // Receipt Meta
        var currentY = 100f
        paint.color = Color.rgb(80, 80, 80)
        paint.textSize = 9f
        canvas.drawText("No. Kuitansi: ${invoice.paymentGatewayRef ?: "INV-${invoice.id.take(8)}"}", 28f, currentY, paint)
        canvas.drawText("Tanggal: ${invoice.tanggalBayar ?: "Hari ini"}", 280f, currentY, paint)

        currentY += 24f
        paint.color = Color.rgb(245, 247, 250)
        canvas.drawRoundRect(RectF(28f, currentY, 392f, currentY + 80f), 8f, 8f, paint)

        paint.color = Color.rgb(50, 50, 50)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Telah diterima dari:", 40f, currentY + 22f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 13f
        canvas.drawText("${invoice.penghuniNama} (Kamar ${invoice.roomNomor})", 40f, currentY + 40f, paint)

        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Untuk Pembayaran: Sewa Kamar Kos Periode ${invoice.bulanTagihan}", 40f, currentY + 58f, paint)
        canvas.drawText("Metode: ${invoice.metodePembayaran?.label ?: "Transfer Bank / QRIS"}", 40f, currentY + 70f, paint)

        // Cost Breakdown Table
        currentY += 100f
        paint.color = Color.rgb(230, 235, 242)
        canvas.drawRect(28f, currentY, 392f, currentY + 20f, paint)
        paint.color = Color.rgb(40, 40, 40)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Rincian Tagihan", 36f, currentY + 14f, paint)
        canvas.drawText("Jumlah", 330f, currentY + 14f, paint)

        currentY += 22f
        paint.typeface = Typeface.DEFAULT
        fun drawLineItem(label: String, amount: Long) {
            paint.color = Color.rgb(60, 60, 60)
            canvas.drawText(label, 36f, currentY + 12f, paint)
            canvas.drawText(KosRepository.formatRupiah(amount), 330f, currentY + 12f, paint)
            currentY += 16f
        }

        drawLineItem("Biaya Pokok Sewa Kamar", invoice.nominalKamar)
        if (invoice.tambahanListrik > 0) drawLineItem("Tambahan Listrik / Elektronik", invoice.tambahanListrik)
        if (invoice.denda > 0) drawLineItem("Denda Keterlambatan", invoice.denda)

        // Total Line
        currentY += 8f
        paint.color = Color.rgb(200, 200, 200)
        canvas.drawLine(28f, currentY, 392f, currentY, paint)

        currentY += 16f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 12f
        paint.color = Color.rgb(21, 101, 192)
        canvas.drawText("TOTAL PEMBAYARAN", 36f, currentY + 14f, paint)
        canvas.drawText(KosRepository.formatRupiah(invoice.totalTagihan), 310f, currentY + 14f, paint)

        // Lunas Stamp
        val stampY = currentY + 55f
        paint.color = Color.rgb(232, 245, 233)
        canvas.drawRoundRect(RectF(140f, stampY, 280f, stampY + 45f), 8f, 8f, paint)
        paint.color = Color.rgb(46, 125, 50)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(RectF(140f, stampY, 280f, stampY + 45f), 8f, 8f, paint)
        paint.style = Paint.Style.FILL
        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("L U N A S", 175f, stampY + 28f, paint)

        // Bottom note
        paint.color = Color.rgb(120, 120, 120)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Kuitansi digital ini sah dan diterbitkan otomatis oleh sistem KosConnect.", 28f, 550f, paint)
        canvas.drawText("Simpan kuitansi ini sebagai bukti pembayaran sewa yang sah.", 28f, 562f, paint)

        document.finishPage(page)

        val file = File(getReceiptsDir(context), "Kuitansi_Kamar_${invoice.roomNomor}_${invoice.id.take(6)}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    /**
     * Generates a formal 2-page A4 PDF Digital Tenancy Agreement.
     */
    fun generateTenancyAgreementPdf(
        context: Context,
        agreement: TenancyAgreement
    ): File {
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // ================= PAGE 1 =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Header Banner (KosConnect Blue)
        paint.color = Color.rgb(21, 101, 192)
        canvas1.drawRect(0f, 0f, 595f, 75f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("KosConnect Residence", 36f, 38f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas1.drawText("MANAJEMEN HUNIAN KOST MODERN & TERINTEGRASI", 36f, 54f, paint)
        canvas1.drawText("Jl. Merdeka No. 45, Jakarta Selatan • Telp/WhatsApp: 0812-9988-7766", 36f, 67f, paint)

        // Title of Agreement
        var y1 = 105f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        val titleText = "SURAT PERJANJIAN SEWA MENYEWA KAMAR KOS"
        val titleWidth = paint.measureText(titleText)
        canvas1.drawText(titleText, (595f - titleWidth) / 2f, y1, paint)

        y1 += 16f
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        val docNum = "Nomor Dokumen: ${agreement.nomorSurat}"
        val docNumWidth = paint.measureText(docNum)
        canvas1.drawText(docNum, (595f - docNumWidth) / 2f, y1, paint)

        y1 += 22f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9.5f
        canvas1.drawText("Pada hari ini, disepakati perjanjian sewa kamar kos dengan rincian pihak-pihak sebagai berikut:", 36f, y1, paint)

        // Box Pihak Pertama
        y1 += 12f
        paint.color = Color.rgb(240, 245, 252)
        canvas1.drawRoundRect(RectF(36f, y1, 559f, y1 + 72f), 6f, 6f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas1.drawText("I. PIHAK PERTAMA (PENGELOLA / PEMILIK KOS)", 46f, y1 + 18f, paint)

        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("Nama Lengkap : ${agreement.pihakPertamaNama} (${agreement.pihakPertamaJabatan})", 46f, y1 + 34f, paint)
        canvas1.drawText("Alamat Kos    : ${agreement.pihakPertamaAlamat}", 46f, y1 + 48f, paint)
        canvas1.drawText("No. Kontak    : ${agreement.pihakPertamaHp} (Selanjutnya disebut sebagai PIHAK PERTAMA)", 46f, y1 + 62f, paint)

        // Box Pihak Kedua
        y1 += 80f
        paint.color = Color.rgb(240, 245, 252)
        canvas1.drawRoundRect(RectF(36f, y1, 559f, y1 + 84f), 6f, 6f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas1.drawText("II. PIHAK KEDUA (PENYEWA / PENGHUNI)", 46f, y1 + 18f, paint)

        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("Nama Lengkap  : ${agreement.pihakKeduaNama}", 46f, y1 + 34f, paint)
        canvas1.drawText("NIK / No KTP  : ${agreement.pihakKeduaNik} • Pekerjaan: ${agreement.pihakKeduaPekerjaan}", 46f, y1 + 48f, paint)
        canvas1.drawText("No. WhatsApp  : ${agreement.pihakKeduaHp}", 46f, y1 + 62f, paint)
        canvas1.drawText("Alamat Asal   : ${agreement.pihakKeduaAlamatKtp} (Selanjutnya disebut sebagai PIHAK KEDUA)", 46f, y1 + 76f, paint)

        y1 += 94f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("Kedua belah pihak sepakat mengadakan perjanjian sewa menyewa dengan pasal-pasal berikut:", 36f, y1, paint)

        // PASAL 1
        y1 += 18f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas1.drawText("PASAL 1: OBJEK SEWA & JANGKA WAKTU", 36f, y1, paint)

        y1 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("1. PIHAK PERTAMA menyewakan kamar nomor ${agreement.roomNomor} (${agreement.tipeKamar}) kepada PIHAK KEDUA.", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("2. Perjanjian sewa ini berlaku terhitung sejak tanggal ${agreement.tanggalMulai} sampai dengan", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("   tanggal ${agreement.tanggalSelesai}. Perpanjangan sewa wajib dikonfirmasi paling lambat H-14.", 36f, y1, paint)

        // PASAL 2
        y1 += 18f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas1.drawText("PASAL 2: BIAYA SEWA, JATUH TEMPO, & UANG JAMINAN (DEPOSIT)", 36f, y1, paint)

        y1 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("1. Tarif sewa kamar ditetapkan sebesar ${KosRepository.formatRupiah(agreement.hargaSewaBulanan)} per bulan.", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("2. Jatuh tempo pembayaran sewa adalah tanggal ${agreement.jatuhTempoTgl} setiap bulan berjalan.", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("3. Uang jaminan (deposit) sebesar ${KosRepository.formatRupiah(agreement.depositJaminan)} dibayarkan di awal dan akan dikembalikan utuh", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("   H+1 setelah masa sewa berakhir pasca inspeksi fisik kamar dan kelengkapan fasilitas.", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("4. Denda keterlambatan ditetapkan sebesar ${KosRepository.formatRupiah(agreement.dendaKeterlambatanPerHari)} per hari kalender setelah tanggal jatuh tempo.", 36f, y1, paint)

        // PASAL 3
        y1 += 18f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas1.drawText("PASAL 3: FASILITAS KAMAR & PEMELIHARAAN", 36f, y1, paint)

        y1 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas1.drawText("1. Fasilitas inventaris kamar yang diserahkan dalam kondisi baik meliputi:", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("   • ${agreement.fasilitas.joinToString("  •  ")}", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("2. Kerusakan fasilitas mayor/struktural (AC, kelistrikan utama, instalasi pipa) ditanggung PIHAK PERTAMA.", 36f, y1, paint)
        y1 += 13f
        canvas1.drawText("3. Kerusakan perabot akibat kelalaian atau pemakaian tidak wajar menjadi tanggung jawab PIHAK KEDUA.", 36f, y1, paint)

        // Page 1 Footer
        paint.color = Color.rgb(150, 150, 150)
        paint.textSize = 8.5f
        canvas1.drawText("KosConnect Residence E-Agreement • Dokumen Elektronik Sah • Halaman 1 dari 2", 36f, 815f, paint)

        document.finishPage(page1)

        // ================= PAGE 2 =================
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        val canvas2 = page2.canvas

        // Header Page 2
        paint.color = Color.rgb(240, 243, 246)
        canvas2.drawRect(0f, 0f, 595f, 38f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("Surat Perjanjian Sewa KosConnect Residence — Kamar ${agreement.roomNomor} (${agreement.pihakKeduaNama})", 36f, 24f, paint)
        canvas2.drawText("Halaman 2 dari 2", 485f, 24f, paint)

        var y2 = 60f

        // PASAL 4
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("PASAL 4: TATA TERTIB & JAM MALAM", 36f, y2, paint)

        y2 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("1. Pintu gerbang utama ditutup dan dikunci pukul 23:00 WIB demi keamanan bersama seluruh penghuni.", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("2. Tamu dilarang menginap tanpa persetujuan tertulis pengelola kos. Tamu berkunjung maksimal pukul 21:00 WIB.", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("3. Wajib menjaga ketertiban, kebersihan, dan ketenangan lingkungan. Dilarang menyalakan suara/musik bising", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("   setelah pukul 22:00 WIB yang dapat mengganggu penghuni lainnya.", 36f, y2, paint)

        // PASAL 5
        y2 += 20f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("PASAL 5: LARANGAN MUTLAK & SANKSI", 36f, y2, paint)

        y2 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("1. DILARANG KERAS membawa, menyimpan, memperjualbelikan, atau mengonsumsi NARKOTIKA, obat-obatan terlarang,", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("   minuman keras beralkohol, senjata tajam/api, serta melakukan perbuatan asusila atau perjudian.", 36f, y2, paint)
        y2 += 13f
        paint.color = Color.rgb(198, 40, 40)
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("   Pelanggaran atas ketentuan ini berakibat pemutusan sewa seketika tanpa pengembalian uang sewa/deposit", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("   dan langsung dilaporkan kepada pihak Kepolisian Republik Indonesia.", 36f, y2, paint)

        y2 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("2. Dilarang merokok atau menggunakan rokok elektrik / vape di dalam kamar tidur untuk mencegah bahaya kebakaran.", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("3. Dilarang mengalihkan, meminjamkan, atau menyewakan ulang kamar kepada pihak ketiga (sub-sewa).", 36f, y2, paint)

        // PASAL 6
        y2 += 20f
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("PASAL 6: PENGAKHIRAN SEWA & PENYELESAIAN PERSELISIHAN", 36f, y2, paint)

        y2 += 14f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("1. PIHAK KEDUA wajib menyampaikan pemberitahuan perpanjangan atau rencana checkout paling lambat H-14.", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("2. Segala bentuk perselisihan yang timbul akan diselesaikan secara musyawarah dan mufakat dengan domisili hukum", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("   yang disepakati di Pengadilan Negeri Jakarta Selatan.", 36f, y2, paint)

        // Closing Statement
        y2 += 24f
        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("Demikian Surat Perjanjian Sewa ini dibuat, disepakati, dan ditandatangani secara elektronik dengan sah,", 36f, y2, paint)
        y2 += 13f
        canvas2.drawText("mengikat, dan mempunyai kekuatan hukum penuh sesuai UU ITE No. 11 Tahun 2008.", 36f, y2, paint)

        // SIGNATURES BOXES (Pihak I, Meterai Digital, Pihak II)
        y2 += 25f
        val signBoxWidth = 155f
        val signBoxHeight = 105f

        // Box 1: Pihak Pertama (Pengelola)
        paint.color = Color.rgb(245, 248, 252)
        canvas2.drawRoundRect(RectF(36f, y2, 36f + signBoxWidth, y2 + signBoxHeight), 8f, 8f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas2.drawRoundRect(RectF(36f, y2, 36f + signBoxWidth, y2 + signBoxHeight), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("PIHAK PERTAMA (PENGELOLA)", 44f, y2 + 18f, paint)

        // Stamp badge
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("✓ DIGITALLY SIGNED", 44f, y2 + 45f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText(agreement.signedAt, 44f, y2 + 58f, paint)

        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText(agreement.pihakPertamaNama, 44f, y2 + 82f, paint)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText(agreement.pihakPertamaJabatan, 44f, y2 + 94f, paint)

        // Box 2: Meterai Elektronik 10.000
        val meteraiX = 220f
        paint.color = Color.rgb(254, 248, 237)
        canvas2.drawRoundRect(RectF(meteraiX, y2 + 10f, meteraiX + signBoxWidth - 20f, y2 + signBoxHeight - 10f), 8f, 8f, paint)
        paint.color = Color.rgb(230, 81, 0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas2.drawRoundRect(RectF(meteraiX, y2 + 10f, meteraiX + signBoxWidth - 20f, y2 + signBoxHeight - 10f), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(230, 81, 0)
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("METERAI ELEKTRONIK", meteraiX + 16f, y2 + 32f, paint)
        paint.textSize = 12f
        canvas2.drawText("10000", meteraiX + 44f, y2 + 50f, paint)
        paint.textSize = 6.5f
        paint.color = Color.rgb(120, 120, 120)
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("TGL: ${agreement.tanggalSurat}", meteraiX + 22f, y2 + 65f, paint)
        canvas2.drawText(agreement.meteraiDigitalSerial.take(18), meteraiX + 12f, y2 + 76f, paint)

        // Box 3: Pihak Kedua (Penyewa)
        val sign2X = 390f
        paint.color = Color.rgb(245, 248, 252)
        canvas2.drawRoundRect(RectF(sign2X, y2, sign2X + signBoxWidth + 14f, y2 + signBoxHeight), 8f, 8f, paint)
        paint.color = Color.rgb(21, 101, 192)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas2.drawRoundRect(RectF(sign2X, y2, sign2X + signBoxWidth + 14f, y2 + signBoxHeight), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("PIHAK KEDUA (PENYEWA)", sign2X + 8f, y2 + 18f, paint)

        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText("✓ DIGITALLY SIGNED", sign2X + 8f, y2 + 45f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText(agreement.signedAt, sign2X + 8f, y2 + 58f, paint)

        paint.color = Color.rgb(33, 33, 33)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas2.drawText(agreement.pihakKeduaNama, sign2X + 8f, y2 + 82f, paint)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("Penghuni Kamar ${agreement.roomNomor}", sign2X + 8f, y2 + 94f, paint)

        // Bottom verification barcode / text
        y2 += signBoxHeight + 20f
        paint.color = Color.rgb(240, 243, 246)
        canvas2.drawRoundRect(RectF(36f, y2, 559f, y2 + 32f), 6f, 6f, paint)
        paint.color = Color.rgb(80, 80, 80)
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        canvas2.drawText("Kode Otentikasi Digital: ${agreement.digitalSignatureHash} • Status: ${agreement.status.label}", 44f, y2 + 14f, paint)
        canvas2.drawText("Surat perjanjian ini sah dan memiliki kekuatan pembuktian digital sesuai hukum Republik Indonesia.", 44f, y2 + 25f, paint)

        document.finishPage(page2)

        val file = File(getAgreementsDir(context), "Surat_Perjanjian_Kamar_${agreement.roomNomor}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    /**
     * Generates a detailed CSV file compatible with Microsoft Excel and Google Sheets.
     */
    fun generateFinancialCsv(
        context: Context,
        report: ProfitSharingReport,
        invoices: List<Invoice>,
        expenses: List<Expense>
    ): File {
        val file = File(getReportsDir(context), "Laporan_KosConnect_${report.periodeBulan.replace(" ", "_")}.csv")
        val writer = FileWriter(file)

        // Write UTF-8 BOM so Excel opens indonesian characters correctly
        writer.write("\uFEFF")

        // Section 1: Executive Summary
        writer.append("LAPORAN KEUANGAN KOSCONNECT RESIDENCE\n")
        writer.append("Periode,${report.periodeBulan}\n")
        writer.append("Tanggal Ekspor,${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())}\n\n")

        writer.append("RINGKASAN EKSEKUTIF\n")
        writer.append("Indikator,Jumlah (Rp)\n")
        writer.append("Total Pemasukan,${report.totalPemasukan}\n")
        writer.append("Total Pengeluaran,${report.totalPengeluaran}\n")
        writer.append("Laba Bersih Kosan,${report.labaBersih}\n\n")

        // Section 2: Invoices Detail
        writer.append("RINCIAN PENERIMAAN SEWA KAMAR\n")
        writer.append("No Kamar,Nama Penghuni,Bulan Tagihan,Sewa Pokok,Tambahan Listrik,Denda,Total Tagihan,Status Pembayaran,Metode Pembayaran,Tanggal Bayar\n")
        invoices.forEach { inv ->
            writer.append("${inv.roomNomor},\"${inv.penghuniNama}\",\"${inv.bulanTagihan}\",${inv.nominalKamar},${inv.tambahanListrik},${inv.denda},${inv.totalTagihan},${inv.statusPembayaran.label},\"${inv.metodePembayaran?.label ?: "-"}\",\"${inv.tanggalBayar ?: "-"}\"\n")
        }
        writer.append("\n")

        // Section 3: Expenses Detail
        writer.append("RINCIAN PENGELUARAN OPERASIONAL\n")
        writer.append("Kategori,Keterangan,Tanggal Pengeluaran,Nominal (Rp)\n")
        expenses.forEach { exp ->
            writer.append("\"${exp.kategori.label}\",\"${exp.deskripsi}\",\"${exp.tanggalPengeluaran}\",${exp.nominal}\n")
        }
        writer.append("\n")

        // Section 4: Investor Dividends
        writer.append("ALOKASI BAGI HASIL INVESTOR\n")
        writer.append("Nama Pemegang Saham,Persentase (%),Nominal Dividen (Rp),Status Penyaluran,Rekening Tujuan\n")
        report.payouts.forEach { po ->
            writer.append("\"${po.investorNama}\",${po.sahamPersen},${po.nominalBagian},\"${po.status.label}\",\"${po.nomorRekening}\"\n")
        }

        writer.flush()
        writer.close()
        return file
    }

    /**
     * Shares a file via Android Intent Chooser.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Terlampir $title dari aplikasi KosConnect.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Bagikan $title"))
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak ada aplikasi untuk membagikan file ini: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens a file directly with the default device viewer.
     */
    fun openFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to sharing if no direct viewer installed
            shareFile(context, file, mimeType, file.name)
        }
    }
}
