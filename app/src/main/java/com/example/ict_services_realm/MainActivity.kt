package com.example.ict_services_realm

import android.app.Application
import android.util.Log
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration

lateinit var app: App

// global Kotlin extension that resolves to the short version
// of the name of the current class. Used for labelling logs.
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
*  Sets up the App and enables Realm-specific logging in debug mode.
*/

// TODO: ADD THOSE "ARE YOU SURE" BOXES IN ADMIN FORM, ADMIN RATE AND TECH MARK AS DONE
//       INTEGRATE EQUIPMENT COLLECTION ONCE MAY GO SIGNAL -> Add Equipment details into tickets
//       PUT CONFIDENTIAL INFO (realm appID, etc) INTO SEPARATE .XML (res -> values folder)
class MainActivity: Application() {

    override fun onCreate() {
        super.onCreate()
        app = App.create(
            AppConfiguration.Builder("ict-services-realm-epnajer")
                .build()
        )
        Log.v(TAG(), "Initialized the App configuration for: ${app.configuration.appId}")
    }
}