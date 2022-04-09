package foo.bar.CHasher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CHasherTest {

    public static final int ROLLOVER_BOUNDARY = 0xFFFF;

    @Test
    void testCHasherGet() {
        var cHasher = new CHasher<>(Integer.class, 1, 2, 3);
        assertEquals(0, getCollisionCount(cHasher));
    }

    @Test
    void testCHasherGetWithExtractor() {
        var cHasher = new CHasher<>(Integer.class, 1, 2, 3);
        assertEquals(0, getCollisionCountWithExtractor(cHasher));
    }

    private int getCollisionCount(CHasher<Integer> cHasher) {
        int prev = 0;
        int collisionCount = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int randomInt = i & 0xFFFF;
            int current = cHasher.get(randomInt);
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
            int randomInt = i & ROLLOVER_BOUNDARY;
            double current = cHasher.get(randomInt, Double::valueOf);
            if (current == prev) {
                ++collisionCount;
            }
            prev = current;
        }
        return collisionCount;
    }
}
