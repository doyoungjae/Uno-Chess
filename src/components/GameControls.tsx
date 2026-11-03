import React from 'react';
import { PlayCircle, RotateCcw } from 'lucide-react';

interface GameControlsProps {
  onStartGame: () => void;
  onRestartGame: () => void;
  gameStarted: boolean;
  gameOver: boolean;
}

const GameControls: React.FC<GameControlsProps> = ({
  onStartGame,
  onRestartGame,
  gameStarted,
  gameOver
}) => {
  return (
    <div className="flex justify-center gap-4 my-4">
      {!gameStarted && (
        <button
          onClick={onStartGame}
          className="bg-green-600 hover:bg-green-700 text-white font-nunito font-bold py-2 px-6 rounded-full flex items-center gap-2 shadow-lg transform hover:scale-105 transition-all"
        >
          <PlayCircle size={24} />
          Start Game
        </button>
      )}
      
      {(gameStarted || gameOver) && (
        <button
          onClick={onRestartGame}
          className="bg-blue-600 hover:bg-blue-700 text-white font-nunito font-bold py-2 px-6 rounded-full flex items-center gap-2 shadow-lg transform hover:scale-105 transition-all"
        >
          <RotateCcw size={24} />
          Restart Game
        </button>
      )}
    </div>
  );
};

export default GameControls;
