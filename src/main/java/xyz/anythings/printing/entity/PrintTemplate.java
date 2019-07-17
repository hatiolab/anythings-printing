package xyz.anythings.printing.entity;

import java.util.Map;

import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "tb_print_templates", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = {
		@Index(name = "ix_tb_print_templates_0", columnList = "name,domain_id", unique = true) 
})
public class PrintTemplate extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3055816825875450647L;
	
	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;
	
	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_LONG_NAME)
	private String name;

	@Column(name = "template_type", nullable = false, length = 20)
	private String templateType;

	@Column(name = "template", type = ColumnType.TEXT)
	private String template;

	@Column(name = "service_name", length = OrmConstants.FIELD_SIZE_NAME)
	private String serviceName;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * templateName 으로 템플릿 조회
	 * @param domainId
	 * @param templateName
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public static PrintTemplate findByName(Long domainId, String templateName, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("name", templateName);
		PrintTemplate template = BeanUtil.get(IQueryManager.class).selectByCondition(PrintTemplate.class, condition);
		
		if(exceptionWhenEmpty && template == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.PrinterTemplate", templateName);
		}
		return template;
	}
	
	/**
	 * 서비스 로직을 실행해 결과를 리턴
	 * @param paramsMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> getServiceLogicData(Map<String,Object> paramsMap){
		
		paramsMap.put("domainId" , this.domainId);
		Map<String, Object> result = BeanUtil.get(IQueryManager.class).callReturnProcedure(this.serviceName, paramsMap, Map.class);
		return result;
	}
}