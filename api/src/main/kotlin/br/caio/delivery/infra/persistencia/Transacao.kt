package br.caio.delivery.infra.persistencia

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

internal suspend fun <T> Database.suspendTransaction(bloco: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, this) { bloco() }
