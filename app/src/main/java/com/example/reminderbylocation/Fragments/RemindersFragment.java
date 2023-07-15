package com.example.reminderbylocation.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.reminderbylocation.MainActivity;
import com.example.reminderbylocation.R;
import com.example.reminderbylocation.RemindersSharedPreferences;
import com.example.reminderbylocation.databinding.FragmentRemindersBinding;

public class RemindersFragment extends Fragment {

    FragmentRemindersBinding binding;


    public RemindersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRemindersBinding.inflate(inflater, container, false);
        binding.recyclerViewReminders.setHasFixedSize(true);
        binding.recyclerViewReminders.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewReminders.setAdapter(new RemindersAdapter(RemindersSharedPreferences.getInstance().getList()));
        View view = binding.getRoot();
        binding.fabAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragments(MapViewFragment.class);
            }
        });
        checkIfNoReminders(view);
        return view;
    }

    private void checkIfNoReminders(View v) {
        boolean isNoReminder = binding.recyclerViewReminders.getAdapter().getItemCount() == 0 ? true : false;
        if(isNoReminder){
            YoYo.with(Techniques.Bounce)
                    .duration(700)
                    .repeat(10)
                    .playOn(v.findViewById(R.id.fab_add_reminder));
        }
    }
}