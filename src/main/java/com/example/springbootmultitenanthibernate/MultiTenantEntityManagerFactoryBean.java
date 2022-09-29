package com.example.springbootmultitenanthibernate;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Environment;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MultiTenantEntityManagerFactoryBean {
    private final MultipleDataSourcesProperties multipleDataSourcesProperties;
    private final Map<Database, EntityManager> mapEntityManager;

    public MultiTenantEntityManagerFactoryBean(MultipleDataSourcesService multipleDataSourcesService, MultipleDataSourcesProperties multipleDataSourcesProperties,
                                               MultiTenantConnectionProvider multiTenantConnectionProvider,
                                               TenantIdentifierResolver tenantIdentifierResolver) {
        this.multipleDataSourcesProperties = multipleDataSourcesProperties;
        this.mapEntityManager = multipleDataSourcesService.getDataSources()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
                    entityManager.setDataSource(e.getValue());
                    entityManager.setPackagesToScan(Person.class.getPackageName());
                    entityManager.setPersistenceUnitName(e.getKey().name());
                    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                    entityManager.setJpaVendorAdapter(vendorAdapter);
                    Map<String, Object> jpaPropertyMap = new HashMap<>(multipleDataSourcesProperties.getJpaPropertiesMap().get(e.getKey()));
                    jpaPropertyMap.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
                    jpaPropertyMap.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
                    entityManager.setJpaPropertyMap(jpaPropertyMap);
                    entityManager.afterPropertiesSet();
                    log.info("entityManagerFactory:dialect: {}", entityManager.getJpaDialect());
                    return SharedEntityManagerCreator.createSharedEntityManager(entityManager.getObject());
                }));
        log.info("Set default map entity manager: {}", mapEntityManager);
    }

    public EntityManager getEntityManagerInterface() {
        log.info("getEntityManagerInterface: {}", TenantContext.getDatabaseInfo());
        return Optional.ofNullable(TenantContext.getDatabaseInfo())
                .or(() -> Optional.of(multipleDataSourcesProperties.getDefaultDatabase()))
                .map(mapEntityManager::get)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not found sepcific database!"));
    }
}
