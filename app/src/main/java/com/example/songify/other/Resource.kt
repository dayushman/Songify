package com.example.songify.other

data class Resource<out T>(
    val status: Status,
    val data:T?,
    val message:String?) {


    companion object{
        fun <T> success(data:T?) = Resource<T>(Status.SUCCESS,data,null)

        fun <T> error(message: String?,data:T?) = Resource<T>(Status.ERROR,data,message)

        fun <T> loading(data:T?) = Resource<T>(Status.LOADING,data,null)
    }

}

enum class Status{
    SUCCESS,
    ERROR,
    LOADING
}