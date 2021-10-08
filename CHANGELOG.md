# Java-Chess Changelog

Project created 3/25/21 -- Changelog begin:

PRE-RELEASES--

	version   date          changes
	------- --------    ----------------------------------------------------------------------------------------------------
	1.0.0	3/25/21		Changes in this version
						    -Board will now display
									
	1.1.0	3/28/21		Changes in this version:
						    -Pieces display on the board from any FEN string
						    -Method of displaying the board completely reworked
						    -Board theme can be changed (although this setting cannot yet be saved)
									
	1.2.0	4/14/21		Changes in this version:
					    	-Pieces can be dragged and moved
				    		-Pieces can capture other pieces
					    	-Piece data structure refined
									
	1.2.1	4/14/21		Changes in this version:
					    	-Fixed the bug outlined in issue #2
						    -Partially fixed the bug outlined in issue #1
									
	1.2.2	4/14/21		Changes in this version:
	    					-Prevent friendly pieces from being captured
									
	1.3.0	4/19/21		Changes in this version:
		    				-Prevent pieces from making illegal movements
			    			-Added pseudo-legal move generation
				    		-Added move highlighting with a enable/disable button
					    	-Added new game button
						    -Fixed issues with the FEN string displayed in the JTextField
									
	1.3.1	4/21/21		Changes in this version:
	    					-Fixed the bug outlined in issue #3
									
	1.4.0	4/24/21		Changes in this version:
		    				-Fixed issue with halfmove and fullmove counters
			    			-Added pawn promotion
									
	1.4.1	4/25/21		Changes in this version:
				    		-Code cleanup
					    	-Added Graphics class, removed graphics methods from Board class
									
	1.5.0	4/27/21		Changes in this version:
						    -Added en passant
	    					-Fixed a minor issue with normal pawn captures
									
	1.6.0	4/29/21		Changes in this version:
		    				-Added algorithm to generate fully legal moves and discard moves that wander in to check or ignore an active check on the king
									
	1.6.1	4/30/21		Changes in this version:
			    			-Fixed the bug outlined in issue #7
									
	1.6.2	4/30/21		Changes in this version:
				    		-Fixed the bug outlined in issue #6
									
	1.6.3	5/1/21		Changes in this version:
					    	-Discovered and fixed various pawn movement issues
						    -Began basic implementation of draw/win states
									
	1.6.4	5/2/21		Changes in this version:
	    					-Discovered and fixed an issue with pawns and check
							
	1.6.5	5/9/21		Changes in this version:
		    				-Fixed a minor issue with component z order when dragging pieces

	1.7.0	5/11/21		Changes in this version:
			    			-Added draw and win/loss game states
				    		-Moves legal move generation to LegalMoveUtility.java

	1.8.0	5/12/21		Changes in this version:
					    	-Fixed a minor issue with stalemate
						    -Added castling


FULL-RELEASES--

	version   date          changes
	------- --------    ----------------------------------------------------------------------------------------------------
	1.0.0	5/13/21		Changes in this version:
    						-Fixed castling through check
	    					-First full version of Java-Chess

	1.0.1	5/14/21		Changes in this version:
		    				-Game state and pawn promotion dialog boxes now display relative to the board

	1.0.2	5/19/21		Changes in this version:
			    			-Material advantage/score is now displayed along with the other settings and config

	1.0.3	5/19/21		Changes in this version:
				    		-Fixed a minor issue with FEN position being displayed

	1.0.4	5/19/21		Changes in this version:
					    	-Documentation updates
    
	1.0.5	5/21/21		Changes in this version:
						    -Move generation optimizations

	1.0.6	5/22/21		Changes in this version:
	    					-Fixed a minor issue with pawn promotion

	2.0.0	5/30/21		Changes in this version:
		    				-Minor all-around improvements
			    			-Added AI class and the ability to enable/disable the computer player

	2.0.1	5/31/21		Changes in this version:
				    		-AI will now prioritize checkmate

    2.1.0   10/7/21     Changes in this version:
                            -Removed json configuration file
                            -Refactored codebase to be more object-oriented and fixed possible code access issues
                            -Refactored package structure
                            -Created compile.sh, jar.sh and build-mac.sh to atomate the build process
                            -Created native dmg for mac app