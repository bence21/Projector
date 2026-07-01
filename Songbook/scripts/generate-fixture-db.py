#!/usr/bin/env python3
"""Build minimal androidTest songbook.db from Songbook API data."""

from __future__ import annotations

import json
import re
import shutil
import sqlite3
import sys
import unicodedata
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_API_BASE = "http://192.168.1.134:8081"
HUNGARIAN_UUID = "5a2d253b8c270b37345af0c3"
SEARCH_QUERY_367 = "367"
SEARCH_QUERY_BAPTISTA = "baptista"
MIN_367_RESULTS = 6


def strip_accents(value: str) -> str:
    normalized = unicodedata.normalize("NFD", value)
    return "".join(ch for ch in normalized if unicodedata.category(ch) != "Mn")


def fetch_json(url: str) -> object:
    with urllib.request.urlopen(url, timeout=120) as response:
        return json.load(response)


def iso_date(epoch_ms: int | None) -> str | None:
    if epoch_ms is None:
        return None
    timestamp = datetime.fromtimestamp(epoch_ms / 1000, tz=timezone.utc)
    return timestamp.strftime("%Y-%m-%d %H:%M:%S") + ".000000"


def song_matches_query(song: dict, collection: dict, element: dict, query: str) -> bool:
    stripped_query = strip_accents(query.lower())
    title = strip_accents((song.get("title") or "").lower())
    if stripped_query in title:
        return True
    collection_name = strip_accents((collection.get("name") or "").lower())
    ordinal = (element.get("ordinalNumber") or "").lower()
    if stripped_query in collection_name:
        return True
    if stripped_query in ordinal:
        return True
    ordinal_digits = re.sub(r"[^0-9]*", "", ordinal)
    query_digits = re.sub(r"[^0-9]*", "", stripped_query)
    if query_digits and query_digits == ordinal_digits:
        return True
    return False


def choose_fixture_match(matches_367: list[tuple[dict, dict, dict]]) -> tuple[dict, dict, dict]:
    for match in matches_367:
        _song, collection, _element = match
        if "baptista" in strip_accents(collection.get("name", "").lower()):
            return match
    if not matches_367:
        raise RuntimeError("No songs match search query 367")
    return matches_367[0]


def create_schema(connection: sqlite3.Connection) -> None:
  connection.executescript(
      """
      CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT);
      INSERT INTO android_metadata VALUES ('en_US');
      CREATE TABLE language (
          englishName VARCHAR,
          nativeName VARCHAR,
          selected SMALLINT,
          selectedForDownload SMALLINT,
          favouriteSongDate VARCHAR,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE song (
          title VARCHAR,
          strippedTitle VARCHAR,
          createdDate VARCHAR,
          modifiedDate VARCHAR,
          language_id BIGINT,
          lastAccessed VARCHAR,
          accessedTimes BIGINT,
          accessedTimeAverage BIGINT,
          versionGroup VARCHAR,
          youtubeUrl VARCHAR,
          views BIGINT,
          verseOrder VARCHAR,
          favourites BIGINT,
          asDeleted SMALLINT,
          savedOnlyToDevice SMALLINT,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE songverse (
          text VARCHAR,
          strippedText VARCHAR,
          sectionType VARCHAR,
          song_id BIGINT,
          ordinalNumber INTEGER,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE songcollection (
          createdDate VARCHAR,
          modifiedDate VARCHAR,
          name VARCHAR,
          language_id BIGINT,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE songcollectionelement (
          ordinalNumber VARCHAR,
          songUuid VARCHAR,
          songCollection_id BIGINT,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE favouritesong (
          song_id BIGINT,
          createdDate VARCHAR,
          modifiedDate VARCHAR,
          serverModifiedDate VARCHAR,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE queuesong (
          song_id BIGINT,
          queueIndex INTEGER,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE songlist (
          name VARCHAR,
          createdDate VARCHAR,
          modifiedDate VARCHAR,
          shared SMALLINT,
          ownerEmail VARCHAR,
          serverUuid VARCHAR,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE songlistelement (
          song_id BIGINT,
          ordinalNumber INTEGER,
          songList_id BIGINT,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      CREATE TABLE loggedinuser (
          email VARCHAR,
          token VARCHAR,
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          uuid VARCHAR
      );
      """
  )


def main() -> int:
    api_base = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_API_BASE
    output_path = ROOT / "app" / "src" / "androidTest" / "assets" / "songbook.db"
    template_path = ROOT / "app" / "src" / "androidTest" / "assets" / "songbook.db"
    if not template_path.exists():
        template_path = ROOT / "songbook-full.db"
    work_path = ROOT / "songbook-fixture-work.db"

    songs = fetch_json(f"{api_base}/api/songs/language/{HUNGARIAN_UUID}")
    collections = fetch_json(
        f"{api_base}/api/songCollections/language/{HUNGARIAN_UUID}/lastModifiedDate/0"
    )
    languages = fetch_json(f"{api_base}/api/languages")
    hungarian = next(lang for lang in languages if lang["uuid"] == HUNGARIAN_UUID)

    song_by_uuid = {song["uuid"]: song for song in songs}
    matches_367: list[tuple[dict, dict, dict]] = []
    selected_keys: set[tuple[str, str, str]] = set()

    for collection in collections:
        for element in collection.get("songCollectionElements", []):
            song = song_by_uuid.get(element.get("songUuid"))
            if song is None:
                continue
            key = (song["uuid"], collection["uuid"], element.get("ordinalNumber", ""))
            if song_matches_query(song, collection, element, SEARCH_QUERY_367):
                matches_367.append((song, collection, element))
                selected_keys.add(key)

    if len({song["uuid"] for song, _, _ in matches_367}) < MIN_367_RESULTS:
        raise RuntimeError(f"Expected at least {MIN_367_RESULTS} songs for query 367")

    fixture_song, fixture_collection, fixture_element = choose_fixture_match(matches_367)
    fixture_title = fixture_song["title"]
    fixture_row_text = f"{fixture_title}\n{fixture_collection['name']} {fixture_element['ordinalNumber']}"

    selected_songs = {song["uuid"]: song for song, _, _ in matches_367}
    selected_collections = {}
    for collection in collections:
        has_selected = False
        for element in collection.get("songCollectionElements", []):
            key = (element.get("songUuid"), collection["uuid"], element.get("ordinalNumber", ""))
            if key in selected_keys:
                has_selected = True
                break
        if has_selected:
            selected_collections[collection["uuid"]] = collection

    if template_path.exists():
        shutil.copyfile(template_path, work_path)
        connection = sqlite3.connect(work_path)
        connection.execute("DELETE FROM queuesong")
        connection.execute("DELETE FROM favouritesong")
        connection.execute("DELETE FROM songlistelement")
        connection.execute("DELETE FROM songlist")
        connection.execute("DELETE FROM loggedinuser")
        connection.execute("DELETE FROM songcollectionelement")
        connection.execute("DELETE FROM songverse")
        connection.execute("DELETE FROM song")
        connection.execute("DELETE FROM songcollection")
        connection.execute("DELETE FROM language")
    else:
        if work_path.exists():
            work_path.unlink()
        connection = sqlite3.connect(work_path)
        create_schema(connection)

    connection.execute(
        "INSERT INTO language (englishName, nativeName, selected, selectedForDownload, uuid) VALUES (?, ?, ?, ?, ?)",
        (hungarian["englishName"], hungarian["nativeName"], 1, 1, hungarian["uuid"]),
    )
    language_id = connection.execute("SELECT id FROM language WHERE uuid = ?", (hungarian["uuid"],)).fetchone()[0]

    collection_ids: dict[str, int] = {}
    for collection in selected_collections.values():
        connection.execute(
            "INSERT INTO songcollection (createdDate, modifiedDate, name, language_id, uuid) VALUES (?, ?, ?, ?, ?)",
            (
                iso_date(collection.get("createdDate")),
                iso_date(collection.get("modifiedDate")),
                collection.get("name"),
                language_id,
                collection["uuid"],
            ),
        )
        collection_ids[collection["uuid"]] = connection.execute(
            "SELECT id FROM songcollection WHERE uuid = ?", (collection["uuid"],)
        ).fetchone()[0]

    song_ids: dict[str, int] = {}
    for song in selected_songs.values():
        title = song.get("title") or ""
        connection.execute(
            """
            INSERT INTO song (
                title, strippedTitle, createdDate, modifiedDate, language_id,
                views, favourites, verseOrder, asDeleted, uuid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                title,
                strip_accents(title.lower()),
                iso_date(song.get("createdDate")),
                iso_date(song.get("modifiedDate")),
                language_id,
                song.get("views") or 0,
                song.get("favourites") or 0,
                song.get("verseOrder"),
                0 if not song.get("deleted") else 1,
                song["uuid"],
            ),
        )
        song_ids[song["uuid"]] = connection.execute(
            "SELECT id FROM song WHERE uuid = ?", (song["uuid"],)
        ).fetchone()[0]

    for collection in selected_collections.values():
        collection_id = collection_ids[collection["uuid"]]
        for element in collection.get("songCollectionElements", []):
            key = (element.get("songUuid"), collection["uuid"], element.get("ordinalNumber", ""))
            if key not in selected_keys:
                continue
            connection.execute(
                """
                INSERT INTO songcollectionelement (ordinalNumber, songUuid, songCollection_id, uuid)
                VALUES (?, ?, ?, ?)
                """,
                (
                    element.get("ordinalNumber"),
                    element.get("songUuid"),
                    collection_id,
                    element.get("uuid") or f"{element.get('songUuid')}-{collection['uuid']}-{element.get('ordinalNumber')}",
                ),
            )

    connection.commit()
    connection.execute("VACUUM")
    connection.close()

    output_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(work_path, output_path)

    fixtures_path = ROOT / "app" / "src" / "androidTest" / "java" / "com" / "bence" / "songbook" / "SongbookTestFixtures.java"
    escaped_row = fixture_row_text.replace("\\", "\\\\").replace("\"", "\\\"")
    escaped_title = fixture_title.replace("\\", "\\\\").replace("\"", "\\\"")
    escaped_row_java = escaped_row.replace("\n", "\\n")
    fixtures_path.write_text(
        "package com.bence.songbook;\n\n"
        "public final class SongbookTestFixtures {\n\n"
        '    public static final String SEARCH_QUERY_SONG_A = "367";\n'
        "    public static final String SONG_A_ROW_TEXT =\n"
        f'            "{escaped_row_java}";\n'
        f'    public static final String SONG_A_TITLE = "{escaped_title}";\n\n'
        '    public static final String HUNGARIAN_LANGUAGE_NAME = "Hungarian";\n'
        '    public static final String HUNGARIAN_NATIVE_NAME = "Magyar";\n\n'
        '    public static final String SEARCH_QUERY_SONG_B = "baptista";\n'
        "    public static final int MIN_SEARCH_RESULTS = 2;\n\n"
        "    private SongbookTestFixtures() {\n"
        "    }\n"
        "}\n",
        encoding="utf-8",
    )

    print(f"Wrote {output_path} ({output_path.stat().st_size} bytes)")
    print(f"Fixture title: {fixture_title}")
    print(f"Fixture row: {fixture_row_text}")
    print(f"Songs: {len(selected_songs)}, collections: {len(selected_collections)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
