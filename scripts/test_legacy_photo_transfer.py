import csv
import tempfile
import unittest
from pathlib import Path
from migrate_legacy_photos import prepare


class LegacyPhotoTransferTests(unittest.TestCase):
    def test_copies_relative_and_absolute_paths_without_changing_originals(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            original = directory / "old"
            original.mkdir()
            image = b"\x89PNG\r\n\x1a\nfixture"
            (original / "one.png").write_bytes(image)
            (original / "quote'$$.png").write_bytes(image)
            csv_file = directory / "photos.csv"
            with csv_file.open("w", newline="", encoding="utf-8") as handle:
                writer = csv.writer(handle)
                writer.writerow(["id", "file_path"])
                writer.writerow([1, "one.png"])
                writer.writerow([2, str(original / "quote'$$.png")])
            sql = directory / "paths.sql"
            prepare(csv_file, original, directory / "new", sql)
            self.assertEqual(image, (directory / "new/legacy/1.png").read_bytes())
            self.assertEqual(image, (directory / "new/legacy/2.png").read_bytes())
            self.assertEqual(image, (original / "quote'$$.png").read_bytes())
            self.assertIn("quote''$$.png", sql.read_text(encoding="utf-8"))
            self.assertTrue(sql.with_suffix(".manifest.json").is_file())

    def test_missing_file_stops_before_any_copies_or_sql(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "one.png").write_bytes(b"\x89PNG\r\n\x1a\nfixture")
            (root / "photos.csv").write_text("id,file_path\n1,one.png\n2,missing.png\n", encoding="utf-8")
            with self.assertRaises(FileNotFoundError):
                prepare(root / "photos.csv", root, root / "new", root / "paths.sql")
            self.assertFalse((root / "new").exists())
            self.assertFalse((root / "paths.sql").exists())

    def test_outside_path_is_rejected_and_existing_destination_is_not_overwritten(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / "source"
            source.mkdir()
            (root / "outside.png").write_bytes(b"\x89PNG\r\n\x1a\nfixture")
            (root / "photos.csv").write_text("id,file_path\n1,../outside.png\n", encoding="utf-8")
            with self.assertRaises(ValueError):
                prepare(root / "photos.csv", source, root / "new", root / "paths.sql")
            (source / "one.png").write_bytes(b"\x89PNG\r\n\x1a\nfixture")
            destination = root / "new/legacy"
            destination.mkdir(parents=True)
            (destination / "1.png").write_text("preserve", encoding="utf-8")
            (root / "photos.csv").write_text("id,file_path\n1,one.png\n", encoding="utf-8")
            with self.assertRaises(ValueError):
                prepare(root / "photos.csv", source, root / "new", root / "paths.sql")
            self.assertEqual("preserve", (destination / "1.png").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
