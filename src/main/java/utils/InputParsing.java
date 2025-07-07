package utils;

import model.Colonna;
import model.Indice;
import model.Tabella;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe di utilità per il parsing dell'input testuale e la stampa degli oggetti del modello.
 * Fornisce metodi statici per leggere e scrivere Indici, Colonne e Tabelle
 * secondo il formato specificato per il progetto.
 * Questa classe non è pensata per essere istanziata.
 */
public class InputParsing {
    /** Pattern Regex per il parsing delle linee di descrittore. */
    static final Pattern DESCRIPTOR_RE =
            Pattern.compile("#(\\w+)\\[(\\d+)(?:\\s*,\\s*([^,\\]]+))?(?:\\s*,\\s*([^]]+))?]");
    /** Formattatore di date standard per il formato ISO DateTime. */
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Costruttore privato per impedire l'istanziazione.
     */
    private InputParsing() {
    }

    /**
     * Converte il nome di un tipo da stringa a oggetto Class.
     *
     * @param typeName Il nome del tipo (es. "integer", "string"). Case-insensitive.
     * @return L'oggetto Class corrispondente, o Object.class se il tipo non è riconosciuto o è nullo.
     */
    private static Class<?> stringToType(String typeName) {
        if (typeName == null) return Object.class;
        return switch (typeName.trim().toLowerCase()) {
            case "string" -> String.class;
            case "boolean" -> Boolean.class;
            case "number" -> Number.class;
            case "integer" -> Integer.class;
            case "double" -> Double.class;
            case "datetime" -> LocalDateTime.class;
            default -> Object.class;
        };
    }

    /**
     * Esegue il parsing di una linea di descrittore (es. "#table[3, 2]").
     *
     * @param line La linea di testo contenente il descrittore.
     * @return Un'istanza di {@link Descriptor} corrispondente, o {@code null} se la linea non è un descrittore valido.
     * @throws IllegalArgumentException se il formato del descrittore non è valido.
     */
    public static Descriptor parseDescriptor(String line) {
        Matcher matcher = DESCRIPTOR_RE.matcher(Objects.requireNonNull(line));
        if (matcher.matches())
            try {
                return switch (matcher.group(1)) {
                    case "index" -> new IndexDescriptor(Integer.parseInt(matcher.group(2)), matcher.group(3));
                    case "column" -> new ColumnDescriptor(
                            Integer.parseInt(matcher.group(2)),
                            stringToType(matcher.group(3)),
                            matcher.groupCount() < 4 || matcher.group(4) == null ? null : matcher.group(4));
                    case "table" -> new TableDescriptor(
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3)),
                            stringToType(matcher.group(4)));
                    default -> throw new IllegalArgumentException("Unknown descriptor type: " + matcher.group(1));
                };
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error parsing descriptor: " + line, e);
            }
        return null;
    }

    /**
     * Esegue il parsing di una linea di valori separati da spazi.
     * Tenta di inferire il tipo più specifico per ogni valore (Boolean, Integer, Double, LocalDateTime, String).
     *
     * @param line La linea di testo contenente i valori.
     * @param n Il numero di valori da leggere.
     * @return Un array di Object contenente i valori letti.
     */
    public static Object[] parseValues(String line, int n) {
        Object[] result = new Object[n];
        try (Scanner s = new Scanner(Objects.requireNonNull(line))) {
            for (int i = 0; i < n && s.hasNext(); i++) {
                Object object;
                if (s.hasNextBoolean()) object = s.nextBoolean();
                else if (s.hasNextInt()) object = s.nextInt();
                else if (s.hasNextDouble()) object = s.nextDouble();
                else {
                    String str = s.next();
                    try {
                        object = LocalDateTime.parse(str, DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        object = str.equalsIgnoreCase("null") ? "" : str;
                    }
                }
                result[i] = object;
            }
        }
        return result;
    }

    /**
     * Legge una {@link Tabella} completa da uno {@link Scanner}.
     * Si aspetta un descrittore di tabella seguito dalle definizioni delle sue colonne.
     *
     * @param scanner Lo scanner da cui leggere l'input.
     * @return La {@link Tabella} letta.
     * @throws IllegalArgumentException se l'input non è formattato correttamente.
     */
    public static Tabella<Object> leggiTabella(Scanner scanner) {
        String line = scanner.nextLine();
        Descriptor d = parseDescriptor(line);
        if (!(d instanceof TableDescriptor td)) {
            throw new IllegalArgumentException("Expected a table descriptor.");
        }
        List<Colonna<Object>> colonneList = new ArrayList<>();
        for (int i = 0; i < td.cols(); i++) {
            colonneList.add(leggiColonna(scanner));
        }
        if (colonneList.isEmpty()) {
            return new Tabella<>(new Indice("", new ArrayList<>()), new ArrayList<>());
        }
        Indice masterIndex = colonneList.getFirst().indice();
        return new Tabella<>(masterIndex, colonneList);
    }

    /**
     * Legge una {@link Colonna} completa da uno {@link Scanner}.
     *
     * @param scanner Lo scanner da cui leggere l'input.
     * @return La {@link Colonna} letta.
     * @throws IllegalArgumentException se l'input non è formattato correttamente.
     */
    public static Colonna<Object> leggiColonna(Scanner scanner) {
        String colLine = scanner.nextLine();
        ColumnDescriptor cd = (ColumnDescriptor) parseDescriptor(colLine);
        if (cd == null) {
            throw new IllegalArgumentException("Invalid column descriptor: " + colLine);
        }
        Indice indice;
        String valuesLine;
        if (scanner.hasNextLine() && scanner.hasNext("#index.*")) {
            String indexLine = scanner.nextLine();
            IndexDescriptor id = (IndexDescriptor) parseDescriptor(indexLine);
            String indexValuesLine = scanner.nextLine();
            assert id != null;
            List<Object> labels = Arrays.asList(parseValues(indexValuesLine, id.len()));
            indice = new Indice(id.name(), labels);
        } else {
            List<Object> labels = new ArrayList<>();
            for (int i = 0; i < cd.rows(); i++) {
                labels.add(i);
            }
            indice = new Indice("", labels);
        }
        valuesLine = scanner.nextLine();
        List<Object> values = Arrays.asList(parseValues(valuesLine, cd.rows()));
        return new Colonna<>(cd.name(), indice, values);
    }

    /**
     * Legge un {@link Indice} completo da uno {@link Scanner}.
     *
     * @param scanner Lo scanner da cui leggere l'input.
     * @return L'{@link Indice} letto.
     * @throws IllegalArgumentException se l'input non è formattato correttamente.
     */
    public static Indice leggiIndice(Scanner scanner) {
        String line = scanner.nextLine();
        Descriptor d = parseDescriptor(line);
        if (d instanceof IndexDescriptor(int len, String name)) {
            String valuesLine = scanner.nextLine();
            return new Indice(name, Arrays.asList(parseValues(valuesLine, len)));
        }
        throw new IllegalArgumentException("Expected an index descriptor.");
    }

    /**
     * Stampa un {@link Indice} nel formato testuale richiesto.
     * @param indice L'indice da stampare.
     */
    public static void stampaIndice(Indice indice) {
        String name = indice.nome();
        List<Object> labels = indice.etichette();
        if (name == null || name.equals("null")) name = "";
        int width = name.length();
        for (Object o : labels) {
            if (o != null) {
                width = Math.max(width, o.toString().length());
            }
        }
        if (!name.isEmpty()) {
            System.out.println(name);
            System.out.println("-".repeat(width));
        } else if (!labels.isEmpty()) {
            System.out.println("-".repeat(width));
        }
        for (Object label : labels) {
            System.out.printf("%" + width + "s%n", label == null ? "" : label);
        }
    }

    /**
     * Stampa una {@link Colonna} nel formato testuale richiesto.
     * @param colonna La colonna da stampare.
     */
    public static void stampaColonna(Colonna<?> colonna) {
        Indice indice = colonna.indice();
        String indexName = indice.nome() == null || indice.nome().equals("null") ? "" : indice.nome();
        int indexWidth = indexName.length();
        for (Object o : indice.etichette()) {
            if (o != null)
                indexWidth = Math.max(indexWidth, o.toString().length());
        }
        String colonnaNome = colonna.nome();
        if (colonnaNome.equals("Unnamed") || colonnaNome.equals("null")) {
            colonnaNome = "";
        }
        int colWidth = colonnaNome.length();
        for (Object o : colonna.valori()) {
            if (o != null)
                colWidth = Math.max(colWidth, o.toString().length());
        }
        System.out.printf("%" + indexWidth + "s | %s%n", indexName, colonnaNome);
        System.out.println("-".repeat(indexWidth) + "-+-" + "-".repeat(colWidth));
        for (int i = 0; i < colonna.size(); i++) {
            Object indexValue = i < indice.getLunghezza() ? indice.etichettaAllaPosizione(i) : "";
            Object colValue = i < colonna.valori().size() ? colonna.valori().get(i) : "";
            System.out.printf("%" + indexWidth + "s | %-" + colWidth + "s%n", indexValue, colValue == null ? "" : colValue);
        }
    }

    /**
     * Stampa una {@link Tabella} nel formato testuale richiesto.
     * @param tabella La tabella da stampare.
     */
    public static void stampaTabella(Tabella<?> tabella) {
        List<String> headers = new ArrayList<>(tabella.getIntestazione());
        Map<String, Integer> colWidths = new LinkedHashMap<>();
        for (String header : headers) {
            int width = header.length();
            for (Object o : tabella.getColonna(header).valori()) {
                if (o != null) {
                    width = Math.max(width, o.toString().length());
                }
            }
            colWidths.put(header, width);
        }
        Indice indiceRighe = tabella.getIndiceRighe();
        String indexName = indiceRighe.nome();
        if (indexName == null || indexName.equals("null")) indexName = "";
        int indexWidth = indexName.length();
        for (Object o : indiceRighe.etichette()) {
            if (o != null)
                indexWidth = Math.max(indexWidth, o.toString().length());
        }
        System.out.printf("%" + indexWidth + "s", indexName);
        for (String header : headers) {
            System.out.print(" | " + String.format("%-" + colWidths.get(header) + "s", header));
        }
        System.out.println();
        System.out.print("-".repeat(indexWidth));
        for (String header : headers) {
            System.out.print("-+-" + "-".repeat(colWidths.get(header)));
        }
        System.out.println();
        for (int i = 0; i < tabella.getNumeroRighe(); i++) {
            System.out.printf("%" + indexWidth + "s", indiceRighe.etichettaAllaPosizione(i));
            for (String header : headers) {
                String value = "";
                Colonna<?> col = tabella.getColonna(header);
                if (i < col.size() && col.valori().get(i) != null) {
                    value = col.valori().get(i).toString();
                }
                System.out.print(" | " + String.format("%-" + colWidths.get(header) + "s", value));
            }
            System.out.println();
        }
    }

    /** Interfaccia marcatore per tutti i tipi di descrittore. */
    public interface Descriptor {
    }

    /**
     * Record che rappresenta un descrittore di indice.
     * @param len lunghezza dell'indice.
     * @param name nome dell'indice.
     */
    public record IndexDescriptor(int len, String name) implements Descriptor {
        /**
         * Costruttore per IndexDescriptor.
         * @param len la lunghezza dell'indice, deve essere positiva.
         * @param name il nome dell'indice.
         */
        public IndexDescriptor(int len, String name) {
            if (len <= 0) throw new IllegalArgumentException("Length must be positive");
            this.len = len;
            this.name = name == null ? "" : name.trim();
        }
    }

    /**
     * Record che rappresenta un descrittore di colonna.
     * @param rows numero di righe.
     * @param type tipo dei dati.
     * @param name nome della colonna.
     */
    public record ColumnDescriptor(int rows, Class<?> type, String name) implements Descriptor {
        /**
         * Costruttore per ColumnDescriptor.
         * @param rows numero di righe, deve essere positivo.
         * @param type il tipo dei dati.
         * @param name il nome della colonna.
         */
        public ColumnDescriptor(int rows, Class<?> type, String name) {
            if (rows <= 0) throw new IllegalArgumentException("Row number must be positive");
            this.rows = rows;
            this.type = type == null ? Object.class : type;
            this.name = name == null ? "Unnamed" : name.trim();
        }
    }

    /**
     * Record che rappresenta un descrittore di tabella.
     * @param rows numero di righe.
     * @param cols numero di colonne.
     * @param type tipo di dati predefinito per la tabella.
     */
    public record TableDescriptor(int rows, int cols, Class<?> type) implements Descriptor {
        /**
         * Costruttore per TableDescriptor.
         * @param rows numero di righe, deve essere positivo.
         * @param cols numero di colonne, deve essere positivo.
         * @param type il tipo di dati predefinito.
         */
        public TableDescriptor(int rows, int cols, Class<?> type) {
            if (rows <= 0) throw new IllegalArgumentException("Row number must be positive");
            if (cols <= 0) throw new IllegalArgumentException("Column number must be positive");
            this.rows = rows;
            this.cols = cols;
            this.type = type == null ? Object.class : type;
        }
    }
}