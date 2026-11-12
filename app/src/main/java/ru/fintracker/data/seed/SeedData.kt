package ru.fintracker.data.seed

import ru.fintracker.data.local.entity.CategoryEntity
import ru.fintracker.data.model.TransactionType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Demo content for the first launch and for the "Демо-данные" action.
 * Numbers are stored in minor units (kopecks).
 */
object SeedData {

    data class SeedCategory(
        val key: String,
        val name: String,
        val emoji: String,
        val color: Int,
        val kind: TransactionType,
        val sortOrder: Int
    ) {
        fun toEntity(): CategoryEntity = CategoryEntity(
            name = name,
            emoji = emoji,
            color = color,
            kind = kind,
            isDefault = true,
            sortOrder = sortOrder
        )
    }

    data class SeedTransaction(
        val categoryKey: String,
        val amountMinor: Long,
        val type: TransactionType,
        val note: String,
        val date: Long,
        val tags: List<String>
    )

    val categories: List<SeedCategory> = listOf(
        SeedCategory("food", "Продукты", "🛒", 0xFFF97316.toInt(), TransactionType.EXPENSE, 1),
        SeedCategory("cafe", "Кафе и рестораны", "☕", 0xFFEF4444.toInt(), TransactionType.EXPENSE, 2),
        SeedCategory("transport", "Транспорт", "🚕", 0xFF3B82F6.toInt(), TransactionType.EXPENSE, 3),
        SeedCategory("home", "Жильё", "🏠", 0xFF8B5CF6.toInt(), TransactionType.EXPENSE, 4),
        SeedCategory("health", "Здоровье", "💊", 0xFF14B8A6.toInt(), TransactionType.EXPENSE, 5),
        SeedCategory("fun", "Развлечения", "🎮", 0xFFEC4899.toInt(), TransactionType.EXPENSE, 6),
        SeedCategory("clothes", "Одежда", "👕", 0xFFF59E0B.toInt(), TransactionType.EXPENSE, 7),
        SeedCategory("education", "Образование", "📚", 0xFF6366F1.toInt(), TransactionType.EXPENSE, 8),
        SeedCategory("gifts", "Подарки", "🎁", 0xFFA855F7.toInt(), TransactionType.EXPENSE, 9),
        SeedCategory("travel", "Путешествия", "✈️", 0xFF0EA5E9.toInt(), TransactionType.EXPENSE, 10),
        SeedCategory("other", "Другое", "🧾", 0xFF64748B.toInt(), TransactionType.EXPENSE, 11),
        SeedCategory("salary", "Зарплата", "💼", 0xFF16A34A.toInt(), TransactionType.INCOME, 1),
        SeedCategory("freelance", "Фриланс", "💻", 0xFF0D9488.toInt(), TransactionType.INCOME, 2),
        SeedCategory("invest", "Инвестиции", "📈", 0xFF65A30D.toInt(), TransactionType.INCOME, 3),
        SeedCategory("other_income", "Другое", "💰", 0xFF64748B.toInt(), TransactionType.INCOME, 4)
    )

    fun transactions(today: LocalDate = LocalDate.now()): List<SeedTransaction> {
        val zone = ZoneId.systemDefault()

        fun tx(
            key: String,
            amount: Long,
            daysAgo: Int,
            hour: Int,
            note: String,
            tags: List<String> = emptyList(),
            type: TransactionType = TransactionType.EXPENSE
        ): SeedTransaction {
            val time = LocalTime.of(hour.coerceIn(0, 23), 0)
            val date = today.minusDays(daysAgo.toLong()).atTime(time).atZone(zone).toInstant().toEpochMilli()
            return SeedTransaction(key, amount, type, note, date, tags)
        }

        return listOf(
            tx("food", 145_000, 0, 19, "Пятёрочка"),
            tx("cafe", 32_000, 0, 9, "Кофе с собой"),
            tx("transport", 6_500, 1, 8, "Метро"),
            tx("food", 230_000, 1, 20, "Лента"),
            tx("cafe", 125_000, 2, 13, "Обед с коллегами", listOf("работа")),
            tx("fun", 90_000, 2, 19, "Кино"),
            tx("health", 62_000, 3, 12, "Аптека"),
            tx("food", 78_000, 3, 18, "Магнит"),
            tx("clothes", 540_000, 4, 16, "Кроссовки"),
            tx("transport", 40_000, 4, 21, "Такси"),
            tx("salary", 8_500_000, 5, 10, "Зарплата", listOf("работа"), TransactionType.INCOME),
            tx("cafe", 45_000, 5, 9, "Завтрак"),
            tx("home", 350_000, 6, 11, "Коммунальные услуги"),
            tx("food", 198_000, 6, 19, "Ашан"),
            tx("fun", 220_000, 7, 20, "Концерт"),
            tx("transport", 6_500, 7, 8, "Метро"),
            tx("food", 112_000, 8, 18, "Перекрёсток"),
            tx("cafe", 68_000, 8, 14, "Бизнес-ланч"),
            tx("education", 490_000, 9, 12, "Курс по Kotlin", listOf("работа", "учёба")),
            tx("food", 94_000, 9, 19, "ВкусВилл"),
            tx("health", 150_000, 10, 10, "Стоматология", listOf("здоровье")),
            tx("transport", 30_000, 10, 18, "Такси"),
            tx("gifts", 320_000, 11, 17, "Подарок маме", listOf("семья")),
            tx("food", 165_000, 11, 19, "Пятёрочка"),
            tx("freelance", 2_200_000, 12, 15, "Фриланс-проект", listOf("работа"), TransactionType.INCOME),
            tx("cafe", 52_000, 12, 13, "Кофе"),
            tx("travel", 780_000, 13, 9, "Билеты в Казань", listOf("отпуск")),
            tx("food", 87_000, 13, 20, "Магнит"),
            tx("home", 120_000, 14, 12, "Хозтовары"),
            tx("fun", 150_000, 14, 19, "Бассейн"),
            tx("food", 245_000, 15, 18, "Ашан"),
            tx("transport", 6_500, 15, 8, "Метро"),
            tx("cafe", 89_000, 16, 20, "Ужин"),
            tx("clothes", 230_000, 16, 15, "Футболка"),
            tx("health", 70_000, 17, 11, "Витамины", listOf("здоровье")),
            tx("food", 134_000, 17, 19, "ВкусВилл"),
            tx("fun", 60_000, 18, 17, "Книга"),
            tx("transport", 28_000, 18, 21, "Такси"),
            tx("food", 156_000, 19, 18, "Перекрёсток"),
            tx("cafe", 41_000, 19, 9, "Кофе с собой"),
            tx("education", 150_000, 20, 10, "Подписка на курсы"),
            tx("home", 280_000, 20, 14, "Интернет и связь"),
            tx("food", 210_000, 21, 19, "Лента"),
            tx("transport", 6_500, 21, 8, "Метро"),
            tx("gifts", 180_000, 22, 18, "Цветы", listOf("семья")),
            tx("cafe", 76_000, 22, 13, "Обед"),
            tx("health", 250_000, 23, 9, "Анализы", listOf("здоровье")),
            tx("food", 99_000, 23, 20, "Магнит"),
            tx("fun", 130_000, 24, 19, "Настольные игры"),
            tx("transport", 35_000, 24, 22, "Такси"),
            tx("food", 187_000, 25, 18, "Пятёрочка"),
            tx("cafe", 64_000, 25, 15, "Кофе"),
            tx("home", 420_000, 26, 11, "Ремонт крана"),
            tx("food", 123_000, 26, 19, "ВкусВилл"),
            tx("clothes", 360_000, 27, 16, "Куртка"),
            tx("transport", 6_500, 27, 8, "Метро"),
            tx("food", 160_000, 28, 18, "Ашан"),
            tx("fun", 80_000, 28, 20, "Кино"),
            tx("travel", 450_000, 29, 12, "Отель в Казани", listOf("отпуск")),
            tx("cafe", 55_000, 29, 9, "Завтрак"),
            tx("food", 205_000, 30, 19, "Лента"),
            tx("health", 48_000, 30, 12, "Аптека", listOf("здоровье")),
            tx("salary", 8_500_000, 35, 10, "Зарплата", listOf("работа"), TransactionType.INCOME),
            tx("other_income", 120_000, 2, 12, "Кэшбэк", emptyList(), TransactionType.INCOME)
        )
    }
}
