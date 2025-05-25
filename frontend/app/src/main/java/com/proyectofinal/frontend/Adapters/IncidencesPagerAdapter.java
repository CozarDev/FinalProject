package com.proyectofinal.frontend.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.proyectofinal.frontend.Fragments.IncidenceListFragment;
import com.proyectofinal.frontend.Fragments.IncidenceStatsFragment;
import com.proyectofinal.frontend.Models.Incidence;

import java.util.ArrayList;
import java.util.List;

public class IncidencesPagerAdapter extends FragmentStateAdapter {

    private final List<TabInfo> tabs;
    private final IncidenceAdapter.UserRole userRole;
    private final List<Fragment> fragments;

    public IncidencesPagerAdapter(@NonNull Fragment fragment, List<TabInfo> tabs, IncidenceAdapter.UserRole userRole) {
        super(fragment);
        this.tabs = tabs;
        this.userRole = userRole;
        this.fragments = new ArrayList<>();
        
        // Crear fragmentos para cada pestaña
        for (TabInfo tab : tabs) {
            fragments.add(createFragmentForTab(tab));
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position < fragments.size()) {
            return fragments.get(position);
        }
        // Fallback: crear nuevo fragmento
        return createFragmentForTab(tabs.get(position));
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    private Fragment createFragmentForTab(TabInfo tabInfo) {
        switch (tabInfo.getType()) {
            case STATS:
                return IncidenceStatsFragment.newInstance();
            
            case ALL_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.ALL_INCIDENCES, userRole, true);
            
            case ACTIVE_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.ACTIVE_INCIDENCES, userRole, true);
            
            case PENDING_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.PENDING_INCIDENCES, userRole, true);
            
            case IN_PROGRESS_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.IN_PROGRESS_INCIDENCES, userRole, true);
            
            case RESOLVED_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.RESOLVED_INCIDENCES, userRole, false);
            
            case MY_ASSIGNED_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.MY_ASSIGNED_INCIDENCES, userRole, true);
            
            case MY_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.MY_INCIDENCES, userRole, true);
            
            case DEPARTMENT_INCIDENCES:
                return IncidenceListFragment.newInstance(TabType.DEPARTMENT_INCIDENCES, userRole, true);
            
            case PENDING_DEPT:
                return IncidenceListFragment.newInstance(TabType.PENDING_DEPT, userRole, true);
            
            case RESOLVED_DEPT:
                return IncidenceListFragment.newInstance(TabType.RESOLVED_DEPT, userRole, false);
            
            case MY_PENDING:
                return IncidenceListFragment.newInstance(TabType.MY_PENDING, userRole, true);
            
            case MY_IN_PROGRESS:
                return IncidenceListFragment.newInstance(TabType.MY_IN_PROGRESS, userRole, false);
            
            case MY_RESOLVED:
                return IncidenceListFragment.newInstance(TabType.MY_RESOLVED, userRole, false);
            
            default:
                return IncidenceListFragment.newInstance(TabType.ALL_INCIDENCES, userRole, false);
        }
    }

    // **MÉTODOS PARA NOTIFICAR CAMBIOS A LOS FRAGMENTOS**

    /**
     * Notifica que se ha creado una nueva incidencia
     */
    public void notifyIncidenceCreated(Incidence newIncidence) {
        for (Fragment fragment : fragments) {
            if (fragment instanceof IncidenceListFragment) {
                ((IncidenceListFragment) fragment).onIncidenceCreated(newIncidence);
            }
        }
    }

    /**
     * Notifica que se ha actualizado una incidencia
     */
    public void notifyIncidenceUpdated(Incidence updatedIncidence) {
        for (Fragment fragment : fragments) {
            if (fragment instanceof IncidenceListFragment) {
                ((IncidenceListFragment) fragment).onIncidenceUpdated(updatedIncidence);
            }
        }
    }

    /**
     * Notifica que se ha eliminado una incidencia
     */
    public void notifyIncidenceRemoved(Incidence incidence) {
        for (Fragment fragment : fragments) {
            if (fragment instanceof IncidenceListFragment) {
                ((IncidenceListFragment) fragment).onIncidenceRemoved(incidence);
            }
        }
    }

    /**
     * Refresca una pestaña específica
     */
    public void refreshTab(int position) {
        if (position < fragments.size()) {
            Fragment fragment = fragments.get(position);
            if (fragment instanceof IncidenceListFragment) {
                ((IncidenceListFragment) fragment).refreshData();
            } else if (fragment instanceof IncidenceStatsFragment) {
                ((IncidenceStatsFragment) fragment).refreshData();
            }
        }
    }

    /**
     * Refresca todas las pestañas
     */
    public void refreshAllTabs() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof IncidenceListFragment) {
                ((IncidenceListFragment) fragment).refreshData();
            } else if (fragment instanceof IncidenceStatsFragment) {
                ((IncidenceStatsFragment) fragment).refreshData();
            }
        }
    }

    // **CLASES AUXILIARES**

    /**
     * Información de una pestaña
     */
    public static class TabInfo {
        private final String title;
        private final TabType type;

        public TabInfo(String title, TabType type) {
            this.title = title;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public TabType getType() {
            return type;
        }
    }

    /**
     * Tipos de pestañas disponibles
     */
    public enum TabType {
        // Para admin y jefe de incidencias
        ALL_INCIDENCES,
        ACTIVE_INCIDENCES,
        PENDING_INCIDENCES,
        IN_PROGRESS_INCIDENCES,
        RESOLVED_INCIDENCES,
        
        // Para empleados de incidencias
        MY_ASSIGNED_INCIDENCES,
        
        // Para empleados y jefes de departamento
        MY_INCIDENCES,
        DEPARTMENT_INCIDENCES,
        PENDING_DEPT,
        RESOLVED_DEPT,
        
        // Para empleados normales
        MY_PENDING,
        MY_IN_PROGRESS,
        MY_RESOLVED,
        
        // Estadísticas
        STATS
    }
} 