package com.example.springbootmultitenanthibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {
    private final MultipleDataSourcesProperties multipleDataSourcesProperties;

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantIdentifier = Optional.ofNullable(TenantContext.getTenantInfo()).orElse(multipleDataSourcesProperties.getGenericSchema());
        log.info("tenantIdentifier: {}", tenantIdentifier);
        return tenantIdentifier;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
