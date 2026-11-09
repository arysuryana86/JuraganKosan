# Struktur Halaman & Konsep Wireframe

Dokumen ini merangkum daftar halaman (screens) yang dibutuhkan dalam aplikasi, beserta sketsa kasar tata letak (layout) untuk layar-layar utama.

## 1. Daftar Halaman Berdasarkan Peran (Role)

### A. Layar Admin (Pemilik/Pengelola)

1. **Layar Login & Register**
2. **Dashboard (Beranda):** Ringkasan performa kos (Okupansi, Tunggakan, Laba bulan berjalan).
3. **Menu Kamar:** Daftar seluruh kamar, status (kosong/terisi), form tambah kamar.
4. **Menu Penghuni:** Daftar penghuni aktif, form tambah penghuni, detail kontak.
5. **Menu Keuangan:**
   * Tab Pemasukan (Status pembayaran otomatis dari Payment Gateway).
   * Tab Pengeluaran (Input biaya listrik, air, maintenance).
   * Tab Investor & Bagi Hasil (Kalkulasi laba bersih dan alokasi per investor).
6. **Menu Komplain:** Daftar keluhan penghuni dan status penyelesaian.
7. **Menu Profil/Pengaturan:** Pengaturan akun, notifikasi, log out.

### B. Layar Penghuni (Tenant)

1. **Layar Login** (Menggunakan nomor HP atau kode unik).
2. **Dashboard (Beranda):** Info kamar saat ini, sisa hari jatuh tempo, pengumuman kos.
3. **Menu Tagihan:** Riwayat tagihan, pilihan metode bayar (VA/QRIS), dan instruksi pembayaran.
4. **Menu Lapor (Komplain):** Form untuk membuat tiket kerusakan (deskripsi + foto).
5. **Menu Profil:** Data diri dan kontak darurat.

### C. Layar Investor

1. **Layar Login**
2. **Dashboard (Beranda):** Ringkasan portofolio, estimasi bagi hasil, tingkat keterisian kamar.
3. **Menu Laporan Keuangan:** Rincian transparan arus kas tiap bulan (Read-only).
4. **Menu Riwayat Pencairan:** Daftar pencairan dana bagi hasil.
5. **Menu Profil:** Data bank investor dan pengaturan akun.

## 2. Konsep Wireframe (Tata Letak) Teks

### Wireframe 1: Dashboard Admin

```text
====================================
[Logo KosConnect]       [Notifikasi(🔔)]
------------------------------------
Halo, Juragan Budi!
------------------------------------
[ Kartu Okupansi ]
  Kamar Terisi: 18 / 20
  Kamar Kosong: 2
------------------------------------
[ Kartu Keuangan Bulan Ini ]
  Pendapatan   : Rp 20.000.000
  Pengeluaran  : Rp  3.000.000
  Laba Bersih  : Rp 17.000.000
------------------------------------
[ Tindakan Cepat (Quick Actions) ]
 ( Tambah Kamar )  ( Catat Pengeluaran )
 ( Tagih Bayaran)  ( Lihat Komplain )
------------------------------------
[ Perlu Perhatian ]
  > 3 Penghuni belum bayar tagihan
  > 1 Komplain baru (AC Bocor)
====================================
[Beranda] [Kamar] [Keuangan] [Profil]
====================================
```

### Wireframe 2: Halaman Tagihan Penghuni (Otomatis VA/QRIS)

```text
====================================
           Tagihan Saya
------------------------------------
Bulan: Oktober 2026
------------------------------------
[ Rincian Tagihan ]
  Sewa Kamar 101    : Rp 1.500.000
  Listrik           : Rp   150.000
  Denda Keterlambatan: Rp        0
  --------------------------------
  Total Tagihan     : Rp 1.650.000
------------------------------------
Status: BELUM DIBAYAR (Tenggat: 5 Nov)
------------------------------------
[ Metode Pembayaran ]
  (o) BCA Virtual Account
  ( ) Mandiri Virtual Account
  ( ) QRIS (GoPay/OVO/Dana)
------------------------------------
[ Tombol: BAYAR SEKARANG ] 
*(Akan memunculkan nomor VA / Barcode)*
====================================
[Beranda] [Tagihan] [Lapor] [Profil]
====================================
```

### Wireframe 3: Dashboard Investor

```text
====================================
[Logo KosConnect]            [Profil]
------------------------------------
Halo, Investor Andi! (Saham: 30%)
------------------------------------
[ Ringkasan Performa Bulan Ini ]
  Okupansi Kos       : 90% (18/20)
------------------------------------
[ Estimasi Bagi Hasil Anda ]
  Rp 5.100.000 
  *(30% dari Laba Bersih Rp 17.000.000)*
------------------------------------
[ Laporan Arus Kas (Bulan Ini) ]
  (+) Pemasukan Sewa  : Rp 20.000.000
  (-) Biaya Listrik   : Rp  2.000.000
  (-) Gaji Penjaga    : Rp  1.000.000
  ---------------------------------
  (=) Laba Bersih     : Rp 17.000.000
------------------------------------
[ Tombol: Unduh Laporan PDF ]
====================================
[ Beranda ] [ Laporan Keuangan ] [ Riwayat ]
====================================
```