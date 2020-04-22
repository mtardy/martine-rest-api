import api.Ping;
import api.RecetteAPI;
import api.UtilisateursAPI;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Swagger;
import io.swagger.models.auth.BasicAuthDefinition;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/v1")
public class App extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public App(){
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("/api/v1");
        beanConfig.setTitle("Les recettes de Martine API");
        beanConfig.setDescription("Bienvenue sur l'API REST des recettes de Martine!");
        beanConfig.setScan(true);
        //beanConfig.setSchemes(new String[]{"HTTPS", "HTTP"});

        Swagger swagger = new Swagger();
        swagger.securityDefinition("basicAuth", new BasicAuthDefinition());
        new SwaggerContextService().updateSwagger(swagger);

        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
        singletons.add(corsFilter);
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet();

        resources.add(Ping.class);
        resources.add(RecetteAPI.class);
        resources.add(UtilisateursAPI.class);

        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        return resources;
    }
}