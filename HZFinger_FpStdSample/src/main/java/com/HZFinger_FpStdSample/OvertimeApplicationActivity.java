package com.HZFinger_FpStdSample;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HZFinger_FpStdSample.adapter.OvertimePersonAdapter;
import com.HZFinger_FpStdSample.model.OvertimeApplication;
import com.HZFinger_FpStdSample.model.OvertimePerson;
import com.HZFinger_FpStdSample.model.Person;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OvertimeApplicationActivity extends AppCompatActivity {

    private TextInputEditText etOvertimeDate;
    private TextInputEditText etStartTime;
    private TextInputEditText etEndTime;
    private TextInputEditText etOvertimeReason;
    private AutoCompleteTextView spDepartment;
    private Button btnAddPerson;
    private Button btnRemovePerson;
    private Button btnGenerateTemplate;
    private RecyclerView rvOvertimePersons;

    private OvertimePersonAdapter adapter;
    private OvertimeApplication overtimeApplication;
    private PersonDatabaseHelper dbHelper;
    private List<String> departmentList;
    private ArrayAdapter<String> departmentAdapter;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overtime_application);

        // 初始化数据库
        dbHelper = new PersonDatabaseHelper(this);

        // 初始化数据
        overtimeApplication = new OvertimeApplication();
        overtimeApplication.setOvertimeDate(new Date());

        // 初始化视图
        initViews();
        initData();
        initListeners();
    }

    private void initViews() {
        etOvertimeDate = findViewById(R.id.et_overtime_date);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        etOvertimeReason = findViewById(R.id.et_overtime_reason);
        spDepartment = findViewById(R.id.sp_department);
        btnAddPerson = findViewById(R.id.btn_add_person);
        btnRemovePerson = findViewById(R.id.btn_remove_person);
        btnGenerateTemplate = findViewById(R.id.btn_generate_template);
        rvOvertimePersons = findViewById(R.id.rv_overtime_persons);

        // 设置RecyclerView
        rvOvertimePersons.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OvertimePersonAdapter(new ArrayList<>());
        rvOvertimePersons.setAdapter(adapter);

        // 设置默认日期和时间
        etOvertimeDate.setText(dateFormat.format(overtimeApplication.getOvertimeDate()));
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        etStartTime.setText(timeFormat.format(calendar.getTime()));
        
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        etEndTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void initData() {
        // 初始化部门列表
        departmentList = new ArrayList<>();
        departmentList.add("管理部");
        departmentList.add("技术部");
        departmentList.add("销售部");
        departmentList.add("财务部");
        departmentList.add("人事部");
        
        departmentAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, departmentList);
        spDepartment.setAdapter(departmentAdapter);
        
        // 设置点击监听器，确保点击时显示下拉框
        spDepartment.setOnClickListener(v -> {
            spDepartment.showDropDown();
        });
    }

    private void initListeners() {
        // 日期选择
        etOvertimeDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        overtimeApplication.setOvertimeDate(selectedDate.getTime());
                        etOvertimeDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 开始时间选择
        etStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        overtimeApplication.setStartTime(timeFormat.format(selectedTime.getTime()));
                        etStartTime.setText(timeFormat.format(selectedTime.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // 结束时间选择
        etEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        overtimeApplication.setEndTime(timeFormat.format(selectedTime.getTime()));
                        etEndTime.setText(timeFormat.format(selectedTime.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // 部门选择
        spDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDepartment = (String) parent.getItemAtPosition(position);
            overtimeApplication.setDepartment(selectedDepartment);
        });

        // 添加人员
        btnAddPerson.setOnClickListener(v -> {
            showPersonSelectionDialog();
        });

        // 删除人员
        btnRemovePerson.setOnClickListener(v -> {
            if (adapter.getSelectedPersons().isEmpty()) {
                Toast.makeText(this, "请先选择要删除的人员", Toast.LENGTH_SHORT).show();
                return;
            }
            
            new AlertDialog.Builder(this)
                    .setTitle("删除人员")
                    .setMessage("确定要删除选中的人员吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        adapter.removeSelectedPersons();
                        Toast.makeText(this, "已删除选中人员", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 生成模板
        btnGenerateTemplate.setOnClickListener(v -> {
            if (validateForm()) {
                generateTemplate();
            }
        });
    }

        private boolean validateForm() {
        if (etOvertimeDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "请选择加班日期", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etStartTime.getText().toString().isEmpty()) {
            Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etEndTime.getText().toString().isEmpty()) {
            Toast.makeText(this, "请选择结束时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etOvertimeReason.getText().toString().isEmpty()) {
            Toast.makeText(this, "请填写加班原因", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (spDepartment.getText().toString().isEmpty()) {
            Toast.makeText(this, "请选择部门", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (adapter.getAllPersons().isEmpty()) {
            Toast.makeText(this, "请添加加班人员", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // 更新加班申请对象
        overtimeApplication.setReason(etOvertimeReason.getText().toString());
        overtimeApplication.setDepartment(spDepartment.getText().toString());
        
        return true;
    }
    
    private void showPersonSelectionDialog() {
        // 从数据库获取所有人员
        List<Person> allPersons = getAllPersonsFromDatabase();
        if (allPersons.isEmpty()) {
            Toast.makeText(this, "没有可添加的人员，请先在人员管理中添加人员", Toast.LENGTH_LONG).show();
            return;
        }
        
        // 创建已添加人员的ID列表，用于过滤
        List<String> addedPersonIds = new ArrayList<>();
        for (OvertimePerson person : adapter.getAllPersons()) {
            addedPersonIds.add(person.getPersonId());
        }
        
        // 过滤掉已添加的人员
        List<Person> availablePersons = new ArrayList<>();
        for (Person person : allPersons) {
            if (!addedPersonIds.contains(person.getId())) {
                availablePersons.add(person);
            }
        }
        
        if (availablePersons.isEmpty()) {
            Toast.makeText(this, "所有人员已添加", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建人员选择对话框
        String[] personNames = new String[availablePersons.size()];
        for (int i = 0; i < availablePersons.size(); i++) {
            Person person = availablePersons.get(i);
            personNames[i] = person.getName() + " (" + person.getId() + ")";
        }
        
        boolean[] checkedItems = new boolean[personNames.length];
        List<Integer> selectedPositions = new ArrayList<>();
        
        new AlertDialog.Builder(this)
                .setTitle("选择加班人员")
                .setMultiChoiceItems(personNames, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedPositions.add(which);
                    } else {
                        selectedPositions.remove(Integer.valueOf(which));
                    }
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    for (Integer position : selectedPositions) {
                        Person selectedPerson = availablePersons.get(position);
                        OvertimePerson overtimePerson = new OvertimePerson(
                                selectedPerson.getId(),
                                selectedPerson.getName(),
                                selectedPerson.getDepartment()
                        );
                        adapter.addPerson(overtimePerson);
                    }
                    
                    if (!selectedPositions.isEmpty()) {
                        Toast.makeText(this, "已添加 " + selectedPositions.size() + " 名人员", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private List<Person> getAllPersonsFromDatabase() {
        List<Person> personList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllPersons();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String personId = cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_PERSON_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_NAME));
                @SuppressLint("Range") String department = cursor.getString(cursor.getColumnIndex(PersonDatabaseHelper.COLUMN_DEPARTMENT));
                
                Person person = new Person();
                person.setId(personId);
                person.setName(name);
                person.setDepartment(department);
                
                personList.add(person);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        return personList;
    }

    private void generateTemplate() {
        try {
            // 创建文件名 - 使用当前日期和时间作为文件名后缀
            SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "加班申请_" + fileNameFormat.format(new Date()) + ".docx";

            // 获取外部存储目录
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            // 模板文件路径 - 假设模板文件放在应用的assets目录下
            String templatePath = Environment.getExternalStorageDirectory() + "/ZF加班申请模板.docx";
            File templateFile = new File(templatePath);

            if (!templateFile.exists()) {
                Toast.makeText(this, "找不到模板文件: " + templatePath, Toast.LENGTH_LONG).show();
                return;
            }

            // 输出文件路径
            File outputFile = new File(documentsDir, fileName);

            // 加载Word文档
            FileInputStream fis = new FileInputStream(templateFile);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            // 获取当前日期
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // 月份从0开始
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // 解析开始和结束时间
            String startTime = etStartTime.getText().toString();
            String endTime = etEndTime.getText().toString();
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            // 获取加班日期
            Date overtimeDate = overtimeApplication.getOvertimeDate();
            Calendar otCalendar = Calendar.getInstance();
            otCalendar.setTime(overtimeDate);
            int otYear = otCalendar.get(Calendar.YEAR);
            int otMonth = otCalendar.get(Calendar.MONTH) + 1;
            int otDay = otCalendar.get(Calendar.DAY_OF_MONTH);

            // 遍历文档中的段落，替换占位符
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        // 替换申请人信息（这里假设从第一个加班人员中获取）
                        if (!adapter.getAllPersons().isEmpty()) {
                            OvertimePerson firstPerson = adapter.getAllPersons().get(0);
                            text = text.replace("JQ", firstPerson.getName());
                        }

                        // 替换日期和时间
                        text = text.replace("2025_年", otYear + " 年");
                        text = text.replace("__月", otMonth + " 月");
                        text = text.replace("__日", otDay + " 日");
                        text = text.replace("__时", startParts[0] + " 时");
                        text = text.replace("__分", startParts[1] + " 分");
                        text = text.replace("2025_年", otYear + " 年");
                        text = text.replace("__月", otMonth + " 月");
                        text = text.replace("__日", otDay + " 日");
                        text = text.replace("__时", endParts[0] + " 时");
                        text = text.replace("__分", endParts[1] + " 分");

                        // 替换当前日期
                        text = text.replace("20__年", year + " 年");
                        text = text.replace("__月", month + " 月");
                        text = text.replace("__日", day + " 日");

                        // 替换加班原因
                        text = text.replace("GZ", etOvertimeReason.getText().toString());

                        run.setText(text, 0);
                    }
                }
            }

            // 处理表格 - 填充加班人员信息
            List<XWPFTable> tables = document.getTables();
            if (!tables.isEmpty()) {
                XWPFTable table = tables.get(0); // 假设第一个表格是人员表格

                // 获取所有加班人员
                List<OvertimePerson> persons = adapter.getAllPersons();

                // 表格已有的行数（包括表头）
                int rowCount = table.getRows().size();

                // 从第二行开始填充数据（第一行是表头）
                for (int i = 0; i < persons.size(); i++) {
                    OvertimePerson person = persons.get(i);

                    // 如果需要添加新行
                    if (i + 2 > rowCount) {
                        table.createRow();
                    }

                    // 获取当前行
                    XWPFTableRow row = table.getRow(i + 1);

                    // 设置序号
                    if (row.getCell(0) != null) {
                        row.getCell(0).setText(String.valueOf(i + 1));
                    }

                    // 设置编号
                    if (row.getCell(1) != null) {
                        row.getCell(1).setText(person.getPersonId());
                    }

                    // 设置姓名
                    if (row.getCell(2) != null) {
                        row.getCell(2).setText(person.getName());
                    }

                    // 设置签字（留空，由用户手动签字）
                    if (row.getCell(3) != null) {
                        row.getCell(3).setText(person.getName() + "签字");
                    }

                    // 如果是双列表格，处理右侧列
                    if (i + persons.size() < persons.size() * 2 && i + persons.size() < persons.size()) {
                        OvertimePerson rightPerson = persons.get(i + persons.size());

                        // 设置右侧序号
                        if (row.getCell(4) != null) {
                            row.getCell(4).setText(String.valueOf(i + persons.size() + 1));
                        }

                        // 设置右侧编号
                        if (row.getCell(5) != null) {
                            row.getCell(5).setText(rightPerson.getPersonId());
                        }

                        // 设置右侧姓名
                        if (row.getCell(6) != null) {
                            row.getCell(6).setText(rightPerson.getName());
                        }

                        // 设置右侧签字
                        if (row.getCell(7) != null) {
                            row.getCell(7).setText(rightPerson.getName() + "签字");
                        }
                    }
                }

                // 更新参与人数
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    List<XWPFRun> runs = paragraph.getRuns();
                    for (XWPFRun run : runs) {
                        String text = run.getText(0);
                        if (text != null && text.contains("100")) {
                            text = text.replace("100", String.valueOf(persons.size()));
                            run.setText(text, 0);
                        }
                    }
                }
            }

            // 保存文档
            FileOutputStream fos = new FileOutputStream(outputFile);
            document.write(fos);
            fos.close();

            Toast.makeText(this, "加班申请文件已保存至: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "生成模板失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}