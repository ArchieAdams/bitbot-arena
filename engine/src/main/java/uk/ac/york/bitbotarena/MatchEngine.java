package uk.ac.york.bitbotarena;

import uk.ac.york.bitbotarena.BotControllers.BotController;
import uk.ac.york.bitbotarena.BotControllers.GreedyBot;
import uk.ac.york.bitbotarena.BotControllers.RandomBot;

import java.util.Arrays;

import static uk.ac.york.bitbotarena.MatchVisualiser.botStates;
import static uk.ac.york.bitbotarena.MatchVisualiser.getVisualGrid;

public class MatchEngine {
    private final int width;
    private final int height;

    private boolean headless = true;
    
    private BotEntity[] bots;
    public MatchEngine(int width, int height,  int numberOfBots) {
        this.width = width;
        this.height = height;
        this.bots = new BotEntity[numberOfBots];

        int[] x = {2,width-3,2,width-3};
        int[] y = {2,2,height-3,height-3};


        for (int i = 0; i < numberOfBots; i++) {
            BotController botController = new GreedyBot();
            if (i==3){
                botController = new RandomBot();
            }
            bots[i] = new BotEntity(width, height, x[i], y[i], botController);
        }
    }


    public void executeTick() {
        BitBoard masterClaimed = new BitBoard(width, height);
        for (int i = 0; i < bots.length; i++) {
            masterClaimed.or(bots[i].getClaimedBoard());
        }

        for (int i = 0; i < bots.length; i++) {
            BotEntity bot = bots[i];

            if (bot.isDead()) {
                continue;
            }

            // Remove own form enemy
            BitBoard enemyClaims = masterClaimed.copy();
            enemyClaims.xor(bot.getClaimedBoard());
            bot.updateInvalidBoard(enemyClaims);

            bot.executeMove();
        }

        killBotHeadCollisions();
        killBotClaimingCollisions();
        killBotInAreaJustClaimed();
    }

    @Override
    public String toString() {
        return getVisualGrid(bots, width, height) + "\n" + botStates(bots);
    }

    private void killBotHeadCollisions(){
        for (int i = 0; i < bots.length; i++) {
            for (int j = i + 1; j < bots.length; j++) {
                BotEntity bot = bots[i];
                BotEntity otherBot = bots[j];
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
        for (int i = 0; i < bots.length; i++) {
            for (int j = i + 1; j < bots.length; j++) {
                BotEntity bot = bots[i];
                BotEntity otherBot = bots[j];
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
        for (int i = 0; i < bots.length; i++) {
            for (int j = i + 1; j < bots.length; j++) {
                BotEntity bot = bots[i];
                BotEntity otherBot = bots[j];
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
        return !botBoard1.intersects(botBoard2);
    }



    public boolean isGameOver() {
        return Arrays.stream(bots).filter(b -> !b.isDead()).count() <= 1;
    }

    public void printFinalScoreboard() {
        if (headless) {
            return;
        }
        log("=== FINAL SCORES ===");
        for (int i = 0; i < bots.length; i++) {
            BotEntity bot = bots[i];
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
