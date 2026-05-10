@echo off
echo === Arrancando sistema UD Las Palmas ===

echo [1/4] Arrancando ActiveMQ...
start "ActiveMQ" cmd /k "C:\apache-activemq-5.19.6-bin\apache-activemq-5.19.6\bin\activemq.bat start"

echo Esperando 10 segundos a que ActiveMQ arranque...
timeout /t 10 /nobreak

echo [2/4] Arrancando Weather Provider...
start "Weather Provider" cmd /k "cd C:\Users\lucia\IdeaProjects\DataScienceProject_Sprint1\weather-provider\target && \"C:\Users\lucia\.jdks\graalvm-jdk-21.0.7\bin\java\" -jar weather-provider-1.0-SNAPSHOT.jar"

echo [3/4] Arrancando Sports Scraper...
start "Sports Scraper" cmd /k "cd C:\Users\lucia\IdeaProjects\DataScienceProject_Sprint1\sports-scraper\target && \"C:\Users\lucia\.jdks\graalvm-jdk-21.0.7\bin\java\" -jar sports-scraper-1.0-SNAPSHOT.jar"

echo [4/4] Arrancando Business Unit...
start "Business Unit" cmd /k "cd C:\Users\lucia\IdeaProjects\DataScienceProject_Sprint1\business-unit\target && \"C:\Users\lucia\.jdks\graalvm-jdk-21.0.7\bin\java\" -jar business-unit-1.0-SNAPSHOT.jar"

echo === Todo arrancado. Abre dashboard.html en el navegador ===
pause