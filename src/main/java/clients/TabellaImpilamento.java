package clients;

import utils.InputParsing;

import java.util.Scanner;

public class TabellaImpilamento {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaTabella(InputParsing.leggiTabella(scanner).impila(InputParsing.leggiTabella(scanner)));
        }
    }
}