package clients;

import model.Indice;
import utils.InputParsing;

import java.util.Scanner;
import java.util.stream.Collectors;

public class IndiceNumericoFusoUltimi {
    public static void main(String[] args) {
        System.out.println(
                Indice.numerico("", Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2])).
                        fondeEUltimi(InputParsing.leggiIndice(new Scanner(System.in)), 10)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))
        );
    }
}