package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Map<String, Object>> listaContactos;
    private OnContactActionListener actionListener;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Declarar Firebase

    public ContactsAdapter(List<Map<String, Object>> listaContactos, OnContactActionListener actionListener) {
        this.listaContactos = listaContactos;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Map<String, Object> contacto = listaContactos.get(position);
        holder.bind(contacto, position);
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTelefono;
        Button btnEdit, btnDelete;
        CheckBox favoriteBox;

        public ContactViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTelefono = itemView.findViewById(R.id.tvTelefono);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            favoriteBox = itemView.findViewById(R.id.favoriteBox);

            // Configuración de los botones
            btnEdit.setOnClickListener(v -> actionListener.onEditClick(getBindingAdapterPosition()));
            btnDelete.setOnClickListener(v -> actionListener.onDeleteClick(getBindingAdapterPosition()));
            favoriteBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    actionListener.onFavoriteClick(getBindingAdapterPosition(), listaContactos.get(getBindingAdapterPosition()));
                } else {
                    actionListener.onDesmarcarFavorito(listaContactos.get(getBindingAdapterPosition()), getBindingAdapterPosition());
                }
            });
        }

        public void bind(Map<String, Object> contacto, int position) {
            tvNombre.setText((String) contacto.get("nombre"));
            tvTelefono.setText((String) contacto.get("telefono"));
            favoriteBox.setChecked((Boolean) contacto.get("favorito"));
        }
    }

    public void actualizarFavorito(String nuevoFavoritoId, int position) {
        // Consultar si hay un favorito actual
        db.collection("contactos")
                .whereEqualTo("favorito", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Si hay un favorito actual, mostrar el diálogo de confirmación
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String nombreActual = (String) doc.get("nombre");
                        mostrarDialogoConfirmacion(nuevoFavoritoId, position, nombreActual);
                    } else {
                        // Si no hay favorito actual, marcar el nuevo favorito directamente
                        marcarNuevoFavorito(nuevoFavoritoId, position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al verificar favorito actual", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para marcar el nuevo favorito
    private void marcarNuevoFavorito(String nuevoFavoritoId, int position) {
        // Marcar el nuevo favorito
        db.collection("contactos")
                .document(nuevoFavoritoId)
                .update("favorito", true)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar el estado del contacto en la lista
                    listaContactos.get(position).put("favorito", true);
                    notifyItemChanged(position); // Actualizar el CheckBox en la interfaz
                    Toast.makeText(context, "Favorito actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al actualizar favorito", Toast.LENGTH_SHORT).show()
                );
    }

    // Método para verificar si hay un favorito actual
    public void verificarFavoritoActual(int position, Map<String, Object> nuevoContacto) {
        // Consultar si hay un favorito actual
        db.collection("contactos")
                .whereEqualTo("favorito", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Si hay un favorito actual, desmarcarlo
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            doc.getReference().update("favorito", false);
                        }
                    }
                    // Ahora marcar el nuevo favorito
                    marcarNuevoFavorito(nuevoContacto.get("documentId").toString(), position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al verificar favorito actual", Toast.LENGTH_SHORT).show();
                });
    }

    // Mostrar confirmación antes de cambiar el favorito
    public void mostrarDialogoConfirmacion(String nuevoFavoritoId, int position, String nombreActual) {
        new AlertDialog.Builder(context)
                .setTitle("¡Aviso!")
                .setMessage("Usted ya tiene un contacto favorito: " + nombreActual +
                        "\n¿Desea cambiar su contacto favorito?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    // Desmarcar el favorito actual
                    desmarcarFavoritoActual(() -> marcarNuevoFavorito(nuevoFavoritoId, position));
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    // Método para desmarcar el favorito actual
    private void desmarcarFavoritoActual(Runnable onComplete) {
        db.collection("contactos")
                .whereEqualTo("favorito", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().update("favorito", false);
                    }
                    onComplete.run(); // Llamar al callback después de desmarcar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al desmarcar el favorito actual", Toast.LENGTH_SHORT).show();
                });
    }

    public interface OnContactActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onFavoriteClick(int position, Map<String, Object> stringObjectMap);
        void onDesmarcarFavorito(Map<String, Object> contacto, int position); // Nuevo método
    }
}
