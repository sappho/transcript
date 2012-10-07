/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.restful.transcript.registry;

import uk.org.sappho.applications.restful.transcript.jersey.AbstractRestService;
import uk.org.sappho.applications.services.transcript.registry.Applications;
import uk.org.sappho.applications.services.transcript.registry.ConfigurationException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/{environment}")
public class ApplicationNamesRestService extends AbstractRestService {

    @PathParam("environment")
    private String environment;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getApplications() throws ConfigurationException {

        return getService().getInstance(Applications.class).getApplicationNames(environment);
    }
}