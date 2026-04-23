package org.example.ev_charge_server.service;

import org.example.ev_charge_server.entity.BillingRule;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Given
* @description 针对表【billing_rule(计费标准表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface BillingRuleService extends IService<BillingRule> {

    /**
     * 根据充电桩ID查询计费规则
     * @param chargerId 充电桩ID
     * @return 计费规则
     */
    BillingRule getByCharger(Integer chargerId);

}
