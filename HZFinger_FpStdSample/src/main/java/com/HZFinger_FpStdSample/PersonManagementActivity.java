package com.HZFinger_FpStdSample;

import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_CHKLIVE_DISABLE;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_ENABLE_BTN;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_ID_ENABLED;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_ID_SETTEXT;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_SHOW_BITMAP;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_SHOW_IMAGE;
import static com.HZFinger_FpStdSample.HZFinger_FpStdSample.MESSAGE_SHOW_TEXT;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_CARD_NO;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_DEPARTMENT;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_FINGERPRINT;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_NAME;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_PERSON_ID;
import static com.HZFinger_FpStdSample.PersonDatabaseHelper.COLUMN_SIGNATURE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HZFINGER.HAPI;
import com.HZFINGER.HostUsb;
import com.HZFINGER.LAPI;
import com.HZFinger_FpStdSample.adapter.PersonAdapter;
import com.HZFinger_FpStdSample.model.Person;
import com.HZFinger_FpStdSample.view.SignatureView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 人员信息管理Activity
 * 用于管理人员基本信息（编号、姓名、卡号、指纹信息、部门）
 */
public class PersonManagementActivity extends Activity {
    private static final String TAG = "PersonManagementActivity";

    // UI组件
    private EditText etPersonId;
    private EditText etName;
    private EditText etCardNo;
    private AutoCompleteTextView spDepartment;
    private TextView tvFingerprint;
    private ImageView ivFingerprint;
    private Button btnCapture;
   /* private Button btnExport;
    private Button btnImport;*/
    private Button btnSign;
    private ImageView ivSignature;
    private TextView tvSignature;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    private RecyclerView rvPersonList;
    private PersonAdapter adapter;
    private LinearLayout formContainer;

    private SQLiteDatabase person_db;

    private boolean isEditMode = false;

    private HostUsb mHostUSb = null;

    private boolean commFlag = true;

    private boolean colorFlag = true;

    // 数据
    private PersonDatabaseHelper dbHelper;
    private List<String> personList;
    private ArrayAdapter<String> personAdapter;
    private List<String> departmentList;
    private ArrayAdapter<String> departmentAdapter;
    private String currentPersonId = "";
    private byte[] currentFingerprint = null;

    private volatile boolean bContinue = false;

    // 指纹相关
    private HAPI m_cHAPI = null;
    private LAPI m_cLAPI = null;
    private long m_hDevice = 0;
    private byte[] m_image = new byte[LAPI.WIDTH * LAPI.HEIGHT];
    private int[] RGBbits = new int[LAPI.WIDTH * LAPI.HEIGHT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_management);

        // 初始化数据库
        if(dbHelper == null) {
            dbHelper = new PersonDatabaseHelper(this);
            person_db = dbHelper.getOpenDatabase();
        }

        // 初始化列表
        rvPersonList = findViewById(R.id.rv_person_list);
        rvPersonList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PersonAdapter(getSampleData());
        rvPersonList.setAdapter(adapter);

        // 初始化API
        m_cLAPI = new LAPI(this);
        m_cHAPI = new HAPI(this,m_fpsdkHandle);
        mHostUSb = new HostUsb(this);

        // 初始化UI组件
        initViews();

        // 初始化数据
        initData();

        // 初始化事件监听
        initListeners();

        // 初始化NFC适配器
        initNFC();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_cHAPI.CloseDevice();

        // 只在应用退出时关闭数据库
        if (person_db != null && dbHelper != null) {
            person_db.close();
            dbHelper.close();
        }
    }

    /**
     * 初始化UI组件
     */
    private void initViews() {
        etPersonId = findViewById(R.id.et_person_id);
        etName = findViewById(R.id.et_name);
        etCardNo = findViewById(R.id.et_card_no);
        spDepartment = findViewById(R.id.sp_department);
        tvFingerprint = findViewById(R.id.tv_fingerprint);
        ivFingerprint = findViewById(R.id.iv_fingerprint);
  /*      btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);*/
        btnCapture = findViewById(R.id.btn_capture);
        btnSign = findViewById(R.id.btn_sign);
        ivSignature = findViewById(R.id.iv_signature);
        tvSignature = findViewById(R.id.tv_signature);
        formContainer = findViewById(R.id.form_container);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化部门列表
        departmentList = new ArrayList<>();
        departmentList.add("管理部");
        departmentList.add("技术部");
        departmentList.add("销售部");
        departmentList.add("财务部");
        departmentList.add("人事部");
        departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departmentList);
//        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartment.setAdapter(departmentAdapter);

        // 设置点击监听器，确保点击时显示下拉框
        spDepartment.setOnClickListener(v -> {
            spDepartment.showDropDown();
        });
    }

    /**
     * 初始化事件监听
     */
    private void initListeners() {

        // 采集指纹按钮
        btnCapture.setOnClickListener(v -> {
            if (bContinue) {
                bContinue = false;
                //btnGetImage.setText("GetImage");
                m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "取消"));
                return;
            }
            btnCapture.setText(R.string.TEXT_STOP);
            bContinue = true;
            btnCapture.setEnabled(false);
            Runnable r = () -> {
                OPEN_DEVICE();
                GET_IMAGE();
            };
            Thread s = new Thread(r);
            s.start();
        });

        // 导出按钮
        /*btnExport.setOnClickListener(v -> exportDatabase());*/

        // 导入按钮
        /*btnImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                importDatabase();
            }
        });*/

        btnSign.setOnClickListener(v -> {
            // 启动签名Activity或显示签名对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_signature, null);
            SignatureView signatureView = dialogView.findViewById(R.id.signature_view);
            Button btnClear = dialogView.findViewById(R.id.btn_clear_sign);
            Button btnSave = dialogView.findViewById(R.id.btn_save_sign);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            btnClear.setOnClickListener(v1 -> signatureView.clear());
            btnSave.setOnClickListener(v1 -> {
                Bitmap signatureBitmap = getSignatureBitmap(signatureView);
                ivSignature.setImageBitmap(signatureBitmap);
                tvSignature.setText("已签字");
                dialog.dismiss();
            });

            dialog.show();
        });

        // 按钮事件
        findViewById(R.id.btn_add).setOnClickListener(v -> showForm(false));
        findViewById(R.id.btn_edit).setOnClickListener(v -> showForm(true));
        findViewById(R.id.btn_delete).setOnClickListener(v -> deleteSelected());
        findViewById(R.id.btn_save).setOnClickListener(v -> savePerson());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> hideForm());

        adapter.setOnItemClickListener(position -> {
            // 可以在这里添加额外的点击处理逻辑
        });

        // 修改部门选择的监听器
        spDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDepartment = (String) parent.getItemAtPosition(position);
            spDepartment.setText(selectedDepartment);
        });
        // 人员列表点击事件
//        lvPersonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String personId = personList.get(position);
//                loadPerson(personId);
//            }
//        });
    }

    /**
     * 初始化NFC设备
     */
    private void initNFC(){
        // 初始化NFC适配器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建PendingIntent
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getAction() != null){
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    String cardId = bytesToHex(tag.getId());
                    etCardNo.setText(cardId);
                }
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }


    private List<Person> getSampleData() {
        // 返回示例数据
        List<Person> persons = new ArrayList<>();
        try (Cursor cursor = dbHelper.getAllPersons()) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_PERSON_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int deptIndex = cursor.getColumnIndex(COLUMN_DEPARTMENT);
                int cardIndex = cursor.getColumnIndex(COLUMN_CARD_NO);
                do {
                    Person person = new Person();
                    person.setId(cursor.getString(idIndex));
                    person.setName(cursor.getString(nameIndex));
                    person.setDepartment(cursor.getString(deptIndex));
                    person.setCardNo(cursor.getString(cardIndex));
                    persons.add(person);
                } while (cursor.moveToNext());
            }
        }
        return persons;
    }

    private void showForm(boolean isEdit) {
        isEditMode = isEdit;

        // 获取标题视图（假设它是父布局的第一个子视图）
        View titleView = ((ViewGroup)formContainer.getParent()).getChildAt(0);

        // 隐藏标题，这样表单就会直接显示在顶部
        titleView.setVisibility(View.GONE);

        // 显示表单，隐藏列表
        formContainer.setVisibility(View.VISIBLE);
        rvPersonList.setVisibility(View.GONE);

        // 隐藏操作按钮区域和搜索框
        findViewById(R.id.btn_add).setVisibility(View.GONE);
        findViewById(R.id.btn_edit).setVisibility(View.GONE);
        findViewById(R.id.btn_delete).setVisibility(View.GONE);
        findViewById(R.id.et_search).setVisibility(View.GONE);

        // 确保下拉框适配器正确设置
        if (departmentAdapter != null) {
            spDepartment.setAdapter(departmentAdapter);
        }

        if (isEdit) {
            // 获取选中的person数据并填充表单
            Person selected = adapter.getSelectedPerson();
            if (selected != null) {
                // 根据人员编号查询去库中查询详细数据
                String id = selected.getId();
                Cursor cursor = dbHelper.getPerson(id);
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(COLUMN_PERSON_ID);
                    int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                    int deptIndex = cursor.getColumnIndex(COLUMN_DEPARTMENT);
                    int cardIndex = cursor.getColumnIndex(COLUMN_CARD_NO);
                    int signatureIndex = cursor.getColumnIndex(COLUMN_SIGNATURE);
                    int fingerprintIndex = cursor.getColumnIndex(COLUMN_FINGERPRINT);
                    do {
                        etPersonId.setText(cursor.getString(idIndex));
                        etName.setText(cursor.getString(nameIndex));
                        spDepartment.setText(cursor.getString(deptIndex));
                        etCardNo.setText(cursor.getString(cardIndex));
                        etCardNo.setText(cursor.getString(signatureIndex));
                        etCardNo.setText(cursor.getString(fingerprintIndex));
                    } while (cursor.moveToNext());
                }
            }
        } else {
            // 清空表单
            clearForm();
        }
    }

    private void hideForm() {
        // 获取标题视图并恢复显示
        View titleView = ((ViewGroup)formContainer.getParent()).getChildAt(0);
        titleView.setVisibility(View.VISIBLE);

        // 重置表单数据（包括签名和指纹）
        clearForm();  // 调用现有的清空方法
        formContainer.setVisibility(View.GONE);
        rvPersonList.setVisibility(View.VISIBLE);

        // 恢复顶部按钮显示
        findViewById(R.id.btn_add).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
        findViewById(R.id.et_search).setVisibility(View.VISIBLE);
    }

    private void savePerson() {
        // 获取表单数据
        String id = etPersonId.getText().toString();
        String name = etName.getText().toString();
        String dept = spDepartment.getText().toString();
        String cardNo = etCardNo.getText().toString();

        if (isEditMode) {
            // 更新操作
            adapter.updatePerson(new Person(id, name, dept, cardNo,null,null));
        } else {
            // 新增操作
            adapter.addPerson(new Person(id, name, dept, cardNo,null,null));
        }
        hideForm();
    }

    private void deleteSelected() {
        adapter.deleteSelectedPerson();
    }

    /**
     * 指纹SDK消息处理Handler
     */
    @SuppressLint("HandlerLeak")
    private Handler m_fpsdkHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TEXT:
                    tvFingerprint.setText((String)msg.obj);
                    break;
                case MESSAGE_ID_ENABLED:
                    Button btn = (Button) findViewById(msg.arg1);
                    if (msg.arg2 != 0) btn.setEnabled(true);
                    else btn.setEnabled(false);
                    break;
                case HAPI.MSG_SHOW_TEXT:
                    String text = (String) msg.obj;
                    Toast.makeText(PersonManagementActivity.this, text, Toast.LENGTH_SHORT).show();
                    break;
                case HAPI.MSG_FINGER_CAPTURED:
                    // 显示指纹图像
                    byte[] image = (byte[]) msg.obj;
                    displayFingerprintImage(image);
                    break;
                case MESSAGE_SHOW_IMAGE:
                    ShowFingerBitmap ((byte[])msg.obj,msg.arg1,msg.arg2);
                    break;
                case HAPI.MSG_CREATED_TEMPLATE:
                    // 保存指纹模板
                    currentFingerprint = (byte[]) msg.obj;
                    tvFingerprint.setText("已采集指纹");
                    Toast.makeText(PersonManagementActivity.this, "指纹采集成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler m_appHandle = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_IMAGE:
                    ShowFingerBitmap ((byte[])msg.obj,msg.arg1,msg.arg2);
                    break;
                case MESSAGE_ENABLE_BTN:
                    boolean bEnable = msg.arg1 == 1 ? true : false;
                    boolean bOpen = msg.arg2 == 1 ? true : false;
                    TextView id = (TextView) findViewById(R.id.idText);
                    id.setEnabled(bEnable);
                    Button btn = (Button) findViewById(R.id.btnVerify);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnSearch);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnDBRefresh);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnRCDelete);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnDBClear);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnEnroll);
                    btn.setEnabled(bEnable);
                    btn = (Button) findViewById(R.id.btnOpenDevice);
                    btn.setEnabled(bOpen);
                    break;
                case MESSAGE_SHOW_BITMAP:
                    ivFingerprint.setImageBitmap((Bitmap)msg.obj);
                    break;
                case MESSAGE_ID_ENABLED:
                    btn = (Button) findViewById(msg.arg1);
                    if (msg.arg2 != 0) btn.setEnabled(true);
                    else btn.setEnabled(false);
                    break;
                case MESSAGE_ID_SETTEXT:
                    btn = (Button) findViewById(msg.arg1);
                    btn.setText(msg.arg2);
                    break;
            }
        }
    };

    /**
     * 显示指纹图像
     *
     * @param image 指纹图像数据
     */
    private void displayFingerprintImage(byte[] image) {
        if (image == null) return;

        // 将指纹图像数据转换为Bitmap显示
        int[] RGBbits = new int[LAPI.WIDTH * LAPI.HEIGHT];
        for (int i = 0; i < LAPI.WIDTH * LAPI.HEIGHT; i++) {
            int v = image[i] & 0xFF;
            RGBbits[i] = Color.rgb(v, v, v);
        }

        Bitmap bm = Bitmap.createBitmap(RGBbits, LAPI.WIDTH, LAPI.HEIGHT, Config.RGB_565);
        ivFingerprint.setImageBitmap(bm);
    }

    /**
     * 刷新人员列表
     */
    /*private void refreshPersonList() {
        personList.clear();
        Cursor cursor = dbHelper.getAllPersons();
        if (cursor.moveToFirst()) {
            do {
                String personId = cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_PERSON_ID));
                personList.add(personId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (personAdapter != null) {
            personAdapter.notifyDataSetChanged();
        }
    }*/

    /**
     * 加载人员信息
     *
     * @param personId 人员编号
     *//*
    private void loadPerson(String personId) {
        currentPersonId = personId;
        Cursor cursor = dbHelper.getPerson(personId);
        if (cursor.moveToFirst()) {
            etPersonId.setText(cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_PERSON_ID)));
            etName.setText(cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_NAME)));
            etCardNo.setText(cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_CARD_NO)));

            String department = cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_DEPARTMENT));
            for (int i = 0; i < departmentList.size(); i++) {
                if (departmentList.get(i).equals(department)) {
                    spDepartment.setSelection(i);
                    break;
                }
            }

            currentFingerprint = cursor.getBlob(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_FINGERPRINT));
            if (currentFingerprint != null) {
                tvFingerprint.setText("已采集指纹");
            } else {
                tvFingerprint.setText("未采集指纹");
            }
        }
        cursor.close();
    }*/

    /**
     * 保存人员信息
     */
    /*private void savePerson() {
        String personId = etPersonId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String cardNo = etCardNo.getText().toString().trim();
        String department = spDepartment.getSelectedItem().toString();

        if (personId.isEmpty()) {
            Toast.makeText(this, "请输入人员编号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean result;
        if (currentPersonId.isEmpty() || !currentPersonId.equals(personId)) {
            // 新增
            result = dbHelper.addPerson(personId, name, cardNo, department, currentFingerprint);
            if (result) {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                currentPersonId = personId;
                refreshPersonList();
                clearForm();
            } else {
                Toast.makeText(this, "添加失败，可能编号已存在", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 更新
            result = dbHelper.updatePerson(personId, name, cardNo, department, currentFingerprint);
            if (result) {
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                refreshPersonList();
            } else {
                Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /**
     * 删除人员信息
     */
    /*private void deletePerson() {
        final String personId = etPersonId.getText().toString().trim();
        if (personId.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的人员", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("确定要删除该人员信息吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean result = dbHelper.deletePerson(personId);
                        if (result) {
                            Toast.makeText(PersonManagementActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            refreshPersonList();
                            clearForm();
                        } else {
                            Toast.makeText(PersonManagementActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }*/

    /**
     * 清空表单
     */
    private void clearForm() {
        // 清空基础字段
        etPersonId.setText("");
        etName.setText("");
        etCardNo.setText("");
        spDepartment.setText("");

        // 重置指纹状态
        tvFingerprint.setText("未采集指纹");
        ivFingerprint.setImageBitmap(null);

        // 重置签名状态
        tvSignature.setText("未签字");
        ivSignature.setImageBitmap(null);
    }

    /**
     * 导出数据库到U盘
     */
    private void exportDatabase() {
        // 检查外部存储是否可用
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取U盘路径 - 使用兼容API级别12的方式
        File usbDrive = null;

        // 在API 19以下，使用Environment获取外部存储路径
        File externalStorage = Environment.getExternalStorageDirectory();
        File[] possibleUsbDrives = new File("/storage").listFiles();

        if (possibleUsbDrives != null) {
            // 查找可能的U盘路径
            for (File drive : possibleUsbDrives) {
                if (drive.isDirectory() && !drive.getAbsolutePath().equals(externalStorage.getAbsolutePath())
                        && drive.canRead() && drive.canWrite()) {
                    usbDrive = drive;
                    break;
                }
            }
        }

        if (usbDrive == null) {
            Toast.makeText(this, "未检测到U盘，请插入U盘后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建导出目录
        File exportDir = new File(usbDrive, "HZFinger_Export");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // 导出数据库
        boolean result = dbHelper.exportDatabase(exportDir.getAbsolutePath());
        if (result) {
            Toast.makeText(this, "导出成功：" + exportDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从U盘导入数据库
     */
    /*private void importDatabase() {
        // 检查外部存储是否可用
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "外部存储不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取U盘路径 - 使用兼容API级别12的方式
        File usbDrive = null;

        // 在API 19以下，使用Environment获取外部存储路径
        File externalStorage = Environment.getExternalStorageDirectory();
        File[] possibleUsbDrives = new File("/storage").listFiles();

        if (possibleUsbDrives != null) {
            // 查找可能的U盘路径
            for (File drive : possibleUsbDrives) {
                if (drive.isDirectory() && !drive.getAbsolutePath().equals(externalStorage.getAbsolutePath())
                        && drive.canRead() && drive.canWrite()) {
                    usbDrive = drive;
                    break;
                }
            }
        }

        if (usbDrive == null) {
            Toast.makeText(this, "未检测到U盘，请插入U盘后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查导入目录
        final File importDir = new File(usbDrive, "HZFinger_Export");
        if (!importDir.exists()) {
            Toast.makeText(this, "未找到导入文件，请确认U盘中存在导出的数据", Toast.LENGTH_SHORT).show();
            return;
        }

        // 导入前确认
        new AlertDialog.Builder(this)
                .setTitle("导入确认")
                .setMessage("导入将覆盖当前数据，确定要继续吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 导入数据库
                        boolean result = dbHelper.importDatabase(importDir.getAbsolutePath());
                        if (result) {
                            Toast.makeText(PersonManagementActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                            // 重新加载数据
                            refreshPersonList();
                            clearForm();
                        } else {
                            Toast.makeText(PersonManagementActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }*/

    /**
    * 指纹采集
    */

    protected void GET_IMAGE() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EnableAllButtons(true);
            }
        });

        m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_ID_ENABLED, R.id.btn_capture, 1));
        //String msg;
        m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "请按手指"));
        //m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_IMAGE, LAPI.WIDTH, LAPI.HEIGHT, null).sendToTarget();
        int secLevel = 3;
        while (bContinue) {
            int ret = m_cLAPI.GetImage(m_hDevice, m_image);
            if (ret == LAPI.NOTCALIBRATED) {
                m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "错误: 指纹采集器没有校正").sendToTarget();
                break;
            }
            if (ret != LAPI.TRUE) {
                m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "错误: 无法采集指纹图像").sendToTarget();
                break;
            }
            ret = m_cLAPI.IsPressFingerEx(m_hDevice, m_image, true, LAPI.LIVECHECK_THESHOLD[secLevel - 1]);
            if (ret >= LAPI.DEF_FINGER_SCORE) {
                m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "采集图像() = 成功").sendToTarget();
                m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_IMAGE, LAPI.WIDTH, LAPI.HEIGHT, m_image));
                break;
            } else if (ret == LAPI.FAKEFINGER) {
                m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, "警告：假指纹 !").sendToTarget();
                SLEEP(500);
            }
        }
        bContinue = false;
        m_appHandle.obtainMessage(MESSAGE_ID_SETTEXT, R.id.btn_capture, R.string.TEXT_GET_IMAGE).sendToTarget();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EnableAllButtons(false);
            }
        });
//        if (DEBUG) {
//            Date date = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//            String filename = dateFormat.format(date) + ".bmp";
//            SaveAsBmpFile(m_image, LAPI.WIDTH, LAPI.HEIGHT, filename);
//
//            filename = dateFormat.format(date) + ".png";
//            SaveAsPngFile(m_image, LAPI.WIDTH, LAPI.HEIGHT, filename);
//
//            long wsqSize = m_cLAPI.CompressToWSQImage(m_hDevice, m_image, bfwsq);
//            if (wsqSize > 0) {
//                filename = dateFormat.format(date) + ".wsq";
//                SaveAsFile(filename, bfwsq, (int) wsqSize);
//            }
//        }
    }

    /**
     * 初始化指纹设备
     */
    protected void OPEN_DEVICE() {
        String msg = "正在打开指纹设备...";
        m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, msg));
        UsbDevice dev = mHostUSb.hasDeviceOpen();
        if (dev != null) {
            m_cLAPI.setHostUsb(mHostUSb);
            mHostUSb.AuthorizeDevice(dev);
        }
        if (commFlag) m_hDevice = m_cLAPI.OpenDeviceEx(LAPI.SCSI_MODE);
        else m_hDevice = m_cLAPI.OpenDeviceEx(LAPI.SPI_MODE);
        if (m_hDevice == 0) {
            msg = "无法打开设备";
        } else {
            if (LAPI.bInitNetManager) msg = "打开设备成功！";
            else {
                msg = "打开指纹设备成功！";
                m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_CHKLIVE_DISABLE, 0, 0, 0));
            }
        }
        m_cHAPI.m_hDev = m_hDevice;
        m_fpsdkHandle.sendMessage(m_fpsdkHandle.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, msg));
    }

    protected void SLEEP (int waittime)
    {
        int startTime, passTime = 0;
        startTime = (int)System.currentTimeMillis();
        while (passTime < waittime) {
            passTime = (int)System.currentTimeMillis();
            passTime = passTime - startTime;
        }
    }

    private void ShowFingerBitmap(byte[] image, int width, int height) {
        if (width==0) return;
        if (height==0) return;
        for (int i = 0; i < width * height; i++ ) {
            int v;
            if (image != null) v = image[i] & 0xff;
            else v = 255;

            if (colorFlag) RGBbits[i] = Color.rgb(v, v, v);
            else {
                if (v < 150) RGBbits[i] = Color.rgb(255, 0, 0);
                else RGBbits[i] = Color.rgb(255, 255, 255);
            }
        }
        Bitmap bmp = Bitmap.createBitmap(RGBbits, width, height,Config.RGB_565);
        ivFingerprint.setImageBitmap(bmp);
    }

    public void EnableAllButtons(boolean bOther)
    {
       if (bOther){
           etPersonId.setEnabled(false);
           etName.setEnabled(false);
           etCardNo.setEnabled(false);
           spDepartment.setEnabled(false);
       }else {
           etPersonId.setEnabled(true);
           etName.setEnabled(true);
           etCardNo.setEnabled(true);
           spDepartment.setEnabled(true);
       }
    }

    private Bitmap getSignatureBitmap(SignatureView signatureView) {
        Bitmap bitmap = Bitmap.createBitmap(signatureView.getWidth(),
                signatureView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        signatureView.draw(canvas);
        return bitmap;
    }


}