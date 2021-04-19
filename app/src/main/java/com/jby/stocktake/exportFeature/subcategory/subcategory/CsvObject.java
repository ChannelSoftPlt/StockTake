package com.jby.stocktake.exportFeature.subcategory.subcategory;

public class CsvObject {
    private String header, content;

    public CsvObject(String header, String content) {
        this.header = header;
        this.content = content;
    }

    public String getHeader() {
        return header;
    }

    public String getColumns() {
        return content;
    }
}
