"""
Genera los efectos de sonido locales de MateLab en app/src/main/res/raw.

No usa ninguna libreria externa: solo `math`, `struct` y `wave` de la
biblioteca estandar. Los sonidos son cortos, suaves y con envolvente para
que no resulten estridentes en el movil de un nino.

Uso:  python tools/gen_sounds.py
"""

import math
import os
import struct
import wave

SAMPLE_RATE = 22050
OUT_DIR = os.path.join("app", "src", "main", "res", "raw")

# Notas en Hz (temperamento igual, A4 = 440)
NOTES = {
    "C4": 261.63, "D4": 293.66, "E4": 329.63, "F4": 349.23, "G4": 392.00,
    "A4": 440.00, "B4": 493.88, "C5": 523.25, "D5": 587.33, "E5": 659.25,
    "F5": 698.46, "G5": 783.99, "A5": 880.00, "C6": 1046.50,
    "E3": 164.81, "G3": 196.00, "A3": 220.00,
}


def envelope(i, total, attack=0.08, release=0.45):
    """Envolvente suave para evitar chasquidos al empezar y al acabar."""
    a = int(total * attack) or 1
    r = int(total * release) or 1
    if i < a:
        return i / a
    if i > total - r:
        return max(0.0, (total - i) / r)
    return 1.0


def tone(freq, ms, amplitude=0.32, harmonics=(1.0, 0.28, 0.12), vibrato=0.0):
    """Un tono con unos pocos armonicos: suena mas calido que un seno puro."""
    total = int(SAMPLE_RATE * ms / 1000.0)
    out = []
    for i in range(total):
        t = i / SAMPLE_RATE
        f = freq * (1.0 + vibrato * math.sin(2 * math.pi * 5.5 * t))
        value = 0.0
        for n, weight in enumerate(harmonics, start=1):
            value += weight * math.sin(2 * math.pi * f * n * t)
        value /= sum(harmonics)
        out.append(value * amplitude * envelope(i, total))
    return out


def noise_click(ms=45, amplitude=0.18):
    """Clic breve: ruido filtrado paso bajo muy sencillo."""
    total = int(SAMPLE_RATE * ms / 1000.0)
    out = []
    prev = 0.0
    seed = 12345
    for i in range(total):
        seed = (1103515245 * seed + 12345) % (2 ** 31)
        white = (seed / (2 ** 30)) - 1.0
        prev = prev * 0.72 + white * 0.28
        out.append(prev * amplitude * envelope(i, total, attack=0.02, release=0.8))
    return out


def silence(ms):
    return [0.0] * int(SAMPLE_RATE * ms / 1000.0)


def mix(a, b):
    """Suma dos pistas alineadas al principio."""
    n = max(len(a), len(b))
    out = []
    for i in range(n):
        va = a[i] if i < len(a) else 0.0
        vb = b[i] if i < len(b) else 0.0
        out.append(va + vb)
    return out


def sequence(*chunks):
    out = []
    for c in chunks:
        out.extend(c)
    return out


def write_wav(name, samples):
    path = os.path.join(OUT_DIR, name)
    frames = bytearray()
    for s in samples:
        v = max(-1.0, min(1.0, s))
        frames += struct.pack("<h", int(v * 32000))
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        w.writeframes(bytes(frames))
    print("  {:22s} {:6.1f} KB".format(name, len(frames) / 1024.0))


def build():
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generando efectos de sonido de MateLab en", OUT_DIR)

    # Toque de interfaz: clic corto y discreto.
    write_wav("sfx_tap.wav", mix(noise_click(40), tone(NOTES["A4"], 45, 0.14)))

    # Acierto: dos notas que suben.
    write_wav("sfx_correct.wav", sequence(
        tone(NOTES["E5"], 95, 0.30),
        tone(NOTES["A5"], 150, 0.30),
    ))

    # Fallo: dos notas graves que bajan, suaves y sin dramatismo.
    write_wav("sfx_wrong.wav", sequence(
        tone(NOTES["A3"], 110, 0.24, harmonics=(1.0, 0.15)),
        tone(NOTES["E3"], 170, 0.22, harmonics=(1.0, 0.15)),
    ))

    # Estrella conseguida: arpegio de tres notas.
    write_wav("sfx_star.wav", sequence(
        tone(NOTES["C5"], 80, 0.26),
        tone(NOTES["E5"], 80, 0.26),
        tone(NOTES["G5"], 190, 0.28),
    ))

    # Desbloqueo de cristal: campanilla con cola.
    write_wav("sfx_unlock.wav", mix(
        sequence(
            tone(NOTES["G4"], 90, 0.24),
            tone(NOTES["C5"], 90, 0.26),
            tone(NOTES["E5"], 90, 0.26),
            tone(NOTES["G5"], 320, 0.28, vibrato=0.004),
        ),
        sequence(silence(180), tone(NOTES["C6"], 340, 0.10)),
    ))

    # Subida de nivel: pequena fanfarria.
    write_wav("sfx_level.wav", sequence(
        tone(NOTES["C5"], 90, 0.28),
        tone(NOTES["D5"], 90, 0.28),
        tone(NOTES["E5"], 90, 0.28),
        tone(NOTES["G5"], 110, 0.30),
        tone(NOTES["C6"], 380, 0.30, vibrato=0.005),
    ))

    print("Listo.")


if __name__ == "__main__":
    build()
