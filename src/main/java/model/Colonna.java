package model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rappresenta una colonna immutabile di dati.
 * Una colonna è una sequenza di valori, ciascuno associato a un'etichetta
 * tramite un indice. Ha un nome, un indice e una lista di valori.
 *
 * @param <V> il tipo dei valori contenuti nella colonna.
 * @param nome Il nome della colonna.
 * @param indice L'indice che associa etichette alle righe.
 * @param valori La lista dei valori della colonna.
 *
 * <p><strong>Funzione di Astrazione (AF):</strong>
 * AF(c) = Una colonna di dati con nome {@code c.nome}, dove la sequenza di valori {@code c.valori}
 * è etichettata dalla sequenza di etichette di {@code c.indice}.
 *
 * <p><strong>Invariante di Rappresentazione (RI):</strong>
 * 1. {@code nome != null}.
 * 2. {@code indice != null}.
 * 3. {@code valori != null}.
 * 4. {@code indice.getLunghezza() == valori.size()}.
 */
public record Colonna<V>(String nome, Indice indice, List<V> valori) {

    /**
     * Costruttore canonico per una {@code Colonna}.
     *
     * @param nome Il nome della colonna. Non può essere nullo.
     * @param indice L'indice della colonna. Non può essere nullo.
     * @param valori La lista di valori. Non può essere nulla.
     * @throws NullPointerException se nome, indice o valori sono nulli.
     * @throws IllegalArgumentException se la lunghezza dell'indice non corrisponde alla dimensione della lista di valori.
     */
    public Colonna(String nome, Indice indice, List<V> valori) {
        Objects.requireNonNull(nome, "Il nome della colonna non può essere nullo.");
        Objects.requireNonNull(indice, "L'indice non può essere nullo.");
        Objects.requireNonNull(valori, "La lista dei valori non può essere nulla.");
        if (indice.getLunghezza() != valori.size()) {
            throw new IllegalArgumentException(
                    "La lunghezza dell'indice (" + indice.getLunghezza() + ") e dei valori (" + valori.size() + ") deve coincidere.");
        }
        this.nome = nome;
        this.indice = indice;
        this.valori = new ArrayList<>(valori);
    }

    /**
     * Restituisce una vista non modificabile della lista dei valori.
     *
     * @return la lista dei valori.
     */
    @Override
    public List<V> valori() {
        return Collections.unmodifiableList(valori);
    }

    /**
     * Restituisce la dimensione (numero di righe) della colonna.
     *
     * @return la dimensione della colonna.
     */
    public int size() {
        return valori.size();
    }

    /**
     * Restituisce il valore corrispondente a una data etichetta dell'indice.
     *
     * @param etichetta L'etichetta della riga di cui ottenere il valore.
     * @return il valore corrispondente all'etichetta, o {@code null} se l'etichetta non è presente nell'indice.
     */
    public V valoreDaEtichetta(Object etichetta) {
        int pos = indice.posizioneDellEtichetta(etichetta);
        if (pos == -1) {
            return null;
        }
        return valori.get(pos);
    }

    /**
     * Crea una nuova colonna identica a questa ma con un nuovo nome.
     *
     * @param nuovoNome Il nuovo nome per la colonna.
     * @return una nuova {@code Colonna} con il nome aggiornato.
     */
    public Colonna<V> conNome(String nuovoNome) {
        return new Colonna<>(nuovoNome, this.indice, this.valori);
    }

    /**
     * Crea una nuova colonna con un nuovo indice.
     * Se il nuovo indice ha una lunghezza diversa, viene creato un indice numerico di default.
     *
     * @param nuovoIndice Il nuovo indice da associare alla colonna.
     * @return una nuova {@code Colonna} con l'indice aggiornato.
     */
    public Colonna<V> sostituisciIndice(Indice nuovoIndice) {
        if (this.size() == nuovoIndice.getLunghezza()) {
            return new Colonna<>(this.nome, nuovoIndice, this.valori);
        } else {
            List<Object> defaultLabels = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                defaultLabels.add(i);
            }
            Indice defaultIndex = new Indice("", defaultLabels);
            return new Colonna<>(this.nome, defaultIndex, this.valori);
        }
    }

    /**
     * Crea una nuova colonna riallineando i valori di questa colonna secondo un nuovo indice.
     * Per ogni etichetta nel nuovo indice, il valore corrispondente nella nuova colonna è:
     * - il valore di questa colonna se l'etichetta esiste nel suo indice.
     * - {@code null} altrimenti.
     *
     * @param nuovoIndice L'indice su cui basare la nuova colonna.
     * @return una nuova {@code Colonna} reindicizzata.
     */
    public Colonna<V> reindicizza(Indice nuovoIndice) {
        Map<Object, V> mappaValori = new LinkedHashMap<>();
        for (int i = 0; i < indice.getLunghezza(); i++) {
            mappaValori.put(indice.etichettaAllaPosizione(i), valori.get(i));
        }
        List<V> nuoviValori = new ArrayList<>();
        for (Object etichetta : nuovoIndice.etichette()) {
            nuoviValori.add(mappaValori.get(etichetta));
        }
        return new Colonna<>(this.nome, nuovoIndice, nuoviValori);
    }

    /**
     * Impila questa colonna sopra un'altra, creando una nuova colonna.
     * L'indice risultante è la fusione degli indici delle due colonne.
     * I valori sono la concatenazione dei valori delle due colonne.
     *
     * @param altra La colonna da impilare sotto questa.
     * @return una nuova {@code Colonna} risultato dell'impilamento.
     */
    public Colonna<V> impila(Colonna<V> altra) {
        Indice indiceFuso = this.indice.fonde(altra.indice());
        List<V> valoriFusi = new ArrayList<>(this.valori);
        valoriFusi.addAll(altra.valori());
        return new Colonna<>(this.nome, indiceFuso, valoriFusi);
    }

    /**
     * Applica una funzione a ogni valore della colonna, producendo una nuova colonna.
     * La nuova colonna avrà lo stesso indice ma valori di tipo potenzialmente diverso.
     *
     * @param <U> il tipo dei valori della nuova colonna.
     * @param f   la funzione da applicare a ogni valore.
     * @return una nuova {@code Colonna} con i valori trasformati.
     */
    public <U> Colonna<U> map(Function<? super V, ? extends U> f) {
        List<U> nuoviValori = this.valori.stream()
                .map(f)
                .collect(Collectors.toList());
        return new Colonna<>(this.nome, this.indice, nuoviValori);
    }

    /**
     * Restituisce una rappresentazione in stringa della colonna.
     *
     * @return una stringa che descrive la colonna.
     */
    @Override
    public String toString() {
        return "Colonna{nome='" + nome + "', valori=" + valori + "}";
    }
}