package com.github.javaxcel.out;

import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class MapWriter<W extends Workbook, T extends Map<String, ?>> extends AbstractExcelWriter<W, T> {

    private final List<String> keys = new ArrayList<>();

    private String defaultValue;

    private MapWriter(W workbook) {
        super(workbook);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> headerStyles(ExcelStyleConfig... configs) {
        super.headerStyles(configs);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> bodyStyles(ExcelStyleConfig... configs) {
        super.bodyStyles(configs);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> disableRolling() {
        super.disableRolling();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> autoResizeCols() {
        super.autoResizeCols();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> hideExtraRows() {
        super.hideExtraRows();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> hideExtraCols() {
        super.hideExtraCols();
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

        /*
        Unlike 'ModelWriter', 'MapWriter' cannot validate header names, header styles and body styles
        before method 'write(OutputStream, List)' is invoked. So it does using this hook.
         */

        // Validates the number of header names.
        if (!this.headerNames.isEmpty() && this.headerNames.size() != this.keys.size()) {
            throw new IllegalArgumentException("The number of header names is not equal to the number of maps' keys");
        }

        Predicate<CellStyle[]> validator = them -> them == null || them.length == 1 || them.length == this.keys.size();

        // Validates the number of header styles.
        if (!validator.test(this.headerStyles)) {
            throw new IllegalArgumentException(String.format(
                    "Number of header styles(%d) must be 1 or equal to number of maps' keys(%d)",
                    this.headerStyles.length, this.keys.size()));
        }

        // Validates the number of body styles.
        if (!validator.test(this.bodyStyles)) {
            throw new IllegalArgumentException(String.format(
                    "Number of body styles(%d) must be 1 or equal to number of maps' keys(%d)",
                    this.bodyStyles.length, this.keys.size()));
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

                if (this.bodyStyles == null) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = this.bodyStyles.length == 1
                        ? this.bodyStyles[0] : this.bodyStyles[j];
                cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumOfColumns() {
        return this.keys.size();
    }

}
