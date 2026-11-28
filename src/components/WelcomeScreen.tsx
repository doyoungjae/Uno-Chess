import React, { useState } from 'react';
import { PlayCircle } from 'lucide-react';

interface WelcomeScreenProps {
  onStartGame: (playerName: string, numComputers: number) => void;
}

const WelcomeScreen: React.FC<WelcomeScreenProps> = ({ onStartGame }) => {
  const [playerName, setPlayerName] = useState('Player 1');
  const [numComputers, setNumComputers] = useState(3);
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onStartGame(playerName || 'Player 1', numComputers);
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-500 to-blue-600 p-4">
      <div className="bg-white rounded-xl shadow-2xl p-8 max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-5xl font-nunito font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-red-500 via-yellow-500 to-blue-500 mb-2">
            UNO Game
          </h1>
          <p className="text-gray-600 font-nunito">A fun card game for everyone!</p>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="playerName" className="block text-gray-700 font-nunito font-semibold mb-2">
              Your Name
            </label>
            <input
              type="text"
              id="playerName"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-nunito"
              placeholder="Enter your name"
            />
          </div>
          
          <div>
            <label htmlFor="numComputers" className="block text-gray-700 font-nunito font-semibold mb-2">
              Number of Computer Players
            </label>
            <select
              id="numComputers"
              value={numComputers}
              onChange={(e) => setNumComputers(Number(e.target.value))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-nunito"
            >
              <option value={1}>1 Computer</option>
              <option value={2}>2 Computers</option>
              <option value={3}>3 Computers</option>
            </select>
          </div>
          
          <div className="pt-4">
            <button
              type="submit"
              className="w-full bg-gradient-to-r from-green-500 to-blue-600 text-white font-nunito font-bold py-3 px-6 rounded-full flex items-center justify-center gap-2 shadow-lg transform hover:scale-105 transition-all"
            >
              <PlayCircle size={24} />
              Start Playing
            </button>
          </div>
        </form>
        
        <div className="mt-8 bg-yellow-50 p-4 rounded-lg">
          <h3 className="font-nunito font-bold text-yellow-800 mb-2">How to Play:</h3>
          <ul className="list-disc pl-5 space-y-1 text-sm text-gray-700 font-nunito">
            <li>Match cards by color or number</li>
            <li>Use special cards to skip turns, reverse direction, or make others draw cards</li>
            <li>First player to get rid of all cards wins!</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default WelcomeScreen;
