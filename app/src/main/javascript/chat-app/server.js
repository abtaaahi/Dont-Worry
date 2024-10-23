require('dotenv').config();
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const nodemailer = require('nodemailer');
const { MongoClient } = require('mongodb');
const cors = require('cors');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const mongoURI = process.env.MONGO_URI;
const client = new MongoClient(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true });
let collection;

app.use(cors());
app.use(express.json());

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.GMAIL_USER,
        pass: process.env.GMAIL_APP_PASSWORD
    }
});

async function connectToMongoDB() {
    await client.connect();
    console.log("Connected to MongoDB Atlas!");
    const db = client.db('abtaaahi_dontworry');
    collection = db.collection('user_status');
}

app.use(express.static('app/src/main/javascript/chat-app/public'));

app.post('/send-email', (req, res) => {
    const { senderName, senderEmail, receiverEmail, senderPhotoUrl } = req.body;

    const mailOptions = {
        from: `${senderName} <${senderEmail}>`,
        to: receiverEmail,
        subject: `${senderName} wants to connect with you`,
        text: `Hello! ${senderName} (${senderEmail}) has requested to connect with you.`,
        html: `
            <p>Hello!</p>
            <p>${senderName} (${senderEmail}) has requested to connect with you.</p>
            <img src="${senderPhotoUrl}" alt="${senderName}'s Profile Photo" style="width:100px;height:auto;"/>
        `
    };

    transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            console.error('Error sending email: ', error);
            return res.status(500).send('Error sending email: ' + error.toString());
        }
        console.log('Email sent: ' + info.response);
        res.status(200).send('Email sent: ' + info.response);
    });
});

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