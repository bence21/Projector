#!/usr/bin/env python3
"""
Production build script for projector-desktop.
Checks out main, pulls latest changes, and runs the production build.
"""

import subprocess
import sys
import time
from pathlib import Path
from typing import Optional


class DesktopProductionBuilder:
    """Handles production build operations for projector-desktop."""

    def __init__(self, workspace_root: Optional[str] = None):
        if workspace_root is None:
            script_dir = Path(__file__).parent.absolute()
            self.workspace_root = script_dir.parent
        else:
            self.workspace_root = Path(workspace_root)

        self.desktop_dir = self.workspace_root / "projector-desktop"

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

    def git_checkout(self, branch: str) -> None:
        print(f"\n=== Checking out branch: {branch} ===")
        self.run_command(["git", "checkout", branch])

    def git_pull(self) -> None:
        print("\n=== Pulling latest changes ===")
        self.run_command(["git", "pull"])

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

            self.git_checkout("main")
            self.git_pull()
            self.run_batch_file("prod.bat")

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

        except FileNotFoundError as e:
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
