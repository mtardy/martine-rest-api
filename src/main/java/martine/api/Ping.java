package martine.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Singleton
@Path("/ping")
@Api(value = "Ping")
public class Ping {

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Vérifier l'état du serveur API")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'opération")
    })
    public Response ping() {
        return Response.ok().entity("pong!").build();
    }
}
