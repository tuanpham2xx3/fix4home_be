const express = require('express');
const cors = require('cors');
const path = require('path');
const http = require('http');
const app = express();
const port = 3001;
const apiDocsUrl = 'http://localhost:8080/api-docs-json';

// Enable CORS for all routes
app.use(cors());

// Serve static files from the current directory
app.use(express.static(path.join(__dirname)));

// Add an endpoint to check if backend API documentation is available
app.get('/api-check', (req, res) => {
  checkApiAvailability((isAvailable, message) => {
    res.json({ available: isAvailable, message: message });
  });
});

// Redirect root to the Swagger UI HTML file
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

// Function to check if the API documentation endpoint is available
function checkApiAvailability(callback) {
  http.get(apiDocsUrl, (res) => {
    if (res.statusCode === 200) {
      callback(true, `API documentation available at ${apiDocsUrl}`);
    } else {
      callback(false, `API returned status code: ${res.statusCode}`);
    }
  }).on('error', (err) => {
    callback(false, `Error connecting to API: ${err.message}`);
  });
}

// Start the server
app.listen(port, () => {
  console.log(`Swagger UI server running at http://localhost:${port}`);
  
  // Check if API docs are available
  checkApiAvailability((isAvailable, message) => {
    if (isAvailable) {
      console.log(`✅ ${message}`);
    } else {
      console.log(`❌ ${message}`);
      console.log('Please ensure the backend server is running and SpringDoc is properly configured.');
      console.log('Check application.properties for correct springdoc.api-docs.path value.');
    }
  });
});
