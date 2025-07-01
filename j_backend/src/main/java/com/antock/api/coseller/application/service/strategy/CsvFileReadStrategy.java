package com.antock.api.coseller.application.service.strategy;

import java.io.BufferedReader;
import java.io.IOException;

public interface CsvFileReadStrategy {
    BufferedReader getBufferedReader(String fileName) throws IOException;
}