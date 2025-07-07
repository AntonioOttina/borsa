package clients;

import utils.InputParsing;

import java.util.Scanner;

public class TabellaMoltiplica {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaTabella(InputParsing.leggiTabella(scanner).map(v -> Integer.parseInt(v.toString()) * Integer.parseInt(args[0])));
        }
    }
}