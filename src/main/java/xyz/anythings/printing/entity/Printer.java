package xyz.anythings.printing.entity;

import java.util.List;

import xyz.anythings.printing.PrintingConstants;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Table(name = "tb_printer", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,printerCd", indexes = {
		@Index(name = "ix_tb_printer_0", columnList = "domain_id,printer_cd", unique = true) 
})
public class Printer extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1554003864904391505L;
	

	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "printer_cd", nullable = false, length = 30)
	private String printerCd;

	@Column(name = "printer_nm", nullable = false, length = 100)
	private String printerNm;

	@Column(name = "printer_type", length = 20)
	private String printerType;

	@Column(name = "printer_ip", nullable = false, length = 16)
	private String printerIp;

	@Column(name = "printer_port", length = 5)
	private Integer printerPort;
	
	@Column(name = "printer_driver", length = 40)
	private String printerDriver;
	
	@Column(name = "printer_agent_url", length = 255)
	private String printerAgentUrl;

	@Column(name = "status", length = 10)
	private String status;

	@Column(name = "remark", length = 1000)
	private String remark;
	
	@Column(name = "default_flag")
	private Boolean defaultFlag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPrinterCd() {
		return printerCd;
	}

	public void setPrinterCd(String printerCd) {
		this.printerCd = printerCd;
	}

	public String getPrinterNm() {
		return printerNm;
	}

	public void setPrinterNm(String printerNm) {
		this.printerNm = printerNm;
	}

	public String getPrinterType() {
		return printerType;
	}

	public void setPrinterType(String printerType) {
		this.printerType = printerType;
	}

	public String getPrinterIp() {
		return printerIp;
	}

	public void setPrinterIp(String printerIp) {
		this.printerIp = printerIp;
	}

	public Integer getPrinterPort() {
		return printerPort;
	}

	public void setPrinterPort(Integer printerPort) {
		this.printerPort = printerPort;
	}

	public String getPrinterDriver() {
		return printerDriver;
	}

	public void setPrinterDriver(String printerDriver) {
		this.printerDriver = printerDriver;
	}

	public String getPrinterAgentUrl() {
		return printerAgentUrl;
	}

	public void setPrinterAgentUrl(String printerAgentUrl) {
		this.printerAgentUrl = printerAgentUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	/**
	 * printerId로 프린터를 조회
	 * 
	 * @param domainId
	 * @param printerId
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer find(Long domainId, String printerId, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("id", printerId);
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Printer", printerId);
		}
		
		return printer;
	}
	
	/**
	 * printerId 혹은 printerName으로 프린터 조회
	 * 
	 * @param domainId
	 * @param printerIdOrName
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findByIdOrName(Long domainId, String printerIdOrName, boolean exceptionWhenEmpty) {
		Printer printer = Printer.find(domainId, printerIdOrName, false);
		
		if(printer == null) {
			printer = Printer.findPrinter(domainId, printerIdOrName, exceptionWhenEmpty);
		}
		
		return printer;
	}
	
	/**
	 * printerName으로 프린터 조회
	 * 
	 * @param domainId
	 * @param printerName
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findPrinter(Long domainId, String printerName, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("printerNm", printerName);
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Printer", printerName);
		}
		
		return printer;
	}
	
	
	/**
	 * 도메인 내 기본 바코드 프린터 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public static Printer findDefaultBarcodePrinter(Long domainId) {
		return findDefaultPrinter(domainId, PrintingConstants.PRINTER_TYPE_BARCODE);
	}
	
	/**
	 * 도메인 내 기본 일반 프린터 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public static Printer findDefaultNormalPrinter(Long domainId) {
		return findDefaultPrinter(domainId, PrintingConstants.PRINTER_TYPE_NORMAL);
	}

	/**
	 * 도메인 내 프린터 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public static Printer findDefaultPrinter(Long domainId, String printerType) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("defaultFlag", true);
		condition.addFilter("printerType", printerType);
		condition.setPageIndex(1);
		condition.setPageSize(1);
		List<Printer> printerList = BeanUtil.get(IQueryManager.class).selectList(Printer.class, condition);
		
		if(ValueUtil.isNotEmpty(printerList)) {
			return printerList.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * 프린터 코드로 프린터 조회 
	 * @param domainId
	 * @param printerCd
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static Printer findByPrinterCd(Long domainId, String printerCd, boolean exceptionWhenEmpty) {
		
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("printerCd", printerCd);
		
		Printer printer = BeanUtil.get(IQueryManager.class).selectByCondition(Printer.class, condition);
		
		if(exceptionWhenEmpty && printer == null) {
			throw new ElidomValidationException("프린터 [" + printer + "]가 존재하지 않습니다.");
		}
		
		return printer;
	}
	
}