package data.cleaner;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.File;
import java.io.PrintStream;

public class Cleaner {

    public void saveTitlesToFileByLanguage(File sourceFile, String destFileName, String columnName, String region) throws Exception {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setProcessor(new RowListProcessor());
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setSkipEmptyLines(true);
        parserSettings.selectFields(columnName, "region");

        TsvParser parser = new TsvParser(parserSettings);
        parser.beginParsing(sourceFile);
        PrintStream writer = new PrintStream(new File(sourceFile.getParent(), destFileName));

        Record record;
        while ((record = parser.parseNextRecord()) != null) {
            if (!record.getString("region").equalsIgnoreCase(region)) continue;
            writer.println(record.getString(columnName));
        }

        parser.stopParsing();
        writer.close();
    }
}
