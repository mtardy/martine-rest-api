import api.Ping;
import api.RecetteAPI;
import io.swagger.jaxrs.config.BeanConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class App extends Application {

    public App(){
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/backend/api");
        beanConfig.setTitle("Les recettes de Martine API");
        beanConfig.setDescription("Bienvenue sur l'API REST des recettes de Martine!");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet();

        resources.add(Ping.class);
        resources.add(RecetteAPI.class);

        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        return resources;
    }
}