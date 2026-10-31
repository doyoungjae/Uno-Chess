import React, { useState, useEffect } from 'react';
import { Card as CardType, CardColor, GameState, Player } from './types/game';
import { 
  initializeGame, 
  canPlayCard, 
  getNextPlayerIndex, 
  applyCardEffect, 
  computerPlayCard,
  checkWinner
} from './utils/gameUtils';
import PlayerHand from './components/PlayerHand';
import ComputerHand from './components/ComputerHand';
import GameBoard from './components/GameBoard';
import GameControls from './components/GameControls';
import GameOverModal from './components/GameOverModal';
import WelcomeScreen from './components/WelcomeScreen';

function App() {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [showWelcome, setShowWelcome] = useState(true);

  // Initialize game
  const startGame = (playerName: string, numComputers: number) => {
    setGameState(initializeGame(playerName, numComputers));
    setShowWelcome(false);
  };

  // Restart game
  const restartGame = () => {
    setShowWelcome(true);
    setGameState(null);
  };

  // Draw a card from the deck
  const drawCard = () => {
    if (!gameState) return;

    const updatedState = { ...gameState };
    const currentPlayer = updatedState.players[updatedState.currentPlayerIndex];

    // Draw a card if there are cards in the deck
    if (updatedState.deck.length > 0) {
      const card = updatedState.deck.pop()!;
      currentPlayer.cards.push(card);
    } 
    // If deck is empty, reshuffle discard pile
    else if (updatedState.discardPile.length > 1) {
      const topCard = updatedState.discardPile.pop()!;
      updatedState.deck = [...updatedState.discardPile];
      updatedState.discardPile = [topCard];
      
      const card = updatedState.deck.pop()!;
      currentPlayer.cards.push(card);
    }

    updatedState.message = `${currentPlayer.name} drew a card`;
    
    // Move to next player
    updatedState.currentPlayerIndex = getNextPlayerIndex(
      updatedState.currentPlayerIndex,
      updatedState.direction,
      updatedState.players.length
    );

    setGameState(updatedState);
  };

  // Play a card
  const playCard = (card: CardType) => {
    if (!gameState) return;

    const updatedState = { ...gameState };
    const currentPlayer = updatedState.players[updatedState.currentPlayerIndex];
    const topCard = updatedState.discardPile[updatedState.discardPile.length - 1];

    // Check if card can be played
    if (!canPlayCard(card, topCard, updatedState.selectedColor)) {
      return;
    }

    // Remove card from player's hand
    currentPlayer.cards = currentPlayer.cards.filter(c => c.id !== card.id);

    // Add card to discard pile
    updatedState.discardPile.push(card);

    // Check if player has won
    if (checkWinner(currentPlayer)) {
      updatedState.gameOver = true;
      updatedState.winner = currentPlayer;
      updatedState.message = `${currentPlayer.name} has won the game!`;
      setGameState(updatedState);
      return;
    }

    // Check if card is wild and needs color selection
    if (card.color === 'wild') {
      updatedState.needToSelectColor = true;
      setGameState(updatedState);
      return;
    }

    // Apply card effect
    const stateAfterEffect = applyCardEffect(updatedState, card, null);

    // Move to next player
    stateAfterEffect.currentPlayerIndex = getNextPlayerIndex(
      stateAfterEffect.currentPlayerIndex,
      stateAfterEffect.direction,
      stateAfterEffect.players.length
    );

    setGameState(stateAfterEffect);
  };

  // Select color for wild cards
  const selectColor = (color: CardColor) => {
    if (!gameState) return;

    const updatedState = { ...gameState };
    updatedState.needToSelectColor = false;
    updatedState.selectedColor = color;

    // Get the last played card (which should be a wild card)
    const playedCard = updatedState.discardPile[updatedState.discardPile.length - 1];

    // Apply card effect with the selected color
    const stateAfterEffect = applyCardEffect(updatedState, playedCard, color);

    // Move to next player
    stateAfterEffect.currentPlayerIndex = getNextPlayerIndex(
      stateAfterEffect.currentPlayerIndex,
      stateAfterEffect.direction,
      stateAfterEffect.players.length
    );

    setGameState(stateAfterEffect);
  };

  // Computer player's turn
  useEffect(() => {
    if (!gameState || gameState.gameOver || gameState.needToSelectColor) return;

    const currentPlayer = gameState.players[gameState.currentPlayerIndex];
    
    // If it's a computer's turn
    if (currentPlayer.isComputer) {
      // Add a delay to make it feel more natural
      const timer = setTimeout(() => {
        const updatedState = { ...gameState };
        const topCard = updatedState.discardPile[updatedState.discardPile.length - 1];
        
        // Computer decides which card to play
        const { card, newColor } = computerPlayCard(
          currentPlayer, 
          topCard, 
          updatedState.selectedColor
        );
        
        // If computer can play a card
        if (card) {
          // Remove card from computer's hand
          currentPlayer.cards = currentPlayer.cards.filter(c => c.id !== card.id);
          
          // Add card to discard pile
          updatedState.discardPile.push(card);
          
          // Check if computer has won
          if (checkWinner(currentPlayer)) {
            updatedState.gameOver = true;
            updatedState.winner = currentPlayer;
            updatedState.message = `${currentPlayer.name} has won the game!`;
            setGameState(updatedState);
            return;
          }
          
          // If it's a wild card, set the selected color
          if (card.color === 'wild' && newColor) {
            // Apply card effect with the selected color
            const stateAfterEffect = applyCardEffect(updatedState, card, newColor);
            
            // Move to next player
            stateAfterEffect.currentPlayerIndex = getNextPlayerIndex(
              stateAfterEffect.currentPlayerIndex,
              stateAfterEffect.direction,
              stateAfterEffect.players.length
            );
            
            setGameState(stateAfterEffect);
          } else {
            // Apply card effect
            const stateAfterEffect = applyCardEffect(updatedState, card, null);
            
            // Move to next player
            stateAfterEffect.currentPlayerIndex = getNextPlayerIndex(
              stateAfterEffect.currentPlayerIndex,
              stateAfterEffect.direction,
              stateAfterEffect.players.length
            );
            
            setGameState(stateAfterEffect);
          }
        } 
        // If computer can't play a card, draw one
        else {
          drawCard();
        }
      }, 1500); // 1.5 second delay
      
      return () => clearTimeout(timer);
    }
  }, [gameState]);

  // Check if a card can be played
  const checkCanPlayCard = (card: CardType) => {
    if (!gameState) return false;
    
    const topCard = gameState.discardPile[gameState.discardPile.length - 1];
    return canPlayCard(card, topCard, gameState.selectedColor);
  };

  if (showWelcome) {
    return <WelcomeScreen onStartGame={startGame} />;
  }

  if (!gameState) {
    return <div>Loading...</div>;
  }

  const humanPlayer = gameState.players.find(p => !p.isComputer) as Player;
  const computerPlayers = gameState.players.filter(p => p.isComputer);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 to-green-100 font-nunito p-4">
      <div className="max-w-6xl mx-auto">
        <header className="text-center mb-6">
          <h1 className="text-4xl font-nunito font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-red-500 via-yellow-500 to-blue-500">
            UNO Game
          </h1>
        </header>

        <GameControls
          onStartGame={() => {}}
          onRestartGame={restartGame}
          gameStarted={gameState.gameStarted}
          gameOver={gameState.gameOver}
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          {computerPlayers.map((player) => (
            <ComputerHand
              key={player.id}
              player={player}
              isCurrentPlayer={gameState.currentPlayerIndex === gameState.players.indexOf(player)}
            />
          ))}
        </div>

        <div className="mb-6">
          <GameBoard
            discardPile={gameState.discardPile}
            deckCount={gameState.deck.length}
            onDrawCard={drawCard}
            direction={gameState.direction}
            message={gameState.message}
            needToSelectColor={gameState.needToSelectColor}
            onSelectColor={selectColor}
          />
        </div>

        <div className="mb-6">
          <PlayerHand
            player={humanPlayer}
            isCurrentPlayer={gameState.currentPlayerIndex === gameState.players.indexOf(humanPlayer)}
            onPlayCard={playCard}
            canPlayCard={checkCanPlayCard}
          />
        </div>

        {gameState.gameOver && gameState.winner && (
          <GameOverModal winner={gameState.winner} onRestartGame={restartGame} />
        )}
      </div>
    </div>
  );
}

export default App;
