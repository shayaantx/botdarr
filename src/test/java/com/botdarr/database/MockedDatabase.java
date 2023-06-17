package com.botdarr.database;

import mockit.Mock;
import mockit.MockUp;

import java.io.File;

public class MockedDatabase extends MockUp<DatabaseHelper> {
    public MockedDatabase(File temporaryDatabase) {
        this.temporaryDatabase = temporaryDatabase;
    }

    @Mock
    public File getDatabaseFile() {
        return temporaryDatabase;
    }

    private final File temporaryDatabase;
}