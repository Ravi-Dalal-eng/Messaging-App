package com.ravimessagingdalal.messagingapp.Models

class UserStatus {
    var name: String? = null
    var profileImage: String? = null
    var lastUpdated: Long = 0
    var userId:String?=null
    var statuses: ArrayList<Status>? = null

    constructor() {}
    constructor(
        name: String?,
        profileImage: String?,
        lastUpdated: Long,
        userId:String?,
        statuses: ArrayList<Status>?
    ) {
        this.name = name
        this.profileImage = profileImage
        this.lastUpdated = lastUpdated
        this.userId=userId
        this.statuses = statuses
    }
}