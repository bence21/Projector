package com.bence.songbook.rules;

import com.bence.songbook.SongbookTestActions;
import com.bence.songbook.SongbookTestSetup;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class BundledDatabaseTestRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                SongbookTestActions.resetMainActivityReadyState();
                SongbookTestSetup.installBundledDatabase();
                base.evaluate();
            }
        };
    }
}
