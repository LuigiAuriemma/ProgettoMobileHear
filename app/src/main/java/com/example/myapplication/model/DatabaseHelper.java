package com.example.myapplication.model;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "gestione_spese.db";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_UTENTI = "utenti";
    public static final String TABLE_SPESE = "spese";
    public static final String TABLE_OBIETTIVI = "obiettivi";

    private static final String CREATE_TABLE_UTENTI =
            "CREATE TABLE " + TABLE_UTENTI + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "cognome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL);";

    private static final String CREATE_TABLE_SPESE =
            "CREATE TABLE " + TABLE_SPESE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_utente INTEGER NOT NULL, " +
                    "importo REAL NOT NULL, " +
                    "descrizione TEXT, " +
                    "categoria TEXT NOT NULL, " +
                    "data_spesa TEXT, " +
                    "FOREIGN KEY (id_utente) REFERENCES utenti(id));";

    private static final String CREATE_TABLE_OBIETTIVI =
            "CREATE TABLE " + TABLE_OBIETTIVI + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_utente INTEGER NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "importo_target REAL NOT NULL, " +
                    "importo_rimanente REAL NOT NULL, " +
                    "FOREIGN KEY (id_utente) REFERENCES utenti(id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_UTENTI);
        db.execSQL(CREATE_TABLE_SPESE);
        db.execSQL(CREATE_TABLE_OBIETTIVI);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPESE);
        db.execSQL(CREATE_TABLE_SPESE);
    }

    public int getUtenteIdByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id FROM " + TABLE_UTENTI + " WHERE email = ? AND password = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        // Controlla se la query ha restituito risultati
        if (cursor != null && cursor.moveToFirst()) {
            // Ottieni l'indice della colonna "id"
            int columnIndex = cursor.getColumnIndex("id");

            // Verifica che la colonna esista (l'indice non deve essere -1)
            if (columnIndex != -1) {
                int userId = cursor.getInt(columnIndex);
                cursor.close();
                return userId;
            }
        }

        // Se non trovato o se la colonna non esiste, restituisci -1
        if (cursor != null) {
            cursor.close();
        }
        return -1;  // Se non trovato, restituisci -1 per indicare che non esiste un utente con quelle credenziali
    }
    public Utente getUtenteById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Utente utente = null;

        // Query SQL per ottenere i dettagli dell'utente in base all'ID
        String query = "SELECT * FROM " + TABLE_UTENTI + " WHERE id = ?";

        // Esegui la query
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            // Crea l'oggetto Utente con i dati recuperati
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            String cognome = cursor.getString(cursor.getColumnIndexOrThrow("cognome"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));

            // Crea l'oggetto Utente
            utente = new Utente(nome, cognome, email,password);
            utente.setId(id);
        }
        cursor.close();
        return utente;
    }
    public int updateUtenteData(int userId, String name, String surname, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nome", name);
        values.put("cognome", surname);
        values.put("email", email);
        values.put("password", password);

        // La condizione per identificare l'utente da aggiornare (per ID)
        String selection = "id = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        // Esegui l'aggiornamento dei dati
        int rowsAffected = db.update(TABLE_UTENTI, values, selection, selectionArgs);
        return rowsAffected;  // Restituisce il numero di righe aggiornate
    }
    public Utente getUtenteDataById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query per ottenere i dati dell'utente in base all'ID
        String query = "SELECT nome, cognome, email, password FROM " + TABLE_UTENTI + " WHERE id = ?";

        // Esegui la query
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        Utente utente = null;
        if (cursor.moveToFirst()) {
            // Controllo sicuro per ottenere l'indice delle colonne
            int nomeIndex = cursor.getColumnIndex("nome");
            int cognomeIndex = cursor.getColumnIndex("cognome");
            int emailIndex = cursor.getColumnIndex("email");
            int passwordIndex = cursor.getColumnIndex("password");

            // Verifica che le colonne esistano
            if (nomeIndex != -1 && cognomeIndex != -1 && emailIndex != -1 && passwordIndex != -1) {
                // Recupera i dati dell'utente
                String nome = cursor.getString(nomeIndex);
                String cognome = cursor.getString(cognomeIndex);
                String email = cursor.getString(emailIndex);
                String password = cursor.getString(passwordIndex);

                // Crea un oggetto Utente con i dati ottenuti
                utente = new Utente(nome, cognome, email, password);
                utente.setId(userId);
            }
        }

        cursor.close();
        return utente;
    }
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_UTENTI + " WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean isRegistered = cursor.moveToFirst();
        cursor.close();
        return isRegistered;
    }
    public long inserisciUtente(Utente utente) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nome", utente.getNome());
        values.put("cognome", utente.getCognome());
        values.put("email", utente.getEmail());
        values.put("password", utente.getPassword());

        // Inserisci l'utente nella tabella utenti
        long userId = db.insert(TABLE_UTENTI, null, values);
        return userId;
    }

    public List<Obiettivo> getAllObiettivi(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Obiettivo> obiettivi = new ArrayList<>();

        // Query per selezionare gli obiettivi dell'utente
        String query = "SELECT id, nome, importo_target, importo_rimanente FROM " + TABLE_OBIETTIVI + " WHERE id_utente = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                // Verifica che gli indici delle colonne siano validi
                int idIndex = cursor.getColumnIndex("id");
                int nomeIndex = cursor.getColumnIndex("nome");
                int targetIndex = cursor.getColumnIndex("importo_target");
                int rimanenteIndex = cursor.getColumnIndex("importo_rimanente");

                if (idIndex != -1 && nomeIndex != -1 && targetIndex != -1 && rimanenteIndex != -1) {
                    // Recupera i dati dalla riga corrente
                    int id = cursor.getInt(idIndex);
                    String nome = cursor.getString(nomeIndex);
                    float importoTarget = cursor.getFloat(targetIndex);
                    float importoRimanente = cursor.getFloat(rimanenteIndex);

                    // Crea un oggetto Obiettivo e aggiungilo alla lista
                    Obiettivo obiettivo = new Obiettivo(id, userId, nome, importoTarget);
                    obiettivo.setImportoRimanente(importoRimanente);
                    obiettivi.add(obiettivo);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return obiettivi;
    }
    public Obiettivo getObiettivoById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Obiettivo obiettivo = null;

        String query = "SELECT id, id_utente, nome, importo_target, importo_rimanente FROM obiettivi WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            int idUtente = cursor.getInt(cursor.getColumnIndexOrThrow("id_utente"));
            String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            float importoTarget = cursor.getFloat(cursor.getColumnIndexOrThrow("importo_target"));
            float importoRimanente = cursor.getFloat(cursor.getColumnIndexOrThrow("importo_rimanente"));

            obiettivo = new Obiettivo(id, idUtente, nome, importoTarget);
            obiettivo.setImportoRimanente(importoRimanente);

            cursor.close();
        }

        return obiettivo;
    }
    public boolean addObiettivo(int userId, String nome, float importoTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_utente", userId);
        values.put("nome", nome);
        values.put("importo_target", importoTarget);
        values.put("importo_rimanente", importoTarget); // L'importo rimanente inizia uguale al target

        long result = db.insert(TABLE_OBIETTIVI, null, values);
        return result != -1; // Restituisce true se l'inserimento ha avuto successo
    }
    public void deleteObiettivo(int obiettivoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBIETTIVI, "id = ?", new String[]{String.valueOf(obiettivoId)});
        db.close();
    }
    public void updateObiettivo(Obiettivo obiettivo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", obiettivo.getNome());
        values.put("importo_target", obiettivo.getImportoTarget());
        values.put("importo_rimanente", obiettivo.getImportoRimanente());
        db.update(TABLE_OBIETTIVI, values, "id = ?", new String[]{String.valueOf(obiettivo.getId())});
        db.close();
    }


    public long addSpesa(Spesa spesa) {
        SQLiteDatabase db = this.getWritableDatabase();
        int dbVersion = db.getVersion();
        Log.d("DatabaseVersion", "Current database version: " + dbVersion);


        ContentValues values = new ContentValues();
        values.put("id_utente", spesa.getIdUtente());
        values.put("importo", spesa.getImporto());
        values.put("descrizione", spesa.getDescrizione());
        values.put("categoria", spesa.getCategoria());
        values.put("data_spesa", spesa.getDataSpesa());

        // Debug: Controllo valori
        System.out.println("Valori inseriti: " + values);

        long id = db.insert(TABLE_SPESE, null, values);
        if (id == -1) {
            System.err.println("Errore durante l'inserimento della spesa");
        }
        return id;
    }
    public Spesa getSpesaById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Spesa spesa = null;

        // Query per ottenere la spesa con l'ID specifico
        String query = "SELECT * FROM " + TABLE_SPESE + " WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            // Estrai i valori dal cursore
            int idSpesa = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int idUtente = cursor.getInt(cursor.getColumnIndexOrThrow("id_utente"));
            float importo = (float) cursor.getDouble(cursor.getColumnIndexOrThrow("importo"));
            String descrizione = cursor.getString(cursor.getColumnIndexOrThrow("descrizione"));
            String idCategoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
            String dataSpesa = cursor.getString(cursor.getColumnIndexOrThrow("data_spesa"));

            // Crea l'oggetto Spesa
            spesa = new Spesa(idUtente, importo, descrizione, idCategoria);
            spesa.setId(idSpesa);
            spesa.setDataSpesa(dataSpesa);
        }
        if (cursor != null) {
            cursor.close();
        }
        return spesa;
    }
    public boolean updateSpesa(Spesa spesa) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepara i valori da aggiornare
        ContentValues values = new ContentValues();
        values.put("id_utente", spesa.getIdUtente());
        values.put("importo", spesa.getImporto());
        values.put("descrizione", spesa.getDescrizione());
        values.put("categoria", spesa.getCategoria());
        values.put("data_spesa", spesa.getDataSpesa()); // Se desideri aggiornare la data

        // Esegui l'aggiornamento
        int rowsAffected = db.update(
                TABLE_SPESE,          // Nome della tabella
                values,               // Valori da aggiornare
                "id = ?",             // Clausola WHERE
                new String[]{String.valueOf(spesa.getId())} // Argomento WHERE
        );

        db.close();
        return rowsAffected > 0; // Restituisce true se almeno una riga è stata aggiornata
    }
    public List<Spesa> getAllSpese(int userId) {
        List<Spesa> speseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query per selezionare tutte le righe dalla tabella spese
        String query = "SELECT * FROM " + TABLE_SPESE + " WHERE id_utente = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                // Creazione dell'oggetto Spesa da ogni riga
                Spesa spesa = new Spesa(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_utente")), // ID Utente
                        cursor.getFloat(cursor.getColumnIndexOrThrow("importo")), // Importo
                        cursor.getString(cursor.getColumnIndexOrThrow("descrizione")), // Descrizione
                        cursor.getString(cursor.getColumnIndexOrThrow("categoria")) // Categoria
                );
                spesa.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id"))); //idSpesa
                spesa.setDataSpesa(cursor.getString(cursor.getColumnIndexOrThrow("data_spesa"))); //dataSpesa

                // Aggiunta alla lista
                speseList.add(spesa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return speseList;
    }
    public void deleteSpesa(int spesaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SPESE, "id = ?", new String[]{String.valueOf(spesaId)});
        db.close();
    }
    public List<Spesa> getSpeseTraDateAndCategoria(String dataIniziale, String dataFinale, String categoria, int idUtente) {
        List<Spesa> speseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query SQL per selezionare tutte le spese tra due date e per un determinato utente
        String query;
        Cursor cursor;

        if(categoria != null) {
            query = "SELECT * FROM " + TABLE_SPESE +
                    " WHERE id_utente = ? AND DATE(data_spesa) BETWEEN ? AND ? AND categoria = ?";

            // Esegui la query con i parametri: idUtente, dataIniziale, dataFinale e categoria
            cursor = db.rawQuery(query, new String[]{String.valueOf(idUtente), dataIniziale, dataFinale, categoria});
        } else {
            query = "SELECT * FROM " + TABLE_SPESE +
                    " WHERE id_utente = ? AND DATE(data_spesa) BETWEEN ? AND ?";

            // Esegui la query con i parametri: idUtente, dataIniziale e dataFinale
            cursor = db.rawQuery(query, new String[]{String.valueOf(idUtente), dataIniziale, dataFinale});
        }

        if (cursor.moveToFirst()) {
            do {
                // Creazione dell'oggetto Spesa per ogni riga
                Spesa spesa = new Spesa(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_utente")), // ID Utente
                        cursor.getFloat(cursor.getColumnIndexOrThrow("importo")), // Importo
                        cursor.getString(cursor.getColumnIndexOrThrow("descrizione")), // Descrizione
                        cursor.getString(cursor.getColumnIndexOrThrow("categoria")) // Categoria
                );
                spesa.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id"))); // Imposta ID
                spesa.setDataSpesa(cursor.getString(cursor.getColumnIndexOrThrow("data_spesa"))); // Imposta data spesa

                // Aggiungi la spesa alla lista
                speseList.add(spesa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return speseList;
    }

    public List<Spesa> getSpeseCategoria(String categoria, int idUtente) {
        List<Spesa> speseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query SQL per selezionare tutte le spese tra due date e per un determinato utente
        String query = "SELECT * FROM " + TABLE_SPESE +
                " WHERE id_utente = ? AND categoria = ?";

        // Esegui la query con i parametri: idUtente, dataIniziale e dataFinale
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idUtente), categoria});

        if (cursor.moveToFirst()) {
            do {
                // Creazione dell'oggetto Spesa per ogni riga
                Spesa spesa = new Spesa(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_utente")), // ID Utente
                        cursor.getFloat(cursor.getColumnIndexOrThrow("importo")), // Importo
                        cursor.getString(cursor.getColumnIndexOrThrow("descrizione")), // Descrizione
                        cursor.getString(cursor.getColumnIndexOrThrow("categoria")) // Categoria
                );
                spesa.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id"))); // Imposta ID
                spesa.setDataSpesa(cursor.getString(cursor.getColumnIndexOrThrow("data_spesa"))); // Imposta data spesa

                // Aggiungi la spesa alla lista
                speseList.add(spesa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return speseList;
    }
    public float getSommaSpeseTraDate(String dataIniziale, String dataFinale,int idUtente) {
        float sommaTotale = 0.0f; // Variabile per accumulare la somma delle spese
        SQLiteDatabase db = this.getReadableDatabase();

        // Query SQL per selezionare tutte le spese tra due date (senza l'ora)
        String query = "SELECT * FROM " + TABLE_SPESE +
                " WHERE id_utente = ? AND DATE(data_spesa) BETWEEN ? AND ?";

        // Esegui la query con le date come parametri
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idUtente),dataIniziale, dataFinale});

        if (cursor.moveToFirst()) {
            do {
                // Ottieni l'importo di ogni spesa e accumula nel totale
                float importo = cursor.getFloat(cursor.getColumnIndexOrThrow("importo"));
                sommaTotale += importo; // Aggiungi l'importo alla somma totale
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sommaTotale; // Restituisci la somma totale delle spese
    }

    public HashMap<String, Float> getCategorieSpeseTraDate(String dataIniziale, String dataFinale, int idUtente) {
        HashMap<String, Float> hashMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query SQL per selezionare tutte le spese tra due date e per un determinato utente
        String query = "SELECT * FROM " + TABLE_SPESE +
                " WHERE id_utente = ? AND DATE(data_spesa) BETWEEN ? AND ?";

        // Esegui la query con i parametri: idUtente, dataIniziale e dataFinale
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idUtente), dataIniziale, dataFinale});

        if (cursor.moveToFirst()) {
            do {
                String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                float importo = cursor.getFloat(cursor.getColumnIndexOrThrow("importo"));

                // Verifica se la categoria è già presente nella mappa
                if (hashMap.containsKey(categoria)) {
                    // Se la categoria è presente, somma l'importo esistente con il nuovo importo
                    float existingAmount = hashMap.get(categoria);
                    hashMap.put(categoria, existingAmount + importo);  // Somma l'importo
                } else {
                    // Se la categoria non è presente, aggiungila alla mappa
                    hashMap.put(categoria, importo);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return hashMap;
    }

}
