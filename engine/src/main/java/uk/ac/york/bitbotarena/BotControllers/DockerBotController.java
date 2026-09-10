package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.Communication.BinaryCommunicator;
import uk.ac.york.bitbotarena.Communication.BotCommunicator;
import uk.ac.york.bitbotarena.MatchState;
import uk.ac.york.bitbotarena.Movement;

import java.io.IOException;
import java.util.concurrent.*;

public class DockerBotController implements BotController {
    private final Process process;
    private final BotCommunicator communicator;
    private final ExecutorService timeoutExecutor = Executors.newSingleThreadExecutor();
    private final MoveReaderTask moveReaderTask = new MoveReaderTask();

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
        long timeoutBudget = 50;
        Future<Movement> futureMove = null;

        try {
            communicator.sendState(matchState, botIndex);

            futureMove = timeoutExecutor.submit(moveReaderTask);

            return futureMove.get(timeoutBudget, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            System.err.printf("\n[TIMEOUT] Bot %d hit a hard timeout at %dms!%n", botIndex, timeoutBudget);

            futureMove.cancel(true);

            handleBotDisqualification(botIndex);

            return Movement.NORTH;
        } catch (Exception e) {
            System.err.println("Bot " + botIndex + " crashed or disconnected.");
            return Movement.NORTH;
        }
    }

    private class MoveReaderTask implements Callable<Movement> {
        @Override
        public Movement call() throws Exception {
            return communicator.readMove();
        }
    }

    private void handleBotDisqualification(byte botIndex) {
        shutdown();
    }

    @Override
    public void init(MatchState matchState, byte botIndex) {
        try {
            communicator.sendGameStart(matchState, botIndex);
            communicator.readACK();
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