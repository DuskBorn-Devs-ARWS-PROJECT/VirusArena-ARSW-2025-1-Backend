document.addEventListener('DOMContentLoaded', () => {
    const gameResultElement = document.getElementById('gameResult');
    const resultDetailsElement = document.getElementById('resultDetails');
    const playersRankingElement = document.getElementById('playersRanking');
    const newGameBtn = document.getElementById('newGameBtn');
    const mainMenuBtn = document.getElementById('mainMenuBtn');

    // Mostrar resultados
    if (gameState.endData) {
        gameResultElement.textContent = gameState.endData.message;

        // Determinar si el jugador actual ganó
        const currentPlayerWon = gameState.endData.winners.some(
            winner => winner.id === gameState.playerId
        );

        if (currentPlayerWon) {
            resultDetailsElement.textContent = '¡Felicidades, has ganado!';
            resultDetailsElement.style.color = 'var(--survivor-color)';
        } else {
            resultDetailsElement.textContent = '¡Mejor suerte la próxima vez!';
            resultDetailsElement.style.color = 'var(--infected-color)';
        }

        // Mostrar clasificación
        gameState.endData.winners.forEach((winner, index) => {
            const li = document.createElement('li');
            li.className = `player ${winner.infected ? 'infected' : 'survivor'}`;
            li.innerHTML = `
                <span class="position">${index + 1}.</span>
                <span class="name">${winner.name}</span>
                <span class="type">${winner.infected ? 'Infectado' : 'Superviviente'}</span>
            `;
            playersRankingElement.appendChild(li);
        });
    }

    // Event listeners
    newGameBtn.addEventListener('click', () => {
        joinGame(null, gameState.playerName);
    });

    mainMenuBtn.addEventListener('click', () => {
        window.location.href = 'index.html';
    });
});