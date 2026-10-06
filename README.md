# Network Escape Room

A multiplayer network-based escape room game developed using Java TCP sockets and a modern Java Swing GUI.

## Technologies

- **Language**: Java 21 / Java SE
- **Networking**: TCP Sockets (`Socket`, `ServerSocket`), Multithreaded Client Handling
- **GUI Framework**: Java Swing & AWT (Custom antialiased dark cyber-terminal UI)
- **Audio**: Pure Java Sound Synthesizer (`javax.sound.sampled`)
- **Architecture**: Client-Server with Server-Authoritative State Management

## Game Flow

1. **Lobby**: 2 to 4 players join, enter their call-signs, and launch the mission.
2. **Room 1 (Initial Access & Configuration)**:
   - Each player gets an individual networking puzzle (IPv4 format, HTTP port 80, username admin, password NETGAME123).
   - 2 attempts per puzzle. Solved puzzles grant 100 points and unlock crucial clues.
   - **Room 1 Team Challenge**: Operatives combine clues to execute `CONNECT <IP> <PORT> [username] [password]`.
3. **Room 2 (Network Topology & Routing)**:
   - Individual puzzles covering shortest routing path, hop counts, TTL packet-loop prevention, and DNS resolution.
   - **Room 2 Team Challenge**: Operatives reconstruct the route: `ROUTE <PATH> HOPS <NUMBER> [TTL <NUM>] [DNS <IP>]`.
4. **Final Room (Escape Sequence)**:
   - Evaluates the complete end-to-end network telemetry.
   - 3 shared team attempts to select the valid sequence (`DNS -> ROUTE -> CONNECT`).
5. **Escape Victory**: Final debrief with live team rankings and scores.

---

## How to Run

### 1. Compile All Sources
```bash
javac -d out src/server/*.java src/client/*.java src/client/ui/*.java
```

### 2. Start the Game Server
```bash
java -cp out server.Server
```

### 3. Launch GUI Client (Player 1, 2, 3, 4)
Run in separate terminals for each player:
```bash
java -cp out client.GameClientGUI
```
*(Or run `java -cp out client.Client` which launches the GUI by default)*

### 4. (Optional) Run Console/Terminal Client
If you ever want the original terminal interface:
```bash
java -cp out client.Client --cli
```