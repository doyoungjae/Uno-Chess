import React from 'react';
import { Player } from '../types/game';
import Card from './Card';

interface ComputerHandProps {
  player: Player;
  isCurrentPlayer: boolean;
}

const ComputerHand: React.FC<ComputerHandProps> = ({ player, isCurrentPlayer }) => {
  return (
    <div className={`
      p-4 rounded-lg 
      ${isCurrentPlayer ? 'bg-yellow-100 border-2 border-yellow-400' : 'bg-gray-100'}
      transition-all duration-300
    `}>
      <div className="flex justify-between items-center mb-2">
        <h3 className="font-nunito font-bold text-lg">
          {player.name} 
          {isCurrentPlayer && <span className="ml-2 text-yellow-600">(Current Turn)</span>}
        </h3>
        <div className="bg-gray-800 text-white px-2 py-1 rounded-full text-sm">
          {player.cards.length} cards
        </div>
      </div>
      
      <div className="flex flex-wrap gap-1 justify-center">
        {Array.from({ length: player.cards.length }).map((_, index) => (
          <Card
            key={index}
            card={{ id: `dummy-${index}`, color: 'wild', value: 'wild' }}
            isPlayable={false}
            faceDown={true}
          />
        ))}
      </div>
    </div>
  );
};

export default ComputerHand;
