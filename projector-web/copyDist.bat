:loop
xcopy c:\workspace\Projector\Projector\projector-web\dist\static c:\workspace\Projector\Projector\Projector-server\out\production\resources\static /S /I /Y
xcopy c:\workspace\Projector\Projector\projector-web\dist\static c:\workspace\Projector\Projector\Projector-server\build\resources\main\static /S /I /Y
pause
goto loop