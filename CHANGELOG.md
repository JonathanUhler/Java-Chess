# Java-Chess Changelog

Improved project created 4/30/23 -- Changelog begin:

## [M.m.p] - M/D/YY
### Added
### Fixed
### Changed

## [1.0.0] - 4/30/23
* First working version of Improved Java-Chess

# Original project created 3/25/21 -- Old changelog begin:

## [2.1.0] - 10/7/21
### Added
* Created compile.sh, jar.sh and build-mac.sh to automate the build process
* Created native dmg for mac app
### Fixed
* Refactored codebase to be more object-oriented and fixed possible code access issues
### Changed
* Removed json configuration file
* Refactored package structure

## [2.0.1] - 5/31/21
### Fixed
* AI will now prioritize checkmate

## [2.0.0] - 5/30/21
### Added
* Added AI class and the ability to enable/disable the computer player

## [1.0.6] - 5/22/21
### Fixed
* Fixed a minor issue with pawn promotion

## [1.0.5] - 5/21/21
### Changed
* Move generation optimizations

## [1.0.4] - 5/19/21
### Changed
* Documentation updates

## [1.0.3] - 5/19/21
### Fixed
* Fixed a minor issue with FEN position being displayed

## [1.0.2] - 5/19/21
### Changed
* Material advantage/score is now displayed along with the other settings and config

## [1.0.1] - 5/14/21
### Changed
* Game state and pawn promotion dialog boxes now display relative to the board

## [1.0.0] - 5/13/21
### Fixed
* Fixed castling through check
### Changed
* First full version of Java-Chess

## [pre-1.8.0] - 5/12/21
### Added
* Added castling
### Fixed
* Fixed a minor issue with stalemate

## [pre-1.7.0] - 5/11/21
### Added
* Added draw and win/loss game states
### Changed
* Moves legal move generation to LegalMoveUtility.java

## [pre-1.6.5] - 5/9/21
### Fixed
* Fixed a minor issue with component z order when dragging pieces

## [pre-1.6.4] - 5/2/21
### Fixed
* Discovered and fixed an issue with pawns and check

## [pre-1.6.3] - 5/1/21
### Added
* Began basic implementation of draw/win states
### Fixed
* Discovered and fixed various pawn movement issues

## [pre-1.6.2] - 4/30/21
### Fixed
* Fixed the bug outlined in issue #6

## [pre-1.6.1] - 4/30/21
### Fixed
* Fixed the bug outlined in issue #7

## [pre-1.6.0] - 4/29/21
### Added
* Added algorithm to generate fully legal moves and discard moves that wander in to check or ignore an active check on the king

## [pre-1.5.0] - 4/27/21
### Added
* Added en passant
### Fixed
* Fixed a minor issue with normal pawn captures

## [pre-1.4.1] - 4/25/21
### Added
* Added Graphics class, removed graphics methods from Board class
### Fixed
* Code cleanup

## [pre-1.4.0] - 4/24/21
### Added
* Added pawn promotion
### Fixed
* Fixed issue with halfmove and fullmove counters

## [pre-1.3.1] - 4/21/21
### Fixed
* Fixed the bug outlined in issue #3

## [pre-1.3.0] - 4/19/21
### Added
* Added pseudo-legal move generation
* Added move highlighting with a enable/disable button
* Added new game button
### Fixed
* Prevent pieces from making illegal movements
* Fixed issues with the FEN string displayed in the JTextField

## [pre-1.2.2] - 4/14/21
### Fixed
* Prevent friendly pieces from being captured

## [pre-1.2.1] - 4/14/21
### Fixed
* Fixed the bug outlined in issue #2
* Partially fixed the bug outlined in issue #1

## [pre-1.2.0] - 4/14/21
### Added
* Pieces can be dragged and moved
* Pieces can capture other pieces
### Changed
* Piece data structure refined

## [pre-1.1.0] - 3/28/21
### Added
* Pieces display on the board from any FEN string
* Board theme can be changed (although this setting cannot yet be saved)
### Changed
* Method of displaying the board completely reworked

## [pre-1.0.0] - 3/25/21
### Changed
* Board will now display
