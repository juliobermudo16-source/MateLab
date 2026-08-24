"""
Generador de PDF para la documentacion de MateLab.

Escribe ficheros PDF 1.4 validos usando SOLO la biblioteca estandar de
Python: no hace falta reportlab, ni fpdf, ni ninguna descarga. Se apoya en
las fuentes base (Helvetica, Helvetica-Bold, Courier) que todo visor de PDF
incluye, con sus anchos reales para partir las lineas correctamente.

Admite el subconjunto de Markdown que usa la documentacion del proyecto:
titulos, parrafos, listas, tablas, citas, reglas, bloques de codigo y, en
linea, negrita y codigo.

Uso:  python tools/md2pdf.py docs/MANUAL_TECNICO.md docs/pdf/MANUAL_TECNICO.pdf "Manual tecnico"
"""

import datetime
import os
import re
import sys
import zlib

# --------------------------------------------------------------------- pagina

PAGE_W, PAGE_H = 595.28, 841.89          # A4 en puntos
MARGIN_L, MARGIN_R = 56.0, 56.0
MARGIN_T, MARGIN_B = 62.0, 58.0
CONTENT_W = PAGE_W - MARGIN_L - MARGIN_R

FONT_REGULAR, FONT_BOLD, FONT_MONO = "F1", "F2", "F3"

# Paleta de MateLab, en componentes 0..1
INK = (0.063, 0.192, 0.239)
INK_SOFT = (0.329, 0.451, 0.494)
TEAL = (0.071, 0.702, 0.651)
MANGO = (1.0, 0.541, 0.239)
SAND = (0.996, 0.965, 0.914)
RULE = (0.827, 0.902, 0.894)
CODE_BG = (0.949, 0.973, 0.969)

# ------------------------------------------------------------------ metricas

_HELV = (
    "278 278 355 556 556 889 667 191 333 333 389 584 278 333 278 278 "
    "556 556 556 556 556 556 556 556 556 556 278 278 584 584 584 556 "
    "1015 667 667 722 722 667 611 778 722 278 500 667 556 833 722 778 "
    "667 778 722 667 611 722 667 944 667 667 611 278 278 278 469 556 "
    "333 556 556 500 556 556 278 556 556 222 222 500 222 833 556 556 "
    "556 556 333 500 278 556 500 722 500 500 500 334 260 334 584"
)
_HELV_BOLD = (
    "278 333 474 556 556 889 722 238 333 333 389 584 278 333 278 278 "
    "556 556 556 556 556 556 556 556 556 556 333 333 584 584 584 611 "
    "975 722 722 722 722 667 611 778 722 278 556 722 611 833 722 778 "
    "667 778 722 667 611 722 667 944 667 667 611 333 278 333 584 556 "
    "333 556 611 556 611 556 333 611 611 278 278 556 278 889 611 611 "
    "611 611 389 556 333 611 556 778 556 556 500 389 280 389 584"
)


def _width_table(raw):
    values = [int(v) for v in raw.split()]
    table = {}
    for index, value in enumerate(values):
        table[chr(32 + index)] = value
    return table


WIDTHS = {
    FONT_REGULAR: _width_table(_HELV),
    FONT_BOLD: _width_table(_HELV_BOLD),
}

# Dos usos:
#  - medir: los acentos se miden como su letra base (Latin-1 los imprime bien).
#  - sustituir: lo que no existe en Latin-1 se cambia por un equivalente ASCII.
ACCENTS = {
    # Acentos: solo para medir el ancho.
    "á": "a", "é": "e", "í": "i", "ó": "o", "ú": "u",
    "ü": "u", "ñ": "n",
    "Á": "A", "É": "E", "Í": "I", "Ó": "O", "Ú": "U",
    "Ü": "U", "Ñ": "N",
    "¿": "?", "¡": "!", "·": ".", "×": "x",

    # Fuera de Latin-1: hay que sustituirlos de verdad.
    "—": "-",      # raya
    "–": "-",      # semirraya
    "−": "-",      # signo menos
    "‘": "'", "’": "'",
    "“": '"', "”": '"',
    "…": "...",
    "→": "->", "←": "<-", "↑": "^", "↓": "v",
    "≤": "<=", "≥": ">=", "≠": "!=", "≈": "~",
    "•": "-", "✓": "v", "✗": "x",
    # Caracteres de dibujo de cajas usados en los diagramas.
    "─": "-", "│": "|",
    "┌": "+", "┐": "+", "└": "+", "┘": "+",
    "├": "+", "┤": "+", "┬": "+", "┴": "+", "┼": "+",
}


def text_width(text, font, size):
    if font == FONT_MONO:
        return len(text) * size * 0.6
    table = WIDTHS[font]
    total = 0
    for char in text:
        char = ACCENTS.get(char, char)
        if len(char) > 1:
            total += sum(table.get(c, 556) for c in char)
        else:
            total += table.get(char, 556)
    return total * size / 1000.0


def to_latin1(text):
    """Sustituye lo que no cabe en Latin-1 por un equivalente imprimible."""
    out = []
    for char in text:
        try:
            char.encode("latin-1")
            out.append(char)
        except UnicodeEncodeError:
            out.append(ACCENTS.get(char, "?"))
    return "".join(out)


def escape(text):
    return (
        to_latin1(text)
        .replace("\\", r"\\")
        .replace("(", r"\(")
        .replace(")", r"\)")
    )


# ------------------------------------------------------------------ markdown

RE_BOLD = re.compile(r"\*\*(.+?)\*\*")
RE_CODE = re.compile(r"`([^`]+)`")
RE_LINK = re.compile(r"\[([^\]]+)\]\([^)]*\)")


def inline_runs(text):
    """Trocea una linea en (texto, fuente) respetando negrita y codigo."""
    text = RE_LINK.sub(r"\1", text)
    tokens = re.split(r"(\*\*.+?\*\*|`[^`]+`)", text)
    runs = []
    for token in tokens:
        if not token:
            continue
        bold = RE_BOLD.fullmatch(token)
        code = RE_CODE.fullmatch(token)
        if bold:
            runs.append((bold.group(1), FONT_BOLD))
        elif code:
            runs.append((code.group(1), FONT_MONO))
        else:
            runs.append((token, FONT_REGULAR))
    return runs or [("", FONT_REGULAR)]


def parse_markdown(source):
    """Convierte Markdown en una lista de bloques."""
    blocks = []
    lines = source.splitlines()
    index = 0
    while index < len(lines):
        line = lines[index]
        stripped = line.strip()

        if stripped.startswith("```"):
            index += 1
            code = []
            while index < len(lines) and not lines[index].strip().startswith("```"):
                code.append(lines[index])
                index += 1
            index += 1
            blocks.append(("code", code))
            continue

        if not stripped:
            index += 1
            continue

        if stripped.startswith("#"):
            level = len(stripped) - len(stripped.lstrip("#"))
            blocks.append(("h{}".format(min(level, 3)), stripped.lstrip("#").strip()))
            index += 1
            continue

        if re.fullmatch(r"-{3,}|\*{3,}|_{3,}", stripped):
            blocks.append(("hr", None))
            index += 1
            continue

        if stripped.startswith("|"):
            rows = []
            while index < len(lines) and lines[index].strip().startswith("|"):
                raw = lines[index].strip().strip("|")
                cells = [c.strip() for c in raw.split("|")]
                if not all(re.fullmatch(r":?-{2,}:?", c) for c in cells if c):
                    rows.append(cells)
                index += 1
            if rows:
                blocks.append(("table", rows))
            continue

        if stripped.startswith("> "):
            quote = []
            while index < len(lines) and lines[index].strip().startswith(">"):
                quote.append(lines[index].strip().lstrip(">").strip())
                index += 1
            blocks.append(("quote", " ".join(quote)))
            continue

        bullet = re.match(r"^(\s*)[-*+]\s+(.*)$", line)
        number = re.match(r"^(\s*)(\d+)\.\s+(.*)$", line)
        if bullet or number:
            items = []
            while index < len(lines):
                b = re.match(r"^(\s*)[-*+]\s+(.*)$", lines[index])
                n = re.match(r"^(\s*)(\d+)\.\s+(.*)$", lines[index])
                if b:
                    items.append((len(b.group(1)) // 2, "-", b.group(2)))
                elif n:
                    items.append((len(n.group(1)) // 2, n.group(2) + ".", n.group(3)))
                elif lines[index].strip() and lines[index].startswith("  "):
                    if items:
                        level, marker, prev = items[-1]
                        items[-1] = (level, marker, prev + " " + lines[index].strip())
                else:
                    break
                index += 1
            blocks.append(("list", items))
            continue

        paragraph = []
        while index < len(lines) and lines[index].strip() and not re.match(
            r"^\s*(#|\||>|```|[-*+]\s|\d+\.\s|-{3,}$)", lines[index]
        ):
            paragraph.append(lines[index].strip())
            index += 1
        if paragraph:
            blocks.append(("p", " ".join(paragraph)))
        else:
            index += 1

    return blocks


# -------------------------------------------------------------------- lienzo


class Pdf:
    """Acumula operaciones de dibujo y las serializa como PDF."""

    def __init__(self, title, subtitle):
        self.title = title
        self.subtitle = subtitle
        self.pages = []
        self.ops = []
        self.y = PAGE_H - MARGIN_T
        self.page_number = 0

    # ------------------------------------------------------------ primitivas

    def _rgb(self, color):
        return "{:.3f} {:.3f} {:.3f}".format(*color)

    def rect(self, x, y, w, h, color, stroke=False, line_width=0.7):
        if stroke:
            self.ops.append(
                "{} RG {:.2f} w {:.2f} {:.2f} {:.2f} {:.2f} re S".format(
                    self._rgb(color), line_width, x, y, w, h
                )
            )
        else:
            self.ops.append(
                "{} rg {:.2f} {:.2f} {:.2f} {:.2f} re f".format(
                    self._rgb(color), x, y, w, h
                )
            )

    def line(self, x1, y1, x2, y2, color, width=0.8):
        self.ops.append(
            "{} RG {:.2f} w {:.2f} {:.2f} m {:.2f} {:.2f} l S".format(
                self._rgb(color), width, x1, y1, x2, y2
            )
        )

    def text(self, x, y, content, font=FONT_REGULAR, size=10.0, color=INK):
        if not content:
            return
        self.ops.append(
            "BT {} rg /{} {:.2f} Tf {:.2f} {:.2f} Td ({}) Tj ET".format(
                self._rgb(color), font, size, x, y, escape(content)
            )
        )

    def circle(self, cx, cy, r, color):
        k = 0.5523 * r
        self.ops.append(
            "{} rg {:.2f} {:.2f} m "
            "{:.2f} {:.2f} {:.2f} {:.2f} {:.2f} {:.2f} c "
            "{:.2f} {:.2f} {:.2f} {:.2f} {:.2f} {:.2f} c "
            "{:.2f} {:.2f} {:.2f} {:.2f} {:.2f} {:.2f} c "
            "{:.2f} {:.2f} {:.2f} {:.2f} {:.2f} {:.2f} c f".format(
                self._rgb(color),
                cx, cy + r,
                cx + k, cy + r, cx + r, cy + k, cx + r, cy,
                cx + r, cy - k, cx + k, cy - r, cx, cy - r,
                cx - k, cy - r, cx - r, cy - k, cx - r, cy,
                cx - r, cy + k, cx - k, cy + r, cx, cy + r,
            )
        )

    # -------------------------------------------------------------- paginado

    def new_page(self, cover=False):
        if self.ops:
            self.pages.append(self.ops)
        self.ops = []
        self.page_number += 1
        if not cover:
            self._page_furniture()
        self.y = PAGE_H - MARGIN_T

    def _page_furniture(self):
        self.rect(0, PAGE_H - 26, PAGE_W, 26, SAND)
        self.text(MARGIN_L, PAGE_H - 18, "MateLab - Islas del Ingenio",
                  FONT_BOLD, 8, INK_SOFT)
        right = self.title
        self.text(PAGE_W - MARGIN_R - text_width(right, FONT_REGULAR, 8),
                  PAGE_H - 18, right, FONT_REGULAR, 8, INK_SOFT)
        self.line(MARGIN_L, 40, PAGE_W - MARGIN_R, 40, RULE, 0.6)
        label = str(self.page_number - 1)
        self.text((PAGE_W - text_width(label, FONT_REGULAR, 9)) / 2, 28,
                  label, FONT_REGULAR, 9, INK_SOFT)

    def ensure(self, needed):
        if self.y - needed < MARGIN_B:
            self.new_page()

    # --------------------------------------------------------------- portada

    def cover(self, subtitle, doc_title):
        self.new_page(cover=True)
        self.rect(0, 0, PAGE_W, PAGE_H, SAND)
        self.rect(0, PAGE_H - 250, PAGE_W, 250, INK)

        # Kubo simplificado, dibujado con trazados
        cx, cy = PAGE_W / 2, PAGE_H - 150
        self.rect(cx - 4, cy + 46, 8, 26, SAND)
        self.circle(cx, cy + 80, 11, MANGO)
        self.rect(cx - 52, cy - 40, 104, 88, SAND)
        self.rect(cx - 40, cy - 16, 80, 46, INK)
        self.circle(cx - 18, cy + 7, 11, TEAL)
        self.circle(cx + 18, cy + 7, 11, TEAL)
        self.circle(cx - 21, cy + 11, 4, (1, 1, 1))
        self.circle(cx + 15, cy + 11, 4, (1, 1, 1))
        self.rect(cx - 66, cy - 8, 12, 22, MANGO)
        self.rect(cx + 54, cy - 8, 12, 22, MANGO)

        y = PAGE_H - 330
        title = "MateLab"
        self.text((PAGE_W - text_width(title, FONT_BOLD, 42)) / 2, y, title,
                  FONT_BOLD, 42, INK)
        y -= 30
        tag = "Islas del Ingenio"
        self.text((PAGE_W - text_width(tag, FONT_REGULAR, 17)) / 2, y, tag,
                  FONT_REGULAR, 17, TEAL)

        y -= 60
        self.line(PAGE_W / 2 - 90, y, PAGE_W / 2 + 90, y, RULE, 1.2)
        y -= 34
        self.text((PAGE_W - text_width(doc_title, FONT_BOLD, 22)) / 2, y,
                  doc_title, FONT_BOLD, 22, INK)
        y -= 24
        self.text((PAGE_W - text_width(subtitle, FONT_REGULAR, 11)) / 2, y,
                  subtitle, FONT_REGULAR, 11, INK_SOFT)

        y = 150
        lines = [
            "Aplicacion Android educativa de matematicas",
            "para ninos de 8 a 12 anos",
            "",
            "Version 1.0.0",
            datetime.date.today().strftime("%d/%m/%Y"),
        ]
        for line in lines:
            if line:
                self.text((PAGE_W - text_width(line, FONT_REGULAR, 10)) / 2, y,
                          line, FONT_REGULAR, 10, INK_SOFT)
            y -= 16

        self.rect(0, 0, PAGE_W, 10, TEAL)

    # --------------------------------------------------------------- bloques

    def wrap(self, runs, width, size):
        """Reparte los tramos en lineas que caben en el ancho dado."""
        lines, current, used = [], [], 0.0
        for content, font in runs:
            for word in re.split(r"(\s+)", content):
                if not word:
                    continue
                w = text_width(word, font, size)
                if word.isspace():
                    if current and used + w <= width:
                        current.append((word, font))
                        used += w
                    continue
                if used + w > width and current:
                    lines.append(current)
                    current, used = [], 0.0
                current.append((word, font))
                used += w
        if current:
            lines.append(current)
        return lines or [[]]

    def draw_line_runs(self, x, y, line, size, color=INK):
        cursor = x
        for content, font in line:
            self.text(cursor, y, content, font, size, color)
            cursor += text_width(content, font, size)

    def paragraph(self, runs, size=9.8, leading=13.6, color=INK,
                  indent=0.0, space_after=8.0):
        width = CONTENT_W - indent
        for line in self.wrap(runs, width, size):
            self.ensure(leading)
            self.y -= leading
            self.draw_line_runs(MARGIN_L + indent, self.y, line, size, color)
        self.y -= space_after

    def heading(self, level, text):
        sizes = {1: 19.0, 2: 13.5, 3: 11.0}
        before = {1: 20.0, 2: 16.0, 3: 11.0}
        after = {1: 12.0, 2: 8.0, 3: 5.0}
        size = sizes[level]
        self.ensure(before[level] + size + after[level] + 12)
        self.y -= before[level]
        if level == 1:
            self.rect(MARGIN_L, self.y - 6, 4, size + 4, TEAL)
            self.draw_line_runs(MARGIN_L + 12, self.y, inline_runs(text), size)
            self.y -= 10
            self.line(MARGIN_L, self.y, PAGE_W - MARGIN_R, self.y, RULE, 0.8)
        elif level == 2:
            self.draw_line_runs(MARGIN_L, self.y, inline_runs(text), size)
        else:
            self.draw_line_runs(MARGIN_L, self.y, inline_runs(text), size,
                                INK_SOFT)
        self.y -= after[level]

    def bullets(self, items):
        for level, marker, content in items:
            indent = 14.0 + level * 14.0
            runs = inline_runs(content)
            lines = self.wrap(runs, CONTENT_W - indent - 12, 9.8)
            for number, line in enumerate(lines):
                self.ensure(13.4)
                self.y -= 13.4
                if number == 0:
                    self.text(MARGIN_L + indent - 10, self.y, marker,
                              FONT_BOLD, 9.8, TEAL)
                self.draw_line_runs(MARGIN_L + indent + 4, self.y, line, 9.8)
        self.y -= 7

    def code_block(self, lines):
        size = 8.2
        leading = 11.0
        padding = 7.0
        chunk = []
        for raw in lines:
            chunk.extend(self._split_code(raw, size))
        height = leading * len(chunk) + padding * 2
        if height > PAGE_H - MARGIN_T - MARGIN_B:
            height = None  # bloque muy largo: se parte entre paginas
        if height:
            self.ensure(height + 6)
            self.rect(MARGIN_L, self.y - height + 4, CONTENT_W, height, CODE_BG)
        self.y -= padding
        for raw in chunk:
            self.ensure(leading)
            self.y -= leading
            self.text(MARGIN_L + 8, self.y, raw, FONT_MONO, size, INK)
        self.y -= padding + 6

    def _split_code(self, raw, size):
        limit = int((CONTENT_W - 16) / (size * 0.6))
        if len(raw) <= limit:
            return [raw]
        parts = []
        while raw:
            parts.append(raw[:limit])
            raw = raw[limit:]
        return parts

    def table(self, rows):
        if not rows:
            return
        columns = max(len(r) for r in rows)
        rows = [r + [""] * (columns - len(r)) for r in rows]
        size = 8.6
        leading = 11.4
        padding = 4.0

        weights = []
        for index in range(columns):
            longest = max(
                text_width(re.sub(r"[*`]", "", row[index]), FONT_REGULAR, size)
                for row in rows
            )
            weights.append(max(longest, 30.0))
        total = sum(weights)
        widths = [w / total * CONTENT_W for w in weights]

        # Un minimo por columna para que no queden columnas ilegibles
        floor = CONTENT_W / (columns * 2.6)
        widths = [max(w, floor) for w in widths]
        scale = CONTENT_W / sum(widths)
        widths = [w * scale for w in widths]

        for number, row in enumerate(rows):
            header = number == 0
            font = FONT_BOLD if header else FONT_REGULAR
            wrapped = []
            for index, cell in enumerate(row):
                runs = inline_runs(cell)
                if header:
                    runs = [(t, FONT_BOLD) for t, _f in runs]
                wrapped.append(self.wrap(runs, widths[index] - 8, size))
            height = leading * max(len(w) for w in wrapped) + padding

            self.ensure(height + 4)
            if header:
                self.rect(MARGIN_L, self.y - height + 3, CONTENT_W, height, SAND)

            top = self.y
            for index, cell_lines in enumerate(wrapped):
                x = MARGIN_L + sum(widths[:index]) + 4
                y = top
                for line in cell_lines:
                    y -= leading
                    self.draw_line_runs(x, y, line, size,
                                        INK if header else INK_SOFT)
            self.y -= height
            self.line(MARGIN_L, self.y + 2, PAGE_W - MARGIN_R, self.y + 2,
                      RULE, 0.5)
        self.y -= 9

    def quote(self, text):
        runs = inline_runs(text)
        lines = self.wrap(runs, CONTENT_W - 26, 9.6)
        height = len(lines) * 13.4 + 8
        self.ensure(height + 6)
        self.rect(MARGIN_L, self.y - height + 6, 3.5, height, MANGO)
        for line in lines:
            self.y -= 13.4
            self.draw_line_runs(MARGIN_L + 16, self.y, line, 9.6, INK_SOFT)
        self.y -= 12

    def rule(self):
        self.ensure(16)
        self.y -= 8
        self.line(MARGIN_L, self.y, PAGE_W - MARGIN_R, self.y, RULE, 0.6)
        self.y -= 8

    # ------------------------------------------------------------- guardado

    def render(self, blocks):
        self.new_page()
        for kind, payload in blocks:
            if kind == "h1":
                self.heading(1, payload)
            elif kind == "h2":
                self.heading(2, payload)
            elif kind == "h3":
                self.heading(3, payload)
            elif kind == "p":
                self.paragraph(inline_runs(payload))
            elif kind == "list":
                self.bullets(payload)
            elif kind == "code":
                self.code_block(payload)
            elif kind == "table":
                self.table(payload)
            elif kind == "quote":
                self.quote(payload)
            elif kind == "hr":
                self.rule()
        if self.ops:
            self.pages.append(self.ops)

    def save(self, path):
        objects = []

        def add(body):
            objects.append(body)
            return len(objects)

        font_regular = add(
            b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
            b"/Encoding /WinAnsiEncoding >>"
        )
        font_bold = add(
            b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold "
            b"/Encoding /WinAnsiEncoding >>"
        )
        font_mono = add(
            b"<< /Type /Font /Subtype /Type1 /BaseFont /Courier "
            b"/Encoding /WinAnsiEncoding >>"
        )
        resources = add(
            "<< /Font << /{} {} 0 R /{} {} 0 R /{} {} 0 R >> >>".format(
                FONT_REGULAR, font_regular, FONT_BOLD, font_bold,
                FONT_MONO, font_mono
            ).encode("latin-1")
        )

        pages_id = len(objects) + 1 + 2 * len(self.pages) + 1
        page_ids = []
        for ops in self.pages:
            stream = zlib.compress("\n".join(ops).encode("latin-1"))
            content_id = add(
                b"<< /Length " + str(len(stream)).encode() +
                b" /Filter /FlateDecode >>\nstream\n" + stream + b"\nendstream"
            )
            page_id = add(
                "<< /Type /Page /Parent {} 0 R /MediaBox [0 0 {:.2f} {:.2f}] "
                "/Resources {} 0 R /Contents {} 0 R >>".format(
                    pages_id, PAGE_W, PAGE_H, resources, content_id
                ).encode("latin-1")
            )
            page_ids.append(page_id)

        kids = " ".join("{} 0 R".format(i) for i in page_ids)
        actual_pages_id = add(
            "<< /Type /Pages /Count {} /Kids [{}] >>".format(
                len(page_ids), kids
            ).encode("latin-1")
        )
        info_id = add(
            "<< /Title ({}) /Author (MateLab) /Subject ({}) /Creator "
            "(tools/md2pdf.py) >>".format(
                escape(self.title), escape(self.subtitle)
            ).encode("latin-1")
        )
        catalog_id = add(
            "<< /Type /Catalog /Pages {} 0 R >>".format(
                actual_pages_id
            ).encode("latin-1")
        )

        out = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
        offsets = [0]
        for number, body in enumerate(objects, start=1):
            offsets.append(len(out))
            out += "{} 0 obj\n".format(number).encode("latin-1")
            out += body
            out += b"\nendobj\n"

        xref_at = len(out)
        out += "xref\n0 {}\n".format(len(objects) + 1).encode("latin-1")
        out += b"0000000000 65535 f \n"
        for offset in offsets[1:]:
            out += "{:010d} 00000 n \n".format(offset).encode("latin-1")
        out += "trailer\n<< /Size {} /Root {} 0 R /Info {} 0 R >>\n".format(
            len(objects) + 1, catalog_id, info_id
        ).encode("latin-1")
        out += "startxref\n{}\n%%EOF\n".format(xref_at).encode("latin-1")

        os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
        with open(path, "wb") as handle:
            handle.write(bytes(out))
        return len(out)


def convert(md_path, pdf_path, doc_title, subtitle):
    with open(md_path, encoding="utf-8") as handle:
        source = handle.read()

    blocks = parse_markdown(source)
    # El primer h1 ya va en la portada; se quita para no repetirlo.
    if blocks and blocks[0][0] == "h1":
        blocks = blocks[1:]
    while blocks and blocks[0][0] in ("h2", "hr") and blocks[0][0] == "hr":
        blocks = blocks[1:]

    pdf = Pdf(doc_title, subtitle)
    pdf.cover(subtitle, doc_title)
    pdf.render(blocks)
    size = pdf.save(pdf_path)
    print("  {:40s} {:2d} paginas  {:6.1f} KB".format(
        os.path.basename(pdf_path), len(pdf.pages), size / 1024.0
    ))


def main():
    if len(sys.argv) >= 4:
        convert(sys.argv[1], sys.argv[2], sys.argv[3],
                sys.argv[4] if len(sys.argv) > 4 else "")
        return 0

    jobs = [
        ("docs/MEMORIA_DESCRIPTIVA.md", "docs/pdf/MEMORIA_DESCRIPTIVA.pdf",
         "Memoria descriptiva", "Diseno, contenido y arquitectura"),
        ("docs/MANUAL_USUARIO.md", "docs/pdf/MANUAL_USUARIO.pdf",
         "Manual de usuario", "Para ninos, familias y profesorado"),
        ("docs/MANUAL_TECNICO.md", "docs/pdf/MANUAL_TECNICO.pdf",
         "Manual tecnico", "Arquitectura, compilacion y mantenimiento"),
    ]
    print("Generando PDF de MateLab")
    for md, pdf, title, subtitle in jobs:
        if not os.path.exists(md):
            print("  AVISO: falta {}".format(md))
            continue
        convert(md, pdf, title, subtitle)
    print("Listo.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
