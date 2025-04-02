document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const createGameBtn = document.getElementById('createGameBtn');
    const joinGameBtn = document.getElementById('joinGameBtn');

    // Obtener gameState de WebSocketService
    const gameState = window.WebSocketService.getGameState();

    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value.trim();

        if (username) {
            gameState.playerName = username;
            document.querySelector('.login-form').classList.add('hidden');
            document.querySelector('.game-options').classList.remove('hidden');
        }
    });

    createGameBtn.addEventListener('click', () => {
        window.WebSocketService.joinGame("NEW_GAME", gameState.playerName);
    });

    joinGameBtn.addEventListener('click', () => {
        const gameCode = prompt('Ingresa el código de la partida:');
        if (gameCode) {
            window.WebSocketService.joinGame(gameCode, gameState.playerName);
        }
    });

    // Manejar confirmación de unión
    if (window.WebSocketService && window.WebSocketService.connect) {
        window.WebSocketService.connect();
    }

    // Suscribirse a mensajes de unión solo si stompClient está conectado
    setTimeout(() => {
        if (window.WebSocketService.getGameState().subscriptions && window.WebSocketService.getGameState().subscriptions.gameUpdate) {
            window.WebSocketService.getGameState().subscriptions.gameUpdate.unsubscribe();
        }
        if (stompClient && stompClient.connected) {
            stompClient.subscribe('/user/queue/join', function(message) {
                const data = JSON.parse(message.body);
                gameState.gameCode = data.gameCode;
                gameState.playerId = data.playerId;
                gameState.isHost = data.isHost;
                gameState.isInfected = data.isInfected;

                // Redirigir al lobby
                window.location.href = 'lobby.html';
            });
        }
    }, 2000); // Espera para asegurarse de que stompClient esté inicializado
});