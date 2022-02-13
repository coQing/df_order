package com.coqing.order.hospital.service.impl;

import com.alibaba.fastjson.JSONObject;


import com.coqing.order.cmn.client.DictFeignClient;
import com.coqing.order.hospital.repository.HospitalRepository;
import com.coqing.order.hospital.service.HospitalService;
import com.coqing.order.model.hospital.Hospital;
import com.coqing.order.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {


    @Autowired
    private HospitalRepository hospitalRepository;


    @Autowired
    private DictFeignClient dictFeignClient;

    //上传医院接口
    @Override
    public void save(Map<String, Object> paramMap) {

        //把map集合转成对象
        String mapString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断数据是否存在
        String hoscode = hospital.getHoscode();
        Hospital hospital1Exist = hospitalRepository.getHospitalByHoscode(hoscode);

        if(hospital1Exist!=null){
            hospital.setStatus(hospital1Exist.getStatus());
            hospital.setCreateTime(hospital1Exist.getCreateTime());
            hospital.setId(hospital1Exist.getId());
        }else{
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
        }
        hospital.setUpdateTime(new Date());
        hospital.setIsDeleted(0);
        hospitalRepository.save(hospital);

    }

    //根据医院编号查询
    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospitalByHoscode;
    }

    //医院列表（分页条件查询）
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        // 创建Pageable对象
        // 0是第一页
        Pageable pageable = PageRequest.of(page - 1, limit);

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //创建条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Hospital> example = Example.of(hospital, matcher);

        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);


        //获取查询list集合，遍历进行医院等级封装
        pages.getContent().stream().forEach(item->{
            this.getHospitalHosType(item);
        });

        return pages;
    }

    //更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }

    }

    //获取医院详情
    @Override
    public Map<String,Object> getHospById(String id) {
        Map<String, Object> result = new HashMap<>();

        Hospital hospital = this.getHospitalHosType(hospitalRepository.findById(id).get());
        result.put("hospital", hospital);

        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;

    }

    //获取查询list集合，遍历进行医院等级封装
    private Hospital getHospitalHosType(Hospital hospital) {
        String hospitalString = dictFeignClient.getName("Hostype", hospital.getHostype());

        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());

        hospital.getParam().put("hospitalString",hospitalString);
        return hospital;
    }
}
