/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.services.transcript.registry;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import uk.org.sappho.applications.services.transcript.registry.vcs.product.SubversionModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceModule extends AbstractModule {

    private final static Map<String, Class<? extends AbstractServiceModule>> vcsModules = new HashMap<String, Class<? extends AbstractServiceModule>>();
    private final Map<String, String> properties = new HashMap<String, String>();

    static {
        vcsModules.put("svn", SubversionModule.class);
    }

    public ServiceModule(String environment, String application) throws ConfigurationException {

        if (System.getProperty("use.system.properties", "false").equalsIgnoreCase("true")) {
            for (String key : System.getProperties().stringPropertyNames()) {
                setProperty(key, System.getProperty(key));
            }
        }
        setProperty("environment", environment);
        setProperty("application", application);
    }

    public void setProperty(String key, List<String> values) {

        String value = null;
        if (values != null && values.size() > 0) {
            value = values.get(values.size() - 1);
        }
        setProperty(key, value);
    }

    public void setProperty(String key, String value) {

        properties.put(key, value != null ? value : "");
    }

    @Override
    protected void configure() {

        Names.bindProperties(binder(), properties);
    }

    public Injector getInjector() throws ConfigurationException {

        String workingCopyPath = properties.get("working.copy.path");
        if (workingCopyPath == null || workingCopyPath.length() == 0) {
            throw new ConfigurationException("Application property working.copy.path not specified");
        }
        String workingCopyId = properties.get("working.copy.id");
        if (workingCopyId == null || workingCopyId.length() == 0) {
            properties.put("working.copy.id", "default");
        }
        AbstractServiceModule vcsModule;
        String vcs = properties.get("vcs");
        if (vcs != null && vcs.length() != 0) {
            try {
                vcsModule = vcsModules.get(vcs).newInstance();
            } catch (Throwable throwable) {
                throw new ConfigurationException("Specified VCS " + vcs + " is not supported", throwable);
            }
        } else {
            throw new ConfigurationException("No VCS has been specified");
        }
        vcsModule.fixProperties(properties);
        return Guice.createInjector(this, vcsModule);
    }
}