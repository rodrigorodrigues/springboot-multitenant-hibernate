package com.example.springbootmultitenanthibernate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.Database;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

@EnableConfigurationProperties(MultipleDataSourcesProperties.class)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(new MultipleDataSourcesInitializer())
                .run(args);
    }

    @Configuration
    @Profile("multiple-datasources")
    @EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class)
    static class MultipleDataSourcesConfiguration {
    }

    @Slf4j
    static class MultipleDataSourcesInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

        @Override
        public void initialize(GenericApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            MultipleDataSourcesProperties multipleDataSources = Binder.get(environment)
                    .bind("datasource.multiple", MultipleDataSourcesProperties.class)
                    .get();

            TenantIdentifierResolver tenantIdentifierResolver = new TenantIdentifierResolver(multipleDataSources);
            applicationContext.registerBean(TenantIdentifierResolver.class.getName(), TenantIdentifierResolver.class, () -> tenantIdentifierResolver);

            if (environment.acceptsProfiles(Profiles.of("multiple-datasources"))) {
                Map<Database, DataSource> datasources = multipleDataSources.getMap()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().initializeDataSourceBuilder().build()));
//                datasources.forEach((k, v) -> applicationContext.registerBean(k.name(), DataSource.class, () -> v));
                datasources.forEach((k, v) -> applicationContext.registerBean(k.name(), DataSource.class, () -> v, bd -> {
                    if (k == multipleDataSources.getDefaultDatabase()) {
                        bd.setPrimary(true);
                    }
                }));
                log.info("multiple datasources: {}", datasources.keySet());

                MultipleDataSourcesService multipleDataSourcesService = new MultipleDataSourcesService(datasources);
                applicationContext.registerBean(MultipleDataSourcesService.class.getName(), MultipleDataSourcesService.class, () -> multipleDataSourcesService);

                MultiTenantConnectionProvider multiTenantConnectionProvider = new MultiTenantConnectionProvider(multipleDataSourcesService, multipleDataSources);
                applicationContext.registerBean(MultiTenantConnectionProvider.class.getName(), MultiTenantConnectionProvider.class, () -> multiTenantConnectionProvider);

                MultiTenantEntityManagerFactoryBean entityManagerFactoryBean = new MultiTenantEntityManagerFactoryBean(multipleDataSourcesService, multipleDataSources, multiTenantConnectionProvider, tenantIdentifierResolver);
                applicationContext.registerBean(MultiTenantEntityManagerFactoryBean.class, () -> entityManagerFactoryBean);
                CustomRepositoryImpl.multiTenantEntityManagerFactoryBean = entityManagerFactoryBean;

                /*Map<String, Map<String, Object>> map = binder.bind("datasource.multiple.map", Map.class).get();
                Map<String, Object> datasourceMap = map
                        .entrySet().stream()
                        .filter(e -> e.getKey().equals(Database.POSTGRESQL.name()))
                        .flatMap(e -> e.getValue().entrySet().stream())
                        .collect(Collectors.toMap(k -> "spring.datasource." + k.getKey(), Map.Entry::getValue));
                environment.getPropertySources().addLast(new MapPropertySource("mainDatasource", datasourceMap));*/
            }
        }
    }
}
