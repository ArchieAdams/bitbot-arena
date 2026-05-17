package uk.ac.york.bitbotarena.BotControllers;

import uk.ac.york.bitbotarena.BitBoard;
import uk.ac.york.bitbotarena.BotState;
import uk.ac.york.bitbotarena.Movement;

import java.util.*;
import java.util.stream.Collectors;

//TODO fix bot suicides
public class GreedyBot implements BotController {
    private static final Movement[] MOVEMENT_VALUES = Movement.values();
    private static final BitBoard[] cachedEdges = new BitBoard[MOVEMENT_VALUES.length];
    int claimingCount = 0;
    boolean retreating = false;
    Random random = new Random();
    BotState currentState;
    public GreedyBot() {
    }

    private BitBoard neighboursWorkspace;
    private BitBoard shiftWorkspace;
    private boolean initialized = false;

    private void lazyInit(int width, int height) {
        if (!initialized) {
            this.neighboursWorkspace = new BitBoard(width, height);
            this.shiftWorkspace = new BitBoard(width, height);
            this.initialized = true;
        }
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


    private BitBoard generateValidNeighbours(BitBoard startingBoard) {
        BitBoard neighbours = generateNeighbours(startingBoard);
        return removeInvalidBits(neighbours);
    }

    private BitBoard generateNeighbours(BitBoard startingBoard) {
        lazyInit(startingBoard.getWidth(), startingBoard.getHeight());

        neighboursWorkspace.clearBoard();
        neighboursWorkspace.copyFrom(startingBoard);

        shiftWorkspace.copyFrom(startingBoard);
        shiftWorkspace.shiftNorth(1);
        neighboursWorkspace.or(shiftWorkspace);

        shiftWorkspace.copyFrom(startingBoard);
        shiftWorkspace.shiftSouth(1);
        neighboursWorkspace.or(shiftWorkspace);


        shiftWorkspace.copyFrom(startingBoard);
        shiftWorkspace.shiftEast(1);
        neighboursWorkspace.or(shiftWorkspace);


        shiftWorkspace.copyFrom(startingBoard);
        shiftWorkspace.shiftWest(1);
        neighboursWorkspace.or(shiftWorkspace);

        neighboursWorkspace.xor(startingBoard);
        return neighboursWorkspace;
    }

    private BitBoard removeInvalidBits(BitBoard board) {
        BitBoard blockedMask = currentState.getClaimingBoard().copy();

        if (currentState.getInvalidBoard()==null) {
            board.and(blockedMask.notOutput());
            return board;
        }
        blockedMask.or(currentState.getInvalidBoard());
        board.and(blockedMask.notOutput());
        return board;
    }

    private Movement getDirectionTo(BitBoard currentPosition, BitBoard target) {
        Arrays.fill(cachedEdges, null);
        BitBoard center = generateValidNeighbours(currentPosition).andOutput(target);


        // Cross +
        if (!center.isEmpty()) {
            int startOffset = getRandomIndex(MOVEMENT_VALUES.length);

            for (int i = 0; i < MOVEMENT_VALUES.length; i++) {
                Movement move = MOVEMENT_VALUES[(i + startOffset) % 4];

                BitBoard edge = generateNeighbourSection(currentPosition, move).andOutput(target);
                cachedEdges[move.ordinal()] = edge;

                if (!edge.intersects(center) && currentState.validMove(move)) {
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

                if (!verticalEdge.intersects(horizontalEdge)) {

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
        int startOffset = getRandomIndex(MOVEMENT_VALUES.length);
        for (int i = 0; i < MOVEMENT_VALUES.length; i++) {
            Movement move = MOVEMENT_VALUES[(i + startOffset) % 4];
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
        return MOVEMENT_VALUES[getRandomIndex(MOVEMENT_VALUES.length)];
    }
    
    private int getRandomIndex(int size) {
        return random.nextInt(size);
    }

    private Movement safeRandomMove() {
        Movement[] validMoves = new Movement[4];
        int index = 0;

        for (Movement move : MOVEMENT_VALUES) {
            if (currentState.validMove(move)) {
                validMoves[index++] = move;
            }
        }

        if (index==0) {
            return randomMove();
        }
        return validMoves[getRandomIndex(index)];
    }

}
