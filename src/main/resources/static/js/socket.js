// Configuración global de WebSocket
let stompClient = null;
const serverUrl = '/virus-arena-websocket';
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

// Estado del juego global
const gameState = {
    playerId: '',
    playerName: '',
    gameCode: '',
    isHost: false,
    isReady: false,
    isInfected: false,
    subscriptions: {}
};

// Función para cargar dinámicamente un script
function loadScript(url, callback) {
    const script = document.createElement('script');
    script.src = url;
    script.onload = callback;
    script.onerror = function () {
        console.error(`No se pudo cargar el script: ${url}`);
        showError(`Error crítico: No se pudo cargar ${url}`);
    };
    document.head.appendChild(script);
}

// Función para asegurarse de que STOMP y SockJS están disponibles
function ensureStompLoaded() {
    if (typeof SockJS === 'undefined') {
        console.warn("SockJS no está definido. Cargando...");
        loadScript("https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js", ensureStompLoaded);
        return;
    }

    if (typeof Stomp === 'undefined') {
        console.warn("STOMP no está definido. Cargando...");
        loadScript("https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundled/stomp.umd.min.js", connectWebSocket);
        return;
    }

    console.log("STOMP y SockJS están listos. Conectando WebSocket...");
    connectWebSocket();
}

// Función para conectar al WebSocket
function connectWebSocket() {
    console.log(`Intentando conexión WebSocket (intento ${reconnectAttempts + 1}/${maxReconnectAttempts})`);

    const socket = new SockJS(serverUrl);
    stompClient = Stomp.over(socket);

    stompClient.reconnect_delay = 5000;
    stompClient.heartbeatIncoming = 4000;
    stompClient.heartbeatOutgoing = 4000;

    stompClient.connect({},
        function(frame) {
            console.log('Conexión establecida:', frame);
            reconnectAttempts = 0;

            gameState.subscriptions.error = stompClient.subscribe('/user/queue/errors', function(message) {
                const error = JSON.parse(message.body);
                console.error('Error del servidor:', error);
                showError(error.message || 'Error en el servidor');
            });

            if (typeof onWebSocketConnected === 'function') {
                onWebSocketConnected();
            }
        },
        function(error) {
            console.error('Error de conexión:', error);
            if (reconnectAttempts < maxReconnectAttempts) {
                reconnectAttempts++;
                setTimeout(connectWebSocket, 5000);
            } else {
                showError('No se pudo conectar al servidor. Por favor recarga la página.');
            }
        }
    );
}

// Función para mostrar errores en la UI
function showError(message) {
    console.error('Error:', message);
    alert(message);
}

// Iniciar conexión cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    ensureStompLoaded();
});

// Exportar funciones para su uso en otros archivos
window.WebSocketService = {
    connect: connectWebSocket,
    joinGame: function (gameCode, playerName) {
        if (!stompClient || !stompClient.connected) {
            showError('No hay conexión con el servidor');
            return false;
        }
        stompClient.send("/app/game/join", {}, JSON.stringify({ gameCode, playerName }));
        return true;
    },
    sendAction: function (action) {
        if (!stompClient || !stompClient.connected) return false;
        stompClient.send(`/app/game/${gameState.gameCode}/action`, {}, JSON.stringify({ playerId: gameState.playerId, action }));
        return true;
    },
    subscribeToGame: function (gameCode, callback) {
        if (!stompClient || !stompClient.connected) return null;
        return gameState.subscriptions.gameUpdate = stompClient.subscribe(`/topic/game/${gameCode}/update`, function(message) {
            callback(JSON.parse(message.body));
        });
    },
    getGameState: () => gameState
};
