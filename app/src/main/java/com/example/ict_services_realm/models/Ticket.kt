package com.example.ict_services_realm.models

import io.realm.kotlin.types.RealmInstant;
import io.realm.kotlin.types.RealmObject;
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId;

class ticket() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var assignedTo: String = ""

    var dateCreated: RealmInstant? = null

    var equipmentID: String = ""

    var issuedBy: String = ""

    var location: String? = null

    var remarks: String? = null

    var status: String = ""

    var ticketID: Int? = null
}
