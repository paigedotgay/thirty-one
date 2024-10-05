# thirty-one
A Clojure library designed process games of 31/Trente et un/Scat  
[![Clojars Project](https://img.shields.io/clojars/v/com.qanazoga/thirty-one.svg)](https://clojars.org/com.qanazoga/thirty-one)
[![cljdoc badge](https://cljdoc.org/badge/com.qanazoga/thirty-one)](https://cljdoc.org/d/com.qanazoga/thirty-one/)

## Usage
### Getting the library
Replace X.X.X with the version you want to use.  
Leiningen/Boot
```clj
[com.qanazoga/thirty-one "X.X.X"]
````
Clojure CLI/deps.edn
```clj
com.qanazoga/thirty-one {:mvn/version "X.X.X"}
```
Gradle
```kt
implementation("com.qanazoga:thirty-one:X.X.X")
```
Maven
```xml
<dependency>
  <groupId>com.qanazoga</groupId>
  <artifactId>thirty-one</artifactId>
  <version>X.X.X</version>
</dependency>
```
### Using the library
`thirty-one.core/start-game` wants you to provide a list of players, and an **io-handler** function.  
This function should take the `gamestate` as an argument, and return a modified `gamestate` when it's done.  
`thirty-one.io` has two functions which should make writing your io-handler easier.
- `(get-actions your-gamestate)` returns a vector of things that can be done in the current gamestate.
- `(perfrom-action your-gamestate action-index)` returns the gamestate after an action has been done to it, where `action-index` corresponds to the index of the action in the vector returned by `get-actions`. For instance, if `(get-actions your-gamestate)` returned `["Draw from deck" "Draw from discard" "Knock"]` and the player wants to draw from the deck, you would use `(perform-action your-gamestate 0)`.

In short:
1. Show the player their cards. (`thirty-one.evaluator/hand->str` can help)
2. Show the player their options. (`thirty-one.io/get-actions`)
3. Allow the player to input what they want to do. (`thirty-one.io/perform-action`)

If `(your-gamestate :awaiting)` returns `:next-turn` or `:end-round` then you should give some extra info to the players (whose turn it is & scores, respectively). See [the wiki page on gamestate](https://github.com/qanazoga/thirty-one/wiki/gamestate) for more info.  

See `thirty-one.io/default-io-handler` for an example of an io-handler made for playing in the terminal.

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
- If the round ended due to a player getting 31, all players (besides the one with 31) lose a life.
- If a player knocked, but does not have the highest score, they lose a life.
