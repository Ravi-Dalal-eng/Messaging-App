package com.ravimessagingdalal.messagingapp.Models

class User {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage: String? = null
    var token: String? = null

    constructor() {}
    constructor(uid: String?, name: String?, email: String?, profileImage: String?) {
        this.uid = uid
        this.name = name
        this.email = email
        this.profileImage = profileImage
    }
}