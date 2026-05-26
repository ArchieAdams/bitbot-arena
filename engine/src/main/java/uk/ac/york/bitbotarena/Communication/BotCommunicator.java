package uk.ac.york.bitbotarena.Communication;

import uk.ac.york.bitbotarena.BotEntity;
import uk.ac.york.bitbotarena.Movement;

import java.io.IOException;

public interface BotCommunicator {
    void sendState(int currentTick, byte botIndex, BotEntity[] bots) throws IOException;

    Movement readMove() throws IOException;
}