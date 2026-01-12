@echo off
REM Sign projector-setup.exe installer with code signing certificate

setlocal

REM Certificate configuration - load from signing.properties
set CONFIG_FILE=c:\workspace\Projector\ssl\createNewPfx\signing.properties
set CERT_PATH=
set CERT_PASSWORD=

if exist "%CONFIG_FILE%" (
    for /f "usebackq tokens=1,2 delims==" %%a in ("%CONFIG_FILE%") do (
        if "%%a"=="CERT_PATH" set CERT_PATH=%%b
        if "%%a"=="CERT_PASSWORD" set CERT_PASSWORD=%%b
    )
) else (
    echo ERROR: signing.properties not found at %CONFIG_FILE%
    echo Please create signing.properties from signing.properties.template
    exit /b 1
)

REM Validate that configuration was loaded
if "%CERT_PATH%"=="" (
    echo ERROR: CERT_PATH not found in signing.properties
    exit /b 1
)
if "%CERT_PASSWORD%"=="" (
    echo ERROR: CERT_PASSWORD not found in signing.properties
    exit /b 1
)

set TARGET_FILE=..\Projector-server\aPublic_folder\projector-setup.exe

REM Check if certificate exists
if not exist "%CERT_PATH%" (
    echo ERROR: Certificate file not found: %CERT_PATH%
    echo Please ensure the certificate has been generated.
    exit /b 1
)

REM Check if target file exists
if not exist "%TARGET_FILE%" (
    echo ERROR: Target file not found: %TARGET_FILE%
    echo Please ensure the installer was created successfully.
    exit /b 1
)

echo Signing projector-setup.exe...
echo Certificate: %CERT_PATH%
echo Target: %TARGET_FILE%
echo.

REM Sign the file (installer typically doesn't need attrib -r)
signtool sign /debug /fd SHA256 /f "%CERT_PATH%" /p "%CERT_PASSWORD%" -t "http://timestamp.comodoca.com/authenticode" "%TARGET_FILE%"

if errorlevel 1 (
    echo ERROR: Failed to sign projector-setup.exe
    exit /b 1
)

echo.
echo SUCCESS: projector-setup.exe has been signed.
echo.

endlocal
