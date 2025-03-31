package com.HZFinger_FpStdSample;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 人员信息数据库帮助类
 * 用于管理人员基本信息（编号、姓名、卡号、指纹信息、部门）
 */
public class PersonDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "PersonDatabaseHelper";
    
    // 数据库名称和版本
    private static final String DATABASE_NAME = "person_info.db";
    private static final int DATABASE_VERSION = 2;
    
    // 表名
    public static final String TABLE_PERSON = "person";
    
    // 列名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PERSON_ID = "person_id"; // 人员编号
    public static final String COLUMN_NAME = "name"; // 姓名
    public static final String COLUMN_CARD_NO = "card_no"; // 卡号
    public static final String COLUMN_DEPARTMENT = "department"; // 部门
    public static final String COLUMN_FINGERPRINT = "fingerprint"; // 指纹数据
    public static final String COLUMN_SIGNATURE = "signature"; // 签名数据
    
    // 创建表SQL语句
    private static final String CREATE_TABLE_PERSON = "CREATE TABLE " + TABLE_PERSON + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PERSON_ID + " TEXT NOT NULL, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_CARD_NO + " TEXT, " +
            COLUMN_DEPARTMENT + " TEXT, " +
            COLUMN_FINGERPRINT + " BLOB, " +
            COLUMN_SIGNATURE + " BLOB" +
            ");";
    
    private Context mContext;
    
    public PersonDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PERSON);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果数据库版本升级，可以在这里处理升级逻辑
        if (oldVersion < 2) {
            // 添加签名字段
            db.execSQL("ALTER TABLE " + TABLE_PERSON + " ADD COLUMN " + COLUMN_SIGNATURE + " BLOB");
        }
        // 简单处理：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
        onCreate(db);
    }

    public SQLiteDatabase getOpenDatabase() {
        return this.getWritableDatabase();
    }
    
    /**
     * 添加人员信息
     * @param personId 人员编号
     * @param name 姓名
     * @param cardNo 卡号
     * @param department 部门
     * @param fingerprint 指纹数据
     * @return 是否添加成功
     */
    public boolean addPerson(String personId, String name, String cardNo, String department, byte[] fingerprint, byte[] signature) {
        if (personId == null || personId.isEmpty() || name == null || name.isEmpty()) {
            return false;
        }
        
        // 检查是否已存在相同编号的人员
        if (isPersonExists(personId)) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PERSON_ID, personId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CARD_NO, cardNo);
        values.put(COLUMN_DEPARTMENT, department);
        values.put(COLUMN_FINGERPRINT, fingerprint);
        values.put(COLUMN_SIGNATURE, signature);
        
        long result = db.insert(TABLE_PERSON, null, values);
        db.close();
        
        return result != -1;
    }
    
    /**
     * 更新人员信息
     * @param personId 人员编号
     * @param name 姓名
     * @param cardNo 卡号
     * @param department 部门
     * @param fingerprint 指纹数据
     * @return 是否更新成功
     */
    public boolean updatePerson(String personId, String name, String cardNo, String department, byte[] fingerprint, byte[] signature) {
        if (personId == null || personId.isEmpty()) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        if (name != null && !name.isEmpty()) {
            values.put(COLUMN_NAME, name);
        }
        
        if (cardNo != null) {
            values.put(COLUMN_CARD_NO, cardNo);
        }
        
        if (department != null) {
            values.put(COLUMN_DEPARTMENT, department);
        }
        
        if (fingerprint != null) {
            values.put(COLUMN_FINGERPRINT, fingerprint);
        }
        
        if (signature != null) {
            values.put(COLUMN_SIGNATURE, signature);
        }
        
        int rowsAffected = db.update(TABLE_PERSON, values, COLUMN_PERSON_ID + " = ?", new String[]{personId});
        db.close();
        
        return rowsAffected > 0;
    }
    
    /**
     * 删除人员信息
     * @param personId 人员编号
     * @return 是否删除成功
     */
    public boolean deletePerson(String personId) {
        if (personId == null || personId.isEmpty()) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PERSON, COLUMN_PERSON_ID + " = ?", new String[]{personId});
        db.close();
        
        return rowsAffected > 0;
    }
    
    /**
     * 查询人员信息
     * @param personId 人员编号
     * @return 人员信息Cursor
     */
    public Cursor getPerson(String personId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PERSON, null, COLUMN_PERSON_ID + " = ?", new String[]{personId}, null, null, null);
    }
    
    /**
     * 获取所有人员信息
     * @return 所有人员信息Cursor
     */
    public Cursor getAllPersons() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PERSON, null, null, null, null, null, COLUMN_PERSON_ID + " ASC");
    }
    
    /**
     * 检查人员是否存在
     * @param personId 人员编号
     * @return 是否存在
     */
    public boolean isPersonExists(String personId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PERSON, new String[]{COLUMN_ID}, COLUMN_PERSON_ID + " = ?", new String[]{personId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    
    /**
     * 获取人员数量
     * @return 人员数量
     */
    public int getPersonCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PERSON, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * 清空人员表
     * @return 是否清空成功
     */
    public boolean clearAllPersons() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PERSON);
        db.close();
        return true;
    }
    
    /**
     * 导出数据库到U盘
     * @param exportPath 导出路径
     * @return 是否导出成功
     */
    public boolean exportDatabase(String exportPath) {
        try {
            File currentDB = mContext.getDatabasePath(DATABASE_NAME);
            File exportDir = new File(exportPath);
            
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File exportDB = new File(exportDir, DATABASE_NAME);
            
            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(exportDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "导出数据库失败", e);
        }
        return false;
    }
    
    /**
     * 从U盘导入数据库
     * @param importPath 导入路径
     * @return 是否导入成功
     */
    public boolean importDatabase(String importPath) {
        try {
            File importDB = new File(importPath, DATABASE_NAME);
            File currentDB = mContext.getDatabasePath(DATABASE_NAME);
            
            if (importDB.exists()) {
                // 关闭数据库连接
                close();
                
                FileChannel src = new FileInputStream(importDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "导入数据库失败", e);
        }
        return false;
    }
    
    /**
     * 根据指纹数据查找人员
     * @param fingerprint 指纹数据
     * @return 人员编号，如果未找到则返回null
     */
    @SuppressLint("Range")
    public String findPersonByFingerprint(byte[] fingerprint) {
        if (fingerprint == null) {
            return null;
        }
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PERSON, new String[]{COLUMN_PERSON_ID}, COLUMN_FINGERPRINT + " = ?", new String[]{new String(fingerprint)}, null, null, null);
        
        String personId = null;
        if (cursor.moveToFirst()) {
            personId = cursor.getString(cursor.getColumnIndex(COLUMN_PERSON_ID));
        }
        
        cursor.close();
        db.close();
        return personId;
    }

    public boolean updatePersonSignature(String personId, byte[] signature) {
        if (personId == null || personId.isEmpty() || signature == null) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SIGNATURE, signature);
        
        int rowsAffected = db.update(TABLE_PERSON, values, COLUMN_PERSON_ID + " = ?", new String[]{personId});
        db.close();
        
        return rowsAffected > 0;
    }
}