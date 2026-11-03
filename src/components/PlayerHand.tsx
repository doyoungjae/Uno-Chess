import React from 'react';
import { Player, Card as CardType } from '../types/game';
import Card from './Card';

interface PlayerHandProps {
  player: Player;
  isCurrentPlayer: boolean;
  onPlayCard: (card: CardType) => void;
  canPlayCard: (card: CardType) => boolean;
}

const PlayerHand: React.FC<PlayerHandProps> = ({ 
  player, 
  isCurrentPlayer, 
  onPlayCard, 
  canPlayCard 
}) => {
  return (
    <div className={`
      p-4 rounded-lg 
      ${isCurrentPlayer ? 'bg-yellow-100 border-2 border-yellow-400' : 'bg-gray-100'}
      transition-all duration-300
    `}>
      <div className="flex justify-between items-center mb-2">
        <h3 className="font-nunito font-bold text-lg">
          {player.name} 
          {isCurrentPlayer && <span className="ml-2 text-yellow-600">(Your Turn)</span>}
        </h3>
        <div className="bg-gray-800 text-white px-2 py-1 rounded-full text-sm">
          {player.cards.length} cards
        </div>
      </div>
      
      <div className="flex flex-wrap gap-2 justify-center">
        {player.cards.map((card) => (
          <Card
            key={card.id}
            card={card}
            isPlayable={isCurrentPlayer && canPlayCard(card)}
            onClick={() => isCurrentPlayer && canPlayCard(card) && onPlayCard(card)}
          />
        ))}
      </div>
    </div>
  );
};

export default PlayerHand;
