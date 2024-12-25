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

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private final List<Map<String, Object>> listaReportes;
    private final OnReportActionListener listener;

    public interface OnReportActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onReportClick(int position);
    }

    public ReportsAdapter(List<Map<String, Object>> listaReportes, OnReportActionListener listener) {
        this.listaReportes = listaReportes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Map<String, Object> reporte = listaReportes.get(position);

        String titulo = (String) reporte.get("titulo");
        String descripcion = (String) reporte.get("descripcion");

        holder.tvTitulo.setText(titulo);
        holder.tvDescripcion.setText(descripcion);

        holder.itemView.setOnClickListener(v -> listener.onReportClick(position));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(position));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return listaReportes.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvDescripcion;
        Button btnEdit, btnDelete;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloReport);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionReport);
            btnEdit = itemView.findViewById(R.id.btnEditReport);
            btnDelete = itemView.findViewById(R.id.btnDeleteReport);
        }
    }
}
