package org.infinispan.rest


import apache.commons.httpclient.HttpClient
import apache.commons.httpclient.methods.{DeleteMethod, PutMethod, GetMethod, PostMethod}
import java.io.ByteArrayInputStream
import java.util.Date
import jboss.resteasy.plugins.server.servlet.{HttpServletDispatcher, ResteasyBootstrap}
import junit.framework.TestCase
import junit.framework.Assert._
import mortbay.jetty.servlet.Context

/**
 * 
 * @author Michael Neale
 */

class IntegrationTest extends TestCase {
  def testPutGet = {
    val server = startServer

    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PutMethod("http://localhost:8888/rest/mycache/mydata")
    val initialXML = <hey>ho</hey>

    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))
    insert.setRequestHeader("Content-Type", "application/octet-stream")

    client.executeMethod(insert)

    val response = insert.getResponseBodyAsString


    val get = new GetMethod("http://localhost:8888/rest/mycache/mydata")
    client.executeMethod(get)
    assertEquals(<hey>ho</hey>.toString, get.getResponseBodyAsString)



    val remove = new DeleteMethod("http://localhost:8888/rest/mycache/mydata");
    client.executeMethod(remove)


    client.executeMethod(get)
    assertNull(get.getResponseBodyAsString)

    client.executeMethod(insert)
    client.executeMethod(get)
    assertEquals(<hey>ho</hey>.toString, get.getResponseBodyAsString)

    val removeAll = new DeleteMethod("http://localhost:8888/rest/mycache");
    client.executeMethod(removeAll)

    client.executeMethod(get)
    assertNull(get.getResponseBodyAsString)
    
    



  }


  def startServer = {
    val server = new org.mortbay.jetty.Server(8888);
    val ctx = new Context(server, "/", Context.SESSIONS)
    ctx.setInitParams(params)
    ctx.addEventListener(new ResteasyBootstrap)
    ctx.addEventListener(new StartupListener)
    ctx.addServlet(classOf[HttpServletDispatcher], "/*")
    server.setStopAtShutdown(true)
    server.start
    server
  }

  def params = {
    val hm = new java.util.HashMap[String, String]
    hm.put("resteasy.resources", "org.infinispan.rest.Server")
    hm
  }


}