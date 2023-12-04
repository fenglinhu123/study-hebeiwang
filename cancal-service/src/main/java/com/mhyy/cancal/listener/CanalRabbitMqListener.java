package com.mhyy.cancal.listener;

import com.alibaba.fastjson.JSONObject;
import com.mhyy.cancal.pojo.CanalMessage;
import com.mhyy.cancal.processor.RedisCommonProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CanalRabbitMqListener {

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "canal.queue", durable = "true"),
                    exchange = @Exchange(value = "canal.exchange", type = "topic"),
                    key = "canal.routing.key"
            )
    })
    public void handleDataChange(String message) {
        try {
            CanalMessage<?> msg = JSONObject.parseObject(message, CanalMessage.class);
            if (msg.getData() != null && msg.getTable().equals("user") && msg.getDatabase().equals("oauth") && !msg.getType().equals("INSERT")) {
                List<Map<String, Object>> dataSet = msg.getData();
                for (Map<String, Object> data : dataSet) {
                    String id = String.valueOf(data.get("id"));
                    if (id != null) {
                        redisCommonProcessor.remove(Integer.valueOf(id) + 10000000 + "");
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
}
