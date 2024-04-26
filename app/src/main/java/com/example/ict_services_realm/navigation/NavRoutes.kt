package com.example.ict_services_realm.navigation

sealed class NavRoutes(var screenroute:String) {
    data object AdminTicketsForm: NavRoutes("adminTicketForm") // Form when issuing tasks
    data object AdminTicketsList: NavRoutes("adminTicketList") // Issued tasks by admin
    data object AdminRate: NavRoutes("adminRate") //Page when admin rates after technician completes
    data object TechnicianProfile: NavRoutes("techProfile")
    data object TechnicianTickets: NavRoutes("techTicketList") // List of tickets for technician
    data object TechnicianTicketInfo: NavRoutes("techTicketInfo") // Expanded
}