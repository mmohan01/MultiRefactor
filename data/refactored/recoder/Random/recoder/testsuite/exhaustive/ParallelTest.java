package recoder.testsuite.exhaustive;
import java.util.ArrayList;

import junit.framework.TestResult;
import recoder.testsuite.CompleteTestSuite;
import recoder.testsuite.RecoderTestCase;

/**
 * Created on 18 feb 2009
 */

/**
 * @author Tobias Gutzmann
 *
 */
public class ParallelTest extends RecoderTestCase {
    private final int nThreads = Runtime.getRuntime().availableProcessors();
    private ArrayList<TestResult> results = new ArrayList<TestResult>();

    public void testCompleteTestsuiteInParallel() {
        System.out.println("Starting parallel testing with " + nThreads + " threads...");
        Thread[] threads = new Thread[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(new RunOnce());
        }
        threads[0].setPriority(Thread.MIN_PRIORITY);
        threads[nThreads - 1].setPriority(Thread.MAX_PRIORITY);
        for (int i = 0; i < nThreads; i++) {
            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++) {
            try {
                threads[i].join();
                System.out.println(i + " died");
            } catch (InterruptedException e) {
                fail();
            }
        }
        for (int i = 0; i < nThreads; i++) {
            if (results.get(i) == null)
                fail("Thread run " + i + " didn't complete");
        }
        for (int i = 1; i < nThreads; i++) {
            assertEquals(results.get(i - 1).errorCount(), results.get(i).errorCount());
            assertEquals(results.get(i - 1).failureCount(), results.get(i).failureCount());
        }
    }

    private class RunOnce implements Runnable {
        public void run() {
            TestResult tr = new TestResult();
            try {
                new CompleteTestSuite().run(tr);
            } catch (Throwable e) {
                e.printStackTrace();
                results.add(null);
                return;
            } // the caller will notice...
            results.
            add(tr);
        }
    }
}
