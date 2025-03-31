package com.HZFinger_FpStdSample.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HZFinger_FpStdSample.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.util.ArrayList;
import java.util.List;

public class OvertimeApplyActivity extends AppCompatActivity {

    // 数据模型
    public static class OvertimePerson {
        private String name;
        private String department;
        private float hours;

        // 构造函数、getter/setter省略...
    }

    private List<OvertimePerson> personList = new ArrayList<>();
    private OvertimePersonAdapter adapter;
    private MaterialAutoCompleteTextView actvDepartment;
    private TextInputEditText etStartTime, etEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overtime_application);

        // 初始化视图
        initViews();

        // 设置部门下拉框
        setupDepartmentSpinner();

        // 设置时间选择器
        setupTimePickers();

        // 初始化RecyclerView
        setupRecyclerView();

        // 按钮事件绑定
        setupButtonActions();
    }

    private void initViews() {
        actvDepartment = findViewById(R.id.actvDepartment);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
    }

    private void setupDepartmentSpinner() {
        String[] departments = {"技术部", "市场部", "人事部", "财务部"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_popup_item,
                departments
        );
        actvDepartment.setAdapter(adapter);
    }

    private void setupTimePickers() {
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择开始时间")
                .build();

        MaterialTimePicker startTimePicker = new MaterialTimePicker.Builder()
                .setTitleText("选择开始时间")
                .build();

        // 类似设置结束时间选择器...

        etStartTime.setOnClickListener(v -> showDateTimePicker(true));
        etEndTime.setOnClickListener(v -> showDateTimePicker(false));
    }

    private void setupRecyclerView() {
        adapter = new OvertimePersonAdapter(personList);
        RecyclerView rvPersonList = findViewById(R.id.rvPersonList);
        rvPersonList.setLayoutManager(new LinearLayoutManager(this));
        rvPersonList.setAdapter(adapter);
    }

    private void setupButtonActions() {
        findViewById(R.id.btnAddPerson).setOnClickListener(v -> showAddPersonDialog());
        findViewById(R.id.btnRemovePerson).setOnClickListener(v -> removeSelectedPerson());
        findViewById(R.id.btnGenerateTemplate).setOnClickListener(v -> generateTemplateFile());
    }

    private void showAddPersonDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_person, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etHours = dialogView.findViewById(R.id.etHours);

        builder.setTitle("添加加班人员")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString();
                    float hours = Float.parseFloat(etHours.getText().toString());
                    String department = actvDepartment.getText().toString();

                    if (!name.isEmpty()) {
                        personList.add(new OvertimePerson(name, department, hours));
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void removeSelectedPerson() {
        if (adapter.getSelectedPosition() != -1) {
            personList.remove(adapter.getSelectedPosition());
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "请先选择要删除的人员", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateTemplateFile() {
        // 实现生成ZIP模板文件的逻辑
        // 可以使用之前讨论的Zip4j库实现加密压缩
    }

    // RecyclerView适配器
    private static class OvertimePersonAdapter extends RecyclerView.Adapter<OvertimePersonAdapter.ViewHolder> {
        private List<OvertimePerson> personList;
        private int selectedPosition = -1;

        // 实现ViewHolder和适配器方法...
    }
}