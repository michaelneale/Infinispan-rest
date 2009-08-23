package org.infinispan.rest

import java.io.InputStream
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response
import manager.DefaultCacheManager



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