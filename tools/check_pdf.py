"""
Validador minimo de los PDF generados por tools/md2pdf.py.

Comprueba la cabecera, que startxref apunte de verdad a la tabla xref, que
cada desplazamiento caiga sobre su objeto y que el catalogo y las paginas
esten enlazados. Es lo que impediria que un visor abriese el fichero.

Uso:  python tools/check_pdf.py docs/pdf/*.pdf
"""

import glob
import re
import sys


def check(path):
    with open(path, "rb") as handle:
        data = handle.read()

    problems = []

    if not data.startswith(b"%PDF-1."):
        problems.append("no empieza por %PDF-1.x")
    if not data.rstrip().endswith(b"%%EOF"):
        problems.append("no termina en %%EOF")

    match = re.search(rb"startxref\s+(\d+)\s+%%EOF\s*$", data)
    if not match:
        problems.append("falta startxref al final")
        return problems, 0

    xref_at = int(match.group(1))
    if data[xref_at:xref_at + 4] != b"xref":
        problems.append("startxref no apunta a la tabla xref")
        return problems, 0

    header = re.match(rb"xref\s+0\s+(\d+)\s+", data[xref_at:])
    if not header:
        problems.append("cabecera de xref ilegible")
        return problems, 0

    count = int(header.group(1))
    entries = re.findall(rb"(\d{10}) (\d{5}) ([nf])", data[xref_at:])
    if len(entries) != count:
        problems.append(
            "xref declara {} objetos y contiene {}".format(count, len(entries))
        )

    for number, (offset, _gen, kind) in enumerate(entries):
        if kind == b"f":
            continue
        position = int(offset)
        expected = "{} 0 obj".format(number).encode()
        if data[position:position + len(expected)] != expected:
            problems.append(
                "el objeto {} deberia estar en {} y hay {!r}".format(
                    number, position, data[position:position + 20]
                )
            )

    if b"/Type /Catalog" not in data:
        problems.append("sin catalogo")
    pages = len(re.findall(rb"/Type /Page[^s]", data))
    if pages == 0:
        problems.append("sin paginas")

    trailer = re.search(rb"/Size (\d+)", data)
    if trailer and int(trailer.group(1)) != count:
        problems.append("el trailer y la xref no coinciden en el numero de objetos")

    return problems, pages


def main():
    targets = sys.argv[1:] or glob.glob("docs/pdf/*.pdf")
    if not targets:
        print("No hay PDF que comprobar.")
        return 1

    failed = 0
    for path in sorted(targets):
        problems, pages = check(path)
        if problems:
            failed += 1
            print("FALLO  {}".format(path))
            for problem in problems:
                print("        - {}".format(problem))
        else:
            print("OK     {:44s} {} paginas".format(path, pages))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
