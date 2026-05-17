package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.BotState;
import uk.ac.york.bitbotarena.Movement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomBot implements BotController {

    private Random random = new Random();
    public RandomBot() {
    }

    @Override
    public Movement getMove(BotState state){
        Movement[] movements = {Movement.NORTH, Movement.EAST, Movement.SOUTH, Movement.WEST};
        List<Movement> movementList = new ArrayList<>(Arrays.asList(movements));
        Movement move = null;

        while (!movementList.isEmpty()) {
            Movement randomMove = movementList.get(random.nextInt(movementList.size()));
            if (state.validMove(randomMove)){
                move = randomMove;
                break;
            } else {
                movementList.remove(randomMove);
            }
        }

        if (move == null) {
            movements = new Movement[]{Movement.NORTH, Movement.EAST, Movement.SOUTH, Movement.WEST};
            move = movements[random.nextInt(movements.length)];
        }
        return move;
    }
}
