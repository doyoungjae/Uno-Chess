# Graph Report - Uno-Chess  (2026-11-13)

## Corpus Check
- 40 files · ~22,875 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: .properties 3, (none) 2, .xml 2)

## Summary
- 533 nodes · 1373 edges · 33 communities (15 shown, 18 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 28 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b148e420`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OnlineGameState
- UnoChessGame
- Online Room Repository
- User Profile & Auth
- Online Room Lobby UI
- ReleaseGameScreen.kt
- OnlineGameScreen.kt
- ♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)
- MainBottomBar.kt
- Player
- Square
- Graphify Project Rules
- Project Overview
- OnlineLobbyScreen.kt
- OnlineGameRulesTest.kt
- MainBottomBar.kt
- HomeTabScreen.kt
- Models.kt
- FirstTurnRound
- GameViewModel.kt

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 43 edges
2. `UnoCard` - 32 edges
3. `OnlineProfile` - 28 edges
4. `Player` - 28 edges
5. `ChessBoard` - 23 edges
6. `LocalSettings` - 22 edges
7. `ReleaseGameScreen()` - 22 edges
8. `Move` - 21 edges
9. `OnlineBattleViewModel` - 21 edges
10. `Square` - 20 edges

## Surprising Connections (you probably didn't know these)
- `2. UI/UX 및 시각적 피드백 검토 사항` --references--> `ChessBoard`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/ChessBoard.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `LocalPreferences`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `1. 게임 규칙 및 핵심 엔진 (`UnoChessGame.kt`) 검토 사항` --references--> `GameResult`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/Models.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `OnlineRoomRepository`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/online/repository/OnlineRoomRepository.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `OnlineProfile`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/online/model/OnlineProfile.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph navigation sources** — claude_graph_json, claude_wiki_index, claude_graph_report [EXTRACTED 0.75]

## Communities (33 total, 18 thin omitted)

### Community 0 - "OnlineGameState"
Cohesion: 0.18
Nodes (9): OnlineGameState, OnlineGameRules, compositeOver(), MandatoryOnlineJokerDialog(), OnlineBoard(), OnlineGameScreen(), OnlineHandCard(), pieceGlyph() (+1 more)

### Community 1 - "UnoChessGame"
Cohesion: 0.19
Nodes (3): UnoCard, UnoChessGame, 1. 게임 규칙 및 핵심 엔진 (`UnoChessGame.kt`) 검토 사항

### Community 2 - "Online Room Repository"
Cohesion: 0.08
Nodes (10): RoomStatus, OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler (+2 more)

### Community 3 - "User Profile & Auth"
Cohesion: 0.09
Nodes (5): OnlineUserRepository, ValueEventListener, LocalGameSessionViewModel, UserProfileUiState, UserProfileViewModel

### Community 4 - "Online Room Lobby UI"
Cohesion: 0.06
Nodes (12): OnlineProfile, OnlineRoom, LobbyRule(), OnlineLobbyPlayerCard(), OnlineLobbyScreen(), OnlineRoomCard(), OnlineTabScreen(), OnlineBattleUiState (+4 more)

### Community 6 - "ReleaseGameScreen.kt"
Cohesion: 0.06
Nodes (25): LocalPreferences, LocalSettings, AnimatedChessBoard(), CardAnchors, CardFlight, CardFlightLayer(), CardFlights, ChessClock() (+17 more)

### Community 9 - "♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)"
Cohesion: 0.15
Nodes (12): 1. 개요, 2.1. 개발 목적, 2.2. 주요 기능 및 UI/UX 구조, 2. 목적 및 구현 계획, 3.1. 게임 준비 및 진행 규칙, 3.2. 터치 및 키 이벤트(인게임 조작 방식) 상세 설계, 3. 게임 플레이 및 규칙 설계, 4.1. 기대 효과 (+4 more)

### Community 10 - "MainBottomBar.kt"
Cohesion: 0.08
Nodes (21): MainActivity, AppRoute, CustomizeCard(), GameModeCard(), HeroBoardCard(), HomeTabScreen(), ImagePickRow(), MiniBoard() (+13 more)

### Community 11 - "Player"
Cohesion: 0.16
Nodes (5): ChessBoard, Move, Player, BLACK, WHITE

### Community 14 - "Square"
Cohesion: 0.20
Nodes (5): Piece, Square, BoardMoveAnimation, UnoChessGameEndgameTest, OnlineGameRulesTest

### Community 15 - "Graphify Project Rules"
Cohesion: 0.40
Nodes (4): graphify-out/graph.json, GRAPH_REPORT.md, graphify knowledge graph rules, graphify-out/wiki/index.md

### Community 21 - "OnlineLobbyScreen.kt"
Cohesion: 0.20
Nodes (3): LobbyPlayerCard(), LocalGameTabScreen(), RuleLine()

### Community 22 - "OnlineGameRulesTest.kt"
Cohesion: 0.14
Nodes (8): PieceType, BISHOP, KING, KNIGHT, PAWN, QUEEN, ROOK, OnlineResultType

### Community 26 - "Models.kt"
Cohesion: 0.21
Nodes (11): CardColor, BLUE, GREEN, RED, YELLOW, Draw, ExchangeResult, GameResult (+3 more)

### Community 27 - "FirstTurnRound"
Cohesion: 0.50
Nodes (4): FirstTurnRound, LocalPlayerSlot, PLAYER1, PLAYER2

## Knowledge Gaps
- **18 isolated node(s):** `BISHOP`, `KING`, `KNIGHT`, `PAWN`, `QUEEN` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 137 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `Online Room Lobby UI` to `OnlineGameState`, `Online Room Repository`, `User Profile & Auth`, `OnlineTabScreen.kt`, `ReleaseGameScreen.kt`, `OnlineGameScreen.kt`, `MainBottomBar.kt`, `OnlineLobbyScreen.kt`, `HomeTabScreen.kt`?**
  _High betweenness centrality (0.148) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `UnoChessGame` (e.g. with `.firstTurnLoserBecomesFirstDiscardAndNoCardIsLost()` and `.noPlayableCardDiscardsTwoAndDrawsOne()`) actually correct?**
  _`UnoChessGame` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `BISHOP`, `KING`, `KNIGHT` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Online Room Repository` be split into smaller, more focused modules?**
  _Cohesion score 0.08244680851063829 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `UnoChessGame` to `ReleaseGameScreen.kt`, `Player`, `Square`, `OnlineGameRulesTest.kt`, `Models.kt`, `FirstTurnRound`, `GameViewModel.kt`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `UnoCard` (e.g. with `.card()` and `.firstTurnJokerLosesAndTiesRedraw()`) actually correct?**
  _`UnoCard` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `User Profile & Auth` be split into smaller, more focused modules?**
  _Cohesion score 0.08558558558558559 - nodes in this community are weakly interconnected._