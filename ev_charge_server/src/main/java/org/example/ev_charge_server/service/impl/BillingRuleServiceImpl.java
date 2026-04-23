package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.entity.BillingRule;
import org.example.ev_charge_server.service.BillingRuleService;
import org.example.ev_charge_server.mapper.BillingRuleMapper;
import org.springframework.stereotype.Service;

/**
* @author Given
* @description 针对表【billing_rule(计费标准表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class BillingRuleServiceImpl extends ServiceImpl<BillingRuleMapper, BillingRule>
    implements BillingRuleService{

    @Override
    public BillingRule getByCharger(Integer chargerId) {
        QueryWrapper<BillingRule> queryWrapper = Wrappers.query(BillingRule.class);
        queryWrapper.eq("charger_id", chargerId);
        return getOne(queryWrapper);
    }
}




