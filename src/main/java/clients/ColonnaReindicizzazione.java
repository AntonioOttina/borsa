package clients;

import utils.InputParsing;

import java.util.Scanner;

public class ColonnaReindicizzazione {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaColonna(InputParsing.leggiColonna(scanner).reindicizza(InputParsing.leggiIndice(scanner)));
        }
    }
}