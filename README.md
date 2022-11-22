# thirty-one
A Clojure library designed process games of 31/Trente et un/Scat

## Usage
Runs a game of 31, also contains an "Old West" mode where players can shoot each other (because I wanted to add a twist).

## Rules
| Card       | Point Value |
|------------|-------------|
| A          | 11          |
| J Q K      | 10          |
| All others | Face Value  |

### Setup
Each player gets 5 lives and a hand of 3 cards. 
The top card of the deck is revealed to all players and placed in the Discard Pile. 
All other cards are hidden and used as the Deck.
    
### Play
All players are trying to get (as close as they can) to 31 points in their hand **of the same suit**.  
If at any point a player has exactly 31 points in their 3 card hand, all in one suit, the round ends, and all other players lose a life.  
Each player takes turns either knocking or drawing..

#### Knocking
If a player believes they have more points in one suit than any other player, they may end the round early by "Knocking".
- When a player "Knocks", all other players have one more turn to raise their points in hand.

#### Drawing
- The player selects either the top card of the Discard Pile (visible), or the top card of the Deck (hidden), and adds it to their hand.
- The player then discards any card in their hand revealing the card as they set it on the Discard Pile.  


### Round End
At the end of the round all players reveal their hands and total up the highest value they can make using cards of a single suit.
- The player (or in the event of a tie, players) with the lowest score loses a life.
- If a player knocked, but the round ended due to another player getting 31, all players (besides the one with 31) lose a life.
- If a player knocked, but does not have the highest score, they lose a life.

## Old West mode
**This mode is only recommended for games with 4 or more players.**  
All prior rules apply, with the following conditions:
- On a players turn, they may reach for their single-shot Derringer and fire at another player, instantly removing them from the game.  
- These tense conditions however, make you sweat, all the way down to your boot-gun.  
- After each players turn, your boots become more soaked with sweat, and the chance of your gun failing to fire goes up 2%.

### Quick Draw Mode
All prior rules apply, except that you may reach for your Derringer at any time, and likewise any player may respond if they're faster than you.
