package clients;

import utils.InputParsing;

import java.util.Scanner;

public class TabellaAffiancamento {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaTabella(InputParsing.leggiTabella(scanner).affianca(InputParsing.leggiTabella(scanner)));
        }
    }
}