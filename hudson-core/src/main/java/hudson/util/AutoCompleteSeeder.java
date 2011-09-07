package hudson.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for taking the current input value and computing a list
 * of potential terms to match against the list of defined labels.
 */
public class AutoCompleteSeeder {
    private String source;

    public AutoCompleteSeeder(String source) {
        this.source = source;
    }

    public List<String> getSeeds() {
        List<String> terms = new ArrayList<String>();
        boolean trailingQuote = source.endsWith("\"");
        boolean leadingQuote = source.startsWith("\"");
        boolean trailingSpace = source.endsWith(" ");

        if (trailingQuote || (trailingSpace && !leadingQuote)) {
            terms.add("");
        } else {
            if (leadingQuote) {
                int quote = source.lastIndexOf('"');
                if (quote == 0) {
                    terms.add(source.substring(1));
                } else {
                    terms.add("");
                }
            } else {
                int space = source.lastIndexOf(' ');
                if (space > -1) {
                    terms.add(source.substring(space + 1));
                } else {
                    terms.add(source);
                }
            }
        }

        return terms;
    }
}