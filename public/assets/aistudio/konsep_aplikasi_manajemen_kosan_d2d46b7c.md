# Konsep Aplikasi Manajemen Kosan (Nama Sementara: "KosConnect" / "JuraganKos")

## 1. Latar Belakang & Tujuan
Banyak pemilik kos (juragan kos) kesulitan mengelola pembayaran, ketersediaan kamar, dan keluhan penghuni, terutama jika memiliki lebih dari satu properti atau jumlah kamar yang banyak. Aplikasi ini bertujuan untuk mendigitalisasi dan mengotomatisasi proses pengelolaan kos agar lebih transparan, rapi, dan mudah dipantau dari mana saja.

## 2. Target Pengguna (User Roles)
Aplikasi ini akan memiliki tiga jenis antarmuka/peran pengguna:
1. **Pemilik/Pengelola Kos (Admin):** Orang yang memiliki dan mengelola operasional properti kos sehari-hari.
2. **Penghuni Kos (Tenant):** Orang yang menyewa dan tinggal di kamar kos.
3. **Investor:** Pihak yang memberikan modal untuk pembangunan atau pembelian properti kos, dan berhak mendapatkan pembagian hasil (profit sharing) sesuai persentase kepemilikan.

## 3. Fitur Utama

### A. Fitur untuk Pemilik Kos (Admin)
*   **Dashboard Utama:** Ringkasan jumlah kamar kosong/terisi, total pendapatan bulan ini, dan tagihan yang belum dibayar.
*   **Manajemen Kamar & Properti:** Menambah/mengedit data properti kos, mengatur tipe kamar, harga sewa, dan status ketersediaan.
*   **Manajemen Penghuni:** Mencatat data diri penghuni (KTP, kontak darurat), tanggal masuk, dan masa sewa.
*   **Sistem Penagihan (Billing):** 
    *   Membuat tagihan sewa otomatis setiap bulan.
    *   Mengirim notifikasi pengingat tagihan ke penghuni (via push notification/WhatsApp).
    *   Verifikasi bukti pembayaran.
*   **Pencatatan Pengeluaran:** Mencatat biaya operasional seperti listrik, air, kebersihan, dan perbaikan.
*   **Manajemen Investor & Bagi Hasil (Profit Sharing):**
    *   Mendaftarkan data investor dan mengatur persentase investasi/saham tiap orang (misal: Admin 20%, Investor A 50%, Investor B 30%).
    *   Kalkulasi bagi hasil otomatis di akhir bulan berdasarkan Laba Bersih (Total Pendapatan - Total Pengeluaran) dikali persentase masing-masing investor.
*   **Manajemen Komplain:** Menerima dan merespons laporan kerusakan atau keluhan dari penghuni.
*   **Laporan Keuangan:** Laporan laba/rugi yang transparan per bulan.

### B. Fitur untuk Penghuni Kos (Tenant)
*   **Profil Kamar:** Melihat informasi detail kamar yang disewa dan masa aktif sewa.
*   **Tagihan & Pembayaran:** 
    *   Melihat rincian tagihan (uang kos, listrik, denda jika ada).
    *   Mengunggah bukti transfer pembayaran.
*   **Lapor Keluhan (Maintenance):** Mengirim tiket komplain (misal: "AC bocor", "Lampu kamar mandi mati") beserta foto, dan melacak status perbaikannya.
*   **Papan Pengumuman:** Melihat informasi atau aturan terbaru yang dibagikan oleh pengelola.

### C. Fitur untuk Investor
*   **Dashboard Transparansi Keuangan (Read-Only):** Melihat secara langsung tingkat keterisian kamar (*occupancy rate*), total pemasukan, dan rincian pengeluaran operasional kos tanpa bisa mengubah datanya.
*   **Laporan Bagi Hasil (Dividen):** Melihat nominal hak bagi hasil bulan ini yang dikalkulasi otomatis oleh sistem.
*   **Riwayat Pencairan (Withdrawal):** Melacak status pencairan dana bagi hasil dari pengelola ke rekening bank investor (misalnya status: "Menunggu Ditransfer" atau "Sudah Ditransfer").

## 4. Alur Penggunaan Sederhana (User Flow)
1. **Onboarding:** Admin mendaftar, membuat profil properti kos, menambahkan data kamar, dan mengundang/mendaftarkan akun Investor dengan persentasenya.
2. **Invite Penghuni:** Admin menambahkan data penghuni ke kamar tertentu. Penghuni akan mendapatkan kode unik/link untuk login.
3. **Siklus Bulanan:** 
   * Aplikasi membuat tagihan otomatis. Penghuni membayar dan unggah bukti. Admin memverifikasi.
   * Admin mencatat pengeluaran operasional bulan tersebut.
   * Di akhir bulan, sistem otomatis memotong Pemasukan Kotor dengan Pengeluaran untuk mendapatkan Laba Bersih.
   * Laba Bersih dipecah sesuai persentase untuk Admin dan Investor. Investor bisa memantau nominalnya dari HP mereka.

## 5. Ide Model Bisnis (Monetization)
Jika aplikasi ini ingin Anda komersialkan ke pemilik kos lain (SaaS):
*   **Freemium:** Gratis untuk pemilik dengan maksimal 5 kamar.
*   **Tiered Subscription:** Berlangganan bulanan/tahunan berdasarkan jumlah kamar (misal: Paket 10-20 kamar Rp 50.000/bulan, Paket >50 kamar Rp 150.000/bulan).
*   **Biaya Admin Payment Gateway:** Jika aplikasi diintegrasikan dengan transfer bank otomatis, bisa ada margin biaya admin dari transaksi.

## 6. Rekomendasi Teknologi Mobile
*   **Platform:** Flutter atau React Native (agar bisa langsung jalan di Android dan iOS dengan satu basis kode).
*   **Database/Backend:** Firebase (untuk kecepatan pengembangan) atau Supabase / Node.js dengan PostgreSQL.