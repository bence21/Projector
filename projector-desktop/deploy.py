#!/usr/bin/env python3
"""
Deploy desktop release artifacts to the production server.

Uploads projector-setup.exe and projectorUpdate{N}.zip via the deployer HTTP API,
then registers the new version through POST /deployer/api/projectorVersion.

Copy deploy.config.example.json to deploy.config.json and fill in your values.
Use a deployer or admin account. Password via deployPassword or DEPLOY_PASSWORD.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
from dataclasses import dataclass
from http.cookiejar import CookieJar
from pathlib import Path
from typing import Any, Optional

from createProd import DesktopProductionBuilder, Version

DESCRIPTION_RE = re.compile(r'projectorVersion\.setDescription\("([^"]+)"\)')
ROLE_ADMIN = 1
ROLE_DEPLOYER = 3


@dataclass(frozen=True)
class DeployConfig:
    base_url: str
    admin_email: str
    admin_password: str
    description: str

    @classmethod
    def load(cls, config_path: Path) -> "DeployConfig":
        if not config_path.exists():
            raise FileNotFoundError(
                f"Deploy config not found: {config_path}\n"
                f"Copy deploy.config.example.json to deploy.config.json and fill in your values."
            )

        with config_path.open(encoding="utf-8") as handle:
            raw: dict[str, Any] = json.load(handle)

        base_url = _require_string(raw, "baseUrl").rstrip("/")
        deploy_email = raw.get("deployEmail") or raw.get("adminEmail")
        if not isinstance(deploy_email, str) or not deploy_email.strip():
            raise ValueError("deploy.config.json: deployEmail is required")
        deploy_email = deploy_email.strip()
        deploy_password = (
            raw.get("deployPassword")
            or raw.get("adminPassword")
            or os.environ.get("DEPLOY_PASSWORD")
            or os.environ.get("DEPLOY_ADMIN_PASSWORD")
        )
        if not deploy_password:
            raise ValueError(
                "Deploy password is required. Set deployPassword in deploy.config.json "
                "or DEPLOY_PASSWORD in the environment."
            )

        description = raw.get("description") or "Performance improvements and bug fixes."

        return cls(
            base_url=base_url,
            admin_email=deploy_email,
            admin_password=deploy_password,
            description=description,
        )


def _require_string(data: dict[str, Any], key: str) -> str:
    value = data.get(key)
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"deploy.config.json: {key} is required")
    return value.strip()


def _multipart_body(file_path: Path, field_name: str = "file") -> tuple[bytes, str]:
    boundary = f"----ProjectorDeploy{uuid.uuid4().hex}"
    file_bytes = file_path.read_bytes()
    body = b"".join(
        [
            f"--{boundary}\r\n".encode(),
            (
                f'Content-Disposition: form-data; name="{field_name}"; '
                f'filename="{file_path.name}"\r\n'
            ).encode(),
            b"Content-Type: application/octet-stream\r\n\r\n",
            file_bytes,
            b"\r\n",
            f"--{boundary}--\r\n".encode(),
        ]
    )
    return body, f"multipart/form-data; boundary={boundary}"


class DesktopDeployer:
    def __init__(self, workspace_root: Optional[str] = None, config_path: Optional[Path] = None):
        self.builder = DesktopProductionBuilder(workspace_root)
        self.desktop_dir = self.builder.desktop_dir
        self.server_dir = self.builder.server_dir
        self.public_dir = self.server_dir / "aPublic_folder"
        self.config_path = config_path or (self.desktop_dir / "deploy.config.json")

    def read_description(self) -> str:
        version_util = (
            self.server_dir
            / "src/main/java/com/bence/projector/server/utils/ProjectorVersionUtil.java"
        )
        text = version_util.read_text(encoding="utf-8")
        match = DESCRIPTION_RE.search(text)
        if match:
            return match.group(1)
        return "Performance improvements and bug fixes."

    def artifact_paths(self, version: Version) -> tuple[Path, Path]:
        setup_exe = self.public_dir / "projector-setup.exe"
        update_zip = self.public_dir / f"projectorUpdate{version.build}.zip"
        return setup_exe, update_zip

    def ensure_artifacts_exist(self, version: Version) -> tuple[Path, Path]:
        setup_exe, update_zip = self.artifact_paths(version)
        missing = [path for path in (setup_exe, update_zip) if not path.exists()]
        if missing:
            missing_list = "\n".join(f"  - {path}" for path in missing)
            raise FileNotFoundError(
                "Release artifacts are missing. Run createProd.py first.\n" + missing_list
            )
        return setup_exe, update_zip

    def upload_artifacts(self, opener: urllib.request.OpenerDirector, config: DeployConfig, files: list[Path]) -> None:
        print(f"\n=== Uploading {len(files)} file(s) via deployer API ===")
        for file_path in files:
            body, content_type = _multipart_body(file_path)
            request = urllib.request.Request(
                f"{config.base_url}/deployer/api/projectorReleaseFile",
                data=body,
                method="POST",
                headers={"Content-Type": content_type},
            )
            try:
                with opener.open(request) as response:
                    message = response.read().decode("utf-8").strip()
                    print(f"Uploaded {file_path.name}: {message or 'ok'}")
            except urllib.error.HTTPError as error:
                details = error.read().decode("utf-8", errors="replace")
                raise RuntimeError(
                    f"Failed to upload {file_path.name}: HTTP {error.code} {error.reason}\n{details}"
                ) from error

    def register_version(self, opener: urllib.request.OpenerDirector, config: DeployConfig, version: Version, description: str) -> None:
        print("\n=== Registering projector version via deployer API ===")
        payload = json.dumps(
            {
                "version": version.semver,
                "versionId": version.build,
                "description": description,
            }
        ).encode("utf-8")

        request = urllib.request.Request(
            f"{config.base_url}/deployer/api/projectorVersion",
            data=payload,
            method="POST",
            headers={"Content-Type": "application/json"},
        )

        try:
            with opener.open(request) as response:
                body = response.read().decode("utf-8")
                print(f"Registered version {version.semver} (build {version.build})")
                if body.strip():
                    print(body)
        except urllib.error.HTTPError as error:
            details = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(
                f"Failed to register version: HTTP {error.code} {error.reason}\n{details}"
            ) from error

    def login(self, config: DeployConfig) -> urllib.request.OpenerDirector:
        cookie_jar = CookieJar()
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookie_jar))
        login_data = urllib.parse.urlencode(
            {
                "username": config.admin_email,
                "password": config.admin_password,
            }
        ).encode("utf-8")
        login_request = urllib.request.Request(
            f"{config.base_url}/login",
            data=login_data,
            method="POST",
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

        try:
            with opener.open(login_request) as response:
                response.read()
        except urllib.error.HTTPError as error:
            if error.code not in (200, 302):
                details = error.read().decode("utf-8", errors="replace")
                raise RuntimeError(
                    f"Login failed: HTTP {error.code} {error.reason}\n{details}"
                ) from error

        self._verify_deploy_session(opener, config)
        return opener

    def _verify_deploy_session(
        self, opener: urllib.request.OpenerDirector, config: DeployConfig
    ) -> None:
        request = urllib.request.Request(f"{config.base_url}/api/username")
        try:
            with opener.open(request) as response:
                payload = json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            details = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(
                f"Could not verify login session: HTTP {error.code} {error.reason}\n{details}"
            ) from error

        role = payload.get("role")
        email = payload.get("email")
        if role not in (ROLE_ADMIN, ROLE_DEPLOYER):
            raise RuntimeError(
                f"Logged in as {email!r}, but deployer or admin role is required "
                f"(role={ROLE_DEPLOYER} or {ROLE_ADMIN}, got {role!r}). "
                "Use a deployer account in deploy.config.json."
            )

    def run(self, skip_upload: bool = False, skip_register: bool = False) -> int:
        try:
            print("\n" + "=" * 60)
            print("DESKTOP DEPLOY")
            print("=" * 60)

            config = DeployConfig.load(self.config_path)
            version = self.builder.read_current_version()
            description = config.description or self.read_description()
            setup_exe, update_zip = self.ensure_artifacts_exist(version)

            print(f"\nVersion: {version.semver} (build {version.build})")
            print(f"Description: {description}")
            print(f"Server: {config.base_url}")

            opener = self.login(config)

            if not skip_upload:
                self.upload_artifacts(opener, config, [setup_exe, update_zip])
            else:
                print("\n=== Skipping file upload ===")

            if not skip_register:
                self.register_version(opener, config, version, description)
            else:
                print("\n=== Skipping version registration ===")

            print("\n" + "=" * 60)
            print("DESKTOP DEPLOY COMPLETE!")
            print("=" * 60)
            return 0

        except (FileNotFoundError, ValueError, RuntimeError) as error:
            print(f"\nDEPLOY FAILED: {error}", file=sys.stderr)
            return 1


def main() -> None:
    parser = argparse.ArgumentParser(description="Deploy desktop release artifacts")
    parser.add_argument(
        "--config",
        type=Path,
        default=None,
        help="Path to deploy.config.json (default: projector-desktop/deploy.config.json)",
    )
    parser.add_argument("--skip-upload", action="store_true", help="Skip HTTP file upload")
    parser.add_argument(
        "--skip-register",
        action="store_true",
        help="Skip admin API version registration",
    )
    args = parser.parse_args()

    deployer = DesktopDeployer(config_path=args.config)
    sys.exit(deployer.run(skip_upload=args.skip_upload, skip_register=args.skip_register))


if __name__ == "__main__":
    main()
