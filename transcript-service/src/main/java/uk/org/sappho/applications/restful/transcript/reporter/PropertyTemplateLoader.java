/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.restful.transcript.reporter;

import com.google.inject.Inject;
import freemarker.cache.TemplateLoader;
import uk.org.sappho.applications.services.transcript.registry.Properties;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PropertyTemplateLoader implements TemplateLoader {

    private Properties properties;
    private String template = null;

    @Inject
    public PropertyTemplateLoader(Properties properties) {

        this.properties = properties;
    }

    public boolean loadTemplate(String name) {

        try {
            template = properties.get(".devops", ".templates", name, false);
        } catch (Throwable throwable) {
        }
        return template != null;
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {

        return template;
    }

    @Override
    public long getLastModified(Object templateSource) {

        return System.currentTimeMillis();
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {

        return new StringReader(template);
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }
}
