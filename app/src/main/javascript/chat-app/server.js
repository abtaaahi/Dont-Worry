const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { MongoClient } = require('mongodb');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const mongoURI = 'mongodb+srv://abtaaahi_dontworry:8d3fmnkz3JuZdii9@cluster0.jg6nk.mongodb.net/abtaaahi_dontworry?retryWrites=true&w=majority';

const client = new MongoClient(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true });

let collection;

async function connectToMongoDB() {
    await client.connect();
    console.log("Connected to MongoDB Atlas!");
    const db = client.db('abtaaahi_dontworry');
    collection = db.collection('user_status');
}

app.use(express.static('app/src/main/javascript/chat-app/public'));

io.on('connection', async (socket) => {
    const userId = socket.handshake.query.userId;

    if (userId) {
        try {
            await collection.updateOne({ user_id: userId }, { $set: { status: 'online' } }, { upsert: true });

            io.emit('user-status-change', { userId, status: 'online' });

            const allStatuses = await collection.find({}).toArray();
            socket.emit('all-user-status', allStatuses);

            console.log(`${userId} connected`);

            socket.on('disconnect', async () => {
                await collection.updateOne({ user_id: userId }, { $set: { status: 'offline' } });

                io.emit('user-status-change', { userId, status: 'offline' });
                console.log(`${userId} disconnected`);
            });
        } catch (error) {
            console.error('Error with database operation', error);
        }
    }
});

const PORT = process.env.PORT || 3000;

connectToMongoDB().then(() => {
    server.listen(PORT, () => {
        console.log(`Server running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Error connecting to MongoDB:', err);
});