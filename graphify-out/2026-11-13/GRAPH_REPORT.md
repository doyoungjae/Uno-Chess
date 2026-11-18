# Graph Report - Uno-Chess  (2026-11-13)

## Corpus Check
- 39 files · ~21,480 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: .properties 3, (none) 2, .xml 2)

## Summary
- 494 nodes · 1265 edges · 19 communities (10 shown, 9 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 31 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b148e420`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Move
- UnoChessGame
- Online Room Repository
- User Profile & Auth
- Online Room Lobby UI
- ProfileSettingsTabScreen.kt
- ReleaseGameScreen.kt
- ♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)
- MainBottomBar.kt
- Graphify Project Rules
- Project Overview

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 43 edges
2. `UnoCard` - 32 edges
3. `Player` - 28 edges
4. `OnlineProfile` - 28 edges
5. `ChessBoard` - 23 edges
6. `ReleaseGameScreen()` - 22 edges
7. `Move` - 21 edges
8. `OnlineBattleViewModel` - 21 edges
9. `Square` - 20 edges
10. `OnlineGameState` - 19 edges

## Surprising Connections (you probably didn't know these)
- `2. UI/UX 및 시각적 피드백 검토 사항` --references--> `ChessBoard`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/ChessBoard.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `OnlineRoomRepository`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/online/repository/OnlineRoomRepository.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `LocalPreferences`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `1. 게임 규칙 및 핵심 엔진 (`UnoChessGame.kt`) 검토 사항` --references--> `UnoChessGame`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/UnoChessGame.kt
- `1. 게임 규칙 및 핵심 엔진 (`UnoChessGame.kt`) 검토 사항` --references--> `GameResult`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/Models.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph navigation sources** — claude_graph_json, claude_wiki_index, claude_graph_report [EXTRACTED 0.75]

## Communities (19 total, 9 thin omitted)

### Community 0 - "Move"
Cohesion: 0.09
Nodes (18): Move, Piece, PieceType, BISHOP, KING, KNIGHT, PAWN, QUEEN (+10 more)

### Community 1 - "UnoChessGame"
Cohesion: 0.06
Nodes (23): ChessBoard, FirstTurnRound, LocalPlayerSlot, PLAYER1, PLAYER2, CardColor, BLUE, GREEN (+15 more)

### Community 2 - "Online Room Repository"
Cohesion: 0.08
Nodes (10): RoomStatus, OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler (+2 more)

### Community 3 - "User Profile & Auth"
Cohesion: 0.09
Nodes (5): OnlineUserRepository, ValueEventListener, LocalGameSessionViewModel, UserProfileUiState, UserProfileViewModel

### Community 4 - "Online Room Lobby UI"
Cohesion: 0.06
Nodes (12): OnlineProfile, OnlineRoom, LobbyRule(), OnlineLobbyPlayerCard(), OnlineLobbyScreen(), OnlineRoomCard(), OnlineTabScreen(), OnlineBattleUiState (+4 more)

### Community 5 - "ProfileSettingsTabScreen.kt"
Cohesion: 0.05
Nodes (10): BottomDestination, MainBottomBar(), GameModeCard(), HeroBoardCard(), HomeTabScreen(), MiniBoard(), StatChip(), LobbyPlayerCard() (+2 more)

### Community 6 - "ReleaseGameScreen.kt"
Cohesion: 0.07
Nodes (19): CardAnchors, CardBack(), CardFlight, CardFlightLayer(), CardFlights, ChessClock(), CompactPlayerPanel(), DeckColumn() (+11 more)

### Community 9 - "♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)"
Cohesion: 0.15
Nodes (12): 1. 개요, 2.1. 개발 목적, 2.2. 주요 기능 및 UI/UX 구조, 2. 목적 및 구현 계획, 3.1. 게임 준비 및 진행 규칙, 3.2. 터치 및 키 이벤트(인게임 조작 방식) 상세 설계, 3. 게임 플레이 및 규칙 설계, 4.1. 기대 효과 (+4 more)

### Community 10 - "MainBottomBar.kt"
Cohesion: 0.06
Nodes (23): LocalPreferences, LocalSettings, MainActivity, AppRoute, compositeOver(), MandatoryOnlineJokerDialog(), OnlineBoard(), OnlineGameScreen() (+15 more)

### Community 15 - "Graphify Project Rules"
Cohesion: 0.40
Nodes (4): graphify-out/graph.json, GRAPH_REPORT.md, graphify knowledge graph rules, graphify-out/wiki/index.md

## Knowledge Gaps
- **18 isolated node(s):** `PLAYER1`, `PLAYER2`, `BLUE`, `GREEN`, `RED` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 119 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `Online Room Lobby UI` to `Online Room Repository`, `User Profile & Auth`, `ProfileSettingsTabScreen.kt`, `ReleaseGameScreen.kt`, `MainBottomBar.kt`?**
  _High betweenness centrality (0.160) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `UnoChessGame` (e.g. with `.firstTurnLoserBecomesFirstDiscardAndNoCardIsLost()` and `.noPlayableCardDiscardsTwoAndDrawsOne()`) actually correct?**
  _`UnoChessGame` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PLAYER1`, `PLAYER2`, `BLUE` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Move` be split into smaller, more focused modules?**
  _Cohesion score 0.08743169398907104 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `UnoChessGame` to `Move`, `ReleaseGameScreen.kt`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `UnoCard` (e.g. with `.card()` and `.firstTurnJokerLosesAndTiesRedraw()`) actually correct?**
  _`UnoCard` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `UnoChessGame` be split into smaller, more focused modules?**
  _Cohesion score 0.06409130816505706 - nodes in this community are weakly interconnected._