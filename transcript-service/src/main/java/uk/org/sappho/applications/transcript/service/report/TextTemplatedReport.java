/**
 *** This software is licensed under the GNU General Public License, version 3.
 *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
 *** Copyright 2012 Andrew Heald.
 */

package uk.org.sappho.applications.transcript.service.report;

import com.google.inject.Inject;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import uk.org.sappho.applications.transcript.service.TranscriptException;
import uk.org.sappho.applications.transcript.service.registry.WorkingCopy;
import uk.org.sappho.applications.transcript.service.report.freemarker.PropertyTemplateLoader;

import java.io.StringWriter;

public class TextTemplatedReport {

    private final WorkingCopy workingCopy;
    private final ReportData reportData;
    private final PropertyTemplateLoader propertyTemplateLoader;

    @Inject
    public TextTemplatedReport(WorkingCopy workingCopy, ReportData reportData,
                               PropertyTemplateLoader propertyTemplateLoader) {

        this.workingCopy = workingCopy;
        this.reportData = reportData;
        this.propertyTemplateLoader = propertyTemplateLoader;
    }

    public String generate(String templateName, TemplateLoader sourceTemplateLoader)
            throws TranscriptException {

        try {
            workingCopy.getUpToDatePath("");
            propertyTemplateLoader.setSourceTemplateLoader(sourceTemplateLoader);
            Configuration freemarkerConfiguration = new Configuration();
            freemarkerConfiguration.setTemplateLoader(propertyTemplateLoader);
            freemarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());
            Template template = freemarkerConfiguration.getTemplate(templateName + ".ftl");
            StringWriter stringWriter = new StringWriter();
            template.process(reportData, stringWriter);
            stringWriter.close();
            return stringWriter.toString();
        } catch (Throwable throwable) {
            throw new TranscriptException("Unable to generate HTML report", throwable);
        }
    }
}
