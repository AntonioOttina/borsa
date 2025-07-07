package clients;

import model.Indice;
import utils.InputParsing;

import java.util.Scanner;

public class IndiceMedesimo {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                Indice indice1 = InputParsing.leggiIndice(scanner);
                if (!scanner.hasNextLine()) break;
                System.out.println(indice1.equals(InputParsing.leggiIndice(scanner)));
            }
        }
    }
}