package clients;

import model.Indice;
import model.Tabella;
import utils.InputParsing;

import java.util.Scanner;

public class TabellaIndice {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Tabella<?> tabellaReindicizzata = InputParsing.leggiTabella(scanner).conIndiceDiRiga(Indice.fromStrings(args));
            InputParsing.stampaTabella(tabellaReindicizzata);
            for (String nomeColonna : tabellaReindicizzata.getIntestazione()) {
                System.out.println();
                InputParsing.stampaColonna(tabellaReindicizzata.getColonna(nomeColonna));
            }

        }
    }
}