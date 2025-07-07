package clients;

import model.Indice;
import utils.InputParsing;

import java.util.Scanner;

public class IndiceFusione {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                Indice indice1 = InputParsing.leggiIndice(scanner);
                if (!scanner.hasNextLine()) break;
                InputParsing.stampaIndice(indice1.fonde(InputParsing.leggiIndice(scanner)));
                System.out.println();
            }
        }
    }
}