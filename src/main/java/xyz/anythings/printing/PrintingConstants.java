package xyz.anythings.printing;

/**
 * Anythings Printing 관련 상수 정의 
 * 
 * @author yang
 */
public class PrintingConstants  {
	
	/**
	 * 프린터 / 템플릿 유형 BARCODE : 바코드 프린터
	 */
	public static final String PRINTER_TYPE_BARCODE = "BARCODE";
	/**
	 * 프린터 / 템플릿  유형 NORMAL : 일반 프린터 
	 */
	public static final String PRINTER_TYPE_NORMAL = "NORMAL";
	
	/**
	 * 바코드 프린트 REST URL
	 */
	public static final String BARCODE_REST_URL = "/barcode?printer=";
	/**
	 * PDF 프린트 REST URL
	 */
	public static final String NORMAL_REST_URL = "/pdf?printer=";
	
}
