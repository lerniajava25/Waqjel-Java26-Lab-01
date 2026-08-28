package org.example;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class Main {
   
    public static void main(String[] args) {

        ElprisClient client = new ElprisClient();
        String valdeRegion = "";
        List<Elpris> timmaDataCache = null;

        while (!valdeRegion.matches("SE[1-4]")) {
            valdeRegion = IO.readln("""
                        
                    Elpriser – Analysverktyg
                    ========================
              
                    Välj elområde (SE1, SE2, SE3, SE4) : """).trim().toUpperCase();

            if (!valdeRegion.matches("SE[1-4]")) {
                System.out.println("Ogiltigt val! Ange SE1, SE2, SE3 eller SE4.");
            }
        }

        while (true) {
            System.out.println("""
                    
                    Elpriser – Analysverktyg
                    ========================
                    Valt elområde: %s
                    ------------------------
                    1. Välj elområde (SE1, SE2, SE3, SE4)
                    2. Min, Max och Medelpris
                    3. Sortera priser (lägst till högst)
                    4. Bästa laddningstid (4h sammanhängande)
                    e. Avsluta
                    """.formatted(valdeRegion));

            String input = IO.readln("Ange val: ").trim().toLowerCase();

            if (input.equals("e") || input.equals("E")) {
                System.out.println("Programmet avslutas.");
                break;
            }

            try {
                int val = Integer.parseInt(input);

                // Hämtar data automatiskt om val 2, 3 eller 4 körs och cachen är tom
                if ((val >= 2 && val <= 4) && timmaDataCache == null) {
                    System.out.println("Laddar ner dagens data för " + valdeRegion + "...");
                    try {
                        timmaDataCache = client.hamtaElprisLista(
                                LocalDate.now(ZoneId.of("Europe/Stockholm")), valdeRegion);
                    } catch (ElprisNotFoundException e) {
                        System.out.println("Fel: " + e.getMessage());
                        continue;
                    } catch (Exception e) {
                        System.out.println("Kunde inte hämta data. Kontrollera din internetanslutning.");
                        e.printStackTrace();
                        continue;
                    }
                }

                switch (val) {
                    case 1:
                        String areaInput = IO.readln("Ange nytt elområde (SE1-SE4): ").trim().toUpperCase();
                        if (areaInput.matches("SE[1-4]")) {
                            valdeRegion = areaInput;
                            timmaDataCache = null; // Töm gamla områdets cache så att ny fil laddas ner
                            System.out.println("Ändrat till område: " + valdeRegion);
                        } else {
                            System.out.println("Ogiltigt val. Ange SE1, SE2, SE3 eller SE4.");
                        }
                        break;

                    case 2:
                        client.visaDagligStatistik(timmaDataCache, valdeRegion);
                        break;

                    case 3:
                        client.visaTimmarSorteradeEfterPris(timmaDataCache);
                        break;

                    case 4:
                        client.visaBastaLaddningstid(timmaDataCache);
                        break;

                    default:
                        System.out.println("Felaktigt val! Välj 1-4 eller e.");
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Felaktig inmatning! Ange en siffra (1-4) eller 'e'.");
            }
        }
    }
}
