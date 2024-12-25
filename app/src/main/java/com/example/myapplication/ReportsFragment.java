package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment implements ReportsAdapter.OnReportActionListener {

    private RecyclerView recyclerView;
    private ReportsAdapter adapter;
    private List<Map<String, Object>> listaReportes = new ArrayList<>();
    private FirebaseFirestore db;

    public ReportsFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listaReportes = new ArrayList<>();
        adapter = new ReportsAdapter(listaReportes, this);
        recyclerView.setAdapter(adapter);

        cargarReportes();

        view.findViewById(R.id.fabAddReport).setOnClickListener(v -> agregarReporte());

        return view;
    }

    private void cargarReportes() {
        db.collection("reportes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaReportes.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> reporte = document.getData();
                            reporte.put("documentId", document.getId());
                            listaReportes.add(reporte);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Error al cargar los reportes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void agregarReporte() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_report, null);
        builder.setView(dialogView);

        final EditText editTextTitulo = dialogView.findViewById(R.id.etTituloReport);
        final EditText editTextDescripcion = dialogView.findViewById(R.id.etDescripcionReport);
        final EditText editTextFechaHora = dialogView.findViewById(R.id.etFechaHora);
        final EditText editTextResponsable = dialogView.findViewById(R.id.etResponsableReport);
        final EditText editTextLugar = dialogView.findViewById(R.id.etLugar);
        final EditText editTextAcontecimientos = dialogView.findViewById(R.id.etAcontecimientos);

        // Configurar el campo de fecha y hora
        editTextFechaHora.setOnClickListener(v -> {
            // Mostrar DatePickerDialog
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Cuando se selecciona una fecha, mostrar el TimePickerDialog
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                getActivity(),
                                (timeView, selectedHour, selectedMinute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);

                                    // Formatear y establecer fecha y hora en el EditText
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    editTextFechaHora.setText(dateFormat.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true // Usar formato 24 horas
                        );
                        timePickerDialog.show();
                    },
                    year,
                    month,
                    day
            );
            datePickerDialog.show();
        });

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String titulo = editTextTitulo.getText().toString().trim();
            String descripcion = editTextDescripcion.getText().toString().trim();
            String lugar = editTextLugar.getText().toString().trim();
            String acontecimientos = editTextAcontecimientos.getText().toString().trim();
            String fechaHora = editTextFechaHora.getText().toString().trim();
            String responsable = editTextResponsable.getText().toString().trim();

            // Validar entradas
            if (responsable.isEmpty() ||lugar.isEmpty() || acontecimientos.isEmpty() || titulo.isEmpty() || descripcion.isEmpty() || fechaHora.isEmpty()){
                Toast.makeText(getActivity(), "Por favor rellene todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear el objeto del reporte
            Map<String, Object> nuevoReporte = new HashMap<>();
            nuevoReporte.put("titulo", titulo);
            nuevoReporte.put("descripcion", descripcion);
            nuevoReporte.put("fechaHora", fechaHora); // Asignar la fecha y hora
            nuevoReporte.put("responsable", responsable); // Asignar el responsable
            nuevoReporte.put("acontecimientos", acontecimientos); // Inicialmente vacío
            nuevoReporte.put("lugar", lugar); // Inicialmente vacío

            // Guardar en Firestore
            db.collection("reportes")
                    .add(nuevoReporte)
                    .addOnSuccessListener(documentReference -> {
                        nuevoReporte.put("documentId", documentReference.getId());
                        listaReportes.add(nuevoReporte);
                        adapter.notifyItemInserted(listaReportes.size() - 1);
                        Toast.makeText(getActivity(), "Reporte agregado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al agregar el reporte", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void eliminarReporte(int position) {
        Map<String, Object> reporte = listaReportes.get(position);
        String documentId = (String) reporte.get("documentId");
        String tituloReporte = (String) reporte.get("titulo"); // Obtener el nombre o título del reporte

        // Crear el diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("¡Aviso!")
                .setMessage("Vas a eliminar el reporte: \"" + tituloReporte + "\".\n¿Estás seguro?")
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia (opcional)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Confirmar eliminación
                    db.collection("reportes")
                            .document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                listaReportes.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getActivity(), "Reporte eliminado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al eliminar el reporte", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Cancelar la acción
                    dialog.dismiss();
                });

        // Mostrar el diálogo
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Opcional: Personalizar botones del diálogo
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryColor));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.secondaryText));
    }


    private void editarReporte(int position) {
        // Obtener el reporte seleccionado
        Map<String, Object> reporte = listaReportes.get(position);
        String documentId = (String) reporte.get("documentId");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Editar Reporte");

        // Inflar la vista personalizada
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_report, null);
        builder.setView(dialogView);

        // Referenciar los campos del formulario
        final EditText editTextTitulo = dialogView.findViewById(R.id.etTituloReport);
        final EditText editTextDescripcion = dialogView.findViewById(R.id.etDescripcionReport);
        final EditText editTextFechaHora = dialogView.findViewById(R.id.etFechaHora);
        final EditText editTextResponsable = dialogView.findViewById(R.id.etResponsableReport);
        final EditText editTextLugar = dialogView.findViewById(R.id.etLugar);
        final EditText editTextAcontecimientos = dialogView.findViewById(R.id.etAcontecimientos);

        // Llenar los campos con los valores actuales del reporte
        editTextTitulo.setText((String) reporte.get("titulo"));
        editTextDescripcion.setText((String) reporte.get("descripcion"));
        editTextFechaHora.setText((String) reporte.get("fechaHora"));
        editTextResponsable.setText((String) reporte.get("responsable"));
        editTextLugar.setText((String) reporte.get("lugar"));
        editTextAcontecimientos.setText((String) reporte.get("acontecimientos"));

        // Configurar el campo de fecha y hora
        editTextFechaHora.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                getActivity(),
                                (timeView, selectedHour, selectedMinute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    editTextFechaHora.setText(dateFormat.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timePickerDialog.show();
                    },
                    year,
                    month,
                    day
            );
            datePickerDialog.show();
        });

        // Botón Guardar
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String titulo = editTextTitulo.getText().toString().trim();
            String descripcion = editTextDescripcion.getText().toString().trim();
            String lugar = editTextLugar.getText().toString().trim();
            String acontecimientos = editTextAcontecimientos.getText().toString().trim();
            String fechaHora = editTextFechaHora.getText().toString().trim();
            String responsable = editTextResponsable.getText().toString().trim();

            if (titulo.isEmpty() || descripcion.isEmpty() || lugar.isEmpty() || acontecimientos.isEmpty() || fechaHora.isEmpty() || responsable.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor rellene todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updatedReporte = Map.of(
                    "titulo", titulo,
                    "descripcion", descripcion,
                    "lugar", lugar,
                    "acontecimientos", acontecimientos,
                    "fechaHora", fechaHora,
                    "responsable", responsable
            );

            db.collection("reportes")
                    .document(documentId)
                    .update(updatedReporte)
                    .addOnSuccessListener(aVoid -> {
                        reporte.putAll(updatedReporte);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(getActivity(), "Reporte actualizado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al actualizar el reporte", Toast.LENGTH_SHORT).show());
        });

        // Botón Cancelar
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void mostrarDetalleReporte(Map<String, Object> reporte) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Crear la vista personalizada
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_detalle_reporte, null);
        builder.setView(dialogView);

        // Referenciar los elementos de la vista
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDetalle);
        TextView tvFechaHora = dialogView.findViewById(R.id.tvFechaHoraDetalle);
        TextView tvResponsable = dialogView.findViewById(R.id.tvResponsableDetalle);
        TextView tvDescripcion = dialogView.findViewById(R.id.tvDescripcionDetalle);
        TextView tvAcontecimientos = dialogView.findViewById(R.id.tvAcontecimientosDetalle);
        TextView tvLugar = dialogView.findViewById(R.id.tvLugarDetalle);

        // Asignar los valores con etiquetas
        tvTitulo.setText((String) reporte.get("titulo"));
        tvFechaHora.setText("Fecha y hora: " + (String) reporte.get("fechaHora"));
        tvResponsable.setText("Responsable: " + (String) reporte.get("responsable"));
        tvDescripcion.setText("Descripción: " + (String) reporte.get("descripcion"));
        tvLugar.setText("Lugar: " + (String) reporte.get("lugar"));
        tvAcontecimientos.setText((String) reporte.get("acontecimientos"));

        // Crear el Díalogo
        AlertDialog dialog = builder.create();

        // Botón de cierre
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onReportClick(int position){
        mostrarDetalleReporte(listaReportes.get(position));
    }

    @Override
    public void onEditClick(int position) {
        editarReporte(position);
    }

    @Override
    public void onDeleteClick(int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Está seguro de que desea eliminar este reporte?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarReporte(position))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
