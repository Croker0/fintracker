"""Generates legacy launcher PNGs and the project logo used in docs."""

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
DOCS = ROOT / "docs" / "images"
DOCS.mkdir(parents=True, exist_ok=True)

SIZE = 1024
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def base_icon(round_mask: bool) -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    top, bottom = (18, 148, 122), (11, 91, 79)
    for y in range(SIZE):
        draw.line([(0, y), (SIZE, y)], fill=mix(top, bottom, y / SIZE))

    scale, offset = 9.0, 26.0

    def p(x: float, y: float) -> tuple[float, float]:
        return offset + x * scale, offset + y * scale

    draw.polygon(
        [p(54, 28), p(34, 48), p(46, 48), p(46, 57), p(62, 57), p(62, 48), p(74, 48)],
        fill=(255, 255, 255, 255),
    )
    for x0, y0, x1, y1 in ((38, 64, 46, 80), (50, 58, 58, 80), (62, 68, 70, 80)):
        draw.rounded_rectangle([p(x0, y0), p(x1, y1)], radius=8, fill=(255, 255, 255, 235))

    mask = Image.new("L", (SIZE, SIZE), 0)
    mask_draw = ImageDraw.Draw(mask)
    if round_mask:
        mask_draw.ellipse([0, 0, SIZE - 1, SIZE - 1], fill=255)
    else:
        mask_draw.rounded_rectangle([0, 0, SIZE - 1, SIZE - 1], radius=210, fill=255)
    img.putalpha(mask)
    return img


def main() -> None:
    square = base_icon(round_mask=False)
    circle = base_icon(round_mask=True)

    for density, px in DENSITIES.items():
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        square.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher.png")
        circle.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher_round.png")

    square.resize((512, 512), Image.LANCZOS).save(DOCS / "app-icon.png")
    print("icons written", list(DENSITIES))


if __name__ == "__main__":
    main()
