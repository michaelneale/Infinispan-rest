package org.infinispan.rest


import config.{GlobalConfiguration, Configuration}
import java.io.FileInputStream
import junit.framework.TestCase
import junit.framework.Assert._
import transaction.lookup.DummyTransactionManagerLookup
import manager.{CacheManager, DefaultCacheManager}
/**
 * 
 * @author Michael Neale
 */

class CacheTest extends TestCase {
  def testMichaelsIgnorance = {
    //mostly for exploratory purposes....
    val cm = new DefaultCacheManager("cache-config.xml")
    val cache: Cache[String, Any] = cm.getCache.asInstanceOf[Cache[String, Any]]
    cache.start
    cache.put("Hey", "ho")
    assertEquals("ho", cache.get("Hey"))
    val cache1: Cache[String, Any] = cm.getCache("man").asInstanceOf[Cache[String, Any]]
    cache1.put("Hey", "no")
    cache1.put("Lets", "go")
    assertEquals("no", cache1.get("Hey"))
    assertEquals("ho", cache.get("Hey"))

    assertFalse(cache.containsKey("Lets"))
    assertTrue(cache1.containsKey("Lets"))


    val cache11 = cm.getCache("man")
    assertEquals("no", cache11.get("Hey"))
    assertTrue(cache11.containsKey("Hey"))

    cache11.stop

    val cache111 = cm.getCache("man")
    assertFalse(cache111.containsKey("Hey"))


    val ls = cm.getMembers
    println(ls)


    cm.getCache("man").put("wee", "waa")
    assertEquals("waa", cm.getCache("man").get("wee"))



  }

  def testAnother = {
    val cache: Cache[String, Any] = (new DefaultCacheManager(GlobalConfiguration.getNonClusteredDefault).getCache).asInstanceOf[Cache[String, Any]]
    cache.start
    cache.put("Hey", "ho")
    assertEquals("ho", cache.get("Hey"))

  }
}