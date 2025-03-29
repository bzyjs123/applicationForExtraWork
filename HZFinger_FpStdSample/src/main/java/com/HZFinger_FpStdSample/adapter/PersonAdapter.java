package com.HZFinger_FpStdSample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HZFinger_FpStdSample.R;
import com.HZFinger_FpStdSample.model.Person;

import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {
    private List<Person> persons;
    private int selectedPosition = -1;

    public PersonAdapter(List<Person> persons) {
        this.persons = persons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            Person person = persons.get(adapterPosition);
            holder.tvId.setText(person.getId());
            holder.tvName.setText(person.getName());
            holder.tvDept.setText(person.getDepartment());
            holder.tvCard.setText(person.getCardNo());

            holder.itemView.setSelected(selectedPosition == adapterPosition);
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = adapterPosition;
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public Person getSelectedPerson() {
        return selectedPosition != -1 ? persons.get(selectedPosition) : null;
    }

    public void addPerson(Person person) {
        persons.add(person);
        notifyItemInserted(persons.size() - 1);
    }

    public void updatePerson(Person person) {
        if (selectedPosition != -1) {
            persons.set(selectedPosition, person);
            notifyItemChanged(selectedPosition);
        }
    }

    public void deleteSelectedPerson() {
        if (selectedPosition != -1) {
            persons.remove(selectedPosition);
            notifyItemRemoved(selectedPosition);
            selectedPosition = -1;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvDept, tvCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_id);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDept = itemView.findViewById(R.id.tv_dept);
            tvCard = itemView.findViewById(R.id.tv_card);
        }
    }
}
