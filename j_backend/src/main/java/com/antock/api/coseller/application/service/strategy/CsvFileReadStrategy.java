package com.antock.api.coseller.application.service.strategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public interface CsvFileReadStrategy {
    BufferedReader getBufferedReader(String fileName) throws IOException;

    InputStream readFile(String fileName);
}