package model;

import utils.InputParsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta un indice immutabile, simile a un asse di un DataFrame o di un Tensore.
 * Un indice può essere definito in tre modi:
 * <ul>
 * <li><b>Esplicito:</b> tramite una lista di etichette (oggetti generici).</li>
 * <li><b>Numerico:</b> come una sequenza di numeri definita da un inizio, una fine e un passo.</li>
 * <li><b>Fuso (Fused):</b> come la combinazione di due altri indici.</li>
 * </ul>
 * La classe è progettata per essere efficiente in termini di memoria, specialmente per gli indici numerici
 * e fusi, che non memorizzano esplicitamente tutte le loro etichette.
 * Tutte le operazioni che modificano un indice (es. {@link #fonde(Indice)}) restituiscono una nuova istanza
 * di {@code Indice}, mantenendo l'immutabilità.
 */
public class Indice {

    /** Il nome dell'indice, utilizzato per identificarlo. Può essere una stringa vuota. */
    private final String nome;

    /** La lista esplicita di etichette. È {@code null} per indici numerici o fusi. */
    private final List<Object> etichette;

    /** Flag che indica se l'indice è una sequenza numerica. */
    private final boolean isNumeric;
    /** Valore iniziale per un indice numerico. */
    private final long inizio;
    /** Valore finale (escluso) per un indice numerico. */
    private final long fine;
    /** Passo utilizzato per generare le etichette in un indice numerico. */
    private final long passo;

    /** Il numero totale di etichette nell'indice. */
    private final int lunghezza;

    /** Flag che indica se l'indice è il risultato della fusione di due altri indici. */
    private final boolean isFused;
    /** Il primo indice in una fusione. */
    private final Indice first;
    /** Il secondo indice in una fusione. */
    private final Indice second;

    /**
     * Costruisce un indice basato su una lista esplicita di etichette.
     * La lista di etichette fornita viene copiata per garantire l'immutabilità.
     *
     * @param nome Il nome dell'indice. Se {@code null}, viene utilizzato un nome vuoto.
     * @param etichette La lista di etichette per questo indice.
     * @throws NullPointerException se la lista di etichette è {@code null}.
     */
    public Indice(String nome, List<Object> etichette) {
        Objects.requireNonNull(etichette, "La lista di etichette non può essere nulla.");
        this.nome = (nome == null) ? "" : nome;
        this.etichette = List.copyOf(etichette);
        this.isNumeric = false;
        this.isFused = false;
        this.lunghezza = this.etichette.size();
        this.inizio = 0;
        this.fine = 0;
        this.passo = 0;
        this.first = null;
        this.second = null;
    }

    /**
     * Costruttore privato per creare un indice numerico.
     * Le etichette non vengono memorizzate esplicitamente ma calcolate su richiesta.
     *
     * @param nome Il nome dell'indice.
     * @param inizio Il valore iniziale della sequenza (incluso).
     * @param fine Il valore finale della sequenza (escluso).
     * @param passo Il passo tra i valori. Non può essere zero.
     * @throws IllegalArgumentException se il passo è zero.
     */
    private Indice(String nome, long inizio, long fine, long passo) {
        if (passo == 0) throw new IllegalArgumentException("Il passo non può essere zero.");
        this.nome = (nome == null) ? "" : nome;
        this.isNumeric = true;
        this.isFused = false;
        this.inizio = inizio;
        this.fine = fine;
        this.passo = passo;
        this.lunghezza = calculateLength(inizio, fine, passo);
        this.etichette = null;
        this.first = null;
        this.second = null;
    }

    /**
     * Costruttore privato per creare un indice "fuso", risultato della combinazione di altri due.
     * Le etichette non vengono memorizzate esplicitamente.
     *
     * @param nome Il nome del nuovo indice fuso.
     * @param first Il primo indice da fondere.
     * @param second Il secondo indice da fondere.
     * @throws NullPointerException se uno dei due indici è {@code null}.
     */
    private Indice(String nome, Indice first, Indice second) {
        this.nome = nome;
        this.isFused = true;
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
        this.lunghezza = first.getLunghezza() + second.getLunghezza();
        this.isNumeric = false;
        this.etichette = null;
        this.inizio = 0;
        this.fine = 0;
        this.passo = 0;
    }

    /**
     * Calcola la lunghezza di una sequenza numerica dati inizio, fine e passo.
     *
     * @param inizio L'inizio della sequenza.
     * @param fine La fine della sequenza.
     * @param passo Il passo.
     * @return Il numero di elementi nella sequenza.
     */
    private static int calculateLength(long inizio, long fine, long passo) {
        if (passo > 0) {
            if (inizio >= fine) return 0;
            return (int) ((fine - 1 - inizio) / passo + 1);
        } else { // passo < 0
            if (inizio <= fine) return 0;
            return (int) ((inizio - 1 - fine) / (-passo) + 1);
        }
    }

    /**
     * Metodo factory per creare un {@code Indice} da una serie di stringhe.
     * Ogni stringa viene parsata per dedurne il tipo (es. Numero, Stringa).
     *
     * @param labels Le etichette in formato stringa da cui creare l'indice.
     * @return Un nuovo {@code Indice} con nome vuoto e le etichette parsate.
     */
    public static Indice fromStrings(String... labels) {
        List<Object> etichette = new ArrayList<>();
        if (labels != null) {
            for (String s : labels) {
                etichette.add(InputParsing.parseValues(s, 1)[0]);
            }
        }
        return new Indice("", etichette);
    }

    /**
     * Metodo factory per creare un {@code Indice} numerico.
     *
     * @param nome Il nome dell'indice.
     * @param inizio Il valore iniziale della sequenza (incluso).
     * @param fine Il valore finale della sequenza (escluso).
     * @param passo Il passo tra i valori.
     * @return Un nuovo {@code Indice} numerico.
     */
    public static Indice numerico(String nome, long inizio, long fine, long passo) {
        return new Indice(nome, inizio, fine, passo);
    }

    /**
     * Metodo factory overload per creare un {@code Indice} numerico con parametri di tipo {@code int}.
     *
     * @param nome Il nome dell'indice.
     * @param inizio Il valore iniziale della sequenza (incluso).
     * @param fine Il valore finale della sequenza (escluso).
     * @param passo Il passo tra i valori.
     * @return Un nuovo {@code Indice} numerico.
     */
    public static Indice numerico(String nome, int inizio, int fine, int passo) {
        return new Indice(nome, (long) inizio, (long) fine, (long) passo);
    }

    /**
     * Restituisce il numero totale di etichette nell'indice.
     *
     * @return La lunghezza dell'indice.
     */
    public int getLunghezza() {
        return this.lunghezza;
    }

    /**
     * Restituisce l'etichetta alla posizione specificata.
     *
     * @param posizione La posizione (basata su zero) dell'etichetta da recuperare.
     * @return L'etichetta alla posizione data.
     * @throws IndexOutOfBoundsException se la posizione è fuori dai limiti dell'indice.
     */
    public Object etichettaAllaPosizione(int posizione) {
        if (posizione < 0 || posizione >= lunghezza) {
            throw new IndexOutOfBoundsException("Posizione " + posizione + " non valida per indice di lunghezza " + lunghezza);
        }
        if (isFused) {
            if (posizione < first.getLunghezza()) {
                return first.etichettaAllaPosizione(posizione);
            } else {
                return second.etichettaAllaPosizione(posizione - first.getLunghezza());
            }
        }
        if (isNumeric) {
            return inizio + (long) posizione * passo;
        }
        return etichette.get(posizione);
    }

    /**
     * Restituisce la posizione (indice intero) della prima occorrenza dell'etichetta specificata.
     *
     * @param etichetta L'etichetta da cercare.
     * @return La posizione dell'etichetta; -1 se l'etichetta non è presente.
     */
    public int posizioneDellEtichetta(Object etichetta) {
        if (etichetta == null) return -1;
        if (isFused) {
            int pos = first.posizioneDellEtichetta(etichetta);
            if (pos != -1) return pos;
            pos = second.posizioneDellEtichetta(etichetta);
            return (pos != -1) ? first.getLunghezza() + pos : -1;
        }
        if (isNumeric) {
            if (!(etichetta instanceof Number)) return -1;
            long val;
            // Gestisce Double e altri tipi numerici
            if (etichetta instanceof Double) {
                if (((Double) etichetta) % 1 != 0) return -1; // Non è un intero
                val = ((Double) etichetta).longValue();
            } else {
                val = ((Number) etichetta).longValue();
            }

            // Controlla se il valore è nel range
            if ((passo > 0 && (val < inizio || val >= fine)) || (passo < 0 && (val > inizio || val <= fine))) {
                return -1;
            }
            // Controlla se il valore è un multiplo del passo
            if ((val - inizio) % passo == 0) {
                long pos = (val - inizio) / passo;
                if (pos >= 0 && pos < Integer.MAX_VALUE) return (int) pos;
            }
            return -1;
        }
        return etichette.indexOf(etichetta);
    }

    /**
     * Crea un nuovo indice fondendo l'indice corrente con un altro.
     * Il nuovo indice conterrà prima tutte le etichette dell'indice corrente,
     * seguite dalle etichette dell'altro indice che non sono già presenti nel corrente.
     *
     * @param altro L'indice da fondere con quello corrente.
     * @return Un nuovo {@code Indice} risultato della fusione.
     * @throws NullPointerException se l'altro indice è {@code null}.
     */
    public Indice fonde(Indice altro) {
        Objects.requireNonNull(altro, "L'altro indice non può essere nullo.");
        List<Object> uniciDiAltro = new ArrayList<>();
        for (Object etichetta : altro.etichette()) {
            if (!this.contiene(etichetta)) {
                uniciDiAltro.add(etichetta);
            }
        }
        // Crea un nuovo indice fondendo `this` con un nuovo indice contenente solo le etichette uniche di `altro`
        return new Indice(this.nome, this, new Indice(altro.nome(), uniciDiAltro));
    }

    /**
     * Restituisce una lista contenente le ultime {@code n} etichette dell'indice.
     * Se {@code n} è maggiore della lunghezza dell'indice, vengono restituite tutte le etichette.
     *
     * @param n Il numero di ultime etichette da restituire.
     * @return Una lista contenente le ultime {@code n} etichette. Se n <= 0, restituisce una lista vuota.
     */
    public List<Object> ultimeEtichette(int n) {
        if (n <= 0) return Collections.emptyList();
        int numToTake = Math.min(n, this.lunghezza);
        if (isFused) {
            // Ottiene le ultime etichette dal secondo indice, e se non bastano, dal primo
            List<Object> result = new ArrayList<>(second.ultimeEtichette(numToTake));
            if (result.size() < numToTake) {
                result.addAll(0, first.ultimeEtichette(numToTake - result.size()));
            }
            return result;
        }
        List<Object> result = new ArrayList<>();
        if (isNumeric) {
            long firstLabelIndex = (long) this.lunghezza - numToTake;
            for (long i = firstLabelIndex; i < this.lunghezza; i++) {
                result.add(this.inizio + i * this.passo);
            }
        } else {
            result.addAll(this.etichette.subList(this.lunghezza - numToTake, this.lunghezza));
        }
        return result;
    }

    /**
     * Controlla se l'indice contiene l'etichetta specificata.
     *
     * @param etichetta L'etichetta da cercare.
     * @return {@code true} se l'etichetta è presente, {@code false} altrimenti.
     * @see #posizioneDellEtichetta(Object)
     */
    public boolean contiene(Object etichetta) {
        return posizioneDellEtichetta(etichetta) != -1;
    }

    /**
     * Combina le etichette uniche di un altro indice con le ultime etichette dell'indice corrente
     * per formare una lista di dimensione {@code n}.
     * La lista risultante conterrà le etichette uniche di `altro` alla fine, precedute
     * dalle ultime etichette dell'indice corrente necessarie per raggiungere la dimensione {@code n}.
     *
     * @param altro L'altro indice da cui prendere le etichette uniche.
     * @param n La dimensione desiderata della lista finale.
     * @return Una lista di oggetti di dimensione {@code n} (o inferiore se il totale delle etichette disponibili è minore di n).
     * @throws NullPointerException se l'altro indice è {@code null}.
     */
    public List<Object> fondeEUltimi(Indice altro, int n) {
        Objects.requireNonNull(altro, "L'altro indice non può essere nullo.");
        // Trova le etichette in `altro` che non sono in `this`
        List<Object> uniciInAltro = new ArrayList<>();
        for (Object etichetta : altro.etichette()) {
            if (!this.contiene(etichetta)) {
                uniciInAltro.add(etichetta);
            }
        }

        List<Object> risultatoFinale = new ArrayList<>();
        int numDaAltro = uniciInAltro.size();

        if (numDaAltro >= n) {
            // Se le etichette uniche di `altro` sono sufficienti, prendi le ultime n
            risultatoFinale.addAll(uniciInAltro.subList(numDaAltro - n, numDaAltro));
        } else {
            // Altrimenti, prendi le ultime etichette da `this` e aggiungi tutte le uniche di `altro`
            risultatoFinale.addAll(this.ultimeEtichette(n - numDaAltro));
            risultatoFinale.addAll(uniciInAltro);
        }
        return risultatoFinale;
    }

    /**
     * Restituisce il nome dell'indice.
     *
     * @return Il nome dell'indice.
     */
    public String nome() {
        return nome;
    }

    /**
     * Restituisce una vista non modificabile della lista di tutte le etichette nell'indice.
     * <p>
     * Attenzione: per indici di tipo numerico o fuso, questa operazione comporta la
     * generazione e la materializzazione in memoria di tutte le etichette,
     * operazione che potrebbe essere costosa per indici molto grandi.
     * </p>
     *
     * @return Una {@code List<Object>} non modificabile contenente tutte le etichette.
     */
    public List<Object> etichette() {
        if (isFused || isNumeric) {
            // Genera le etichette al momento se non sono memorizzate esplicitamente
            List<Object> generatedLabels = new ArrayList<>(this.lunghezza);
            for (int i = 0; i < this.lunghezza; i++) {
                generatedLabels.add(etichettaAllaPosizione(i));
            }
            return Collections.unmodifiableList(generatedLabels);
        }
        // Restituisce la lista immutabile creata nel costruttore
        return etichette;
    }


    /**
     * Confronta questo indice con un altro oggetto per verificarne l'uguaglianza.
     * Due indici sono considerati uguali se hanno la stessa lunghezza e le stesse
     * etichette nello stesso ordine.
     *
     * @param o L'oggetto da confrontare.
     * @return {@code true} se gli oggetti sono uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indice that = (Indice) o;
        if (this.getLunghezza() != that.getLunghezza()) return false;
        // Confronta le etichette una per una senza materializzare l'intera lista se non necessario
        for (int i = 0; i < this.getLunghezza(); i++) {
            if (!Objects.equals(this.etichettaAllaPosizione(i), that.etichettaAllaPosizione(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Restituisce il codice hash per questo indice.
     * Il codice hash è calcolato sulla base della lista completa delle etichette.
     *
     * @return Il codice hash dell'indice.
     */
    @Override
    public int hashCode() {
        // Usa etichette() per garantire che l'hash sia consistente per tutti i tipi di indice
        return Objects.hash(this.etichette());
    }
}