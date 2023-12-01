package com.zary.sniffer.core.model.ext;

import java.util.List;

/**
 * @Author weiyi
 * @create 2020/3/17 10:00
 */
public class SqlParseResultInfoExt {
    /**
     * 唯一表示
     */
    private String parseId;
    /**
     * 检查状态
     */
    private int statusCode;
    /**
     * 参数化sql hash
     */
    private String hash;
    /**
     * 语法错误
     */
    private boolean syntaxError;
    /**
     * 参数化sql
     */
    private String parameterizedSql;
    /**
     * 语法分析的结果集
     */
    private List<ViolationItem> violations;


    /**
     * sql中使用的table及操作类型
     */
    private List<String> tables;
    private List<String> operates;
    private List<String> table_operates;
    /**
     * sql中使用到的function
     */
    private List<String> functions;

    /**
     * 分值
     */
    private Integer score;


    public String getParseId() {
        return parseId;
    }

    public void setParseId(String parseId) {
        this.parseId = parseId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isSyntaxError() {
        return syntaxError;
    }

    public void setSyntaxError(boolean syntaxError) {
        this.syntaxError = syntaxError;
    }

    public String getParameterizedSql() {
        return parameterizedSql;
    }

    public void setParameterizedSql(String parameterizedSql) {
        this.parameterizedSql = parameterizedSql;
    }

    public List<ViolationItem> getViolations() {
        return violations;
    }

    public void setViolations(List<ViolationItem> violations) {
        this.violations = violations;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<String> getOperates() {
        return operates;
    }

    public void setOperates(List<String> operates) {
        this.operates = operates;
    }

    public List<String> getTable_operates() {
        return table_operates;
    }

    public void setTable_operates(List<String> table_operates) {
        this.table_operates = table_operates;
    }

    public List<String> getFunctions() {
        return functions;
    }

    public void setFunctions(List<String> functions) {
        this.functions = functions;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public static class ViolationItem {
        int code;
        String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
