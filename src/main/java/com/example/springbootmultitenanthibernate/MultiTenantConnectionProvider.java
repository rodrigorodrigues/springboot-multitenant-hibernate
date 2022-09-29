/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.springbootmultitenanthibernate;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.orm.jpa.vendor.Database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider implements HibernatePropertiesCustomizer {

    private final MultipleDataSourcesService multipleDataSourcesService;

    private final MultipleDataSourcesProperties multipleDataSourcesProperties;

    private final Map<String, ConnectionProvider> map;

    public MultiTenantConnectionProvider(MultipleDataSourcesService multipleDataSourcesService,
                                     MultipleDataSourcesProperties multipleDataSourcesProperties) {
        this.multipleDataSourcesService = multipleDataSourcesService;
        this.multipleDataSourcesProperties = multipleDataSourcesProperties;
        map = multipleDataSourcesService.getDataSources()
                .entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> {
                    DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
                    connectionProvider.setDataSource(e.getValue());
                    return connectionProvider;
                }));
        log.info("Set default map connection providers: {}", map);
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        ConnectionProvider connectionProvider = map.get(multipleDataSourcesProperties.getDefaultDatabase().name());
        log.info("getAnyConnectionProvider: {}", connectionProvider);
        return connectionProvider;
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        log.info("selectConnectionProvider: {}", tenantIdentifier);
        return map.get(tenantIdentifier);
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection("PUBLIC");
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        log.info("schema: {}", schema);
        Database database = TenantContext.getDatabaseInfo();
        final Connection connection;
        if (database == null) {
            DataSource dataSource = multipleDataSourcesService.getDataSources().get(multipleDataSourcesProperties.getDefaultDatabase());
            log.info("Set default connection: {}", dataSource);
            connection = dataSource.getConnection();
        } else {
            log.info("Found database: {}", database);
            connection = multipleDataSourcesService.getDataSources().entrySet().stream()
                    .filter(e -> e.getKey() == database).findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not find configuration for database: "+ database)).getValue().getConnection();
        };
        try {
            connection.setSchema(schema);
        } catch (SQLException e) {
            String genericSchema = multipleDataSourcesProperties.getGenericSchema();
            log.warn("Could not found specific schema: {}, will use '{}' schema. error message: {}", schema, genericSchema, e.getLocalizedMessage());
            connection.setSchema(genericSchema); //In the case schema not found use generic schema.
        }
        return connection;
    }

    @Override
    public void releaseConnection(String s, Connection connection) throws SQLException {
        log.info("releaseConnection:schema {}={}", s, connection.getSchema());
//        connection.setSchema(connection.getSchema());
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        throw new UnsupportedOperationException("Can't unwrap this.");
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}