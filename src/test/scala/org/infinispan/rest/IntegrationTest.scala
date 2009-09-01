package org.infinispan.rest


import apache.commons.httpclient.methods._
import apache.commons.httpclient.{Header, HttpClient}
import java.io._
import java.util.Date
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Response.Status
import jboss.resteasy.plugins.server.servlet.{HttpServletDispatcher, ResteasyBootstrap}
import junit.framework.TestCase
import junit.framework.Assert._
import mortbay.jetty.servlet.Context

/**
 * 
 * @author Michael Neale
 */

class IntegrationTest extends TestCase {

  def testBasicOperation = {
    val server = ServerInstance.server

    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PutMethod("http://localhost:8888/rest/mycache/mydata")
    val initialXML = <hey>ho</hey>

    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))
    insert.setRequestHeader("Content-Type", "application/octet-stream")

    client.executeMethod(insert)
    assertNull(insert.getResponseBodyAsString)

    val get = new GetMethod("http://localhost:8888/rest/mycache/mydata")
    client.executeMethod(get)
    val bytes = get.getResponseBody
    assertEquals(bytes.size, initialXML.toString.getBytes.size)
    assertEquals(<hey>ho</hey>.toString, get.getResponseBodyAsString)
    val hdr: Header = get.getResponseHeader("Content-Type")
    assertEquals("application/octet-stream", hdr.getValue)

    val remove = new DeleteMethod("http://localhost:8888/rest/mycache/mydata");
    client.executeMethod(remove)
    client.executeMethod(get)

    assertEquals(HttpServletResponse.SC_NOT_FOUND, get.getStatusCode)

    client.executeMethod(insert)
    client.executeMethod(get)
    assertEquals(<hey>ho</hey>.toString, get.getResponseBodyAsString)

    val removeAll = new DeleteMethod("http://localhost:8888/rest/mycache");
    client.executeMethod(removeAll)

    client.executeMethod(get)
    assertEquals(HttpServletResponse.SC_NOT_FOUND, get.getStatusCode)

    val bout = new ByteArrayOutputStream
    val oo = new ObjectOutputStream(bout)
    oo.writeObject(new CacheEntry("foo", "hey".getBytes))
    oo.flush
    val byteData = bout.toByteArray
    println(byteData.length)

    val insertMore = new PutMethod("http://localhost:8888/rest/mycache/mydata")

    insertMore.setRequestBody(new ByteArrayInputStream(byteData))
    insertMore.setRequestHeader("Content-Type", "application/octet-stream")

    client.executeMethod(insertMore)

    val getMore = new GetMethod("http://localhost:8888/rest/mycache/mydata")
    client.executeMethod(getMore)

    val bytesBack = getMore.getResponseBody
    assertEquals(byteData.length, bytesBack.length)

    val oin = new ObjectInputStream(new ByteArrayInputStream(bytesBack))
    val ce = oin.readObject.asInstanceOf[CacheEntry]
    assertEquals("foo", ce.mediaType)

  }

  def testEmptyGet = {
    assertEquals(
      HttpServletResponse.SC_NOT_FOUND,
      Client.call(new GetMethod("http://localhost:8888/rest/emptycache/nodata")).getStatusCode
      )
  }

  def testGet = {
    val post = new PostMethod("http://localhost:8888/rest/more2/data")
    post.setRequestHeader("Content-Type", "application/text")
    post.setRequestBody("data")
    Client.call(post)

    val get = Client.call(new GetMethod("http://localhost:8888/rest/more2/data"))
    assertEquals(HttpServletResponse.SC_OK, get.getStatusCode)
    assertNotNull(get.getResponseHeader("ETag").getValue)
    assertNotNull(get.getResponseHeader("Last-Modified").getValue)
    assertEquals("application/text", get.getResponseHeader("Content-Type").getValue)
    assertEquals("data", get.getResponseBodyAsString)


  }


  def testHead = {
    val post = new PostMethod("http://localhost:8888/rest/more/data")
    post.setRequestHeader("Content-Type", "application/text")
    post.setRequestBody("data")
    Client.call(post)

    val get = Client.call(new HeadMethod("http://localhost:8888/rest/more/data"))
    assertEquals(HttpServletResponse.SC_OK, get.getStatusCode)
    assertNotNull(get.getResponseHeader("ETag").getValue)
    assertNotNull(get.getResponseHeader("Last-Modified").getValue)
    assertEquals("application/text", get.getResponseHeader("Content-Type").getValue)

    assertNull(get.getResponseBodyAsString)

  }

  def testPost() = {
    val post = new PostMethod("http://localhost:8888/rest/posteee/data")
    post.setRequestHeader("Content-Type", "application/text")
    post.setRequestBody("data")
    Client.call(post)

    //Should get a conflict as its a DUPE post
    assertEquals(HttpServletResponse.SC_CONFLICT, Client.call(post).getStatusCode)

    val put = new PutMethod("http://localhost:8888/rest/posteee/data")
    put.setRequestHeader("Content-Type", "application/text")
    put.setRequestBody("data")

    //Should be all ok as its a put
    assertEquals(HttpServletResponse.SC_OK, Client.call(put).getStatusCode)

  }

  def testPutTimeToLive() = {
    val post = new PostMethod("http://localhost:8888/rest/putttl/data")
    post.setRequestHeader("Content-Type", "application/text")
    post.setRequestHeader("timeToLiveSeconds", "2")
    post.setRequestHeader("maxIdleTimeSeconds", "3")
    post.setRequestBody("data")
    Client.call(post)


  }










}