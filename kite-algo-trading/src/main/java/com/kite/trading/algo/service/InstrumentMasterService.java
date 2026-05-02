package com.kite.trading.algo.service;

import com.kite.trading.algo.config.UniverseProperties;
import com.kite.trading.algo.runtime.InstrumentMasterRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentMasterService {

    private final UniverseProperties universeProperties;

    public List<InstrumentMasterRecord> loadInstrumentMaster() {
        ClassPathResource resource = new ClassPathResource(universeProperties.getInstrumentMasterResource());
        if (!resource.exists()) {
            log.warn("Instrument master resource {} does not exist", universeProperties.getInstrumentMasterResource());
            return List.of();
        }

        List<InstrumentMasterRecord> records = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                records.add(new InstrumentMasterRecord(
                        Long.parseLong(record.get("instrument_token")),
                        record.get("symbol").trim().toUpperCase(),
                        record.get("exchange").trim().toUpperCase(),
                        record.get("instrument_type").trim().toUpperCase(),
                        new BigDecimal(record.get("last_price").trim())
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load bootstrap instrument master", exception);
        }
        return records;
    }
}
