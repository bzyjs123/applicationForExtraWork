package com.HZFinger_FpStdSample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HZFinger_FpStdSample.R;
import com.HZFinger_FpStdSample.model.OvertimePerson;

import java.util.ArrayList;
import java.util.List;

public class OvertimePersonAdapter extends RecyclerView.Adapter<OvertimePersonAdapter.ViewHolder> {

    private List<OvertimePerson> personList;
    private List<OvertimePerson> selectedPersons;

    public OvertimePersonAdapter(List<OvertimePerson> personList) {
        this.personList = personList;
        this.selectedPersons = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overtime_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OvertimePerson person = personList.get(position);
        holder.tvPersonId.setText(person.getPersonId());
        holder.tvPersonName.setText(person.getName());
        holder.tvDepartment.setText(person.getDepartment());
        holder.cbSelect.setChecked(person.isSelected());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !person.isSelected();
            person.setSelected(newState);
            holder.cbSelect.setChecked(newState);
            
            if (newState) {
                if (!selectedPersons.contains(person)) {
                    selectedPersons.add(person);
                }
            } else {
                selectedPersons.remove(person);
            }
        });

        holder.cbSelect.setOnClickListener(v -> {
            boolean isChecked = holder.cbSelect.isChecked();
            person.setSelected(isChecked);
            
            if (isChecked) {
                if (!selectedPersons.contains(person)) {
                    selectedPersons.add(person);
                }
            } else {
                selectedPersons.remove(person);
            }
        });
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public void addPerson(OvertimePerson person) {
        if (!personList.contains(person)) {
            personList.add(person);
            notifyItemInserted(personList.size() - 1);
        }
    }

    public void removeSelectedPersons() {
        personList.removeAll(selectedPersons);
        selectedPersons.clear();
        notifyDataSetChanged();
    }

    public List<OvertimePerson> getSelectedPersons() {
        return selectedPersons;
    }

    public List<OvertimePerson> getAllPersons() {
        return personList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvPersonId;
        TextView tvPersonName;
        TextView tvDepartment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvPersonId = itemView.findViewById(R.id.tv_person_id);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvDepartment = itemView.findViewById(R.id.tv_department);
        }
    }
}