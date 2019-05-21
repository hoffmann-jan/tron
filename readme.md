# Tron Game

server client based game.

## Client Java
* [Java 12](https://www.oracle.com/technetwork/java/javase/downloads/jdk12-downloads-5295953.html)
* Import in eclipse as "Existing Maven Project"
* Adjust path of JRE in pom.xml
* Run as Maven Build with goal "clean javafx:run"

## Server CSharp

* [.Net Core 2.2](https://dotnet.microsoft.com/download/dotnet-core/2.2)
* configure server in AppConfig.json
  * ip address
  * port
  
### Run Release
* open terminal
* navigate to ./tron/server_cs/Server
* command: dotnet run --configuration Release

### Run Debug
* open terminal
* navigate to ./tron/server_cs/Server
* command: dotnet run
