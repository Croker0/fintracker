"""Builds the FinTracker defense deck (12 slides, 16:9)."""

from __future__ import annotations

from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Inches, Pt

ROOT = Path(__file__).resolve().parents[1]
IMAGES = ROOT / "docs" / "images"
OUT = ROOT / "presentation" / "FinTracker.pptx"
OUT.parent.mkdir(parents=True, exist_ok=True)

# ---------- palette ----------
BRAND = RGBColor.from_string("0E7C66")
BRAND_DARK = RGBColor.from_string("0B4F43")
BRAND_LIGHT = RGBColor.from_string("118A72")
MINT = RGBColor.from_string("D6F2EA")
MINT_TEXT = RGBColor.from_string("A9E5D9")
BG = RGBColor.from_string("F5F7F9")
WHITE = RGBColor.from_string("FFFFFF")
TEXT = RGBColor.from_string("101828")
MUTED = RGBColor.from_string("667085")
BORDER = RGBColor.from_string("E4E7EC")
SURFACE = RGBColor.from_string("EEF2F5")
EXPENSE = RGBColor.from_string("E5484D")
INCOME = RGBColor.from_string("12A150")
AMBER = RGBColor.from_string("F2A33A")

FONT = "Segoe UI"
MONO = "Consolas"
EMOJI_FONT = "Segoe UI Emoji"

SW, SH = 13.333, 7.5

prs = Presentation()
prs.slide_width = Inches(SW)
prs.slide_height = Inches(SH)
BLANK = prs.slide_layouts[6]
prs.core_properties.title = "ФинТрекер — трекер личных финансов"
prs.core_properties.author = "Штефан, Кислов, Ибрагимов, Давлетханов"
prs.core_properties.subject = "Учебный проект по Android-разработке"

_page = 1


def add_shape(slide, shape_type, x, y, w, h, fill, line=None, radius=None):
    shape = slide.shapes.add_shape(
        shape_type, Inches(x), Inches(y), Inches(w), Inches(h)
    )
    if fill is None:
        shape.fill.background()
    else:
        shape.fill.solid()
        shape.fill.fore_color.rgb = fill
    if line is None:
        shape.line.fill.background()
    else:
        shape.line.color.rgb = line
        shape.line.width = Pt(0.75)
    shape.shadow.inherit = False
    if radius is not None and shape.adjustments:
        shape.adjustments[0] = radius
    return shape


def add_text(
    slide,
    value,
    x,
    y,
    w,
    h,
    size=14,
    bold=False,
    color=TEXT,
    align=PP_ALIGN.LEFT,
    anchor=MSO_ANCHOR.TOP,
    font=FONT,
    italic=False,
    line_spacing=1.0,
):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.clear()
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    tf.vertical_anchor = anchor
    p = tf.paragraphs[0]
    p.alignment = align
    p.line_spacing = line_spacing
    run = p.add_run()
    run.text = value
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    run.font.name = font
    run.font.color.rgb = color
    return box


def shape_text(shape, value, size=12, bold=False, color=TEXT, font=FONT):
    tf = shape.text_frame
    tf.clear()
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    p = tf.paragraphs[0]
    p.alignment = PP_ALIGN.CENTER
    run = p.add_run()
    run.text = value
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.name = font
    run.font.color.rgb = color


def add_card(slide, x, y, w, h, fill=WHITE, line=BORDER, radius=0.09):
    return add_shape(
        slide, MSO_SHAPE.ROUNDED_RECTANGLE, x, y, w, h, fill, line, radius
    )


def add_pill(slide, x, y, w, h, value, fill, color, size=11, bold=True):
    shape = add_shape(
        slide, MSO_SHAPE.ROUNDED_RECTANGLE, x, y, w, h, fill, None, 0.5
    )
    shape_text(shape, value, size=size, bold=bold, color=color)
    return shape


def add_icon(slide, x, y, d, emoji, bg=MINT):
    circle = add_shape(slide, MSO_SHAPE.OVAL, x, y, d, d, bg, None)
    shape_text(circle, emoji, size=int(d * 24), color=TEXT, font=EMOJI_FONT)


def add_dot(slide, x, y, d, fill):
    add_shape(slide, MSO_SHAPE.OVAL, x, y, d, d, fill, None)


def add_arrow_down(slide, x, y, w=0.22, h=0.18, color=BRAND):
    shape = add_shape(slide, MSO_SHAPE.DOWN_ARROW, x, y, w, h, color, None)
    return shape


def add_arrow_right(slide, x, y, w=0.4, h=0.2, color=BRAND):
    add_shape(slide, MSO_SHAPE.RIGHT_ARROW, x, y, w, h, color, None)


def add_bullets(slide, items, x, y, w, size=11.5, gap=0.32, color=TEXT):
    for i, item in enumerate(items):
        add_dot(slide, x, y + i * gap + 0.06, 0.07, BRAND)
        add_text(slide, item, x + 0.18, y + i * gap - 0.02, w - 0.18, 0.3, size=size, color=color)


def background(slide, color=BG):
    fill = slide.background.fill
    fill.solid()
    fill.fore_color.rgb = color


def gradient_background(slide, c1, c2):
    shape = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height
    )
    shape.line.fill.background()
    shape.shadow.inherit = False
    fill = shape.fill
    fill.gradient()
    fill.gradient_angle = 35
    stops = fill.gradient_stops
    stops[0].color.rgb = c1
    stops[0].position = 0.0
    stops[1].color.rgb = c2
    stops[1].position = 1.0


def footer(slide, dark=False):
    global _page
    _page += 1
    color = MINT_TEXT if dark else MUTED
    line = RGBColor.from_string("2A6B5E") if dark else BORDER
    add_shape(slide, MSO_SHAPE.RECTANGLE, 0.6, 7.08, 12.13, 0.008, line, None)
    add_text(slide, "ФинТрекер · 2026", 0.6, 7.15, 4.0, 0.25, size=8.5, color=color)
    add_text(
        slide,
        f"{_page:02d}",
        12.0,
        7.15,
        0.73,
        0.25,
        size=8.5,
        color=color,
        align=PP_ALIGN.RIGHT,
    )


def content_slide(title, kicker):
    slide = prs.slides.add_slide(BLANK)
    background(slide)
    add_text(slide, kicker.upper(), 0.62, 0.33, 8.0, 0.25, size=9.5, bold=True, color=BRAND)
    add_text(slide, title, 0.6, 0.56, 12.1, 0.62, size=27, bold=True, color=TEXT)
    add_shape(slide, MSO_SHAPE.RECTANGLE, 0.62, 1.22, 0.5, 0.05, BRAND, None)
    footer(slide)
    return slide


def speaker_notes(slide, text):
    slide.notes_slide.notes_text_frame.text = text


def phone(slide, name, x, y, h, border=True):
    path = IMAGES / name
    w = h * 720 / 1560
    if border:
        add_card(slide, x - 0.06, y - 0.06, w + 0.12, h + 0.12, WHITE, BORDER, 0.04)
    slide.shapes.add_picture(str(path), Inches(x), Inches(y), Inches(w), Inches(h))
    return w


# =====================================================================
# 1. TITLE
# =====================================================================
s = prs.slides.add_slide(BLANK)
gradient_background(s, BRAND_DARK, BRAND_LIGHT)
add_shape(s, MSO_SHAPE.OVAL, 8.7, -1.7, 7.8, 7.8, RGBColor.from_string("0F6B59"), None)
add_shape(s, MSO_SHAPE.OVAL, 10.4, 4.6, 4.4, 4.4, RGBColor.from_string("0D5E50"), None)

logo = add_shape(s, MSO_SHAPE.ROUNDED_RECTANGLE, 0.7, 0.65, 0.85, 0.85, WHITE, None, 0.16)
shape_text(logo, "₽", size=30, bold=True, color=BRAND)

add_text(s, "УЧЕБНЫЙ ПРОЕКТ · ANDROID · 2026", 0.7, 1.85, 8.0, 0.3, size=11, bold=True, color=MINT_TEXT)
add_text(s, "ФинТрекер", 0.66, 2.15, 8.2, 0.95, size=54, bold=True, color=WHITE)
add_text(
    s,
    "Трекер личных финансов\nс категоризацией и графиками",
    0.7,
    3.1,
    8.2,
    1.05,
    size=24,
    color=WHITE,
    line_spacing=1.05,
)
add_text(
    s,
    "Учёт доходов и расходов, быстрый ввод, наглядная аналитика\nи экспорт отчёта — офлайн, на русском языке.",
    0.7,
    4.25,
    7.6,
    0.8,
    size=12.5,
    color=MINT_TEXT,
    line_spacing=1.15,
)

team = [
    ("Штефан", "главный разработчик"),
    ("Кислов", "backend и база данных"),
    ("Ибрагимов", "аналитик"),
    ("Давлетханов", "дизайнер"),
]
for i, (name, role) in enumerate(team):
    col, row = i % 2, i // 2
    x = 0.7 + col * 3.35
    y = 5.45 + row * 0.62
    pill = add_pill(s, x, y, 3.1, 0.5, f"{name}  ·  {role}", RGBColor.from_string("147A65"), WHITE, 9.5)
    pill.line.color.rgb = RGBColor.from_string("2A8F79")
    pill.line.width = Pt(0.75)

phone(s, "mockup-dashboard.png", 9.95, 0.55, 6.35, border=False)
add_text(s, "MVP · версия 1.0", 0.7, 7.05, 4.0, 0.3, size=9, color=MINT_TEXT)
speaker_notes(
    s,
    "Здравствуйте! Мы команда из четырёх человек. Наш проект — ФинТрекер, Android-приложение "
    "для учёта личных финансов. Покажу проблему, решение, архитектуру, дизайн и результаты.",
)

# =====================================================================
# 2. PROBLEM
# =====================================================================
s = content_slide("Проблема, которую решаем", "Зачем продукт")
pains = [
    ("01", "Траты забываются", "Записывать вручную долго: блокнот, таблица, ещё одно приложение."),
    ("02", "Непонятно, куда уходит", "Список операций без структуры не отвечает на главный вопрос месяца."),
    ("03", "Сложные приложения", "Подписки, банки и регистрация создают барьер на старте."),
]
for i, (num, title, text) in enumerate(pains):
    y = 1.62 + i * 1.22
    add_card(s, 0.65, y, 5.95, 1.05)
    add_text(s, num, 0.9, y + 0.26, 0.5, 0.4, size=16, bold=True, color=BRAND)
    add_text(s, title, 1.55, y + 0.16, 4.8, 0.3, size=14, bold=True, color=TEXT)
    add_text(s, text, 1.55, y + 0.48, 4.8, 0.45, size=10.5, color=MUTED)

quote = add_card(s, 6.95, 1.62, 5.75, 2.25, BRAND, None, 0.08)
add_text(s, "ПРОДУКТОВАЯ ИДЕЯ", 7.25, 1.86, 4.8, 0.25, size=9, bold=True, color=MINT_TEXT)
add_text(s, "«Записал трату —\nувидел картину месяца»", 7.22, 2.2, 5.2, 1.25, size=25, bold=True, color=WHITE, line_spacing=1.05)

metrics = [("15 сек", "на новую запись"), ("0", "сетевых запросов"), ("100%", "операций с категорией")]
for i, (value, label) in enumerate(metrics):
    x = 6.95 + i * 1.95
    add_card(s, x, 4.12, 1.8, 1.15)
    add_text(s, value, x + 0.15, 4.28, 1.5, 0.4, size=21, bold=True, color=BRAND)
    add_text(s, label, x + 0.15, 4.7, 1.55, 0.4, size=8.5, color=MUTED)

add_text(
    s,
    "Фокус версии 1.0 — ежедневный учёт и прозрачная структура расходов, без банковских интеграций.",
    0.65,
    5.85,
    12.0,
    0.4,
    size=12.5,
    color=MUTED,
)
speaker_notes(
    s,
    "Боль: люди не хотят вести таблицы, а существующие приложения перегружены. "
    "Мы делаем быстрый офлайн-учёт, где категория обязательна, а главный экран сразу показывает структуру.",
)

# =====================================================================
# 3. AUDIENCE
# =====================================================================
s = content_slide("Для кого продукт", "Аудитория и сценарии")
personas = [
    ("🎓", "Студент", "«Хочу дожить до стипендии»", ["Контроль мелких трат", "Быстрый ввод между парами"], "Обед и проезд → остаток на неделю."),
    ("💼", "Молодой специалист", "«Куда уходит зарплата?»", ["Аренда, продукты, досуг", "Топ категорий за месяц"], "Зарплата → анализ структуры → корректировка."),
    ("🏠", "Семья", "«Нужен общий отчёт»", ["Крупные покупки", "Выгрузка в Excel"], "Месяц → CSV → обсуждение бюджета."),
]
for i, (icon, name, quote_text, bullets, scenario) in enumerate(personas):
    x = 0.65 + i * 4.1
    add_card(s, x, 1.6, 3.85, 4.35)
    add_icon(s, x + 0.25, 1.82, 0.55, icon)
    add_text(s, name, x + 0.95, 1.88, 2.7, 0.3, size=15, bold=True, color=TEXT)
    add_text(s, quote_text, x + 0.25, 2.52, 3.35, 0.55, size=11, italic=True, color=BRAND)
    add_bullets(s, bullets, x + 0.25, 3.2, 3.35, size=10.5, gap=0.4)
    add_shape(s, MSO_SHAPE.ROUNDED_RECTANGLE, x + 0.2, 4.55, 3.45, 1.15, SURFACE, None, 0.08)
    add_text(s, "СЦЕНАРИЙ", x + 0.38, 4.66, 2.8, 0.2, size=7.5, bold=True, color=MUTED)
    add_text(s, scenario, x + 0.38, 4.88, 3.05, 0.7, size=9.5, color=TEXT)

add_text(
    s,
    "Аналоги: CoinKeeper, Money Lover, Дзен-мани — мощно, но платно, сложно или требует онлайн-банка. "
    "Наша ниша — лёгкий офлайн-учёт на русском.",
    0.65,
    6.22,
    12.0,
    0.55,
    size=10.5,
    color=MUTED,
)
speaker_notes(
    s,
    "Аналитик выделил три сегмента. Для всех ключевых сценариев важны скорость ввода и понятная структура, "
    "а не интеграция с банком. Поэтому v1.0 сознательно офлайн.",
)

# =====================================================================
# 4. FEATURES
# =====================================================================
s = content_slide("Ключевые возможности", "Что умеет приложение")
features = [
    ("⚡", "Быстрый ввод", "Сумма, тип, категория, дата, заметка и теги — на одном экране."),
    ("🗂", "Категории и теги", "15 стартовых категорий, свои цвета и эмодзи, до 6 тегов."),
    ("📊", "Графики", "Кольцевая структура расходов и динамика по дням или месяцам."),
    ("🔎", "Поиск и фильтры", "По заметке, тегу и категории; фильтры по типу и категории."),
    ("🔔", "Напоминания", "Ежедневное уведомление в выбранное время через WorkManager."),
    ("📤", "Экспорт", "CSV для Excel и JSON-резервная копия через системное «Поделиться»."),
]
for i, (icon, title, desc) in enumerate(features):
    col, row = i % 3, i // 3
    x = 0.65 + col * 4.08
    y = 1.58 + row * 2.28
    add_card(s, x, y, 3.82, 2.02)
    add_icon(s, x + 0.25, y + 0.24, 0.55, icon)
    add_text(s, title, x + 0.95, y + 0.3, 2.6, 0.3, size=14, bold=True, color=TEXT)
    add_text(s, desc, x + 0.25, y + 1.0, 3.3, 0.85, size=10.5, color=MUTED, line_spacing=1.1)

add_text(
    s,
    "Все данные хранятся в локальной Room-базе. Приложение работает без интернета и не требует аккаунта.",
    0.65,
    6.35,
    12.0,
    0.35,
    size=11.5,
    color=BRAND,
    bold=True,
)
speaker_notes(
    s,
    "Шесть возможностей закрывают полный цикл: записать, найти, понять, выгрузить и не забыть. "
    "Ничего лишнего для MVP.",
)

# =====================================================================
# 5. INTERFACE
# =====================================================================
s = content_slide("Интерфейс", "Три главных экрана")
positions = [1.35, 5.45, 9.55]
captions = [
    ("01 · Главная", "Баланс, структура расходов и динамика"),
    ("02 · Операции", "Поиск, фильтры и итоги по дням"),
    ("03 · Аналитика", "Топ категорий и инсайт по периоду"),
]
for x, (title, subtitle) in zip(positions, captions):
    phone(s, "mockup-dashboard.png" if title.startswith("01") else ("mockup-transactions.png" if title.startswith("02") else "mockup-stats.png"), x, 1.38, 5.12)
    add_text(s, title, x, 6.66, 2.5, 0.25, size=10, bold=True, color=BRAND)
    add_text(s, subtitle, x, 6.86, 2.5, 0.3, size=8.5, color=MUTED)

add_text(
    s,
    "Material 3 · светлая и тёмная тема",
    9.5,
    1.2,
    3.4,
    0.25,
    size=8.5,
    color=MUTED,
    align=PP_ALIGN.RIGHT,
)
speaker_notes(
    s,
    "Слева направо: главная, операции, аналитика. На главной — единственный насыщенный блок с балансом, "
    "дальше спокойные карточки с графиками. Все экраны помещаются на типичный телефон без горизонтального скролла.",
)

# =====================================================================
# 6. ARCHITECTURE
# =====================================================================
s = content_slide("Архитектура", "MVVM + Repository")
layers = [
    ("UI", "Activities · Fragments · Material 3", "ViewBinding, RecyclerView, кастомные View"),
    ("ViewModel", "StateFlow · фильтры · выбранный период", "переживает поворот экрана"),
    ("Repository", "бизнес-правила · теги · сиды · экспорт", "Result<T> для ошибок"),
    ("Room", "SQLite · DAO · агрегации", "Flow, @Transaction, FK, индексы"),
]
for i, (name, subtitle, detail) in enumerate(layers):
    y = 1.55 + i * 1.16
    fill = MINT if i in (0, 3) else WHITE
    add_card(s, 0.65, y, 5.7, 0.92, fill)
    add_text(s, name, 0.88, y + 0.13, 1.55, 0.3, size=14, bold=True, color=BRAND)
    add_text(s, subtitle, 2.35, y + 0.14, 3.8, 0.28, size=10.5, bold=True, color=TEXT)
    add_text(s, detail, 2.35, y + 0.45, 3.8, 0.28, size=9, color=MUTED)
    if i < 3:
        add_arrow_down(s, 3.35, y + 0.94, 0.22, 0.2)

add_card(s, 6.75, 1.55, 5.95, 4.65)
add_text(s, "Фон и инфраструктура", 7.0, 1.72, 5.4, 0.3, size=15, bold=True, color=TEXT)
ops = [
    ("⏰", "WorkManager", "напоминание 24 ч · снимок 6 ч · unique work"),
    ("📤", "CSV / JSON", "FileProvider + ACTION_SEND, UTF-8 BOM"),
    ("⚙️", "SharedPreferences", "настройки в StateFlow"),
    ("🧵", "Coroutines", "Room на IO, агрегации на Default"),
    ("✅", "CI", "GitHub Actions: сборка + unit-тесты"),
]
for i, (icon, title, desc) in enumerate(ops):
    y = 2.2 + i * 0.72
    add_icon(s, 7.0, y, 0.38, icon)
    add_text(s, title, 7.5, y - 0.01, 1.55, 0.25, size=10.5, bold=True, color=TEXT)
    add_text(s, desc, 9.05, y - 0.01, 3.4, 0.35, size=8.8, color=MUTED, line_spacing=0.95)

add_text(s, "Данные", 0.65, 6.26, 1.0, 0.25, size=8.5, bold=True, color=MUTED)
tech = ["Kotlin", "Room", "WorkManager", "Material 3", "Coroutines", "JUnit"]
x = 1.55
for tech_name in tech:
    width = 0.55 + len(tech_name) * 0.075
    add_pill(s, x, 6.2, width, 0.34, tech_name, SURFACE, TEXT, 9, False)
    x += width + 0.12

speaker_notes(
    s,
    "Классический MVVM: UI не знает про базу, ViewModel отдаёт StateFlow, Repository инкапсулирует Room. "
    "Тяжёлые агрегации уходят в SQL и Dispatchers.Default. WorkManager и экспорт живут сбоку и не блокируют UI.",
)

# =====================================================================
# 7. DATA MODEL
# =====================================================================
s = content_slide("Модель данных", "Room · SQLite")
add_card(s, 0.65, 1.55, 3.55, 3.5)
add_text(s, "categories", 0.9, 1.72, 2.8, 0.3, size=13, bold=True, color=BRAND, font=MONO)
for i, line_text in enumerate(["id  PK", "name  · emoji · color", "kind  EXPENSE / INCOME", "isDefault · sortOrder"]):
    add_text(s, line_text, 0.9, 2.2 + i * 0.42, 3.0, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "15 стартовых категорий", 0.9, 4.45, 3.0, 0.25, size=8.5, color=MUTED)

add_card(s, 4.75, 1.55, 3.9, 3.5)
add_text(s, "transactions", 5.0, 1.72, 3.3, 0.3, size=13, bold=True, color=BRAND, font=MONO)
for i, line_text in enumerate(["id  PK", "amountMinor  Long", "type  EXPENSE / INCOME", "categoryId  FK", "note · date · createdAt"]):
    add_text(s, line_text, 5.0, 2.2 + i * 0.42, 3.3, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "индексы: date, type, categoryId", 5.0, 4.45, 3.3, 0.25, size=8.5, color=MUTED)

add_card(s, 9.35, 1.55, 3.35, 3.5)
add_text(s, "tags", 9.6, 1.72, 2.8, 0.3, size=13, bold=True, color=BRAND, font=MONO)
add_text(s, "id  PK", 9.6, 2.2, 2.6, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "name  UNIQUE NOCASE", 9.6, 2.62, 2.8, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "transaction_tags", 9.6, 3.15, 2.8, 0.3, size=12, bold=True, color=BRAND, font=MONO)
add_text(s, "transactionId  PK,FK", 9.6, 3.55, 2.8, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "tagId  PK,FK", 9.6, 3.92, 2.8, 0.3, size=9.5, color=TEXT, font=MONO)
add_text(s, "M2M, CASCADE", 9.6, 4.45, 2.8, 0.25, size=8.5, color=MUTED)

add_arrow_right(s, 4.2, 2.45, 0.5, 0.22)
add_text(s, "1 : N", 4.25, 2.12, 0.5, 0.25, size=8, bold=True, color=BRAND)
add_arrow_right(s, 8.65, 3.35, 0.62, 0.22)
add_text(s, "N : M", 8.72, 3.02, 0.6, 0.25, size=8, bold=True, color=BRAND)

add_card(s, 0.65, 5.25, 12.05, 1.55)
ideas = [
    ("Деньги — Long в копейках", "никаких ошибок Double"),
    ("Дата — epoch millis", "BETWEEN и strftime по дням"),
    ("FK RESTRICT", "нельзя удалить категорию с операциями"),
]
for i, (title, desc) in enumerate(ideas):
    x = 0.9 + i * 3.95
    add_text(s, title, x, 5.45, 3.55, 0.28, size=11, bold=True, color=TEXT)
    add_text(s, desc, x, 5.78, 3.55, 0.45, size=9, color=MUTED)
    if i < 2:
        add_shape(s, MSO_SHAPE.RECTANGLE, x + 3.75, 5.45, 0.008, 0.75, BORDER, None)

speaker_notes(
    s,
    "Четыре таблицы. Ключевые решения: суммы в копейках, даты в epoch millis, теги через many-to-many, "
    "а внешний ключ с RESTRICT защищает категории от случайного удаления.",
)

# =====================================================================
# 8. BACKGROUND
# =====================================================================
s = content_slide("Фоновая работа и данные", "WorkManager · экспорт")
cards = [
    ("⏰", "20:00", "Ежедневное напоминание", "PeriodicWorkRequest 24 ч, initial delay до выбранного времени. Уникальная работа — без дублей.", MINT),
    ("☁️", "6 ч", "Автосохранение", "Корутина формирует JSON-снимок в filesDir/backups. Сеть не нужна — имитация облака.", WHITE),
    ("📤", "По запросу", "Экспорт", "CSV для Excel (UTF-8 BOM, ;) и JSON-бэкап. FileProvider + системное «Поделиться».", WHITE),
]
for i, (icon, value, title, desc, fill) in enumerate(cards):
    x = 0.65 + i * 4.1
    add_card(s, x, 1.55, 3.85, 3.25, fill)
    add_icon(s, x + 0.25, 1.75, 0.55, icon, WHITE if fill == MINT else MINT)
    add_text(s, value, x + 0.25, 2.48, 3.3, 0.5, size=26, bold=True, color=BRAND)
    add_text(s, title, x + 0.25, 3.05, 3.3, 0.3, size=13, bold=True, color=TEXT)
    add_text(s, desc, x + 0.25, 3.38, 3.3, 1.1, size=9.5, color=MUTED, line_spacing=1.1)

add_card(s, 0.65, 5.1, 12.05, 1.55)
add_text(s, "Почему WorkManager", 0.9, 5.27, 3.0, 0.3, size=12, bold=True, color=TEXT)
add_bullets(
    s,
    [
        "Переживает перезагрузку и смерть процесса",
        "Учитывает constraints (например, сеть) и экономит батарею",
        "Гарантирует одну уникальную задачу на тип",
    ],
    4.1,
    5.27,
    8.4,
    size=9.5,
    gap=0.36,
)
add_text(
    s,
    "Сервера в проекте нет: «синхронизация» — локальный снимок, который легко заменить на реальный API.",
    0.65,
    6.8,
    12.0,
    0.25,
    size=8.5,
    color=MUTED,
)
speaker_notes(
    s,
    "Напоминание — периодическая задача на 24 часа с задержкой до выбранного времени. Автосохранение — раз в 6 часов "
    "и только при наличии сети, хотя сеть не используется: это демонстрация constraints. Экспорт запускается вручную.",
)

# =====================================================================
# 9. DESIGN
# =====================================================================
s = content_slide("Дизайн-система", "Спокойные деньги")
add_text(s, "ПАЛИТРА", 0.65, 1.5, 2.0, 0.25, size=8.5, bold=True, color=MUTED)
palette = [
    ("#0E7C66", "бренд", BRAND),
    ("#D6F2EA", "контейнер", MINT),
    ("#101828", "текст", TEXT),
    ("#667085", "вторичный", MUTED),
    ("#12A150", "доход", INCOME),
    ("#E5484D", "расход", EXPENSE),
]
for i, (hex_value, label, color) in enumerate(palette):
    x = 0.65 + i * 0.93
    add_shape(s, MSO_SHAPE.ROUNDED_RECTANGLE, x, 1.78, 0.82, 0.55, color, None, 0.12)
    add_text(s, hex_value, x, 2.4, 0.88, 0.2, size=6.8, bold=True, color=TEXT, font=MONO)
    add_text(s, label, x, 2.6, 0.88, 0.2, size=7.2, color=MUTED)

add_text(s, "ТИПОГРАФИКА", 0.65, 3.0, 2.5, 0.25, size=8.5, bold=True, color=MUTED)
add_text(s, "Мои финансы", 0.65, 3.28, 5.0, 0.5, size=25, bold=True, color=TEXT)
add_text(s, "54 790 ₽   ·   баланс за период", 0.65, 3.82, 5.0, 0.35, size=13, color=BRAND)
add_text(s, "Системный шрифт · крупные суммы · табличные цифры", 0.65, 4.17, 5.4, 0.25, size=8.5, color=MUTED)

add_text(s, "ПРИНЦИПЫ", 0.65, 4.65, 2.5, 0.25, size=8.5, bold=True, color=MUTED)
add_bullets(
    s,
    [
        "Один экран — одна задача",
        "Цвет означает доход или расход",
        "Мягкие карточки, много воздуха",
        "Понятные пустые состояния",
    ],
    0.65,
    4.95,
    5.7,
    size=10,
    gap=0.38,
)

# Donut example
add_card(s, 6.35, 1.55, 2.65, 2.35)
add_shape(s, MSO_SHAPE.DONUT, 6.55, 1.72, 1.55, 1.55, BRAND, None)
add_shape(s, MSO_SHAPE.OVAL, 6.98, 2.15, 0.7, 0.7, WHITE, None)
add_text(s, "30 210 ₽", 6.62, 3.35, 2.1, 0.3, size=12, bold=True, color=TEXT, align=PP_ALIGN.CENTER)
add_text(s, "структура расходов", 6.62, 3.62, 2.1, 0.2, size=7.5, color=MUTED, align=PP_ALIGN.CENTER)

add_card(s, 9.25, 1.22, 3.5, 5.55, WHITE, BORDER, 0.04)
phone(s, "mockup-editor.png", 9.78, 1.45, 5.1, border=False)

add_text(
    s,
    "Тёмная тема — отдельная палитра, а не инверсия: фон #0B1220, бренд светлее, контраст сохранён.",
    0.65,
    6.5,
    8.3,
    0.3,
    size=9,
    color=MUTED,
)
speaker_notes(
    s,
    "Дизайн опирается на три принципа: спокойный фон, один яркий блок баланса и цвет как смысл. "
    "Палитра и типографика вынесены в дизайн-систему, тёмная тема проработана отдельно.",
)

# =====================================================================
# 10. TESTING / CI
# =====================================================================
s = content_slide("Проверка качества", "Тесты · сборка · CI")
add_card(s, 0.65, 1.55, 6.0, 2.55, BRAND_DARK, None)
add_text(s, "BUILD SUCCESSFUL", 0.95, 1.78, 5.2, 0.4, size=19, bold=True, color=WHITE, font=MONO)
add_text(s, "assembleDebug · 13.11.2026", 0.95, 2.22, 4.5, 0.3, size=9.5, color=MINT_TEXT)
add_text(s, "15 / 15", 0.95, 2.72, 2.2, 0.55, size=27, bold=True, color=WHITE)
add_text(s, "unit-тестов зелёные", 0.95, 3.28, 2.4, 0.25, size=8.5, color=MINT_TEXT)
add_text(s, "7,1 МБ", 3.45, 2.72, 2.2, 0.55, size=27, bold=True, color=WHITE)
add_text(s, "debug APK", 3.45, 3.28, 2.0, 0.25, size=8.5, color=MINT_TEXT)

add_card(s, 6.95, 1.55, 5.75, 2.55)
add_text(s, "CI на GitHub Actions", 7.2, 1.72, 4.5, 0.3, size=13, bold=True, color=TEXT)
steps = ["checkout", "JDK 17", "gradle build + test", "artifact APK"]
for i, step in enumerate(steps):
    y = 2.15 + i * 0.44
    circle = add_shape(s, MSO_SHAPE.OVAL, 7.2, y, 0.28, 0.28, MINT, None)
    shape_text(circle, str(i + 1), size=8, bold=True, color=BRAND)
    add_text(s, step, 7.62, y + 0.03, 4.6, 0.25, size=10, color=TEXT, font=MONO)

add_text(s, "Что покрыто тестами", 0.65, 4.4, 5.5, 0.3, size=13, bold=True, color=TEXT)
tests = [
    ("MoneyTest · 5", "формат, парсинг, границы суммы"),
    ("SummaryBuilderTest · 6", "итоги, проценты, дни и месяцы, пустые данные"),
    ("TransactionListBuilderTest · 4", "группировка, поиск, фильтры"),
]
for i, (name, desc) in enumerate(tests):
    y = 4.82 + i * 0.55
    add_card(s, 0.65, y, 11.95, 0.46, WHITE, BORDER, 0.12)
    add_text(s, name, 0.85, y + 0.12, 3.1, 0.25, size=9.5, bold=True, color=BRAND, font=MONO)
    add_text(s, desc, 4.0, y + 0.12, 8.3, 0.25, size=9.5, color=MUTED)

add_text(
    s,
    "Ручной чек-лист — 16 сценариев (docs/TEST_PLAN.md): ввод, фильтры, категории, экспорт, темы, поворот.",
    0.65,
    6.6,
    12.0,
    0.3,
    size=8.5,
    color=MUTED,
)
speaker_notes(
    s,
    "Сборка проходит, 15 unit-тестов зелёные. CI на каждом push собирает APK и гоняет тесты. "
    "UI-тесты не автоматизированы: есть ручной чек-лист из 16 сценариев.",
)

# =====================================================================
# 11. RESULTS / ROADMAP
# =====================================================================
s = content_slide("Результат и развитие", "Итоги MVP")
add_text(s, "ГОТОВО В ВЕРСИИ 1.0", 0.65, 1.52, 5.5, 0.25, size=8.5, bold=True, color=MUTED)
done = [
    "Полный цикл учёта: доходы, расходы, категории, теги",
    "Главная с балансом, donut и динамикой",
    "Поиск, фильтры и итоги по дням",
    "Аналитика: топ категорий, средний чек, инсайт",
    "Room + репозиторий, 15 unit-тестов",
    "WorkManager, CSV/JSON, тёмная тема, CI",
]
for i, item in enumerate(done):
    y = 1.85 + i * 0.55
    check = add_shape(s, MSO_SHAPE.OVAL, 0.65, y, 0.32, 0.32, MINT, None)
    shape_text(check, "✓", size=10, bold=True, color=BRAND)
    add_text(s, item, 1.12, y + 0.04, 6.1, 0.3, size=11, color=TEXT)

add_card(s, 7.55, 1.5, 5.15, 4.55)
add_text(s, "Дальше", 7.82, 1.68, 4.5, 0.3, size=15, bold=True, color=TEXT)
roadmap = [
    ("☁️", "Облачная синхронизация", "Retrofit + backend, аккаунт и восстановление"),
    ("🎯", "Бюджеты и лимиты", "прогресс по категориям и предупреждения"),
    ("📈", "Больше аналитики", "сравнение периодов, средний чек, прогноз"),
    ("📱", "Виджет быстрого ввода", "добавление траты с домашнего экрана"),
]
for i, (icon, title, desc) in enumerate(roadmap):
    y = 2.15 + i * 0.9
    add_icon(s, 7.85, y, 0.42, icon)
    add_text(s, title, 8.42, y - 0.02, 3.9, 0.3, size=11, bold=True, color=TEXT)
    add_text(s, desc, 8.42, y + 0.28, 3.9, 0.4, size=8.5, color=MUTED, line_spacing=1.0)

add_text(
    s,
    "Приоритет: синхронизация → бюджеты → аналитика.",
    7.82,
    5.55,
    4.6,
    0.25,
    size=8,
    color=MUTED,
)

add_card(s, 0.65, 5.35, 6.55, 1.15, MINT, None)
add_text(s, "Команда", 0.9, 5.5, 1.8, 0.25, size=8, bold=True, color=BRAND)
add_text(
    s,
    "Штефан — лид · Кислов — данные · Ибрагимов — аналитика · Давлетханов — дизайн",
    0.9,
    5.75,
    6.05,
    0.5,
    size=9.5,
    color=TEXT,
)

speaker_notes(
    s,
    "MVP закрывает основной сценарий. Дальше — облако, бюджеты и расширенная аналитика. "
    "Проект уже собирается в CI и готов к защите.",
)

# =====================================================================
# 12. THANKS
# =====================================================================
s = prs.slides.add_slide(BLANK)
gradient_background(s, BRAND_DARK, BRAND_LIGHT)
add_shape(s, MSO_SHAPE.OVAL, 9.5, -1.2, 6.5, 6.5, RGBColor.from_string("0F6B59"), None)
add_text(s, "Спасибо!", 0.8, 1.7, 8.0, 0.95, size=54, bold=True, color=WHITE)
add_text(s, "Вопросы и обсуждение", 0.85, 2.8, 7.0, 0.5, size=24, color=MINT_TEXT)
add_text(
    s,
    "Исходный код, документация и презентация — в репозитории проекта.\n"
    "Приложение собирается командой gradlew assembleDebug и работает офлайн.",
    0.85,
    3.75,
    7.3,
    0.85,
    size=13,
    color=WHITE,
    line_spacing=1.2,
)
add_text(s, "ФинТрекер · 2026", 0.85, 6.75, 4.0, 0.3, size=10, color=MINT_TEXT)
add_text(
    s,
    "Штефан  ·  Кислов  ·  Ибрагимов  ·  Давлетханов",
    4.0,
    6.75,
    8.6,
    0.3,
    size=10,
    color=MINT_TEXT,
    align=PP_ALIGN.RIGHT,
)
speaker_notes(s, "Спасибо за внимание. Готовы ответить на вопросы по архитектуре, данным и дизайну.")

prs.save(OUT)
print("saved", OUT, "slides:", len(prs.slides))
