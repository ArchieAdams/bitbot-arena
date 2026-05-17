package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.BotState;
import uk.ac.york.bitbotarena.Movement;

public interface BotController {
    Movement getMove(BotState state);
}
