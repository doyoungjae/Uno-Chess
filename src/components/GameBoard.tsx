import React from 'react';
import { Card as CardType, CardColor } from '../types/game';
import Card from './Card';
import { ArrowDownCircle, RotateCcw } from 'lucide-react';

interface GameBoardProps {
  discardPile: CardType[];
  deckCount: number;
  onDrawCard: () => void;
  direction: 1 | -1;
  message: string;
  needToSelectColor: boolean;
  onSelectColor: (color: CardColor) => void;
}

const GameBoard: React.FC<GameBoardProps> = ({
  discardPile,
  deckCount,
  onDrawCard,
  direction,
  message,
  needToSelectColor,
  onSelectColor
}) => {
  const topCard = discardPile[discardPile.length - 1];
  
  return (
    <div className="bg-green-800 p-6 rounded-xl shadow-lg">
      <div className="text-center mb-4">
        <div className="bg-yellow-100 p-2 rounded-lg shadow-inner font-nunito font-semibold">
          {message}
        </div>
      </div>
      
      <div className="flex justify-center items-center gap-8 mb-6">
        {/* Direction indicator */}
        <div className="text-white">
          {direction === 1 ? (
            <RotateCcw className="h-8 w-8 animate-spin-slow" />
          ) : (
            <RotateCcw className="h-8 w-8 animate-spin-slow transform scale-x-[-1]" />
          )}
        </div>
        
        {/* Discard pile */}
        <div className="relative">
          {discardPile.length > 1 && (
            <div className="absolute -top-1 -left-1 rotate-[-5deg]">
              <Card card={discardPile[discardPile.length - 2]} isPlayable={false} />
            </div>
          )}
          <div className="relative z-10">
            <Card card={topCard} isPlayable={false} />
          </div>
        </div>
        
        {/* Draw pile */}
        <div className="relative">
          <div 
            className="flex flex-col items-center cursor-pointer transform hover:scale-105 transition-transform"
            onClick={onDrawCard}
          >
            <Card 
              card={{ id: 'deck', color: 'wild', value: 'wild' }} 
              faceDown={true} 
              isPlayable={true}
            />
            <div className="mt-2 bg-white px-3 py-1 rounded-full text-sm font-nunito font-bold">
              {deckCount} cards
            </div>
            <ArrowDownCircle className="mt-1 text-yellow-300 animate-bounce-slow" size={24} />
          </div>
        </div>
      </div>
      
      {/* Color selector */}
      {needToSelectColor && (
        <div className="bg-white p-4 rounded-lg shadow-lg">
          <h3 className="text-center font-nunito font-bold mb-3">Select a color:</h3>
          <div className="flex justify-center gap-4">
            {(['red', 'blue', 'green', 'yellow'] as CardColor[]).map(color => (
              <button
                key={color}
                onClick={() => onSelectColor(color)}
                className={`w-16 h-16 rounded-full border-4 border-white shadow-md transform hover:scale-110 transition-transform`}
                style={{ backgroundColor: color === 'red' ? '#ff5555' : 
                                        color === 'blue' ? '#5555ff' : 
                                        color === 'green' ? '#55aa55' : 
                                        '#ffaa00' }}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default GameBoard;
