package foo.bar.CHasher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CHasherTest {

    public static final int ROLLOVER_BOUNDARY = 0xFFFF;

    @Test
    void testCHasherGet() {
        var cHasher = new CHasher<>(1, 2, 3);
        assertEquals(0, getCollisionCount(cHasher));
    }

    @Test
    void testCHasherGetWithExtractor() {
        var cHasher = new CHasher<>(1, 2, 3);
        assertEquals(0, getCollisionCountWithExtractor(cHasher));
    }

    private int getCollisionCount(CHasher<Integer> cHasher) {
        int prev = 0;
        int collisionCount = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int current = cHasher.get(i & ROLLOVER_BOUNDARY);
            if (current == prev) {
                ++collisionCount;
            }
            prev = current;
        }
        return collisionCount;
    }

    private int getCollisionCountWithExtractor(CHasher<Integer> cHasher) {
        double prev = 0;
        int collisionCount = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            double current = cHasher.get(i & ROLLOVER_BOUNDARY, Double::valueOf);
            if (current == prev) {
                ++collisionCount;
            }
            prev = current;
        }
        return collisionCount;
    }

    @Test
    void testMarkDead() {
        var cHasher = new CHasher<>(1, 2, 3);
        cHasher.markDead(2);

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            assertNotEquals(2, cHasher.get(i & ROLLOVER_BOUNDARY));
        }
    }

    @Test
    void testRevive() {
        var cHasher = new CHasher<>(1, 2, 3);
        cHasher.markDead(2);

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            assertNotEquals(2, cHasher.get(i & ROLLOVER_BOUNDARY));
        }

        cHasher.revive(2);

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int resource = cHasher.get(i & ROLLOVER_BOUNDARY);
            if(2 == resource) {
                return;
            }
        }
        assertTrue(false);
    }
}
