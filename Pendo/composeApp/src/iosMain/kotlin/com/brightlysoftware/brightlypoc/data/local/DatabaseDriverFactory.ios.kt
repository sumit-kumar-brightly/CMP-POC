package com.brightlysoftware.brightlypoc.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.brightlysoftware.brightlypoc.database.MovieDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MovieDatabase.Schema, "movie.db")
    }
}