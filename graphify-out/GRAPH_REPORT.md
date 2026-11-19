# Graph Report - Uno-Chess  (2026-11-15)

## Corpus Check
- 42 files · ~23,220 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: .properties 3, (none) 2, .xml 2)

## Summary
- 552 nodes · 1448 edges · 28 communities (10 shown, 18 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 41 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b148e420`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ChessBoard
- UnoChessGame
- Online Room Repository
- User Profile & Auth
- Online Room Lobby UI
- ThemedVisuals.kt
- OnlineGameScreen.kt
- ♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)
- UnoChessTheme.kt
- ReleaseGameScreen.kt
- Graphify Project Rules
- Project Overview
- ProfileSettingsTabScreen.kt
- UnoChessApp.kt

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 45 edges
2. `UnoCard` - 32 edges
3. `Player` - 28 edges
4. `OnlineProfile` - 28 edges
5. `LocalSettings` - 24 edges
6. `ChessBoard` - 23 edges
7. `Move` - 21 edges
8. `OnlineBattleViewModel` - 21 edges
9. `ReleaseGameScreen()` - 21 edges
10. `Square` - 20 edges

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

## Communities (28 total, 18 thin omitted)

### Community 0 - "ChessBoard"
Cohesion: 0.07
Nodes (22): ChessBoard, Move, Piece, PieceType, BISHOP, KING, KNIGHT, PAWN (+14 more)

### Community 1 - "UnoChessGame"
Cohesion: 0.07
Nodes (25): FirstTurnRound, LocalPlayerSlot, PLAYER1, PLAYER2, CardColor, BLUE, GREEN, RED (+17 more)

### Community 2 - "Online Room Repository"
Cohesion: 0.09
Nodes (9): OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler, Handler (+1 more)

### Community 3 - "User Profile & Auth"
Cohesion: 0.09
Nodes (5): OnlineUserRepository, ValueEventListener, LocalGameSessionViewModel, UserProfileUiState, UserProfileViewModel

### Community 4 - "Online Room Lobby UI"
Cohesion: 0.06
Nodes (16): OnlineProfile, OnlineRoom, RoomStatus, LobbyPlayerCard(), LocalGameTabScreen(), RuleLine(), LobbyRule(), OnlineLobbyPlayerCard() (+8 more)

### Community 6 - "ThemedVisuals.kt"
Cohesion: 0.07
Nodes (34): LocalPreferences, LocalSettings, CustomizeCard(), CustomizeScreen(), ImagePickRow(), ThemePresetChip(), GameModeCard(), HeroBoardCard() (+26 more)

### Community 9 - "♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)"
Cohesion: 0.15
Nodes (12): 1. 개요, 2.1. 개발 목적, 2.2. 주요 기능 및 UI/UX 구조, 2. 목적 및 구현 계획, 3.1. 게임 준비 및 진행 규칙, 3.2. 터치 및 키 이벤트(인게임 조작 방식) 상세 설계, 3. 게임 플레이 및 규칙 설계, 4.1. 기대 효과 (+4 more)

### Community 10 - "UnoChessTheme.kt"
Cohesion: 0.18
Nodes (4): MainActivity, BoardPalette, boardPalette(), UnoChessTheme()

### Community 15 - "Graphify Project Rules"
Cohesion: 0.40
Nodes (4): graphify-out/graph.json, GRAPH_REPORT.md, graphify knowledge graph rules, graphify-out/wiki/index.md

### Community 23 - "UnoChessApp.kt"
Cohesion: 0.07
Nodes (10): AppRoute, BottomDestination, MainBottomBar(), ProfileSettingsTabScreen(), SectionTitle(), SettingCard(), StatBox(), ToggleRow() (+2 more)

## Knowledge Gaps
- **18 isolated node(s):** `BLACK`, `WHITE`, `BISHOP`, `KING`, `KNIGHT` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 144 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `Online Room Lobby UI` to `ChessBoard`, `Online Room Repository`, `User Profile & Auth`, `OnlineTabScreen.kt`, `ThemedVisuals.kt`, `OnlineGameScreen.kt`, `ReleaseGameScreen.kt`, `ProfileSettingsTabScreen.kt`, `UnoChessApp.kt`, `CustomizeScreen.kt`?**
  _High betweenness centrality (0.144) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `UnoChessGame` (e.g. with `.deckHasEightOfEachColorAndEightJokers()` and `.firstTurnLoserBecomesFirstDiscardAndNoCardIsLost()`) actually correct?**
  _`UnoChessGame` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `BLACK`, `WHITE`, `BISHOP` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ChessBoard` be split into smaller, more focused modules?**
  _Cohesion score 0.06812291743798593 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `UnoChessGame` to `ChessBoard`, `ReleaseGameScreen.kt`, `ThemedVisuals.kt`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `UnoCard` (e.g. with `.card()` and `.firstTurnJokerLosesAndTiesRedraw()`) actually correct?**
  _`UnoCard` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `UnoChessGame` be split into smaller, more focused modules?**
  _Cohesion score 0.06946386946386947 - nodes in this community are weakly interconnected._