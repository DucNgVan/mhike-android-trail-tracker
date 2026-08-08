package com.example.hiking_application_coursework.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.activities.HikeDetailActivity;
import com.example.hiking_application_coursework.activities.MainActivity;
import com.example.hiking_application_coursework.adapters.HikeAdapter;
import com.example.hiking_application_coursework.helpers.FirestoreHelper;
import com.example.hiking_application_coursework.models.Hike;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText editSearch;
    private RecyclerView recyclerView;
    private HikeAdapter adapter;
    private FirestoreHelper firestoreHelper;
    private List<Hike> allHikes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);

        firestoreHelper = new FirestoreHelper();
        editSearch = view.findViewById(R.id.editSearchQuery);
        recyclerView = view.findViewById(R.id.recyclerViewSearchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.searchToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToHomeTab();
                } else if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        loadData();

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        firestoreHelper.getAllHikes(new FirestoreHelper.DataStatus() {
            @Override
            public void dataIsLoaded(List<Hike> hikes) {
                if (hikes != null) {
                    allHikes = hikes;
                } else {
                    allHikes = new ArrayList<>();
                }
                String currentQuery = editSearch != null ? editSearch.getText().toString() : "";
                performSearch(currentQuery);
            }
            @Override public void dataIsInserted() {}
            @Override public void dataIsDeleted() {}
        });
    }

    private void performSearch(String query) {
        String q = query != null ? query.trim().toLowerCase() : "";
        List<Hike> results = new ArrayList<>();
        
        for (Hike h : allHikes) {
            if (q.isEmpty()) {
                results.add(h);
                continue;
            }
            
            boolean matchesName = h.getName() != null && h.getName().toLowerCase().contains(q);
            boolean matchesLoc = h.getLocation() != null && h.getLocation().toLowerCase().contains(q);
            boolean matchesDiff = h.getDifficulty() != null && h.getDifficulty().toLowerCase().contains(q);
            boolean matchesDate = h.getDate() != null && h.getDate().toLowerCase().contains(q);
            boolean matchesDesc = h.getDescription() != null && h.getDescription().toLowerCase().contains(q);
            boolean matchesWeather = h.getWeather() != null && h.getWeather().toLowerCase().contains(q);

            if (matchesName || matchesLoc || matchesDiff || matchesDate || matchesDesc || matchesWeather) {
                results.add(h);
            }
        }

        if (adapter == null) {
            adapter = new HikeAdapter(results, hike -> {
                Intent intent = new Intent(getActivity(), HikeDetailActivity.class);
                intent.putExtra("hike", hike);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setHikes(results);
        }
    }
}
