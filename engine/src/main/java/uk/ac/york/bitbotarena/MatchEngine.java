package uk.ac.york.bitbotarena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MatchEngine {
    private int width;
    private int height;

    private int numberOfBots;
    private BotState[] bots;
    public MatchEngine(int width, int height,  int numberOfBots) {
        this.width = width;
        this.height = height;
        this.numberOfBots = numberOfBots;
        this.bots = new BotState[numberOfBots];
        Random rand = new Random();
        for (int i = 0; i < numberOfBots; i++) {
            bots[i] = new BotState(width, height, rand.nextInt(width), rand.nextInt(height));
        }
    }

    public void executeTick() {
        BotState[] aliveBots = Arrays.stream(bots).filter(b -> !b.isDead()).toArray(BotState[]::new);

        for (int i = 0; i < bots.length; i++) {
            BitBoard invalid = new BitBoard(width, height);
            BotState bot = bots[i];
            if (bot.isDead()) {
                continue;
            }
            for (int j = 0; j < bots.length; j++) {
                if (j == i) continue;
                invalid.or(bots[j].getClaimedBoard());
            }
            bot.updateInvalidBoard(invalid);
        }

        for (BotState bot : aliveBots) {
            Movement move = getBotMove(bot);
            bot.move(move);
        }
        killBotHeadCollisions();
        killBotClaimingCollisions();

        System.out.println(getVisualGrid());
        System.out.println(botStates());
    }

    private Movement getBotMove(BotState bot) {
        Random rand = new Random();

        Movement[] movements = {Movement.NORTH, Movement.EAST, Movement.SOUTH, Movement.WEST};
        List<Movement> movementList = new ArrayList<>(Arrays.asList(movements));
        Movement move = null;
        while (!movementList.isEmpty()) {
            Movement randomMove = movementList.get(rand.nextInt(movementList.size())); // use current size
            if (bot.validMove(randomMove)){
                move = randomMove;
                System.out.println(move);
                break;
            } else {
                movementList.remove(randomMove);
            }
        }
        if (move == null) {
            movements = new Movement[]{Movement.NORTH, Movement.EAST, Movement.SOUTH, Movement.WEST};
            move = movements[rand.nextInt(movements.length)];
        }
        return move;
    }

    private void killBotHeadCollisions(){
        BotState[] aliveBots = Arrays.stream(bots).filter(b -> !b.isDead()).toArray(BotState[]::new);
        for (int i = 0; i < aliveBots.length; i++) {
            for (int j = i + 1; j < aliveBots.length; j++) {
                BotState bot = aliveBots[i];
                BotState otherBot = aliveBots[j];
                if (doCollide(bot.getCurrentPosition(), otherBot.getCurrentPosition())) {
                    bot.kill();
                    otherBot.kill();
                }
            }
        }
    }

    private void killBotClaimingCollisions(){
        BotState[] aliveBots = Arrays.stream(bots).filter(b -> !b.isDead()).toArray(BotState[]::new);
        for (int i = 0; i < aliveBots.length; i++) {
            for (int j = i + 1; j < aliveBots.length; j++) {
                BotState bot = aliveBots[i];
                BotState otherBot = aliveBots[j];
                if (doCollide(bot.getCurrentPosition(), otherBot.getClaimingBoard())) {
                    otherBot.kill();
                }
                if (doCollide(bot.getClaimingBoard(), otherBot.getCurrentPosition())) {
                    bot.kill();
                }
            }
        }
    }

    private boolean doCollide(BitBoard botBoard1,BitBoard botBoard2) {
        return !botBoard1.andOutput(botBoard2).isEmpty();
    }

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String[] COLOURS = {RED, GREEN, YELLOW, BLUE, PURPLE, CYAN};

    public String colourText(int botIndex, String text) {
        if (botIndex < COLOURS.length) {
            return COLOURS[botIndex] + text + RESET;
        }
        return "";
    }

    public String getVisualGrid() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String foundString = "";
                for (int i = 0; i < numberOfBots; i++) {

                    BotState bot = bots[i];
                    if (bot.getCurrentPosition().getBit(x, y)) {
                        foundString = colourText(i,"P");
                        break;
                    }
                    else if (bot.getClaimingBoard().getBit(x, y)) {
                        foundString = colourText(i,"+");
                        break;
                    }
                    else if (bot.getClaimedBoard().getBit(x, y)) {
                        foundString = colourText(i,"#");
                        break;
                    }
                }
                if (foundString.isEmpty()) {
                    sb.append(". ");
                }
                else {
                    sb.append(foundString).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String botStates(){
        StringBuilder deaths = new StringBuilder();
        deaths.append("Deaths:\t");
        StringBuilder claiming = new StringBuilder();
        claiming.append("Claiming:\t");
        for (int i = 0; i < numberOfBots; i++) {
            if(bots[i].isDead()){
                deaths.append(colourText(i,"Bot "+i)).append("\t");
            }
            if (bots[i].isClaiming()) {
                claiming.append(colourText(i,"Bot "+i)).append("\t");
            }
        }
        return deaths.append("\n").append(claiming).toString();
    }

    public boolean isGameOver() {
        return Arrays.stream(bots).filter(b -> !b.isDead()).count() <= 1;
    }


}
