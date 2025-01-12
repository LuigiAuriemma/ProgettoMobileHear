package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "gestione_spese.db";
    private static final int DATABASE_VERSION = 4; // Aggiorniamo la versione del DB
    public static final String TABLE_UTENTI = "utenti";
    public static final String TABLE_SPESE = "spese";
    public static final String TABLE_RISPARMI = "risparmi";
    public static final String TABLE_CATEGORIE = "categorie";
    public static final String TABLE_OBIETTIVI = "obiettivi";  // Nuova tabella obiettivi

    private static final String CREATE_TABLE_UTENTI =
            "CREATE TABLE " + TABLE_UTENTI + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "cognome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_TABLE_SPESE =
            "CREATE TABLE " + TABLE_SPESE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_utente INTEGER NOT NULL, " +
                    "importo REAL NOT NULL, " +
                    "descrizione TEXT, " +
                    "id_categoria INTEGER NOT NULL, " +
                    "data_spesa TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_utente) REFERENCES utenti(id), " +
                    "FOREIGN KEY (id_categoria) REFERENCES categorie(id));";

    private static final String CREATE_TABLE_RISPARMI =
            "CREATE TABLE " + TABLE_RISPARMI + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_utente INTEGER NOT NULL, " +
                    "importo REAL NOT NULL, " +
                    "descrizione TEXT, " +
                    "id_obiettivo INTEGER, " +
                    "data_risparmio TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_utente) REFERENCES utenti(id), " +
                    "FOREIGN KEY (id_obiettivo) REFERENCES obiettivi(id));";

    private static final String CREATE_TABLE_CATEGORIE =
            "CREATE TABLE " + TABLE_CATEGORIE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL UNIQUE);";

    private static final String CREATE_TABLE_OBIETTIVI =
            "CREATE TABLE " + TABLE_OBIETTIVI + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_utente INTEGER NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "importo_target REAL NOT NULL, " +
                    "importo_rimanente REAL NOT NULL, " + // Nuovo campo
                    "FOREIGN KEY (id_utente) REFERENCES utenti(id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_UTENTI);
        db.execSQL(CREATE_TABLE_CATEGORIE);
        db.execSQL(CREATE_TABLE_SPESE);
        db.execSQL(CREATE_TABLE_RISPARMI);
        db.execSQL(CREATE_TABLE_OBIETTIVI);  // Creiamo anche la tabella obiettivi
        // Inserisci alcune categorie predefinite
        inserisciCategoria(db, "Utenze");
        inserisciCategoria(db, "Alimentari");
        inserisciCategoria(db, "Trasporti");
        inserisciCategoria(db, "Abbigliamento");
        inserisciCategoria(db, "Affitto");
        inserisciCategoria(db, "Svago");
        inserisciCategoria(db, "Ristoranti");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_CATEGORIE); // Aggiungiamo la tabella categorie
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPESE);
            db.execSQL(CREATE_TABLE_SPESE); // Riprogettiamo la tabella spese
        }

        if (oldVersion < 4) {
            db.execSQL(CREATE_TABLE_OBIETTIVI); // Creiamo la nuova tabella obiettivi
            db.execSQL("ALTER TABLE " + TABLE_RISPARMI + " ADD COLUMN id_obiettivo INTEGER"); // Aggiungiamo id_obiettivo alla tabella risparmi
        }
    }


    public void inserisciObiettivo(int idUtente, String nome, Double importoTarget, Double importoRimanente) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idUtente", idUtente);
        values.put("nome", nome);
        values.put("importoTarget", importoTarget);
        values.put("importoRimanente", importoRimanente);
        db.insert(TABLE_OBIETTIVI, null, values);
        db.close();
    }
    public void inserisciUtente(String nome,String cognome, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("cognome", cognome);
        values.put("email", email);
        values.put("password", password);
        db.insert(TABLE_UTENTI, null, values);
        db.close();
    }
    private void inserisciCategoria(SQLiteDatabase db, String nomeCategoria) {
        ContentValues values = new ContentValues();
        values.put("nome", nomeCategoria);
        db.insert(TABLE_CATEGORIE, null, values);
    }
    public void inserisciSpesa(int idUtente, double importo, String descrizione, int idCategoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_utente", idUtente);
        values.put("importo", importo);
        values.put("descrizione", descrizione);
        values.put("id_categoria", idCategoria);
        db.insert(TABLE_SPESE, null, values);
        db.close();
    }
    public void inserisciRisparmio(int idUtente, double importo, String descrizione, int idObiettivo) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Aggiungi il risparmio alla tabella risparmi
        ContentValues values = new ContentValues();
        values.put("id_utente", idUtente);
        values.put("importo", importo);
        values.put("descrizione", descrizione);
        values.put("id_obiettivo", idObiettivo);
        db.insert(TABLE_RISPARMI, null, values);

        // 2. Calcola il nuovo importo rimanente per l'obiettivo
        Cursor cursor = db.rawQuery(
                "SELECT SUM(importo) FROM " + TABLE_RISPARMI + " WHERE id_obiettivo = ?",
                new String[]{String.valueOf(idObiettivo)}
        );

        if (cursor != null) {
            cursor.moveToFirst();
            double totaleRisparmi = cursor.getDouble(0);
            cursor.close();

            // 3. Aggiorna l'importo rimanente dell'obiettivo
            double nuovoImportoRimanente = getImportoTarget(idObiettivo) - totaleRisparmi;

            ContentValues updateValues = new ContentValues();
            updateValues.put("importo_rimanente", nuovoImportoRimanente);
            db.update(TABLE_OBIETTIVI, updateValues, "id = ?", new String[]{String.valueOf(idObiettivo)});
        }

        db.close();
    }
    public double getImportoTarget(int idObiettivo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_OBIETTIVI, new String[]{"importo_target"}, "id = ?",
                new String[]{String.valueOf(idObiettivo)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            @SuppressLint("Range") double target = cursor.getDouble(cursor.getColumnIndex("importo_target"));
            cursor.close();
            return target;
        }
        return 0;
    }
    public Cursor getObiettiviPerUtente(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_OBIETTIVI, null, "id_utente = ?", new String[]{String.valueOf(idUtente)}, null, null, null);
    }
    public Cursor getRisparmiPerUtente(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RISPARMI, null, "id_utente = ?", new String[]{String.valueOf(idUtente)}, null, null, null);
    }
    public Cursor getCategorie() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIE, null, null, null, null, null, null);
    }
    public Cursor getSpesePerUtente(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SPESE, null, "id_utente = ?", new String[]{String.valueOf(idUtente)}, null, null, null);
    }



    //metodi riguardanti le spese
    public double calcolaTotaleSpese(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(importo) FROM " + TABLE_SPESE + " WHERE id_utente = ?", new String[]{String.valueOf(idUtente)});
        if (cursor != null) {
            cursor.moveToFirst();
            double totale = cursor.getDouble(0); // Prende il risultato della somma
            cursor.close();
            return totale;
        }
        return 0;
    }
    /*startDate e endDate devono essere passati nel formato YYYY-MM-DD.
    Ad esempio, per ottenere tutte le spese di gennaio 2025,
    puoi passare startDate = "2025-01-01" e endDate = "2025-01-31"*/public double calcolaTotaleSpesePerPeriodo(int idUtente, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        // La query filtra per la data della spesa (startDate e endDate sono in formato 'YYYY-MM-DD')
        Cursor cursor = db.rawQuery(
                "SELECT SUM(importo) FROM " + TABLE_SPESE + " WHERE id_utente = ? AND data_spesa BETWEEN ? AND ?",
                new String[]{String.valueOf(idUtente), startDate, endDate}
        );
        if (cursor != null) {
            cursor.moveToFirst();
            double totale = cursor.getDouble(0); // Prende il risultato della somma
            cursor.close();
            return totale;
        }
        return 0;
    }
    public Cursor getSpeseByCategoria(int idCategoria) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SPESE, null, "id_categoria = ?", new String[]{String.valueOf(idCategoria)}, null, null, null);
    }
    public void modificaSpesa(int idSpesa, double nuovoImporto, String nuovaDescrizione, int nuovaCategoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("importo", nuovoImporto);
        values.put("descrizione", nuovaDescrizione);
        values.put("id_categoria", nuovaCategoria);
        db.update(TABLE_SPESE, values, "id = ?", new String[]{String.valueOf(idSpesa)});
        db.close();
    }
    public void eliminaSpesa(int idSpesa) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SPESE, "id = ?", new String[]{String.valueOf(idSpesa)});
        db.close();
    }
    public Cursor getSpeseInOrdineCronologico(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SPESE, null, "id_utente = ?", new String[]{String.valueOf(idUtente)}, null, null, "data_spesa DESC");
    }
    public int getNumeroSpesePerPeriodo(int idUtente, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_SPESE + " WHERE id_utente = ? AND data_spesa BETWEEN ? AND ?",
                new String[]{String.valueOf(idUtente), startDate, endDate}
        );
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0); // Ottiene il numero di spese
            cursor.close();
            return count;
        }
        return 0;
    }
    public Cursor getSpeseByCategoriaInPeriodo(int idCategoria, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_SPESE + " WHERE id_categoria = ? AND data_spesa BETWEEN ? AND ?",
                new String[]{String.valueOf(idCategoria), startDate, endDate}
        );
    }
    public Cursor getTotaleSpesePerCategoria(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT id_categoria, SUM(importo) FROM " + TABLE_SPESE + " WHERE id_utente = ? GROUP BY id_categoria",
                new String[]{String.valueOf(idUtente)}
        );
    }
    public void aggiornaCategoriaSpesa(int idSpesa, int nuovaCategoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_categoria", nuovaCategoria);
        db.update(TABLE_SPESE, values, "id = ?", new String[]{String.valueOf(idSpesa)});
        db.close();
    }


    //metodi riguardanti i risparmi
    public double calcolaTotaleRisparmi(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(importo) FROM " + TABLE_RISPARMI + " WHERE id_utente = ?", new String[]{String.valueOf(idUtente)});
        if (cursor != null) {
            cursor.moveToFirst();
            double totale = cursor.getDouble(0); // Prende il risultato della somma
            cursor.close();
            return totale;
        }
        return 0;
    }
    public void modificaRisparmio(int idRisparmio, double nuovoImporto, String nuovaDescrizione) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("importo", nuovoImporto);
        values.put("descrizione", nuovaDescrizione);
        db.update(TABLE_RISPARMI, values, "id = ?", new String[]{String.valueOf(idRisparmio)});
        db.close();
    }
    public void eliminaRisparmio(int idRisparmio) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RISPARMI, "id = ?", new String[]{String.valueOf(idRisparmio)});
        db.close();
    }
    /*startDate e endDate devono essere passati nel formato YYYY-MM-DD.
    Ad esempio, per ottenere tutti i risparmi di gennaio 2025,
    puoi passare startDate = "2025-01-01" e endDate = "2025-01-31"*/
    public double calcolaTotaleRisparmiPerPeriodo(int idUtente, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        // La query filtra per la data del risparmio (startDate e endDate sono in formato 'YYYY-MM-DD')
        Cursor cursor = db.rawQuery(
                "SELECT SUM(importo) FROM " + TABLE_RISPARMI + " WHERE id_utente = ? AND data_risparmio BETWEEN ? AND ?",
                new String[]{String.valueOf(idUtente), startDate, endDate}
        );
        if (cursor != null) {
            cursor.moveToFirst();
            double totale = cursor.getDouble(0); // Prende il risultato della somma
            cursor.close();
            return totale;
        }
        return 0;
    }
    public Cursor getRisparmiInOrdineCronologico(int idUtente) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RISPARMI, null, "id_utente = ?", new String[]{String.valueOf(idUtente)}, null, null, "data_risparmio DESC");
    }
    public int getNumeroRisparmiPerPeriodo(int idUtente, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_RISPARMI + " WHERE id_utente = ? AND data_risparmio BETWEEN ? AND ?",
                new String[]{String.valueOf(idUtente), startDate, endDate}
        );
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0); // Ottiene il numero di risparmi
            cursor.close();
            return count;
        }
        return 0;
    }
}
