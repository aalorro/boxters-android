# Changelog

All notable changes to Boxters will be documented in this file.

## [1.0.0] - 2026-08-08

### Added
- Hex-grid word puzzle game with 56 levels across 4 modes (Simple, Clear, Chain, Illuminate)
- Compose Canvas rendering for hex grid, trace lines, particles, HUD, and overlays
- Touch-based word tracing with real-time validation
- 80,363-word dictionary with Trie-based prefix/word lookup
- Seeded board generation (Mulberry32 PRNG) matching web version output
- 4 board generation strategies: word-first, non-overlapping tiling, random+solvability, overlapping coverage
- Scoring engine with star ratings (1-3 stars)
- Player profile with persistent stats (games played, levels completed, best score, total score)
- Lives system with 5-minute cooldown on game over
- Level navigation (back/forward) restricted to highest reached level
- Board sharing via encoded URL (clipboard)
- Info dialog with 4 tabs: About, Gameplay, Privacy, Contact
- Contact form powered by Formspree
- Audio system: 7 MP3 SFX, synthesized letter tones (pentatonic scale), word chords, victory fanfare
- Sound toggle on welcome screen and game HUD
- Welcome screen with player stats, mode selector, and play button
- Mode unlock progression (complete a mode to unlock the next)
- Mode completion celebration with unique titles per mode (first completion only)
- Automatic advancement to next mode on level 14 completion
- Canvas-drawn icons for info, share, and exit buttons
- Solution words display on defeat with highlighted paths
- Auto-clear of isolated cells in Clear mode
- Anchor cell enforcement in Simple mode board generation
- Edge-to-edge display with portrait lock
