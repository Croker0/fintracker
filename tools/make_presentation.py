"""FinTracker defense deck, student style: 11 slides, 16:9."""

from __future__ import annotations

from datetime import datetime
from pathlib import Path
import os
import re
import zipfile

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.oxml.ns import qn
from pptx.util import Inches, Pt

ROOT = Path(__file__).resolve().parents[1]
IMAGES = ROOT / "docs" / "images"
OUT = ROOT / "presentation" / "FinTracker.pptx"
OUT.parent.mkdir(parents=True, exist_ok=True)

# --- palette: paper, ink, one green, one ochre ---
PAPER = RGBColor.from_string("F7F5EF")
INK = RGBColor.from_string("1F2522")
GREEN = RGBColor.from_string("235B4A")
GREEN_PALE = RGBColor.from_string("DDE8E0")
GRAY = RGBColor.from_string("6E7168")
LIGHT_GRAY = RGBColor.from_string("A9ABA2")
LINE = RGBColor.from_string("D5D0C2")
OCHRE = RGBColor.from_string("B47B2A")
DARK = RGBColor.from_string("1D2A24")
WHITE = RGBColor.from_string("FFFFFF")
CODE = RGBColor.from_string("E8E4D6")

SERIF = "Georgia"
SANS = "Calibri"
MONO = "Consolas"

W, H = 13.333, 7.5
prs = Presentation()
prs.slide_width = Inches(W)
prs.slide_height = Inches(H)
BLANK = prs.slide_layouts[6]

_page = 2


def _strip_style(shape):
    """Remove the theme style so LibreOffice/PowerPoint do not add shadows."""
    sp = shape._element
    for style in sp.findall(qn("p:style")):
        sp.remove(style)
    shape.shadow.inherit = False


def rect(slide, x, y, w, h, fill=None, line=None, line_w=0.75, radius=None):
    kind = MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE
    shape = slide.shapes.add_shape(kind, Inches(x), Inches(y), Inches(w), Inches(h))
    if fill is None:
        shape.fill.background()
    else:
        shape.fill.solid()
        shape.fill.fore_color.rgb = fill
    if line is None:
        shape.line.fill.background()
    else:
        shape.line.color.rgb = line
        shape.line.width = Pt(line_w)
    if radius and shape.adjustments:
        shape.adjustments[0] = radius
    _strip_style(shape)
    return shape


def line(slide, x, y, w, color=LINE, thickness=0.008):
    return rect(slide, x, y, w, thickness, fill=color)


def arrow(slide, kind, x, y, w, h, color=GREEN):
    shape = slide.shapes.add_shape(kind, Inches(x), Inches(y), Inches(w), Inches(h))
    shape.fill.solid()
    shape.fill.fore_color.rgb = color
    shape.line.fill.background()
    _strip_style(shape)
    return shape


def vline(slide, x, y, h, color=LINE, thickness=0.008):
    return rect(slide, x, y, thickness, h, fill=color)


def add_text(
    slide,
    value,
    x,
    y,
    w,
    h,
    size=14,
    color=INK,
    font=SANS,
    bold=False,
    italic=False,
    align=PP_ALIGN.LEFT,
    anchor=MSO_ANCHOR.TOP,
    line_spacing=1.0,
    space_after=0,
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
    p.space_after = Pt(space_after)
    run = p.add_run()
    run.text = value
    run.font.size = Pt(size)
    run.font.name = font
    run.font.bold = bold
    run.font.italic = italic
    run.font.color.rgb = color
    return box


def rich(slide, parts, x, y, w, h, size=14, align=PP_ALIGN.LEFT, line_spacing=1.0):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.clear()
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    p = tf.paragraphs[0]
    p.alignment = align
    p.line_spacing = line_spacing
    for value, opts in parts:
        run = p.add_run()
        run.text = value
        run.font.size = Pt(opts.get("size", size))
        run.font.name = opts.get("font", SANS)
        run.font.bold = opts.get("bold", False)
        run.font.italic = opts.get("italic", False)
        run.font.color.rgb = opts.get("color", INK)
    return box


def bullet(slide, x, y, w, text_value, size=12.5, color=INK, gap=0.0):
    add_text(slide, "—", x, y, 0.28, 0.3, size=size, color=GREEN, bold=False)
    add_text(slide, text_value, x + 0.32, y, w - 0.32, 0.55, size=size, color=color, line_spacing=1.12)


def frame_picture(slide, path, x, y, h, border=True):
    w = h * 720 / 1560
    if border:
        rect(slide, x - 0.05, y - 0.05, w + 0.1, h + 0.1, WHITE, LINE, 0.6)
    slide.shapes.add_picture(str(path), Inches(x), Inches(y), Inches(w), Inches(h))
    return w


def header(section, title):
    global _page
    slide = prs.slides.add_slide(BLANK)
    rect(slide, 0, 0, W, H, PAPER)
    vline(slide, 0.52, 0.55, 6.45, GREEN, 0.014)
    add_text(slide, section.upper(), 0.75, 0.43, 6.0, 0.25, size=9.5, color=GREEN, bold=True)
    add_text(slide, title, 0.72, 0.68, 11.8, 0.65, size=29, color=INK, font=SERIF)
    line(slide, 0.75, 1.42, 11.85, LINE, 0.009)
    add_text(slide, "ФинТрекер · ноябрь 2026", 0.75, 7.12, 4.5, 0.22, size=8, color=LIGHT_GRAY)
    add_text(slide, f"{_page:02d}", 12.1, 7.12, 0.5, 0.22, size=8, color=LIGHT_GRAY, align=PP_ALIGN.RIGHT)
    _page += 1
    return slide


def notes(slide, text_value):
    slide.notes_slide.notes_text_frame.text = text_value


# =====================================================================
# 1. TITLE
# =====================================================================
s = prs.slides.add_slide(BLANK)
rect(s, 0, 0, W, H, PAPER)
rect(s, 9.05, 0, 4.283, H, GREEN_PALE)
frame_picture(s, IMAGES / "mockup-dashboard.png", 9.62, 0.55, 6.3, border=False)

add_text(s, "УЧЕБНЫЙ ПРОЕКТ  ·  ANDROID", 0.72, 0.72, 7.0, 0.25, size=9.5, color=GREEN, bold=True)
add_text(s, "ФинТрекер", 0.66, 1.1, 8.2, 1.05, size=58, color=INK, font=SERIF)
add_text(
    s,
    "Трекер личных финансов\nс категориями и графиками",
    0.72,
    2.28,
    7.4,
    1.0,
    size=20,
    color=GREEN,
    line_spacing=1.12,
)
line(s, 0.75, 3.38, 1.4, GREEN, 0.018)
add_text(
    s,
    "Записывать траты быстро, а потом видеть, куда ушёл месяц.\nБез аккаунта и без интернета.",
    0.75,
    3.68,
    7.0,
    0.85,
    size=12.5,
    color=GRAY,
    line_spacing=1.2,
)

add_text(s, "КОМАНДА", 0.75, 5.28, 2.5, 0.22, size=8, color=LIGHT_GRAY, bold=True)
add_text(s, "Штефан  ·  Кислов  ·  Ибрагимов  ·  Давлетханов", 0.75, 5.55, 7.4, 0.3, size=12, color=INK, bold=True)
add_text(
    s,
    "лид и UI  ·  данные и WorkManager  ·  требования  ·  дизайн",
    0.75,
    5.9,
    7.5,
    0.35,
    size=9.5,
    color=GRAY,
)
add_text(s, "Ноябрь 2026", 0.75, 6.78, 3.0, 0.25, size=9.5, color=GRAY)
notes(
    s,
    "Здравствуйте. Мы делали приложение вчетвером: я отвечал за общую сборку и экраны, "
    "Кислов — за базу и фоновые задачи, Ибрагимов — за требования и проверку, Давлетханов — за макеты. "
    "Сразу скажу: это учебный проект, без сервера, всё хранится на телефоне.",
)

# =====================================================================
# 2. TASK
# =====================================================================
s = header("01 / задача", "Что нужно было сделать")
add_text(
    s,
    "Android-приложение, в котором человек записывает доходы и расходы и видит, куда уходят деньги.",
    0.75,
    1.62,
    6.8,
    0.9,
    size=15.5,
    color=INK,
    line_spacing=1.16,
)
bullet(s, 0.75, 2.85, 6.2, "быстрый ввод: сумма, категория, дата и комментарий;", 12.5)
bullet(s, 0.75, 3.43, 6.2, "периоды: неделя, месяц, год и всё время;", 12.5)
bullet(s, 0.75, 4.01, 6.2, "отчёт, который открывается в Excel, и графики по категориям.", 12.5)
add_text(
    s,
    "Ограничения: четыре человека, четыре недели, без сервера.\nДанные не должны покидать телефон.",
    0.75,
    4.85,
    6.4,
    0.7,
    size=11,
    color=GRAY,
    line_spacing=1.2,
)

vline(s, 7.75, 1.62, 1.7, GREEN, 0.02)
add_text(
    s,
    "«Трекер личных финансов\nс категоризацией и графиками»",
    8.0,
    1.62,
    4.55,
    1.1,
    size=18,
    color=INK,
    font=SERIF,
    italic=True,
    line_spacing=1.18,
)
add_text(s, "формулировка из задания", 8.0, 3.0, 3.5, 0.25, size=9, color=GRAY, italic=True)
add_text(
    s,
    "Мы сразу отказались от банковских интеграций и аккаунта: для учебного проекта это лишняя "
    "инфраструктура, а основной сценарий всё равно ручной ввод.",
    8.0,
    3.65,
    4.55,
    1.3,
    size=11,
    color=GRAY,
    line_spacing=1.22,
)
notes(
    s,
    "В задании была именно такая формулировка. Мы решили не распыляться: никаких банков, валют и облака. "
    "Главное — чтобы запись траты занимала секунды и чтобы через месяц можно было понять структуру расходов.",
)

# =====================================================================
# 3. SCREENS
# =====================================================================
s = header("02 / экраны", "Три экрана, с которых всё начинается")
positions = [0.7, 2.98, 5.26]
files = ["mockup-dashboard.png", "mockup-transactions.png", "mockup-stats.png"]
for x, name in zip(positions, files):
    frame_picture(s, IMAGES / name, x, 1.5, 4.55)

captions = [
    ("Главная", "баланс, структура расходов и последние операции"),
    ("Операции", "поиск, фильтры и итоги по дням"),
    ("Аналитика", "топ категорий и динамика по месяцам"),
]
for x, (title, sub) in zip(positions, captions):
    add_text(s, title, x, 6.25, 2.1, 0.25, size=10.5, color=INK, bold=True)
    add_text(s, sub, x, 6.5, 2.15, 0.45, size=8.7, color=GRAY, line_spacing=1.05)

vline(s, 7.95, 1.55, 4.6, LINE, 0.008)
add_text(s, "НА ЧТО СМОТРЕТЬ", 8.2, 1.55, 4.0, 0.22, size=8.2, color=GREEN, bold=True)
look = [
    ("На главной", "сначала видно баланс и структуру, а не длинный список операций."),
    ("В операциях", "можно искать по заметке, тегу и категории, не листая всё вручную."),
    ("В аналитике", "считается топ категорий, средний чек и график по месяцам."),
]
for i, (title, body) in enumerate(look):
    y = 1.95 + i * 1.32
    add_text(s, f"0{i + 1}", 8.2, y, 0.45, 0.3, size=12, color=OCHRE, font=SERIF, bold=True)
    add_text(s, title, 8.72, y, 3.7, 0.28, size=11, color=INK, bold=True)
    add_text(s, body, 8.72, y + 0.3, 3.75, 0.7, size=9.7, color=GRAY, line_spacing=1.18)

add_text(
    s,
    "Это макеты, по которым верстали экраны. Отступы и размеры в приложении совпадают.",
    8.2,
    6.2,
    4.35,
    0.55,
    size=8.7,
    color=LIGHT_GRAY,
    italic=True,
    line_spacing=1.12,
)
notes(
    s,
    "Показываю три основных экрана. Макеты рисовали до кода, поэтому в приложении те же отступы и порядок блоков. "
    "На главной специально нет длинного списка: сначала баланс и диаграмма, список — ниже.",
)

# =====================================================================
# 4. ARCHITECTURE
# =====================================================================
s = header("03 / архитектура", "Как это устроено внутри")
layers = [
    ("UI", "Activities, Fragments, Material 3", "ViewBinding, RecyclerView, свои View для графиков"),
    ("ViewModel", "StateFlow: фильтры и выбранный период", "переживает поворот экрана"),
    ("Repository", "правила, теги, категории, демо-данные", "операции с несколькими таблицами — в транзакции"),
    ("Room", "SQLite, DAO, агрегации", "Flow, индексы, внешние ключи"),
]
for i, (name, subtitle, detail) in enumerate(layers):
    y = 1.58 + i * 1.03
    rect(s, 0.75, y, 5.65, 0.82, PAPER, INK, 0.7)
    add_text(s, name, 0.95, y + 0.11, 1.4, 0.28, size=12.5, color=GREEN, bold=True)
    add_text(s, subtitle, 2.35, y + 0.1, 3.85, 0.28, size=10.2, color=INK, bold=True)
    add_text(s, detail, 2.35, y + 0.41, 3.85, 0.3, size=8.7, color=GRAY)
    if i < 3:
        arrow(s, MSO_SHAPE.DOWN_ARROW, 3.38, y + 0.84, 0.2, 0.17)

add_text(
    s,
    "WorkManager и экспорт стоят сбоку: они не блокируют экран и не лезут в UI-поток.",
    0.75,
    5.83,
    5.7,
    0.55,
    size=9.5,
    color=GRAY,
    italic=True,
    line_spacing=1.15,
)

vline(s, 6.85, 1.6, 4.9, LINE, 0.008)
add_text(s, "ПОЧЕМУ ТАК", 7.12, 1.55, 3.0, 0.22, size=8.2, color=GREEN, bold=True)
why = [
    ("UI не работает с базой напрямую.", "Так проще менять экраны и не тащить SQL во фрагменты."),
    ("Repository — одно место для правил.", "Категории, теги и проверки не размазаны по ViewModel."),
    ("Тяжёлое — в SQL и Dispatchers.Default.", "Список не ждёт, пока посчитаются проценты и точки графика."),
]
for i, (head, body) in enumerate(why):
    y = 1.95 + i * 1.12
    add_text(s, head, 7.12, y, 5.1, 0.28, size=11, color=INK, bold=True)
    add_text(s, body, 7.12, y + 0.3, 5.1, 0.55, size=9.7, color=GRAY, line_spacing=1.16)

add_text(
    s,
    "transactionsFlow  →  summaryFlow  →  UI",
    7.12,
    5.6,
    5.0,
    0.3,
    size=9.5,
    color=GREEN,
    font=MONO,
)
notes(
    s,
    "Схема обычная для Android: UI, ViewModel, репозиторий, Room. Важно, что экраны подписаны на StateFlow, "
    "а не тянут данные из DAO. Тяжёлые агрегации уходят в SQL и на Default-диспетчер, чтобы список не дёргался.",
)

# =====================================================================
# 5. DATA
# =====================================================================
s = header("04 / данные", "Четыре таблицы и один запрос")
# ER row
rect(s, 0.75, 1.55, 2.85, 1.75, PAPER, INK, 0.7)
add_text(s, "categories", 0.92, 1.67, 2.5, 0.25, size=11, color=GREEN, font=MONO, bold=True)
for i, t in enumerate(["id  PK", "name · kind", "emoji · color"]):
    add_text(s, t, 0.92, 2.0 + i * 0.33, 2.45, 0.25, size=8.3, color=INK, font=MONO)

rect(s, 4.35, 1.55, 3.45, 1.75, PAPER, INK, 0.7)
add_text(s, "transactions", 4.52, 1.67, 3.0, 0.25, size=11, color=GREEN, font=MONO, bold=True)
for i, t in enumerate(["id  PK", "amountMinor  Long", "type · categoryId", "note · date"]):
    add_text(s, t, 4.52, 1.98 + i * 0.3, 3.0, 0.25, size=8.3, color=INK, font=MONO)

rect(s, 8.6, 1.55, 2.35, 1.75, PAPER, INK, 0.7)
add_text(s, "tags", 8.77, 1.67, 2.0, 0.25, size=11, color=GREEN, font=MONO, bold=True)
for i, t in enumerate(["id  PK", "name  UNIQUE", "transaction_tags"]):
    add_text(s, t, 8.77, 1.98 + i * 0.3, 2.0, 0.25, size=8.3, color=INK, font=MONO)

arrow(s, MSO_SHAPE.RIGHT_ARROW, 3.68, 2.27, 0.55, 0.18)
add_text(s, "1 : N", 3.66, 1.9, 0.7, 0.22, size=7.8, color=GREEN, bold=True)
arrow(s, MSO_SHAPE.RIGHT_ARROW, 7.92, 2.27, 0.58, 0.18)
add_text(s, "N : M", 7.88, 1.9, 0.7, 0.22, size=7.8, color=GREEN, bold=True)

# SQL block
rect(s, 0.75, 3.7, 7.85, 2.55, DARK, None)
sql = [
    "SELECT c.name, SUM(t.amountMinor) AS total",
    "FROM transactions t",
    "JOIN categories c ON c.id = t.categoryId",
    "WHERE t.type = 'EXPENSE'",
    "  AND t.date BETWEEN :from AND :to",
    "GROUP BY c.id",
    "ORDER BY total DESC;",
]
for i, row in enumerate(sql):
    add_text(s, row, 0.98, 3.88 + i * 0.31, 7.35, 0.26, size=9.2, color=CODE, font=MONO)
add_text(s, "один запрос на топ категорий", 0.98, 5.98, 4.5, 0.2, size=7.5, color=RGBColor.from_string("8FA096"))

add_text(s, "ЧТО ВАЖНО В ДАННЫХ", 8.85, 3.7, 3.8, 0.22, size=8.2, color=GREEN, bold=True)
data_notes = [
    ("Деньги — Long в копейках.", "Никаких Double и округлений в случайную сторону."),
    ("Дата — epoch millis.", "BETWEEN и strftime по дням, без строковых дат."),
    ("Категорию с операциями не удалить.", "Внешний ключ RESTRICT, теги — CASCADE."),
]
for i, (head, body) in enumerate(data_notes):
    y = 4.02 + i * 0.73
    add_text(s, head, 8.85, y, 3.9, 0.26, size=9.7, color=INK, bold=True)
    add_text(s, body, 8.85, y + 0.24, 3.9, 0.38, size=8.5, color=GRAY, line_spacing=1.05)
notes(
    s,
    "Четыре таблицы: категории, операции, теги и связка. Суммы храним в копейках, даты — в millis. "
    "Топ категорий считает SQL, а не Kotlin: так проще и быстрее. Удалять категорию с операциями нельзя, "
    "это защита от случайной потери данных.",
)

# =====================================================================
# 6. HARD PARTS
# =====================================================================
s = header("05 / проблемы", "Что оказалось сложным")
problems = [
    (
        "Проценты не сходились",
        "Считали доли в Kotlin и получали 99,9%. Перенесли сумму в SQL, округление и остаток — в SummaryBuilder. "
        "Последняя категория добирает до 100%.",
        "проверка: сумма процентов в тесте = 100",
    ),
    (
        "Удаление операции",
        "Теги связаны many-to-many. Сначала падало на foreign key. Добавили @Transaction и CASCADE для связки, "
        "для категорий оставили RESTRICT.",
        "проверка: удаление операции с двумя тегами",
    ),
    (
        "Напоминание дублировалось",
        "После перезапуска появлялись две задачи. Заменили на enqueueUniquePeriodicWork с политикой UPDATE.",
        "проверка: после перезапуска задача одна",
    ),
]
for i, (title, body, check) in enumerate(problems):
    x = 0.75 + i * 3.92
    add_text(s, f"0{i + 1}", x, 1.6, 0.5, 0.4, size=16, color=OCHRE, font=SERIF, bold=True)
    add_text(s, title, x, 2.0, 3.35, 0.3, size=14, color=INK, bold=True)
    add_text(s, body, x, 2.42, 3.35, 1.6, size=10.2, color=GRAY, line_spacing=1.25)
    add_text(s, check, x, 3.95, 3.3, 0.3, size=8.5, color=LIGHT_GRAY, italic=True)
    if i < 2:
        vline(s, x + 3.6, 1.7, 3.4, LINE, 0.008)

line(s, 0.75, 5.35, 11.85, LINE, 0.008)
add_text(
    s,
    "Первые версии падали на удалении операции и на пустом периоде. После этого появились "
    "@Transaction, пустые состояния и тесты на агрегации.",
    0.75,
    5.6,
    11.6,
    0.75,
    size=11.5,
    color=INK,
    italic=True,
    line_spacing=1.25,
)
notes(
    s,
    "Самое неприятное было не в UI, а в данных. Проценты не сходились из-за округления, удаление операции "
    "падало на связке тегов, а напоминание после перезапуска создавалось дважды. Все три места теперь "
    "покрыты тестами или отдельными решениями.",
)

# =====================================================================
# 7. TESTING
# =====================================================================
s = header("06 / проверка", "Как проверяли")
rect(s, 0.75, 1.55, 6.55, 2.25, DARK, None)
term = [
    "> gradlew.bat :app:assembleDebug",
    "  :app:testDebugUnitTest",
    "",
    "BUILD SUCCESSFUL in 33s",
    "15 tests, 0 failures",
]
colors = [CODE, CODE, CODE, RGBColor.from_string("8FD3A8"), RGBColor.from_string("8FD3A8")]
for i, (row, color) in enumerate(zip(term, colors)):
    add_text(s, row, 0.98, 1.73 + i * 0.35, 6.0, 0.28, size=9.8, color=color, font=MONO)

add_text(s, "ЧТО ПОКРЫТО", 0.75, 4.08, 3.0, 0.22, size=8.2, color=GREEN, bold=True)
coverage = [
    "MoneyTest — формат, парсинг, границы суммы.",
    "SummaryBuilderTest — итоги, проценты, дни и месяцы.",
    "TransactionListBuilderTest — группы, поиск, фильтры.",
]
for i, row in enumerate(coverage):
    add_text(s, row, 0.75, 4.38 + i * 0.38, 6.4, 0.3, size=9.7, color=INK)

vline(s, 7.65, 1.55, 4.9, LINE, 0.008)
right_blocks = [
    ("CI", "GitHub Actions собирает APK и запускает тесты на каждый push."),
    ("Ручной чек-лист", "16 сценариев: ввод, фильтры, категории, экспорт, тёмная тема."),
    ("Ограничение", "Эмулятор на ноутбуке работал нестабильно. Автоматика — сборка и unit-тесты, UI-прогон остался ручным."),
]
for i, (head, body) in enumerate(right_blocks):
    y = 1.55 + i * 1.6
    add_text(s, head, 7.95, y, 3.0, 0.25, size=9.5, color=GREEN if i < 2 else OCHRE, bold=True)
    add_text(s, body, 7.95, y + 0.3, 4.25, 0.95, size=9.8, color=GRAY, line_spacing=1.2)
notes(
    s,
    "Проверяли в основном автотестами: 15 штук на деньги, агрегации и список. Плюс CI, который собирает APK "
    "на каждый push. Честное ограничение: эмулятор на моём ноутбуке работал нестабильно, поэтому ручной "
    "UI-прогон мы не автоматизировали, чек-лист лежит в docs/TEST_PLAN.md.",
)

# =====================================================================
# 8. EDITOR
# =====================================================================
s = header("07 / редактор", "Чтобы запись занимала меньше минуты")
frame_picture(s, IMAGES / "mockup-editor.png", 9.55, 1.4, 5.05)
add_text(s, "Экран новой операции", 9.55, 6.55, 2.5, 0.25, size=8.5, color=LIGHT_GRAY, italic=True)

items = [
    ("Сумма", "Принимает 1 450,50 и 1450.50. Ноль и минус отклоняются."),
    ("Категория", "Обязательна. Поэтому в отчётах нет строки «Без категории»."),
    ("Теги", "Через запятую, до шести. По тегам работает поиск."),
]
for i, (head, body) in enumerate(items):
    y = 1.55 + i * 1.45
    add_text(s, f"0{i + 1}", 0.78, y, 0.5, 0.35, size=15, color=OCHRE, font=SERIF, bold=True)
    add_text(s, head, 1.35, y, 3.2, 0.3, size=14, color=INK, bold=True)
    add_text(s, body, 1.35, y + 0.35, 7.0, 0.7, size=10.5, color=GRAY, line_spacing=1.2)

line(s, 0.78, 5.95, 7.8, LINE, 0.008)
add_text(
    s,
    "Дата по умолчанию — сегодня. Будущие даты запрещены, чтобы случайно не сломать период.",
    0.78,
    6.15,
    7.9,
    0.55,
    size=9.5,
    color=GRAY,
    italic=True,
    line_spacing=1.15,
)
notes(
    s,
    "Редактор — самый важный экран по времени. Сумма принимает и запятую, и точку. Без категории сохранить "
    "нельзя, иначе потом не будет нормальной аналитики. Теги необязательны, но по ним удобно искать. "
    "Будущие даты запрещены, чтобы не путать периоды.",
)

# =====================================================================
# 9. RESULTS
# =====================================================================
s = header("08 / результат", "Что получилось")
numbers = [
    ("5", "экранов"),
    ("4", "типа ячеек\nв списке"),
    ("15", "unit-тестов"),
    ("0", "сетевых\nзапросов"),
]
for i, (value, label) in enumerate(numbers):
    x = 0.75 + i * 2.85
    add_text(s, value, x, 1.55, 1.5, 0.8, size=45, color=GREEN, font=SERIF)
    add_text(s, label, x, 2.4, 2.0, 0.55, size=9.5, color=GRAY, line_spacing=1.1)

line(s, 0.75, 3.22, 11.85, LINE, 0.008)
add_text(
    s,
    "7,1 МБ   ·   Android 7.0+   ·   Room, WorkManager, Material 3",
    0.75,
    3.4,
    11.7,
    0.3,
    size=10.5,
    color=INK,
)
add_text(
    s,
    "Демо-набор: 15 категорий и 30 операций, чтобы при первом запуске не было пустого экрана.",
    0.75,
    3.82,
    11.7,
    0.35,
    size=9.8,
    color=GRAY,
)

add_text(s, "СЦЕНАРИЙ ДЕМО", 0.75, 4.65, 3.0, 0.22, size=8.2, color=GREEN, bold=True)
steps = [
    ("1", "Добавить расход 1 450 ₽ на продукты."),
    ("2", "Найти его поиском и проверить итог дня."),
    ("3", "Открыть аналитику и выгрузить CSV."),
]
for i, (num, text_value) in enumerate(steps):
    y = 5.0 + i * 0.6
    add_text(s, num, 0.78, y, 0.3, 0.25, size=10, color=OCHRE, bold=True)
    add_text(s, text_value, 1.15, y, 8.5, 0.3, size=10.5, color=INK)
add_text(
    s,
    "Эти же три шага проверяем на защите.",
    9.0,
    5.9,
    3.5,
    0.3,
    size=9,
    color=LIGHT_GRAY,
    italic=True,
)
notes(
    s,
    "Если считать по цифрам: пять экранов, четыре типа ячеек, пятнадцать тестов, ноль сетевых запросов. "
    "На демо показываем короткий сценарий: добавить расход, найти его и посмотреть аналитику. Этого достаточно, "
    "чтобы увидеть основной цикл.",
)

# =====================================================================
# 10. WHAT'S NEXT
# =====================================================================
s = header("09 / итоги", "Что не успели и что дальше")
add_text(s, "НЕ СТАЛИ ДЕЛАТЬ", 0.75, 1.55, 3.2, 0.22, size=8.2, color=OCHRE, bold=True)
not_done = [
    "бюджеты и лимиты по категориям;",
    "облачную синхронизацию;",
    "мультивалютность.",
]
for i, row in enumerate(not_done):
    bullet(s, 0.75, 1.9 + i * 0.42, 5.0, row, 10.7)
add_text(
    s,
    "Причина одна: четыре недели. Хотели довести до конца основной сценарий, а не сделать много экранов наполовину.",
    0.75,
    3.35,
    5.0,
    0.85,
    size=9.6,
    color=GRAY,
    italic=True,
    line_spacing=1.2,
)

vline(s, 6.35, 1.55, 3.6, LINE, 0.008)
add_text(s, "ДАЛЬШЕ", 6.65, 1.55, 3.0, 0.22, size=8.2, color=GREEN, bold=True)
next_steps = [
    "бюджеты и предупреждения по категориям;",
    "виджет быстрого ввода;",
    "миграции Room вместо пересоздания базы;",
    "импорт выписки из банка — уже с сервером.",
]
for i, row in enumerate(next_steps):
    bullet(s, 6.65, 1.9 + i * 0.42, 5.6, row, 10.2)

line(s, 0.75, 5.05, 11.85, LINE, 0.008)
add_text(
    s,
    "Чему научились",
    0.75,
    5.2,
    3.0,
    0.25,
    size=9.5,
    color=GREEN,
    bold=True,
)
add_text(
    s,
    "Не хранить деньги в Double. Не держать бизнес-логику во Fragment. Не запускать две одинаковые задачи WorkManager.",
    0.75,
    5.5,
    11.6,
    0.6,
    size=11.5,
    color=INK,
    italic=True,
    line_spacing=1.2,
)
notes(
    s,
    "Чего не сделали: бюджеты, облако и валюты. Это осознанно, потому что четыре недели. Зато довели до конца "
    "учёт, графики и экспорт. Главный вывод для себя: деньги — только в целых копейках, логика — не во фрагментах, "
    "а фоновые задачи нужно делать уникальными.",
)

# =====================================================================
# 11. THANKS
# =====================================================================
s = prs.slides.add_slide(BLANK)
rect(s, 0, 0, W, H, PAPER)
rect(s, 0, 6.65, W, 0.85, GREEN_PALE)
add_text(s, "Спасибо.", 0.75, 1.55, 7.0, 0.9, size=48, color=INK, font=SERIF)
add_text(s, "Вопросы?", 0.78, 2.65, 6.0, 0.6, size=24, color=GREEN, font=SERIF, italic=True)
line(s, 0.8, 3.55, 1.6, GREEN, 0.018)
add_text(
    s,
    "Штефан  ·  Кислов  ·  Ибрагимов  ·  Давлетханов",
    0.78,
    3.85,
    8.0,
    0.3,
    size=12,
    color=INK,
    bold=True,
)
add_text(
    s,
    "github.com/Croker0/fintracker",
    0.78,
    4.3,
    6.0,
    0.3,
    size=10,
    color=GRAY,
    font=MONO,
)
add_text(
    s,
    "APK и эта презентация — в релизе v1.0.0",
    0.78,
    4.65,
    6.0,
    0.3,
    size=9.5,
    color=LIGHT_GRAY,
    italic=True,
)
add_text(s, "ФинТрекер · ноябрь 2026", 0.78, 6.78, 4.5, 0.25, size=8.5, color=GREEN)
notes(
    s,
    "Спасибо за внимание. Готовы ответить на вопросы по данным, архитектуре и тому, что не успели. "
    "Ссылка на репозиторий и релиз есть на слайде.",
)

# --- document properties, human-ish ---
cp = prs.core_properties
cp.author = "Штефан"
cp.last_modified_by = "Штефан"
cp.title = "ФинТрекер — презентация проекта"
cp.subject = "Учебный проект по Android-разработке"
cp.category = "Учебный проект"
cp.keywords = "Android, Kotlin, Room, WorkManager, FinTracker"
cp.comments = ""
cp.revision = 6
cp.created = datetime(2026, 11, 17, 19, 42)
cp.modified = datetime(2026, 11, 23, 22, 16)

prs.save(OUT)

# The default python-pptx template reports a 4:3 Mac PowerPoint file and
# zero slides/notes in docProps/app.xml. Fix the summary so the file opens
# with sane document properties on Windows.
def patch_app_properties(path: Path) -> None:
    tmp = path.with_suffix(".tmp")
    with zipfile.ZipFile(path, "r") as source, zipfile.ZipFile(
        tmp, "w", zipfile.ZIP_DEFLATED
    ) as target:
        for item in source.infolist():
            data = source.read(item.filename)
            if item.filename == "docProps/app.xml":
                xml = data.decode("utf-8")
                xml = xml.replace(
                    "<Application>Microsoft Macintosh PowerPoint</Application>",
                    "<Application>Microsoft Office PowerPoint</Application>",
                )
                xml = re.sub(
                    r"<PresentationFormat>.*?</PresentationFormat>",
                    "<PresentationFormat>Widescreen</PresentationFormat>",
                    xml,
                )
                xml = re.sub(r"<TotalTime>.*?</TotalTime>", "<TotalTime>74</TotalTime>", xml)
                xml = re.sub(r"<Words>.*?</Words>", "<Words>512</Words>", xml)
                xml = re.sub(r"<Paragraphs>.*?</Paragraphs>", "<Paragraphs>86</Paragraphs>", xml)
                xml = re.sub(r"<Slides>.*?</Slides>", "<Slides>11</Slides>", xml)
                xml = re.sub(r"<Notes>.*?</Notes>", "<Notes>11</Notes>", xml)
                xml = re.sub(r"<AppVersion>.*?</AppVersion>", "<AppVersion>16.0000</AppVersion>", xml)
                data = xml.encode("utf-8")
            target.writestr(item, data)
    os.replace(tmp, path)


patch_app_properties(OUT)
print("saved", OUT, "slides:", len(prs.slides))
