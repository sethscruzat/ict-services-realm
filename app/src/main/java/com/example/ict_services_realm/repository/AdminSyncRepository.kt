package com.example.ict_services_realm.repository

import com.example.ict_services_realm.app
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.models.user
import com.example.ict_services_realm.models.user_remarks
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
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

    //gets list of technicians for dropdown menu in FormScaffold
    fun getTechList(): Flow<ResultsChange<user>>

    // gets current user data
    fun getUser(): Flow<ResultsChange<user>>

    //get name of technician for rate screen
    fun getTechName(userId: String): Flow<ResultsChange<user>>

    // gets list of completed tickets
    fun getCompletedTickets(): Flow<ResultsChange<ticket>>

    // gets specific ticket info
    suspend fun getTicketInfo(ticketID: Int): Flow<ResultsChange<ticket>>

    suspend fun addRemark(rating: user_remarks, techID: String)

    // adds new ticket and assigns to technician
    suspend fun addTicket(ticket: ticket)

    // deletes ticket
    suspend fun deleteTicket(ticketID: Int)

    //pauses realm device sync
    fun pauseSync()

    // resumes realm device sync
    fun resumeSync()

    // updates the realm db to be synced up with the mongodb incase transactions happen outside the app
    suspend fun updateChanges()

    //closes the current realm instance
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

    override fun getTechName(userId: String): Flow<ResultsChange<user>> {
        return realm.query<user>("user_id=='${userId}'").asFlow()
    }

    override fun getCompletedTickets(): Flow<ResultsChange<ticket>> {
        return realm.query<ticket>("issuedBy=='${currentUser.id}' AND status='Complete'").asFlow()
    }

    override suspend fun getTicketInfo(ticketID: Int): Flow<ResultsChange<ticket>> {
        return realm.query<ticket>("ticketID==$0", ticketID).asFlow()
    }

    override suspend fun addRemark(rating: user_remarks, techID: String) {
        val techToRate = realm.query<user>("user_id=='${techID}'").first().find()
        val newRemark = user_remarks().apply {
            this.rating = rating.rating
            this.ratedBy = rating.ratedBy
            this.ticketID = rating.ticketID
            this.comment = rating.comment
        }
        realm.write {
            if(techToRate!=null) {
                val newestTech = findLatest(techToRate)
                copyToRealm(newestTech!!, updatePolicy = UpdatePolicy.ALL).remarks.add(newRemark)
            }
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
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
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    override suspend fun deleteTicket(ticketID: Int) {
        realm.write {

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
