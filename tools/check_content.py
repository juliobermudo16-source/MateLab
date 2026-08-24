"""
Comprueba las invariantes de contenido que verifican los tests de Kotlin,
leyendo directamente los ficheros del catalogo.

Sirve para no descubrir en CI que una explicacion se paso de largo.

Uso:  python tools/check_content.py
"""

import os
import re
import sys

CATALOG_DIR = os.path.join(
    "app", "src", "main", "java", "com", "matelab", "islas", "domain", "content"
)

LIMITS = {"prompt": 130, "explanation": 260, "hint": 130}

FIELD = re.compile(
    r'(prompt|explanation|hint)\s*=\s*((?:"(?:[^"\\]|\\.)*"\s*\+\s*)*"(?:[^"\\]|\\.)*")',
    re.S,
)
CHALLENGE_ID = re.compile(r'id\s*=\s*"(c_[\w]+)"')
STRING_PART = re.compile(r'"((?:[^"\\]|\\.)*)"')


def join_string(literal: str) -> str:
    """Une un literal Kotlin partido en varias lineas con el operador +."""
    return "".join(STRING_PART.findall(literal))


def main():
    problems = []
    counts = {"retos": 0, "misiones": 0}
    quiz = 0

    for name in sorted(os.listdir(CATALOG_DIR)):
        if not name.startswith("Catalog") or not name.endswith(".kt"):
            continue
        path = os.path.join(CATALOG_DIR, name)
        with open(path, encoding="utf-8") as handle:
            text = handle.read()

        ids = CHALLENGE_ID.findall(text)
        counts["retos"] += len(ids)
        counts["misiones"] += len(re.findall(r'id\s*=\s*"(m_[\w]+)"', text))
        quiz += len(re.findall(r"GameKind\.QUIZ", text))

        # Los campos aparecen en el mismo orden que los retos.
        for field, literal in FIELD.findall(text):
            value = join_string(literal)
            limit = LIMITS[field]
            if len(value) > limit:
                problems.append(
                    "{}: {} de {} caracteres (limite {}) -> {}...".format(
                        name, field, len(value), limit, value[:60]
                    )
                )
            if not value.strip():
                problems.append("{}: {} vacio".format(name, field))

        duplicates = [i for i in set(ids) if ids.count(i) > 1]
        if duplicates:
            problems.append("{}: ids de reto repetidos {}".format(name, duplicates))

    total = counts["retos"]
    print("Misiones: {}".format(counts["misiones"]))
    print("Retos: {}".format(total))
    if total:
        print("Retos de eleccion: {} ({} %)".format(quiz, quiz * 100 // total))
        if quiz * 100 // total >= 50:
            problems.append("Mas del 50 % del contenido son retos de eleccion")

    if problems:
        print("\nPROBLEMAS ({}):".format(len(problems)))
        for problem in problems:
            print(" - " + problem)
        return 1

    print("\nContenido dentro de los limites.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
