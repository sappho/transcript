/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.transcript.service.report;

import com.google.gson.Gson;
import com.google.inject.Inject;
import uk.org.sappho.applications.transcript.service.TranscriptParameters;
import uk.org.sappho.applications.transcript.service.registry.Applications;
import uk.org.sappho.applications.transcript.service.registry.Environments;
import uk.org.sappho.applications.transcript.service.registry.Properties;
import uk.org.sappho.applications.transcript.service.report.freemarker.EmbeddedFreemarker;
import uk.org.sappho.applications.transcript.service.report.restful.client.RestfulClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class ReportData extends BasicReportData {

    private final Environments environments;
    private final Applications applications;
    private final Properties properties;
    private final TranscriptParameters transcriptParameters;
    private final DataDictionary dataDictionary;
    private final RestfulClient restfulClient;
    private final Gson gson;
    private final EmbeddedFreemarker freemarker;
    private Map<String, Map<String, Map<String, Object>>> environmentCache;

    @Inject
    public ReportData(Environments environments,
                      Applications applications,
                      Properties properties,
                      TranscriptParameters transcriptParameters,
                      DataDictionary dataDictionary,
                      RestfulClient restfulClient,
                      Gson gson,
                      EmbeddedFreemarker freemarker) {

        super(new HashMap<String, Object>());
        this.environments = environments;
        this.applications = applications;
        this.properties = properties;
        this.transcriptParameters = transcriptParameters;
        this.dataDictionary = dataDictionary;
        this.restfulClient = restfulClient;
        this.gson = gson;
        this.freemarker = freemarker;
        resetPropertyCache();
    }

    public TranscriptParameters getParameters() {

        return transcriptParameters;
    }

    public Map<String, Object> getDictionary() {

        Map<String, Object> dictionary = new TreeMap<String, Object>();
        try {
            dictionary = dataDictionary.getDictionary();
        } catch (Throwable throwable) {
        }
        return dictionary;
    }

    public String[] getEnvironments(String regex) {

        String[] environmentList = new String[0];
        try {
            environmentList = regex != null && regex.length() != 0 ?
                    environments.getEnvironmentNames(Pattern.compile(regex)) : environments.getEnvironmentNames();
        } catch (Throwable throwable) {
        }
        return environmentList;
    }

    public String[] getEnvironmentsWithApplications(String[] environmentList, String[] applicationList,
                                                    String keyRegex, boolean includeVersionControlProperties) {

        Pattern keyPattern = keyPattern(keyRegex);
        List<String> environments = new LinkedList<String>();
        for (String environment : environmentList) {
            for (String application : applicationList) {
                boolean found = false;
                for (String key : getProperties(environment, application, includeVersionControlProperties).keySet()) {
                    if (keyPattern.matcher(key).matches()) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    environments.add(environment);
                    break;
                }
            }
        }
        return environments.toArray(new String[environments.size()]);
    }

    public String[] getApplications(String[] environmentList, String regex) {

        SortedMap<String, String> applicationMap = new TreeMap<String, String>();
        try {
            for (String environment : environmentList) {
                String[] applicationList = regex != null && regex.length() != 0 ?
                        applications.getApplicationNames(environment, Pattern.compile(regex)) :
                        applications.getApplicationNames(environment);
                for (String application : applicationList) {
                    applicationMap.put(application, application);
                }
            }
        } catch (Throwable throwable) {
        }
        return applicationMap.keySet().toArray(new String[applicationMap.size()]);
    }

    public String[] getApplicationsWithEnvironments(String[] environmentList, String[] applicationList,
                                                    String keyRegex, boolean includeVersionControlProperties) {

        Pattern keyPattern = keyPattern(keyRegex);
        List<String> applications = new LinkedList<String>();
        for (String application : applicationList) {
            for (String environment : environmentList) {
                boolean found = false;
                for (String key : getProperties(environment, application, includeVersionControlProperties).keySet()) {
                    if (keyPattern.matcher(key).matches()) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    applications.add(application);
                    break;
                }
            }
        }
        return applications.toArray(new String[applications.size()]);
    }

    public String[] getKeys(String[] environments, String[] applications, String keyRegex,
                            boolean includeVersionControlProperties) {

        Pattern keyPattern = keyPattern(keyRegex);
        SortedMap<String, String> keyMap = new TreeMap<String, String>();
        for (String environment : environments) {
            for (String application : applications) {
                Map<String, Object> properties =
                        getProperties(environment, application, includeVersionControlProperties);
                for (String key : properties.keySet()) {
                    if (keyPattern.matcher(key).matches()) {
                        keyMap.put(key, key);
                    }
                }
            }
        }
        return keyMap.keySet().toArray(new String[keyMap.size()]);
    }

    private Pattern keyPattern(String keyRegex) {

        return Pattern.compile(keyRegex != null && keyRegex.length() != 0 ? keyRegex : ".*");
    }

    public Map<String, Object> getProperties(String environment, String application,
                                             boolean includeVersionControlProperties) {

        Map<String, Map<String, Object>> applicationCache = environmentCache.get(environment);
        if (applicationCache == null) {
            applicationCache = new TreeMap<String, Map<String, Object>>();
            environmentCache.put(environment, applicationCache);
        }
        Map<String, Object> propertyCache = applicationCache.get(application);
        if (propertyCache == null) {
            try {
                propertyCache = properties.getAllProperties(environment, application, includeVersionControlProperties);
            } catch (Throwable throwable) {
            }
            if (propertyCache == null) {
                propertyCache = new TreeMap<String, Object>();
            }
            applicationCache.put(application, propertyCache);
        }
        return propertyCache;
    }

    public void resetPropertyCache() {

        environmentCache = new TreeMap<String, Map<String, Map<String, Object>>>();
    }

    public SortedMap<String, String> getAssociations(String[] list, String type) {

        SortedMap<String, String> associations = new TreeMap<String, String>();
        for (String rawName : list) {
            associations.put(getPrettyName(rawName, type), rawName);
        }
        return associations;
    }

    public String getPrettyName(String rawName, String type) {

        String name = rawName;
        try {
            name = (String) ((Map<String, Map<String, Object>>) getDictionary().get(type)).get(rawName).get("name");
        } catch (Throwable throwable) {
        }
        return name;
    }

    public RestfulClient getRestfulClient() {

        return restfulClient;
    }

    public Object fromJson(String json) {

        return gson.fromJson(json, Object.class);
    }

    public Object fromJson(byte[] json) {

        return gson.fromJson(Arrays.toString(json), Object.class);
    }

    public String toJson(Object object) {

        return gson.toJson(object);
    }

    public EmbeddedFreemarker getFreemarker() {

        return freemarker;
    }
}
