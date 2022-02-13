package com.coqing.order.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coqing.order.model.hospital.HospitalSet;


public interface HospitalSetService extends IService<HospitalSet> {

    //根据医院编码，查询数据库里的签名
    String getSignKey(String hoscode);
}
