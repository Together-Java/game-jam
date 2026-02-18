# SolutionWCMD-DukeRecovery
This repository contains a puzzle game for the TogetherJava Game Jam. 
This puzzle will also be included as an actual part of the Solution game, and is part of the universe!

## Original Design Idea
We have implemented the original base design idea successfully as imagined, 
though we could not implement the Duke's personality as described below in the original game design idea:

The game has an ASCII / terminal aesthetic with Duke the Java mascot as a smiling systems engineer inspired by the corporate from Knights of Guinevere. 
The puzzle plays inside the head of the protagonist Vey from SolutionWCMD,
fatally damaged as she tries to regenerate herself with the guidance of Duke who plays the figure of the protective operating system inside Vey.
All interactions with the game are purely possible with the use of the keyboard; no mouse will be used!

## How to run
Make sure you have `java-21-openjdk` installed.

Download the newest jar file from the releases, 
or compile one yourself by cloning this repository and running the gradle build task. Jar will appear in `build/libs/...`

Double-click the jar file to run the application.

## How to play
Once the game is launched you start with the loading and introduction phase, and get later transitioned into the first
phase where you have to stop all columns at the right symbol to match the expected value (they don't need to be perfectly in-line).
In the second phase that follows, you have to solve the maze by rotating the arrows to create a path from the green node to the red node,
while a corrupted snake-like feature corrupts the visuals of the grid and randomly rotates one node, making it harder to create the right path.
The path has to be found a total of three times in order to complete the game and enter the ending phase.
But beware, every 30s-60s the player might transition into a special phase where he has to push the corrupted memory fragments out of the column before Duke gets corrupted and the player fails.
When failed the special phase simply resets, not a big L fail :p...

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/13ca088e-bf75-4d83-be98-27d085c580da" />

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/2d600b27-f715-4807-b0eb-5b00d536c957" />

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/ef833135-1c46-4ba5-94ef-e58ea8c66283" />

## Known issues

### Terminal gets flooded with corrupted symbols
If you're running the game from terminal and the entire terminal gets flooded with corrupted symbols,
it means that you have installed `java-21-openjdk-headless` without the required dependencies.

On Debian based Linux run `sudo dnf install java-21-openjdk`. 
You Linux nerds already know how you would install it without Debian.

### Other issues
If your issue isn't listed above, please create an issue and address it in detail.
Low quality issues will get deleted/ignored.

## Contribution
While the TogetherJava game jam ended we'll continue the work on this minigame to meet our standards to be embedded into the actual game.
We highly welcome contributions that help us develop this minigame further by optimizing its performance and improving the visuals including the animations.

To contribute simply create an issue describing the feature you want to work on and then fork this repository to implement the feature.
Once implemented create a pull-request into the development branch and wait for a review.

## Technical
**Tech stack:** Java 21, Gradle

The game should be capable to work with only stdin/stout, without a GUI.
This is relevant to in cooperate this puzzle in SolutionWCMD as a feature.
Standalone the game will render the visuals on Swing components and the entire processing will be CPU based.

While the game has an ASCII / terminal aesthetic, the visual rendering is not within a real terminal.
Characters and images will are pre-converted into ASCII data to keep the game light.

### Legal
For legal reasons the embedded version of this minigame may include a different similar character to Duke, 
but this open source version will remain with Duke the Java Mascot as the main character. 

## Concept ideas before development started
Below are the original concept ideas for this minigame. Not all of those concepts are included as described below!
```
### Phase 1 - Checksum Stabilization

Duke introduces himself and requests to verify with the correct checksum.
The checksum is displayed centered in the terminal in a corrupted state.
Every symbol position has a preset direction up or down with a different speed, 
the symbol moves into the preset direction, and gets replaced by a random symbol.
By pressing arrow keys into the opposite direction, the player can slow down/accelerate or even stop the symbol rotations.
The checksum to be found is displayed in the bottom left corner.
The goal is to stop all correct symbols to show the right checksum.

---

**vvv Everything within this block is speculation and depends on the progress of other features! vvv**

### Phase 2 - Path Routing

Once the checksum has been found, Duke explains Vey's state and her capabilities to regenerate.
In the second phase the player has to rotate arrow symbols (<, >, ^, v) in order to make a path from a point A to point B in a maze.
Every time the player manages to make a path from point A to B, a new path is required starting from point B, 
that 3 times until the system regenerates and Duke holds his final speech, before the terminal fades black. 

**^^^ Everything within this block is speculation and depends on the progress of other features! ^^^**

---

**vvv Everything within this block is speculation and depends on the progress of other features! vvv**

In both phases (checksum and maze) the terminal sometimes suddenly switches to a different state showing Duke smiling while he gets corrupted by the corruption.
The player has to save Duke before he is fully corrupted holding a random key combination with 5 - 10 keys in a short time period for a total of 5 times.

**^^^ Everything within this block is speculation and depends on the progress of other features! ^^^**

## Technical
**Tech stack:** Java 21, Gradle

The game should be capable to work with only stdin/stout, without a GUI.
This is relevant to in cooperate this puzzle in SolutionWCMD as a feature.
Standalone the game will render the visuals on Swing components and the entire processing will be CPU based.

While the game has an ASCII / terminal aesthetic, the visual rendering will not be within a real terminal.
Characters and images will be pre-converted into ASCII data to keep the game light.

Duke's dialogue will be purely text based

**vvv Everything within this block is speculation and depends on the progress of other features! vvv**

Duke's dialogue may be voice-acted and processed to sound synthetic and degraded, matching the failing system aesthetic.

**^^^ Everything within this block is speculation and depends on the progress of other features! ^^^**
```
