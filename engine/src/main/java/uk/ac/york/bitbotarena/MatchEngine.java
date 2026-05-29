package uk.ac.york.bitbotarena;

import uk.ac.york.bitbotarena.BotControllers.BotController;
import uk.ac.york.bitbotarena.BotControllers.DockerBotController;
import uk.ac.york.bitbotarena.BotControllers.RandomBot;

import java.util.Arrays;

import static uk.ac.york.bitbotarena.MatchVisualiser.botStates;
import static uk.ac.york.bitbotarena.MatchVisualiser.getVisualGrid;

public class MatchEngine {
    private final int width;
    private final int height;

    private final boolean headless = true;

    MatchState matchState;
    public MatchEngine(int width, int height,  int numberOfBots) {
        this.width = width;
        this.height = height;
        BotEntity[] bots = new BotEntity[numberOfBots];

        int[] x = {2,width-3,2,width-3};
        int[] y = {2,2,height-3,height-3};


        for (int i = 0; i < numberOfBots; i++) {
            BotController botController = new RandomBot();
            if (i == 0) {
                botController = new DockerBotController("java-template");
            }

            bots[i] = new BotEntity(width, height, x[i], y[i], botController, (byte) i);
        }

        matchState = new MatchState(width, height, bots);

        for (BotEntity bot : matchState.getBots()) {
            bot.initController(matchState);
        }
    }

    public void executeTick() {
        if (isGameOver()) {
            for (BotEntity bot : matchState.getBots()) {
                bot.gameOver(getWinningBotIndex(), getScores());
            }
        }

        BitBoard masterClaimed = new BitBoard(width, height);
        for (BotEntity botEntity : matchState.getBots()) {
            masterClaimed.or(botEntity.getClaimedBoard());
        }

        for (BotEntity bot : matchState.getBots()) {
            if (bot.isDead()) {
                continue;
            }

            // Remove own form enemy
            BitBoard enemyClaims = masterClaimed.copy();
            enemyClaims.xor(bot.getClaimedBoard());
            bot.updateInvalidBoard(enemyClaims);

            bot.executeMove(matchState);
        }

        killBotHeadCollisions();
        killBotClaimingCollisions();
        killBotInAreaJustClaimed();
        matchState.incrementTick();
    }

    @Override
    public String toString() {
        return getVisualGrid(matchState.getBots(), width, height) + "\n" + botStates(matchState.getBots());
    }

    private void killBotHeadCollisions(){
        for (int i = 0; i < matchState.getBots().length; i++) {
            for (int j = i + 1; j < matchState.getBots().length; j++) {
                BotEntity bot = matchState.getBots()[i];
                BotEntity otherBot = matchState.getBots()[j];
                if (bot.isDead() || otherBot.isDead()) continue;
                if (doCollide(bot.getCurrentPosition(), otherBot.getCurrentPosition())) {
                    log("Head collision between "+MatchVisualiser.colourBotName(i)+" and "+MatchVisualiser.colourBotName(j));
                    bot.kill();
                    otherBot.kill();
                }
            }
        }
    }

    private void killBotClaimingCollisions(){
        for (int i = 0; i < matchState.getBots().length; i++) {
            for (int j = i + 1; j < matchState.getBots().length; j++) {
                BotEntity bot = matchState.getBots()[i];
                BotEntity otherBot = matchState.getBots()[j];
                if (bot.isDead() || otherBot.isDead()) continue;
                if (doCollide(bot.getCurrentPosition(), otherBot.getClaimingBoard())) {
                    otherBot.kill();
                    bot.killedOtherBot();
                    log(MatchVisualiser.colourBotName(i)+ " cut off "+MatchVisualiser.colourBotName(j));
                }
                if (doCollide(bot.getClaimingBoard(), otherBot.getCurrentPosition())) {
                    log(MatchVisualiser.colourBotName(j)+ " cut off "+MatchVisualiser.colourBotName(i));
                    bot.kill();
                    otherBot.killedOtherBot();
                }
            }
        }
    }

    private void killBotInAreaJustClaimed(){
        for (int i = 0; i < matchState.getBots().length; i++) {
            for (int j = i + 1; j < matchState.getBots().length; j++) {
                BotEntity bot = matchState.getBots()[i];
                BotEntity otherBot = matchState.getBots()[j];
                if (bot.isDead() || otherBot.isDead()) continue;
                if (doCollide(bot.getClaimedBoard(), otherBot.getCurrentPosition())) {
                    otherBot.kill();
                    bot.killedOtherBot();
                    log(MatchVisualiser.colourBotName(i)+ " claimed "+MatchVisualiser.colourBotName(j)+"'s head");
                }
                if (doCollide(otherBot.getClaimedBoard(), bot.getCurrentPosition())) {
                    log(MatchVisualiser.colourBotName(j)+ " claimed "+MatchVisualiser.colourBotName(i)+"'s head");
                    bot.kill();
                    otherBot.killedOtherBot();
                }
            }
        }
    }

    private boolean doCollide(BitBoard botBoard1,BitBoard botBoard2) {
        return !botBoard1.noIntersection(botBoard2);
    }



    public boolean isGameOver() {
        return Arrays.stream(matchState.getBots()).filter(b -> !b.isDead()).count() <= 1;
    }

    private byte getWinningBotIndex() {
        return Arrays.stream(matchState.getBots()).filter(b -> !b.isDead()).findFirst().map(BotEntity::getIndex).orElse((byte) 0b100);
    }

    private short[] getScores() {
        short[] scores = new short[matchState.getBots().length];
        for (int i = 0; i < matchState.getBots().length; i++) {
            BotEntity bot = matchState.getBots()[i];
            int score = bot.getClaimedBoard().getWeight();
            score += bot.getKills() * 10;
            scores[i] = (short) score;
        }
        return scores;
    }

    public void printFinalScoreboard() {
        if (headless) {
            return;
        }
        log("=== FINAL SCORES ===");
        for (int i = 0; i < matchState.getBots().length; i++) {
            BotEntity bot = matchState.getBots()[i];
            int score = bot.getClaimedBoard().getWeight();
            score += bot.getKills() * 10;
            String status = bot.isDead() ? "[DEAD]" : "[ALIVE]";
            log(MatchVisualiser.colourBotName(i)+ " " + status + "\t Score: " + score+"\t Kills: "+bot.getKills());
        }
    }

    private void log(String message) {
        if(headless) {
            return;
        }
        System.out.println(message);
    }
}
