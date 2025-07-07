package clients;

import model.Colonna;
import utils.InputParsing;

import java.util.Scanner;

public class ColonnaImpilamento {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                Colonna<Object> c1 = InputParsing.leggiColonna(scanner);
                if (!scanner.hasNextLine()) break;
                InputParsing.stampaColonna(c1.impila(InputParsing.leggiColonna(scanner)));
            }
        }
    }
}