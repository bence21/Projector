pip install pyinstaller wmi pywin32

@REM pyinstaller --onefile --console get_monitors.py
pyinstaller get_monitors.spec

copy dist\get_monitors.exe ..\app\get_monitors.exe