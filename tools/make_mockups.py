"""Generates design mockups for README and the presentation.

The mockups mirror the implemented Material 3 screens. They are illustrations,
not device screenshots; the README labels them accordingly.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "images"
OUT.mkdir(parents=True, exist_ok=True)

# Logical screen is 360 x 780 dp; canvas is rendered at 4x and downscaled to 720 x 1560.
W_DP, H_DP = 360, 780
S = 4
W, H = W_DP * S, H_DP * S

BG = "#F5F7F9"
CARD = "#FFFFFF"
BORDER = "#E7EBF0"
TEXT = "#101828"
MUTED = "#667085"
PRIMARY = "#0E7C66"
PRIMARY_DARK = "#0A5F4F"
PRIMARY_CONTAINER = "#D6F2EA"
ON_PRIMARY_CONTAINER = "#064E40"
SURFACE_VAR = "#EEF2F5"
INCOME = "#12A150"
INCOME_BG = "#EDFBF2"
EXPENSE = "#E5484D"
AMBER = "#F2A33A"

FONT_DIR = Path("C:/Windows/Fonts")
REGULAR = FONT_DIR / "segoeui.ttf"
SEMIBOLD = FONT_DIR / "seguisb.ttf"
BOLD = FONT_DIR / "segoeuib.ttf"
EMOJI = FONT_DIR / "seguiemj.ttf"

CATEGORIES = [
    ("Продукты", "🛒", "#F97316"),
    ("Кафе и рестораны", "☕", "#EF4444"),
    ("Жильё", "🏠", "#8B5CF6"),
    ("Транспорт", "🚕", "#3B82F6"),
    ("Развлечения", "🎮", "#EC4899"),
    ("Другое", "🧾", "#64748B"),
]


def font(size: float, weight: str = "regular", emoji: bool = False) -> ImageFont.FreeTypeFont:
    path = {"regular": REGULAR, "semibold": SEMIBOLD, "bold": BOLD, "emoji": EMOJI}[weight]
    return ImageFont.truetype(str(path), int(size * S))


def px(value: float) -> int:
    return int(round(value * S))


def mix(a: str, b: str, t: float) -> tuple[int, int, int]:
    a_rgb = tuple(int(a[i : i + 2], 16) for i in (1, 3, 5))
    b_rgb = tuple(int(b[i : i + 2], 16) for i in (1, 3, 5))
    return tuple(round(x + (y - x) * t) for x, y in zip(a_rgb, b_rgb))


def text(
    draw: ImageDraw.ImageDraw,
    x: float,
    y: float,
    value: str,
    size: float,
    color: str = TEXT,
    weight: str = "regular",
    anchor: str = "lt",
) -> None:
    draw.text((px(x), px(y)), value, font=font(size, weight), fill=color, anchor=anchor)


def rrect(
    draw: ImageDraw.ImageDraw,
    box: tuple[float, float, float, float],
    radius: float,
    fill: str | None,
    outline: str | None = None,
    width: float = 1,
) -> None:
    draw.rounded_rectangle(
        (px(box[0]), px(box[1]), px(box[2]), px(box[3])),
        radius=px(radius),
        fill=fill,
        outline=outline,
        width=px(width),
    )


def card(
    draw: ImageDraw.ImageDraw,
    box: tuple[float, float, float, float],
    radius: float = 16,
    fill: str = CARD,
) -> None:
    rrect(draw, box, radius, fill, BORDER, 0.8)


def line(draw: ImageDraw.ImageDraw, x1: float, y1: float, x2: float, y2: float, color: str, width: float = 1) -> None:
    draw.line((px(x1), px(y1), px(x2), px(y2)), fill=color, width=px(width))


def circle(draw: ImageDraw.ImageDraw, cx: float, cy: float, r: float, fill: str, outline: str | None = None, width: float = 1) -> None:
    draw.ellipse(
        (px(cx - r), px(cy - r), px(cx + r), px(cy + r)),
        fill=fill,
        outline=outline,
        width=px(width) if outline else 1,
    )


def emoji(draw: ImageDraw.ImageDraw, cx: float, cy: float, value: str, size: float) -> None:
    try:
        draw.text(
            (px(cx), px(cy)),
            value,
            font=font(size, "emoji"),
            anchor="mm",
            embedded_color=True,
        )
    except (TypeError, OSError):
        draw.text((px(cx), px(cy)), "•", font=font(size, "bold"), fill=TEXT, anchor="mm")


def gradient_card(
    image: Image.Image,
    draw: ImageDraw.ImageDraw,
    box: tuple[float, float, float, float],
    radius: float,
    top: str,
    bottom: str,
) -> None:
    x0, y0, x1, y1 = (px(box[0]), px(box[1]), px(box[2]), px(box[3]))
    grad = Image.new("RGB", (x1 - x0, y1 - y0))
    gd = ImageDraw.Draw(grad)
    for y in range(y1 - y0):
        gd.line((0, y, x1 - x0, y), fill=mix(top, bottom, y / max(1, y1 - y0 - 1)))
    mask = Image.new("L", (x1 - x0, y1 - y0), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, x1 - x0 - 1, y1 - y0 - 1), radius=px(radius), fill=255
    )
    image.paste(grad, (x0, y0), mask)


def status_bar(draw: ImageDraw.ImageDraw) -> None:
    text(draw, 16, 7, "9:41", 11, TEXT, "semibold")
    # signal / wifi / battery
    for i, h in enumerate((2.4, 3.4, 4.4, 5.4)):
        rrect(draw, (306 + i * 4.4, 13.6 - h, 309.4 + i * 4.4, 13.6), 0.8, TEXT)
    for r in (6.5, 4.8, 3.1):
        draw.arc(
            (px(325 - r), px(9 - r), px(325 + r), px(9 + r)),
            start=225,
            end=315,
            fill=TEXT,
            width=px(1.2),
        )
    rrect(draw, (336, 8.8, 349, 14.8), 2.2, None, TEXT, 0.8)
    rrect(draw, (337.2, 10.0, 346.4, 13.6), 1.2, TEXT)
    rrect(draw, (349.4, 10.8, 350.6, 12.8), 0.5, TEXT)


def nav_icon(draw: ImageDraw.ImageDraw, name: str, cx: float, cy: float, color: str) -> None:
    if name == "home":
        draw.polygon(
            [(px(cx - 7), px(cy - 0.5)), (px(cx), px(cy - 7)), (px(cx + 7), px(cy - 0.5)),
             (px(cx + 7), px(cy + 6)), (px(cx + 1.8), px(cy + 6)), (px(cx + 1.8), px(cy + 0.5)),
             (px(cx - 1.8), px(cy + 0.5)), (px(cx - 1.8), px(cy + 6)), (px(cx - 7), px(cy + 6))],
            fill=color,
        )
    elif name == "list":
        for i, w in enumerate((12, 12, 8)):
            rrect(draw, (cx - 6.5, cy - 5 + i * 4.2, cx - 6.5 + w, cy - 3.6 + i * 4.2), 1.0, color)
    elif name == "chart":
        for i, h in enumerate((6, 9, 12)):
            rrect(draw, (cx - 6 + i * 4.6, cy + 5 - h, cx - 2.8 + i * 4.6, cy + 5), 1.0, color)
    elif name == "tag":
        draw.polygon(
            [(px(cx - 6), px(cy - 5)), (px(cx + 1), px(cy - 5)), (px(cx + 6), px(cy)),
             (px(cx + 1), px(cy + 5)), (px(cx - 6), px(cy + 5))],
            outline=color,
            width=px(1.4),
        )
        circle(draw, cx - 2.8, cy - 2.2, 1.1, color)
    elif name == "settings":
        for i, offset in enumerate((-4.5, 0, 4.5)):
            line(draw, cx - 7, cy + offset, cx + 7, cy + offset, color, 1.2)
            knob = (-2.5 if i == 0 else (2.5 if i == 1 else -0.5))
            circle(draw, cx + knob, cy + offset, 1.8, CARD, color, 1.2)


def bottom_nav(draw: ImageDraw.ImageDraw, selected: int = 0) -> None:
    line(draw, 0, 719, 360, 719, BORDER, 0.8)
    draw.rectangle((0, px(720), W, H), fill=CARD)
    items = [
        ("home", "Главная"),
        ("list", "Операции"),
        ("chart", "Аналитика"),
        ("tag", "Категории"),
        ("settings", "Настройки"),
    ]
    for i, (icon, label) in enumerate(items):
        cx = 36 + i * 72
        color = PRIMARY if i == selected else MUTED
        nav_icon(draw, icon, cx, 737, color)
        text(draw, cx, 748, label, 8.3, color, "semibold" if i == selected else "regular", "mt")


def donut(draw: ImageDraw.ImageDraw, cx: float, cy: float, r: float, slices: list[tuple[int, str]]) -> None:
    total = sum(v for v, _ in slices)
    start = -90
    for value, color in slices:
        sweep = 360 * value / total
        draw.arc(
            (px(cx - r), px(cy - r), px(cx + r), px(cy + r)),
            start=start + 1.2,
            end=start + sweep - 1.2,
            fill=color,
            width=px(13),
        )
        start += sweep


def chip(draw: ImageDraw.ImageDraw, x: float, y: float, label: str, selected: bool, width: float | None = None) -> float:
    w = width if width is not None else max(46, len(label) * 6.2 + 18)
    rrect(
        draw,
        (x, y, x + w, y + 28),
        14,
        PRIMARY_CONTAINER if selected else SURFACE_VAR,
        PRIMARY if selected else None,
        0.8,
    )
    text(
        draw,
        x + w / 2,
        y + 7.2,
        label,
        10.5,
        ON_PRIMARY_CONTAINER if selected else MUTED,
        "semibold" if selected else "regular",
        "mt",
    )
    return x + w + 6


def header(draw: ImageDraw.ImageDraw, title: str) -> None:
    text(draw, 16, 33, title, 21, TEXT, "bold")
    line(draw, 0, 60, 360, 60, BG)


def transaction_row(
    draw: ImageDraw.ImageDraw,
    y: float,
    title: str,
    subtitle: str,
    amount: str,
    icon: str,
    color: str,
    time: str,
    income: bool = False,
    height: float = 56,
) -> None:
    if income:
        rrect(draw, (16, y + 1, 344, y + height - 1), 12, INCOME_BG)
    circle(draw, 38, y + height / 2, 15, mix(color, "#FFFFFF", 0.86))
    emoji(draw, 38, y + height / 2, icon, 15)
    text(draw, 62, y + 12, title, 12.5, TEXT, "semibold")
    text(draw, 62, y + 29, subtitle, 9.5, MUTED)
    text(draw, 338, y + 12, amount, 12.5, INCOME if income else EXPENSE, "bold", "rt")
    text(draw, 338, y + 30, time, 9, MUTED, "regular", "rt")


def phone_base(title: str | None = None) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(image)
    status_bar(draw)
    if title:
        header(draw, title)
    return image, draw


def dashboard() -> Image.Image:
    image, draw = phone_base()
    text(draw, 16, 36, "ФИНТРЕКЕР", 8.5, PRIMARY, "bold")
    text(draw, 16, 47, "Мои финансы", 24, TEXT, "bold")
    text(draw, 16, 78, "Сегодня, 13 ноября", 10.5, MUTED)

    x = 16
    for label, selected in (("Неделя", False), ("Месяц", True), ("Год", False), ("Всё время", False)):
        x = chip(draw, x, 98, label, selected)

    # Balance card
    gradient_card(image, draw, (16, 138, 344, 250), 16, "#12947A", "#0B5B4F")
    text(draw, 30, 150, "Баланс за период", 9.5, "#D7F5EE")
    text(draw, 30, 163, "54 790 ₽", 27, "#FFFFFF", "bold")
    line(draw, 30, 205, 330, 205, "#4EA394", 0.8)
    text(draw, 30, 214, "↑ Доходы", 9, "#D7F5EE")
    text(draw, 30, 227, "85 000 ₽", 11.5, "#FFFFFF", "semibold")
    text(draw, 180, 214, "↓ Расходы", 9, "#D7F5EE")
    text(draw, 180, 227, "30 210 ₽", 11.5, "#FFFFFF", "semibold")

    # Donut card
    card(draw, (16, 262, 344, 418))
    text(draw, 30, 272, "Структура расходов", 11.5, TEXT, "semibold")
    donut(draw, 92, 343, 46, [(34, CATEGORIES[0][2]), (22, CATEGORIES[1][2]), (18, CATEGORIES[2][2]), (12, CATEGORIES[3][2]), (8, CATEGORIES[4][2]), (6, CATEGORIES[5][2])])
    text(draw, 92, 331, "Расходы", 8, MUTED, "regular", "mt")
    text(draw, 92, 344, "30 210 ₽", 11, TEXT, "bold", "mt")
    total = 30210
    for i, (name, icon, color) in enumerate(CATEGORIES[:5]):
        pct = [34, 22, 18, 12, 8][i]
        cy = 296 + i * 21
        circle(draw, 164, cy + 4, 3.4, color)
        text(draw, 172, cy - 1, name, 9.2, MUTED)
        text(draw, 328, cy - 1, f"{pct}%", 9.2, TEXT, "semibold", "rt")

    # Bar card
    card(draw, (16, 428, 344, 570))
    text(draw, 30, 438, "Динамика", 11.5, TEXT, "semibold")
    circle(draw, 30, 459, 3.2, EXPENSE)
    text(draw, 37, 453, "Расходы", 8.2, MUTED)
    values = [1450, 2300, 1250, 900, 620, 780, 450, 3500, 1980, 2200, 1120, 680, 940]
    max_v = max(values)
    left, right, base, top = 30, 330, 550, 474
    slot = (right - left) / len(values)
    for i, value in enumerate(values):
        h = (value / max_v) * (base - top)
        x = left + slot * i + slot * 0.2
        rrect(draw, (x, base - h, x + slot * 0.6, base), 2, EXPENSE)
    line(draw, 30, 552, 330, 552, BORDER, 0.8)
    for i, label in ((0, "1"), (6, "7"), (12, "13")):
        text(draw, left + slot * (i + 0.5), 556, label, 7.5, MUTED, "regular", "mt")

    # Recent
    text(draw, 16, 584, "Последние операции", 11.5, TEXT, "semibold")
    text(draw, 344, 586, "Все", 9.5, PRIMARY, "semibold", "rt")
    transaction_row(draw, 602, "Продукты", "Пятёрочка", "−1 450 ₽", "🛒", "#F97316", "19:00")
    transaction_row(draw, 660, "Зарплата", "Ноябрьская зарплата", "+85 000 ₽", "💼", INCOME, "10:00", True)

    bottom_nav(draw, 0)
    return image


def transactions() -> Image.Image:
    image, draw = phone_base("Операции")
    # Search
    rrect(draw, (16, 70, 344, 106), 12, CARD, BORDER, 0.8)
    circle(draw, 33, 87.5, 5.2, None, MUTED, 1.2)
    line(draw, 37, 92, 41.5, 96.5, MUTED, 1.2)
    text(draw, 48, 80, "Поиск по заметкам и тегам", 9.8, MUTED)

    x = 16
    x = chip(draw, x, 114, "Все", True)
    x = chip(draw, x, 114, "Расходы", False)
    x = chip(draw, x, 114, "Доходы", False)
    rrect(draw, (270, 114, 344, 142), 14, SURFACE_VAR)
    text(draw, 279, 120.5, "Категория", 9.3, MUTED)
    draw.polygon([(px(333), px(123)), (px(339), px(123)), (px(336), px(127))], fill=MUTED)

    text(draw, 16, 153, "Сегодня", 9, MUTED, "bold")
    text(draw, 344, 153, "−4 070 ₽", 9, MUTED, "regular", "rt")
    transaction_row(draw, 167, "Продукты", "Пятёрочка", "−1 450 ₽", "🛒", "#F97316", "19:00")
    transaction_row(draw, 224, "Кафе и рестораны", "Кофе с собой", "−320 ₽", "☕", "#EF4444", "09:00")
    transaction_row(draw, 281, "Продукты", "Лента", "−2 300 ₽", "🛒", "#F97316", "20:00")

    text(draw, 16, 347, "Вчера", 9, MUTED, "bold")
    text(draw, 344, 347, "−2 300 ₽", 9, MUTED, "regular", "rt")
    transaction_row(draw, 361, "Здоровье", "Аптека", "−620 ₽", "💊", "#14B8A6", "12:00")
    transaction_row(draw, 418, "Развлечения", "Кино", "−900 ₽", "🎮", "#EC4899", "19:30")
    transaction_row(draw, 475, "Продукты", "Магнит", "−780 ₽", "🛒", "#F97316", "18:00")

    text(draw, 16, 541, "10 ноября", 9, MUTED, "bold")
    text(draw, 344, 541, "−6 400 ₽", 9, MUTED, "regular", "rt")
    transaction_row(draw, 555, "Здоровье", "Стоматология", "−1 500 ₽", "💊", "#14B8A6", "10:00")
    transaction_row(draw, 612, "Образование", "Курс по Kotlin", "−4 900 ₽", "📚", "#6366F1", "12:00")

    # Footer
    rrect(draw, (16, 676, 344, 708), 12, SURFACE_VAR)
    text(draw, 28, 684, "Итого", 10.5, TEXT, "semibold")
    text(draw, 28, 697, "8 операций", 8, MUTED)
    text(draw, 332, 683, "−12 770 ₽", 9.5, EXPENSE, "bold", "rt")
    text(draw, 332, 696, "+85 000 ₽", 9.5, INCOME, "bold", "rt")

    bottom_nav(draw, 1)
    return image


def stats() -> Image.Image:
    image, draw = phone_base("Аналитика")
    x = 16
    for label, selected in (("Неделя", False), ("Месяц", True), ("Год", False), ("Всё время", False)):
        x = chip(draw, x, 70, label, selected)

    card(draw, (16, 106, 344, 244))
    text(draw, 30, 118, "Расходы за период", 8.8, MUTED)
    text(draw, 30, 128, "30 210 ₽", 24, EXPENSE, "bold")
    text(draw, 30, 157, "Ноябрь 2026", 8.8, MUTED)
    line(draw, 30, 174, 330, 174, BORDER, 0.8)
    text(draw, 30, 182, "Доходы", 8, MUTED)
    text(draw, 30, 191, "85 000 ₽", 10.5, INCOME, "semibold")
    text(draw, 180, 182, "Баланс", 8, MUTED)
    text(draw, 180, 191, "54 790 ₽", 10.5, TEXT, "semibold")
    text(draw, 30, 211, "Операций", 8, MUTED)
    text(draw, 30, 220, "24", 10.5, TEXT, "semibold")
    text(draw, 180, 211, "В среднем в день", 8, MUTED)
    text(draw, 180, 220, "2 324 ₽", 10.5, TEXT, "semibold")

    # Insight
    card(draw, (16, 254, 344, 304), 14)
    emoji(draw, 34, 279, "💡", 14)
    text(draw, 48, 264, "Больше всего вы тратите на «Продукты»", 8.7, MUTED)
    text(draw, 48, 276, "— 34% расходов. Средний чек — 1 259 ₽.", 8.7, MUTED)

    # Top categories
    card(draw, (16, 312, 344, 494))
    text(draw, 30, 322, "Топ категорий", 11.5, TEXT, "semibold")
    rows = [
        ("Продукты", "10 274 ₽", 34, "#F97316"),
        ("Кафе и рестораны", "6 646 ₽", 22, "#EF4444"),
        ("Жильё", "5 438 ₽", 18, "#8B5CF6"),
        ("Транспорт", "3 625 ₽", 12, "#3B82F6"),
        ("Развлечения", "2 417 ₽", 8, "#EC4899"),
    ]
    for i, (name, amount, pct, color) in enumerate(rows):
        y = 340 + i * 29
        circle(draw, 31, y + 6.5, 2.8, color)
        text(draw, 38, y + 1, name, 9.5, TEXT)
        text(draw, 235, y + 1, f"{pct}%", 8.5, MUTED, "regular", "rt")
        text(draw, 330, y + 1, amount, 9.5, TEXT, "semibold", "rt")
        rrect(draw, (38, y + 14, 330, y + 18.5), 2.2, SURFACE_VAR)
        rrect(draw, (38, y + 14, 38 + (292 * pct / 34), y + 18.5), 2.2, color)

    # Chart
    card(draw, (16, 502, 344, 704))
    text(draw, 30, 512, "Динамика", 11.5, TEXT, "semibold")
    values = [5200, 4300, 6100, 3800, 7100, 4600, 5500, 4200, 6800, 5900, 4700, 6300]
    max_v = max(values)
    left, right, base, top = 30, 330, 688, 536
    slot = (right - left) / len(values)
    for i, value in enumerate(values):
        h = (value / max_v) * (base - top)
        bx = left + slot * i + slot * 0.18
        rrect(draw, (bx, base - h, bx + slot * 0.64, base), 2.5, EXPENSE)
    line(draw, 30, 690, 330, 690, BORDER, 0.8)
    for i, label in enumerate(("Янв", "Мар", "Май", "Июл", "Сен", "Ноя")):
        text(draw, 42 + i * 50.5, 694, label, 7.4, MUTED, "regular", "mt")

    bottom_nav(draw, 2)
    return image


def editor() -> Image.Image:
    image = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(image)
    status_bar(draw)
    # Toolbar
    line(draw, 22, 40, 29, 33, TEXT, 1.6)
    line(draw, 22, 40, 29, 47, TEXT, 1.6)
    text(draw, 40, 33, "Новая операция", 16, TEXT, "bold")
    line(draw, 0, 60, 360, 60, BG)

    # Toggle
    rrect(draw, (16, 72, 344, 110), 12, SURFACE_VAR)
    rrect(draw, (18, 74, 180, 108), 10, CARD)
    text(draw, 99, 84, "Расход", 11, PRIMARY, "semibold", "mt")
    text(draw, 262, 84, "Доход", 11, MUTED, "regular", "mt")

    # Amount
    rrect(draw, (16, 124, 344, 188), 14, CARD, BORDER, 0.8)
    text(draw, 30, 136, "Сумма", 8.5, MUTED)
    text(draw, 30, 148, "1 450 ₽", 26, TEXT, "bold")
    text(draw, 330, 158, "₽", 12, MUTED, "regular", "rt")

    # Categories
    text(draw, 16, 204, "Категория", 10.5, TEXT, "semibold")
    cats = [
        ("🛒", "Продукты", True, "#F97316"),
        ("☕", "Кафе", False, "#EF4444"),
        ("🚕", "Транспорт", False, "#3B82F6"),
        ("🏠", "Жильё", False, "#8B5CF6"),
        ("💊", "Здоровье", False, "#14B8A6"),
        ("🎮", "Развлечения", False, "#EC4899"),
        ("👕", "Одежда", False, "#F59E0B"),
        ("📚", "Образование", False, "#6366F1"),
    ]
    for i, (icon, name, selected, color) in enumerate(cats):
        col, row = i % 4, i // 4
        x0 = 16 + col * 82
        y0 = 218 + row * 60
        rrect(draw, (x0, y0, x0 + 76, y0 + 54), 12, PRIMARY_CONTAINER if selected else SURFACE_VAR, PRIMARY if selected else None, 1)
        emoji(draw, x0 + 38, y0 + 18, icon, 17)
        text(draw, x0 + 38, y0 + 36, name, 7.6, TEXT if selected else MUTED, "semibold" if selected else "regular", "mt")

    # Date
    rrect(draw, (16, 352, 344, 392), 12, CARD, BORDER, 0.8)
    text(draw, 30, 362, "Дата", 8.2, MUTED)
    text(draw, 30, 372, "13 ноября 2026", 10.5, TEXT)
    emoji(draw, 330, 370, "📅", 11)

    # Note
    rrect(draw, (16, 400, 344, 440), 12, CARD, BORDER, 0.8)
    text(draw, 30, 410, "Комментарий", 8.2, MUTED)
    text(draw, 30, 420, "Пятёрочка", 10.5, TEXT)

    # Tags
    rrect(draw, (16, 448, 344, 488), 12, CARD, BORDER, 0.8)
    text(draw, 30, 458, "Теги через запятую", 8.2, MUTED)
    text(draw, 30, 468, "покупки, дом", 10.5, TEXT)
    for i, tag in enumerate(("покупки", "дом")):
        rrect(draw, (16 + i * 58, 496, 16 + i * 58 + 50, 516), 10, PRIMARY_CONTAINER)
        text(draw, 41 + i * 58, 501, tag, 8, ON_PRIMARY_CONTAINER, "regular", "mt")

    # Save
    rrect(draw, (16, 728, 344, 768), 14, PRIMARY)
    text(draw, 180, 740, "Сохранить", 12.5, "#FFFFFF", "semibold", "mt")
    return image


def main() -> None:
    screens = {
        "mockup-dashboard.png": dashboard(),
        "mockup-transactions.png": transactions(),
        "mockup-stats.png": stats(),
        "mockup-editor.png": editor(),
    }
    for name, image in screens.items():
        image = image.resize((720, 1560), Image.LANCZOS)
        image.save(OUT / name, optimize=True)
        print("saved", OUT / name)


if __name__ == "__main__":
    main()

