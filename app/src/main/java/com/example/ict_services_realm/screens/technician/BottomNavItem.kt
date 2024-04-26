package com.example.ict_services_realm.screens.technician

import com.example.ict_services_realm.R

sealed class TechnicianNavItem(var title:String, var icon:Int, var screenroute:String){
    data object Profile : TechnicianNavItem("Profile", R.drawable.baseline_account_box_24,"techProfile")
    data object TechTicketList: TechnicianNavItem("Ticket List",
        R.drawable.baseline_align_horizontal_left_24,"techTicketList")
}