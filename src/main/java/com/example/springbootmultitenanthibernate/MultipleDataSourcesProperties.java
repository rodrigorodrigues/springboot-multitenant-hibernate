package com.example.springbootmultitenanthibernate;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.vendor.Database;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "datasource.multiple")
public class MultipleDataSourcesProperties {
    private Map<Database, DataSourceProperties> map = new HashMap<>();
    private Database defaultDatabase;
    private String genericSchema = "generic";
    private Map<Database, Map<String, String>> jpaPropertiesMap = new HashMap<>();
}
