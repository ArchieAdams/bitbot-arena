package uk.ac.york.bitbotarena.Communication;

import uk.ac.york.bitbotarena.MatchState;
import uk.ac.york.bitbotarena.Movement;

import java.io.IOException;

public interface BotCommunicator {
    void sendState(MatchState matchState, byte botIndex) throws IOException;

    void sendGameStart(MatchState matchState, byte botIndex) throws IOException;

    void sendGameEnd(byte winningBot, short[] scores, byte botIndex) throws IOException;

    Movement readMove() throws IOException;

    void close() throws IOException;
}