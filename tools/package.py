"""
Prepara la carpeta deliverables/ de MateLab.

Crea el ZIP del codigo fuente con la raiz directa exigida (app/, database/,
docs/, gradle/, build.gradle.kts, ...), copia los PDF y calcula el SHA-256
de cada entregable.

Uso:  python tools/package.py
"""

import hashlib
import os
import shutil
import zipfile

VERSION = "1.0.0"
OUT = "deliverables"
ZIP_NAME = "MateLab-v{}-source.zip".format(VERSION)

# Lo que entra en el ZIP, en la raiz directa (nunca PROYECTO/PROYECTO/app).
INCLUDE_DIRS = ["app", "database", "docs", "gradle", ".github", "tools"]
INCLUDE_FILES = [
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "README.md",
    ".gitignore",
]

# Lo que nunca debe viajar dentro del ZIP.
EXCLUDE_DIRS = {".gradle", "build", ".idea", ".git", "__pycache__", ".cxx"}
EXCLUDE_EXT = {".apk", ".aab", ".zip", ".iml", ".hprof", ".pyc"}


def keep(path):
    parts = path.replace("\\", "/").split("/")
    if any(part in EXCLUDE_DIRS for part in parts):
        return False
    return os.path.splitext(path)[1].lower() not in EXCLUDE_EXT


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            digest.update(chunk)
    return digest.hexdigest()


def build_zip():
    os.makedirs(OUT, exist_ok=True)
    target = os.path.join(OUT, ZIP_NAME)
    if os.path.exists(target):
        os.remove(target)

    count = 0
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for folder in INCLUDE_DIRS:
            if not os.path.isdir(folder):
                continue
            for base, dirs, files in os.walk(folder):
                dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
                for name in files:
                    path = os.path.join(base, name)
                    if not keep(path):
                        continue
                    archive.write(path, path.replace("\\", "/"))
                    count += 1
        for name in INCLUDE_FILES:
            if os.path.exists(name):
                archive.write(name, name)
                count += 1
    return target, count


def copy_pdfs():
    copied = []
    for name in (
        "MEMORIA_DESCRIPTIVA.pdf",
        "MANUAL_USUARIO.pdf",
        "MANUAL_TECNICO.pdf",
    ):
        source = os.path.join("docs", "pdf", name)
        if os.path.exists(source):
            destination = os.path.join(OUT, name)
            shutil.copy2(source, destination)
            copied.append(destination)
    return copied


def verify_zip(path):
    """Comprueba que la raiz del ZIP es la correcta y que no hay anidamiento."""
    problems = []
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        bad = archive.testzip()
        if bad:
            problems.append("entrada corrupta: {}".format(bad))
        roots = {n.split("/")[0] for n in names}
        expected = set(INCLUDE_DIRS) | set(INCLUDE_FILES)
        extra = roots - expected
        if extra:
            problems.append("raices inesperadas: {}".format(sorted(extra)))
        for needed in ("app/build.gradle.kts", "settings.gradle.kts",
                       "database/schema.sql", "gradlew"):
            if needed not in names:
                problems.append("falta {}".format(needed))
        if any(n.startswith("MateLab/") for n in names):
            problems.append("el ZIP esta anidado")
    return problems


def main():
    print("Empaquetando MateLab v{}\n".format(VERSION))

    zip_path, count = build_zip()
    problems = verify_zip(zip_path)
    pdfs = copy_pdfs()

    entries = [zip_path] + pdfs
    print("{:<38} {:>9}  {}".format("ENTREGABLE", "TAMANO", "SHA-256"))
    print("-" * 100)
    lines = []
    for path in entries:
        size = os.path.getsize(path)
        digest = sha256(path)
        print("{:<38} {:>8.1f}K  {}".format(
            os.path.basename(path), size / 1024.0, digest
        ))
        lines.append((os.path.basename(path), size, digest))

    print("\nFicheros dentro del ZIP: {}".format(count))
    if problems:
        print("\nPROBLEMAS EN EL ZIP:")
        for problem in problems:
            print(" - {}".format(problem))
        return 1
    print("Estructura del ZIP correcta (raiz directa, sin anidamiento).")

    # Deja constancia para el informe de compilacion.
    with open(os.path.join(OUT, "SHA256SUMS.txt"), "w", encoding="utf-8") as handle:
        for name, size, digest in lines:
            handle.write("{}  {}  ({} bytes)\n".format(digest, name, size))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
