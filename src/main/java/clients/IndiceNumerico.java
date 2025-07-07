package clients;

import model.Indice;
import utils.InputParsing;

import java.util.Scanner;

public class IndiceNumerico {

    public static void main(String[] args) {
        int passo = 1;
        if (args.length > 0) {
            passo = Integer.parseInt(args[0]);
        }
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextInt()) {
                InputParsing.stampaIndice(Indice.numerico("", scanner.nextInt(), scanner.nextInt(), passo));
                System.out.println();
            }
        }
    }
}