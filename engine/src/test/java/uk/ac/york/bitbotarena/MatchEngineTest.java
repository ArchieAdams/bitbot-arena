package uk.ac.york.bitbotarena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.ac.york.bitbotarena.BotControllers.BotController;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MatchEngine Collision Tests")
class MatchEngineCollisionTest {

    private static BotController fixed(Movement move) {
        return state -> move;
    }

    private static void injectBots(MatchEngine engine, BotEntity[] bots) {
        try {
            Field botsField = MatchEngine.class.getDeclaredField("bots");
            botsField.setAccessible(true);
            botsField.set(engine, bots);
        } catch (ReflectiveOperationException e) {
            fail("Failed to inject bots into MatchEngine for test setup: " + e.getMessage());
        }
    }

    @Nested
    @DisplayName("Head Collision Tests")
    class HeadCollisionTests {

        @Test
        void executeTick_shouldKillBothBotsOnHeadCollision() {
            MatchEngine engine = new MatchEngine(32, 32, 2);

            BotEntity left = new BotEntity(32, 32, 10, 10, fixed(Movement.EAST), (byte) 0);
            BotEntity right = new BotEntity(32, 32, 12, 10, fixed(Movement.WEST), (byte) 1);

            injectBots(engine, new BotEntity[]{left, right});

            engine.executeTick();

            assertTrue(left.isDead(), "Left bot should die in head collision");
            assertTrue(right.isDead(), "Right bot should die in head collision");
        }

        @Test
        void executeTick_shouldSkipHeadCollisionWhenOneBotAlreadyDead() {
            MatchEngine engine = new MatchEngine(32, 32, 2);

            BotEntity left = new BotEntity(32, 32, 10, 10, fixed(Movement.EAST), (byte) 0);
            BotEntity right = new BotEntity(32, 32, 12, 10, fixed(Movement.WEST), (byte) 1);
            right.kill();

            injectBots(engine, new BotEntity[]{left, right});

            engine.executeTick();

            assertFalse(left.isDead(), "Live bot should not be killed by dead bot in head-collision pass");
            assertTrue(right.isDead(), "Dead bot should remain dead");
        }
    }

    @Nested
    @DisplayName("Claiming Collision Tests")
    class ClaimingCollisionTests {

        @Test
        void executeTick_shouldKillOtherBotWhenHeadHitsClaimingTrail() {
            MatchEngine engine = new MatchEngine(32, 32, 2);

            // Bot A head will move onto (11,10)
            BotEntity botA = new BotEntity(32, 32, 10, 10, fixed(Movement.EAST), (byte) 0);
            // Bot B has claiming trail on (11,10)
            BotEntity botB = new BotEntity(32, 32, 20, 20, fixed(Movement.NORTH), (byte) 1);
            botB.getClaimingBoard().setBit(11, 10);

            injectBots(engine, new BotEntity[]{botA, botB});

            engine.executeTick();

            assertFalse(botA.isDead(), "Bot A should survive when cutting Bot B trail");
            assertTrue(botB.isDead(), "Bot B should die when its trail is cut");
            assertEquals(1, botA.getKills(), "Bot A should receive one kill");
        }

        @Test
        void executeTick_shouldKillBotWhenItsTrailHitsOtherHead() {
            MatchEngine engine = new MatchEngine(32, 32, 2);

            // Bot A trail on (11,10), Bot B head moves to (11,10)
            BotEntity botA = new BotEntity(32, 32, 5, 5, fixed(Movement.NORTH), (byte) 0);
            botA.getClaimingBoard().setBit(11, 10);

            BotEntity botB = new BotEntity(32, 32, 10, 10, fixed(Movement.EAST), (byte) 1);

            injectBots(engine, new BotEntity[]{botA, botB});

            engine.executeTick();

            assertTrue(botA.isDead(), "Bot A should die when other bot head intersects A trail");
            assertFalse(botB.isDead(), "Bot B should survive");
            assertEquals(1, botB.getKills(), "Bot B should receive one kill");
        }
    }

    @Nested
    @DisplayName("Claimed Area Collision Tests")
    class ClaimedAreaCollisionTests {

        @Test
        void executeTick_shouldKillBotTrappedInsideCompletedClaimedArea() {
            MatchEngine engine = new MatchEngine(32, 32, 2);

            // Owner builds a claimed square around intruder
            BotEntity owner = new BotEntity(32, 32, 8, 9, fixed(Movement.NORTH), (byte) 0);
            // Ensure owner starts out of a claim
            owner.getClaimedBoard().clearBoard();
            owner.getClaimedBoard().setBit(8, 7);

            // Draw a 1-thick claimed square from (8,8) to (13,13) leaving intruder trapped inside at (11,11)
            for (int x = 8; x <= 13; x++) {
                owner.getClaimingBoard().setBit(x, 8);   // top edge
                owner.getClaimingBoard().setBit(x, 13);  // bottom edge
            }
            for (int y = 8; y <= 13; y++) {
                owner.getClaimingBoard().setBit(8, y);   // left edge
                owner.getClaimingBoard().setBit(13, y);  // right edge
            }

            owner.getClaimingBoard().clearBit(8, 8);

            // Intruder trapped in the middle, tries to move (any direction hits claimed)
            BotEntity intruder = new BotEntity(32, 32, 11, 11, fixed(Movement.NORTH), (byte) 1);
            intruder.getClaimingBoard().clearBoard();

            injectBots(engine, new BotEntity[]{owner, intruder});

            engine.executeTick();
            engine.executeTick();

            assertTrue(intruder.isDead(), "Intruder surrounded by claimed area should die");
            assertEquals(1, owner.getKills(), "Owner should gain kill credit");
        }
    }
}