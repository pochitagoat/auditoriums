"""Подготовка переноса старых фотографий: копии файлов, manifest и отдельный SQL.

Скрипт не подключается к БД и не изменяет исходные файлы. CSV содержит id,file_path.
"""
import argparse
import csv
import hashlib
import json
import shutil
import secrets
from pathlib import Path


def sql_string(value):
    return "'" + value.replace("'", "''") + "'"


def prepare(csv_file, source_root, target_root, output_sql):
    source_root = source_root.resolve(strict=True)
    target_root = target_root.resolve()
    if output_sql.exists():
        raise ValueError("Файл SQL уже существует")
    planned = []
    ids = set()
    with csv_file.open(encoding="utf-8-sig", newline="") as handle:
        for row in csv.DictReader(handle):
            photo_id = int(row["id"])
            if photo_id <= 0 or photo_id in ids:
                raise ValueError(f"Недопустимый или повторный id: {photo_id}")
            ids.add(photo_id)
            old = row["file_path"]
            original = Path(old)
            source = (original if original.is_absolute() else source_root / original).resolve(strict=True)
            if not source.is_relative_to(source_root) or not source.is_file():
                raise ValueError(f"Файл фотографии {photo_id} находится вне исходного каталога")
            content = source.read_bytes()
            if content.startswith(b"\x89PNG\r\n\x1a\n"):
                extension, mime = ".png", "image/png"
            elif content.startswith(b"\xff\xd8\xff"):
                extension, mime = ".jpg", "image/jpeg"
            else:
                raise ValueError(f"Неизвестный формат фотографии {photo_id}")
            key = f"legacy/{photo_id}{extension}"
            destination = target_root / key
            if destination.exists():
                raise ValueError(f"Файл назначения уже существует для фотографии {photo_id}")
            planned.append((photo_id, old, source, destination, key, mime, hashlib.sha256(content).hexdigest()))
    statements = ["BEGIN;", "SET LOCAL standard_conforming_strings = on;"]
    manifest = []
    for photo_id, old, source, destination, key, mime, checksum in planned:
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, destination)
        if hashlib.sha256(destination.read_bytes()).hexdigest() != checksum:
            raise ValueError(f"Не совпала контрольная сумма фотографии {photo_id}")
        delimiter = "$photo_" + secrets.token_hex(12) + "$"
        while delimiter in old:
            delimiter = "$photo_" + secrets.token_hex(12) + "$"
        statements.append("DO " + delimiter + " BEGIN IF NOT EXISTS (SELECT 1 FROM auditorium_photo WHERE id = "
                          + str(photo_id) + " AND file_path = " + sql_string(old)
                          + ") THEN RAISE EXCEPTION 'Photo metadata changed: " + str(photo_id) + "'; END IF; END " + delimiter + ";")
        statements.append("UPDATE auditorium_photo SET file_path = " + sql_string(key)
                          + ", content_type = " + sql_string(mime) + ", version = version + 1 WHERE id = " + str(photo_id) + ";")
        manifest.append({"id": photo_id, "oldPath": old, "key": key, "sha256": checksum})
    statements.append("COMMIT;")
    output_sql.parent.mkdir(parents=True, exist_ok=True)
    output_sql.write_text("\n".join(statements) + "\n", encoding="utf-8")
    output_sql.with_suffix(".manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Подготовлены {len(planned)} файлов. Проверьте SQL и manifest перед применением.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", required=True, type=Path)
    parser.add_argument("--source-root", required=True, type=Path)
    parser.add_argument("--target-root", required=True, type=Path)
    parser.add_argument("--sql-output", required=True, type=Path)
    options = parser.parse_args()
    prepare(options.csv, options.source_root, options.target_root, options.sql_output)
