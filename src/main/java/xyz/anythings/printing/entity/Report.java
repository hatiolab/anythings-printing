package xyz.anythings.printing.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "tb_report", idStrategy = GenerationRule.UUID, uniqueFields="domainId,reportCd", indexes = {
	@Index(name = "ix_tb_report_0", columnList = "report_cd,domain_id", unique = true)
})
public class Report extends xyz.elidom.orm.entity.basic.ElidomStampHook {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "report_cd", nullable = false, length = 30)
	private String reportCd;

	@Column (name = "report_nm", length = 100)
	private String reportNm;

	@Column (name = "report_print_url", length = 255)
	private String reportPrintUrl;

	@Column (name = "image", type = ColumnType.TEXT)
	private String image;
	
	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReportCd() {
		return reportCd;
	}

	public void setReportCd(String reportCd) {
		this.reportCd = reportCd;
	}

	public String getReportNm() {
		return reportNm;
	}

	public void setReportNm(String reportNm) {
		this.reportNm = reportNm;
	}

	public String getReportPrintUrl() {
		return reportPrintUrl;
	}

	public void setReportPrintUrl(String reportPrintUrl) {
		this.reportPrintUrl = reportPrintUrl;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

}
