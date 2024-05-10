@ECHO OFF

REM Define variables
    SET APP_DIR=%~dp0
    SET SRC_DIR=%APP_DIR%src
    SET BIN_DIR=%APP_DIR%bin
    SET LIB_DIR=%APP_DIR%lib
    SET TEMP_JAVA_DIR=%APP_DIR%tempjava
    SET TEST_NAME_DIR=TestFramework

REM Copier les *.java dans un dossier temporaire tempjava
    MKDIR "%APP_DIR%\tempjava"
    for /R "%SRC_DIR%" %%G IN (*.java) DO (
        XCOPY /Y "%%G" "%APP_DIR%\tempjava"
    )

REM Compile Java classes
    javac -cp "%LIB_DIR%\*" -d "%BIN_DIR%" "%TEMP_JAVA_DIR%\*.java"

REM Supprimer le dossier temporaire apres compilation
    RD /S /Q "%TEMP_JAVA_DIR%"

REM Archiver en .jar
    jar cf Dispatcher.jar -C "%BIN_DIR%" .

REM Copy the .jar file to the lib directory of Project tomcat
move /Y Dispatcher.jar "..\%TEST_NAME_DIR%\WEB-INF\lib\"