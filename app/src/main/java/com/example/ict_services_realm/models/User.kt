package com.example.ict_services_realm.models

import io.realm.kotlin.ext.realmListOf;
import io.realm.kotlin.types.RealmList;
import io.realm.kotlin.types.RealmObject;
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId;
import io.realm.kotlin.types.EmbeddedRealmObject;
import io.realm.kotlin.types.annotations.Index

class user() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    @Index
    var user_id: String = ""
    var firstName: String? = null
    var lastName: String? = null
    var remarks: RealmList<user_remarks> = realmListOf()
    var role: String = ""
}

class user_remarks : EmbeddedRealmObject {
    var comment: String? = null

    var ratedBy: String? = null

    var rating: Double? = null

    var ticketID: Int? = null
}

