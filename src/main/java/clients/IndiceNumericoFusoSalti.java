package clients;

import model.Indice;
import utils.InputParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IndiceNumericoFusoSalti {
    public static void main(String[] args) {
        Indice indiceFuso = Indice.numerico("", Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2])).fonde(InputParsing.leggiIndice(new Scanner(System.in)));
        List<String> risultato = new ArrayList<>();
        for (int i = 0; i < indiceFuso.getLunghezza(); i += Integer.parseInt(args[3])) {
            risultato.add(String.valueOf(indiceFuso.etichettaAllaPosizione(i)));
        }
        System.out.println(String.join(", ", risultato));
    }
}