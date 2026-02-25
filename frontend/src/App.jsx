import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  // Game state variables
  const canvasRef = useRef(null);
  const [score, setScore] = useState(0)
  const [status, setStatus] = useState("Press Start")
  const gridSize = 20;


  // Handle keyboard input for snake direction
  useEffect(() => {
    const handleKeyDown = (event) => {
      let direction = null;
      if (event.key === 'ArrowUp') direction = 'UP';
      if (event.key === 'ArrowDown') direction = 'DOWN';
      if (event.key === 'ArrowLeft') direction = 'LEFT';
      if (event.key === 'ArrowRight') direction = 'RIGHT';


      if (direction) {
        // Update snake direction
        fetch("http://localhost:8080/api/input", { method: "POST", body: direction })
          .catch(err => console.error("Server offline", err));
      }
  };

  // Add event listener for key presses
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown); // Cleanup
    }, []);


    // fetch game state from server 10x per second (100ms)
    useEffect(() => {
      const interval = setInterval(async () => {
        try {
          const res = await fetch("http://localhost:8080/api/state");
          const state = await res.json();

          // Update game state variables
          setScore(state.score);
          setStatus(state.status ? "GAME OVER!" : "Playing...");
          draw(state); // Function to render game state on canvas
          // Update snake and food positions as needed
        } catch (err) {
          setStatus("Server offline");
        }
      }, 100);
      
      return () => clearInterval(interval); // Cleanup on unmount
    }, []);

    // draw game state on canvas
    const draw = (state) => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      const ctx = canvas.getContext("2d");

      ctx.clearRect(0, 0, canvas.width, canvas.height); // Clear canvas

      if (!state.food || !state.snake) return; // Wait for valid state

      // Draw food
      ctx.fillStyle = "red";
      ctx.fillRect(state.food.x * gridSize, state.food.y * gridSize, gridSize, gridSize);

      // Draw snake
      ctx.fillStyle = "green";
      state.snake.forEach(segment => {
        ctx.fillRect(segment.x * gridSize, segment.y * gridSize, gridSize, gridSize);
      });
    };


    // Start game by sending POST request to server
    const startGame = () => {
      fetch("http://localhost:8080/api/start", { method: "POST" })
        .catch(err => console.error("Server offline", err));
    }

  return (
    <div className="container">
      <h1>React + Java Snake</h1>
      <div className="game-wrapper">
        <canvas ref={canvasRef} width={400} height={400}></canvas>
        <div className="scoreboard">
          <button onClick={startGame}>Start Game</button>
          <h2>Score: {score}</h2>
          <p className="status">{status}</p>
        </div>
      </div>
    </div>
  )
}

export default App
