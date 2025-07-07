package clients;

import model.Indice;

import java.util.Scanner;
import java.util.stream.Collectors;

public class IndiceNumericoUltimi {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextInt()) {
                System.out.println(Indice.numerico("", Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2]))
                        .ultimeEtichette(scanner.nextInt())
                        .stream().map(String::valueOf)
                        .collect(Collectors.joining(", ")));
            }
        }
    }
}