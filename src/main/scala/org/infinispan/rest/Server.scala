package org.infinispan.rest

import java.io.{Serializable, InputStream}
import java.util.concurrent.TimeUnit
import java.util.Date
import javax.servlet.{ServletContextEvent, ServletContextListener}
import javax.ws.rs._
import core.Response.Status
import core.{MediaType, Response}
import manager.{CacheManager, DefaultCacheManager}
@Path("/rest")
class Server {

  @GET
  @Path("/{cacheName}/{cacheKey}")
  def getItem(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
      ManagerInstance.getEntry(cacheName, key) match {
        case b: CacheEntry => {
          Response ok(b.data, b.mediaType) build
        }
        case null => Response noContent() build()
      }
  }


  //TODO: get on a cache name to return a list of entries??
  //TODO: can we iterate over the number of caches under management? 

  @PUT
  @POST            //SPLIT out POST to only create new...
  @Path("/{cacheName}/{cacheKey}")
  def putIt(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String,
            @HeaderParam("Content-Type") mediaType: String, data: Array[Byte],
            @HeaderParam("timeToLiveSeconds") ttl: Long,
            @HeaderParam("maxIdleTimeSeconds") idleTime: Long) = {
            (ttl, idleTime) match {
              case (0, 0) => ManagerInstance.getCache(cacheName).put(key, new CacheEntry(mediaType, data))
              case (x, 0) => ManagerInstance.getCache(cacheName).put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS)
              case (x, y) => ManagerInstance.getCache(cacheName).put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS)
            }

  }

  @DELETE
  @Path("/{cacheName}/{cacheKey}")
  def remove(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
    ManagerInstance.getCache(cacheName).remove(key)
  }

  @DELETE
  @Path("/{cacheName}")
  def killCache(@PathParam("cacheName") cacheName: String) = {
    ManagerInstance.getCache(cacheName).stop
  }




  

}


/**
 * To init the cache manager. Nice to do this on startup as any config problems will be picked up before any
 * requests are attempted to be serviced. Less kitten carnage.
 */
class StartupListener extends ServletContextListener {
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
      instance.getCache(name).asInstanceOf[Cache[String, CacheEntry]]
   }
   def getEntry(cacheName: String, key: String) : CacheEntry = {
     getCache(cacheName).get(key)
   }
}

/** What is actually stored in the cache */
class CacheEntry(var mediaType: String, var data: Array[Byte]) extends Serializable {
  //need to put date last modified here, and ETag calc...  

}