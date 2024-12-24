package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.btnFavorite.setOnClickListener(v -> actionListener.onFavoriteClick(position, contacto));
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTelefono;
        Button btnEdit, btnDelete, btnFavorite;

        public ContactViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTelefono = itemView.findViewById(R.id.tvTelefono);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            // Configuración de los botones
            btnEdit.setOnClickListener(v -> actionListener.onEditClick(getBindingAdapterPosition()));
            btnDelete.setOnClickListener(v -> actionListener.onDeleteClick(getBindingAdapterPosition()));
        }

        public void bind(Map<String, Object> contacto, int position) {
            tvNombre.setText((String) contacto.get("nombre"));
            tvTelefono.setText((String) contacto.get("telefono"));
        }
    }

    // Actualizar el favorito en Firebase
    public void actualizarFavorito(String nuevoFavoritoId, int position) {
        // Desmarcar cualquier favorito anterior
        db.collection("contactos")
                .whereEqualTo("favorito", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().update("favorito", false);
                    }

                    // Marcar el nuevo favorito
                    db.collection("contactos")
                            .document(nuevoFavoritoId)
                            .update("favorito", true)
                            .addOnSuccessListener(aVoid -> {
                                listaContactos.get(position).put("favorito", true);
                                notifyItemChanged(position);
                                Toast.makeText(context, "Favorito actualizado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error al actualizar favorito", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al obtener favorito actual", Toast.LENGTH_SHORT).show()
                );
    }

    // Mostrar confirmación antes de cambiar el favorito
    public void mostrarDialogoConfirmacion(String nuevoFavoritoId, int position, String nombreActual) {
        new AlertDialog.Builder(context)
                .setTitle("¡Aviso!")
                .setMessage("Usted ya tiene un contacto favorito: " + nombreActual +
                        "\n¿Desea cambiar su contacto favorito?")
                .setPositiveButton("Confirmar", (dialog, which) -> actualizarFavorito(nuevoFavoritoId, position))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public interface OnContactActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onFavoriteClick(int position, Map<String, Object> stringObjectMap);
    }
}
