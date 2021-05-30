package pers.chartiose.file.manager;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws InterruptedException {
        SynchronousQueue<Long> synchronousQueue = new SynchronousQueue<>(true);
        assertTrue(synchronousQueue.offer((long) 10));
    }
}