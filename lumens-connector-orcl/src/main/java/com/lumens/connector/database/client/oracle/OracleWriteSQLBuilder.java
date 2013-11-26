package com.lumens.connector.database.client.oracle;

import com.lumens.model.Element;
import com.lumens.model.Value;

/**
 *
 * @author shaofeng.wang@outlook.com
 */
public class OracleWriteSQLBuilder extends OracleSQLBuilder {
    @Override
    public String generateInsertSQL(Element input) {
        String tableName = input.getFormat().getName();
        StringBuilder sql = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Element e : input.getChildren()) {
            if (fields.length() > 0) {
                fields.append(", ");
            }
            fields.append(e.getFormat().getName());
            if (values.length() > 0) {
                values.append(", ");
            }
            Value v = e.getValue();
            if (v.isString()) {
                values.append("'").append(v.getString()).append("'");
            } else {
                values.append(v.getString());
            }
        }
        return sql.append("INSERT INTO ").append(tableName).append(" (").append(fields.toString()).append(") VALUES (").append(values.toString()).append(")").toString();
    }

    @Override
    public String generateUpdateSQL(Element input) {
        String tableName = input.getFormat().getName();
        StringBuilder sql = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Element e : input.getChildren()) {
            if (values.length() > 0) {
                values.append(", ");
            }
            values.append(e.getFormat().getName()).append('=');
            Value v = e.getValue();
            if (v.isString()) {
                values.append("'").append(v.getString()).append("'");
            } else {
                values.append(v.getString());
            }
        }
        sql.append("UPDATE ").append(tableName).append(" SET ").append(values.toString());
        if (input != null) {
            String sqlClause = input.getValue().getString();
            if (sqlClause != null && !sqlClause.isEmpty()) {
                sql.append(' ').append(sqlClause);
            }
        }
        return sql.toString();
    }
}