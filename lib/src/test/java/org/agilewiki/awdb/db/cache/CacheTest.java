package org.agilewiki.awdb.db.cache;

import junit.framework.TestCase;

public class CacheTest extends TestCase {
    public void test() throws Exception {
        CacheFactory<String, String> cf = new CacheFactory<String, String>(2);
        Cache<String, String> c1 = cf.createCache();
        c1.put("1", "a");
        c1.put("2", "b");
        c1.put("3", "c");
        Cache<String, String> c2 = cf.createCache();
        c2.put("4", "d");
        c2.put("5", "e");
        c2.put("6", "f");
        System.out.println(c2.get("1"));
        System.out.println(c2.get("2"));
        System.out.println(c2.get("3"));
        System.out.println(c2.get("4"));
        System.out.println(c2.get("5"));
        System.out.println(c2.get("6"));
    }
}
