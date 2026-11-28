import React from 'react';
import { Card as CardType } from '../types/game';

interface CardProps {
  card: CardType;
  onClick?: () => void;
  isPlayable?: boolean;
  faceDown?: boolean;
}

const Card: React.FC<CardProps> = ({ card, onClick, isPlayable = true, faceDown = false }) => {
  const getCardColorClass = () => {
    if (faceDown) return 'bg-gradient-to-br from-gray-700 to-gray-900 border-gray-500';
    
    switch (card.color) {
      case 'red':
        return 'bg-gradient-to-br from-red-500 to-red-700 border-red-300';
      case 'blue':
        return 'bg-gradient-to-br from-blue-500 to-blue-700 border-blue-300';
      case 'green':
        return 'bg-gradient-to-br from-green-500 to-green-700 border-green-300';
      case 'yellow':
        return 'bg-gradient-to-br from-yellow-400 to-yellow-600 border-yellow-200';
      case 'wild':
        return 'bg-gradient-to-br from-purple-500 to-purple-800 border-purple-300';
      default:
        return 'bg-gradient-to-br from-gray-500 to-gray-700 border-gray-300';
    }
  };

  const getCardSymbol = () => {
    if (faceDown) return '🎴';
    
    switch (card.value) {
      case 'skip': return '⊘';
      case 'reverse': return '↺';
      case 'draw2': return '+2';
      case 'wild': return '🌈';
      case 'wild4': return '+4';
      default: return card.value;
    }
  };

  return (
    <div 
      onClick={isPlayable ? onClick : undefined}
      className={`
        ${getCardColorClass()}
        ${isPlayable && !faceDown ? 'cursor-pointer transform hover:-translate-y-2 hover:shadow-lg transition-all' : ''}
        ${!isPlayable && !faceDown ? 'opacity-70' : ''}
        w-16 h-24 md:w-20 md:h-28 rounded-lg border-2 flex items-center justify-center
        font-nunito font-bold text-white shadow-md relative overflow-hidden
      `}
    >
      {!faceDown && (
        <>
          <div className="absolute top-1 left-1 text-xs">{getCardSymbol()}</div>
          <div className="text-2xl md:text-3xl">{getCardSymbol()}</div>
          <div className="absolute bottom-1 right-1 text-xs transform rotate-180">{getCardSymbol()}</div>
        </>
      )}
      {faceDown && (
        <div className="flex items-center justify-center">
          <div className="text-2xl">UNO</div>
        </div>
      )}
    </div>
  );
};

export default Card;
