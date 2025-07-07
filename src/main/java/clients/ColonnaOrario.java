package clients;

import utils.InputParsing;

import java.time.LocalDateTime;
import java.util.Scanner;

public class ColonnaOrario {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            InputParsing.stampaColonna(InputParsing.leggiColonna(scanner).map(v -> ((LocalDateTime) v).toLocalTime()));
        }
    }
}