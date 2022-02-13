package com.coqing.order.hospital.service;

import com.coqing.order.model.hospital.Hospital;
import com.coqing.order.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {

    //上传医院接口
    void save(Map<String, Object> paramMap);

    //根据医院编号查询
    Hospital getByHoscode(String hoscode);

    //医院列表（分页条件查询）
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //更新医院上线状态
    void updateStatus(String id, Integer status);

    //获取医院详情
    Map<String,Object> getHospById(String id);
}
