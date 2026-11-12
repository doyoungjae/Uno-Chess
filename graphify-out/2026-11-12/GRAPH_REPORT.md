# Graph Report - Uno-Chess  (2026-11-12)

## Corpus Check
- Corpus is ~19,242 words - fits in a single context window. You may not need a graph.

## Summary
- 445 nodes · 1187 edges · 21 communities (11 shown, 10 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS · INFERRED: 5 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Chess Board Engine
- Game Models & Cards
- Online Room Repository
- User Profile & Auth
- Online Room Lobby UI
- App Routing & Settings UI
- Local Preferences & Online Board
- Bottom Navigation
- First Turn Draw
- Online Lobby Screen
- Chess Piece Types
- Graphify Project Rules
- Project Overview

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 31 edges
2. `OnlineProfile` - 29 edges
3. `Player` - 23 edges
4. `UnoCard` - 22 edges
5. `ChessBoard` - 21 edges
6. `Move` - 21 edges
7. `OnlineBattleViewModel` - 21 edges
8. `OnlineGameState` - 19 edges
9. `OnlineRoom` - 19 edges
10. `Square` - 18 edges

## Surprising Connections (you probably didn't know these)
- `UnoChessApp()` --calls--> `FirstTurnDrawScreen()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/UnoChessApp.kt → app/src/main/java/com/example/unochessprototype/ui/FirstTurnDrawScreen.kt
- `ProfileSettingsTabScreen()` --references--> `LocalSettings`  [EXTRACTED]
  app/src/main/java/com/example/unochessprototype/ui/screen/ProfileSettingsTabScreen.kt → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `AnimatedChessBoard()` --references--> `LocalSettings`  [EXTRACTED]
  app/src/main/java/com/example/unochessprototype/ui/screen/ReleaseGameScreen.kt → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `ReleaseGameScreen()` --references--> `LocalSettings`  [EXTRACTED]
  app/src/main/java/com/example/unochessprototype/ui/screen/ReleaseGameScreen.kt → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `UnoChessApp()` --calls--> `LocalPreferences`  [EXTRACTED]
  app/src/main/java/com/example/unochessprototype/ui/UnoChessApp.kt → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph navigation sources** — claude_graph_json, claude_wiki_index, claude_graph_report [EXTRACTED 0.75]

## Communities (21 total, 10 thin omitted)

### Community 0 - "Chess Board Engine"
Cohesion: 0.08
Nodes (15): ChessBoard, Move, Piece, Player, BLACK, WHITE, Square, Win (+7 more)

### Community 1 - "Game Models & Cards"
Cohesion: 0.09
Nodes (26): CardColor, BLUE, GREEN, RED, YELLOW, Draw, ExchangeResult, GameResult (+18 more)

### Community 2 - "Online Room Repository"
Cohesion: 0.09
Nodes (9): OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler, Handler (+1 more)

### Community 3 - "User Profile & Auth"
Cohesion: 0.07
Nodes (13): OnlineProfile, OnlineUserRepository, ValueEventListener, GameModeCard(), HeroBoardCard(), HomeTabScreen(), MiniBoard(), StatChip() (+5 more)

### Community 4 - "Online Room Lobby UI"
Cohesion: 0.06
Nodes (7): OnlineRoom, RoomStatus, OnlineRoomCard(), OnlineTabScreen(), LocalGameSessionViewModel, OnlineBattleUiState, OnlineBattleViewModel

### Community 7 - "App Routing & Settings UI"
Cohesion: 0.10
Nodes (13): MainActivity, AppRoute, ProfileSettingsTabScreen(), SectionTitle(), SettingCard(), StatBox(), ThemeChoice(), ToggleRow() (+5 more)

### Community 8 - "Local Preferences & Online Board"
Cohesion: 0.16
Nodes (10): LocalPreferences, LocalSettings, compositeOver(), MandatoryOnlineJokerDialog(), OnlineBoard(), OnlineGameScreen(), OnlineHandCard(), pieceGlyph() (+2 more)

### Community 12 - "First Turn Draw"
Cohesion: 0.24
Nodes (7): FirstTurnDecider, FirstTurnDrawResult, LocalPlayerSlot, PLAYER1, PLAYER2, DrawPlayerRow(), FirstTurnDrawScreen()

### Community 13 - "Online Lobby Screen"
Cohesion: 0.27
Nodes (3): LobbyRule(), OnlineLobbyPlayerCard(), OnlineLobbyScreen()

### Community 14 - "Chess Piece Types"
Cohesion: 0.29
Nodes (7): PieceType, BISHOP, KING, KNIGHT, PAWN, QUEEN, ROOK

### Community 15 - "Graphify Project Rules"
Cohesion: 0.40
Nodes (4): graphify-out/graph.json, GRAPH_REPORT.md, graphify knowledge graph rules, graphify-out/wiki/index.md

## Knowledge Gaps
- **18 isolated node(s):** `PLAYER1`, `PLAYER2`, `WHITE`, `BLACK`, `KING` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 96 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `User Profile & Auth` to `Game Models & Cards`, `Online Room Repository`, `Online Room Lobby UI`, `Local Game Screens`, `Game Screen Animations`, `App Routing & Settings UI`, `Local Preferences & Online Board`, `Online Tab Widgets`, `First Turn Draw`, `Online Lobby Screen`?**
  _High betweenness centrality (0.165) - this node is a cross-community bridge._
- **What connects `PLAYER1`, `PLAYER2`, `WHITE` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chess Board Engine` be split into smaller, more focused modules?**
  _Cohesion score 0.08413461538461539 - nodes in this community are weakly interconnected._
- **Why does `Move` connect `Chess Board Engine` to `Game Models & Cards`, `Online Room Repository`, `Online Room Lobby UI`, `Game Screen Animations`, `Local Preferences & Online Board`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Should `Game Models & Cards` be split into smaller, more focused modules?**
  _Cohesion score 0.08627450980392157 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `Game Models & Cards` to `Chess Board Engine`, `Game Screen Animations`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Should `Online Room Repository` be split into smaller, more focused modules?**
  _Cohesion score 0.08792270531400966 - nodes in this community are weakly interconnected._