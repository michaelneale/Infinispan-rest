package org.infinispan.rest

import java.util.concurrent.TimeUnit
import javax.ws.rs._
import core.Response.{ResponseBuilder, Status}
import core.{Context, Request, Response}
import manager.{CacheManager, DefaultCacheManager}

@Path("/rest")
class Server(@Context request: Request, @HeaderParam("performAsync") useAsync: Boolean) {

  @GET
  @Path("/{cacheName}/{cacheKey}")
  def getEntry(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
      ManagerInstance.getEntry(cacheName, key) match {
        case b: CacheEntry => {
          request.evaluatePreconditions(b.lastModified, b.etag) match {
            case bldr: ResponseBuilder => bldr.build
            case null => Response.ok(b.data, b.mediaType).lastModified(b.lastModified).tag(b.etag).build
          }
        }
        case null => Response status(Status.NOT_FOUND) build
      }
  }

  @HEAD
  @Path("/{cacheName}/{cacheKey}")
  def headEntry(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
      ManagerInstance.getEntry(cacheName, key) match {
        case b: CacheEntry => {
          request.evaluatePreconditions(b.lastModified, b.etag) match {
            case bldr: ResponseBuilder => bldr.build
            case null => Response.ok.`type`(b.mediaType).lastModified(b.lastModified).tag(b.etag).build
          }
        }
        case null => Response status(Status.NOT_FOUND) build
      }
  }


  @PUT
  @POST
  @Path("/{cacheName}/{cacheKey}")
  def putEntry(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String,
            @HeaderParam("Content-Type") mediaType: String, data: Array[Byte],
            @HeaderParam("timeToLiveSeconds") ttl: Long,
            @HeaderParam("maxIdleTimeSeconds") idleTime: Long) = {
            val cache = ManagerInstance.getCache(cacheName)
            if (request.getMethod == "POST" && cache.containsKey(key)) {
                Response.status(Status.CONFLICT).build()
            } else {
              (ttl, idleTime, useAsync) match {
                case (0, 0, false) => cache.put(key, new CacheEntry(mediaType, data))
                case (x, 0, false) => cache.put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS)
                case (x, y, false) => cache.put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS)

                case (0, 0, true) => cache.put(key, new CacheEntry(mediaType, data))
                case (x, 0, true) => cache.put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS)
                case (x, y, true) => cache.put(key, new CacheEntry(mediaType, data), ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS)

              }
            }
  }

  private def put(cache: Cache[String, CacheEntry], key: String, entry: CacheEntry) = {
    if (useAsync) {
      cache.putAsync(key, entry)
    } else {
      cache.put(key, entry) 
    }
  }




  @DELETE
  @Path("/{cacheName}/{cacheKey}")
  def removeEntry(@PathParam("cacheName") cacheName: String, @PathParam("cacheKey") key: String) = {
    if (useAsync) {
      ManagerInstance.getCache(cacheName).removeAsync(key)      
    } else {
      ManagerInstance.getCache(cacheName).remove(key)
    }
  }

  @DELETE
  @Path("/{cacheName}")
  def killCache(@PathParam("cacheName") cacheName: String) = {
    ManagerInstance.getCache(cacheName).stop
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

