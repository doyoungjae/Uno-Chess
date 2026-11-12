# Graph Report - Uno-Chess  (2026-11-12)

## Corpus Check
- 40 files · ~20,500 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: .properties 3, (none) 2, .xml 2)

## Summary
- 476 nodes · 1240 edges · 19 communities (12 shown, 7 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 20 edges (avg confidence: 0.89)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7f4e7827`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Chess Board Engine
- Game Models & Cards
- Online Room Repository
- User Profile & Auth
- Online Room Lobby UI
- ProfileSettingsTabScreen.kt
- Game Screen Animations
- App Routing & Settings UI
- Local Preferences & Online Board
- ♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)
- First Turn Draw
- Graphify Project Rules
- Project Overview

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 36 edges
2. `OnlineProfile` - 30 edges
3. `Player` - 26 edges
4. `UnoCard` - 24 edges
5. `ChessBoard` - 23 edges
6. `OnlineBattleViewModel` - 22 edges
7. `Move` - 21 edges
8. `Square` - 19 edges
9. `OnlineGameState` - 19 edges
10. `OnlineRoomRepository` - 19 edges

## Surprising Connections (you probably didn't know these)
- `2. UI/UX 및 시각적 피드백 검토 사항` --references--> `ChessBoard`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/ChessBoard.kt
- `1. 게임 규칙 및 핵심 엔진 (`UnoChessGame.kt`) 검토 사항` --references--> `FirstTurnDecider`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/game/FirstTurnDecider.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `LocalPreferences`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/data/LocalPreferences.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `OnlineRoomRepository`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/online/repository/OnlineRoomRepository.kt
- `3. 인프라 및 부가 기능 검토 사항` --references--> `OnlineBattleViewModel`  [INFERRED]
  fix.md → app/src/main/java/com/example/unochessprototype/viewmodel/OnlineBattleViewModel.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph navigation sources** — claude_graph_json, claude_wiki_index, claude_graph_report [EXTRACTED 0.75]

## Communities (19 total, 7 thin omitted)

### Community 0 - "Chess Board Engine"
Cohesion: 0.07
Nodes (20): ChessBoard, Move, Piece, PieceType, BISHOP, KING, KNIGHT, PAWN (+12 more)

### Community 1 - "Game Models & Cards"
Cohesion: 0.11
Nodes (15): CardColor, BLUE, GREEN, RED, YELLOW, Draw, ExchangeResult, GameResult (+7 more)

### Community 2 - "Online Room Repository"
Cohesion: 0.09
Nodes (9): OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler, Handler (+1 more)

### Community 3 - "User Profile & Auth"
Cohesion: 0.09
Nodes (8): OnlineProfile, OnlineUserRepository, ValueEventListener, UserProfileUiState, UserProfileViewModel, 2. UI/UX 및 시각적 피드백 검토 사항, 3. 인프라 및 부가 기능 검토 사항, Uno-Chess 프로젝트 구현 점검 및 수정 체크리스트

### Community 4 - "Online Room Lobby UI"
Cohesion: 0.06
Nodes (7): OnlineRoom, RoomStatus, OnlineRoomCard(), OnlineTabScreen(), LocalGameSessionViewModel, OnlineBattleUiState, OnlineBattleViewModel

### Community 5 - "ProfileSettingsTabScreen.kt"
Cohesion: 0.06
Nodes (13): BottomDestination, MainBottomBar(), GameModeCard(), HeroBoardCard(), HomeTabScreen(), MiniBoard(), StatChip(), LobbyPlayerCard() (+5 more)

### Community 6 - "Game Screen Animations"
Cohesion: 0.07
Nodes (15): AnimatedChessBoard(), CardBack(), CompactPlayerPanel(), DraggableFanCard(), FanCardHand(), GameGuide(), GameHeader(), GameLogOverlay() (+7 more)

### Community 7 - "App Routing & Settings UI"
Cohesion: 0.10
Nodes (13): MainActivity, AppRoute, ProfileSettingsTabScreen(), SectionTitle(), SettingCard(), StatBox(), ThemeChoice(), ToggleRow() (+5 more)

### Community 8 - "Local Preferences & Online Board"
Cohesion: 0.21
Nodes (10): LocalPreferences, LocalSettings, compositeOver(), MandatoryOnlineJokerDialog(), OnlineBoard(), OnlineGameScreen(), OnlineHandCard(), pieceGlyph() (+2 more)

### Community 9 - "♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)"
Cohesion: 0.15
Nodes (12): 1. 개요, 2.1. 개발 목적, 2.2. 주요 기능 및 UI/UX 구조, 2. 목적 및 구현 계획, 3.1. 게임 준비 및 진행 규칙, 3.2. 터치 및 키 이벤트(인게임 조작 방식) 상세 설계, 3. 게임 플레이 및 규칙 설계, 4.1. 기대 효과 (+4 more)

### Community 12 - "First Turn Draw"
Cohesion: 0.24
Nodes (7): FirstTurnDecider, FirstTurnDrawResult, LocalPlayerSlot, PLAYER1, PLAYER2, DrawPlayerRow(), FirstTurnDrawScreen()

### Community 15 - "Graphify Project Rules"
Cohesion: 0.40
Nodes (4): graphify-out/graph.json, GRAPH_REPORT.md, graphify knowledge graph rules, graphify-out/wiki/index.md

## Knowledge Gaps
- **18 isolated node(s):** `BLACK`, `WHITE`, `BLUE`, `GREEN`, `RED` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 109 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `User Profile & Auth` to `Online Room Repository`, `Online Room Lobby UI`, `ProfileSettingsTabScreen.kt`, `Game Screen Animations`, `App Routing & Settings UI`, `Local Preferences & Online Board`, `First Turn Draw`?**
  _High betweenness centrality (0.156) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `UnoChessGame` (e.g. with `.numberCardsActAsJokerWhenFourOrFewerPiecesRemain()` and `.standardBoardIsNotEndgame()`) actually correct?**
  _`UnoChessGame` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `BLACK`, `WHITE`, `BLUE` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chess Board Engine` be split into smaller, more focused modules?**
  _Cohesion score 0.0699099099099099 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `Game Models & Cards` to `Chess Board Engine`, `Game Screen Animations`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **Should `Game Models & Cards` be split into smaller, more focused modules?**
  _Cohesion score 0.1076923076923077 - nodes in this community are weakly interconnected._
- **Why does `Move` connect `Chess Board Engine` to `Game Models & Cards`, `Online Room Repository`, `Online Room Lobby UI`, `Game Screen Animations`, `Local Preferences & Online Board`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._