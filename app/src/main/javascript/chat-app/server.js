require('dotenv').config();

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const admin = require('firebase-admin');

const serviceAccount = {
  type: "service_account",
  project_id: process.env.FIREBASE_PROJECT_ID,
  private_key_id: process.env.FIREBASE_PRIVATE_KEY_ID,
  private_key: process.env.FIREBASE_PRIVATE_KEY,
  client_email: process.env.FIREBASE_CLIENT_EMAIL,
  client_id: process.env.FIREBASE_CLIENT_ID,
  auth_uri: "https://accounts.google.com/o/oauth2/auth",
  token_uri: "https://oauth2.googleapis.com/token",
  auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
  client_x509_cert_url: process.env.FIREBASE_CLIENT_X509_CERT_URL
};

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: process.env.FIREBASE_DATABASE_URL
});

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(express.static('public'));

let usersOnline = {};

io.on('connection', (socket) => {
  const userId = socket.handshake.query.userId;

  if (userId) {
    usersOnline[userId] = true;
    admin.database().ref(`users/${userId}/status`).set('online');
    io.emit('userStatus', { userId, status: 'online' });
  }

  socket.on('disconnect', () => {
    if (userId) {
      usersOnline[userId] = false;
      admin.database().ref(`users/${userId}/status`).set('offline');
      io.emit('userStatus', { userId, status: 'offline' });
    }
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
