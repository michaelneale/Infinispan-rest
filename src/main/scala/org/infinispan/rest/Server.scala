package org.infinispan.rest

import java.io.{Serializable, InputStream}
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
        case b: CachedBlob => {
          Response ok(b.data, b.mediaType) build
        }
        case null => Response noContent() build()
      }
  }


  @PUT
  @POST
  @Path("/{cacheName}/{cacheKey}")
  def putIt(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String,
            @HeaderParam("Content-Type") mediaType: String, data: Array[Byte]) = {
        ManagerInstance.getCache(cacheName).put(key, new CachedBlob(mediaType, data))
  }

  @DELETE
  @Path("/{cacheName}/{cacheKey}")
  def remove(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
    ManagerInstance.getCache(cacheName).remove(key)
  }

  @DELETE
  @Path("/{cacheName}/{cacheKey}")
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
      instance.getCache(name).asInstanceOf[Cache[String, CachedBlob]]
   }
   def getEntry(cacheName: String, key: String) : CachedBlob = {
     getCache(cacheName).get(key)
   }
}

class CachedBlob(val mediaType: String, val data: Array[Byte]) extends Serializable