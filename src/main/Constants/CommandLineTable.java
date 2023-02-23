package main.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandLineTable {
    private static final String HORIZONTAL_SEP = "-";
    private final String verticalSep = "|";
    private String[] headers;
    private List<List<String>> rows = new ArrayList<>();

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public void addRow(List<String> cells) {
        rows.add(cells);
    }

    public void print() {
        int[] maxWidths = headers != null ?
                Arrays.stream(headers).mapToInt(String::length).toArray() : null;

        for (List<String> cells : rows) {
            if (maxWidths == null) {
                maxWidths = new int[cells.size()];
            }
            if (cells.size() != maxWidths.length) {
                throw new IllegalArgumentException("Number of row-cells and headers should be consistent");
            }
            for (int i = 0; i < cells.size(); i++) {
                maxWidths[i] = Math.max(maxWidths[i], cells.get(i).length());
            }
        }

        if (headers != null) {
            printLine(maxWidths);
            printRow(headers, maxWidths);
            printLine(maxWidths);
        }
        for (List<String> cells : rows) {
            printRow(cells.toArray(new String[0]), maxWidths);
        }
        if (headers != null) {
            printLine(maxWidths);
        }
    }

    private void printLine(int[] columnWidths) {
        for (int i = 0; i < columnWidths.length; i++) {
            String line = String.join("", Collections.nCopies(columnWidths[i] +
                    verticalSep.length() + 1, HORIZONTAL_SEP));
            String joinSep = "+";
            System.out.print(joinSep + line + (i == columnWidths.length - 1 ? joinSep : ""));
        }
        System.out.println();
    }

    private void printRow(String[] cells, int[] maxWidths) {
        for (int i = 0; i < cells.length; i++) {
            String s = cells[i];
            String verStrTemp = i == cells.length - 1 ? verticalSep : "";
            System.out.printf("%s %" + maxWidths[i] + "s %s", verticalSep, s, verStrTemp);
        }
        System.out.println();
    }
}
