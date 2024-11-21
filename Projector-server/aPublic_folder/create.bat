@echo off
setlocal

REM Set the main directory name
set "mainDirectory=C:\workspace\Projector\Projector\Projector-server\aPublic_folder\update"

REM Set the subdirectory name
set "subDirectory=app"

REM Set the file to copy
set "fileToCopy=C:\workspace\Projector\Projector\projector-desktop\build\jpackage\Projector\Projector.exe"

REM Create the main directory
mkdir "%mainDirectory%"

REM Create the subdirectory inside the main directory
mkdir "%mainDirectory%\%subDirectory%"

REM Copy the file to the subdirectory
copy "%fileToCopy%" "%mainDirectory%"

REM Copy the file to the subdirectory
copy "C:\workspace\Projector\Projector\projector-desktop\build\jpackage\Projector\app\.jpackage.xml" "%mainDirectory%\%subDirectory%"
copy "C:\workspace\Projector\Projector\projector-desktop\build\jpackage\Projector\app\Projector.cfg" "%mainDirectory%\%subDirectory%"
copy "C:\workspace\Projector\Projector\projector-desktop\build\jpackage\Projector\app\projector-common.jar" "%mainDirectory%\%subDirectory%"
copy "C:\workspace\Projector\Projector\projector-desktop\build\jpackage\Projector\app\Projector-Desktop.jar" "%mainDirectory%\%subDirectory%"

echo "Directory and subdirectory created, file copied successfully."

endlocal

powershell Compress-Archive -Path "C:\workspace\Projector\Projector\Projector-server\aPublic_folder\update\*" -DestinationPath "projectorUpdate87.zip" -Force

echo "Update tried to create!"

echo "Run this:"
echo `powershell Compress-Archive -Path "C:\workspace\Projector\Projector\Projector-server\aPublic_folder\update\*" -DestinationPath "projectorUpdateX.zip" -Force`