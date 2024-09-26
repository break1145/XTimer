package org.example.xtimer.common.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@Slf4j
public class ReentrantDistributeLock {
    @Autowired
    RedisBase redisBase;

    /***
     * 检查某个 Redis 键是否已经被当前线程持有。如果没有，则尝试获取锁。
     * @param key redis的key
     * @param token key对应的value
     * @param expireSeconds RedisBase.setnx方法参数，设置时间
     * @return 属于返回true，否则false
     */
    public boolean lock(String key, String token, Long expireSeconds) {
        Object object = redisBase.get(key);
        if (object != null && object.toString().equals(token)) {
            return true;
        }
        boolean ok = redisBase.setnx(key,token,expireSeconds);
        if(!ok){
            log.info("lock is acquired by others");
        }
        return ok;
    }

    /**
     * 解锁
     * @param key
     * @param token
     */
    public void unlock(String key,String token){
        // 执行 Lua 脚本，确保只有当前持有锁的线程才能释放锁
        Long execute = redisBase.executeLua(getUnlockScript(), Arrays.asList(key), token, null);
        if (execute.longValue() == 0) {
            log.info("释放锁{}失败:{}", key, execute);
        } else if (execute.longValue() == 1) {
            log.info("释放锁{}成功:{}", key,execute);
        }
    }

    private DefaultRedisScript<Long> getUnlockScript() {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                "then\n" +
                "return redis.call('del',KEYS[1])\n" +
                "else\n" +
                "   return 0\n" +
                "end";
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText(script);
        return defaultRedisScript;
    }

    /**
     * 延长锁的过期时间
     * @param key
     * @param token
     * @param expireSeconds
     */
    public void expireLock(String key, String token, long expireSeconds){
        Long execute = redisBase.executeLua(getExpireLockScript(), Arrays.asList(key), token, expireSeconds);
        if (execute.longValue() == 0) {
            log.info("延期{}失败:{}", key, execute);
        } else if (execute.longValue() == 1) {
            log.info("延期{}成功:{}", key,execute);
        }
    }

    private DefaultRedisScript<Long> getExpireLockScript() {
        String script = "local lockerKey = KEYS[1]\n" +
                "  local targetToken = ARGV[1]\n" +
                "  local duration = ARGV[2]\n" +
                "  local getToken = redis.call('get',lockerKey)\n" +
                "  if (not getToken or getToken ~= targetToken) then\n" +
                "    return 0\n" +
                "\telse\n" +
                "\t\treturn redis.call('expire',lockerKey,duration)\n" +
                "  end";
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText(script);
        return defaultRedisScript;
    }

}
