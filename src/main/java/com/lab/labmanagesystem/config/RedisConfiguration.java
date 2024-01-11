package com.lab.labmanagesystem.config;

import com.arcsoft.face.FaceInfo;
import com.lab.labmanagesystem.entity.ProcessInfo;
import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");

        RedisTemplate redisTemplate = new RedisTemplate<>();

        // 设置redis连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, StudentFace> studentFaceRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, StudentFace> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, Student> studentRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Student> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    public RedisTemplate<String, FaceInfo> faceInfoRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, FaceInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));

        return template;
    }

    @Bean
    public RedisTemplate<String, List<ProcessInfo>> processInfoRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<ProcessInfo>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));

        return template;
    }
}
