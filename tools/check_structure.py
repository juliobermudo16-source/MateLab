"""
Segunda comprobacion estatica de MateLab.

Detecta tres cosas que el compilador encontraria enseguida pero que aqui
conviene cazar antes: ficheros truncados (llaves sin cerrar), recursos
referenciados que no existen y mini-juegos sin rama en el despachador.

Uso:  python tools/check_structure.py
"""

import os
import re
import sys

SRC = os.path.join("app", "src")
MAIN = os.path.join(SRC, "main")
RES = os.path.join(MAIN, "res")

LINE_COMMENT = re.compile(r"//[^\n]*")
BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.S)
RAW_STRING = re.compile(r'"""(?:.|\n)*?"""')
STRING = re.compile(r'"(?:[^"\\\n]|\\.)*"')
CHAR = re.compile(r"'(?:[^'\\]|\\.)'")


def strip_noise(source: str) -> str:
    """Quita comentarios, cadenas y caracteres para contar solo estructura."""
    text = BLOCK_COMMENT.sub(" ", source)
    text = LINE_COMMENT.sub(" ", text)
    text = RAW_STRING.sub('""', text)
    text = STRING.sub('""', text)
    text = CHAR.sub("''", text)
    return text


def kotlin_files():
    for base, _dirs, files in os.walk(SRC):
        for name in files:
            if name.endswith(".kt"):
                yield os.path.join(base, name)


def check_balance(problems):
    checked = 0
    for path in kotlin_files():
        with open(path, encoding="utf-8") as handle:
            text = strip_noise(handle.read())
        checked += 1
        for opener, closer, label in (("{", "}", "llaves"), ("(", ")", "parentesis")):
            delta = text.count(opener) - text.count(closer)
            if delta != 0:
                problems.append(
                    "{}: {} descuadradas ({:+d})".format(
                        os.path.relpath(path), label, delta
                    )
                )
    return checked


def check_resources(problems):
    existing = set()
    for base, _dirs, files in os.walk(RES):
        kind = os.path.basename(base).split("-")[0]
        for name in files:
            path = os.path.join(base, name)
            if kind == "values":
                # En values/ los recursos se declaran dentro del XML.
                with open(path, encoding="utf-8") as handle:
                    text = handle.read()
                for tag, res_name in re.findall(
                    r"<(color|string|style|dimen|bool|integer)\s+name=\"([\w.]+)\"", text
                ):
                    existing.add("{}/{}".format(tag, res_name))
            else:
                existing.add("{}/{}".format(kind, os.path.splitext(name)[0]))

    refs = set()
    for base, _dirs, files in os.walk(MAIN):
        for name in files:
            if not (name.endswith(".kt") or name.endswith(".xml")):
                continue
            with open(os.path.join(base, name), encoding="utf-8") as handle:
                text = handle.read()
            refs |= set(re.findall(r"R\.(raw|drawable|mipmap|string|color|xml)\.(\w+)", text))
            refs |= set(re.findall(r"@(raw|drawable|mipmap|string|color|xml)/(\w+)", text))

    for kind, name in sorted(refs):
        key = "{}/{}".format(kind, name)
        if key not in existing:
            problems.append("recurso ausente: @{}".format(key))
    return len(refs)


def check_game_dispatch(problems):
    content = os.path.join(
        MAIN, "java", "com", "matelab", "islas", "domain", "model", "Content.kt"
    )
    payloads = os.path.join(
        MAIN, "java", "com", "matelab", "islas", "domain", "model", "Payloads.kt"
    )
    host = os.path.join(
        MAIN, "java", "com", "matelab", "islas", "ui", "games", "GameHost.kt"
    )

    with open(content, encoding="utf-8") as handle:
        block = re.search(r"enum class GameKind \{(.*?)\n\}", handle.read(), re.S)
    kinds = set()
    if block:
        for line in block.group(1).splitlines():
            token = line.strip().rstrip(",")
            if token and re.fullmatch(r"[A-Z_]+", token):
                kinds.add(token)

    with open(payloads, encoding="utf-8") as handle:
        subclasses = set(re.findall(r"data class (\w+Payload)", handle.read()))

    with open(host, encoding="utf-8") as handle:
        host_text = handle.read()
    dispatched = set(re.findall(r"is (\w+Payload) -> \w+Game", host_text))
    labelled = set(re.findall(r"is (\w+Payload) ->", host_text)) - dispatched

    missing_dispatch = subclasses - dispatched
    if missing_dispatch:
        problems.append(
            "mini-juegos sin rama en ChallengeGame: {}".format(sorted(missing_dispatch))
        )
    missing_label = subclasses - (labelled | dispatched)
    if missing_label:
        problems.append("payloads sin etiqueta de actividad: {}".format(sorted(missing_label)))
    if len(kinds) != len(subclasses):
        problems.append(
            "GameKind tiene {} valores pero hay {} payloads".format(
                len(kinds), len(subclasses)
            )
        )
    return len(kinds), len(subclasses)


def main():
    problems = []
    files = check_balance(problems)
    refs = check_resources(problems)
    kinds, payloads = check_game_dispatch(problems)

    print("Ficheros Kotlin equilibrados: {}".format(files))
    print("Referencias a recursos comprobadas: {}".format(refs))
    print("GameKind: {} valores / Payloads: {}".format(kinds, payloads))

    if problems:
        print("\nPROBLEMAS ({}):".format(len(problems)))
        for problem in problems:
            print(" - " + problem)
        return 1

    print("\nSin problemas estructurales.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
