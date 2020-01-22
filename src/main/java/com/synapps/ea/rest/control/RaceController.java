package com.synapps.ea.rest.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import jxl.write.WriteException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.synapps.ea.reporting.dql.DataTypeResolver;
import com.synapps.ea.reporting.dql.ReportTemplate;
import com.synapps.ea.reporting.dql.xml.Query;
import com.synapps.ea.reporting.generate.ReportGenerator;
import com.synapps.ea.reporting.query.RptTemplateListQueryRunner;
import com.synapps.ea.reporting.query.ValueListQueryRunner;
import com.synapps.ea.rest.cache.DocumentumTypeCache;
import com.synapps.ea.rest.model.CreateReportResult;
import com.synapps.ea.rest.model.QueryTemplateBean;
import com.synapps.ea.rest.model.ReportQueryOptions;
import com.synapps.ea.rest.session.SessionProvider;

/**
 * @author Mark Billingham
 *
 */
@RestController
@SessionAttributes({"queryTemplate"})
public class RaceController {
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	Environment env;

	@Autowired
	@Qualifier("userSessionProvider")
	SessionProvider sessionProvider;

	@Autowired
	DocumentumTypeCache typeCache;
	
	/**
	 * @return
	 */
	@RequestMapping("/race/templatelist")
	public @ResponseBody List<QueryTemplateBean> templateList() {
		String queryType = env.getProperty("query.template.type");
		String templateLocation = env.getProperty("template.location");
		String queryStr = "SELECT object_name,title FROM " + queryType + " WHERE folder('" + templateLocation + "') ORDER BY title";
		RptTemplateListQueryRunner queryRunner = new RptTemplateListQueryRunner();
		List<QueryTemplateBean> queries = null;
		try {
			queries = queryRunner.execute(sessionProvider.getDfSession(), queryStr, IDfQuery.DF_READ_QUERY, null);
		} catch (DfException e) {
			logger.error("Can't fetch temlate list!", e);
			throw new RuntimeException(e);
		}
		return queries;
	}

	
	/**
	 * @param model
	 * @param templateSpec
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/race/getreportoptions", consumes="application/json", produces="application/json")
	public @ResponseBody ReportQueryOptions getReportColumns(Model model, 
			@RequestBody QueryTemplateBean templateSpec) throws Exception {
		ReportQueryOptions queryOptions;

		try {
			ReportTemplate template = ReportTemplate.getTemplate(
					env,
					sessionProvider.getDfSession(), 
					templateSpec.getName(),
					env.getProperty("template.location"));
			
			Query queryModel = template.getQueryModel();
			// Save the template as a session param
			model.addAttribute("queryTemplate", template);
			DataTypeResolver resolver = new DataTypeResolver(queryModel);
			resolver.resolveDataTypes(typeCache);
			queryOptions = new ReportQueryOptions(queryModel);
			
		} catch (DfException | JAXBException e) {
			logger.error("Could not get Report template", e);
			throw e;
		}
		
		return queryOptions;
	}

	
	/**
	 * @param model
	 * @param typeAlias
	 * @param attribute
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/race/getvalueassistvalues", produces="application/json")
	public @ResponseBody List<String> getValueAssistValues(Model model, 
			@RequestParam(value = "typeAlias", required = true) String typeAlias, @RequestParam(value = "attribute", required = true) String attribute) throws Exception {
		
		Map<String, Object> modelMap = model.asMap();
		ReportTemplate template = (ReportTemplate) modelMap.get("queryTemplate");
		Query queryTemplate = template.getQueryModel();
		DataTypeResolver resolver = new DataTypeResolver(queryTemplate);
		String type = resolver.findTypeFromAlias(typeAlias);
		String valueQuery = "SELECT DISTINCT " + attribute + " FROM " + type + " ORDER BY " + attribute;
		ValueListQueryRunner vqr = new ValueListQueryRunner(attribute);
		return vqr.execute(sessionProvider.getDfSession(), valueQuery, IDfQuery.DF_READ_QUERY, new ArrayList<String>());
	}

	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/race/createreport", consumes="application/json", produces="application/json")
	public @ResponseBody CreateReportResult generateReport(Model model, @RequestBody ReportQueryOptions request) {
		
		ReportGenerator generator = new ReportGenerator(env);
		CreateReportResult result = new CreateReportResult();
		Map<String, Object> modelMap = model.asMap();
		ReportTemplate template = (ReportTemplate) modelMap.get("queryTemplate");
		
		Query queryModel = template.getQueryModel();
		String templateName = template.getName();
		String reportTitle = template.getTitle();
		// Replace the template columns with the options

		try {
			generator.generate(
					sessionProvider.getDfSession(),
					templateName,
					queryModel,
					request,
					reportTitle,
					result);
			
			result.setSuccess(true);

		} catch (DfException | JAXBException | WriteException | IOException e) {
			result.setObjectId(null);
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			logger.error("Could not generate Report", e);
		}

		return result;
	}
}
