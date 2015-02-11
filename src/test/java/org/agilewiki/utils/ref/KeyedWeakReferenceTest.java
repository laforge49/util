package org.agilewiki.utils.ref;

import junit.framework.TestCase;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;

public class KeyedWeakReferenceTest extends TestCase {
    private final ReferenceQueue<? super String> q = new ReferenceQueue();
    String s;
    ArrayList<KeyedWeakReference<String, String>> l = new ArrayList<>();

    public void test() throws Exception {
        KeyedWeakReference<String, String> w = null;
        while (w == null) {
            s = "abc" + l.size();
            KeyedWeakReference<String, String> x = new KeyedWeakReference<String, String>("Hi!", s, q);
            l.add(x);
            w = (KeyedWeakReference<String, String>) q.poll();
        }
        System.out.println("Number of weak references created before poll returns non-null: " + l.size());
        assertEquals("Hi!", w.key);
    }
}