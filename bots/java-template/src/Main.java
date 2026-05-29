import java.io.DataInputStream;
import java.io.IOException;

public class Main {
    static void main(String[] args) {
        GameStateView state = new GameStateView();
        FrameReader reader = new FrameReader(new DataInputStream(System.in));
        FrameType currentFrame;

        try {
            while (true) {
                currentFrame = reader.readNextFrame(state);
                if (currentFrame == FrameType.GameOver) {
                    break;
                } else if (currentFrame == FrameType.GameState) {
                    byte nextMove = MyBot.calculateBestMove(state);

                    System.out.write(nextMove);
                    System.out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}