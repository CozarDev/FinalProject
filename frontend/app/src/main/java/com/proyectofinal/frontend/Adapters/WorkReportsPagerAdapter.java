package com.proyectofinal.frontend.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.proyectofinal.frontend.Fragments.CreateWorkReportFragment;
import com.proyectofinal.frontend.Fragments.WorkReportListFragment;

import java.util.ArrayList;
import java.util.List;

public class WorkReportsPagerAdapter extends FragmentStateAdapter {
    
    private List<TabInfo> tabs = new ArrayList<>();
    
    public static class TabInfo {
        public String title;
        public String type;
        
        public TabInfo(String title, String type) {
            this.title = title;
            this.type = type;
        }
    }
    
    public WorkReportsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    public void setTabs(List<TabInfo> tabs) {
        this.tabs = tabs;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position >= tabs.size()) {
            return new WorkReportListFragment();
        }
        
        TabInfo tab = tabs.get(position);
        
        switch (tab.type) {
            case "LIST":
                return new WorkReportListFragment();
            case "CREATE":
                return new CreateWorkReportFragment();
            default:
                return new WorkReportListFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return tabs.size();
    }
    
    public String getTabTitle(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).title;
        }
        return "";
    }
} 