package com.lotus.lotustargram.navigation.model

data class AlarmDTO(
    var destinationUid :String? =null,
    var usedId :String? =null,
    var uid :String? =null,
    //어떤 타입의 알림(메시지 종류인지 확인
    //0 좋아요
    //1 코맨트
    //2 팔로우
    var kind :Int? =null,
    var message :String? =null,
    var timestamp :Long? =null
)