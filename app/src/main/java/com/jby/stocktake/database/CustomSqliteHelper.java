package com.jby.stocktake.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jby.stocktake.exportFeature.category.ExportCategoryListViewObject;
import com.jby.stocktake.exportFeature.category.searchFeature.ExportCategorySearchCategoryObject;
import com.jby.stocktake.exportFeature.category.searchFeature.ExportCategorySearchSubCategoryObject;
import com.jby.stocktake.exportFeature.file.ExportFileListViewObject;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryObject;
import com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction.TakeActionObject;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 3/11/2018.
 */

public class CustomSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 1;

    private static final String TB_FILE = "tb_export_file";
    private static final String TB_CATEGORY = "tb_export_category";
    private static final String TB_SUB_CATEGORY = "tb_export_sub_category";


    private static final String CREATE_TABLE_FILE = "CREATE TABLE "+ TB_FILE +
            "(file_id INTEGER PRIMARY KEY, " +
            "file_name Text, " +
            "user_id Text, " +
            "created_at Text, " +
            "updated_at Text)";

    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TB_CATEGORY +
            "(category_id INTEGER PRIMARY KEY, " +
            "category_name Text, " +
            "user_id Text, " +
            "file_id Text, " +
            "created_at Text, " +
            "updated_at Text)";

    private static final String CREATE_TABLE_SUB_CATEGORY = "CREATE TABLE " + TB_SUB_CATEGORY +
            "(sub_category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "barcode Text, " +
            "quantity Text, " +
            "category_id Text, " +
            "user_id Text, " +
            "date_create Text, " +
            "time_create Text, " +
            "priority Text)";

    private String timeStamp;
    private Context context;

   public CustomSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_FILE);
        sqLiteDatabase.execSQL(CREATE_TABLE_CATEGORY);
        sqLiteDatabase.execSQL(CREATE_TABLE_SUB_CATEGORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FILE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_CATEGORY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_SUB_CATEGORY);
        onCreate(sqLiteDatabase);
    }

    public int saveFile(String file_name)
    {
        boolean fileIsExited = checkFileIsExisted(file_name);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        int status;
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        String id = String.valueOf(android.text.format.DateFormat.format("yyMMddHHmmss", new java.util.Date()));
        String userID = SharedPreferenceManager.getUserID(context);
        String fileId = userID + id;

        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", fileId);
        contentValues.put("file_name", file_name);
        contentValues.put("user_id", userID);
        contentValues.put("created_at", timeStamp);

//         new record
        if(!fileIsExited) {
            result = db.insert(TB_FILE, null, contentValues);
            status = 1;
        }
//        if existed
        else {
            status = 2;
            result = 1;
        }
//        if failed
        if(result == -1)
            status = 3;

        return status;

    }

    public boolean updateFile(String file_name, String file_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));

        ContentValues contentValues = new ContentValues();
        contentValues.put("file_name", file_name);
        contentValues.put("updated_at", timeStamp);

        result = db.update(TB_FILE, contentValues, "file_id=?", new String[] { file_id });
        if(result != -1)
            status = true;

        return status;
    }

    private boolean checkFileIsExisted(String file_name){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT file_id FROM " +TB_FILE+ " WHERE file_name=?";
        Cursor cursor = db.rawQuery(sql, new String[] { file_name });
        boolean status = false;

        if(cursor.getCount() > 0)
            status = true;
        cursor.close();
        return status;
    }

    public ArrayList<ExportFileListViewObject> fetchAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ExportFileListViewObject> results = new ArrayList<>();
        String userID = SharedPreferenceManager.getUserID(context);
        String sql = "SELECT tb_export_file.file_id, tb_export_file.file_name, COUNT(tb_export_category.category_id) FROM " +TB_FILE+
                " LEFT JOIN "+ TB_CATEGORY +
                " ON tb_export_file.file_id = tb_export_category.file_id" +
                " WHERE tb_export_file.user_id=" + userID +
                " GROUP BY tb_export_file.file_name" +
                " ORDER BY tb_export_file.file_id DESC";

        Cursor crs = db.rawQuery(sql, null);
        while (crs.moveToNext()) {
            results.add(new ExportFileListViewObject(crs.getString(crs.getColumnIndex("file_id")),
                    crs.getString(crs.getColumnIndex("file_name")),
                    String.valueOf(crs.getInt(2))));
        }
        db.close();
        crs.close();
        return results;
    }

    public boolean deleteFile(List ids){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        String args = TextUtils.join(", ", ids);
        result = db.delete(TB_FILE,"file_id IN (" + args + ")", null);
        if(result != -1){
            boolean deleteAllCategoryRelatedToFile = deleteAllCategoryRelatedToFile(args);
            if(deleteAllCategoryRelatedToFile)
                status = true;
        }
        return status;

    }

    private boolean deleteAllCategoryRelatedToFile(String fileID){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<String>();
        String sql = "SELECT category_id FROM " +TB_CATEGORY+ " WHERE file_id IN (" + fileID + ")";
        boolean result = false;

        Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndex("category_id")));
            }
        boolean deleteCategory = deleteCategory(list);
        if(deleteCategory){
            boolean deleteSubCategory = deleteSubCategoryRelatedToCategory(list);
            if(deleteSubCategory)
                result = true;
        }
        db.close();
        cursor.close();
        return result;
    }

    /**************************************************************************** category purpose*******************************************************************/

    public ArrayList<ExportCategoryListViewObject> fetchAllCategory(String fileID, int page, ArrayList<ExportCategoryListViewObject> currentArrayList) {
        SQLiteDatabase db = this.getReadableDatabase();

        double start;
        double limit = 10.0;
        int totalCategoryRow = countCategoryRow(fileID);
        int page_limit = (int) Math.ceil(totalCategoryRow/limit);

        page_limit = (int) Math.ceil(page_limit);
        if(page <= page_limit) {
            start = (page - 1) * limit;

            String sql = "SELECT tb_export_category.category_id, tb_export_category.category_name, COUNT(tb_export_sub_category.sub_category_id) FROM " + TB_CATEGORY +
                    " LEFT JOIN tb_export_sub_category" +
                    " ON tb_export_category.category_id = tb_export_sub_category.category_id" +
                    " WHERE tb_export_category.file_id=" + fileID +
                    " GROUP BY tb_export_category.category_name" +
                    " ORDER BY tb_export_category.category_id DESC" +
                    " LIMIT " + start + " , " + limit;

            Cursor crs = db.rawQuery(sql, null);


            while (crs.moveToNext()) {
                currentArrayList.add(new ExportCategoryListViewObject(crs.getString(crs.getColumnIndex("category_id")),
                        crs.getString(crs.getColumnIndex("category_name")),
                        String.valueOf(crs.getInt(2))));
            }
            crs.close();
        }
        db.close();

        return currentArrayList;
    }

    private int countCategoryRow(String fileID){
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT category_id FROM "+ TB_CATEGORY + " WHERE file_id=" + fileID;
        Cursor cursor = db.rawQuery(sql, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount;
    }

    public int saveCategory(String category_name, String file_id)
    {
        boolean categoryIsExited = checkCategoryIsExisted(category_name);
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        int status;
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        String id = String.valueOf(android.text.format.DateFormat.format("yyMMddHHmmss", new java.util.Date()));
        String userID = SharedPreferenceManager.getUserID(context);
        String categoryId = userID + id;



        ContentValues contentValues = new ContentValues();
        contentValues.put("category_id", categoryId);
        contentValues.put("category_name", category_name);
        contentValues.put("user_id", userID);
        contentValues.put("file_id", file_id);
        contentValues.put("created_at", timeStamp);

//         new record
        if(!categoryIsExited) {
            result = db.insert(TB_CATEGORY, null, contentValues);
            status = 1;
        }
//        if existed
        else {
            status = 2;
            result = 1;
        }
//        if failed
        if(result == -1)
            status = 3;

        return status;

    }

    public int updateCategory(String category_name, String category_id){
        boolean categoryIsExited = checkCategoryIsExisted(category_name);
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        int status = 3;
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));

        if(!categoryIsExited)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("category_name", category_name);
            contentValues.put("updated_at", timeStamp);

            result = db.update(TB_CATEGORY, contentValues, "category_id=?", new String[] { category_id });
            if(result != -1)
                status = 1;
        }
        else{
            status = 2;
        }

        return status;
    }

    public boolean deleteCategory(List ids){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        String args = TextUtils.join(", ", ids);
        result = db.delete(TB_CATEGORY,"category_id IN (" + args + ")", null);
        if(result != -1){
            boolean deleteSubCategory = deleteSubCategoryRelatedToCategory(ids);
            if(deleteSubCategory)
                status = true;
        }
        return status;

    }

    private boolean deleteSubCategoryRelatedToCategory(List ids){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        String args = TextUtils.join(", ", ids);
        result = db.delete(TB_SUB_CATEGORY,"category_id IN (" + args + ")", null);
        if(result != -1)
            status = true;
        return status;
    }

    private boolean checkCategoryIsExisted(String category_name){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT category_id FROM " +TB_CATEGORY+ " WHERE category_name=?";
        Cursor cursor = db.rawQuery(sql, new String[] { category_name });
        boolean status = false;

        if(cursor.getCount() > 0)
            status = true;
        cursor.close();
        return status;
    }

    public ArrayList<ExportCategorySearchSubCategoryObject> searchAllSubCategoryFromCategory(String fileID, ArrayList<ExportCategorySearchSubCategoryObject> currentArrayList, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

            String sql = "SELECT tb_export_sub_category.barcode, tb_export_sub_category.quantity, tb_export_category.category_name," +
                    " tb_export_category.category_id, tb_export_sub_category.date_create" +
                    " FROM " + TB_SUB_CATEGORY +
                    " INNER JOIN " + TB_CATEGORY +
                    " ON tb_export_sub_category.category_id = tb_export_category.category_id" +
                    " WHERE tb_export_category.file_id=" + fileID + " AND (tb_export_sub_category.barcode LIKE '%" + keyword + "%')"+
                    " ORDER BY tb_export_sub_category.priority DESC";

            Cursor crs = db.rawQuery(sql, null);

            while (crs.moveToNext()) {
                currentArrayList.add(new ExportCategorySearchSubCategoryObject(
                        crs.getString(crs.getColumnIndex("barcode")),
                        crs.getString(crs.getColumnIndex("quantity")),
                        crs.getString(crs.getColumnIndex("category_name")),
                        crs.getString(crs.getColumnIndex("category_id")),
                        crs.getString(crs.getColumnIndex("date_create"))));
            }
            crs.close();
        db.close();

        return currentArrayList;
    }

    public ArrayList<ExportCategorySearchCategoryObject> searchAllCategoryByQuery(String fileID, ArrayList<ExportCategorySearchCategoryObject> currentArrayList, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

            String sql = "SELECT tb_export_category.category_id, tb_export_category.category_name, COUNT(tb_export_sub_category.sub_category_id) FROM "
                    + TB_CATEGORY + " LEFT JOIN " + TB_SUB_CATEGORY +
                    " ON tb_export_category.category_id = tb_export_sub_category.category_id" +
                    " WHERE tb_export_category.file_id="+ fileID +" AND (tb_export_category.category_name LIKE '%" + keyword + "%')" +
                    " GROUP BY tb_export_category.category_name" +
                    " ORDER BY tb_export_category.category_id DESC";

            Cursor crs = db.rawQuery(sql, null);

            while (crs.moveToNext()) {
                currentArrayList.add(new ExportCategorySearchCategoryObject(crs.getString(crs.getColumnIndex("category_id")),
                        crs.getString(crs.getColumnIndex("category_name")),
                        String.valueOf(crs.getInt(2))));
            }
        crs.close();
        db.close();
        return currentArrayList;
    }


    /**************************************************************************** subcategory purpose*******************************************************************/

    public ArrayList<SubCategoryObject> fetchAllSubCategory(String category_id, int page, ArrayList<SubCategoryObject> currentArrayList) {
        SQLiteDatabase db = this.getReadableDatabase();

        double start;
        double limit = 10.0;
        int totalSubCategoryRow = countSubCategoryRow(category_id);
        int page_limit = (int) Math.ceil(totalSubCategoryRow/limit);

        page_limit = (int) Math.ceil(page_limit);
        if(page <= page_limit) {
            start = (page - 1) * limit;

            String sql = "SELECT * FROM " + TB_SUB_CATEGORY + " WHERE category_id=" + category_id +
                    " ORDER BY priority DESC" +
                    " LIMIT " + start + " , " + limit;

            Cursor crs = db.rawQuery(sql, null);


            while (crs.moveToNext()) {
                currentArrayList.add(new SubCategoryObject(
                        crs.getString(crs.getColumnIndex("sub_category_id")),
                        crs.getString(crs.getColumnIndex("barcode")),
                        crs.getString(crs.getColumnIndex("quantity")),
                        crs.getString(crs.getColumnIndex("date_create")),
                        crs.getString(crs.getColumnIndex("time_create"))));
            }
            crs.close();
        }
        db.close();

        return currentArrayList;
    }

    public int countSubCategoryRow(String categoryID){
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT category_id FROM "+ TB_SUB_CATEGORY + " WHERE category_id=" + categoryID;
        Cursor cursor = db.rawQuery(sql, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount;
    }
    /**************************************************************************** subcategory save purpose*******************************************************************/

    public int saveSubCategory(String file_id, int count, String categoryID, String barcode, int quantity)
    {
        String reminder = SharedPreferenceManager.getReminder(context);
        int status = 0;
        boolean reminderStatus = false;
        if(reminder.equals("1") && count == 0){
//            check this record existed or not
            int checkRecordAvailabilityInOthers = checkSubCategoryExistedInOther(categoryID, barcode, file_id);
//            if exist
            if(checkRecordAvailabilityInOthers > 0){
                reminderStatus = true;
                status = 3;
            }

        }
        if(!reminderStatus){
//            if reminder = off or record is not exist in other category then proceed to here
            boolean checkRecordAvailability = checkSubCategoryExisted(barcode, categoryID, quantity);
            if(checkRecordAvailability)
                status = 1;
            else
                status = 2;
        }
        return status;
    }

    private int checkSubCategoryExistedInOther(String categoryID, String barcode, String file_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT tb_export_sub_category.sub_category_id FROM " + TB_SUB_CATEGORY +
                " INNER JOIN " + TB_CATEGORY +
                " ON tb_export_sub_category.category_id = tb_export_category.category_id" +
                " WHERE tb_export_sub_category.category_id <>?" +
                " AND tb_export_sub_category.barcode =?"  +
                " AND tb_export_category.file_id =?";

        Cursor cursor = db.rawQuery(sql,  new String[] { categoryID, barcode, file_id });
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount;
    }

    private boolean checkSubCategoryExisted(String barcode, String categoryID, int quantity){
        SQLiteDatabase db = this.getReadableDatabase();
        boolean status = false;

        String sql = "SELECT quantity, sub_category_id FROM "+ TB_SUB_CATEGORY +
                " WHERE barcode=? AND category_id=?";

        Cursor cursor = db.rawQuery(sql, new String[] { barcode, categoryID });


        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            int newQuantity = cursor.getInt(cursor.getColumnIndex("quantity")) + quantity;
            String subCategoryID = cursor.getString(cursor.getColumnIndex("sub_category_id"));

            boolean updateExistedRecord = updateExistedSubCategory(subCategoryID, newQuantity);
            if(updateExistedRecord)
                status = true;
        }
        else{
            boolean saveNewRecord = saveNewSubCategory(categoryID, quantity, barcode);
            if(saveNewRecord)
                status = true;
        }
        cursor.close();
        return status;
    }

    private boolean updateExistedSubCategory(String subCategoryID, int newQuantity){
        String dateCreate = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
        String timeCreate = String.valueOf(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
        String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put("quantity", newQuantity);
        contentValues.put("date_create", dateCreate);
        contentValues.put("time_create", timeCreate);
        contentValues.put("priority", priority);

        result = db.update(TB_SUB_CATEGORY, contentValues, "sub_category_id=?", new String[] { subCategoryID });
        if(result != -1)
            status = true;
        return status;
    }

    private boolean saveNewSubCategory(String categoryID, int quantity, String barcode){
        String dateCreate = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
        String timeCreate = String.valueOf(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
        String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        String userID = SharedPreferenceManager.getUserID(context);
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put("category_id", categoryID);
        contentValues.put("user_id", userID);
        contentValues.put("quantity", quantity);
        contentValues.put("barcode", barcode);
        contentValues.put("date_create", dateCreate);
        contentValues.put("time_create", timeCreate);
        contentValues.put("priority", priority);

        result = db.insert(TB_SUB_CATEGORY, null, contentValues);

        if(result != -1)
            status = true;

        return status;

    }
    /**************************************************************************** subcategory update purpose*******************************************************************/

    public int updateSubCategory(String file_id, int count, String categoryID, String barcode, int quantity, String subCategoryID)
    {
        String reminder = SharedPreferenceManager.getReminder(context);
        int status = 0;
        boolean reminderStatus = false;
        if(reminder.equals("1") && count == 0){
//            check this record existed or not
            int checkRecordAvailabilityInOthers = checkSubCategoryExistedInOther(categoryID, barcode, file_id);
//            if exist
            if(checkRecordAvailabilityInOthers > 0){
                reminderStatus = true;
                status = 3;
            }

        }
        if(!reminderStatus){
//            if reminder = off or record is not exist in other category then proceed to here
            boolean update = checkSubCategoryExistedForUpdate(barcode, categoryID, quantity, subCategoryID);
            if(update)
                status = 1;
            else
                status = 2;
        }
        return status;
    }

    private boolean checkSubCategoryExistedForUpdate(String barcode, String categoryID, int quantity, String subCategoryID){
        SQLiteDatabase db = this.getReadableDatabase();
        boolean status = false;

        String sql = "SELECT quantity, sub_category_id FROM "+ TB_SUB_CATEGORY +
                " WHERE barcode=? AND category_id=? AND sub_category_id<>?";

        Cursor cursor = db.rawQuery(sql, new String[] { barcode, categoryID, subCategoryID });


        if(cursor.getCount() != 0){
            boolean updateExistedRecord = getExistedRecordForUpdate(categoryID, barcode, quantity, subCategoryID);
            if(updateExistedRecord)
                status = true;
        }
        else{
            boolean updateRecord = updateRecord(subCategoryID, quantity, barcode);
            if(updateRecord)
                status = true;
        }
        cursor.close();
        return status;
    }

    private boolean getExistedRecordForUpdate(String categoryID, String barcode, int updateQuantity, String selectedID){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT quantity, sub_category_id FROM "+ TB_SUB_CATEGORY +
                " WHERE barcode=? AND category_id=?";
        boolean status = false;

        Cursor cursor = db.rawQuery(sql, new String[] { barcode, categoryID });
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            int newQuantity = cursor.getInt(cursor.getColumnIndex("quantity")) + updateQuantity;
            String subCategoryID = cursor.getString(cursor.getColumnIndex("sub_category_id"));
            boolean updateExistedRecord = updateExistedRecord(subCategoryID, newQuantity, selectedID);
            if(updateExistedRecord)
                status = true;

        }
        cursor.close();
        return status;
    }

    private boolean updateExistedRecord(String subCategoryID, int newQuantity, String selectedID){
        SQLiteDatabase db = this.getReadableDatabase();
        String dateCreate = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
        String timeCreate = String.valueOf(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
        String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        ContentValues contentValues = new ContentValues();
        long result;
        boolean status = false;

        contentValues.put("quantity", newQuantity);
        contentValues.put("date_create", dateCreate);
        contentValues.put("time_create", timeCreate);
        contentValues.put("priority", priority);

        result = db.update(TB_SUB_CATEGORY, contentValues, "sub_category_id=?", new String[] { subCategoryID });
        if(result != -1){
            boolean deleteRecordAfterUpdate = deleteExistedRecordAfterUpdate(selectedID);
            if(deleteRecordAfterUpdate)
                status = true;
        }
        return status;
    }

    private boolean deleteExistedRecordAfterUpdate(String selectedID){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean status = false;
        long result;
        result = db.delete(TB_SUB_CATEGORY,"sub_category_id=?", new String[] { selectedID });

        if(result != -1)
            status = true;
        return status;
    }

    private boolean updateRecord(String subCategoryID, int quantity, String barcode){
        SQLiteDatabase db = this.getReadableDatabase();
        String dateCreate = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
        String timeCreate = String.valueOf(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
        String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        ContentValues contentValues = new ContentValues();
        long result;
        boolean status = false;

        contentValues.put("quantity", quantity);
        contentValues.put("barcode", barcode);
        contentValues.put("date_create", dateCreate);
        contentValues.put("time_create", timeCreate);
        contentValues.put("priority", priority);

        result = db.update(TB_SUB_CATEGORY, contentValues, "sub_category_id=?", new String[] { subCategoryID });
        if(result != -1){
            status = true;

        }
        return status;
    }

    public boolean deleteSubCategory(List ids){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        boolean status = false;

        String args = TextUtils.join(", ", ids);
        result = db.delete(TB_SUB_CATEGORY,"sub_category_id IN (" + args + ")", null);
        if(result != -1)
            status = true;
        return status;

    }
    /**************************************************************************** subcategory move existed record to other category purpose*******************************************************************/
    public ArrayList<TakeActionObject> fetchAllExistedRecordFromOther(String categoryID, String barcode, String fileID, ArrayList<TakeActionObject> arrayList){
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "SELECT tb_export_sub_category.sub_category_id, tb_export_sub_category.barcode," +
                " tb_export_sub_category.quantity, tb_export_sub_category.date_create, tb_export_sub_category.time_create," +
                " tb_export_sub_category.category_id, tb_export_category.category_name FROM "+ TB_SUB_CATEGORY +
                " INNER JOIN " + TB_CATEGORY +
                " ON tb_export_sub_category.category_id = tb_export_category.category_id" +
                " WHERE tb_export_sub_category.category_id<>?" +
                " AND tb_export_sub_category.barcode=? AND tb_export_category.file_id=?" +
                " ORDER BY tb_export_sub_category.priority DESC";

        Cursor cursor = db.rawQuery(sql, new String[] { categoryID, barcode, fileID });

        while (cursor.moveToNext()) {
            arrayList.add(new TakeActionObject(
                    cursor.getString(cursor.getColumnIndex("sub_category_id")),
                    cursor.getString(cursor.getColumnIndex("barcode")),
                    cursor.getString(cursor.getColumnIndex("quantity")),
                    cursor.getString(cursor.getColumnIndex("date_create")),
                    cursor.getString(cursor.getColumnIndex("time_create")),
                    cursor.getString(cursor.getColumnIndex("category_id")),
                    cursor.getString(cursor.getColumnIndex("category_name"))));
        }
        cursor.close();
        db.close();
        return arrayList;

    }


    public boolean getMoveItemQuantity(String subCategoryID, int quantity){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean status = false;
        String sql = "SELECT quantity FROM "+ TB_SUB_CATEGORY +
                " WHERE sub_category_id=?";

        Cursor cursor = db.rawQuery(sql, new String[] { subCategoryID });
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            int newQuantity = cursor.getInt(cursor.getColumnIndex("quantity")) + quantity;
            boolean moveQuantity = updateMoveItemQuantity(subCategoryID, newQuantity);

            if(moveQuantity)
                status = true;

        }
        cursor.close();
        return status;
    }

    private boolean updateMoveItemQuantity(String subCategoryID, int quantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        long result;
        boolean status = false;

        contentValues.put("quantity", quantity);
        contentValues.put("priority", priority);
        result = db.update(TB_SUB_CATEGORY, contentValues, "sub_category_id=?", new String[] { subCategoryID });
        if(result != -1)
            status = true;
        return status;
    }

    /**************************************************************************** subcategory search purpose*******************************************************************/
    public ArrayList<SubCategoryObject> searchAllSubCategoryByQuery(String category_id, int page, ArrayList<SubCategoryObject> currentArrayList, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

        double start;
        double limit = 10.0;
        int totalSubCategoryRow = countSubCategoryRow(category_id);
        int page_limit = (int) Math.ceil(totalSubCategoryRow/limit);

        page_limit = (int) Math.ceil(page_limit);
        if(page <= page_limit) {
            start = (page - 1) * limit;

            String sql = "SELECT * FROM " + TB_SUB_CATEGORY +
                    " WHERE category_id=" + category_id + " AND (barcode LIKE '%" + keyword + "%')"+
                    " ORDER BY priority DESC" +
                    " LIMIT " + start + " , " + limit;

            Cursor crs = db.rawQuery(sql, null);


            while (crs.moveToNext()) {
                currentArrayList.add(new SubCategoryObject(
                        crs.getString(crs.getColumnIndex("sub_category_id")),
                        crs.getString(crs.getColumnIndex("barcode")),
                        crs.getString(crs.getColumnIndex("quantity")),
                        crs.getString(crs.getColumnIndex("date_create")),
                        crs.getString(crs.getColumnIndex("time_create"))));
            }
            crs.close();
        }
        db.close();

        return currentArrayList;
    }

    public String convertFileIntoString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TB_FILE;

        //            String file = null;
        StringBuilder sb = new StringBuilder();

        Cursor crs = db.rawQuery(sql, null);

        while (crs.moveToNext()) {
            sb.append(crs.getString(crs.getColumnIndex("file_name")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("created_at")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("file_id")));
            if(crs.getCount() != crs.getPosition()+1)
                sb.append(".");
        }
        crs.close();
        db.close();

        return sb.toString();
    }

    public String convertCategoryIntoString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TB_CATEGORY;

        //            String file = null;
        StringBuilder sb = new StringBuilder();

        Cursor crs = db.rawQuery(sql, null);

        while (crs.moveToNext()) {
            sb.append(crs.getString(crs.getColumnIndex("category_name")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("file_id")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("category_id")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("created_at")));
            if(crs.getCount() != crs.getPosition()+1)
                sb.append(".");
        }
        crs.close();
        db.close();

        return sb.toString();
    }

    public String convertSubCategoryIntoString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TB_SUB_CATEGORY;

        //            String file = null;
        StringBuilder sb = new StringBuilder();

        Cursor crs = db.rawQuery(sql, null);

        while (crs.moveToNext()) {
            sb.append(crs.getString(crs.getColumnIndex("barcode")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("quantity")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("category_id")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("date_create")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("time_create")));
            sb.append(",");
            sb.append(crs.getString(crs.getColumnIndex("priority")));

            if(crs.getCount() != crs.getPosition()+1)
                sb.append(".");
        }
        crs.close();
        db.close();

        return sb.toString();
    }
}
