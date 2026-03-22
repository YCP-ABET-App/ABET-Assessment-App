write-host "Building the project..."
./gradlew clean build -x test

write-host "Deploying the application to the server..."
scp build/libs/abet-app-0.0.31-dev.war root@64.225.24.47:src

write-host "Connecting to the server to restart the application..."
ssh root@64.225.24.47 "cd src && nohup java -jar abet-app-0.0.31-dev.war > app.log 2>&1 &"
