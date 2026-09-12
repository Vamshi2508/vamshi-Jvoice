import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, HashRouter } from 'react-router-dom'
import App from './App.jsx'
import { AppProvider } from './store/store.jsx'
import './styles.css'

/**
 * The standalone single-file build is served from a nested path with no server
 * rewrites, so it routes on the hash. The dev server keeps clean URLs.
 */
const Router = import.meta.env.VITE_STANDALONE ? HashRouter : BrowserRouter

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Router>
      <AppProvider>
        <App />
      </AppProvider>
    </Router>
  </React.StrictMode>
)
