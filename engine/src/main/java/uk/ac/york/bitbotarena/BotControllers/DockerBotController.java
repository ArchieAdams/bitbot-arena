package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.Communication.BinaryCommunicator;
import uk.ac.york.bitbotarena.Communication.BotCommunicator;
import uk.ac.york.bitbotarena.MatchState;
import uk.ac.york.bitbotarena.Movement;

import java.io.IOException;

public class DockerBotController implements BotController {
    private final Process process;
    private final BotCommunicator communicator;

    public DockerBotController(String dockerImageName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "run", "-i", "--rm", dockerImageName);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            this.process = pb.start();
            this.communicator = new BinaryCommunicator(process);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start container: " + dockerImageName, e);
        }
    }

    public void shutdown() {
        try {
            communicator.close();
            if (process != null) process.destroyForcibly();
        } catch (IOException ignored) {
        }
    }

    @Override
    public Movement getMove(MatchState matchState, byte botIndex) {
        try {
            communicator.sendState(matchState, botIndex);
            Thread.sleep(50);
            return communicator.readMove();
        } catch (IOException e) {
            throw new RuntimeException("Failed to communicate with bot in container", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(MatchState matchState, byte botIndex) {
        try {
            communicator.sendGameStart(matchState, botIndex);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send game start to bot in container", e);
        }
    }

    @Override
    public void gameOver(byte winningBot, short[] scores, byte botIndex) {
        try {
            communicator.sendGameEnd(winningBot, scores, botIndex);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send game end to bot in container", e);
        }
    }
}