package no.ndla.taxonomy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUtils {
    public static final String BASE_URL = "http://localhost:5000";
    public static final String CLIENT_ID = "ITEST";
    public static final String CLIENT_SECRET = "[CLIENT SECRET]";
    public static final String TOKEN_SERVER = "[https://url.somewhere]";

    public static <V> void assertAnyTrue(V[] objects, Predicate<V> predicate) {
        assertTrue("Array was empty", objects.length > 0);
        String className = objects[0].getClass().getSimpleName();
        assertTrue("No " + className + " matching predicate found.", Arrays.stream(objects).anyMatch(predicate));
    }

    public static <V> void assertAnyTrue(Iterable<V> objects, Predicate<V> predicate) {
        assertAnyTrue(objects.iterator(), predicate);
    }

    public static <V> void assertAnyTrue(Iterator<V> objects, Predicate<V> predicate) {
        assertTrue(objects.hasNext());

        String className = null;
        while (objects.hasNext()) {
            V next = objects.next();
            className = next.getClass().getSimpleName();
            if (predicate.test(next)) return;
        }

        if (null == className) fail("Empty collection");
        fail("No " + className + " matching predicate found.");
    }
}
