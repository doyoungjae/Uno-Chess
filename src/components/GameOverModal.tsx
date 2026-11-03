import React from 'react';
import { Player } from '../types/game';
import { Trophy, RotateCcw } from 'lucide-react';

interface GameOverModalProps {
  winner: Player | null;
  onRestartGame: () => void;
}

const GameOverModal: React.FC<GameOverModalProps> = ({ winner, onRestartGame }) => {
  if (!winner) return null;
  
  const isPlayerWinner = !winner.isComputer;
  
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl p-8 max-w-md w-full shadow-2xl transform animate-bounce-slow">
        <div className="text-center">
          <Trophy className={`mx-auto h-16 w-16 ${isPlayerWinner ? 'text-yellow-500' : 'text-gray-500'}`} />
          
          <h2 className="text-3xl font-nunito font-bold mt-4 mb-2">
            {isPlayerWinner ? 'Congratulations!' : 'Game Over!'}
          </h2>
          
          <p className="text-xl mb-6">
            {winner.name} {isPlayerWinner ? 'won the game!' : 'has won the game.'}
          </p>
          
          {isPlayerWinner ? (
            <div className="bg-yellow-100 p-4 rounded-lg mb-6">
              <p className="font-nunito font-semibold text-yellow-800">
                You've successfully played all your cards! Great job!
              </p>
            </div>
          ) : (
            <div className="bg-blue-100 p-4 rounded-lg mb-6">
              <p className="font-nunito font-semibold text-blue-800">
                Better luck next time! Keep practicing your UNO skills.
              </p>
            </div>
          )}
          
          <button
            onClick={onRestartGame}
            className="bg-green-600 hover:bg-green-700 text-white font-nunito font-bold py-3 px-8 rounded-full flex items-center gap-2 mx-auto shadow-lg transform hover:scale-105 transition-all"
          >
            <RotateCcw size={20} />
            Play Again
          </button>
        </div>
      </div>
    </div>
  );
};

export default GameOverModal;
