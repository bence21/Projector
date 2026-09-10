#!/usr/bin/env python3
"""
Production build script for projector-desktop.
Checks out main, pulls latest changes, bumps version files, runs the production
build, then commits and pushes the version bump only if the build succeeds.
"""

import re
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional


APP_VERSION_GRADLE_RE = re.compile(r'(appVersion\s*=\s*")([^"]+)(")')
APP_VERSION_ISS_RE = re.compile(r"(AppVersion=)([\d.]+)")
PROJECTOR_VERSION_NUMBER_RE = re.compile(
    r"(private final int projectorVersionNumber\s*=\s*)(\d+)(;)"
)
SET_VERSION_RE = re.compile(r'(projectorVersion\.setVersion\(")([^"]+)("\);)')
SET_VERSION_ID_RE = re.compile(r"(projectorVersion\.setVersionId\()(\d+)(\);)")
UPDATE_ZIP_RE = re.compile(r"(projectorUpdate)\d+(\.zip)")


@dataclass(frozen=True)
class Version:
    major: int
    minor: int
    patch: int
    build: int

    @property
    def semver(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"

    def next(self) -> "Version":
        return Version(self.major, self.minor, self.patch + 1, self.build + 1)

    def commit_message(self) -> str:
        return f"Desktop - {self.semver} - {self.build}"

    @classmethod
    def parse_semver(cls, value: str) -> "Version":
        parts = value.strip().split(".")
        if len(parts) != 3:
            raise ValueError(f"Invalid semver: {value!r}")
        try:
            major, minor, patch = (int(part) for part in parts)
        except ValueError as exc:
            raise ValueError(f"Invalid semver: {value!r}") from exc
        return cls(major, minor, patch, 0)


class DesktopProductionBuilder:
    """Handles production build operations for projector-desktop."""

    def __init__(self, workspace_root: Optional[str] = None):
        if workspace_root is None:
            script_dir = Path(__file__).parent.absolute()
            self.workspace_root = script_dir.parent
        else:
            self.workspace_root = Path(workspace_root)

        self.desktop_dir = self.workspace_root / "projector-desktop"
        self.server_dir = self.workspace_root / "Projector-server"

    @property
    def version_file_paths(self) -> list[Path]:
        return [
            self.desktop_dir / "build.gradle",
            self.desktop_dir / "setup.iss",
            self.desktop_dir
            / "src/main/java/projector/application/Updater.java",
            self.server_dir
            / "src/main/java/com/bence/projector/server/utils/ProjectorVersionUtil.java",
            self.server_dir / "aPublic_folder/create.bat",
        ]

    def run_command(
        self, command: list[str], cwd: Optional[Path] = None, check: bool = True
    ) -> subprocess.CompletedProcess:
        if cwd is None:
            cwd = self.workspace_root

        command_text = " ".join(command)
        print(f"Running in {cwd}: {command_text}")
        try:
            result = subprocess.run(
                command,
                cwd=cwd,
                check=check,
                capture_output=True,
                text=True,
            )
        except subprocess.CalledProcessError as e:
            if e.stdout:
                print(e.stdout)
            if e.stderr:
                print(e.stderr, file=sys.stderr)
            print(f"FAILED COMMAND: {command_text}", file=sys.stderr)
            print(f"FAILED IN: {cwd}", file=sys.stderr)
            raise

        if result.stdout:
            print(result.stdout)
        if result.stderr:
            print(result.stderr, file=sys.stderr)

        return result

    def ensure_clean_working_tree(self) -> None:
        print("\n=== Checking working tree ===")
        result = self.run_command(["git", "status", "--porcelain"])
        if result.stdout.strip():
            raise RuntimeError(
                "Working tree is dirty. Commit or stash changes before running."
            )
        print("Working tree is clean.")

    def git_checkout(self, branch: str) -> None:
        print(f"\n=== Checking out branch: {branch} ===")
        self.run_command(["git", "checkout", branch])

    def git_pull(self) -> None:
        print("\n=== Pulling latest changes ===")
        self.run_command(["git", "pull"])

    def read_current_version(self) -> Version:
        build_gradle = self.desktop_dir / "build.gradle"
        updater_java = (
            self.desktop_dir / "src/main/java/projector/application/Updater.java"
        )

        gradle_text = build_gradle.read_text(encoding="utf-8")
        gradle_match = APP_VERSION_GRADLE_RE.search(gradle_text)
        if not gradle_match:
            raise ValueError(f"Could not find appVersion in {build_gradle}")

        updater_text = updater_java.read_text(encoding="utf-8")
        updater_match = PROJECTOR_VERSION_NUMBER_RE.search(updater_text)
        if not updater_match:
            raise ValueError(
                f"Could not find projectorVersionNumber in {updater_java}"
            )

        version = Version.parse_semver(gradle_match.group(2))
        return Version(version.major, version.minor, version.patch, int(updater_match.group(2)))

    @staticmethod
    def _replace_one(
        content: str, pattern: re.Pattern[str], replacement: str, file_path: Path
    ) -> str:
        new_content, count = pattern.subn(replacement, content, count=1)
        if count != 1:
            raise ValueError(
                f"Expected 1 replacement in {file_path}, got {count}"
            )
        return new_content

    def update_version_files(self, version: Version) -> None:
        build_gradle = self.desktop_dir / "build.gradle"
        setup_iss = self.desktop_dir / "setup.iss"
        updater_java = (
            self.desktop_dir / "src/main/java/projector/application/Updater.java"
        )
        version_util = (
            self.server_dir
            / "src/main/java/com/bence/projector/server/utils/ProjectorVersionUtil.java"
        )
        create_bat = self.server_dir / "aPublic_folder/create.bat"

        build_gradle.write_text(
            self._replace_one(
                build_gradle.read_text(encoding="utf-8"),
                APP_VERSION_GRADLE_RE,
                rf'\g<1>{version.semver}\g<3>',
                build_gradle,
            ),
            encoding="utf-8",
        )
        setup_iss.write_text(
            self._replace_one(
                setup_iss.read_text(encoding="utf-8"),
                APP_VERSION_ISS_RE,
                rf"\g<1>{version.semver}",
                setup_iss,
            ),
            encoding="utf-8",
        )
        updater_java.write_text(
            self._replace_one(
                updater_java.read_text(encoding="utf-8"),
                PROJECTOR_VERSION_NUMBER_RE,
                rf"\g<1>{version.build}\g<3>",
                updater_java,
            ),
            encoding="utf-8",
        )

        version_util_text = version_util.read_text(encoding="utf-8")
        version_util_text = self._replace_one(
            version_util_text,
            SET_VERSION_RE,
            rf'\g<1>{version.semver}\g<3>',
            version_util,
        )
        version_util_text = self._replace_one(
            version_util_text,
            SET_VERSION_ID_RE,
            rf"\g<1>{version.build}\g<3>",
            version_util,
        )
        version_util.write_text(version_util_text, encoding="utf-8")

        create_bat.write_text(
            self._replace_one(
                create_bat.read_text(encoding="utf-8"),
                UPDATE_ZIP_RE,
                rf"\g<1>{version.build}\g<2>",
                create_bat,
            ),
            encoding="utf-8",
        )

    def git_commit_and_push(self, message: str, files: list[Path]) -> None:
        relative_paths = [
            path.relative_to(self.workspace_root).as_posix() for path in files
        ]

        print(f"\n=== Committing version bump: {message} ===")
        self.run_command(["git", "add", *relative_paths])
        self.run_command(["git", "commit", "-m", message])

        commit_result = self.run_command(["git", "rev-parse", "--short", "HEAD"])
        commit_hash = commit_result.stdout.strip()
        print(f"Created commit {commit_hash}: {message}")

        print("\n=== Pushing to origin/main ===")
        self.run_command(["git", "push", "origin", "main"])

    def bump_version_files(self) -> Version:
        current = self.read_current_version()
        next_version = current.next()

        print("\n=== Bumping version ===")
        print(
            f"{current.semver} (build {current.build}) "
            f"-> {next_version.semver} (build {next_version.build})"
        )

        self.update_version_files(next_version)
        return next_version

    def run_batch_file(self, batch_file: str) -> None:
        batch_path = self.desktop_dir / batch_file
        if not batch_path.exists():
            raise FileNotFoundError(f"Batch file not found: {batch_path}")

        print(f"\n=== Running {batch_file} ===")
        self.run_command(["cmd", "/c", str(batch_path)], cwd=self.desktop_dir)

    def format_elapsed(self, seconds: float) -> str:
        minutes, secs = divmod(int(seconds), 60)
        hours, minutes = divmod(minutes, 60)
        if hours:
            return f"{hours}h {minutes}m {secs}s"
        if minutes:
            return f"{minutes}m {secs}s"
        return f"{secs}s"

    def run(self) -> int:
        start_time = time.perf_counter()

        try:
            print("\n" + "=" * 60)
            print("DESKTOP PRODUCTION BUILD")
            print("=" * 60)

            self.ensure_clean_working_tree()
            self.git_checkout("main")
            self.git_pull()
            next_version = self.bump_version_files()
            self.run_batch_file("prod.bat")
            self.git_commit_and_push(
                next_version.commit_message(), self.version_file_paths
            )

            elapsed = time.perf_counter() - start_time
            print("\n" + "=" * 60)
            print("DESKTOP PRODUCTION BUILD COMPLETE!")
            print(f"Elapsed time: {self.format_elapsed(elapsed)}")
            print("=" * 60)
            return 0

        except subprocess.CalledProcessError as e:
            elapsed = time.perf_counter() - start_time
            failed_command = (
                " ".join(e.cmd) if isinstance(e.cmd, list) else str(e.cmd)
            )
            print(f"\nBUILD FAILED after {self.format_elapsed(elapsed)}")
            print(f"Command failed with exit code {e.returncode}")
            print(f"Failed command: {failed_command}")
            return 1

        except (FileNotFoundError, RuntimeError, ValueError) as e:
            elapsed = time.perf_counter() - start_time
            print(f"\nBUILD FAILED after {self.format_elapsed(elapsed)}")
            print(f"Error: {e}")
            return 1

        except Exception as e:
            elapsed = time.perf_counter() - start_time
            print(f"\nBUILD FAILED after {self.format_elapsed(elapsed)}")
            print(f"Unexpected error: {e}")
            return 1


def main() -> None:
    builder = DesktopProductionBuilder()
    sys.exit(builder.run())


if __name__ == "__main__":
    main()
