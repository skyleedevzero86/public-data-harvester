package com.antock.global.jpa;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class SqlStatementCounter implements StatementInspector {

    private static final ThreadLocal<int[]> ACTIVE = new ThreadLocal<>();

    public static void reset() {
        ACTIVE.set(new int[] {0});
    }

    public static int count() {
        int[] current = ACTIVE.get();
        return current == null ? 0 : current[0];
    }

    public static void close() {
        ACTIVE.remove();
    }

    @Override
    public String inspect(String sql) {
        int[] current = ACTIVE.get();
        if (current != null) {
            current[0]++;
        }
        return sql;
    }
}
