package com.example.reminderbylocation.Fragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminderbylocation.R;
import com.example.reminderbylocation.Model.Reminder;
import com.example.reminderbylocation.Activities.ReminderDetailsActivity;
import com.example.reminderbylocation.Utils.RemindersSharedPreferences;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder> {

    private ArrayList<Reminder> remindersList;
    public RemindersAdapter(ArrayList<Reminder> list) {
        this.remindersList = list;
    }

    @NonNull
    @Override
    public RemindersAdapter.ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item,parent,false);
        ReminderViewHolder scoreViewHolder = new ReminderViewHolder(view);
        return scoreViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = remindersList.get(position);
        holder.textview_title.setText(reminder.getTitle());
        holder.textview_location.setText(reminder.getFeatureName());
        holder.textview_distance.setText("Remind me if ".concat(String.valueOf(reminder.getRadius())).concat(" KM near location"));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open an activity with reminder details
                openReminderDetailsActivity(reminder,holder);
            }
        });
    }

    private void openReminderDetailsActivity(Reminder reminder, ReminderViewHolder holder) {
        // Replace `YourReminderDetailsActivity.class` with the actual class name of your reminder details activity
        Intent intent = new Intent(holder.itemView.getContext(), ReminderDetailsActivity.class);
        Gson gson = new Gson();
        String jsonReminder = gson.toJson(reminder);
        intent.putExtra("REMINDER_JSON",jsonReminder);
        // Add any other extras you want to pass to the details activity

        holder.itemView.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }
    public class ReminderViewHolder extends RecyclerView.ViewHolder{
        MaterialTextView textview_title;
        MaterialTextView textview_location;
        MaterialTextView textview_distance;

        CheckBox isChecked_CB;
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textview_title = itemView.findViewById(R.id.textview_title);
            textview_location = itemView.findViewById(R.id.textview_location);
            textview_distance = itemView.findViewById(R.id.textview_distance);
            isChecked_CB = itemView.findViewById(R.id.isChecked_CB);
            isChecked_CB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Reminder reminder = remindersList.get(position);
                        if (isChecked) {
                            //RemindersSharedPreferences.getInstance().getList().remove(position);
                            remindersList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, remindersList.size());

                            // Update the shared preferences or perform any other necessary actions
                            RemindersSharedPreferences.getInstance().setList(remindersList);
                        }
                    }
                }
            });
        }
    }
}
