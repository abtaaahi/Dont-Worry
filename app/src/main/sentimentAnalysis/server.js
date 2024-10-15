const express = require('express');
const bodyParser = require('body-parser');
const { spawn } = require('child_process');
const path = require('path');
const app = express();
const port = process.env.PORT || 3000;

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

app.post('/analyze', (req, res) => {
    const userInput = req.body.text;

    const pythonProcess = spawn('python3', [path.join(__dirname, 'sentiment.py'), userInput]);

    pythonProcess.stdout.on('data', (data) => {
        const result = JSON.parse(data.toString());
        res.json(result);
    });

    pythonProcess.stderr.on('data', (data) => {
        console.error(`Error: ${data}`);
        if (!res.headersSent) {
            res.status(500).send("Error analyzing sentiment");
        }
    });

    pythonProcess.on('close', (code) => {
        console.log(`Python process exited with code ${code}`);
    });
});

app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});