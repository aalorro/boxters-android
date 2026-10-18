# Changelog

All notable changes to Boxters will be documented in this file.

## [2.1.0] - 2026-10-18

### Added
- 3 new dictionary words: yada, rethaw, vax

### Changed
- Back, forward, and load-board buttons are now hidden during active gameplay; only the share button remains visible, freeing up screen space on smaller phones

## [2.0.5] - 2026-10-12

### Added
- Minimum 4-character requirement for player names (registration and settings)
- Simple level 4: added "Form 2 words" objective alongside anchor requirement
- Simple level 7: added "Form 2 words of 4+ letters" objective
- Simple levels 11 & 13: increased anchors from 2 to 3

### Changed
- Objectives panel uses compact fonts and tighter spacing on phones when 3+ objectives are displayed

## [2.0.4] - 2026-09-01

### Added
- "Lives restored!" notification on victory overlay when lives are regained
- Drag grip indicator on tutorial popup for discoverability

### Fixed
- In-app update button doing nothing (migrated to ActivityResultLauncher API, added install listener)
- Tutorial popup initial position no longer obscured by buttons on phones

### Changed
- Tutorial popup now appears centered below button row instead of overlapping it
- Lives explanation in info dialog updated to describe the restoration mechanic

## [2.0.3] - 2026-08-28

### Added
- Random mode: endless random games (level 6+ from any mode) unlocked after reaching the final Apex level
- Editable player name in Settings with Save button and Firebase sync
- Leaderboard now shows player's actual rank as a full row at the bottom when outside top 30

### Changed
- Leaderboard fetches all entries to compute real player rank (previously limited to top 30)
- Welcome text ("Welcome back, ...") is 50% larger on tablets
- Apex level 9 (Quantum Flux): added illuminate 60% objective and apex weights (15/25/60)

## [2.0.2] - 2026-08-26

### Added
- Dictionary: che, etc, milf, omni, perp
- Apex level 5 (Entropy): illuminate-weighted apex weights (20/30/50 clear/chain/illuminate)
- Apex level 7 (Fusion Core): illuminate-weighted apex weights (15/25/60 clear/chain/illuminate)

### Fixed
- Total score showing zero on welcome screen until player starts playing

### Changed
- Feedback message display duration increased from 2s to 5s
- Chain mode highlighted tiles now use a stronger, more saturated blue with higher opacity

## [2.0.1] - 2026-08-24

### Added
- In-app update detection using Play In-App Updates API (flexible flow)
- Update prompt dialog shown once per session on the menu screen

### Fixed
- Leaderboard scores of 20,000+ no longer wrap to a second line on mobile

### Changed
- Upgraded Android Gradle Plugin from 8.7.3 to 8.10.1
- Upgraded Gradle wrapper from 8.9 to 8.11.1

## [2.0.0] - 2026-08-17

### Added
- Apex mode: unlocked by completing all 4 modes, gauntlet run through all 56 levels
- Firebase-powered global leaderboard with player rankings
- Per-level scoring with best score tracking
- Settings dialog with dark mode toggle, gender/age group, profile export/import
- Leaderboard dialog with medal rankings and current player highlight

### Changed
- Updated game instructions and privacy policy for v2.0.0

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
