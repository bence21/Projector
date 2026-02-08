@echo off
REM Sign Projector.exe with code signing certificate

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

set TARGET_FILE=build\jpackage\Projector\Projector.exe

REM Check if certificate exists
if not exist "%CERT_PATH%" (
    echo ERROR: Certificate file not found: %CERT_PATH%
    echo Please ensure the certificate has been generated.
    exit /b 1
)

REM Check if target file exists
if not exist "%TARGET_FILE%" (
    echo ERROR: Target file not found: %TARGET_FILE%
    echo Please ensure the build completed successfully.
    exit /b 1
)

echo Signing Projector.exe...
echo Certificate: %CERT_PATH%
echo Target: %TARGET_FILE%
echo.

REM Remove read-only attribute
attrib -r "%TARGET_FILE%"

REM Sign the file
signtool sign /debug /fd SHA256 /f "%CERT_PATH%" /p "%CERT_PASSWORD%" -t "http://timestamp.comodoca.com/authenticode" "%TARGET_FILE%"

if errorlevel 1 (
    echo ERROR: Failed to sign Projector.exe
    exit /b 1
)

echo.
echo SUCCESS: Projector.exe has been signed.
echo.

endlocal
