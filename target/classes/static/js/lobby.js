document.addEventListener('DOMContentLoaded', () => {
    const readyBtn = document.getElementById('readyBtn');
    const startBtn = document.getElementById('startBtn');
    const leaveBtn = document.getElementById('leaveBtn');
    const playersList = document.getElementById('playersList');
    const codeDisplay = document.getElementById('codeDisplay');

    // Mostrar código de juego
    codeDisplay.textContent = gameState.gameCode;

    // Configurar botones
    if (gameState.isHost) {
        startBtn.classList.remove('hidden');
    }

    // Suscribirse a actualizaciones de jugadores
    stompClient.subscribe(`/topic/game/${gameState.gameCode}/players`, (message) => {
        const players = JSON.parse(message.body);
        updatePlayersList(players);
    });

    // Suscribirse a actualizaciones del juego
    stompClient.subscribe(`/topic/game/${gameState.gameCode}/update`, (message) => {
        const gameUpdate = JSON.parse(message.body);

        // Si el juego ha comenzado, redirigir
        if (gameUpdate.state === 'IN_PROGRESS') {
            window.location.href = 'game.html';
        }
    });

    // Event listeners
    readyBtn.addEventListener('click', () => {
        gameState.isReady = !gameState.isReady;
        sendReadyStatus();
        readyBtn.textContent = gameState.isReady ? 'No listo' : 'Listo';
        readyBtn.style.backgroundColor = gameState.isReady ? 'var(--danger-color)' : 'var(--primary-color)';
    });

    startBtn.addEventListener('click', () => {
        startGame();
    });

    leaveBtn.addEventListener('click', () => {
        // TODO: Implementar salida del juego
        window.location.href = 'index.html';
    });

    function updatePlayersList(players) {
        playersList.innerHTML = '';
        players.forEach(player => {
            const li = document.createElement('li');
            li.className = `player ${player.infected ? 'infected' : 'survivor'} ${player.ready ? 'ready' : ''}`;

            li.innerHTML = `
                <span class="player-name">${player.name}</span>
                <span class="player-status">${player.ready ? 'Listo' : 'Esperando'}</span>
            `;

            playersList.appendChild(li);
        });
    }
});