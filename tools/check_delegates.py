"""
Caza dos errores de Kotlin/Compose que el compilador detecto en CI y que las
comprobaciones anteriores no veian:

1. `val x by ...` / `var x by ...` sin importar androidx.compose.runtime.getValue
   (y setValue para var). Sin esos imports el delegado no compila.

2. Llamadas a funciones de extension con nombre completamente cualificado,
   por ejemplo androidx.compose.foundation.gestures.detectTapGestures { }.
   Kotlin NO permite eso con receptor implicito: hay que importarlas.

Uso:  python tools/check_delegates.py
"""

import os
import re
import sys

SRC = os.path.join("app", "src")

# Delegados: val/var nombre by ...
DELEGATE_VAL = re.compile(r"^\s*val\s+\w+\s+by\s+", re.M)
DELEGATE_VAR = re.compile(r"^\s*var\s+\w+\s+by\s+", re.M)

# Delegados que traen su propio getValue y no necesitan el import de runtime.
SELF_CONTAINED = re.compile(
    r"\bby\s+(lazy|viewModels|activityViewModels|Delegates\.|remember\s*\{\s*mutableStateOf)",
)

# Extensiones llamadas con nombre completo (paquete.funcion { ... } o (...))
QUALIFIED_EXT = re.compile(
    r"\bandroidx\.compose\.(?:foundation|ui|runtime)[\w.]*\.([a-z]\w+)\s*[({]"
)

NEEDS_IMPORT = {
    "detectTapGestures", "detectDragGestures", "detectTransformGestures",
    "detectVerticalDragGestures", "detectHorizontalDragGestures",
    "awaitEachGesture", "pointerInput", "clickable", "combinedClickable",
}


def main():
    problems = []
    scanned = 0

    for base, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".kt"):
                continue
            path = os.path.join(base, name)
            rel = os.path.relpath(path)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            scanned += 1

            has_get = "import androidx.compose.runtime.getValue" in text
            has_set = "import androidx.compose.runtime.setValue" in text

            for match in DELEGATE_VAL.finditer(text):
                line = text[match.start():text.find("\n", match.start())]
                if SELF_CONTAINED.search(line):
                    continue
                if not has_get:
                    number = text[:match.start()].count("\n") + 1
                    problems.append(
                        "{}:{} delegado 'val by' sin import getValue\n"
                        "      {}".format(rel, number, line.strip())
                    )
                    break

            for match in DELEGATE_VAR.finditer(text):
                number = text[:match.start()].count("\n") + 1
                line = text[match.start():text.find("\n", match.start())].strip()
                if not has_get:
                    problems.append(
                        "{}:{} delegado 'var by' sin import getValue\n      {}".format(
                            rel, number, line
                        )
                    )
                    break
                if not has_set:
                    problems.append(
                        "{}:{} delegado 'var by' sin import setValue\n      {}".format(
                            rel, number, line
                        )
                    )
                    break

            for match in QUALIFIED_EXT.finditer(text):
                function = match.group(1)
                if function in NEEDS_IMPORT:
                    number = text[:match.start()].count("\n") + 1
                    problems.append(
                        "{}:{} extension llamada con nombre completo: {}\n"
                        "      Kotlin exige importarla".format(rel, number, function)
                    )

    print("Ficheros Kotlin analizados: {}".format(scanned))
    if problems:
        print("\nPROBLEMAS ({}):".format(len(problems)))
        for problem in problems:
            print(" - " + problem)
        return 1
    print("\nSin delegados ni extensiones mal resueltos.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
