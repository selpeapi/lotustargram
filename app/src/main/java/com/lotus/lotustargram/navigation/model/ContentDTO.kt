package com.lotus.lotustargram.navigation.model


data class ContentDTO(var explain : String? =null,
                        //컨텐츠의 설명을 관리하는 exlpain 변수 생성
                      var imageUrl :String? =null,
                        //이미지 주소를 관리하는
                      var uid :String? =null,
                        //어느 유저가 올린 것인지 id를 관리하는
                      var userId :String? =null,
                        //올린 유저의 이미지를 관리하는
                      var timestamp :Long? =null,
                        //몇시에 컨텐츠를 올렸는지 관리하는
                      var favoriteCount :Int =0,
                        //좋아요가 몇개 눌렸는지 관리하는
                      var favorites :MutableMap<String, Boolean> =HashMap()
                        //중복 좋아요를 방지할 수 있도록 좋아요를 누른 유저를 관리하는
                    ){
    data class Comment(var uid: String? =null,
                       var userId: String? =null,
                       var comment: String? =null,
                       var timestamp: Long? =null)
}