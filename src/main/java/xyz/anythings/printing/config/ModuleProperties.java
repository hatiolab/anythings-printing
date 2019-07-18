/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.printing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * anythings-printing 모듈 정보 파일
 * 
 * @author yang
 */
@Component("anythingsPrintingModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/anythings-printing.properties")
public class ModuleProperties implements IModuleProperties {
	
	/**
	 * 모듈명
	 */
	@Value("${anythings.printing.name}")
	private String name;
	
	/**
	 * 버전
	 */
	@Value("${anythings.printing.version}")
	private String version;
	
	/**
	 * Module Built Time 
	 */
	@Value("${anythings.printing.built.at}")
	private String builtAt;	
	
	/**
	 * 모듈 설명
	 */
	@Value("${anythings.printing.description}")
	private String description;
	
	/**
	 * 부모 모듈
	 */
	@Value("${anythings.printing.parentModule}")
	private String parentModule;
	
	/**
	 * 모듈 Base Package
	 */
	@Value("${anythings.printing.basePackage}")
	private String basePackage;
	
	/**
	 * Scan Service Path
	 */
	@Value("${anythings.printing.scanServicePackage}")
	private String scanServicePackage;
	
	/**
	 * Scan Entity Path
	 */
	@Value("${anythings.printing.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * 모듈에서 사용할 rabbitmq 큐 명칭 
	 */
	@Value("${anythings.printing.rabbitQueue:not_use}")
	private String rabbitQueue;

	
	/**
	 * Project Name
	 * @return
	 */
	@Value("${anythings.printing.projectName}")
	private String projectName;
	
	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}
	
	public String getBuiltAt() {
		return builtAt;
	}

	public String getDescription() {
		return this.description;
	}
	
	public String getParentModule() {
		return this.parentModule;
	}

	public String getBasePackage() {
		return this.basePackage;
	}

	public String getScanServicePackage() {
		return this.scanServicePackage;
	}

	public String getScanEntityPackage() {
		return this.scanEntityPackage;
	}
	
	public String getProjectName() {
		return this.projectName;
	}

	public String getRabbitQueue() {
		return this.rabbitQueue;
	}

	@Override
	public String toString() {
		return FormatUtil.toJsonString(this);
	}
}