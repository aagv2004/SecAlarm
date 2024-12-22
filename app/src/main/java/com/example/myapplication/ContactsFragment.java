package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnContactActionListener {

    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private List<Map<String, Object>> listaContactos;
    private FirebaseFirestore db;

    public ContactsFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listaContactos = new ArrayList<>();
        adapter = new ContactsAdapter(listaContactos, this);
        recyclerView.setAdapter(adapter);

        cargarContactos();

        view.findViewById(R.id.fabAddContact).setOnClickListener(v -> agregarContacto());

        return view;
    }

    private void cargarContactos() {
        db.collection("contactos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaContactos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> contacto = document.getData();
                            contacto.put("documentId", document.getId());  // Guardamos el ID del documento
                            listaContactos.add(contacto);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Error al cargar los contactos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void agregarContacto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Agregar Contacto");

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        final EditText editTextNombre = dialogView.findViewById(R.id.etNombre);
        final EditText editTextTelefono = dialogView.findViewById(R.id.etTelefono);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = editTextNombre.getText().toString();
            String telefono = editTextTelefono.getText().toString();

            if (nombre.isEmpty() || telefono.isEmpty() || !PhoneNumberUtils.isGlobalPhoneNumber(telefono)) {
                Toast.makeText(getActivity(), "Por favor ingrese un nombre y un teléfono válidos.", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> nuevoContacto = Map.of("nombre", nombre, "telefono", telefono);
                db.collection("contactos")
                        .add(nuevoContacto)
                        .addOnSuccessListener(documentReference -> {
                            nuevoContacto.put("documentId", documentReference.getId());
                            listaContactos.add(nuevoContacto);
                            adapter.notifyItemInserted(listaContactos.size() - 1);
                            Toast.makeText(getActivity(), "Contacto agregado", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al agregar el contacto", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void eliminarContacto(int position) {
        Map<String, Object> contacto = listaContactos.get(position);
        String documentId = (String) contacto.get("documentId");  // Obtener el ID del documento

        db.collection("contactos")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaContactos.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getActivity(), "Contacto eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al eliminar el contacto", Toast.LENGTH_SHORT).show());
    }

    private void editarContacto(int position) {
        Map<String, Object> contacto = listaContactos.get(position);
        String documentId = (String) contacto.get("documentId");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Editar Contacto");

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        final EditText editTextNombre = dialogView.findViewById(R.id.etNombre);
        final EditText editTextTelefono = dialogView.findViewById(R.id.etTelefono);

        editTextNombre.setText((String) contacto.get("nombre"));
        editTextTelefono.setText((String) contacto.get("telefono"));

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = editTextNombre.getText().toString();
            String telefono = editTextTelefono.getText().toString();

            if (nombre.isEmpty() || telefono.isEmpty() || !PhoneNumberUtils.isGlobalPhoneNumber(telefono)) {
                Toast.makeText(getActivity(), "Por favor ingrese un nombre y un teléfono válidos.", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> updatedContacto = Map.of("nombre", nombre, "telefono", telefono);
                db.collection("contactos")
                        .document(documentId)
                        .update(updatedContacto)
                        .addOnSuccessListener(aVoid -> {
                            contacto.put("nombre", nombre);
                            contacto.put("telefono", telefono);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getActivity(), "Contacto actualizado", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al actualizar el contacto", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    public void onEditClick(int position) {
        editarContacto(position);
    }

    @Override
    public void onDeleteClick(int position) {
        eliminarContacto(position);
    }

    @Override
    public void onFavoriteClick(int position) {
        Map<String, Object> contacto = listaContactos.get(position);
        Toast.makeText(getActivity(), "Contacto marcado como favorito: " + contacto.get("nombre"), Toast.LENGTH_SHORT).show();
    }
}
