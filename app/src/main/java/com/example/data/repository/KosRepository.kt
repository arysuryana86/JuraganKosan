package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class KosRepository(private val db: FirebaseFirestore? = null) {

    constructor(context: Context) : this(
        try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("KosRepository", "Fallback to local repository: ${e.message}")
            null
        }
    )

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        if (db != null) {
            setupFirestoreListeners()
        }
    }

    private fun setupFirestoreListeners() {
        val database = db ?: return

        // 1. Observe rooms from cloud
        database.collection("rooms").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to rooms: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudRooms = snapshot.documents.mapNotNull { it.toObject(Room::class.java) }
                if (cloudRooms.isNotEmpty()) {
                    _rooms.value = cloudRooms.sortedBy { it.nomorKamar }
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _rooms.value.forEach { room ->
                        database.collection("rooms").document(room.id).set(room)
                    }
                }
            }
        }

        // 2. Observe invoices from cloud
        database.collection("invoices").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to invoices: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudInvoices = snapshot.documents.mapNotNull { it.toObject(Invoice::class.java) }
                if (cloudInvoices.isNotEmpty()) {
                    _invoices.value = cloudInvoices
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _invoices.value.forEach { inv ->
                        database.collection("invoices").document(inv.id).set(inv)
                    }
                }
            }
        }

        // 3. Observe expenses from cloud
        database.collection("expenses").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to expenses: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudExpenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                if (cloudExpenses.isNotEmpty()) {
                    _expenses.value = cloudExpenses
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _expenses.value.forEach { exp ->
                        database.collection("expenses").document(exp.id).set(exp)
                    }
                }
            }
        }

        // 4. Observe complaints from cloud
        database.collection("complaints").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to complaints: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudComplaints = snapshot.documents.mapNotNull { it.toObject(Complaint::class.java) }
                if (cloudComplaints.isNotEmpty()) {
                    _complaints.value = cloudComplaints
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _complaints.value.forEach { c ->
                        database.collection("complaints").document(c.id).set(c)
                    }
                }
            }
        }

        // 5. Observe announcements from cloud
        database.collection("announcements").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to announcements: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudAnnouncements = snapshot.documents.mapNotNull { it.toObject(Announcement::class.java) }
                if (cloudAnnouncements.isNotEmpty()) {
                    _announcements.value = cloudAnnouncements
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _announcements.value.forEach { ann ->
                        database.collection("announcements").document(ann.id).set(ann)
                    }
                }
            }
        }

        // 6. Observe investor shares from cloud
        database.collection("investor_shares").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("KosRepo", "Error listening to investor_shares: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val cloudShares = snapshot.documents.mapNotNull { it.toObject(InvestorShare::class.java) }
                if (cloudShares.isNotEmpty()) {
                    _investorShares.value = cloudShares
                }
            } else if (snapshot != null && snapshot.isEmpty) {
                repositoryScope.launch {
                    _investorShares.value.forEach { share ->
                        database.collection("investor_shares").document(share.id).set(share)
                    }
                }
            }
        }
    }

    private fun persistRoom(room: Room) {
        db?.collection("rooms")?.document(room.id)?.set(room)
    }

    private fun persistInvoice(invoice: Invoice) {
        db?.collection("invoices")?.document(invoice.id)?.set(invoice)
    }

    private fun persistExpense(expense: Expense) {
        db?.collection("expenses")?.document(expense.id)?.set(expense)
    }

    private fun persistComplaint(complaint: Complaint) {
        db?.collection("complaints")?.document(complaint.id)?.set(complaint)
    }

    private fun persistAnnouncement(announcement: Announcement) {
        db?.collection("announcements")?.document(announcement.id)?.set(announcement)
    }

    private fun persistInvestor(investor: InvestorShare) {
        db?.collection("investor_shares")?.document(investor.id)?.set(investor)
    }

    // Active User / Role
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Kos Profile
    private val _kosProfile = MutableStateFlow(Kos())
    val kosProfile: StateFlow<Kos> = _kosProfile.asStateFlow()

    // Rooms State
    private val _rooms = MutableStateFlow(createInitialRooms())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    // Invoices State
    private val _invoices = MutableStateFlow(createInitialInvoices())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    // Expenses State
    private val _expenses = MutableStateFlow(createInitialExpenses())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Investor Shares State
    private val _investorShares = MutableStateFlow(createInitialInvestorShares())
    val investorShares: StateFlow<List<InvestorShare>> = _investorShares.asStateFlow()

    // Complaints State
    private val _complaints = MutableStateFlow(createInitialComplaints())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    // Announcements State
    private val _announcements = MutableStateFlow(createInitialAnnouncements())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Tenancy Agreements State
    private val _agreements = MutableStateFlow(createInitialAgreements())
    val agreements: StateFlow<List<TenancyAgreement>> = _agreements.asStateFlow()

    fun getAgreementForRoom(roomNomor: String): TenancyAgreement? {
        return _agreements.value.find { it.roomNomor == roomNomor }
    }

    fun saveAgreement(agreement: TenancyAgreement) {
        _agreements.update { list ->
            listOf(agreement) + list.filterNot { it.id == agreement.id || it.roomNomor == agreement.roomNomor }
        }
    }

    // Switch active role
    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    // Room operations
    fun addRoom(nomorKamar: String, tipeKamar: String, harga: Long, fasilitas: List<String>) {
        val newRoom = Room(
            nomorKamar = nomorKamar,
            tipeKamar = tipeKamar,
            hargaSewaDasar = harga,
            status = RoomStatus.TERSEDIA,
            fasilitas = fasilitas
        )
        _rooms.update { listOf(newRoom) + it }
    }

    fun updateRoomStatus(roomId: String, newStatus: RoomStatus, penghuniNama: String? = null, penghuniHp: String? = null) {
        _rooms.update { list ->
            list.map {
                if (it.id == roomId) {
                    it.copy(
                        status = newStatus,
                        penghuniNama = if (newStatus == RoomStatus.TERISI) penghuniNama ?: it.penghuniNama else null,
                        penghuniHp = if (newStatus == RoomStatus.TERISI) penghuniHp ?: it.penghuniHp else null
                    )
                } else it
            }
        }
    }

    fun checkInTenant(roomId: String, nama: String, noHp: String, deposit: Long = 500000L) {
        val targetRoom = _rooms.value.find { it.id == roomId } ?: return
        _rooms.update { list ->
            list.map {
                if (it.id == roomId) {
                    it.copy(
                        status = RoomStatus.TERISI,
                        penghuniNama = nama,
                        penghuniHp = noHp
                    )
                } else it
            }
        }

        // Generate first month invoice for the new tenant
        val newInvoice = Invoice(
            roomNomor = targetRoom.nomorKamar,
            penghuniNama = nama,
            nominalKamar = targetRoom.hargaSewaDasar,
            tambahanListrik = 150000L,
            denda = 0L,
            totalTagihan = targetRoom.hargaSewaDasar + 150000L,
            statusPembayaran = PaymentStatus.BELUM_BAYAR,
            tanggalJatuhTempo = "10 Des 2026",
            metodePembayaran = PaymentMethod.BCA_VA,
            kodePembayaran = "80012${(100000..999999).random()}"
        )
        _invoices.update { listOf(newInvoice) + it }

        // Generate digital tenancy agreement for the new occupant
        val newAgreement = TenancyAgreement(
            nomorSurat = "0${targetRoom.nomorKamar}/SP-KOS/XI/2026",
            tanggalSurat = "10 November 2026",
            roomNomor = targetRoom.nomorKamar,
            tipeKamar = targetRoom.tipeKamar,
            pihakKeduaNama = nama,
            pihakKeduaHp = noHp,
            pihakKeduaPekerjaan = "Karyawan / Mahasiswa",
            hargaSewaBulanan = targetRoom.hargaSewaDasar,
            depositJaminan = deposit,
            tanggalMulai = "10 November 2026",
            tanggalSelesai = "9 November 2027",
            fasilitas = targetRoom.fasilitas,
            signedAt = "10 Nov 2026, 14:00 WIB",
            digitalSignatureHash = "E-SIGN-KOS-${targetRoom.nomorKamar}-${System.currentTimeMillis().toString().takeLast(6)}"
        )
        _agreements.update { listOf(newAgreement) + it.filterNot { a -> a.roomNomor == targetRoom.nomorKamar } }
    }

    fun checkOutTenant(roomId: String) {
        _rooms.update { list ->
            list.map {
                if (it.id == roomId) {
                    it.copy(
                        status = RoomStatus.TERSEDIA,
                        penghuniNama = null,
                        penghuniHp = null
                    )
                } else it
            }
        }
    }

    fun updateRoom(roomId: String, nomor: String, tipe: String, harga: Long, fasilitas: List<String>) {
        _rooms.update { list ->
            list.map {
                if (it.id == roomId) {
                    it.copy(
                        nomorKamar = nomor,
                        tipeKamar = tipe,
                        hargaSewaDasar = harga,
                        fasilitas = fasilitas
                    )
                } else it
            }
        }
    }

    fun deleteRoom(roomId: String) {
        _rooms.update { list -> list.filterNot { it.id == roomId } }
    }

    // Expense operations
    fun addExpense(
        kategori: ExpenseCategory,
        nominal: Long,
        deskripsi: String,
        tanggal: String,
        buktiStruk: String? = null
    ) {
        val exp = Expense(
            kategori = kategori,
            nominal = nominal,
            deskripsi = deskripsi,
            tanggalPengeluaran = tanggal,
            buktiStruk = buktiStruk
        )
        _expenses.update { listOf(exp) + it }
        persistExpense(exp)
    }

    fun deleteExpense(expenseId: String) {
        _expenses.update { list -> list.filterNot { it.id == expenseId } }
    }

    // Invoice operations (Pay via VA / QRIS & Admin Billing)
    fun payInvoice(invoiceId: String, method: PaymentMethod, buktiUri: String? = null) {
        _invoices.update { list ->
            list.map { inv ->
                if (inv.id == invoiceId) {
                    val updated = inv.copy(
                        statusPembayaran = PaymentStatus.LUNAS,
                        metodePembayaran = method,
                        tanggalBayar = "Hari ini, 14:20 WIB",
                        buktiPembayaranUri = buktiUri ?: inv.buktiPembayaranUri
                    )
                    persistInvoice(updated)
                    updated
                } else inv
            }
        }
    }

    fun uploadPaymentProof(invoiceId: String, buktiUri: String) {
        _invoices.update { list ->
            list.map { inv ->
                if (inv.id == invoiceId) {
                    val updated = inv.copy(
                        statusPembayaran = PaymentStatus.MENUNGGU_VERIFIKASI,
                        buktiPembayaranUri = buktiUri
                    )
                    persistInvoice(updated)
                    updated
                } else inv
            }
        }
    }

    fun markInvoiceUnpaid(invoiceId: String) {
        _invoices.update { list ->
            list.map { inv ->
                if (inv.id == invoiceId) {
                    val updated = inv.copy(
                        statusPembayaran = PaymentStatus.BELUM_BAYAR,
                        tanggalBayar = null
                    )
                    persistInvoice(updated)
                    updated
                } else inv
            }
        }
    }


    fun createInvoice(
        roomNomor: String,
        penghuniNama: String,
        nominalKamar: Long,
        tambahanListrik: Long,
        denda: Long,
        bulanTagihan: String,
        tanggalJatuhTempo: String,
        metodePembayaran: PaymentMethod
    ) {
        val newInv = Invoice(
            roomNomor = roomNomor,
            penghuniNama = penghuniNama,
            bulanTagihan = bulanTagihan,
            tanggalJatuhTempo = tanggalJatuhTempo,
            nominalKamar = nominalKamar,
            tambahanListrik = tambahanListrik,
            denda = denda,
            totalTagihan = nominalKamar + tambahanListrik + denda,
            statusPembayaran = PaymentStatus.BELUM_BAYAR,
            metodePembayaran = metodePembayaran,
            kodePembayaran = "${metodePembayaran.codePrefix}${(100000..999999).random()}"
        )
        _invoices.update { listOf(newInv) + it }
    }

    fun updateInvoiceAdjustment(invoiceId: String, tambahanListrik: Long, denda: Long) {
        _invoices.update { list ->
            list.map { inv ->
                if (inv.id == invoiceId) {
                    inv.copy(
                        tambahanListrik = tambahanListrik,
                        denda = denda,
                        totalTagihan = inv.nominalKamar + tambahanListrik + denda
                    )
                } else inv
            }
        }
    }

    fun generateMonthlyInvoices(bulanTagihan: String, jatuhTempo: String): Int {
        val occupied = _rooms.value.filter { it.status == RoomStatus.TERISI && !it.penghuniNama.isNullOrBlank() }
        var generatedCount = 0
        val newInvoices = mutableListOf<Invoice>()

        occupied.forEach { r ->
            val exists = _invoices.value.any { it.roomNomor == r.nomorKamar && it.bulanTagihan == bulanTagihan }
            if (!exists) {
                newInvoices.add(
                    Invoice(
                        roomNomor = r.nomorKamar,
                        penghuniNama = r.penghuniNama ?: "Penghuni",
                        bulanTagihan = bulanTagihan,
                        tanggalJatuhTempo = jatuhTempo,
                        nominalKamar = r.hargaSewaDasar,
                        tambahanListrik = 150000L,
                        denda = 0L,
                        totalTagihan = r.hargaSewaDasar + 150000L,
                        statusPembayaran = PaymentStatus.BELUM_BAYAR,
                        metodePembayaran = PaymentMethod.BCA_VA,
                        kodePembayaran = "80012${(100000..999999).random()}"
                    )
                )
                generatedCount++
            }
        }

        if (newInvoices.isNotEmpty()) {
            _invoices.update { newInvoices + it }
        }
        return generatedCount
    }

    fun deleteInvoice(invoiceId: String) {
        _invoices.update { list -> list.filterNot { it.id == invoiceId } }
    }

    // Complaint operations
    fun addComplaint(
        penghuniNama: String,
        roomNomor: String,
        judul: String,
        deskripsi: String,
        kategori: String = "Umum",
        fotoKerusakanUri: String? = null
    ) {
        val complaint = Complaint(
            penghuniNama = penghuniNama,
            roomNomor = roomNomor,
            judulMasalah = judul,
            deskripsiMasalah = deskripsi,
            kategori = kategori,
            fotoKerusakanUri = fotoKerusakanUri,
            createdAt = "Hari ini, Baru saja"
        )
        _complaints.update { listOf(complaint) + it }
        persistComplaint(complaint)
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: ComplaintStatus,
        catatan: String? = null,
        fotoPerbaikanUri: String? = null
    ) {
        _complaints.update { list ->
            list.map {
                if (it.id == complaintId) {
                    val updated = it.copy(
                        statusKomplain = newStatus,
                        catatanPerbaikan = catatan ?: it.catatanPerbaikan,
                        fotoPerbaikanUri = fotoPerbaikanUri ?: it.fotoPerbaikanUri
                    )
                    persistComplaint(updated)
                    updated
                } else it
            }
        }
    }


    // Announcement operations
    fun addAnnouncement(
        judul: String,
        isi: String,
        prioritas: AnnouncementPriority = AnnouncementPriority.NORMAL,
        kategori: String = "Umum",
        tanggal: String = "Hari ini"
    ) {
        val newAnn = Announcement(
            judul = judul,
            tanggal = tanggal,
            isi = isi,
            prioritas = prioritas,
            kategori = kategori
        )
        _announcements.update { listOf(newAnn) + it }
    }

    fun deleteAnnouncement(id: String) {
        _announcements.update { list -> list.filterNot { it.id == id } }
    }

    private val _payoutStatuses = MutableStateFlow<Map<String, PayoutStatus>>(
        mapOf("Budi Prakoso (Pengelola Kos)" to PayoutStatus.DITRANSFER)
    )
    val payoutStatuses: StateFlow<Map<String, PayoutStatus>> = _payoutStatuses.asStateFlow()

    fun addInvestor(nama: String, email: String, rekening: String, persentase: Double) {
        val newInv = InvestorShare(
            investorId = "inv-${System.currentTimeMillis()}",
            investorNama = nama,
            investorEmail = email,
            rekeningBank = rekening,
            persentase = persentase
        )
        _investorShares.update { it + newInv }
    }

    fun updateInvestor(id: String, nama: String, email: String, rekening: String, persentase: Double) {
        _investorShares.update { list ->
            list.map {
                if (it.id == id) it.copy(investorNama = nama, investorEmail = email, rekeningBank = rekening, persentase = persentase)
                else it
            }
        }
    }

    fun deleteInvestor(id: String) {
        _investorShares.update { list -> list.filterNot { it.id == id } }
    }

    fun transferInvestorPayout(investorNama: String, tanggal: String = "Hari ini") {
        _payoutStatuses.update { it + (investorNama to PayoutStatus.DITRANSFER) }
    }

    fun revertInvestorPayout(investorNama: String) {
        _payoutStatuses.update { it + (investorNama to PayoutStatus.MENUNGGU) }
    }

    // Calculate dynamic profit sharing report
    fun getProfitSharingReport(): ProfitSharingReport {
        val paidInvoices = _invoices.value.filter { it.statusPembayaran == PaymentStatus.LUNAS }
        val totalRevenue = paidInvoices.sumOf { it.totalTagihan }
        val totalExpense = _expenses.value.sumOf { it.nominal }
        val netProfit = (totalRevenue - totalExpense).coerceAtLeast(0L)

        val shares = _investorShares.value
        val statuses = _payoutStatuses.value
        val payouts = shares.map { share ->
            val nominal = (netProfit * (share.persentase / 100.0)).toLong()
            val status = statuses[share.investorNama] ?: PayoutStatus.MENUNGGU
            InvestorPayout(
                investorNama = share.investorNama,
                sahamPersen = share.persentase,
                nominalBagian = nominal,
                status = status,
                nomorRekening = share.rekeningBank,
                tanggalTransfer = if (status == PayoutStatus.DITRANSFER) "5 Nov 2026, 10:30 WIB" else null
            )
        }

        val pastList = listOf(
            WithdrawalRecord(periodeBulan = "Oktober 2026", nominal = 4950000L, tanggalTransfer = "2 Nov 2026", nomorRekening = "BCA 8839-0129-39", bankRef = "BCA-TRX-098231"),
            WithdrawalRecord(periodeBulan = "September 2026", nominal = 5100000L, tanggalTransfer = "1 Okt 2026", nomorRekening = "BCA 8839-0129-39", bankRef = "BCA-TRX-082736"),
            WithdrawalRecord(periodeBulan = "Agustus 2026", nominal = 4800000L, tanggalTransfer = "2 Sep 2026", nomorRekening = "BCA 8839-0129-39", bankRef = "BCA-TRX-071625")
        )

        return ProfitSharingReport(
            periodeBulan = "November 2026",
            totalPemasukan = totalRevenue,
            totalPengeluaran = totalExpense,
            labaBersih = netProfit,
            payouts = payouts,
            pastWithdrawals = pastList
        )
    }

    companion object {
        fun formatRupiah(amount: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }

        private fun createInitialRooms(): List<Room> {
            return listOf(
                Room(nomorKamar = "101", tipeKamar = "Deluxe AC", hargaSewaDasar = 1800000L, status = RoomStatus.TERISI, penghuniNama = "Ahmad Pratama", penghuniHp = "0812-3456-7890"),
                Room(nomorKamar = "102", tipeKamar = "Deluxe AC", hargaSewaDasar = 1800000L, status = RoomStatus.TERISI, penghuniNama = "Siti Rahma", penghuniHp = "0813-8899-0011"),
                Room(nomorKamar = "103", tipeKamar = "Standard Fan", hargaSewaDasar = 1200000L, status = RoomStatus.TERSEDIA),
                Room(nomorKamar = "104", tipeKamar = "Deluxe AC", hargaSewaDasar = 1800000L, status = RoomStatus.TERISI, penghuniNama = "Budi Wicaksono", penghuniHp = "0856-7788-9900"),
                Room(nomorKamar = "105", tipeKamar = "Standard Fan", hargaSewaDasar = 1200000L, status = RoomStatus.PERBAIKAN, fasilitas = listOf("Fan", "Kamar Mandi Luar")),
                Room(nomorKamar = "201", tipeKamar = "Executive Balcony", hargaSewaDasar = 2300000L, status = RoomStatus.TERISI, penghuniNama = "Dimas Surya", penghuniHp = "0819-2233-4455", fasilitas = listOf("AC", "Water Heater", "Balkon", "Smart TV")),
                Room(nomorKamar = "202", tipeKamar = "Executive Balcony", hargaSewaDasar = 2300000L, status = RoomStatus.TERISI, penghuniNama = "Jessica Tan", penghuniHp = "0821-3344-5566", fasilitas = listOf("AC", "Water Heater", "Balkon", "Smart TV")),
                Room(nomorKamar = "203", tipeKamar = "Deluxe AC", hargaSewaDasar = 1800000L, status = RoomStatus.TERSEDIA),
                Room(nomorKamar = "204", tipeKamar = "Standard Fan", hargaSewaDasar = 1200000L, status = RoomStatus.TERISI, penghuniNama = "Fajar Nugraha", penghuniHp = "0878-1122-3344"),
                Room(nomorKamar = "205", tipeKamar = "Deluxe AC", hargaSewaDasar = 1800000L, status = RoomStatus.TERISI, penghuniNama = "Rani Paramitha", penghuniHp = "0896-5566-7788")
            )
        }

        private fun createInitialInvoices(): List<Invoice> {
            return listOf(
                Invoice(
                    id = "inv-101",
                    roomNomor = "101",
                    penghuniNama = "Ahmad Pratama",
                    nominalKamar = 1800000L,
                    tambahanListrik = 150000L,
                    denda = 0L,
                    totalTagihan = 1950000L,
                    statusPembayaran = PaymentStatus.BELUM_BAYAR,
                    metodePembayaran = PaymentMethod.BCA_VA,
                    kodePembayaran = "800129847291823",
                    tanggalJatuhTempo = "10 Nov 2026"
                ),
                Invoice(
                    id = "inv-101-okt",
                    roomNomor = "101",
                    penghuniNama = "Ahmad Pratama",
                    bulanTagihan = "Oktober 2026",
                    nominalKamar = 1800000L,
                    tambahanListrik = 100000L,
                    denda = 0L,
                    totalTagihan = 1900000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.BCA_VA,
                    kodePembayaran = "800129847291823",
                    tanggalJatuhTempo = "10 Okt 2026",
                    tanggalBayar = "3 Okt 2026, 14:20 WIB"
                ),
                Invoice(
                    id = "inv-101-sep",
                    roomNomor = "101",
                    penghuniNama = "Ahmad Pratama",
                    bulanTagihan = "September 2026",
                    nominalKamar = 1800000L,
                    tambahanListrik = 50000L,
                    denda = 0L,
                    totalTagihan = 1850000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.QRIS,
                    kodePembayaran = "NMD-QRIS-992182",
                    tanggalJatuhTempo = "10 Sep 2026",
                    tanggalBayar = "4 Sep 2026, 10:10 WIB"
                ),
                Invoice(
                    id = "inv-102",
                    roomNomor = "102",
                    penghuniNama = "Siti Rahma",
                    nominalKamar = 1800000L,
                    tambahanListrik = 120000L,
                    denda = 0L,
                    totalTagihan = 1920000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.QRIS,
                    tanggalBayar = "1 Nov 2026, 09:15 WIB"
                ),
                Invoice(
                    id = "inv-104",
                    roomNomor = "104",
                    penghuniNama = "Budi Wicaksono",
                    nominalKamar = 1800000L,
                    tambahanListrik = 200000L,
                    denda = 25000L,
                    totalTagihan = 2025000L,
                    statusPembayaran = PaymentStatus.BELUM_BAYAR,
                    metodePembayaran = PaymentMethod.MANDIRI_VA,
                    kodePembayaran = "88019384721990",
                    tanggalJatuhTempo = "5 Nov 2026"
                ),
                Invoice(
                    id = "inv-201",
                    roomNomor = "201",
                    penghuniNama = "Dimas Surya",
                    nominalKamar = 2300000L,
                    tambahanListrik = 250000L,
                    denda = 0L,
                    totalTagihan = 2550000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.BCA_VA,
                    tanggalBayar = "2 Nov 2026, 11:45 WIB"
                ),
                Invoice(
                    id = "inv-202",
                    roomNomor = "202",
                    penghuniNama = "Jessica Tan",
                    nominalKamar = 2300000L,
                    tambahanListrik = 180000L,
                    denda = 0L,
                    totalTagihan = 2480000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.BCA_VA,
                    tanggalBayar = "3 Nov 2026, 16:30 WIB"
                ),
                Invoice(
                    id = "inv-204",
                    roomNomor = "204",
                    penghuniNama = "Fajar Nugraha",
                    nominalKamar = 1200000L,
                    tambahanListrik = 100000L,
                    denda = 0L,
                    totalTagihan = 1300000L,
                    statusPembayaran = PaymentStatus.LUNAS,
                    metodePembayaran = PaymentMethod.BRI_VA,
                    tanggalBayar = "4 Nov 2026, 10:10 WIB"
                ),
                Invoice(
                    id = "inv-205",
                    roomNomor = "205",
                    penghuniNama = "Rani Paramitha",
                    nominalKamar = 1800000L,
                    tambahanListrik = 130000L,
                    denda = 0L,
                    totalTagihan = 1930000L,
                    statusPembayaran = PaymentStatus.BELUM_BAYAR,
                    metodePembayaran = PaymentMethod.QRIS,
                    kodePembayaran = "NMD-QRIS-2059102",
                    tanggalJatuhTempo = "10 Nov 2026"
                )
            )
        }

        private fun createInitialExpenses(): List<Expense> {
            return listOf(
                Expense(kategori = ExpenseCategory.LISTRIK, nominal = 1450000L, deskripsi = "Token Listrik Induk & Pompa Air", tanggalPengeluaran = "1 Nov 2026"),
                Expense(kategori = ExpenseCategory.GAJI, nominal = 1800000L, deskripsi = "Gaji Penjaga Kos & Cleaning Service", tanggalPengeluaran = "1 Nov 2026"),
                Expense(kategori = ExpenseCategory.WIFI, nominal = 550000L, deskripsi = "Tagihan Indihome High-speed 100 Mbps", tanggalPengeluaran = "2 Nov 2026"),
                Expense(kategori = ExpenseCategory.AIR, nominal = 350000L, deskripsi = "PDAM & 10 Galon Air Minum Bersama", tanggalPengeluaran = "3 Nov 2026"),
                Expense(kategori = ExpenseCategory.PERBAIKAN, nominal = 250000L, deskripsi = "Servis Pipa Wastafel Kamar 105", tanggalPengeluaran = "4 Nov 2026")
            )
        }

        private fun createInitialInvestorShares(): List<InvestorShare> {
            return listOf(
                InvestorShare(
                    investorId = "inv-andi",
                    investorNama = "Andi Wijaya (Anda)",
                    investorEmail = "andi.wijaya@investor.com",
                    rekeningBank = "BCA 8839-0129-39 (a.n Andi Wijaya)",
                    persentase = 30.0
                ),
                InvestorShare(
                    investorId = "inv-hendra",
                    investorNama = "Hendra Santoso",
                    investorEmail = "hendra.s@investor.com",
                    rekeningBank = "Mandiri 132-00-9827-111",
                    persentase = 40.0
                ),
                InvestorShare(
                    investorId = "inv-admin",
                    investorNama = "Budi Prakoso (Pengelola Kos)",
                    investorEmail = "budi@juragankos.com",
                    rekeningBank = "BCA 014-9988-12",
                    persentase = 30.0
                )
            )
        }

        private fun createInitialComplaints(): List<Complaint> {
            return listOf(
                Complaint(
                    penghuniNama = "Ahmad Pratama",
                    roomNomor = "101",
                    judulMasalah = "AC Kurang Dingin & Netes Air",
                    deskripsiMasalah = "Sejak 2 hari lalu AC meneteskan air ke lantai dan hembusan angin terasa kurang sejuk.",
                    statusKomplain = ComplaintStatus.MENUNGGU,
                    createdAt = "Hari ini, 08:30 WIB"
                ),
                Complaint(
                    penghuniNama = "Jessica Tan",
                    roomNomor = "202",
                    judulMasalah = "Lampu Kamar Mandi Mati",
                    deskripsiMasalah = "Lampu bohlam utama kamar mandi tiba-tiba putus semalam. Mohon dibantu ganti.",
                    statusKomplain = ComplaintStatus.DIPROSES,
                    createdAt = "Kemarin, 19:15 WIB",
                    catatanPerbaikan = "Teknisi dijadwalkan datang siang ini jam 13:00"
                ),
                Complaint(
                    penghuniNama = "Budi Wicaksono",
                    roomNomor = "104",
                    judulMasalah = "WiFi Lantai 1 Kadang Putus",
                    deskripsiMasalah = "Koneksi router lantai 1 suka drop sekitar jam 8 malam.",
                    statusKomplain = ComplaintStatus.SELESAI,
                    createdAt = "3 Nov 2026",
                    catatanPerbaikan = "Router telah di-restart dan ganti kabel LAN baru."
                )
            )
        }

        private fun createInitialAnnouncements(): List<Announcement> {
            return listOf(
                Announcement(
                    judul = "Pembersihan Tandon Air Utama",
                    tanggal = "8 November 2026",
                    isi = "Akan dilakukan pembersihan tandon rutin mulai pukul 10:00 - 12:00 WIB. Aliran air dimatikan sementara selama 2 jam.",
                    prioritas = AnnouncementPriority.PENTING,
                    kategori = "Pemeliharaan"
                ),
                Announcement(
                    judul = "Jam Tenang Malam",
                    tanggal = "Berlaku Setiap Hari",
                    isi = "Mohon menjaga volume musik atau suara setelah pukul 22:00 WIB demi kenyamanan dan ketenangan istirahat bersama.",
                    prioritas = AnnouncementPriority.NORMAL,
                    kategori = "Aturan Kos"
                ),
                Announcement(
                    judul = "Pemberitahuan Tagihan Sewa Otomatis",
                    tanggal = "1 November 2026",
                    isi = "Sistem penagihan otomatis kini mendukung transfer instan Virtual Account BCA, Mandiri, dan QRIS. Terima kasih bagi yang sudah membayar tepat waktu.",
                    prioritas = AnnouncementPriority.NORMAL,
                    kategori = "Keuangan"
                )
            )
        }

        private fun createInitialAgreements(): List<TenancyAgreement> {
            return listOf(
                TenancyAgreement(
                    nomorSurat = "042/SP-KOS/XI/2026",
                    tanggalSurat = "1 Januari 2026",
                    roomNomor = "101",
                    tipeKamar = "Deluxe AC",
                    pihakPertamaNama = "Budi Prakoso",
                    pihakPertamaJabatan = "Pengelola KosConnect Residence",
                    pihakPertamaHp = "0812-9988-7766",
                    pihakPertamaAlamat = "Jl. Merdeka No. 45, Jakarta Selatan",
                    pihakKeduaNama = "Ahmad Pratama",
                    pihakKeduaNik = "3174092810980002",
                    pihakKeduaHp = "0812-3456-7890",
                    pihakKeduaPekerjaan = "Software Engineer",
                    pihakKeduaAlamatKtp = "Jl. Melati No. 12, Bandung",
                    hargaSewaBulanan = 1800000L,
                    depositJaminan = 500000L,
                    tanggalMulai = "1 Januari 2026",
                    tanggalSelesai = "31 Desember 2026",
                    jatuhTempoTgl = 10,
                    dendaKeterlambatanPerHari = 25000L,
                    fasilitas = listOf(
                        "AC 1 PK Inverter",
                        "Kamar Mandi Dalam & Water Heater",
                        "Kasur Springbed Queen",
                        "High-Speed WiFi 100 Mbps",
                        "Lemari Pakaian 2 Pintu",
                        "Meja & Kursi Kerja"
                    ),
                    status = AgreementStatus.AKTIF,
                    signedAt = "1 Jan 2026, 10:15 WIB",
                    digitalSignatureHash = "E-SIGN-KOS-89283749210",
                    meteraiDigitalSerial = "MTR-DIGITAL-KOSCONNECT-2026-09182"
                )
            )
        }
    }
}

