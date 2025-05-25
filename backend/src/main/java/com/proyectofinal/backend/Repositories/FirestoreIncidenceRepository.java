package com.proyectofinal.backend.Repositories;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.proyectofinal.backend.Models.Incidence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreIncidenceRepository {

    @Autowired(required = false)
    private Firestore firestore;

    private static final String COLLECTION_NAME = "incidences";

    /**
     * Guarda una incidencia en Firestore
     */
    public Incidence save(Incidence incidence) throws ExecutionException, InterruptedException {
        if (incidence.getId() == null || incidence.getId().isEmpty()) {
            // Crear nuevo documento con ID automático
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            incidence.setId(docRef.getId());
        }
        
        // Guardar en Firestore
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(incidence.getId())
                .set(incidence);
        
        future.get(); // Esperar a que se complete
        return incidence;
    }

    /**
     * Busca incidencia por ID
     */
    public Incidence findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        
        if (document.exists()) {
            Incidence incidence = document.toObject(Incidence.class);
            if (incidence != null) {
                incidence.setId(document.getId());
            }
            return incidence;
        }
        return null;
    }

    /**
     * Obtiene todas las incidencias
     */
    public List<Incidence> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME).get().get();
        List<Incidence> incidences = new ArrayList<>();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Incidence incidence = document.toObject(Incidence.class);
            if (incidence != null) {
                incidence.setId(document.getId());
                incidences.add(incidence);
            }
        }
        
        return incidences;
    }

    /**
     * Busca incidencias por estado
     */
    public List<Incidence> findByStatus(Incidence.Status status) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", status.name())
                .get()
                .get();
        
        List<Incidence> incidences = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Incidence incidence = document.toObject(Incidence.class);
            if (incidence != null) {
                incidence.setId(document.getId());
                incidences.add(incidence);
            }
        }
        
        return incidences;
    }

    /**
     * Busca incidencias por creador
     */
    public List<Incidence> findByCreatedBy(String createdBy) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("createdBy", createdBy)
                .get()
                .get();
        
        List<Incidence> incidences = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Incidence incidence = document.toObject(Incidence.class);
            if (incidence != null) {
                incidence.setId(document.getId());
                incidences.add(incidence);
            }
        }
        
        return incidences;
    }

    /**
     * Busca incidencias asignadas a un empleado
     */
    public List<Incidence> findByAssignedTo(String assignedTo) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("assignedTo", assignedTo)
                .get()
                .get();
        
        List<Incidence> incidences = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Incidence incidence = document.toObject(Incidence.class);
            if (incidence != null) {
                incidence.setId(document.getId());
                incidences.add(incidence);
            }
        }
        
        return incidences;
    }

    /**
     * Elimina una incidencia
     */
    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    /**
     * Verifica si existe una incidencia
     */
    public boolean existsById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        return document.exists();
    }

    /**
     * Cuenta incidencias por estado
     */
    public long countByStatus(Incidence.Status status) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", status.name())
                .get()
                .get();
        return querySnapshot.size();
    }
} 