export type CardColor = 'red' | 'blue' | 'green' | 'yellow' | 'wild';
export type CardValue = 
  | '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' 
  | 'skip' | 'reverse' | 'draw2' | 'wild' | 'wild4';

export interface Card {
  id: string;
  color: CardColor;
  value: CardValue;
}

export interface Player {
  id: string;
  name: string;
  isComputer: boolean;
  cards: Card[];
}

export interface GameState {
  players: Player[];
  currentPlayerIndex: number;
  direction: 1 | -1;
  deck: Card[];
  discardPile: Card[];
  gameStarted: boolean;
  gameOver: boolean;
  winner: Player | null;
  message: string;
  selectedColor: CardColor | null;
  needToSelectColor: boolean;
}
