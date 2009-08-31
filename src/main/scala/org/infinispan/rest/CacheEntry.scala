package org.infinispan.rest


import java.io.Serializable
import java.util.Date
import javax.ws.rs.core.EntityTag

/** What is actually stored in the cache */
class CacheEntry(var mediaType: String, var data: Array[Byte]) extends Serializable {
  var lastModified = new Date
  def etag = new EntityTag("INFS" + lastModified.getTime + data.length)
}