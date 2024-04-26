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
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface AdminSyncRepository {

    /**
     * Returns a flow with the tasks for the current subscription.
     */
    fun getTechList(): Flow<ResultsChange<user>>
    fun getUser(): Flow<ResultsChange<user>>

    suspend fun markAsDone(ticketID: Int)

    /**
     * Adds a task that belongs to the current user using the specified [taskSummary].
     */
    suspend fun addTicket(ticket: ticket)


    /**
     * Deletes a given task.
     */
    suspend fun deleteTask(task: user)

    /**
     * Pauses synchronization with MongoDB. This is used to emulate a scenario of no connectivity.
     */
    fun pauseSync()

    /**
     * Resumes synchronization with MongoDB.
     */
    fun resumeSync()

    suspend fun updateChanges()

    /**
     * Closes the realm instance held by this repository.
     */
    fun close()
}

/**
 * Repo implementation used in runtime.
 */
class RealmSyncRepositoryAdmin(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
) : AdminSyncRepository{

    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!

    init {
        config = SyncConfiguration.Builder(currentUser, setOf(user::class, ticket::class, user_remarks::class))
            .initialSubscriptions { realm ->
                add(realm.query<user>("user_id==$0 OR role='technician'", currentUser.id))
                add(realm.query<ticket>())
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .log(LogLevel.ALL)
            .name("admin")
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            realm.subscriptions.waitForSynchronization()
        }
    }

    override fun getTechList(): Flow<ResultsChange<user>> {
        return realm.query<user>("role='technician'")
            .asFlow()
    }

    override fun getUser(): Flow<ResultsChange<user>> {
        return realm.query<user>("user_id=='${currentUser.id}'").asFlow()
    }

    override suspend fun markAsDone(ticketID: Int) {
        realm.write {
            val ticketToUpdate = query<ticket>(query="ticketID==$0",ticketID).first().find()
            if(ticketToUpdate!=null){
                ticketToUpdate.status = "Complete"
                copyToRealm(ticketToUpdate)
            }
        }
    }

    override suspend fun updateChanges() {
        realm.syncSession.downloadAllServerChanges()
        realm.syncSession.uploadAllLocalChanges()
    }


    override suspend fun addTicket(ticket: ticket) {
        val highestTicket = realm.query<ticket>().sort("ticketID", Sort.DESCENDING).first().find()
        realm.write {
            copyToRealm(ticket.apply {
                ticket.ticketID = highestTicket?.ticketID?.plus(1)
                ticket.issuedBy = ticket.issuedBy
                ticket.remarks = ticket.remarks
                ticket.assignedTo = ticket.assignedTo
                ticket.dateCreated = RealmInstant.now()
                ticket.status = "In Progress"
                ticket.equipmentID = ticket.equipmentID
                ticket.location = ticket.location
            })
        }
    }

    override suspend fun deleteTask(task: user) {
        realm.write {
            delete(findLatest(task)!!)
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    override fun pauseSync() {
        realm.syncSession.pause()
    }

    override fun resumeSync() {
        realm.syncSession.resume()
    }

    override fun close() = realm.close()
}
