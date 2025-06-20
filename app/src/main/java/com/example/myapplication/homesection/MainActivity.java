package com.example.myapplication.homesection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.speseSection.SpeseFragment;
import com.example.myapplication.R;
import com.example.myapplication.obiettivisection.ObiettiviFragment;
import com.example.myapplication.usersection.LoginActivity;
import com.example.myapplication.usersection.UserFragment;

public class MainActivity extends AppCompatActivity {
    private static int changeFragment=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verifica se l'utente è autenticato
        boolean isAuthenticated = getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_authenticated", false);
        // Se non autenticato, manda l'utente alla schermata di login
        if (!isAuthenticated) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();  // Chiude MainActivity, così l'utente non può tornare indietro senza fare login
        }

        // ImageButton per il menu di navigazione
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSummary = findViewById(R.id.btn_summary);
        ImageButton btnSavings = findViewById(R.id.btn_savings);
        ImageButton btnUser = findViewById(R.id.btn_user);

        // Controlla se ci sono dati salvati dallo stato precedente
        if(savedInstanceState == null) {
            // Inizializza il primo fragment con HomeFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            setActiveButton(btnHome, R.drawable.home_active);
        } else {
            // Recupera l'ultimo fragment selezionato
            changeFragment = savedInstanceState.getInt("changeFragment");

            // Imposta il pulsante attivo in base al fragment selezionato
            switch (changeFragment) {
                case 1 : setActiveButton(btnHome, R.drawable.home_active); break;
                case 2 : setActiveButton(btnSummary, R.drawable.layers_active); break;
                case 3 : setActiveButton(btnSavings, R.drawable.book_active); break;
                case 4 : setActiveButton(btnUser, R.drawable.user_active);
            }
        }

        // Gestione click sui pulsanti di navigazione
        btnHome.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right).replace(R.id.fragment_container, new HomeFragment()).commit();
            setActiveButton(btnHome, R.drawable.home_active);
            changeFragment=1;
        });
        btnSummary.setOnClickListener(v -> {
            // Anima il cambio di fragment in base al fragment precedente
            if (changeFragment==3 || changeFragment==4)
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right).replace(R.id.fragment_container, new SpeseFragment()).commit();
            else
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.fragment_container, new SpeseFragment()).commit();

            changeFragment=2;
            setActiveButton(btnSummary, R.drawable.layers_active);
        });
        btnSavings.setOnClickListener(v -> {
            // Anima il cambio di fragment in base al fragment precedente
            if (changeFragment==4)
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right).replace(R.id.fragment_container, new ObiettiviFragment()).commit();
            else
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.fragment_container, new ObiettiviFragment()).commit();

            changeFragment=3;
            setActiveButton(btnSavings, R.drawable.book_active);
        });
        btnUser.setOnClickListener(v -> {
            // Passa al fragment UserFragment con animazione
            changeFragment=4;
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.fragment_container, new UserFragment()).commit();
            setActiveButton(btnUser, R.drawable.user_active);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Salva il fragment attualmente visualizzato
        outState.putInt("changeFragment", changeFragment);
    }

    // Metodo per evidenziare il pulsante attivo
    private void setActiveButton(ImageButton activeButton, int activeImageResId) {
        // Reset immagini e sfondi di tutti i pulsanti
        resetButtons();

        // Cambia immagine e sfondo del pulsante attivo
        activeButton.setImageResource(activeImageResId); // Imposta l'immagine attiva
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple)); // Cambia colore di sfondo
    }

    // Metodo per resettare immagini e colori dei pulsanti
    private void resetButtons() {
        // Home
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setImageResource(R.drawable.home_inactive);
        btnHome.setBackgroundColor(getResources().getColor(R.color.white));

        // Summary
        ImageButton btnSummary = findViewById(R.id.btn_summary);
        btnSummary.setImageResource(R.drawable.layers_inactive);
        btnSummary.setBackgroundColor(getResources().getColor(R.color.white));

        // Savings
        ImageButton btnSavings = findViewById(R.id.btn_savings);
        btnSavings.setImageResource(R.drawable.book_inactive);
        btnSavings.setBackgroundColor(getResources().getColor(R.color.white));

        // User
        ImageButton btnUser = findViewById(R.id.btn_user);
        btnUser.setImageResource(R.drawable.user_inactive);
        btnUser.setBackgroundColor(getResources().getColor(R.color.white));
    }
}
