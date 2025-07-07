package clients;

import utils.InputParsing;

import java.util.Scanner;

public class TabellaValore {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(InputParsing.leggiTabella(scanner).valore(InputParsing.parseValues(args[0], 1)[0], args[1]));
        }
    }
}