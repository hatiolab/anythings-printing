package xyz.anythings.base.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import xyz.anythings.base.entity.Printer;
import xyz.anythings.base.entity.Printout;
import xyz.anythings.printing.PrintingConstants;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dev.entity.DiyTemplate;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/printouts")
@ServiceDesc(description = "Printout Service API")
public class PrintoutController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Printout.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Printout findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Printout create(@RequestBody Printout input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Printout update(@PathVariable("id") String id, @RequestBody Printout input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Printout> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/print_pdf/by_template/{template_name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Printing Report PDF By Print Template Name")
	public Map<String, Object> printReportTemplate(
			@PathVariable("template_name") String templateName,
			@RequestBody Map<String, Object> params,
			@RequestParam(name = "printer_id", required = false) String printerId) {
		
		Long domainId = Domain.currentDomainId();
		Printer printer = null;
		
		// 1. printerId 파라미터로 프린트 조회
		if(printerId != null) {
			printer = Printer.findByIdOrName(domainId, printerId, false);
		}
		
		if(printer == null || ValueUtil.isNotEqual(PrintingConstants.PRINTER_TYPE_NORMAL, printer.getPrinterType())) {
			printer = Printer.findDefaultNormalPrinter(domainId);
		}
		
		// 2. Print Template 이름으로 Print Template을 조회한 후  
		DiyTemplate template = AnyEntityUtil.findEntityByCode(domainId, true, DiyTemplate.class, "name", templateName);
		String reportContent = template.getTemplate();
		String reportLogic = template.getLogic();
		
		if(ValueUtil.isEmpty(reportContent)) {
			throw new ElidomValidationException("리포트 템플릿의 내용이 없습니다.");
		}
		
		if(ValueUtil.isEmpty(reportLogic)) {
			throw new ElidomValidationException("리포트 템플릿의 서비스가 등록 되지 않았습니다.");
		}
		
		// 3. 조회한 Print Template으로 로직 실행
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		Map<String, Object> result = (Map<String, Object>)scriptEngine.runScript("groovy", reportLogic, ValueUtil.newMap("domain_id,params", template.getDomainId(), params));		
		Map<String, Object> header = (Map<String, Object>) result.get("header");
		List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
		this.printReport(domainId, templateName, reportContent, header, items, printer);
		return ValueUtil.newMap("success,result", true, AnyConstants.OK_STRING);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/show_pdf/by_template/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Show PDF by Print Template")
	public void showPdfByPrintTemplate(
			HttpServletRequest req, 
			HttpServletResponse res,
			@PathVariable("id") String id,
			@RequestBody Map<String, Object> params) {
		
		// 1. ID로 객체 찾기 
		Printout report = this.findOne(id);
		
		// 2. 리포트 정보로 부터 커스텀 템플릿 조회  
		String templateName = report.getTemplateCd();
		if(ValueUtil.isEmpty(templateName)) {
			throw new ElidomValidationException("리포트 템플릿을 찾을 수 없습니다.");
		}
		
		// 3. 프린트 템플릿으로 부터 리포트 템플릿, 서비스 로직 조회
		DiyTemplate template = AnyEntityUtil.findEntityByCode(report.getDomainId(), true, DiyTemplate.class, "name", templateName);
		String reportContent = template.getTemplate();
		String reportLogic = template.getLogic();
		
		if(ValueUtil.isEmpty(reportContent)) {
			throw new ElidomValidationException("리포트 템플릿의 내용이 없습니다.");
		}
		
		if(ValueUtil.isEmpty(reportLogic)) {
			throw new ElidomValidationException("리포트 템플릿의 서비스가 등록되지 않았습니다.");
		}
		
		// 4. 리포트에 데이터 매핑 및 다운로드
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		Map<String, Object> result = (Map<String, Object>)scriptEngine.runScript("groovy", reportLogic, ValueUtil.newMap("domain_id,params", template.getDomainId(), params));		
		Map<String, Object> header = (Map<String, Object>) result.get("header");
		List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
		this.downloadReportByJRXml(res, report.getReportCd(), reportContent, header, items);
	}
	
	/**
	 * 리포트 다운로드 
	 * 
	 * @param res
	 * @param reportCd
	 * @param reportSrcPath
	 * @param reportHeader
	 * @param reportDataList
	 */
	public void downloadReportByJRXml(
			HttpServletResponse res, 
			String reportCd,
			String jasperXml, 
			Map<String, Object> reportHeader, 
			List<Map<String, Object>> reportDataList) {
		
		// 1. response에 헤더 설정 
		res.setCharacterEncoding(SysConstants.CHAR_SET_UTF8);
		res.setContentType("text/plain;charset=" + SysConstants.CHAR_SET_UTF8);
		res.addHeader("Content-Type", "application/pdf");
		res.setHeader("Content-Disposition", "attachment; filename=" + reportCd + ".pdf");
		res.addHeader("Content-Transfer-Encoding", "binary;");
		res.addHeader("Pragma", "no-cache;");
		res.addHeader("Expires", "-1;");	
		
		try {
			// 2. JasperReport 정보를 로딩
			JasperReport jasperReport = this.loadReportByJRxml(jasperXml);
			
			// 3. JasperReport에 데이터 바인딩하기 위한 리포트 데이터소스를 빌드 
			JsonDataSource jds = this.buildReportDataSource(reportDataList);

			// 4. JapserReport와 데이터소스로 리포트를 response의 output stream에 write
			this.writeReportToStream(jasperReport, reportHeader, jds, res.getOutputStream());
			
		} catch(ElidomException ee) {
			throw ee;

		} catch(JRException jre) {
			throw ThrowUtil.newFailToProcessTemplate(reportCd + " Report", jre);

		} catch(Exception e) {
			throw ThrowUtil.newFailToProcessTemplate(reportCd + " Report", e);
		}
	}
	
	/**
	 * Jasper JRXML 내용으로 부터 리포트 로딩
	 *  
	 * @param jrxmlContent
	 * @return
	 * @throws
	 */
	private JasperReport loadReportByJRxml(String jrxmlContent) throws Exception {
		InputStream is = new ByteArrayInputStream(jrxmlContent.getBytes());
		JasperDesign jasperDesign = JRXmlLoader.load(is);
		return JasperCompileManager.compileReport(jasperDesign);
	}
	
	/**
	 * 리포트 생성을 위한 데이터 소스를 생성한다.
	 * 
	 * @param reportDataList
	 * @return
	 * @throws
	 */
	private JsonDataSource buildReportDataSource(List<Map<String, Object>> reportDataList) {
		String jsonData = FormatUtil.toUnderScoreJsonString(reportDataList);
		InputStream stream = null;
		
		try {
			stream = new ByteArrayInputStream(jsonData.getBytes(SysConstants.CHAR_SET_UTF8));
			
		} catch (UnsupportedEncodingException e1) {
			throw ThrowUtil.newFailToParseCode("JSON ", e1);

		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
				}
		}

		try {
			return new JsonDataSource(stream);
		} catch (JRException e2) {
			throw ThrowUtil.newFailToParseCode("JSON", e2);
		}
	}
	
	/**
	 * 리포트 데이터를 스트림에 write
	 * 
	 * @param jasperReport
	 * @param reportHeader
	 * @param jds
	 * @param os
	 * @throws
	 */
	private void writeReportToStream(JasperReport jasperReport, Map<String, Object> reportHeader, JRDataSource jds, OutputStream os) throws Exception {
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportHeader, jds);
		
		if (jasperPrint != null) {
			JasperExportManager.exportReportToPdfStream(jasperPrint, os);
		}
	}
	
	/**
	 * 리포트 인쇄 
	 * 
	 * @param domainId
	 * @param reportCd
	 * @param jrxmlContent
	 * @param reportHeader
	 * @param reportDataList
	 * @param printer
	 */
	public void printReport(Long domainId, String reportCd, String jrxmlContent, Map<String, Object> reportHeader, List<Map<String, Object>> reportDataList, Printer printer) {
		try {
			// 1. JasperReport 정보를 로딩 
			JasperReport jasperReport = this.loadReportByJRxml(jrxmlContent);
			
			// 2. JasperReport에 데이터 바인딩하기 위한 리포트 데이터소스를 빌드 
			JsonDataSource jds = this.buildReportDataSource(reportDataList);

			// 3. JapserReport와 데이터소스로 리포트를 response의 output stream에 write
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportHeader, jds);
			
			// 4. 인쇄 내용을 스트림에 실어서 PrintAgent에 인쇄 요청  
			if (jasperPrint != null) {
				// 4.1 스트림 리소스 생성
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
			    Resource resource = new ByteArrayResource(baos.toByteArray());
				
			    // 4.2 octet stream으로 인쇄 요청
			    String printUrl = printer.getPrinterAgentUrl() + "/pdf?printer=" + printer.getPrinterDriver();
				URI uri = new URI(printUrl);
				RequestEntity<Resource> requestEntity = RequestEntity
				        .post(uri)
				        .contentType(MediaType.APPLICATION_OCTET_STREAM)
				        .body(resource);
				new RestTemplate().postForEntity(uri, requestEntity, Boolean.class);
			}
			
		} catch(ElidomException ee) {
			throw ee;

		} catch(JRException jre) {
			throw ThrowUtil.newFailToProcessTemplate(reportCd + " Report", jre);

		} catch(Exception e) {
			throw new ElidomServiceException(e);
		}
	}
	
	/**
	 * 리포트 일반 프린터로 인쇄
	 * 
	 * @param domainId
	 * @param reportCd
	 * @param reportSrcPath
	 * @param reportHeader
	 * @param reportDataList
	 * @param printerId
	 */
	public void printReport(Long domainId, String reportCd, String reportSrcPath, Map<String, Object> reportHeader, List<Map<String, Object>> reportDataList, String printerId) {
		Printer printer = Printer.findByIdOrName(domainId, printerId, true);
		this.printReport(domainId, reportCd, reportSrcPath, reportHeader, reportDataList, printer);
	}

}