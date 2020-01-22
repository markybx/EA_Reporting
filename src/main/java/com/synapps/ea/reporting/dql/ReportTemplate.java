package com.synapps.ea.reporting.dql;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.core.env.Environment;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.synapps.ea.reporting.dql.xml.Query;

/**
 * @author Mark Billingham
 *
 */
public class ReportTemplate implements Serializable {
	private static final long serialVersionUID = -1823407078349226688L;
	private Environment env;
	private MessageFormat reportTemplateQualifier;
	private IDfId objectId;
	private String name = null;
	private String title = null;
	private Query queryModel = null;
	
	private ReportTemplate(Environment env) {
		this.env = env;
		String queryType = env.getProperty("query.template.type");
		reportTemplateQualifier = 
				new MessageFormat(queryType + " WHERE object_name=''{0}'' and folder(''{1}'')");
	}
	
	/**
	 * @param env
	 * @param dfSession
	 * @param templateName
	 * @param templateLocation
	 * @return
	 * @throws DfException
	 * @throws JAXBException
	 */
	public static ReportTemplate getTemplate(Environment env, IDfSession dfSession, String templateName, String templateLocation) throws DfException, JAXBException {
		ReportTemplate template = new ReportTemplate(env);
		template.fetchContent(dfSession, templateName, templateLocation);
		return template;
	}
	
	private void fetchContent(IDfSession dfSession, String templateName,
			String templateLocation) throws DfException, JAXBException {
		String templateQuery = reportTemplateQualifier.format(new String[] {templateName, templateLocation});
		IDfSysObject templateObj = (IDfSysObject) dfSession.getObjectByQualification(templateQuery);
		if (null != templateObj) {
			this.objectId = templateObj.getObjectId();
			this.name = templateObj.getObjectName();
			this.title = templateObj.getTitle();
			ByteArrayInputStream bais = templateObj.getContent();
			JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			queryModel = (Query) unMarshaller.unmarshal(bais);
			if (null == queryModel.getDescription() || queryModel.getDescription().isEmpty()) {
				queryModel.setDescription(templateObj.getTitle());
			}
		}
		
	}

	/**
	 * @return
	 */
	public Query getQueryModel() {
		if (null == queryModel) {
			queryModel = new Query();
		}
		return queryModel;
	}

	/**
	 * @return
	 */
	public IDfId getObjectId() {
		return objectId;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

}
