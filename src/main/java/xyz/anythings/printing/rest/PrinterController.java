package xyz.anythings.printing.rest;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.printing.PrintingConstants;
import xyz.anythings.printing.entity.PrintTemplate;
import xyz.anythings.printing.entity.Printer;
import xyz.anythings.sys.AnyConstants;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.engine.ITemplateEngine;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/printer")
@ServiceDesc(description = "Printer Service API")
public class PrinterController extends AbstractRestService {

	@Autowired
	@Qualifier("basic")
    private ITemplateEngine templateEngine;
	
	@Override
	protected Class<?> entityClass() {
		return Printer.class;
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
	public Printer findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Printer> checkImport(@RequestBody List<Printer> list) {
		for (Printer item : list) {
			this.checkForImport(Printer.class, item);
		}

		return list;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Printer create(@RequestBody Printer input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Printer update(@PathVariable("id") String id, @RequestBody Printer input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.getClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Printer> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/print_label", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Print label")
	public Map<String, Object> printLabel(@RequestBody Map<String, Object> printOptions) {
		
		// 인쇄 옵션 정보 추출
		String printerCd = ValueUtil.toString(printOptions.get("printer_cd"));
		String templateName = (String)printOptions.get("label_template");
		Map<String,Object> serviceParam = (Map<String,Object>)printOptions.get("service_param");
		
		Long domainId = Domain.currentDomainId();
		
		Printer printer = Printer.findByPrinterCd(domainId, printerCd, true);
		PrintTemplate template = PrintTemplate.findByName(domainId, templateName, true);
		
		// 인쇄 요청
		this.printLabelByLabelTemplate(printer, template, serviceParam);
		
		// 리턴
		return ValueUtil.newMap("result", AnyConstants.OK_STRING);
	} 
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/print_labels", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Print labels")
	public Map<String, Object> printLabels(@RequestBody Map<String, Object> printOptions) {
		
		// 인쇄 옵션 정보 추출
		String printerCd = ValueUtil.toString(printOptions.get("printer_cd"));
		String templateName = (String)printOptions.get("label_template");
		List<Map<String,Object>> service_params = (List<Map<String,Object>>)printOptions.get("service_params");
		
		Long domainId = Domain.currentDomainId();
		
		Printer printer = Printer.findByPrinterCd(domainId, printerCd, true);
		PrintTemplate template = PrintTemplate.findByName(domainId, templateName, true);
		
		for(Map<String,Object> serviceParam : service_params) {
			// 인쇄 요청
			this.printLabelByLabelTemplate(printer, template, serviceParam);
		}
		
		
		// 리턴
		return ValueUtil.newMap("result", AnyConstants.OK_STRING);
	} 
	
	/**
	 * 라벨 템플릿으로 라벨 인쇄 
	 * @param printer
	 * @param template
	 * @param service_params
	 * @return
	 */
	private void printLabelByLabelTemplate(Printer printer, PrintTemplate template, Map<String,Object> service_params) {
		
		Map<String, Object> variables = template.getServiceLogicData(service_params);
		
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template.getTemplate(), writer, variables, null);
		this.printLabelByLabelCommand(printer.getPrinterAgentUrl(), printer.getPrinterNm(), writer.toString());
	}
	
	
	/**
	 * 라벨 command으로 라벨 인쇄, 라벨코맨드를 직접 제공하는 고객을 위한 API
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param command
	 */
	private void printLabelByLabelCommand(String printAgentUrl, String printerName, String command) {
		if(ValueUtil.isNotEmpty(printerName)) {		
			RestTemplate rest = new RestTemplate();
			rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8)));
			printAgentUrl = printAgentUrl + PrintingConstants.BARCODE_REST_URL + printerName;
			rest.postForEntity(printAgentUrl, command, Boolean.class);
		}
	}
}