package foo.bar.CHasher;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class CHasher<T> {
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int HASH_BITS = 0x7fffffff;
    public static final int MINIMUM_RESOURCE_ARRAY_LENGTH = 32;

    private final AtomicReference<T[]> resourceRef = new AtomicReference<>();
    private final T[] originalResources;

    private final T TOMBSTONE = null;

    private static int resourceArraySizeFor(int c) {
        int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
        int size = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        return size < MINIMUM_RESOURCE_ARRAY_LENGTH ? MINIMUM_RESOURCE_ARRAY_LENGTH : size;
    }

    @SafeVarargs
    public CHasher(T... ts) {
        this.resourceRef.set(getResourcesArray(ts));
        originalResources = ts;
    }

    private void fillResources(T[] ts, T[] resources) {
        int tsCounter = 0;
        for (int i = 0; i < resources.length; ++i) {
            if (tsCounter == ts.length) {
                tsCounter = 0;
            }
            resources[i] = ts[tsCounter++];
        }
    }

    private static int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    public T get(int hash) {
        int spreadHash = spread(hash);
        T[] resources = this.resourceRef.get();
        T t = resources[((resources.length - 1) & spreadHash++)];
        while (t == TOMBSTONE) {
            t = resources[((resources.length - 1) & spreadHash++)];
        }
        return t;
    }

    public void markDead(T t) {
        T[] resources = this.resourceRef.get();
        T[] newResources = (T[]) Array.newInstance(resources[0].getClass(), resources.length);

        markDead(resources, t, newResources);

        while (!this.resourceRef.compareAndSet(resources, newResources)) {
            resources = this.resourceRef.get();
            newResources = (T[]) Array.newInstance(resources[0].getClass(), resources.length);
            markDead(resources, t, newResources);
        }
    }

    private void markDead(T[] resources, T t, T[] newResources) {
        for (int i = 0; i < resources.length; ++i) {
            if (t.equals(resources[i])) {
                newResources[i] = TOMBSTONE;
            } else {
                newResources[i] = resources[i];
            }
        }
    }

    public void revive(T t) {
        T[] resources = this.resourceRef.get();
        T[] newResources = getResourcesArray(originalResources);

        revive(resources, t, newResources);

        while (!this.resourceRef.compareAndSet(resources, newResources)) {
            resources = this.resourceRef.get();
            newResources = getResourcesArray(originalResources);

            revive(resources, t, newResources);
        }
    }

    private void revive(T[] resources, T t, T[] newResources) {
        for (int i = 0; i < resources.length; ++i) {
            if (resources[i] == TOMBSTONE && !t.equals(newResources[i])) {
                newResources[i] = TOMBSTONE;
            }
        }
    }

    public <V> V get(int hash, Function<T, V> extractor) {
        return extractor.apply(get(hash));
    }

    @SuppressWarnings("unchecked")
    private T[] getResourcesArray(T[] ts) {
        T[] resources = (T[]) Array.newInstance(ts[0].getClass(), resourceArraySizeFor(ts.length));
        fillResources(ts, resources);
        return resources;
    }
}
