import com.fasterxml.jackson.annotation.JsonCreator
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.websocket.inbound.BaseRequest

class KeepAliveRequest @JsonCreator constructor() : BaseRequest(MessageType.KEEP_ALIVE)
