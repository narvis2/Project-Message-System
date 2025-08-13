package com.narvi.messagesystem.database

object ShardContext {
    private val threadLocal = ThreadLocal<Long>()

    fun getChannelId(): Long? = threadLocal.get()

    fun setChannelId(channelId: Long?) {
        require(channelId != null) { "channelId cannot be null." }
        threadLocal.set(channelId)
    }

    fun clear() {
        threadLocal.remove()
    }

    class ShardContextScope(channelId: Long?) : AutoCloseable {
        init {
            setChannelId(channelId)
        }

        override fun close() {
            clear()
        }
    }
}