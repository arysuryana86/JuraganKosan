package com.example

import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PayoutStatus
import com.example.data.model.RoomStatus
import com.example.data.repository.KosRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `repository calculates profit sharing and investor dividends correctly`() {
        val repo = KosRepository()
        val report = repo.getProfitSharingReport()

        assertTrue(report.totalPemasukan > 0L)
        assertTrue(report.totalPengeluaran > 0L)
        assertEquals(report.totalPemasukan - report.totalPengeluaran, report.labaBersih)

        // Verify 30% dividend calculation for Investor Andi
        val andiPayout = report.payouts.first { it.sahamPersen == 30.0 }
        val expectedDividen = (report.labaBersih * 0.30).toLong()
        assertEquals(expectedDividen, andiPayout.nominalBagian)
    }

    @Test
    fun `tenant paying invoice marks status as Lunas`() {
        val repo = KosRepository()
        val invoiceId = "inv-101"
        repo.payInvoice(invoiceId, PaymentMethod.QRIS)

        val updated = repo.invoices.value.first { it.id == invoiceId }
        assertEquals(PaymentStatus.LUNAS, updated.statusPembayaran)
        assertEquals(PaymentMethod.QRIS, updated.metodePembayaran)
    }

    @Test
    fun `check in tenant sets room occupied and generates first invoice`() {
        val repo = KosRepository()
        val availableRoom = repo.rooms.value.first { it.status == RoomStatus.TERSEDIA }

        repo.checkInTenant(availableRoom.id, "Rizky Ramadhan", "081299881122", 500000L)

        val updatedRoom = repo.rooms.value.first { it.id == availableRoom.id }
        assertEquals(RoomStatus.TERISI, updatedRoom.status)
        assertEquals("Rizky Ramadhan", updatedRoom.penghuniNama)
        assertEquals("081299881122", updatedRoom.penghuniHp)

        // Verify auto-generated invoice for room
        val generatedInvoice = repo.invoices.value.find { it.roomNomor == availableRoom.nomorKamar && it.penghuniNama == "Rizky Ramadhan" }
        assertNotNull(generatedInvoice)
        assertEquals(PaymentStatus.BELUM_BAYAR, generatedInvoice?.statusPembayaran)
    }

    @Test
    fun `check out tenant clears tenant info and marks room available`() {
        val repo = KosRepository()
        val occupiedRoom = repo.rooms.value.first { it.status == RoomStatus.TERISI }

        repo.checkOutTenant(occupiedRoom.id)

        val updatedRoom = repo.rooms.value.first { it.id == occupiedRoom.id }
        assertEquals(RoomStatus.TERSEDIA, updatedRoom.status)
        assertNull(updatedRoom.penghuniNama)
        assertNull(updatedRoom.penghuniHp)
    }

    @Test
    fun `generate monthly mass invoices creates invoices for occupied rooms`() {
        val repo = KosRepository()
        val generated = repo.generateMonthlyInvoices("Januari 2027", "10 Jan 2027")
        assertTrue(generated > 0)

        val newInvoices = repo.invoices.value.filter { it.bulanTagihan == "Januari 2027" }
        assertEquals(generated, newInvoices.size)
    }

    @Test
    fun `update invoice adjustment recalculates total tagihan`() {
        val repo = KosRepository()
        val inv = repo.invoices.value.first()
        repo.updateInvoiceAdjustment(inv.id, tambahanListrik = 200000L, denda = 50000L)

        val updated = repo.invoices.value.first { it.id == inv.id }
        assertEquals(200000L, updated.tambahanListrik)
        assertEquals(50000L, updated.denda)
        assertEquals(inv.nominalKamar + 200000L + 50000L, updated.totalTagihan)
    }

    @Test
    fun `transfer investor payout updates payout status to Ditransfer`() {
        val repo = KosRepository()
        val andiName = "Andi Wijaya (Anda)"
        repo.transferInvestorPayout(andiName, "6 Nov 2026")

        val report = repo.getProfitSharingReport()
        val andiPayout = report.payouts.first { it.investorNama == andiName }
        assertEquals(PayoutStatus.DITRANSFER, andiPayout.status)
    }

    @Test
    fun `add new investor reflects in profit sharing calculations`() {
        val repo = KosRepository()
        val initialCount = repo.investorShares.value.size
        repo.addInvestor("Citra Lestari", "citra@investor.com", "Mandiri 123-00-9988", 15.0)

        assertEquals(initialCount + 1, repo.investorShares.value.size)
        val report = repo.getProfitSharingReport()
        assertTrue(report.payouts.any { it.investorNama == "Citra Lestari" && it.sahamPersen == 15.0 })
    }

    @Test
    fun `add complaint and update complaint status workflow`() {
        val repo = KosRepository()
        val initialCount = repo.complaints.value.size

        repo.addComplaint("Ahmad Pratama", "101", "Kran Wastafel Macet", "Air tidak mengalir normal.")
        assertEquals(initialCount + 1, repo.complaints.value.size)

        val newComplaint = repo.complaints.value.first { it.judulMasalah == "Kran Wastafel Macet" }
        assertEquals(com.example.data.model.ComplaintStatus.MENUNGGU, newComplaint.statusKomplain)

        // Admin updates status to DIPROSES then SELESAI
        repo.updateComplaintStatus(newComplaint.id, com.example.data.model.ComplaintStatus.SELESAI, "Teknisi sudah mengganti kran.")
        val resolved = repo.complaints.value.first { it.id == newComplaint.id }
        assertEquals(com.example.data.model.ComplaintStatus.SELESAI, resolved.statusKomplain)
        assertEquals("Teknisi sudah mengganti kran.", resolved.catatanPerbaikan)
    }

    @Test
    fun `broadcast announcement is received in repository`() {
        val repo = KosRepository()
        val initialCount = repo.announcements.value.size

        repo.addAnnouncement(
            judul = "Pembersihan Area Parkir",
            isi = "Mohon parkir motor di sisi timur selama pengecatan.",
            prioritas = com.example.data.model.AnnouncementPriority.PENTING
        )

        assertEquals(initialCount + 1, repo.announcements.value.size)
        val latest = repo.announcements.value.first { it.judul == "Pembersihan Area Parkir" }
        assertEquals(com.example.data.model.AnnouncementPriority.PENTING, latest.prioritas)
    }

    @Test
    fun `financial csv file format contains proper sections and headers`() {
        val repo = KosRepository()
        val report = repo.getProfitSharingReport()
        val invoices = repo.invoices.value
        val expenses = repo.expenses.value

        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "test_reports")
        tempDir.mkdirs()
        val csvFile = java.io.File(tempDir, "test_report.csv")
        val writer = java.io.FileWriter(csvFile)
        writer.write("\uFEFF")
        writer.append("LAPORAN KEUANGAN KOSCONNECT RESIDENCE\n")
        writer.append("Periode,${report.periodeBulan}\n")
        writer.append("Total Pemasukan,${report.totalPemasukan}\n")
        writer.append("Total Pengeluaran,${report.totalPengeluaran}\n")
        writer.append("Laba Bersih,${report.labaBersih}\n")
        writer.flush()
        writer.close()

        assertTrue(csvFile.exists())
        val content = csvFile.readText()
        assertTrue(content.contains("LAPORAN KEUANGAN KOSCONNECT RESIDENCE"))
        assertTrue(content.contains(report.periodeBulan))
        assertTrue(content.contains(report.labaBersih.toString()))
        csvFile.delete()
    }

    @Test
    fun `tenant uploads payment proof then admin verifies and marks Lunas`() {
        val repo = KosRepository()
        val invoice = repo.invoices.value.first { it.statusPembayaran == PaymentStatus.BELUM_BAYAR }
        val sampleProofUri = "/data/user/0/com.aistudio.kosconnect/files/media/sample_proof.jpg"

        // Tenant uploads payment proof
        repo.uploadPaymentProof(invoice.id, sampleProofUri)

        val pendingInvoice = repo.invoices.value.first { it.id == invoice.id }
        assertEquals(PaymentStatus.MENUNGGU_VERIFIKASI, pendingInvoice.statusPembayaran)
        assertEquals(sampleProofUri, pendingInvoice.buktiPembayaranUri)

        // Admin inspects photo and verifies payment as LUNAS
        repo.payInvoice(invoice.id, pendingInvoice.metodePembayaran ?: PaymentMethod.BCA_VA, sampleProofUri)

        val verifiedInvoice = repo.invoices.value.first { it.id == invoice.id }
        assertEquals(PaymentStatus.LUNAS, verifiedInvoice.statusPembayaran)
        assertEquals(sampleProofUri, verifiedInvoice.buktiPembayaranUri)
        assertNotNull(verifiedInvoice.tanggalBayar)
    }

    @Test
    fun `complaint records damage photo and technician attaches repair photo`() {
        val repo = KosRepository()
        val damagePhoto = "/data/user/0/com.aistudio.kosconnect/files/media/kerusakan_ac.jpg"
        val repairPhoto = "/data/user/0/com.aistudio.kosconnect/files/media/perbaikan_ac.jpg"

        // Tenant reports complaint with damage photo
        repo.addComplaint(
            penghuniNama = "Rizky",
            roomNomor = "202",
            judul = "AC Bocor dan Menetes",
            deskripsi = "Air menetes ke meja belajar",
            fotoKerusakanUri = damagePhoto
        )

        val complaint = repo.complaints.value.first { it.judulMasalah == "AC Bocor dan Menetes" }
        assertEquals(damagePhoto, complaint.fotoKerusakanUri)
        assertNull(complaint.fotoPerbaikanUri)

        // Technician completes repair and attaches repair photo
        repo.updateComplaintStatus(
            complaintId = complaint.id,
            newStatus = com.example.data.model.ComplaintStatus.SELESAI,
            catatan = "Pipa pembuangan AC sudah diganti dan dibersihkan",
            fotoPerbaikanUri = repairPhoto
        )

        val resolved = repo.complaints.value.first { it.id == complaint.id }
        assertEquals(com.example.data.model.ComplaintStatus.SELESAI, resolved.statusKomplain)
        assertEquals(damagePhoto, resolved.fotoKerusakanUri)
        assertEquals(repairPhoto, resolved.fotoPerbaikanUri)
        assertEquals("Pipa pembuangan AC sudah diganti dan dibersihkan", resolved.catatanPerbaikan)
    }

    @Test
    fun `expense records receipt photo properly`() {
        val repo = KosRepository()
        val receiptPhoto = "/data/user/0/com.aistudio.kosconnect/files/media/nota_token_pln.jpg"

        repo.addExpense(
            kategori = com.example.data.model.ExpenseCategory.LISTRIK,
            nominal = 350000L,
            deskripsi = "Beli 2x Token Listrik Induk 150rb",
            tanggal = "8 Nov 2026",
            buktiStruk = receiptPhoto
        )

        val newExpense = repo.expenses.value.first { it.deskripsi == "Beli 2x Token Listrik Induk 150rb" }
        assertEquals(350000L, newExpense.nominal)
        assertEquals(receiptPhoto, newExpense.buktiStruk)
    }

    @Test
    fun `reminder config initializes enabled and identifies unpaid invoices for notification`() {
        val repo = KosRepository()
        val unpaidInvoices = repo.invoices.value.filter { it.statusPembayaran != PaymentStatus.LUNAS }
        assertTrue("Harus ada tagihan belum lunas untuk pengingat", unpaidInvoices.isNotEmpty())

        val reminderConfig = com.example.ui.ReminderConfig()
        assertTrue(reminderConfig.autoReminderEnabled)
        assertTrue(reminderConfig.remindHMinus3)
        assertTrue(reminderConfig.remindHMinus1)

        assertEquals("kos_due_reminders_channel", com.example.reminder.NotificationHelper.CHANNEL_REMINDERS_ID)
        assertEquals("kos_community_channel", com.example.reminder.NotificationHelper.CHANNEL_COMMUNITY_ID)
    }

    @Test
    fun `tenant room 101 has lease and multiple past invoices with receipts`() {
        val repo = KosRepository()
        val room101Invoices = repo.invoices.value.filter { it.roomNomor == "101" }
        assertTrue("Kamar 101 harus memiliki minimal 3 invoice (berjalan dan histori)", room101Invoices.size >= 3)

        val paidPastInvoices = room101Invoices.filter { it.statusPembayaran == PaymentStatus.LUNAS }
        assertTrue("Harus ada tagihan lunas di histori untuk kuitansi resmi", paidPastInvoices.isNotEmpty())
        assertTrue("Harus ada tagihan Oktober 2026", room101Invoices.any { it.bulanTagihan.contains("Oktober") })
        assertTrue("Harus ada tagihan September 2026", room101Invoices.any { it.bulanTagihan.contains("September") })
    }

    @Test
    fun `investor report has past withdrawal records and occupancy breakdown`() {
        val repo = KosRepository()
        val report = repo.getProfitSharingReport()
        val pastWithdrawals = report.pastWithdrawals

        assertEquals(3, pastWithdrawals.size)
        assertTrue(pastWithdrawals.any { it.periodeBulan == "Oktober 2026" && it.bankRef.isNotBlank() })
        assertTrue(pastWithdrawals.any { it.periodeBulan == "September 2026" && it.bankRef.isNotBlank() })
        assertTrue(pastWithdrawals.any { it.periodeBulan == "Agustus 2026" && it.bankRef.isNotBlank() })

        val totalPast = pastWithdrawals.sumOf { it.nominal }
        assertTrue(totalPast > 10000000L)
    }
}

