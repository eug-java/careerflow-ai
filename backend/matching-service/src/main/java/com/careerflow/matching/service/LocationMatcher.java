package com.careerflow.matching.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class LocationMatcher {

    private static final Set<String> US_STATE_CODES = Set.of(
            "al", "ak", "az", "ar", "ca", "co", "ct", "de", "fl", "ga", "hi", "id", "il", "in", "ia", "ks", "ky",
            "la", "me", "md", "ma", "mi", "mn", "ms", "mo", "mt", "ne", "nv", "nh", "nj", "nm", "ny", "nc", "nd",
            "oh", "ok", "or", "pa", "ri", "sc", "sd", "tn", "tx", "ut", "vt", "va", "wa", "wv", "wi", "wy", "dc"
    );

    private static final Map<String, List<String>> METRO_AREAS = Map.ofEntries(
            Map.entry("san francisco bay area", List.of(
                    "san francisco", "sf", "oakland", "san jose", "berkeley", "palo alto", "mountain view",
                    "sunnyvale", "fremont", "redwood city", "menlo park", "bay area"
            )),
            Map.entry("new york metro", List.of(
                    "new york", "nyc", "brooklyn", "manhattan", "queens", "bronx", "staten island",
                    "jersey city", "newark", "hoboken", "long island"
            )),
            Map.entry("los angeles metro", List.of(
                    "los angeles", "la", "santa monica", "pasadena", "long beach", "burbank", "irvine", "orange county"
            )),
            Map.entry("dallas-fort worth", List.of(
                    "dallas", "fort worth", "arlington", "plano", "irving", "frisco", "dfw"
            )),
            Map.entry("austin metro", List.of("austin", "round rock", "cedar park", "georgetown", "pflugerville")),
            Map.entry("seattle metro", List.of("seattle", "bellevue", "redmond", "tacoma", "kirkland")),
            Map.entry("chicago metro", List.of("chicago", "evanston", "naperville", "schaumburg")),
            Map.entry("boston metro", List.of("boston", "cambridge", "somerville", "waltham")),
            Map.entry("denver metro", List.of("denver", "boulder", "aurora", "lakewood")),
            Map.entry("atlanta metro", List.of("atlanta", "marietta", "alpharetta", "decatur")),
            Map.entry("miami metro", List.of("miami", "fort lauderdale", "boca raton", "west palm beach")),
            Map.entry("washington dc metro", List.of("washington", "dc", "arlington", "bethesda", "silver spring", "alexandria"))
    );

    public int score(String profileLocation, String locationPreference, String jobLocation, boolean remote) {
        if (remote) {
            return 100;
        }

        String preference = normalize(locationPreference);
        String profile = normalize(profileLocation);
        String job = normalize(jobLocation);

        if ("nationwide".equals(preference) || profile.contains("relocation") || profile.contains("anywhere in usa")) {
            return looksLikeUsJob(job) ? 95 : 70;
        }

        if (job.isBlank()) {
            return 50;
        }

        if ("metro".equals(preference)) {
            return scoreMetro(profile, job);
        }

        return scoreCity(profile, job);
    }

    private int scoreMetro(String profileMetro, String jobLocation) {
        String metroKey = resolveMetroKey(profileMetro);
        if (metroKey == null) {
            return scoreCity(profileMetro, jobLocation);
        }

        List<String> cities = METRO_AREAS.get(metroKey);
        for (String city : cities) {
            if (jobLocation.contains(city)) {
                return 100;
            }
        }

        if (sameState(profileMetro, jobLocation)) {
            return 65;
        }

        return 35;
    }

    private int scoreCity(String profileLocation, String jobLocation) {
        if (profileLocation.isBlank() || jobLocation.isBlank()) {
            return 50;
        }

        if (profileLocation.equals(jobLocation)
                || profileLocation.contains(jobLocation)
                || jobLocation.contains(profileLocation)) {
            return 100;
        }

        if (sameState(profileLocation, jobLocation)) {
            return 75;
        }

        String profileMetro = resolveMetroKey(profileLocation);
        String jobMetro = resolveMetroKey(jobLocation);
        if (profileMetro != null && profileMetro.equals(jobMetro)) {
            return 85;
        }

        return 30;
    }

    private String resolveMetroKey(String location) {
        for (Map.Entry<String, List<String>> entry : METRO_AREAS.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getKey();
            }
            for (String city : entry.getValue()) {
                if (location.contains(city)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private boolean sameState(String left, String right) {
        String leftState = extractState(left);
        String rightState = extractState(right);
        return leftState != null && leftState.equals(rightState);
    }

    private String extractState(String location) {
        if (location.contains(",")) {
            String part = location.substring(location.lastIndexOf(',') + 1).trim();
            if (part.length() == 2 && US_STATE_CODES.contains(part)) {
                return part;
            }
        }

        for (String code : US_STATE_CODES) {
            if (location.matches(".*\\b" + code + "\\b.*")) {
                return code;
            }
        }
        return null;
    }

    private boolean looksLikeUsJob(String jobLocation) {
        if (jobLocation.contains("united states") || jobLocation.contains("usa") || jobLocation.contains("u.s.")) {
            return true;
        }
        return extractState(jobLocation) != null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
