package uk.ac.york.bitbotarena;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Main {
    private static final int BOARD_WIDTH = 32;
    private static final int BOARD_HEIGHT = 32;

    static void main(String[] args) {
        MatchEngine matchEngine = new MatchEngine(BOARD_WIDTH,BOARD_HEIGHT,4);
        int turn=0;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime turnTime;
        StringBuilder turnTimes = new StringBuilder();
        while (!matchEngine.isGameOver()) {
            turnTime = LocalDateTime.now();
            turn++;
            try {
                Thread.sleep(50);
                System.out.println(matchEngine);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            matchEngine.executeTick();
            System.out.printf("\033[%dA", 14);
            turnTimes.append("Turn ").append(turn).append(" time: ").append(ChronoUnit.MICROS.between(turnTime, LocalDateTime.now())).append(" μs\n");

            if (turn==100) {
                break;
            }
        }
        System.out.println("Game Over in " + turn + " turns!");
        LocalDateTime end = LocalDateTime.now();
        System.out.println("Total Time: " + ChronoUnit.MILLIS.between(start, end) + " ms");
        System.out.println(turnTimes);
        System.out.println(matchEngine);
        matchEngine.printFinalScoreboard();
    }
}
