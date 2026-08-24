"""
Comprobacion estatica ligera del codigo Kotlin de MateLab.

No sustituye al compilador, pero detecta el error mas facil de cometer
cuando se escribe mucho codigo de golpe: importar un simbolo del propio
proyecto que en realidad no existe, o que vive en otro paquete.

Uso:  python tools/check_imports.py
"""

import os
import re
import sys
from collections import defaultdict

ROOT = os.path.join("app", "src")
BASE = "com.matelab.islas"

MODIFIERS = (
    r"(?:public\s+|internal\s+|private\s+|abstract\s+|sealed\s+|open\s+|data\s+"
    r"|value\s+|inline\s+|suspend\s+|operator\s+|infix\s+|expect\s+|actual\s+)*"
)

# Tipos: class / interface / object / enum class / typealias
DECL_TYPE = re.compile(
    r"^\s*(?:@\w+(?:\([^)]*\))?\s*)*" + MODIFIERS +
    r"(?:enum\s+class|annotation\s+class|class|interface|object|typealias)\s+"
    r"([A-Za-z_][A-Za-z0-9_]*)",
    re.MULTILINE,
)

# Funciones, con posibles parametros de tipo y receptor de extension:
#   fun nombre(...)            fun <T> nombre(...)
#   fun Receptor.nombre(...)   fun <T> Receptor<T>.nombre(...)
DECL_FUN = re.compile(
    r"^\s*(?:@\w+(?:\([^)]*\))?\s*)*" + MODIFIERS +
    r"fun\s+"
    r"(?:<[^>]*>\s*)?"
    r"(?:[A-Za-z_][\w.]*(?:<[^>]*>)?\s*\.\s*)?"
    r"([A-Za-z_][A-Za-z0-9_]*)\s*\(",
    re.MULTILINE,
)

# Propiedades, con posible receptor de extension.
DECL_PROP = re.compile(
    r"^\s*(?:@\w+(?:\([^)]*\))?\s*)*" + MODIFIERS +
    r"(?:val|var)\s+"
    r"(?:<[^>]*>\s*)?"
    r"(?:[A-Za-z_][\w.]*(?:<[^>]*>)?\s*\.\s*)?"
    r"([A-Za-z_][A-Za-z0-9_]*)",
    re.MULTILINE,
)

# Simbolos generados por el compilador de Android que no viven en el codigo.
GENERATED = {"R", "BuildConfig"}

PACKAGE = re.compile(r"^\s*package\s+([\w.]+)", re.MULTILINE)
IMPORT = re.compile(r"^\s*import\s+([\w.]+)(?:\s+as\s+\w+)?", re.MULTILINE)


def kotlin_files():
    for base, _dirs, files in os.walk(ROOT):
        for name in files:
            if name.endswith(".kt"):
                yield os.path.join(base, name)


def main():
    # paquete -> conjunto de simbolos declarados en el
    declared = defaultdict(set)
    # simbolo -> paquetes donde aparece
    where = defaultdict(set)
    files = list(kotlin_files())

    for path in files:
        with open(path, encoding="utf-8") as handle:
            text = handle.read()
        pkg_match = PACKAGE.search(text)
        if not pkg_match:
            print("AVISO: sin package -> {}".format(path))
            continue
        pkg = pkg_match.group(1)
        for pattern in (DECL_TYPE, DECL_FUN, DECL_PROP):
            for name in pattern.findall(text):
                declared[pkg].add(name)
                where[name].add(pkg)

    problems = []
    for path in files:
        with open(path, encoding="utf-8") as handle:
            text = handle.read()
        for imported in IMPORT.findall(text):
            if not imported.startswith(BASE):
                continue
            pkg, _, symbol = imported.rpartition(".")
            if symbol == "*" or symbol in GENERATED:
                continue
            if symbol in declared.get(pkg, ()):
                continue
            # Puede ser un miembro anidado: paquete.Clase.Miembro
            parent_pkg, _, parent = pkg.rpartition(".")
            if parent in declared.get(parent_pkg, ()):
                continue
            hint = ""
            if symbol in where:
                hint = "  (existe en: {})".format(", ".join(sorted(where[symbol])))
            problems.append(
                "{}\n    import {}{}".format(
                    os.path.relpath(path), imported, hint
                )
            )

    print("Ficheros Kotlin analizados: {}".format(len(files)))
    print("Simbolos declarados: {}".format(sum(len(v) for v in declared.values())))

    if problems:
        print("\nIMPORTS QUE NO RESUELVEN ({}):\n".format(len(problems)))
        for problem in problems:
            print(" - " + problem)
        return 1

    print("\nTodos los imports internos resuelven correctamente.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
