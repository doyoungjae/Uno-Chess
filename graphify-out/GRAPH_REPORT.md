# Graph Report - Uno-Chess  (2026-11-26)

## Corpus Check
- 54 files · ~28,204 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: .properties 3, (none) 2, .xml 2)

## Summary
- 616 nodes · 1805 edges · 27 communities (14 shown, 13 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 73 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `341f9f0f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Player
- UnoChessGame
- OnlineProfile
- CustomizeScreen.kt
- GameComponents.kt
- OnlineRoomRepository
- UnoChessApp.kt
- RateAppSettingsScreen.kt
- ReleaseGameScreen.kt
- SplashScreen.kt
- OnlineUserRepository
- UnoChessColors
- ChessAudio.kt
- OnlineLobbyScreen.kt
- OnlineTabScreen.kt
- fillmaxsize
- .settle
- ♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)
- HomeTabScreen.kt
- ProfileSettingsTabScreen.kt
- SettingsCategory
- CLAUDE.md
- README.md

## God Nodes (most connected - your core abstractions)
1. `UnoChessGame` - 56 edges
2. `UnoCard` - 36 edges
3. `LocalSettings` - 33 edges
4. `Player` - 32 edges
5. `OnlineGameScreen()` - 32 edges
6. `OnlineGameState` - 29 edges
7. `OnlineProfile` - 29 edges
8. `UnoChessApp()` - 23 edges
9. `OnlineBattleViewModel` - 23 edges
10. `Move` - 22 edges

## Surprising Connections (you probably didn't know these)
- `UnoChessApp()` --calls--> `SettingsMainScreen()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/UnoChessApp.kt → app/src/main/java/com/example/unochessprototype/ui/screen/SettingsMainScreen.kt
- `UnoChessApp()` --calls--> `SoundSettingsScreen()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/UnoChessApp.kt → app/src/main/java/com/example/unochessprototype/ui/screen/SoundSettingsScreen.kt
- `UnoChessApp()` --calls--> `StatisticsSettingsScreen()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/UnoChessApp.kt → app/src/main/java/com/example/unochessprototype/ui/screen/StatisticsSettingsScreen.kt
- `OnlineGameScreen()` --calls--> `rememberChessAudio()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/screen/OnlineGameScreen.kt → app/src/main/java/com/example/unochessprototype/ui/screen/ChessAudio.kt
- `ReleaseGameScreen()` --calls--> `rememberChessAudio()`  [INFERRED]
  app/src/main/java/com/example/unochessprototype/ui/screen/ReleaseGameScreen.kt → app/src/main/java/com/example/unochessprototype/ui/screen/ChessAudio.kt

## Import Cycles
- None detected.

## Communities (27 total, 13 thin omitted)

### Community 0 - "Player"
Cohesion: 0.08
Nodes (19): ChessBoard, Move, MoveOutcome, Piece, Player, BLACK, WHITE, PlayResult (+11 more)

### Community 1 - "UnoChessGame"
Cohesion: 0.07
Nodes (13): FirstTurnRound, LocalPlayerSlot, PLAYER1, PLAYER2, GameSnapshot, Draw, ExchangeResult, GameResult (+5 more)

### Community 2 - "OnlineProfile"
Cohesion: 0.05
Nodes (8): OnlineProfile, OnlineRoom, RoomStatus, LocalGameSessionViewModel, OnlineBattleUiState, OnlineBattleViewModel, UserProfileUiState, UserProfileViewModel

### Community 3 - "CustomizeScreen.kt"
Cohesion: 0.08
Nodes (17): CustomThemeSet, LocalPreferences, LocalSettings, savedSetImagePaths(), CustomizeCard(), CustomizeScreen(), ImagePickRow(), SaveThemeSetRow() (+9 more)

### Community 4 - "GameComponents.kt"
Cohesion: 0.07
Nodes (29): CardColor, BLUE, GREEN, RED, YELLOW, PieceType, BISHOP, KING (+21 more)

### Community 5 - "OnlineRoomRepository"
Cohesion: 0.08
Nodes (9): OnlineRoomRepository, Handler, Handler, ValueEventListener, ValueEventListener, Handler, Handler, Handler (+1 more)

### Community 6 - "UnoChessApp.kt"
Cohesion: 0.06
Nodes (13): MainActivity, AppRoute, BottomDestination, MainBottomBar(), FriendsSettingsScreen(), RateAppSettingsScreen(), SplashScreen(), appColorScheme() (+5 more)

### Community 7 - "RateAppSettingsScreen.kt"
Cohesion: 0.19
Nodes (3): SoundSettingsScreen(), StatisticRow(), StatisticsSettingsScreen()

### Community 13 - "OnlineLobbyScreen.kt"
Cohesion: 0.25
Nodes (6): LobbyPlayerCard(), LocalGameTabScreen(), RuleLine(), LobbyRule(), OnlineLobbyPlayerCard(), OnlineLobbyScreen()

### Community 16 - ".settle"
Cohesion: 0.32
Nodes (3): OnlineResultType, OnlineClock, OnlineClockTest

### Community 17 - "♟️ [프로젝트 기획서] 우노 체스 (Uno Chess)"
Cohesion: 0.15
Nodes (12): 1. 개요, 2.1. 개발 목적, 2.2. 주요 기능 및 UI/UX 구조, 2. 목적 및 구현 계획, 3.1. 게임 준비 및 진행 규칙, 3.2. 터치 및 키 이벤트(인게임 조작 방식) 상세 설계, 3. 게임 플레이 및 규칙 설계, 4.1. 기대 효과 (+4 more)

### Community 18 - "HomeTabScreen.kt"
Cohesion: 0.38
Nodes (5): GameModeCard(), HeroBoardCard(), HomeTabScreen(), MiniBoard(), StatChip()

### Community 19 - "ProfileSettingsTabScreen.kt"
Cohesion: 0.42
Nodes (5): ProfileSettingsTabScreen(), SectionTitle(), SettingCard(), StatBox(), ToggleRow()

### Community 20 - "SettingsCategory"
Cohesion: 0.33
Nodes (6): SettingsCategory, FRIENDS, PROFILE, RATE_APP, SOUND, STATISTICS

## Knowledge Gaps
- **19 isolated node(s):** `PLAYER1`, `PLAYER2`, `WHITE`, `BLACK`, `KING` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 146 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OnlineProfile` connect `OnlineProfile` to `Player`, `GameComponents.kt`, `OnlineRoomRepository`, `RateAppSettingsScreen.kt`, `ReleaseGameScreen.kt`, `OnlineUserRepository`, `OnlineLobbyScreen.kt`, `OnlineTabScreen.kt`, `HomeTabScreen.kt`, `ProfileSettingsTabScreen.kt`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Are the 12 inferred relationships involving `UnoChessGame` (e.g. with `.deckHasEightOfEachColorAndEightJokers()` and `.firstTurnLoserBecomesFirstDiscardAndNoCardIsLost()`) actually correct?**
  _`UnoChessGame` has 12 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PLAYER1`, `PLAYER2`, `WHITE` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Player` be split into smaller, more focused modules?**
  _Cohesion score 0.0848982785602504 - nodes in this community are weakly interconnected._
- **Why does `UnoChessGame` connect `UnoChessGame` to `Player`, `ReleaseGameScreen.kt`, `GameComponents.kt`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `UnoCard` (e.g. with `.card()` and `.firstTurnJokerLosesAndTiesRedraw()`) actually correct?**
  _`UnoCard` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Should `UnoChessGame` be split into smaller, more focused modules?**
  _Cohesion score 0.06829488919041157 - nodes in this community are weakly interconnected._