package xyz.anythings.printing.service;

import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.base.entity.Printer;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.dev.entity.DiyTemplate;
import xyz.elidom.dev.rest.DiyTemplateController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

/**
 * 라벨 프린트 서비스
 * 
 * @author shortstop
 */
@Component
public class LabelPrintService {

	/**
	 * DiyTemplate Controller
	 */
	@Autowired
	private DiyTemplateController templateCtrl;
	
	/**
	 * 바코드 라벨 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.isSyncMode == true")
	public void printLabelSyncMode(PrintEvent printEvent) {
		
		this.printLabel(printEvent);
	}
	
	/**
	 * 바코드 라벨 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.isSyncMode == false")
	public void printLabelAsyncMode(PrintEvent printEvent) {
		
		this.printLabel(printEvent);
	}
	
	/**
	 * 라벨 프린트 처리
	 * 
	 * @param printEvent
	 */
	private void printLabel(PrintEvent printEvent) {
		
		// 1. 인쇄 옵션 정보 추출
		Long domainId = Domain.currentDomainId();
		String printerIdOrName = printEvent.getPrinterId();
		String templateName = printEvent.getPrintTemplate();
		Map<String, Object> templateParams = printEvent.getTemplateParams();
		
		// 2. 프린터 정보 조회
		Printer printer = Printer.findByIdOrName(domainId, printerIdOrName, true);
		
		// 3. 라벨 인쇄
		this.printLabelByLabelTemplate(printer.getPrinterAgentUrl(), printer.getPrinterDriver(), templateName, templateParams);		
	}

	/**
	 * 라벨 템플릿으로 라벨 인쇄 
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param templateName
	 * @param labelData
	 */
	private void printLabelByLabelTemplate(String printAgentUrl, String printerName, String templateName, Map<String, Object> labelData) {
		
		// 변수를 넣어서 템플릿 엔진을 돌리고 커맨드를 생성 
		DiyTemplate template = this.templateCtrl.dynamicTemplate(templateName, labelData);
		String command = template.getTemplate();
		
		// 송장 라벨 인쇄
		this.printLabelByLabelCommand(printAgentUrl, printerName, command);
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
			printAgentUrl = printAgentUrl + "/barcode?printer=" + printerName;
			rest.postForEntity(printAgentUrl, command, Boolean.class);
		}
	}
}
