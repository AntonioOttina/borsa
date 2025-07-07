package clients;

import model.Indice;
import model.Tabella;
import utils.InputParsing;

import java.util.Scanner;

public class TabellaIntestazioni {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Tabella<?> risultato = InputParsing.leggiTabella(scanner).conIntestazioni(Indice.fromStrings(args));
            InputParsing.stampaTabella(risultato);
            for (String nomeColonna : risultato.getIntestazione()) {
                System.out.println();
                InputParsing.stampaColonna(risultato.getColonna(nomeColonna));
            }
        }
    }
}