package com.example.data.model

import java.util.UUID

enum class UserRole(val label: String) {
    ADMIN("Pemilik / Admin"),
    PENGHUNI("Penghuni (Tenant)"),
    INVESTOR("Investor")
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val namaLengkap: String = "",
    val noHp: String = "",
    val email: String = "",
    val role: UserRole = UserRole.ADMIN,
    val fotoProfil: String? = null,
    val kontakDaruratNama: String? = null,
    val kontakDaruratHp: String? = null
)

data class Kos(
    val id: String = "kos-1",
    val adminId: String = "user-admin",
    val namaKos: String = "KosConnect Residence",
    val alamat: String = "Jl. Merdeka No. 45, Jakarta Selatan",
    val jumlahKamar: Int = 20
)

enum class RoomStatus(val label: String) {
    TERSEDIA("Tersedia"),
    TERISI("Terisi"),
    PERBAIKAN("Perbaikan")
}

data class Room(
    val id: String = UUID.randomUUID().toString(),
    val kosId: String = "kos-1",
    val nomorKamar: String = "101",
    val tipeKamar: String = "Deluxe AC",
    val hargaSewaDasar: Long = 1800000L,
    val status: RoomStatus = RoomStatus.TERSEDIA,
    val penghuniNama: String? = null,
    val penghuniHp: String? = null,
    val fasilitas: List<String> = listOf("AC", "Kamar Mandi Dalam", "WiFi", "Kasur Springbed")
)

enum class ContractStatus {
    AKTIF,
    SELESAI
}

data class Contract(
    val id: String = UUID.randomUUID().toString(),
    val penghuniId: String = "",
    val roomId: String = "",
    val tanggalMasuk: String = "",
    val tanggalKeluar: String? = null,
    val depositAwal: Long = 500000L,
    val dendaHarian: Long = 25000L,
    val statusSewa: ContractStatus = ContractStatus.AKTIF
)

enum class PaymentStatus(val label: String) {
    BELUM_BAYAR("Belum Bayar"),
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi"),
    LUNAS("Lunas")
}

enum class PaymentMethod(val label: String, val codePrefix: String) {
    BCA_VA("BCA Virtual Account", "80012"),
    MANDIRI_VA("Mandiri Virtual Account", "88019"),
    BRI_VA("BRI Virtual Account", "12890"),
    QRIS("QRIS (GoPay / OVO / Dana / ShopeePay)", "NMD-QRIS-")
}

data class Invoice(
    val id: String = UUID.randomUUID().toString(),
    val contractId: String = "contract-1",
    val roomNomor: String = "101",
    val penghuniNama: String = "Ahmad Pratama",
    val bulanTagihan: String = "November 2026",
    val tanggalJatuhTempo: String = "10 Nov 2026",
    val nominalKamar: Long = 1800000L,
    val tambahanListrik: Long = 150000L,
    val denda: Long = 0L,
    val totalTagihan: Long = 1950000L,
    val statusPembayaran: PaymentStatus = PaymentStatus.BELUM_BAYAR,
    val metodePembayaran: PaymentMethod? = PaymentMethod.BCA_VA,
    val kodePembayaran: String? = "800129847291823",
    val paymentGatewayRef: String? = "TX-MID-928172",
    val tanggalBayar: String? = null,
    val buktiPembayaranUri: String? = null
)


enum class ExpenseCategory(val label: String) {
    LISTRIK("Listrik & Token"),
    AIR("PDAM & Air Galon"),
    GAJI("Gaji Penjaga & Kebersihan"),
    PERBAIKAN("Perbaikan / Maintenance"),
    WIFI("Internet WiFi"),
    LAINNYA("Lain-lain")
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val kosId: String = "kos-1",
    val kategori: ExpenseCategory = ExpenseCategory.LAINNYA,
    val nominal: Long = 0L,
    val deskripsi: String = "",
    val tanggalPengeluaran: String = "",
    val buktiStruk: String? = null
)

data class InvestorShare(
    val id: String = UUID.randomUUID().toString(),
    val kosId: String = "kos-1",
    val investorId: String = "",
    val investorNama: String = "",
    val investorEmail: String = "",
    val rekeningBank: String = "",
    val persentase: Double = 0.0,
    val statusAktif: Boolean = true
)

enum class PayoutStatus(val label: String) {
    MENUNGGU("Menunggu Transfer"),
    DITRANSFER("Sudah Ditransfer")
}

data class InvestorPayout(
    val id: String = UUID.randomUUID().toString(),
    val investorNama: String = "",
    val sahamPersen: Double = 0.0,
    val nominalBagian: Long = 0L,
    val status: PayoutStatus = PayoutStatus.MENUNGGU,
    val nomorRekening: String = "BCA 8839-0129-39 (a.n Andi)",
    val tanggalTransfer: String? = null
)

data class WithdrawalRecord(
    val id: String = UUID.randomUUID().toString(),
    val periodeBulan: String = "November 2026",
    val nominal: Long = 0L,
    val tanggalTransfer: String = "6 Nov 2026",
    val nomorRekening: String = "BCA 8839-0129-39",
    val bankRef: String = "TRX-BCA-928172910",
    val status: PayoutStatus = PayoutStatus.DITRANSFER
)

data class ProfitSharingReport(
    val id: String = "ps-current",
    val kosId: String = "kos-1",
    val periodeBulan: String = "November 2026",
    val totalPemasukan: Long = 32400000L,
    val totalPengeluaran: Long = 6200000L,
    val labaBersih: Long = 26200000L,
    val payouts: List<InvestorPayout> = emptyList(),
    val pastWithdrawals: List<WithdrawalRecord> = emptyList()
)

enum class ComplaintStatus(val label: String) {
    MENUNGGU("Menunggu Tindakan"),
    DIPROSES("Sedang Dikerjakan"),
    SELESAI("Selesai Ditangani")
}

data class Complaint(
    val id: String = UUID.randomUUID().toString(),
    val penghuniNama: String = "",
    val roomNomor: String = "",
    val judulMasalah: String = "",
    val deskripsiMasalah: String = "",
    val kategori: String = "Fasilitas Kamar",
    val statusKomplain: ComplaintStatus = ComplaintStatus.MENUNGGU,
    val createdAt: String = "Hari ini, 09:30 WIB",
    val catatanPerbaikan: String? = null,
    val fotoKerusakanUri: String? = null,
    val fotoPerbaikanUri: String? = null
)


enum class AnnouncementPriority(val label: String) {
    NORMAL("Informasi"),
    PENTING("Penting"),
    DARURAT("Mendesak")
}

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val judul: String = "",
    val tanggal: String = "6 Nov 2026",
    val isi: String = "",
    val prioritas: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val kategori: String = "Umum"
)

enum class AgreementStatus(val label: String) {
    AKTIF("Berlaku & Sah"),
    MENUNGGU_TANDA_TANGAN("Menunggu Tanda Tangan"),
    SELESAI("Selesai")
}

data class TenancyAgreement(
    val id: String = UUID.randomUUID().toString(),
    val nomorSurat: String = "042/SP-KOS/XI/2026",
    val tanggalSurat: String = "1 Januari 2026",
    val roomNomor: String = "101",
    val tipeKamar: String = "Deluxe AC",
    // Pihak Pertama (Pengelola / Pemilik)
    val pihakPertamaNama: String = "Budi Prakoso",
    val pihakPertamaJabatan: String = "Pengelola KosConnect Residence",
    val pihakPertamaHp: String = "0812-9988-7766",
    val pihakPertamaAlamat: String = "Jl. Merdeka No. 45, Jakarta Selatan",
    // Pihak Kedua (Penyewa / Penghuni)
    val pihakKeduaNama: String = "Ahmad Pratama",
    val pihakKeduaNik: String = "3174092810980002",
    val pihakKeduaHp: String = "0812-3456-7890",
    val pihakKeduaPekerjaan: String = "Software Engineer",
    val pihakKeduaAlamatKtp: String = "Jl. Melati No. 12, Bandung",
    // Rincian Sewa
    val hargaSewaBulanan: Long = 1800000L,
    val depositJaminan: Long = 500000L,
    val tanggalMulai: String = "1 Januari 2026",
    val tanggalSelesai: String = "31 Desember 2026",
    val jatuhTempoTgl: Int = 10,
    val dendaKeterlambatanPerHari: Long = 25000L,
    val fasilitas: List<String> = listOf(
        "AC 1 PK Inverter",
        "Kamar Mandi Dalam & Water Heater",
        "Kasur Springbed Queen",
        "High-Speed WiFi 100 Mbps",
        "Lemari Pakaian 2 Pintu",
        "Meja & Kursi Kerja"
    ),
    val status: AgreementStatus = AgreementStatus.AKTIF,
    val signedAt: String = "1 Jan 2026, 10:15 WIB",
    val digitalSignatureHash: String = "E-SIGN-KOS-89283749210",
    val meteraiDigitalSerial: String = "MTR-DIGITAL-KOSCONNECT-2026-09182"
)
