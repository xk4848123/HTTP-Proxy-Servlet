package com.zary.sniffer.agent.plugin.jdbc.handler;
;

import com.zary.sniffer.agent.core.plugin.handler.IConstructorHandler;

import java.sql.*;

public class ConnectionHandler implements IConstructorHandler {

    @Override
    public void onConstruct(Object instance, Object[] allArguments) {
        Connection connection = (Connection) instance;
        StringBuilder sb = new StringBuilder();
        sb.append(">>>>>>>>>>Connection构造函数执行完成");
        sb.append("\nclass:" + instance.getClass().getName());
        try {
            sb.append("\nprops:" + connection.getClientInfo());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            sb.append("\nuser:" + connection.getClientInfo("user"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            sb.append("\npassword:" + connection.getClientInfo("password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            DatabaseMetaData metaData = (DatabaseMetaData) connection.getMetaData();
            sb.append("\nurl:" + metaData.getURL());
            sb.append("\nproductname:" + metaData.getDatabaseProductName());
            sb.append("\ndrivername:" + metaData.getDriverName());
            sb.append("\nusername:" + metaData.getUserName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            sb.append("\ncatalog:" + connection.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        System.out.println(sb.toString());

        Statement st;
        PreparedStatement ps;
        CallableStatement cs;

    }
}
