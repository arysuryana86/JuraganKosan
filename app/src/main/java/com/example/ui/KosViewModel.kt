package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.data.model.*
import com.example.data.repository.KosRepository
import com.example.reminder.DueReminderWorker
import com.example.reminder.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ReminderConfig(
    val autoReminderEnabled: Boolean = true,
    val remindHMinus3: Boolean = true,
    val remindHMinus1: Boolean = true,
    val remindOnDueDate: Boolean = true,
    val remindOverdue: Boolean = true,
    val lastSyncText: String = "Hari ini, 08:00 WIB"
)

data class AdminSummaryState(
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val availableRooms: Int = 0,
    val repairRooms: Int = 0,
    val occupancyRate: Int = 0,
    val totalRevenue: Long = 0L,
    val unpaidCount: Int = 0,
    val unpaidAmount: Long = 0L,
    val totalExpense: Long = 0L,
    val netProfit: Long = 0L,
    val pendingComplaintsCount: Int = 0
)

class KosViewModel(
    private val repository: KosRepository = KosRepository()
) : ViewModel() {

    val currentRole: StateFlow<UserRole> = repository.currentRole
    val kosProfile: StateFlow<Kos> = repository.kosProfile
    val rooms: StateFlow<List<Room>> = repository.rooms
    val invoices: StateFlow<List<Invoice>> = repository.invoices
    val expenses: StateFlow<List<Expense>> = repository.expenses
    val complaints: StateFlow<List<Complaint>> = repository.complaints
    val investorShares: StateFlow<List<InvestorShare>> = repository.investorShares
    val announcements: StateFlow<List<Announcement>> = repository.announcements
    val agreements: StateFlow<List<TenancyAgreement>> = repository.agreements

    // Derived Admin Dashboard stats
    val summaryState: StateFlow<AdminSummaryState> = combine(
        rooms,
        invoices,
        expenses,
        complaints
    ) { roomList, invoiceList, expenseList, complaintList ->
        val total = roomList.size
        val occupied = roomList.count { it.status == RoomStatus.TERISI }
        val available = roomList.count { it.status == RoomStatus.TERSEDIA }
        val repair = roomList.count { it.status == RoomStatus.PERBAIKAN }
        val rate = if (total > 0) ((occupied.toDouble() / total) * 100).toInt() else 0

        val paidInvoices = invoiceList.filter { it.statusPembayaran == PaymentStatus.LUNAS }
        val unpaidInvoices = invoiceList.filter { it.statusPembayaran != PaymentStatus.LUNAS }

        val revenue = paidInvoices.sumOf { it.totalTagihan }
        val unpaidTotal = unpaidInvoices.sumOf { it.totalTagihan }
        val expenseTotal = expenseList.sumOf { it.nominal }
        val net = (revenue - expenseTotal).coerceAtLeast(0L)

        val pendingComplaints = complaintList.count { it.statusKomplain != ComplaintStatus.SELESAI }

        AdminSummaryState(
            totalRooms = total,
            occupiedRooms = occupied,
            availableRooms = available,
            repairRooms = repair,
            occupancyRate = rate,
            totalRevenue = revenue,
            unpaidCount = unpaidInvoices.size,
            unpaidAmount = unpaidTotal,
            totalExpense = expenseTotal,
            netProfit = net,
            pendingComplaintsCount = pendingComplaints
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminSummaryState()
    )

    fun setRole(role: UserRole) {
        repository.setRole(role)
    }

    fun addRoom(nomor: String, tipe: String, harga: Long, fasilitas: List<String>) {
        repository.addRoom(nomor, tipe, harga, fasilitas)
    }

    fun updateRoomStatus(roomId: String, newStatus: RoomStatus, penghuniNama: String? = null, penghuniHp: String? = null) {
        repository.updateRoomStatus(roomId, newStatus, penghuniNama, penghuniHp)
    }

    fun checkInTenant(roomId: String, nama: String, noHp: String, deposit: Long = 500000L) {
        repository.checkInTenant(roomId, nama, noHp, deposit)
    }

    fun checkOutTenant(roomId: String) {
        repository.checkOutTenant(roomId)
    }

    fun updateRoom(roomId: String, nomor: String, tipe: String, harga: Long, fasilitas: List<String>) {
        repository.updateRoom(roomId, nomor, tipe, harga, fasilitas)
    }

    fun deleteRoom(roomId: String) {
        repository.deleteRoom(roomId)
    }

    fun addExpense(
        kategori: ExpenseCategory,
        nominal: Long,
        deskripsi: String,
        tanggal: String,
        buktiStruk: String? = null
    ) {
        repository.addExpense(kategori, nominal, deskripsi, tanggal, buktiStruk)
    }

    fun deleteExpense(expenseId: String) {
        repository.deleteExpense(expenseId)
    }

    fun payInvoice(invoiceId: String, method: PaymentMethod, buktiUri: String? = null) {
        repository.payInvoice(invoiceId, method, buktiUri)
    }

    fun uploadPaymentProof(invoiceId: String, buktiUri: String) {
        repository.uploadPaymentProof(invoiceId, buktiUri)
    }

    fun markInvoiceUnpaid(invoiceId: String) {
        repository.markInvoiceUnpaid(invoiceId)
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
        repository.createInvoice(
            roomNomor, penghuniNama, nominalKamar, tambahanListrik, denda, bulanTagihan, tanggalJatuhTempo, metodePembayaran
        )
    }

    fun updateInvoiceAdjustment(invoiceId: String, tambahanListrik: Long, denda: Long) {
        repository.updateInvoiceAdjustment(invoiceId, tambahanListrik, denda)
    }

    fun generateMonthlyInvoices(bulanTagihan: String, jatuhTempo: String): Int {
        return repository.generateMonthlyInvoices(bulanTagihan, jatuhTempo)
    }

    fun deleteInvoice(invoiceId: String) {
        repository.deleteInvoice(invoiceId)
    }

    fun addComplaint(
        penghuniNama: String,
        roomNomor: String,
        judul: String,
        deskripsi: String,
        kategori: String = "Umum",
        fotoKerusakanUri: String? = null
    ) {
        repository.addComplaint(penghuniNama, roomNomor, judul, deskripsi, kategori, fotoKerusakanUri)
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: ComplaintStatus,
        catatan: String? = null,
        fotoPerbaikanUri: String? = null
    ) {
        repository.updateComplaintStatus(complaintId, newStatus, catatan, fotoPerbaikanUri)
    }


    fun getProfitSharingReport(): ProfitSharingReport {
        return repository.getProfitSharingReport()
    }

    fun addInvestor(nama: String, email: String, rekening: String, persentase: Double) {
        repository.addInvestor(nama, email, rekening, persentase)
    }

    fun updateInvestor(id: String, nama: String, email: String, rekening: String, persentase: Double) {
        repository.updateInvestor(id, nama, email, rekening, persentase)
    }

    fun deleteInvestor(id: String) {
        repository.deleteInvestor(id)
    }

    fun transferInvestorPayout(investorNama: String, tanggal: String = "Hari ini") {
        repository.transferInvestorPayout(investorNama, tanggal)
    }

    fun revertInvestorPayout(investorNama: String) {
        repository.revertInvestorPayout(investorNama)
    }

    fun addAnnouncement(
        judul: String,
        isi: String,
        prioritas: AnnouncementPriority = AnnouncementPriority.NORMAL,
        kategori: String = "Umum"
    ) {
        repository.addAnnouncement(judul, isi, prioritas, kategori)
    }

    fun deleteAnnouncement(id: String) {
        repository.deleteAnnouncement(id)
    }

    // Reminder & WorkManager features (Opsi B)
    private val _reminderConfig = MutableStateFlow(ReminderConfig())
    val reminderConfig: StateFlow<ReminderConfig> = _reminderConfig.asStateFlow()

    fun toggleAutoReminder(context: Context, enabled: Boolean) {
        _reminderConfig.update { it.copy(autoReminderEnabled = enabled) }
        if (enabled) {
            DueReminderWorker.enqueuePeriodicReminder(context)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(DueReminderWorker.PERIODIC_WORK_TAG)
        }
    }

    fun triggerBroadcastReminders(context: Context) {
        DueReminderWorker.triggerImmediateReminder(context)
        _reminderConfig.update { it.copy(lastSyncText = "Baru saja disinkronkan via WorkManager") }
    }

    fun sendTestTenantReminder(context: Context, invoice: Invoice) {
        NotificationHelper.sendInvoiceDueNotification(
            context = context,
            invoice = invoice,
            urgencyMessage = "Jatuh tempo pada ${invoice.tanggalJatuhTempo} (Pengingat Otomatis)"
        )
    }

    fun sendInstantTestNotification(context: Context, title: String, message: String) {
        NotificationHelper.sendInstantNotification(context, title, message)
    }

    fun getAgreementForRoom(roomNomor: String): TenancyAgreement {
        return repository.getAgreementForRoom(roomNomor) ?: TenancyAgreement(
            roomNomor = roomNomor,
            nomorSurat = "0${roomNomor}/SP-KOS/XI/2026",
            pihakKeduaNama = rooms.value.find { it.nomorKamar == roomNomor }?.penghuniNama ?: "Penghuni"
        )
    }

    fun saveAgreement(agreement: TenancyAgreement) {
        repository.saveAgreement(agreement)
    }
}
