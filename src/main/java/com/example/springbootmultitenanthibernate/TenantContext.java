package com.example.springbootmultitenanthibernate;

import org.springframework.orm.jpa.vendor.Database;

public final class TenantContext {
    private TenantContext() {
    }

    private static final ThreadLocal<String> TENANT_INFO = new InheritableThreadLocal<>();
    private static final ThreadLocal<Database> DATABASE_INFO = new InheritableThreadLocal<>();

    public static String getTenantInfo() {
        return TENANT_INFO.get();
    }

    public static Database getDatabaseInfo() {
        return DATABASE_INFO.get();
    }

    public static void setTenantInfo(String tenant) {
        TENANT_INFO.set(tenant);
    }

    public static void setDatabaseInfo(Database datasource) {
        DATABASE_INFO.set(datasource);
    }

    public static void clear() {
        TENANT_INFO.remove();
        DATABASE_INFO.remove();
    }
}
