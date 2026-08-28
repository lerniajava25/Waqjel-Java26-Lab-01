package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Client for fetching and analyzing electricity price data from the Swedish electricity price API.
 * Provides methods for retrieving hourly prices, calculating statistics, and finding optimal charging times.
 */
public class ElprisClient {

    private static final DateTimeFormatter TIME_WITH_OFFSET = DateTimeFormatter.ofPattern("HH:mm XXX");
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new ElprisClient with a configured Jackson ObjectMapper for JSON processing.
     */
    public ElprisClient(){
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Läser elpriser från lokal fil cache. Om filen saknas, hämtas data via live
     * nätverksanrop mot API:et och sparas direkt ner lokalt.
     *
     * @param date the date for which to fetch electricity prices
     * @param region the Swedish electricity region code (e.g., SE1, SE2, SE3, SE4)
     * @return list of hourly electricity prices for the specified date and region
     * @throws IOException if there is an error reading from cache or network
     * @throws ElprisNotFoundException if no price data is available for the specified date and region
     */
    public List<Elpris> hamtaElprisLista(LocalDate date, String region)
            throws IOException, ElprisNotFoundException {

        String year = String.valueOf(date.getYear());
        String monthDay = date.format(DateTimeFormatter.ofPattern("MM-dd"));

        // Generera det unika filnamnet för den lokala cache
        String filename = String.format("elpris_%s-%s_%s.json", year, monthDay, region);
        Path cachePath = Path.of(filename);

        String jsonContent;

        // Kontrollera lokal fil-cache först [File I/O]
        if (Files.exists(cachePath)) {
            System.out.println("[Lokal-Cache] Hittade sparad data lokalt. Läser från fil: " + filename);
            jsonContent = Files.readString(cachePath);
        } else {
            System.out.println("[API] Ingen lokal fil hittades. Ansluter till API...");

            // sökvägen
            String fullUrl = "https://elprisetjustnu.se/api/v1/prices/" + year + "/" + monthDay + "_" + region + ".json";

            URL url = new URL(fullUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream inputStream = connection.getInputStream()) {
                jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                // Spara nedladdad JSON lokalt på hårddisken direkt för framtida körningar
                Files.writeString(cachePath, jsonContent);
                System.out.println("Sparade data lokalt till: " + filename);
            }
        }

        // Mappa JSON-strängen till den Elpris list-struktur via Jackson extern-bibliotek
        List<Elpris> hourlyPrices = objectMapper.readValue(
                jsonContent,
                new TypeReference<List<Elpris>>() {}
        );

        if (hourlyPrices.isEmpty()) {
            throw new ElprisNotFoundException("Ingen prisdata returnerades.");
        }

        return hourlyPrices;
    }

    /**
     * Displays daily electricity price statistics including minimum, maximum, and average prices.
     * Prints formatted output to the console with prices in öre/kWh.
     *
     * @param prisPerTimma list of hourly electricity prices for the day
     * @param region the electricity region code for display purposes
     */
    public void visaDagligStatistik(List<Elpris> prisPerTimma, String region) {
        double totalSum = 0;
        double minPris = prisPerTimma.get(0).sekPerKwh();
        double maxPris = prisPerTimma.get(0).sekPerKwh();

        for (Elpris currentHour : prisPerTimma) {
            double pris = currentHour.sekPerKwh();
            totalSum += pris;

            if (pris < minPris) {
                minPris = pris;
            }
            if (pris > maxPris) {
                maxPris = pris;
            }
        }

        double averageSek = totalSum / prisPerTimma.size();

        System.out.println("\n*** Daglig Statistik (" + region + ") ***");
        System.out.printf("Lägsta pris: %.2f öre/kWh%n", minPris * 100);
        System.out.printf("Högsta pris: %.2f öre/kWh%n", maxPris * 100);
        System.out.printf("Medelpris:   %.2f öre/kWh%n", averageSek * 100);
    }

    /**
     * Finds and displays the optimal 4-hour continuous charging window with the lowest average price.
     * Uses a sliding window algorithm to identify the cheapest consecutive 4-hour period.
     * Prints the start time and average price during the charging window to the console.
     *
     * @param hourlyPrices list of hourly electricity prices for the day
     */
    // Implementera en algoritm (t.ex. Sliding Window) som hittar de 4 sammanhängande timmar på dygnet som har lägst totalpris/medelpris.
    public void visaBastaLaddningstid(List<Elpris> hourlyPrices) {
        int bastaStartTimmaIndex = 0;
        double biligastFyraTimmarSumman = Double.MAX_VALUE;

        for (int i = 0; i <= hourlyPrices.size() - 4; i++) {
            double aktuellFyraTimmarsSumma = hourlyPrices.get(i).sekPerKwh() +
                    hourlyPrices.get(i + 1).sekPerKwh() +
                    hourlyPrices.get(i + 2).sekPerKwh() +
                    hourlyPrices.get(i + 3).sekPerKwh();

            if (aktuellFyraTimmarsSumma < biligastFyraTimmarSumman) {
                biligastFyraTimmarSumman = aktuellFyraTimmarsSumma;
                bastaStartTimmaIndex = i;
            }
        }

        String timeStartStr = hourlyPrices.get(bastaStartTimmaIndex).timeStart();
        ZonedDateTime basta4hStartTid = ZonedDateTime.parse(timeStartStr);
        double best4HourAverageOre = (biligastFyraTimmarSumman / 4.0) * 100;

        System.out.println("\n*** Bästa laddningstid (4h sammanhängande) ***");
        System.out.printf("Börja ladda kl: %s%n", basta4hStartTid.format(TIME_WITH_OFFSET));
        System.out.printf("Medelpris under laddning: %.2f öre/kWh%n", best4HourAverageOre);
    }

    /**
     * Displays hourly electricity prices sorted from lowest to highest.
     * Shows each hour's time range and corresponding price in öre/kWh.
     *
     * @param hourlyPrices list of hourly electricity prices to sort and display
     */
    public void visaTimmarSorteradeEfterPris(List<Elpris> hourlyPrices) {
        System.out.println("\n*** Timmar Sorterade Efter Pris  ***");
        List<Elpris> sorteradeTimmar = new ArrayList<>(hourlyPrices);
        sorteradeTimmar.sort(Comparator.comparingDouble(Elpris::sekPerKwh));

        for (Elpris currentHour : sorteradeTimmar) {
            ZonedDateTime timeStart = ZonedDateTime.parse(currentHour.timeStart());
            ZonedDateTime timeEnd = ZonedDateTime.parse(currentHour.timeEnd());
            double priceInOre = currentHour.sekPerKwh() * 100;
            System.out.printf("%s-%s: %.2f öre/kWh%n",
                    timeStart.format(TIME_WITH_OFFSET), timeEnd.format(TIME_WITH_OFFSET), priceInOre);
        }
    }
}
