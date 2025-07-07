package clients;

import utils.InputParsing;

import java.util.Scanner;

public class ColonnaIndice {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaColonna(InputParsing.leggiColonna(scanner).sostituisciIndice(InputParsing.leggiIndice(scanner)));
        }
    }
}