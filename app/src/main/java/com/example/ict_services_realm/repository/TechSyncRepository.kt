package com.example.ict_services_realm.repository

import com.example.ict_services_realm.app
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.models.user
import com.example.ict_services_realm.models.user_remarks
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.sync.SyncSession
import io.realm.kotlin.mongodb.syncSession
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface TechSyncRepository {
    // gets list of incomplete tickets
    fun getTicketList(): Flow<ResultsChange<ticket>>

    // gets user data
    fun getUser(): Flow<ResultsChange<user>>

    //gets specific ticket info
    suspend fun getTicketInfo(ticketID: Int): Flow<ResultsChange<ticket>>

    // marks tickets as done
    suspend fun markAsDone(ticketID: Int)

    // pauses realm device sync
    fun pauseSync()

    //resumes realm device sync
    fun resumeSync()

    // updates the realm db to be synced up with the mongodb incase transactions happen outside the app
    suspend fun updateChanges()

    // closes the current realm instance
    fun close()
}

/**
 * Repo implementation used in runtime.
 */
class RealmSyncRepositoryTech(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
) : TechSyncRepository{

    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!

    init {
        config = SyncConfiguration.Builder(currentUser, setOf(user::class, ticket::class, user_remarks::class))
            .initialSubscriptions { realm ->
                add(realm.query<user>("user_id==$0", currentUser.id))
                add(realm.query<ticket>("assignedTo==$0", currentUser.id))
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .log(LogLevel.ALL)
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            realm.subscriptions.waitForSynchronization()
        }
    }

    override fun getTicketList(): Flow<ResultsChange<ticket>> {
        return realm.query<ticket>("assignedTo=='${currentUser.id}' AND status =='In Progress'")
            .asFlow()
    }

    override fun getUser(): Flow<ResultsChange<user>> {
        return realm.query<user>("user_id=='${currentUser.id}'").asFlow()
    }

    override suspend fun getTicketInfo(ticketID: Int): Flow<ResultsChange<ticket>> {
        return realm.query<ticket>("ticketID==$0", ticketID).asFlow()
    }

    override suspend fun markAsDone(ticketID: Int) {
        realm.write {
            val ticketToUpdate = query<ticket>(query="ticketID==$0",ticketID).first().find()
            if(ticketToUpdate!=null){
                ticketToUpdate.status = "Complete"
                copyToRealm(ticketToUpdate)
            }
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    override suspend fun updateChanges() {
        realm.syncSession.downloadAllServerChanges()
        realm.syncSession.uploadAllLocalChanges()
    }

    override fun pauseSync() {
        realm.syncSession.pause()
    }

    override fun resumeSync() {
        realm.syncSession.resume()
    }

    override fun close() = realm.close()
}
