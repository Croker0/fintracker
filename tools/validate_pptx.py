"""Structural validation of the generated deck."""

from __future__ import annotations

import math
from pathlib import Path

from pptx import Presentation
from pptx.enum.shapes import MSO_SHAPE_TYPE

PATH = Path(__file__).resolve().parents[1] / "presentation" / "FinTracker.pptx"
EMU = 914400

prs = Presentation(PATH)
print("slides:", len(prs.slides))
print("size:", round(prs.slide_width / EMU, 3), "x", round(prs.slide_height / EMU, 3))

width, height = prs.slide_width, prs.slide_height
bounds_issues = []
overflow_suspects = []
empty_text = []

for index, slide in enumerate(prs.slides, 1):
    pictures = 0
    texts = 0
    for shape in slide.shapes:
        if shape.shape_type == MSO_SHAPE_TYPE.PICTURE:
            pictures += 1
        if shape.left is not None:
            if (
                shape.left < -EMU // 10
                or shape.top < -EMU // 10
                or shape.left + shape.width > width + EMU // 10
                or shape.top + shape.height > height + EMU // 10
            ):
                bounds_issues.append(
                    (
                        index,
                        shape.name,
                        round(shape.left / EMU, 2),
                        round(shape.top / EMU, 2),
                        round(shape.width / EMU, 2),
                        round(shape.height / EMU, 2),
                    )
                )
        if not shape.has_text_frame:
            continue
        value = shape.text_frame.text.strip()
        if not value:
            continue
        texts += 1
        size = 14.0
        for paragraph in shape.text_frame.paragraphs:
            for run in paragraph.runs:
                if run.font.size is not None:
                    size = run.font.size.pt
                    break
            if size:
                break
        box_w = shape.width / EMU
        box_h = shape.height / EMU
        chars_per_line = max(4.0, box_w * 96.0 / (size * 0.56))
        explicit_lines = value.count("\n") + 1
        lines = sum(
            max(1, math.ceil(len(part) / chars_per_line))
            for part in value.split("\n")
        )
        needed = (lines * size * 1.22) / 72.0 + 0.02
        if needed > box_h * 1.18 + 0.12:
            overflow_suspects.append(
                (
                    index,
                    shape.name,
                    value[:55].replace("\n", " / "),
                    round(size, 1),
                    round(box_w, 2),
                    round(box_h, 2),
                    round(needed, 2),
                )
            )

    notes = ""
    if slide.has_notes_slide:
        notes = slide.notes_slide.notes_text_frame.text.strip()
    print(
        f"  {index:02d}: shapes={len(slide.shapes):2d} pictures={pictures} "
        f"texts={texts:2d} notes={len(notes)}"
    )

print("\nout-of-bounds:", len(bounds_issues))
for item in bounds_issues[:20]:
    print("  ", item)

print("\npossible text overflow:", len(overflow_suspects))
for item in overflow_suspects[:30]:
    print("  ", item)
