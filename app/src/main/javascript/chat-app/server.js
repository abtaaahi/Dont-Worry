const fs = require('fs');
const path = require('path');

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const statusFilePath = path.join('app/src/main/javascript/chat-app/user_status.json');

let usersOnline = {};

if (fs.existsSync(statusFilePath)) {
    usersOnline = JSON.parse(fs.readFileSync(statusFilePath, 'utf8'));
}

app.use(express.static('app/src/main/javascript/chat-app/public'));

io.on('connection', (socket) => {
    const userId = socket.handshake.query.userId;

    if (userId) {
        usersOnline[userId] = 'online';
        io.emit('user-status-change', { userId, status: 'online' });
        fs.writeFileSync(statusFilePath, JSON.stringify(usersOnline), 'utf8');

        console.log(`${userId} connected`);

        socket.on('disconnect', () => {
            delete usersOnline[userId];
            io.emit('user-status-change', { userId, status: 'offline' });
            fs.writeFileSync(statusFilePath, JSON.stringify(usersOnline), 'utf8');
            console.log(`${userId} disconnected`);
        });
    }
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
