package xyz.anythings.printing.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import xyz.anythings.sys.event.model.PrintEvent;

/**
 * PDF 프린트 서비스
 * 
 * @author shortstop
 */
@Component
public class PdfPrintService {

	/**
	 * PDF 프린트 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.isSyncMode == true")
	public void printPdfSyncMode(PrintEvent printEvent) {
		
	}
	
	/**
	 * PDF 프린트 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.isSyncMode == false")
	public void printPdfAsyncMode(PrintEvent printEvent) {
		
	}

}
