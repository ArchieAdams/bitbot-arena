package uk.ac.york.bitbotarena;

public class MatchVisualiser {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String[] COLOURS = {RED, GREEN,BLUE, YELLOW, PURPLE, CYAN};

    public static String colourBotName(int botIndex) {
        return colourText(botIndex, "Bot " + botIndex);
    }

    public static String colourText(int botIndex, String text) {
        if (botIndex < COLOURS.length) {
            return COLOURS[botIndex] + text + RESET;
        }
        return "";
    }

    public static String getVisualGrid(BotEntity[] bots, int width, int height) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String foundString = "";
                for (int i = 0; i < bots.length; i++) {

                    BotEntity bot = bots[i];
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

    public static String botStates(BotEntity[] bots) {
        StringBuilder deaths = new StringBuilder();
        deaths.append("Deaths:\t");
        StringBuilder claiming = new StringBuilder();
        claiming.append("Claiming:\t");
        for (int i = 0; i < bots.length; i++) {
            if(bots[i].isDead()){
                deaths.append(colourText(i,"Bot "+i)).append("\t");
            }
            if (bots[i].isClaiming()) {
                claiming.append(colourText(i,"Bot "+i)).append("\t");
            }
        }
        return deaths.append("\n").append(claiming).toString();
    }
}
