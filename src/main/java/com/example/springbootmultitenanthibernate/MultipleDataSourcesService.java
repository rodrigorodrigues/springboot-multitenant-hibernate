package com.example.springbootmultitenanthibernate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.orm.jpa.vendor.Database;

import javax.sql.DataSource;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MultipleDataSourcesService {
    private final Map<Database, DataSource> dataSources;
}
