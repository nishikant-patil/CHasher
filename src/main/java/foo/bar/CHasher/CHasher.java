package foo.bar.CHasher;

import java.lang.reflect.Array;
import java.util.function.Function;

public class CHasher<T> {
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int HASH_BITS = 0x7fffffff;
    public static final int MINIMUM_RESOURCE_ARRAY_LENGTH = 32;

    private final T[] resources;

    private static int resourceArraySizeFor(int c) {
        int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
        int size = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        return size < MINIMUM_RESOURCE_ARRAY_LENGTH ? MINIMUM_RESOURCE_ARRAY_LENGTH : size;
    }

    @SafeVarargs
    public CHasher(Class<T> clazz, T... ts) {
        int noOfTs = ts.length;
        this.resources = getResourcesArray(clazz, noOfTs);
        fillResources(ts);
    }

    private void fillResources(T[] ts) {
        int tsCounter = 0;
        for (int i = 0; i < resources.length; i++) {
            if (tsCounter == ts.length) {
                tsCounter = 0;
            }
            this.resources[i] = ts[tsCounter++];
        }
    }

    private static int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    public T get(int hash) {
        int spreadHash = spread(hash);
        return this.resources[(this.resources.length - 1) & spreadHash];
    }


    public <V> V get(int hash, Function<T, V> extractor) {
        int spreadHash = spread(hash);
        return extractor.apply(this.resources[(this.resources.length - 1) & spreadHash]);
    }

    @SuppressWarnings("unchecked")
    private T[] getResourcesArray(Class<T> clazz, int noOfTs) {
        return (T[]) Array.newInstance(clazz, resourceArraySizeFor(noOfTs));
    }
}
