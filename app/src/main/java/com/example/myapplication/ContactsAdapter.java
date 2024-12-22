package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Map<String, Object>> listaContactos;
    private OnContactActionListener actionListener;

    public ContactsAdapter(List<Map<String, Object>> listaContactos, OnContactActionListener actionListener) {
        this.listaContactos = listaContactos;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Map<String, Object> contacto = listaContactos.get(position);
        holder.bind(contacto);
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
            btnEdit.setOnClickListener(v -> actionListener.onEditClick(getAdapterPosition()));
            btnDelete.setOnClickListener(v -> actionListener.onDeleteClick(getAdapterPosition()));
            btnFavorite.setOnClickListener(v -> actionListener.onFavoriteClick(getAdapterPosition()));
        }

        public void bind(Map<String, Object> contacto) {
            tvNombre.setText((String) contacto.get("nombre"));
            tvTelefono.setText((String) contacto.get("telefono"));
        }
    }

    public interface OnContactActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onFavoriteClick(int position);
    }
}
