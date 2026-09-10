package uk.ac.york.bitbotarena;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import uk.ac.york.bitbotarena.BotControllers.BotController;
import uk.ac.york.bitbotarena.BotControllers.DockerBotController;
import uk.ac.york.bitbotarena.BotControllers.GreedyBot;
import uk.ac.york.bitbotarena.BotControllers.RandomBot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Command(name = "bitbotarena", mixinStandardHelpOptions = true, version = "1.0",
        description = "Headless execution engine for BitBotArena.")
public class Main implements Runnable {

    @Option(names = {"-w", "--width"}, description = "The width of the arena grid.", defaultValue = "32")
    private int boardWidth;

    @Option(names = {"-H", "--height"}, description = "The height of the arena grid.", defaultValue = "32")
    private int boardHeight;

    @Option(names = {"-m", "--max-turns"}, description = "Maximum number of ticks before timeout.", defaultValue = "1000")
    private int maxTurns;

    @Option(names = {"-b", "--bots"}, description = "List of bot controllers.",
            split = ",", defaultValue = "java-template,greedy,random,random")
    private List<String> botIdentifiers;


    @Option(names = {"-d", "--debug"}, description = "Print per-tick latency logs.")
    private boolean debugMode = false;

    private static final Map<String, Supplier<BotController>> BOT_REGISTRY = Map.of(
            "random", RandomBot::new,
            "greedy", GreedyBot::new
    );

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {

        if (debugMode) {
            System.out.println("Debug mode enabled");
        }

        List<BotController> controllers = new ArrayList<>();

        for (String id : botIdentifiers) {
            String cleanId = id.trim().toLowerCase();
            Supplier<BotController> botSupplier = BOT_REGISTRY.get(cleanId);

            if (botSupplier != null) {
                controllers.add(botSupplier.get());
            } else {
                controllers.add(new DockerBotController(id.trim()));
            }
        }

        try {
            System.out.println("Initializing Match with " + controllers.size() + " bots...");
            MatchEngine matchEngine = new MatchEngine(boardWidth, boardHeight, controllers);

            int turn = 0;
            LocalDateTime start = LocalDateTime.now();

            System.out.println("\nStarting Match...");

            while (!matchEngine.isGameOver() && turn < maxTurns) {
                turn++;
                matchEngine.executeTick();
            }

            System.out.println("\n\nGame Over in " + turn + " turns!");
            LocalDateTime end = LocalDateTime.now();
            System.out.println("Total Time: " + ChronoUnit.MILLIS.between(start, end) + " ms\n");

            matchEngine.printFinalScoreboard();

        } finally {
            System.out.println("Cleaning up match allocations and container processes...");
            for (BotController controller : controllers) {
                if (controller instanceof DockerBotController) {
                    ((DockerBotController) controller).shutdown();
                }
            }
        }
    }
}