package org.agilewiki.utils.weakvalues;

import junit.framework.TestCase;

public class ConcurrentWeakValueMapTest extends TestCase {
    public void test() throws Exception {
        ConcurrentWeakValueMap<String, String> m = new ConcurrentWeakValueMap();
        int i = 0;
        while (((i % 100000) != 0) || m.map.size() == i) {
            m.put("k"+i, "v"+i);
            i++;
            m.poll();
        }
        System.out.println("count: "+i+", size: "+m.map.size());
    }
}
