package com.example.dogakunen.holidayCsv;

import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//祝日のCSVデータを読み込むクラス
@Component
public class HolidayCsvParser {
    private static final String CSV_FILE = "file/syukujitsu.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

    public List<Holiday> parse() {
        List<Holiday> holidays = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(CSV_FILE).getInputStream(), Charset.forName("Shift_JIS")))) {

            String line;
            boolean isFirstLine = true; //ヘッダー行をスキップ
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                LocalDate date = LocalDate.parse(fields[0], FORMATTER);
                String name = fields[1];
                holidays.add(new Holiday(date, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return holidays;
    }
}
