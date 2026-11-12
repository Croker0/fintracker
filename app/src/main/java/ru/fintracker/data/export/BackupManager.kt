package ru.fintracker.data.export

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.fintracker.data.model.TransactionType
import ru.fintracker.data.repository.FinanceRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local JSON backup. In a production app the same payload could be uploaded to a server;
 * this study project keeps it on the device so the flow is fully demonstrable offline.
 */
object BackupManager {

    private val ISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val STAMP = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    suspend fun writeLocalBackup(context: Context, repository: FinanceRepository): File =
        withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, "backups").apply { mkdirs() }
            val file = File(directory, "latest.json")
            file.writeText(buildJson(repository), Charsets.UTF_8)
            file
        }

    suspend fun exportToCache(context: Context, repository: FinanceRepository): File =
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(directory, "fintracker_backup_${STAMP.format(Date())}.json")
            file.writeText(buildJson(repository), Charsets.UTF_8)
            file
        }

    private suspend fun buildJson(repository: FinanceRepository): String {
        val rows = repository.exportRows()
        val income = rows.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expense = rows.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }

        val transactions = JSONArray()
        rows.forEach { row ->
            transactions.put(
                JSONObject()
                    .put("date", ISO.format(Date(row.date)))
                    .put("type", row.type.name)
                    .put("category", row.category)
                    .put("amountMinor", row.amountMinor)
                    .put("note", row.note)
                    .put("tags", JSONArray(row.tags))
            )
        }

        return JSONObject()
            .put("app", "FinTracker")
            .put("schemaVersion", 1)
            .put("exportedAt", ISO.format(Date()))
            .put("currency", "RUB")
            .put("totals", JSONObject().put("incomeMinor", income).put("expenseMinor", expense))
            .put("transactions", transactions)
            .toString(2)
    }
}
