package com.jby.stocktake.exportFeature.file;

/**
 * Created by user on 11/5/2017.
 */

public class ExportFileListViewObject {
    private String id;
    private String file;
    private String numCategory;
    private String date;

    public ExportFileListViewObject(String id, String file, String numCategory, String date) {
        this.id = id;
        this.file = file;
        this.numCategory = numCategory;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    String getCategory_numb() {
        return numCategory;
    }

    public String getDate() {
        return date;
    }
}
