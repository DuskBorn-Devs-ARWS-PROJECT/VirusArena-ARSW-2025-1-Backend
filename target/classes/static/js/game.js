document.addEventListener('DOMContentLoaded', () => {
    const playerNameElement = document.getElementById('playerName');
    const playerTypeElement = document.getElementById('playerType');
    const powerUpCountElement = document.getElementById('powerUpCount');
    const gameStateElement = document.getElementById('gameState');
    const playerCountElement = document.getElementById('playerCount');
    const mapElement = document.getElementById('map');
    const quitBtn = document.getElementById('quitBtn');

    // Mostrar información del jugador
    playerNameElement.textContent = gameState.playerName;
    playerTypeElement.textContent = gameState.isInfected ? 'Infectado' : 'Superviviente';
    playerTypeElement.style.color = gameState.isInfected ? 'var(--infected-color)' : 'var(--survivor-color)';

    // Configurar controles de teclado
    document.addEventListener('keydown', (e) => {
        if (!gameState.isInfected) {
            handleSurvivorControls(e.key.toLowerCase());
        } else {
            handleInfectedControls(e.key.toLowerCase());
        }
    });

    // Suscribirse a actualizaciones del juego
    stompClient.subscribe(`/topic/game/${gameState.gameCode}/update`, (message) => {
        const gameUpdate = JSON.parse(message.body);
        renderGameState(gameUpdate);
    });

    // Suscribirse a finalización del juego
    stompClient.subscribe(`/topic/game/${gameState.gameCode}/end`, (message) => {
        const endData = JSON.parse(message.body);
        gameState.endData = endData;
        window.location.href = 'results.html';
    });

    quitBtn.addEventListener('click', () => {
        // TODO: Implementar salida del juego
        window.location.href = 'index.html';
    });

    function handleSurvivorControls(key) {
        let action = null;

        switch(key) {
            case 'w': action = { type: 'MOVE', x: 0, y: -1 }; break;
            case 'a': action = { type: 'MOVE', x: -1, y: 0 }; break;
            case 's': action = { type: 'MOVE', x: 0, y: 1 }; break;
            case 'd': action = { type: 'MOVE', x: 1, y: 0 }; break;
            case 'e': action = { type: 'COLLECT' }; break;
            case 'q': action = { type: 'USE_POWERUP' }; break;
        }

        if (action) {
            sendPlayerAction(action);
        }
    }

    function handleInfectedControls(key) {
        let action = null;

        switch(key) {
            case 'w': action = { type: 'MOVE', x: 0, y: -1 }; break;
            case 'a': action = { type: 'MOVE', x: -1, y: 0 }; break;
            case 's': action = { type: 'MOVE', x: 0, y: 1 }; break;
            case 'd': action = { type: 'MOVE', x: 1, y: 0 }; break;
        }

        if (action) {
            sendPlayerAction(action);
        }
    }

    function renderGameState(gameUpdate) {
        // Actualizar estado del juego
        gameStateElement.textContent = getGameStateText(gameUpdate.state);
        playerCountElement.textContent = gameUpdate.players.length;

        // Actualizar información del jugador actual
        const currentPlayer = gameUpdate.players.find(p => p.id === gameState.playerId);
        if (currentPlayer) {
            powerUpCountElement.textContent = currentPlayer.powerUpCount || 0;
        }

        // Renderizar mapa
        renderMap(gameUpdate);
    }

    function getGameStateText(state) {
        switch(state) {
            case 'WAITING': return 'Esperando jugadores';
            case 'COUNTDOWN': return 'Comenzando pronto';
            case 'IN_PROGRESS': return 'En progreso';
            case 'FINISHED': return 'Terminado';
            default: return state;
        }
    }

    function renderMap(gameUpdate) {
        mapElement.innerHTML = '';

        if (!gameUpdate.map || gameUpdate.map.length === 0) return;

        for (let y = 0; y < gameUpdate.map.length; y++) {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'map-row';

            for (let x = 0; x < gameUpdate.map[y].length; x++) {
                const cell = document.createElement('div');
                cell.className = 'map-cell';
                cell.id = `cell-${x}-${y}`;

                const cellContent = gameUpdate.map[y][x];
                let cellType = 'empty';
                let displayChar = '.';

                if (cellContent === '#') {
                    cellType = 'wall';
                    displayChar = '#';
                } else {
                    const player = gameUpdate.players.find(p => p.x === x && p.y === y);
                    if (player) {
                        cellType = player.infected ? 'infected' : 'survivor';
                        displayChar = player.infected ? 'I' : 'S';

                        // Resaltar al jugador actual
                        if (player.id === gameState.playerId) {
                            cell.classList.add('current-player');
                        }
                    }

                    const powerUp = gameUpdate.powerUps.find(p => p.x === x && p.y === y);
                    if (powerUp) {
                        cellType = 'powerup';
                        displayChar = 'O';
                    }
                }

                cell.textContent = displayChar;
                cell.classList.add(cellType);
                rowDiv.appendChild(cell);
            }

            mapElement.appendChild(rowDiv);
        }
    }
});