## Game Description
Pac-Man clone with Duke as Pac-Man.

## Setup
1. Ensure Java 17 is installed
2. Run `cd duke-man`

### Linux/MacOS users
`mkdir -p out && javac -d out $(find src -name "*.java") && java -cp out com.example.Main`

### Windows users
Using PowerShell:
`New-Item -ItemType Directory -Force out | Out-Null; javac -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }); java -cp out com.example.Main`

Using CMD:
`mkdir out 2>nul & dir /s /b src\*.java > sources.txt && javac -d out @sources.txt && java -cp out com.example.Main`

## Controls
* WASD or arrow keys to move
* R to restart after game over
