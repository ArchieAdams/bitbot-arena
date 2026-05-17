package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.BitBoard;
import uk.ac.york.bitbotarena.BotState;
import uk.ac.york.bitbotarena.Movement;

import java.util.*;
import java.util.stream.Collectors;

//TODO fix bot suicides
public class GreedyBot implements BotController {
    int claimingCount = 0;
    boolean retreating = false;
    Random random = new Random();
    BotState currentState;
    public GreedyBot() {
    }

    @Override
    public Movement getMove(BotState state) {
        currentState = state;
        if (state.isClaiming()) {
            if (claimingCount > 0 || retreating) {
                retreating = true;
                return getDirectionTo(state.getCurrentPosition(),state.getClaimedBoard());
            }
            else {
                claimingCount++;
                return getDirectionTo(state.getCurrentPosition(),state.getClaimedBoard().notOutput());
            }
        }
        else {
            claimingCount=0;
            retreating = false;
            return getDirectionTo(state.getCurrentPosition(),state.getClaimedBoard().notOutput());
        }
    }

    private BitBoard neighbourOverlap(BitBoard currentPosition, BitBoard target) {
        BitBoard neighbours = generateNeighbours(currentPosition);
        BitBoard overlap = target.andOutput(neighbours);
        if (!overlap.isEmpty()){
            return overlap;
        }

        return null;
    }

    private BitBoard generateValidNeighbours(BitBoard startingBoard) {
        BitBoard neighbours = generateNeighbours(startingBoard);
        return removeInvalidBits(neighbours);
    }

    private BitBoard generateNeighbours(BitBoard startingBoard) {
        BitBoard neighbours = startingBoard.copy();

        neighbours.or(startingBoard.shiftNorthOutput(1));
        neighbours.or(startingBoard.shiftSouthOutput(1));
        neighbours.or(startingBoard.shiftEastOutput(1));
        neighbours.or(startingBoard.shiftWestOutput(1));

        neighbours.xor(startingBoard);
        return neighbours;
    }

    private BitBoard removeInvalidBits(BitBoard board) {
        BitBoard blockedMask = currentState.getClaimingBoard().copy();

        if (currentState.getInvalidBoard()==null) {
            return board.andOutput(blockedMask.notOutput());
        }
        blockedMask.or(currentState.getInvalidBoard());
        return board.andOutput(blockedMask.notOutput());
    }

    private Movement getDirectionTo(BitBoard currentPosition, BitBoard target) {
        BitBoard center = generateValidNeighbours(currentPosition).andOutput(target);

        BitBoard[] cachedEdges = new BitBoard[Movement.values().length];


        // Cross +
        if (!center.isEmpty()) {
            int startOffset = getRandomIndex(Movement.values().length);

            for (int i = 0; i < Movement.values().length; i++) {
                Movement move = Movement.values()[(i + startOffset) % 4];

                BitBoard edge = generateNeighbourSection(currentPosition, move).andOutput(target);
                cachedEdges[move.ordinal()] = edge;

                if (!edge.andOutput(center).isEmpty() && currentState.validMove(move)) {
                    return move;
                }
            }
        }

        Movement[] verticals = random.nextBoolean() ?
                new Movement[]{Movement.NORTH, Movement.SOUTH} : new Movement[]{Movement.SOUTH, Movement.NORTH};
        Movement[] horizontals = random.nextBoolean() ?
                new Movement[]{Movement.EAST, Movement.WEST} : new Movement[]{Movement.WEST, Movement.EAST};

        // Responsible for North East, North West... e.g. corners
        for (Movement verticalMove : verticals) {
            for (Movement horizontalMove : horizontals) {

                BitBoard verticalEdge = getOrComputeEdge(cachedEdges, currentPosition, target, verticalMove);
                BitBoard horizontalEdge = getOrComputeEdge(cachedEdges, currentPosition, target, horizontalMove);

                if (!verticalEdge.andOutput(horizontalEdge).isEmpty()) {

                    // Check validity in a random order
                    boolean verticalFirst = random.nextBoolean();
                    Movement primary = verticalFirst ? verticalMove : horizontalMove;
                    Movement secondary = verticalFirst ? horizontalMove : verticalMove;

                    if (currentState.validMove(primary)) {
                        return primary;
                    } else if (currentState.validMove(secondary)) {
                        return secondary;
                    }
                }
            }
        }

        // Must be on far edges
        int startOffset = getRandomIndex(Movement.values().length);
        for (int i = 0; i < Movement.values().length; i++) {
            Movement move = Movement.values()[(i + startOffset) % 4];
            BitBoard edge = getOrComputeEdge(cachedEdges, currentPosition, target, move);

            if (!edge.isEmpty() && currentState.validMove(move)) {
                return move;
            }
        }

        return safeRandomMove();
    }


    private BitBoard getOrComputeEdge(BitBoard[] cache, BitBoard pos, BitBoard target, Movement move) {
        if (cache[move.ordinal()] == null) {
            cache[move.ordinal()] = generateNeighbourSection(pos, move).andOutput(target);
        }
        return cache[move.ordinal()];
    }



    // Generates the following mask
    /*
    X
    XX
    X
     */
    private BitBoard generateNeighbourSection(BitBoard startingBoard, Movement movement) {
        BitBoard newCentre = startingBoard.shiftOutput(movement,1);
        BitBoard neighbours = generateNeighbours(newCentre);

        // Removes old center
        neighbours.xor(startingBoard);

        // Add back new center
        neighbours.or(newCentre);

        return removeInvalidBits(neighbours);
    }


    private Movement randomMove() {
        return Movement.values()[getRandomIndex(Movement.values().length)];
    }
    
    private int getRandomIndex(int size) {
        return random.nextInt(size);
    }

    private Movement safeRandomMove() {
        List<Movement> validMove = filterByValidMoves(Arrays.stream(Movement.values()).toList());
        if (validMove.isEmpty()) {
            return randomMove();
        }
        return validMove.get(getRandomIndex(validMove.size()));
    }

    private List<Movement> filterByValidMoves(List<Movement> moves) {
        return moves.stream()
                .filter(move -> currentState.validMove(move))
                .collect(Collectors.toList());
    }

}
