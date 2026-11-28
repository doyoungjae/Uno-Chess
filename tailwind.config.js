/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        nunito: ['Nunito', 'sans-serif'],
      },
      colors: {
        'uno-red': '#ff5555',
        'uno-blue': '#5555ff',
        'uno-green': '#55aa55',
        'uno-yellow': '#ffaa00',
        'uno-black': '#333333',
      },
      animation: {
        'bounce-slow': 'bounce 2s infinite',
        'spin-slow': 'spin 3s linear infinite',
      }
    },
  },
  plugins: [],
}
