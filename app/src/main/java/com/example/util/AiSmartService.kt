package com.example.util

import android.util.Log
import com.example.data.model.Expense
import com.example.data.model.ProfitSharingReport
import com.example.data.repository.KosRepository
import com.example.ui.AdminSummaryState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiSmartService"

data class ComplaintAiTriage(
    val tingkatUrgensi: String = "SEDANG",
    val estimasiBiayaMin: Long = 100000L,
    val estimasiBiayaMax: Long = 250000L,
    val estimasiBiayaFormatted: String = "Rp 100.000 - Rp 250.000",
    val solusiTeknis: String = "Pemeriksaan teknisi standar.",
    val rekomendasiTindakan: String = "Jadwalkan kunjungan teknisi."
)

object AiSmartService {

    // General purpose model for streaming chat & financial narrative
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )
    }

    // Structured JSON model for Complaint Triage
    private val triageSchema = Schema.obj(
        properties = mapOf(
            "tingkatUrgensi" to Schema.string(description = "Tingkat urgensi: MENDESAK, SEDANG, atau MINOR"),
            "estimasiBiayaMin" to Schema.integer(description = "Estimasi biaya perbaikan terendah dalam Rupiah"),
            "estimasiBiayaMax" to Schema.integer(description = "Estimasi biaya perbaikan tertinggi dalam Rupiah"),
            "solusiTeknis" to Schema.string(description = "Diagnosa teknis dan penyebab kemungkinan kerusakan"),
            "rekomendasiTindakan" to Schema.string(description = "Saran langkah perbaikan konkrit untuk pengelola")
        )
    )

    private val triageModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = triageSchema
                temperature = 0.2f
            }
        )
    }

    /**
     * AI Smart Triage for facility maintenance complaints using structured JSON output.
     */
    suspend fun analyzeComplaint(
        judul: String,
        deskripsi: String,
        roomNomor: String
    ): ComplaintAiTriage {
        return withTimeout(50_000L) {
            val prompt = """
                Anda adalah AI Konsultan Teknisi & Maintenance Properti Kosan di Indonesia.
                Analisis keluhan kerusakan fasilitas berikut dari Kamar $roomNomor:
                - Judul Kerusakan: $judul
                - Deskripsi: $deskripsi

                Berikan:
                1. Tingkat urgensi (MENDESAK jika membahayakan/mengganggu tidur/air/listrik mati; SEDANG jika kenyamanan terganggu; MINOR jika kosmetik).
                2. Estimasi kisaran biaya perbaikan wajar di Indonesia (IDR).
                3. Diagnosa teknis ringkas dan penyebabnya.
                4. Rekomendasi langkah penanganan teknisi.
            """.trimIndent()

            val response = triageModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("AI mengembalikan respon kosong.")

            val json = JSONObject(jsonText)
            val urgensi = json.optString("tingkatUrgensi", "SEDANG")
            val minBiaya = json.optLong("estimasiBiayaMin", 100000L)
            val maxBiaya = json.optLong("estimasiBiayaMax", 250000L)
            val solusi = json.optString("solusiTeknis", "Pemeriksaan teknisi.")
            val tindakan = json.optString("rekomendasiTindakan", "Hubungi teknisi spesialis.")

            ComplaintAiTriage(
                tingkatUrgensi = urgensi,
                estimasiBiayaMin = minBiaya,
                estimasiBiayaMax = maxBiaya,
                estimasiBiayaFormatted = "${KosRepository.formatRupiah(minBiaya)} - ${KosRepository.formatRupiah(maxBiaya)}",
                solusiTeknis = solusi,
                rekomendasiTindakan = tindakan
            )
        }
    }

    /**
     * AI Financial Analysis stream generating professional insights and cost optimization tips.
     */
    fun generateFinancialAnalysisStream(
        report: ProfitSharingReport,
        summary: AdminSummaryState,
        expenses: List<Expense>
    ): Flow<String> = flow {
        val expenseSummary = expenses.joinToString(", ") { "${it.kategori.label}: ${KosRepository.formatRupiah(it.nominal)}" }

        val prompt = """
            Anda adalah Chief Financial Officer (CFO) & Konsultan Bisnis Properti Kosan KosConnect di Indonesia.
            Analisis data keuangan kosan bulan ${report.periodeBulan} berikut:
            - Okupansi: ${summary.occupancyRate}% (${summary.occupiedRooms} dari ${summary.totalRooms} kamar terisi)
            - Total Pemasukan: ${KosRepository.formatRupiah(report.totalPemasukan)}
            - Total Biaya Operasional: ${KosRepository.formatRupiah(report.totalPengeluaran)}
            - Rincian Pengeluaran: $expenseSummary
            - Laba Bersih: ${KosRepository.formatRupiah(report.labaBersih)}
            - Tagihan Tertunggak: ${summary.unpaidCount} kamar (${KosRepository.formatRupiah(summary.unpaidAmount)})

            Tolong buatkan analisis profesional, ringkas, dan actionable dalam Bahasa Indonesia yang mencakup:
            1. **Evaluasi Kesehatan Finansial & Margin Laba**
            2. **Peluang Efisiensi & Pemotongan Biaya Operasional** (fokus pada pos terbesar)
            3. **Proyeksi Pendapatan Bulan Berikutnya**
            4. **Strategi Cepat Mengisi Kamar Kosong**

            Gunakan gaya bahasa santun, profesional, berbobot, dan gunakan poin-poin tebal yang mudah dibaca.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                generalModel.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gagal generate analisa keuangan", e)
            emit("\n[Gagal memuat respon AI: ${e.localizedMessage ?: "Koneksi terputus"}]")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * General AI Assistant chat stream for boarding house management tips.
     */
    fun askKosAssistantStream(
        userPrompt: String,
        contextInfo: String
    ): Flow<String> = flow {
        val prompt = """
            Anda adalah "KosConnect AI Assistant", asisten pintar pengelola kosan ramah dan ahli manajemen properti.
            Konteks Kosan Saat Ini:
            $contextInfo

            Pertanyaan Pengguna:
            $userPrompt

            Jawablah dengan praktis, solutif, berbasis situasi kos-kosan di Indonesia, dan gunakan format yang rapi.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                generalModel.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gagal streaming chat asisten", e)
            emit("\n[Gagal memuat respon AI: ${e.localizedMessage ?: "Koneksi terputus"}]")
        }
    }.flowOn(Dispatchers.IO)
}
