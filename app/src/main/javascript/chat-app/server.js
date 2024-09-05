const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.cert('DontWorry/app/google-services.json'),
  databaseURL: "https://javascriptquiz01-default-rtdb.asia-southeast1.firebasedatabase.app"
});

const db = admin.database();
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

let usersOnline = {};

io.on('connection', (socket) => {
  const userId = socket.handshake.query.userId;

  if (userId) {
    usersOnline[userId] = true;
    db.ref(`users/${userId}/status`).set('online');
  }

  socket.on('disconnect', () => {
    if (userId) {
      usersOnline[userId] = false;
      db.ref(`users/${userId}/status`).set('offline');
    }
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
