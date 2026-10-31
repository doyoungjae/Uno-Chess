# UNO Card Game

A beautiful, interactive UNO card game built with React, TypeScript, and Tailwind CSS.

## Features

- 🎮 Full UNO game implementation with all standard rules
- 🤖 Play against 1-3 computer opponents
- 🎯 Special cards: Skip, Reverse, Draw 2, Wild, and Wild Draw 4
- 🔄 Dynamic game direction
- 🎨 Beautiful UI with smooth animations
- 📱 Responsive design for desktop and mobile

## Tech Stack

- **React** - UI library
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Vite** - Build tool
- **Lucide React** - Icons

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/uno-card-game.git
   cd uno-card-game
   ```

2. Install dependencies:
   ```bash
   npm install
   # or
   yarn
   ```

3. Start the development server:
   ```bash
   npm run dev
   # or
   yarn dev
   ```

4. Open your browser and navigate to `http://localhost:5173`

## How to Play

1. Enter your name and select the number of computer opponents
2. Match cards by color or number
3. Use special cards to your advantage:
   - **Skip**: Skip the next player's turn
   - **Reverse**: Change the direction of play
   - **Draw 2**: Next player draws 2 cards and loses their turn
   - **Wild**: Change the color of play
   - **Wild Draw 4**: Change the color and next player draws 4 cards

4. First player to get rid of all their cards wins!

## Game Rules

- Players take turns playing a card that matches either the color or number of the top card on the discard pile
- If a player cannot play a card, they must draw a card from the deck
- Special cards have unique effects:
  - **Number cards (0-9)**: Regular play
  - **Skip**: The next player loses their turn
  - **Reverse**: The direction of play changes
  - **Draw 2**: The next player draws 2 cards and loses their turn
  - **Wild**: The player chooses the next color to be played
  - **Wild Draw 4**: The player chooses the next color, and the next player draws 4 cards and loses their turn

## Project Structure

```
uno-card-game/
├── public/
├── src/
│   ├── components/
│   │   ├── Card.tsx
│   │   ├── ComputerHand.tsx
│   │   ├── GameBoard.tsx
│   │   ├── GameControls.tsx
│   │   ├── GameOverModal.tsx
│   │   ├── PlayerHand.tsx
│   │   └── WelcomeScreen.tsx
│   ├── types/
│   │   └── game.ts
│   ├── utils/
│   │   └── gameUtils.ts
│   ├── App.tsx
│   ├── index.css
│   └── main.tsx
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## Development

### Available Scripts

- `npm run dev` - Start the development server
- `npm run build` - Build for production
- `npm run preview` - Preview the production build
- `npm run lint` - Run ESLint

## Future Enhancements

- Online multiplayer support
- Customizable game rules
- Sound effects and music
- Player statistics and leaderboards
- Additional card themes

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by the classic UNO card game by Mattel
- Built with React and Tailwind CSS
- Icons provided by Lucide React
