const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

let usersOnline = {};

app.use(express.static('app/src/main/javascript/chat-app/public'));

io.on('connection', (socket) => {
    const userId = socket.handshake.query.userId;

    if (userId) {
        usersOnline[userId] = true;
        io.emit('user-status-change', { userId, status: 'online' });

        console.log(`${userId} connected`);

        socket.on('disconnect', () => {
            delete usersOnline[userId];
            io.emit('user-status-change', { userId, status: 'offline' });
            console.log(`${userId} disconnected`);
        });
    }
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
