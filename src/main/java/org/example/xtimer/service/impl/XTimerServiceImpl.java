package org.example.xtimer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.common.enums.TimerStatus;
import org.example.xtimer.common.redis.ReentrantDistributeLock;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.manager.MigratorManager;
import org.example.xtimer.mapper.XTimerMapper;
import org.example.xtimer.service.XTimerService;
import org.example.xtimer.utils.TimerUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class XTimerServiceImpl implements XTimerService {

    @Autowired
    XTimerMapper xTimerMapper;

    @Autowired
    private ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    private MigratorManager migratorManager;

    @Autowired
    private ApplicationContext applicationContext;


    private static final int  defaultGapSeconds= 3;

    @Override
    public Long createTimer(XTimer timer) {
        // 检查cron表达式是否合法,及其他异常
        if (timer == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isValid = CronExpression.isValidExpression(timer.getCron());
        if (!isValid) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"invalid cron");
        }

        xTimerMapper.save(timer);
        return timer.getTimerId();
    }

    @Override
    public void enableTimer(String app, Long timerId) {
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
            TimerUtils.GetEnableLockKey(app),
            lockToken,
            (long) defaultGapSeconds
        );
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活/去激活操作过于频繁，请稍后再试！");
        }
        // 激活逻辑
        // 获取当前类的代理对象，通过代理对象调用方法
        XTimerServiceImpl proxy = applicationContext.getBean(XTimerServiceImpl.class);
        proxy.doEnableTimer(timerId);
    }
    @Transactional
    public void doEnableTimer(long id){
        //1. 数据库获取Timer
        XTimer timer = xTimerMapper.getTimerById(id);
        //2. 校验状态
        if(timer.getStatus() != TimerStatus.Unable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Unable状态，去激活失败，id:"+id);
        }
        //3. 修改timer为激活态
        timer.setStatus(TimerStatus.Enable.getStatus());
        xTimerMapper.update(timer);
        //4. 迁移数据
        migratorManager.migrateTimer(timer);

    }

    @Override
    public void UnEnableTimer(String app, long id) {
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetEnableLockKey(app),
                lockToken,
                (long) defaultGapSeconds);
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活/去激活操作过于频繁，请稍后再试！");
        }

        // 去激活逻辑
//        self.doUnEnableTimer(id);
        doUnEnableTimer(id);
    }

    @Transactional
    public void doUnEnableTimer(Long id){
        // 1. 数据库获取Timer
        XTimer timerModel = xTimerMapper.getTimerById(id);
        // 2. 校验状态
        if(timerModel.getStatus() != TimerStatus.Unable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Unable状态，去激活失败，id:"+id);
        }
        timerModel.setStatus(TimerStatus.Unable.getStatus());
        xTimerMapper.update(timerModel);
    }
}
