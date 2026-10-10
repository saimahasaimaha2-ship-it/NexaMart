# Contributing to NexaMart

Steps from `git clone` to a running local instance on Windows (PowerShell).

## Prerequisites
- JDK 17
- Maven 3.9+
- Apache Tomcat 9.0.x (for example `C:\apache-tomcat-9.0.120`)
- Git

## 1. Clone
```
git clone https://github.com/saimahasaimaha2-ship-it/NexaMart.git
cd NexaMart
```

## 2. Create your local config
`config.properties` is excluded from version control, so a fresh clone does not have it. Create `src/main/resources/config.properties` with your database settings (JDBC URL pointing at the H2 TCP server on port 9092, plus user and password). See `.env.example` for the list of values.

## 3. Start the H2 database
Run this in its own terminal tab and leave it open (closing the tab stops the database):
```
java -cp "<path-to>\h2-2.2.224.jar" org.h2.tools.Server -web -webAllowOthers -tcp -tcpAllowOthers -ifNotExists -baseDir .\data
```
The TCP server must listen on port 9092. The schema is created automatically when the app starts.

## 4. Run the tests
```
mvn clean test
```
All 35 tests should pass.

## 5. Build and deploy to Tomcat
```
mvn clean package
Remove-Item -Recurse -Force C:\apache-tomcat-9.0.120\webapps\nexamart
copy target\nexamart.war C:\apache-tomcat-9.0.120\webapps\
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.120"
C:\apache-tomcat-9.0.120\bin\startup.bat
```
Open http://localhost:8080/nexamart/ and check http://localhost:8080/nexamart/api/v1/health returns `{"status":"UP","db":"UP"}`.

## Workflow
- Branch from `main` (`feature/<name>`), keep `main` deployable.
- Use conventional commit messages: `feat:`, `fix:`, `test:`, `docs:`.
- Add or update tests with each change; CI (`mvn -B clean verify`) must be green before merging.
