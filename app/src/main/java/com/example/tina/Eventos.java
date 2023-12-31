package com.example.tina;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Eventos extends Fragment {
    private ListView mListView;
    private EventosAdapter mAdapter;
    private TextView mEmptyView;
    private ArrayList<EventoItem> todosEventos; // Lista de todos os eventos
    private ArrayList<EventoItem> eventosFiltrados; // Eventos filtrados pela data selecionada

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos, container, false);

        // Inicializar o Firebase
        FirebaseApp.initializeApp(getContext());

        // Criar a referência ao banco de dados Firebase
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // Criar a lista de eventos
        todosEventos = new ArrayList<>();
        eventosFiltrados = new ArrayList<>();

        // Configurar o adaptador
        mAdapter = new EventosAdapter(getContext(), eventosFiltrados);

        // Vincular o adaptador ao ListView
        mListView = view.findViewById(R.id.listView);
        mEmptyView = view.findViewById(R.id.empty_view);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mAdapter);

        // Configurar o CalendarView
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        // Obter a data atual em formato yyyy-MM-dd
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = dateFormat.format(calendar.getTime());

        // Carregar todos os eventos do Firebase inicialmente
        DatabaseReference eventosRef = mDatabase.child("Eventos");
        eventosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                todosEventos.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String nome = eventSnapshot.child("nome").getValue(String.class);
                    String data = eventSnapshot.child("data").getValue(String.class);
                    String descricao = eventSnapshot.child("descricao").getValue(String.class);

                    if (nome != null && data != null && descricao != null) {
                        EventoItem evento = new EventoItem(nome, data, descricao);
                        todosEventos.add(evento);
                    }
                }

                // Filtrar os eventos com base na data atual
                filtrarEventosPorData(dataAtual);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Erro ao carregar os eventos: " + databaseError.getMessage());
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String dataSelecionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                // Filtrar os eventos com base na data selecionada
                filtrarEventosPorData(dataSelecionada);
            }
        });

        return view;
    }

    // Método para filtrar eventos por data
    private void filtrarEventosPorData(String dataSelecionada) {
        eventosFiltrados.clear();

        for (EventoItem evento : todosEventos) {
            if (evento.getData().equals(dataSelecionada)) {
                eventosFiltrados.add(evento);
            }
        }

        mAdapter.notifyDataSetChanged();

        if (eventosFiltrados.isEmpty()) {
            mEmptyView.setText("Sem eventos nesta data");
        } else {
            mEmptyView.setText("");
        }
    }
}
