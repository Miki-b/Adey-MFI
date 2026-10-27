package et.adey.mfi.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The error code list must stay complete: every code the source can produce is in ErrorCodes.ALL
 * (and therefore in the OpenAPI enum), and every listed code has a row in backend/README.md.
 */
class ErrorCodeCatalogTest {

    /** The ways a code is set in this code base. */
    private static final List<Pattern> CODE_SITES = List.of(
            Pattern.compile("HttpStatus\\.[A-Z_]+,\\s*\"([A-Z][A-Z0-9_]+)\""),          // new ApiException(status, "CODE", ..)
            Pattern.compile("setProperty\\(\"code\",\\s*\"([A-Z][A-Z0-9_]+)\"\\)"),     // problem.setProperty("code", "CODE")
            Pattern.compile("Exception\\(\\s*\"([A-Z][A-Z0-9_]+)\""),                    // XxxException("CODE", ..)
            Pattern.compile("Rejected\\(\\d{3},\\s*\"([A-Z][A-Z0-9_]+)\""),             // webhook rejections
            Pattern.compile("->\\s*\"([A-Z][A-Z0-9_]+)\";"));                            // status -> code switches

    /** Patterns that only make sense in the two files that translate errors into codes. */
    private static final List<String> CODE_FILES = List.of("GlobalExceptionHandler.java", "FineractErrorCodes.java");
    private static final List<Pattern> CODE_FILE_SITES = List.of(
            Pattern.compile("return\\s+\"([A-Z][A-Z0-9_]+)\";"),                        // code-picking helpers
            Pattern.compile("static final String \\w+\\s*=\\s*\"([A-Z][A-Z0-9_]+)\";")); // code constants

    @Test
    void everyCodeInTheSourceIsInTheCatalogue() throws IOException {
        Set<String> found = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file, StandardCharsets.UTF_8);
                List<Pattern> patterns = new java.util.ArrayList<>(CODE_SITES);
                if (CODE_FILES.contains(file.getFileName().toString())) {
                    patterns.addAll(CODE_FILE_SITES);
                }
                for (Pattern p : patterns) {
                    Matcher m = p.matcher(source);
                    while (m.find()) {
                        found.add(m.group(1));
                    }
                }
            }
        }
        assertThat(found).as("codes used in src/main/java").isNotEmpty();
        assertThat(ErrorCodes.ALL).as("ErrorCodes.ALL must list every code used in the source").containsAll(found);
        assertThat(found).as("ErrorCodes.ALL lists codes nothing produces").containsAll(ErrorCodes.ALL);
    }

    @Test
    void everyCodeIsDocumentedInTheReadme() throws IOException {
        String readme = Files.readString(Path.of("README.md"), StandardCharsets.UTF_8);
        String table = readme.substring(readme.indexOf("### Errors"), readme.indexOf("## Code layout"));
        Set<String> documented = new TreeSet<>();
        Matcher m = Pattern.compile("^\\| \\d{3} \\| `([A-Z][A-Z0-9_]+)`", Pattern.MULTILINE).matcher(table);
        while (m.find()) {
            documented.add(m.group(1));
        }
        assertThat(documented).containsExactlyInAnyOrderElementsOf(ErrorCodes.ALL);
        assertThat(ErrorCodes.ALL).doesNotHaveDuplicates();
    }
}
