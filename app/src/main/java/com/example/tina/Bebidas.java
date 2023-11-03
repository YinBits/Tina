package com.example.tina;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView; // Adicione a importação da SearchView
import android.widget.TextView; // Adicione a importação do TextView

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Bebidas extends Fragment {

    private RecyclerView recyclerView;
    private CardapioAdapter cardapioAdapter;
    private List<MenuItem> bebidasList; // Atualize o nome da lista para "bebidasList"
    private SearchView searchView;
    private TextView noResultsTextView; // Adicione um TextView para exibir a mensagem

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pratos, container, false);

        recyclerView = rootView.findViewById(R.id.gridlayout);
        bebidasList = new ArrayList<>(); // Atualize o nome da lista para "bebidasList"
        cardapioAdapter = new CardapioAdapter(bebidasList); // Atualize o nome da lista para "bebidasList"
        recyclerView.setAdapter(cardapioAdapter);

        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

        // Botão "Voltar"
        Button buttonVoltarCardapio = rootView.findViewById(R.id.btn_voltar);
        buttonVoltarCardapio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        // Inicialização da barra de pesquisa
        searchView = rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterResults(newText);
                return true;
            }
        });

        // Adicione o TextView para exibir a mensagem
        noResultsTextView = rootView.findViewById(R.id.noResultsTextView);
        noResultsTextView.setVisibility(View.GONE); // Inicialmente, oculte o TextView

        // Carregar os itens do Firebase Realtime Database
        loadItemsFromFirebase();

        return rootView;
    }

    private void loadItemsFromFirebase() {
        DatabaseReference cardapioRef = FirebaseDatabase.getInstance().getReference("Cardapio");
        cardapioRef.orderByChild("categoria").equalTo("Bebidas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bebidasList.clear(); // Limpe a lista existente

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String nome = itemSnapshot.child("nome").getValue(String.class);
                    String descricao = itemSnapshot.child("descricao").getValue(String.class);
                    double preco = itemSnapshot.child("preco").getValue(Double.class);
                    String imagem = itemSnapshot.child("imagem").getValue(String.class);

                    if (nome != null && descricao != null && imagem != null) {
                        bebidasList.add(new MenuItem(nome, descricao, preco, imagem));
                    }
                }

                // Verifique se há resultados após o carregamento dos itens
                if (bebidasList.isEmpty()) {
                    noResultsTextView.setVisibility(View.VISIBLE); // Exiba a mensagem
                } else {
                    noResultsTextView.setVisibility(View.GONE); // Oculte a mensagem
                }

                // Notificar o adapter de que os dados foram atualizados
                cardapioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Lida com erros ao acessar o banco de dados
                // Aqui você pode tratar erros, como exibir uma mensagem de erro para o usuário
            }
        });
    }

    private void filterResults(String query) {
        List<MenuItem> filteredList = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        for (MenuItem item : bebidasList) {
            String nome = item.getNome().toLowerCase();
            String descricao = item.getDescricao().toLowerCase();

            if (nome.contains(lowercaseQuery) || descricao.contains(lowercaseQuery)) {
                filteredList.add(item);
            }
        }

        // Atualize a visibilidade do TextView de acordo com os resultados
        if (filteredList.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE); // Exiba a mensagem
        } else {
            noResultsTextView.setVisibility(View.GONE); // Oculte a mensagem
        }

        cardapioAdapter.setItems(filteredList);
    }
}
