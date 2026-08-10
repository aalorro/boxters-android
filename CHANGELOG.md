# Changelog

All notable changes to Boxters will be documented in this file.

## [1.1.1] - 2026-08-10

### Changed
- Target Android 16 (API level 36)
- Center-justified registration tagline on phones

### Added
- Dictionary: merch, glute, glutes

## [1.1.0] - 2026-08-09

### Added
- Info dialog with 4 tabs: About, Gameplay, Privacy, Contact
- Contact form powered by Formspree
- Sound toggle on welcome screen
- Total score display on welcome screen and in-game HUD
- Solution words display on defeat with highlighted paths
- Canvas-drawn icons for info, share, and exit buttons
- Mode completion celebration with unique titles per mode (first completion only)
- Automatic advancement to next mode on level 14 completion

### Fixed
- Anchor cell enforcement in Simple mode board generation (words now must pass through anchor cells)
- Solution word list now correctly filters by anchor constraint
- Stale board no longer persists after defeat/victory/cooldown transitions
- Clear mode now correctly triggers victory after auto-clearing isolated cells
- Info button hit detection on game canvas now matches rendering position
- Defeat retry no longer shows the same board with previously formed words

### Changed
- Increased tile and text sizes for mobile accessibility
- Increased navigation button size by 30% and repositioned higher to avoid footer overlap
- Aligned share button with back/forward buttons
- Enforced objective word length constraints in board generation
- Restricted level navigation to highest reached level
- Swapped Simple levels 10 and 11 for correct difficulty progression
- Replaced text-based HUD icons with Canvas-drawn icons (info, share, exit)

## [1.0.0] - 2026-08-07

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
- Level navigation (back/forward)
- Board sharing via encoded URL (clipboard)
- Audio system: 7 MP3 SFX, synthesized letter tones (pentatonic scale), word chords, victory fanfare
- Welcome screen with player stats, mode selector, and play button
- Mode unlock progression (complete a mode to unlock the next)
- Auto-clear of isolated cells in Clear mode
- Edge-to-edge display with portrait lock
