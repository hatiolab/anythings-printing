/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.printing.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.printing.config.ModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Anythings Printing Startup시 Framework 초기화 클래스 
 * 
 * @author yang
 */
@Component
public class AnythingsPrintingInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(AnythingsPrintingInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private ModuleProperties module;
	
	@Autowired
	private ModuleConfigSet configSet;

	@EventListener({ ApplicationReadyEvent.class })
	public void ready(ApplicationReadyEvent event) {
		this.logger.info("Anythings Printing module initializing ready...");
	}
	
	@EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent(ContextRefreshedEvent event) {
		this.logger.info("Anythings Printing module initializing started...");	
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();		
    }
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}
}