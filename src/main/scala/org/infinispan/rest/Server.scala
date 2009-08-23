
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response



@Path("/echo")
class Server {
  @GET
  @Path("/{message}")
  def echoService(@PathParam("message") message: String) = {
      Response.status(200).entity(message).build();
  }
}