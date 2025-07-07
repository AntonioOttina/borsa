package clients;

import utils.InputParsing;

import java.util.Scanner;

public class IndicePosizione {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(InputParsing.leggiIndice(scanner).posizioneDellEtichetta(InputParsing.parseValues(args[0], 1)[0]));
        }
    }
}