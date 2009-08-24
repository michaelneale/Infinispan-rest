package org.infinispan.rest

import java.io.{Serializable, InputStream}
import javax.servlet.{ServletContextEvent, ServletContextListener}
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response
import manager.{CacheManager, DefaultCacheManager}
@Path("/rest")
class Server {
  @GET
  @Path("/echo/{part1}/{message}")
  def echoService(@PathParam("part1") p1: String, @PathParam("message") message: String) = {
      println(p1 + " " + message)
      Response.status(200).entity(message).build();
  }


  @GET
  @Path("/{cacheName}/{cacheKey}")
  def getItem(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") keyName: String) = {
      val ins: InputStream = null
      new DefaultCacheManager(ins).getCache()
  }



  

}


/**
 * To init the cache manager. Nice to do this on startup as any config problems will be picked up before any
 * requests are attempted to be serviced. Less kitten carnage.
 */
class AgentListener extends ServletContextListener {
  def contextInitialized(ev: ServletContextEvent) = {
    ManagerInstance.instance = new DefaultCacheManager("cache-config.xml")
    ManagerInstance.instance.start
  }
  def contextDestroyed(ev: ServletContextEvent) = {
    ManagerInstance.instance.stop
  }
}

/**
 * Just wrap a single instance of the Infinispan cache manager. 
 */
object ManagerInstance {
   var instance: CacheManager = null
   def getCache(name: String) = {
      instance.getCache(name).asInstanceOf[Cache[String, Any]]
   }
}

class CachedBlob(mimeType: String, data: Array[Byte]) extends Serializable