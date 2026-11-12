package ru.fintracker.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.fintracker.core.Money
import ru.fintracker.data.model.TransactionType
import ru.fintracker.data.repository.FinanceRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val DATE = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru", "RU"))
    private val STAMP = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    suspend fun export(context: Context, repository: FinanceRepository): File =
        withContext(Dispatchers.IO) {
            val rows = repository.exportRows()
            val directory = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(directory, "fintracker_${STAMP.format(Date())}.csv")

            val builder = StringBuilder("\uFEFF")
            builder.append("Дата;Тип;Категория;Сумма;Комментарий;Теги\r\n")
            rows.forEach { row ->
                builder.append(quote(DATE.format(Date(row.date)))).append(';')
                    .append(quote(if (row.type == TransactionType.EXPENSE) "Расход" else "Доход"))
                    .append(';')
                    .append(quote(row.category)).append(';')
                    .append(Money.plain(row.amountMinor)).append(';')
                    .append(quote(row.note)).append(';')
                    .append(quote(row.tags.joinToString(" ") { "#$it" }))
                    .append("\r\n")
            }
            file.writeText(builder.toString(), Charsets.UTF_8)
            file
        }

    private fun quote(value: String): String =
        "\"" + value.replace("\"", "\"\"") + "\""
}

object FileSharer {

    fun share(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
