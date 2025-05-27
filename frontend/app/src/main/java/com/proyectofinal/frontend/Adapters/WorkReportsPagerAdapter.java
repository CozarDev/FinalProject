package com.proyectofinal.frontend.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.proyectofinal.frontend.Fragments.CreateWorkReportFragment;
import com.proyectofinal.frontend.Fragments.WorkReportListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkReportsPagerAdapter extends FragmentStateAdapter {
    
    private List<TabInfo> tabs = new ArrayList<>();
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private FragmentManager fragmentManager;
    
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
        this.fragmentManager = fragmentActivity.getSupportFragmentManager();
    }
    
    public void setTabs(List<TabInfo> tabs) {
        this.tabs = tabs;
        // Limpiar el mapa de fragmentos cuando se actualicen las pestañas
        fragmentMap.clear();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position >= tabs.size()) {
            Fragment fragment = new WorkReportListFragment();
            fragmentMap.put(position, fragment);
            return fragment;
        }
        
        TabInfo tab = tabs.get(position);
        Fragment fragment;
        
        switch (tab.type) {
            case "LIST":
                fragment = new WorkReportListFragment();
                break;
            case "CREATE":
                fragment = new CreateWorkReportFragment();
                break;
            default:
                fragment = new WorkReportListFragment();
                break;
        }
        
        fragmentMap.put(position, fragment);
        return fragment;
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
    
    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
    
    public void refreshAllWorkReportFragments() {
        // Primero intentar con el mapa de fragmentos
        boolean foundFragments = false;
        for (Fragment fragment : fragmentMap.values()) {
            if (fragment instanceof WorkReportListFragment && fragment.isAdded() && !fragment.isDetached()) {
                ((WorkReportListFragment) fragment).onPageSelected();
                foundFragments = true;
            }
        }
        
        // Si no se encontraron fragmentos en el mapa, buscar en el FragmentManager
        if (!foundFragments && fragmentManager != null) {
            List<Fragment> allFragments = fragmentManager.getFragments();
            for (Fragment fragment : allFragments) {
                if (fragment instanceof WorkReportListFragment && fragment.isAdded() && !fragment.isDetached()) {
                    ((WorkReportListFragment) fragment).onPageSelected();
                }
            }
        }
    }
    
    public void refreshFragment(int position) {
        Fragment fragment = fragmentMap.get(position);
        if (fragment instanceof WorkReportListFragment) {
            ((WorkReportListFragment) fragment).onPageSelected();
        }
    }
} 