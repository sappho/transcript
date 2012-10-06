/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.restful.transcript.registry;

import uk.org.sappho.applications.restful.transcript.jersey.AbstractRestService;
import uk.org.sappho.applications.services.transcript.registry.ConfigurationException;
import uk.org.sappho.applications.services.transcript.registry.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/{environment}/{application}/{key}")
public class PropertyRestService extends AbstractRestService {

    @PathParam("environment")
    private String environment;
    @PathParam("application")
    private String application;
    @PathParam("key")
    private String key;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getProperty() throws ConfigurationException {

        return getService().getInstance(Properties.class).get(environment, application, key, true);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setProperty(String value) throws ConfigurationException {

        getService().getInstance(Properties.class).put(environment, application, key, value);
    }

    @DELETE
    public void deleteProperty() throws ConfigurationException {

        getService().getInstance(Properties.class).delete(environment, application, key);
    }
}
