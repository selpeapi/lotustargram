package com.lotus.lotustargram.navigation.model

data class FollowDTO(
    var followerCount :Int =0,
    //중복 팔로워 방지역할
    var followers :MutableMap<String, Boolean> =HashMap(),

    var followingCount :Int =0,
    var followings :MutableMap<String, Boolean> =HashMap()
)