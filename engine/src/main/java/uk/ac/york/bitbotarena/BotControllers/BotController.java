package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.MatchState;
import uk.ac.york.bitbotarena.Movement;

public interface BotController {
    Movement getMove(MatchState matchState, byte botIndex);

    void init(MatchState matchState, byte botIndex);

    void gameOver(byte winningBot, short[] scores, byte botIndex);
}
