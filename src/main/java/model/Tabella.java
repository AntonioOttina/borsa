package model;

import java.util.*;
import java.util.function.Function;

/**
 * Rappresenta una tabella immutabile, una struttura dati 2D simile a un foglio di calcolo.
 * È composta da un insieme di colonne nominate, tutte condividenti lo stesso indice di riga.
 *
 * @param <V> il tipo di dati omogeneo (o un super-tipo comune) per i valori nelle colonne.
 *
 * <p><strong>Funzione di Astrazione (AF):</strong>
 * AF(t) = Una tabella bidimensionale dove le righe sono identificate da {@code t.indiceRighe}
 * e le colonne sono identificate dai nomi (le chiavi di {@code t.colonne}).
 * Il valore nella cella (etichettaRiga, nomeColonna) è {@code t.colonne.get(nomeColonna).valoreDaEtichetta(etichettaRiga)}.
 *
 * <p><strong>Invariante di Rappresentazione (RI):</strong>
 * 1. {@code indiceRighe != null}.
 * 2. {@code colonne != null}.
 * 3. Nessuna chiave o valore in {@code colonne} è nullo.
 * 4. Per ogni {@code Colonna c} in {@code colonne.values()}, {@code c.indice().equals(indiceRighe)} deve essere {@code true}.
 * 5. Le chiavi (nomi delle colonne) in {@code colonne} sono uniche e non nulle.
 */
public class Tabella<V> {
    /** L'indice che etichetta le righe della tabella. */
    private final Indice indiceRighe;
    /** La mappa che associa i nomi delle colonne alle istanze di Colonna. Usa LinkedHashMap per mantenere l'ordine. */
    private final Map<String, Colonna<V>> colonne;

    /**
     * Costruisce una tabella a partire da un indice di riga e una lista di colonne.
     * Le colonne devono avere tutte lo stesso indice, uguale all'indice di riga fornito.
     * Alle colonne senza nome viene assegnato un nome di default ("Column_i").
     *
     * @param indiceRighe L'indice comune per tutte le righe.
     * @param colonneList La lista di colonne da cui creare la tabella.
     * @throws NullPointerException se indiceRighe o colonneList sono nulli.
     * @throws IllegalArgumentException se gli indici delle colonne non corrispondono
     * o se ci sono nomi di colonna duplicati.
     */
    public Tabella(Indice indiceRighe, List<Colonna<V>> colonneList) {
        Objects.requireNonNull(indiceRighe, "L'indice di riga non può essere nullo.");
        Objects.requireNonNull(colonneList, "La lista di colonne non può essere nulla.");
        this.indiceRighe = indiceRighe;
        Map<String, Colonna<V>> map = new LinkedHashMap<>();
        int colIdx = 0;
        for (Colonna<V> colonna : colonneList) {
            if (!colonna.indice().equals(indiceRighe))
                throw new IllegalArgumentException("Tutte le colonne devono avere il medesimo indice di riga.");
            String nomeColonna = colonna.nome();
            if (nomeColonna == null || nomeColonna.equals("Unnamed") || nomeColonna.trim().isEmpty()) {
                nomeColonna = "Column_" + colIdx;
            }
            if (map.containsKey(nomeColonna))
                throw new IllegalArgumentException("Nomi di colonna duplicati non ammessi: " + nomeColonna);
            map.put(nomeColonna, new Colonna<>(nomeColonna, colonna.indice(), colonna.valori()));
            colIdx++;
        }
        this.colonne = Collections.unmodifiableMap(map);
    }

    /**
     * Restituisce l'indice di riga della tabella.
     *
     * @return l'indice di riga.
     */
    public Indice getIndiceRighe() {
        return indiceRighe;
    }

    /**
     * Restituisce l'intestazione (i nomi delle colonne) della tabella.
     *
     * @return un insieme non modificabile dei nomi delle colonne.
     */
    public Set<String> getIntestazione() {
        return Collections.unmodifiableSet(colonne.keySet());
    }

    /**
     * Restituisce la colonna con il nome specificato.
     *
     * @param nomeColonna Il nome della colonna da restituire.
     * @return la {@code Colonna} corrispondente, o {@code null} se non esiste.
     */
    public Colonna<V> getColonna(String nomeColonna) {
        return colonne.get(nomeColonna);
    }

    /**
     * Restituisce il numero di colonne nella tabella.
     *
     * @return il numero di colonne.
     */
    public int getNumeroColonne() {
        return this.colonne.size();
    }

    /**
     * Restituisce il numero di righe nella tabella.
     *
     * @return il numero di righe.
     */
    public int getNumeroRighe() {
        return this.indiceRighe.getLunghezza();
    }

    /**
     * Restituisce il valore nella cella specificata da etichetta di riga e nome di colonna.
     *
     * @param etichettaRiga L'etichetta della riga.
     * @param nomeColonna   Il nome della colonna.
     * @return il valore nella cella specificata.
     * @throws IllegalArgumentException se la colonna non esiste.
     */
    public V valore(Object etichettaRiga, String nomeColonna) {
        Colonna<V> col = getColonna(nomeColonna);
        if (col == null) throw new IllegalArgumentException("Colonna non trovata: " + nomeColonna);
        return col.valoreDaEtichetta(etichettaRiga);
    }

    /**
     * Applica una funzione a ogni valore della tabella, producendo una nuova tabella.
     *
     * @param <U> il tipo dei valori della nuova tabella.
     * @param f   la funzione da applicare.
     * @return una nuova {@code Tabella} con i valori trasformati.
     */
    public <U> Tabella<U> map(Function<? super V, ? extends U> f) {
        List<Colonna<U>> nuoveColonne = new ArrayList<>();
        for (Colonna<V> col : this.colonne.values()) {
            nuoveColonne.add(col.map(f));
        }
        return new Tabella<>(this.indiceRighe, nuoveColonne);
    }

    /**
     * Impila questa tabella sopra un'altra (unione verticale).
     *
     * @param altra La tabella da impilare sotto questa.
     * @return una nuova {@code Tabella} risultato dell'impilamento.
     */
    public Tabella<V> impila(Tabella<V> altra) {
        Indice indiceRigheFuso = this.indiceRighe.fonde(altra.indiceRighe);
        Set<String> nomiColonneComuni = new LinkedHashSet<>(this.getIntestazione());
        nomiColonneComuni.addAll(altra.getIntestazione());
        List<Colonna<V>> colonneImpilate = new ArrayList<>();
        for (String nome : nomiColonneComuni) {
            Colonna<V> col1 = this.colonne.get(nome);
            Colonna<V> col2 = altra.colonne.get(nome);
            List<V> valori1 = (col1 != null) ? col1.valori() : Collections.nCopies(this.getNumeroRighe(), null);
            List<V> valori2 = (col2 != null) ? col2.valori() : Collections.nCopies(altra.getNumeroRighe(), null);
            List<V> valoriImpilati = new ArrayList<>(valori1);
            valoriImpilati.addAll(valori2);
            colonneImpilate.add(new Colonna<>(nome, indiceRigheFuso, valoriImpilati));
        }
        return new Tabella<>(indiceRigheFuso, colonneImpilate);
    }

    /**
     * Affianca questa tabella a un'altra (unione orizzontale).
     *
     * @param altra La tabella da affiancare a destra di questa.
     * @return una nuova {@code Tabella} risultato dell'affiancamento.
     * @throws IllegalArgumentException se le tabelle hanno nomi di colonna in comune.
     */
    public Tabella<V> affianca(Tabella<V> altra) {
        Indice indiceFuso = this.indiceRighe.fonde(altra.indiceRighe);
        Map<String, Colonna<V>> colonneAffiancate = new LinkedHashMap<>();
        this.colonne.values().forEach(col -> colonneAffiancate.put(col.nome(), col.reindicizza(indiceFuso)));
        altra.colonne.values().forEach(col -> {
            if (colonneAffiancate.containsKey(col.nome())) {
                throw new IllegalArgumentException("Le tabelle da affiancare non possono avere nomi di colonna in comune: " + col.nome());
            } else {
                colonneAffiancate.put(col.nome(), col.reindicizza(indiceFuso));
            }
        });
        return new Tabella<>(indiceFuso, new ArrayList<>(colonneAffiancate.values()));
    }

    /**
     * Crea una nuova tabella identica a questa ma con un nuovo indice di riga.
     *
     * @param nuovoIndice Il nuovo indice di riga.
     * @return una nuova {@code Tabella} con l'indice di riga aggiornato.
     * @throws IllegalArgumentException se la lunghezza del nuovo indice non corrisponde al numero di righe.
     */
    public Tabella<V> conIndiceDiRiga(Indice nuovoIndice) {
        if (this.getNumeroRighe() != nuovoIndice.getLunghezza()) {
            throw new IllegalArgumentException("La dimensione del nuovo indice di riga non corrisponde.");
        }
        List<Colonna<V>> nuoveColonne = new ArrayList<>();
        for (Colonna<V> c : this.colonne.values()) {
            nuoveColonne.add(new Colonna<>(c.nome(), nuovoIndice, c.valori()));
        }
        return new Tabella<>(nuovoIndice, nuoveColonne);
    }

    /**
     * Crea una nuova tabella identica a questa ma con nuove intestazioni (nomi di colonna).
     *
     * @param nuoveIntestazioni Un indice contenente i nuovi nomi per le colonne.
     * @return una nuova {@code Tabella} con le intestazioni aggiornate.
     * @throws IllegalArgumentException se il numero di intestazioni non corrisponde al numero di colonne.
     */
    public Tabella<V> conIntestazioni(Indice nuoveIntestazioni) {
        if (nuoveIntestazioni.getLunghezza() != this.getNumeroColonne()) {
            throw new IllegalArgumentException("Il numero di nuove intestazioni non corrisponde al numero di colonne.");
        }
        List<Colonna<V>> nuoveColonne = new ArrayList<>();
        List<Colonna<V>> vecchieColonne = new ArrayList<>(this.colonne.values());
        for (int i = 0; i < nuoveIntestazioni.getLunghezza(); i++) {
            String nuovoNome = nuoveIntestazioni.etichettaAllaPosizione(i).toString();
            Colonna<V> vecchiaColonna = vecchieColonne.get(i);
            nuoveColonne.add(new Colonna<>(nuovoNome, vecchiaColonna.indice(), vecchiaColonna.valori()));
        }
        return new Tabella<>(this.indiceRighe, nuoveColonne);
    }

    /**
     * Applica una funzione di trasformazione a ogni colonna della tabella.
     * La funzione riceve una colonna e ne restituisce una nuova, trasformata.
     *
     * @param <U> il tipo delle colonne risultanti.
     * @param func la funzione da applicare a ogni colonna.
     * @return una nuova {@code Tabella} con le colonne trasformate.
     * @throws IllegalStateException se la funzione fornita produce colonne con indici diversi.
     */
    public <U> Tabella<U> mapColumn(Function<Colonna<V>, Colonna<U>> func) {
        if (this.colonne.isEmpty()) {
            return new Tabella<>(new Indice("", Collections.emptyList()), Collections.emptyList());
        }

        List<Colonna<U>> nuoveColonne = new ArrayList<>();
        Indice nuovoIndiceRighe = null;

        for (String nomeColonna : this.colonne.keySet()) {
            Colonna<V> vecchiaColonna = this.colonne.get(nomeColonna);
            Colonna<U> colonnaTrasformata = func.apply(vecchiaColonna);
            Colonna<U> colonnaFinale = colonnaTrasformata.conNome(nomeColonna);

            if (nuovoIndiceRighe == null) {
                nuovoIndiceRighe = colonnaFinale.indice();
            } else {
                if (!nuovoIndiceRighe.equals(colonnaFinale.indice())) {
                    throw new IllegalStateException(
                            "La funzione fornita a mapColumn deve produrre colonne con lo stesso indice."
                    );
                }
            }
            nuoveColonne.add(colonnaFinale);
        }
        return new Tabella<>(nuovoIndiceRighe, nuoveColonne);
    }

    /**
     * Calcola la somma dei valori per ogni colonna e restituisce una nuova tabella
     * con i totali. I valori non numerici vengono ignorati.
     *
     * @return una nuova {@code Tabella} di interi contenente le somme per colonna.
     */
    public Tabella<Integer> somma() {
        final Indice resultIndex = new Indice("", Collections.singletonList(0));

        Function<Colonna<V>, Colonna<Integer>> sumFunction = col -> {
            int sum = 0;
            for (Object val : col.valori()) {
                if (val instanceof Number) {
                    sum += ((Number) val).intValue();
                } else if (val != null && !val.toString().trim().isEmpty()) {
                    try {
                        sum += Integer.parseInt(val.toString().trim());
                    } catch (NumberFormatException e) {
                        // Ignora valori non-interi
                    }
                }
            }
            return new Colonna<>(col.nome(), resultIndex, Collections.singletonList(sum));
        };

        return this.mapColumn(sumFunction);
    }
}