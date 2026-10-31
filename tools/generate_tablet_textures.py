#!/usr/bin/env python3
"""Draw the tablet's pixel artwork and Patchouli 1.20 GUI atlas. Requires Pillow."""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/ariente/textures'
INK = '#080d17'
CASE = '#171d2a'
EDGE = '#3d4b62'
PANEL = '#101a2a'
CYAN = '#71dce4'
DIM = '#325767'
WHITE = '#d1f4f3'


def canvas(size):
    image = Image.new('RGBA', size)
    return image, ImageDraw.Draw(image)


def save(image, name):
    path = ROOT / name
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def tablet_item():
    image, draw = canvas((16, 16))
    draw.polygon([(3, 0), (12, 0), (14, 2), (14, 13), (12, 15), (3, 15), (1, 13), (1, 2)], fill=INK)
    draw.rectangle((3, 1, 12, 14), fill=CASE)
    draw.line([(2, 3), (2, 12)], fill=EDGE)
    draw.line([(3, 1), (11, 1)], fill='#68748a')
    draw.rectangle((4, 3, 11, 11), fill=DIM)
    draw.rectangle((5, 4, 10, 10), fill='#162c43')
    # Angular alien glyph, a status light, and a recessed home key.
    draw.line([(5, 9), (7, 5), (8, 5), (10, 9)], fill=CYAN)
    draw.line([(6, 8), (9, 8)], fill=WHITE)
    draw.point((11, 2), fill=CYAN)
    draw.line([(7, 13), (8, 13)], fill=CYAN)
    save(image, 'item/tools/tablet.png')


def glyph(draw, x, y, name, color, width=11, height=11):
    cx, cy = x + width // 2, y + height // 2
    if name in ('left', 'right', 'back'):
        direction = -1 if name in ('left', 'back') else 1
        draw.line([(cx - direction * 3, cy - 3), (cx + direction * 2, cy), (cx - direction * 3, cy + 3)], fill=color)
        draw.line((cx - 3, cy, cx + 3, cy), fill=color)
    elif name == 'search':
        draw.rectangle((x + 2, y + 2, x + 6, y + 6), outline=color)
        draw.line((x + 6, y + 6, x + 9, y + 9), fill=color)
    elif name == 'eye':
        draw.polygon([(x + 1, cy), (cx, y + 2), (x + 9, cy), (cx, y + 8)], outline=color)
        draw.point((cx, cy), fill=color)
    elif name == 'edit':
        draw.line((x + 3, y + 8, x + 8, y + 3), fill=color, width=2)
        draw.point((x + 2, y + 9), fill=color)
    elif name == 'resize':
        draw.line([(x + 2, y + 5), (x + 2, y + 2), (x + 5, y + 2)], fill=color)
        draw.line([(x + 5, y + 8), (x + 8, y + 8), (x + 8, y + 5)], fill=color)
    elif name == 'config':
        for xx in (3, 5, 7):
            draw.line((x + xx, y + 2, x + xx, y + 8), fill=color)
        draw.line((x + 2, y + 4, x + 4, y + 4), fill=WHITE)
        draw.line((x + 6, y + 6, x + 8, y + 6), fill=WHITE)
    elif name == 'history':
        draw.rectangle((x + 2, y + 2, x + 8, y + 8), outline=color)
        draw.line([(cx, y + 3), (cx, cy), (x + 7, cy)], fill=color)
    elif name == 'bookmark':
        draw.polygon([(cx - 2, y + 1), (cx + 2, y + 1), (cx + 2, y + 8), (cx, y + 6), (cx - 2, y + 8)], fill=color)
    else:
        draw.line([(x + 2, cy), (x + 4, cy + 2), (x + 8, cy - 2)], fill=color)


def book_atlas():
    image, draw = canvas((512, 256))
    # Patchouli renders a 272x180 rectangle, with two 116px text columns.
    # This is one rigid tablet casing, with two screen panes and no paper/spine.
    draw.polygon([(5, 0), (266, 0), (271, 5), (271, 174), (266, 179), (5, 179), (0, 174), (0, 5)], fill=INK)
    draw.rectangle((3, 3, 268, 176), fill=CASE, outline=EDGE)
    draw.line((8, 4, 263, 4), fill='#6a778c')
    draw.rectangle((9, 10, 262, 168), fill=PANEL, outline=DIM)
    draw.line((135, 15, 135, 164), fill='#273c50')
    draw.line((136, 15, 136, 164), fill=INK)
    # Accents stay outside the text area.
    for x in (11, 249):
        draw.line((x, 12, x + 11, 12), fill=CYAN)
    for x in (7, 264):
        draw.point((x, 7), fill='#7c8a9b')
        draw.point((x, 172), fill='#7c8a9b')
    draw.line((116, 173, 155, 173), fill=DIM)
    draw.line((128, 174, 143, 174), fill=CYAN)
    # Landing nameplate and the search/underline strip.
    draw.rectangle((0, 180, 139, 210), fill=CASE, outline=DIM)
    draw.line((1, 181, 1, 209), fill=CYAN)
    draw.line((8, 207, 131, 207), fill=DIM)
    draw.rectangle((140, 180, 249, 196), fill=PANEL)
    draw.line((140, 196, 249, 196), fill=DIM)
    glyph(draw, 140, 184, 'search', CYAN)
    # Lock and entry status indicators.
    draw.rectangle((253, 186, 263, 193), fill=DIM, outline=CYAN)
    draw.line([(255, 185), (255, 182), (261, 182), (261, 185)], fill=CYAN)
    for x, color in ((140, CYAN), (148, '#b3a3e3'), (156, '#659e93')):
        draw.polygon([(x + 4, 198), (x + 7, 201), (x + 4, 204), (x + 1, 201)], fill=color)
    # Every standard button includes adjacent normal and hovered UV regions.
    specs = [(272, 0, 18, 10, 'right'), (272, 10, 18, 10, 'left'),
             (308, 0, 18, 9, 'back'), (308, 9, 11, 11, 'edit'),
             (330, 9, 11, 11, 'resize'), (308, 20, 11, 11, 'config'),
             (330, 20, 11, 11, 'check'), (308, 31, 11, 11, 'eye'),
             (330, 31, 11, 11, 'history'), (272, 160, 13, 10, 'bookmark'),
             (272, 170, 13, 10, 'bookmark')]
    for x, y, width, height, name in specs:
        for offset, color in ((0, CYAN), (width, WHITE)):
            xx = x + offset
            draw.rectangle((xx, y, xx + width - 1, y + height - 1), fill=CASE, outline=DIM)
            glyph(draw, xx, y, name, color, width, height)
    for y, left in ((20, False), (27, True)):
        for offset, color in ((0, CYAN), (5, WHITE)):
            x = 272 + offset
            points = [(x + 3, y + 1), (x + 1, y + 3), (x + 3, y + 5)] if left else [(x + 1, y + 1), (x + 3, y + 3), (x + 1, y + 5)]
            draw.line(points, fill=color)
    save(image, 'gui/manual/tablet.png')


def crafting_atlas():
    image, draw = canvas((128, 256))
    draw.rectangle((0, 0, 99, 61), fill=PANEL, outline=DIM)
    for y in (4, 23, 42):
        for x in (4, 23, 42):
            draw.rectangle((x, y, x + 17, y + 17), fill=CASE, outline=EDGE)
    draw.rectangle((79, 23, 96, 40), fill=CASE, outline=CYAN)
    draw.line((64, 31, 74, 31), fill=CYAN)
    draw.line([(70, 27), (74, 31), (70, 35)], fill=CYAN)
    glyph(draw, 0, 64, 'config', CYAN)
    save(image, 'gui/manual/crafting.png')


def filler():
    image, draw = canvas((128, 128))
    # A quiet circuit motif on otherwise empty pages.
    color = '#233a4c'
    for points in [[(18, 96), (18, 70), (42, 46), (42, 24)],
                   [(30, 104), (30, 80), (54, 56), (54, 20)],
                   [(108, 32), (90, 32), (66, 56), (66, 106)],
                   [(98, 16), (78, 16), (66, 28)]]:
        draw.line(points, fill=color, width=2)
        x, y = points[-1]
        draw.rectangle((x - 2, y - 2, x + 2, y + 2), outline=DIM)
    draw.polygon([(44, 91), (57, 65), (69, 65), (82, 91)], outline=DIM, width=2)
    draw.line((50, 82, 76, 82), fill=DIM, width=2)
    save(image, 'gui/manual/circuit.png')


if __name__ == '__main__':
    tablet_item()
    book_atlas()
    crafting_atlas()
    filler()
