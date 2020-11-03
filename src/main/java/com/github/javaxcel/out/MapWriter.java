package com.github.javaxcel.out;

import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class MapWriter<W extends Workbook, T extends Map<String, ?>> extends AbstractExcelWriter<W, T> {

    private final List<String> keys = new ArrayList<>();

    private String defaultValue;

    private MapWriter(W workbook) {
        super(workbook);
    }

    @Override
    public MapWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.defaultValue = defaultValue;
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeWrite(OutputStream out, List<T> list) {
        // To write header names, this doesn't allow to accept empty list of maps.
        if (list.isEmpty()) throw new IllegalArgumentException("List of maps cannot be null");

        // Gets all the maps' keys.
        this.keys.addAll(list.stream().flatMap(it -> it.keySet().stream()).distinct().collect(toList()));

        if (!this.headerNames.isEmpty() && this.headerNames.size() != this.keys.size()) {
            throw new IllegalArgumentException("The number of header names is not equal to the number of maps' keys");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p> If the header names are not set through {@link ExcelWriter#headerNames},
     * this method brings the values from {@link Map#keySet()}.
     *
     * @see Map#keySet()
     */
    @Override
    protected void ifHeaderNamesAreEmpty(List<String> headerNames) {
        headerNames.addAll(this.keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeToSheet(Sheet sheet, List<T> list) {
        final int numOfMaps = list.size();
        final int numOfKeys = this.keys.size();

        for (int i = 0; i < numOfMaps; i++) {
            T map = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfKeys; j++) {
                Object value = map.get(this.keys.get(j));
                Cell cell = row.createCell(j);

                // Not allows empty string to be written.
                if (value != null) {
                    cell.setCellValue(StringUtils.ifNullOrEmpty(value.toString(), (String) null));
                } else if (this.defaultValue != null) {
                    cell.setCellValue(this.defaultValue);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorate(Sheet sheet, int numOfModels) {

    }

}
