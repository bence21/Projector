#!/usr/bin/env python3
"""Report total and per-language song counts from a Projector H2 database."""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
from pathlib import Path

H2_VERSION = "1.4.193"
DEFAULT_DB_RELATIVE = Path("data") / "projector"


class H2QueryError(RuntimeError):
    pass


def default_db_path() -> Path:
    return Path(__file__).resolve().parents[1] / DEFAULT_DB_RELATIVE


def resolve_db_base(path: Path) -> Path:
    """Return the JDBC base path (without .mv.db) for an H2 database file or folder."""
    path = path.resolve()
    if path.is_file() and path.name.lower().endswith(".mv.db"):
        return path.parent / path.name[:-6]
    if path.is_dir():
        candidate = path / "projector.mv.db"
        if candidate.is_file():
            return path / "projector"
        raise FileNotFoundError(f"No projector.mv.db found in directory: {path}")
    mv_db = Path(f"{path}.mv.db")
    if mv_db.is_file():
        return path
    raise FileNotFoundError(f"H2 database not found: {path}")


def find_h2_jar(explicit: str | None) -> Path:
    if explicit:
        jar = Path(explicit)
        if not jar.is_file():
            raise FileNotFoundError(f"H2 jar not found: {jar}")
        return jar

    env_jar = os.environ.get("H2_JAR")
    if env_jar:
        jar = Path(env_jar)
        if jar.is_file():
            return jar
        raise FileNotFoundError(f"H2_JAR points to a missing file: {jar}")

    cache_root = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1" / "com.h2database" / "h2"
    version_dir = cache_root / H2_VERSION
    if version_dir.is_dir():
        matches = sorted(version_dir.glob(f"*/h2-{H2_VERSION}.jar"))
        if matches:
            return matches[0]

    raise FileNotFoundError(
        f"Could not locate h2-{H2_VERSION}.jar. "
        "Run './gradlew dependencies' in projector-desktop or pass --h2-jar."
    )


def build_db_url(db_base: Path) -> str:
    return f"jdbc:h2:{db_base.as_posix()};AUTO_SERVER=TRUE"


def run_h2_sql(h2_jar: Path, db_base: Path, sql: str) -> str:
    db_url = build_db_url(db_base)
    command = [
        "java",
        "-Dfile.encoding=UTF-8",
        "-cp",
        str(h2_jar),
        "org.h2.tools.Shell",
        "-url",
        db_url,
        "-sql",
        sql,
    ]
    completed = subprocess.run(
        command,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=False,
    )
    output = (completed.stdout or "") + (completed.stderr or "")
    if completed.returncode != 0 or "Exception" in output or "Error:" in output:
        if "Database may be already in use" in output or "The file is locked" in output:
            raise H2QueryError(
                "Database is locked. If Projector is running, restart it so AUTO_SERVER=TRUE "
                "is active, or close the app and retry."
            )
        raise H2QueryError(output.strip() or f"H2 query failed with exit code {completed.returncode}")
    return output


def parse_scalar(output: str) -> int:
    for line in output.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("(") or "|" in stripped:
            continue
        if stripped.isdigit():
            return int(stripped)
    raise H2QueryError(f"Could not parse scalar result:\n{output}")


def parse_table(output: str, expected_columns: int) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    headers: list[str] | None = None
    for line in output.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("("):
            if headers and stripped.startswith("("):
                break
            continue
        if "|" not in stripped:
            continue
        cells = [cell.strip() for cell in stripped.split("|")]
        if headers is None:
            headers = [cell.lower() for cell in cells]
            continue
        if len(cells) != expected_columns:
            if expected_columns == 3 and len(cells) >= 3:
                cells = [cells[0], " | ".join(cells[1:-1]), cells[-1]]
            elif expected_columns == 2 and len(cells) >= 2:
                cells = [cells[0], cells[-1]]
            else:
                raise H2QueryError(f"Unexpected H2 row ({len(cells)} columns): {stripped}")
        rows.append(dict(zip(headers, cells)))
    if headers is None:
        raise H2QueryError(f"Could not parse table result:\n{output}")
    return rows


def fetch_counts(h2_jar: Path, db_base: Path) -> tuple[int, list[dict[str, str]], int]:
    total_output = run_h2_sql(h2_jar, db_base, "SELECT COUNT(*) FROM SONG;")
    total = parse_scalar(total_output)

    languages_output = run_h2_sql(
        h2_jar,
        db_base,
        """
        SELECT ID, NATIVENAME, ENGLISHNAME
        FROM LANGUAGE
        ORDER BY NATIVENAME, ENGLISHNAME, ID;
        """.strip(),
    )
    languages = parse_table(languages_output, expected_columns=3)

    counts_output = run_h2_sql(
        h2_jar,
        db_base,
        "SELECT LANGUAGE_ID, COUNT(*) AS SONG_COUNT FROM SONG GROUP BY LANGUAGE_ID;",
    )
    count_rows = parse_table(counts_output, expected_columns=2)
    counts_by_language_id = {
        row["language_id"]: int(row["song_count"])
        for row in count_rows
        if row.get("language_id")
    }

    by_language = [
        {
            "native_name": language["nativename"],
            "english_name": language["englishname"],
            "song_count": str(counts_by_language_id.get(language["id"], 0)),
        }
        for language in languages
    ]

    unassigned_output = run_h2_sql(
        h2_jar,
        db_base,
        "SELECT COUNT(*) FROM SONG WHERE LANGUAGE_ID IS NULL;",
    )
    unassigned = parse_scalar(unassigned_output)
    return total, by_language, unassigned


def print_report(db_base: Path, total: int, by_language: list[dict[str, str]], unassigned: int) -> None:
    print(f"Database: {db_base}.mv.db")
    print(f"Total songs: {total}")
    print()
    print("Songs by language:")
    print(f"{'Count':>8}  {'Native name':<28}  English name")
    print("-" * 72)
    rows = [row for row in by_language if int(row["song_count"]) > 0]
    if unassigned:
        rows.append(
            {
                "native_name": "(no language)",
                "english_name": "-",
                "song_count": str(unassigned),
            }
        )
    rows.sort(key=lambda row: int(row["song_count"]), reverse=True)
    for row in rows:
        count = int(row["song_count"])
        native = row["native_name"]
        english = row["english_name"]
        print(f"{count:8d}  {native:<28}  {english}")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Print total song count and per-language song counts from a Projector H2 database.",
    )
    parser.add_argument(
        "--db",
        type=Path,
        default=default_db_path(),
        help="Path to projector.mv.db or its parent data folder (default: projector-desktop/data/projector)",
    )
    parser.add_argument(
        "--h2-jar",
        type=Path,
        default=None,
        help="Path to h2.jar (default: Gradle cache or H2_JAR env var)",
    )
    return parser


def configure_stdout() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        except (AttributeError, ValueError, OSError):
            pass


def main() -> int:
    configure_stdout()
    parser = build_parser()
    args = parser.parse_args()

    try:
        db_base = resolve_db_base(args.db)
        h2_jar = find_h2_jar(str(args.h2_jar) if args.h2_jar else None)
        total, by_language, unassigned = fetch_counts(h2_jar, db_base)
    except (FileNotFoundError, H2QueryError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 1

    print_report(db_base, total, by_language, unassigned)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
