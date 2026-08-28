package org.example;

/**
 * Record representing electricity price calculation results for a specific region.
 * Contains statistics including minimum, maximum prices, and optimal charging time.
 *
 * @param elomrade electricity region code (e.g., SE1, SE2, SE3, SE4)
 * @param min minimum electricity price for the period
 * @param max maximum electricity price for the period
 * @param bastaLadningsTid optimal charging start time (hour of day)
 */
public record ElprisCalculation(
        String elomrade,
        float min,
        float max,
        int bastaLadningsTid
) {
}
