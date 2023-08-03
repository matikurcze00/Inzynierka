@echo off

echo Starting Spring Boot server...
cd API
start "" call mvnw.cmd spring-boot:run
cd ..

echo Starting Angular project...
cd GUI\inz-gui

sleep 5

call npm install
call npm start