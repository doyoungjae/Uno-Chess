import { Card, CardColor, CardValue, GameState, Player } from '../types/game';

// Create a new deck of UNO cards
export const createDeck = (): Card[] => {
  const colors: CardColor[] = ['red', 'blue', 'green', 'yellow'];
  const numbers: CardValue[] = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
  const specials: CardValue[] = ['skip', 'reverse', 'draw2'];
  const wilds: CardValue[] = ['wild', 'wild4'];
  
  const deck: Card[] = [];
  
  // Add number cards (0-9) for each color
  colors.forEach(color => {
    // Only one '0' card per color
    deck.push({ id: `${color}-0-1`, color, value: '0' });
    
    // Two of each 1-9 card per color
    numbers.slice(1).forEach(num => {
      deck.push({ id: `${color}-${num}-1`, color, value: num });
      deck.push({ id: `${color}-${num}-2`, color, value: num });
    });
    
    // Two of each special card per color
    specials.forEach(special => {
      deck.push({ id: `${color}-${special}-1`, color, value: special });
      deck.push({ id: `${color}-${special}-2`, color, value: special });
    });
  });
  
  // Add wild cards (4 of each)
  wilds.forEach(wild => {
    for (let i = 1; i <= 4; i++) {
      deck.push({ id: `wild-${wild}-${i}`, color: 'wild', value: wild });
    }
  });
  
  return shuffleDeck(deck);
};

// Shuffle the deck using Fisher-Yates algorithm
export const shuffleDeck = (deck: Card[]): Card[] => {
  const newDeck = [...deck];
  for (let i = newDeck.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [newDeck[i], newDeck[j]] = [newDeck[j], newDeck[i]];
  }
  return newDeck;
};

// Deal cards to players
export const dealCards = (deck: Card[], players: Player[], cardsPerPlayer: number = 7): { updatedDeck: Card[], updatedPlayers: Player[] } => {
  const updatedDeck = [...deck];
  const updatedPlayers = [...players];
  
  for (let i = 0; i < cardsPerPlayer; i++) {
    for (let j = 0; j < updatedPlayers.length; j++) {
      if (updatedDeck.length > 0) {
        const card = updatedDeck.pop()!;
        updatedPlayers[j].cards.push(card);
      }
    }
  }
  
  return { updatedDeck, updatedPlayers };
};

// Check if a card can be played on top of the current card
export const canPlayCard = (card: Card, topCard: Card, selectedColor: CardColor | null): boolean => {
  // Wild cards can always be played
  if (card.color === 'wild') return true;
  
  // If the top card is wild, check against the selected color
  if (topCard.color === 'wild' && selectedColor) {
    return card.color === selectedColor;
  }
  
  // Regular matching: same color or same value
  return card.color === topCard.color || card.value === topCard.value;
};

// Get the next player index based on current direction
export const getNextPlayerIndex = (currentIndex: number, direction: 1 | -1, totalPlayers: number): number => {
  let nextIndex = currentIndex + direction;
  
  if (nextIndex < 0) {
    nextIndex = totalPlayers - 1;
  } else if (nextIndex >= totalPlayers) {
    nextIndex = 0;
  }
  
  return nextIndex;
};

// Apply special card effects
export const applyCardEffect = (
  gameState: GameState,
  playedCard: Card,
  selectedColor: CardColor | null
): GameState => {
  let state = { ...gameState };
  
  switch (playedCard.value) {
    case 'skip':
      // Skip the next player's turn
      state.currentPlayerIndex = getNextPlayerIndex(
        state.currentPlayerIndex,
        state.direction,
        state.players.length
      );
      state.message = `${state.players[state.currentPlayerIndex].name}'s turn was skipped!`;
      break;
      
    case 'reverse':
      // Reverse the direction of play
      state.direction = state.direction === 1 ? -1 : 1;
      state.message = "Direction reversed!";
      break;
      
    case 'draw2':
      // Next player draws 2 cards
      const nextPlayerIndex = getNextPlayerIndex(
        state.currentPlayerIndex,
        state.direction,
        state.players.length
      );
      
      // Draw 2 cards for the next player
      for (let i = 0; i < 2; i++) {
        if (state.deck.length > 0) {
          const card = state.deck.pop()!;
          state.players[nextPlayerIndex].cards.push(card);
        } else if (state.discardPile.length > 1) {
          // Reshuffle discard pile if deck is empty
          const topCard = state.discardPile.pop()!;
          state.deck = shuffleDeck(state.discardPile);
          state.discardPile = [topCard];
          
          const card = state.deck.pop()!;
          state.players[nextPlayerIndex].cards.push(card);
        }
      }
      
      state.message = `${state.players[nextPlayerIndex].name} draws 2 cards!`;
      break;
      
    case 'wild4':
      // Next player draws 4 cards
      const wildNextPlayerIndex = getNextPlayerIndex(
        state.currentPlayerIndex,
        state.direction,
        state.players.length
      );
      
      // Draw 4 cards for the next player
      for (let i = 0; i < 4; i++) {
        if (state.deck.length > 0) {
          const card = state.deck.pop()!;
          state.players[wildNextPlayerIndex].cards.push(card);
        } else if (state.discardPile.length > 1) {
          // Reshuffle discard pile if deck is empty
          const topCard = state.discardPile.pop()!;
          state.deck = shuffleDeck(state.discardPile);
          state.discardPile = [topCard];
          
          const card = state.deck.pop()!;
          state.players[wildNextPlayerIndex].cards.push(card);
        }
      }
      
      state.message = `${state.players[wildNextPlayerIndex].name} draws 4 cards!`;
      
      // Set the selected color
      if (selectedColor) {
        const lastCard = state.discardPile[state.discardPile.length - 1];
        lastCard.color = selectedColor;
      }
      break;
      
    case 'wild':
      // Set the selected color
      if (selectedColor) {
        const lastCard = state.discardPile[state.discardPile.length - 1];
        lastCard.color = selectedColor;
      }
      state.message = `Color changed to ${selectedColor}!`;
      break;
      
    default:
      state.message = `${state.players[state.currentPlayerIndex].name} played ${playedCard.color} ${playedCard.value}`;
  }
  
  return state;
};

// Computer AI to play a card
export const computerPlayCard = (player: Player, topCard: Card, selectedColor: CardColor | null): { card: Card | null, newColor: CardColor | null } => {
  // First try to find a matching card
  const playableCards = player.cards.filter(card => canPlayCard(card, topCard, selectedColor));
  
  if (playableCards.length === 0) {
    return { card: null, newColor: null };
  }
  
  // Prioritize special cards
  const specialCards = playableCards.filter(card => 
    card.value === 'skip' || card.value === 'reverse' || 
    card.value === 'draw2' || card.value === 'wild4'
  );
  
  if (specialCards.length > 0) {
    const card = specialCards[Math.floor(Math.random() * specialCards.length)];
    
    // If it's a wild card, choose a color
    if (card.color === 'wild') {
      // Count colors in hand to choose the most common
      const colorCounts: Record<string, number> = { red: 0, blue: 0, green: 0, yellow: 0 };
      player.cards.forEach(c => {
        if (c.color !== 'wild') {
          colorCounts[c.color]++;
        }
      });
      
      let maxColor: CardColor = 'red';
      let maxCount = 0;
      
      (Object.keys(colorCounts) as CardColor[]).forEach(color => {
        if (colorCounts[color] > maxCount) {
          maxCount = colorCounts[color];
          maxColor = color;
        }
      });
      
      return { card, newColor: maxColor };
    }
    
    return { card, newColor: null };
  }
  
  // Otherwise play a regular card
  const card = playableCards[Math.floor(Math.random() * playableCards.length)];
  return { card, newColor: null };
};

// Check if a player has won
export const checkWinner = (player: Player): boolean => {
  return player.cards.length === 0;
};

// Initialize a new game
export const initializeGame = (playerName: string, numComputers: number = 3): GameState => {
  const deck = createDeck();
  
  // Create players
  const players: Player[] = [
    { id: 'player', name: playerName, isComputer: false, cards: [] }
  ];
  
  // Add computer players
  for (let i = 1; i <= numComputers; i++) {
    players.push({
      id: `computer-${i}`,
      name: `Computer ${i}`,
      isComputer: true,
      cards: []
    });
  }
  
  // Deal cards
  const { updatedDeck, updatedPlayers } = dealCards(deck, players);
  
  // Set up initial discard pile
  const firstCard = updatedDeck.pop()!;
  
  // If the first card is a wild card, assign it a random color
  if (firstCard.color === 'wild') {
    const colors: CardColor[] = ['red', 'blue', 'green', 'yellow'];
    firstCard.color = colors[Math.floor(Math.random() * colors.length)];
  }
  
  return {
    players: updatedPlayers,
    currentPlayerIndex: 0,
    direction: 1,
    deck: updatedDeck,
    discardPile: [firstCard],
    gameStarted: true,
    gameOver: false,
    winner: null,
    message: "Game started! It's your turn.",
    selectedColor: null,
    needToSelectColor: false
  };
};
