package com.coqing.order.hospital.service.impl;

import com.alibaba.fastjson.JSONObject;

import com.coqing.order.hospital.repository.DepartmentRepository;
import com.coqing.order.hospital.service.DepartmentService;
import com.coqing.order.model.hospital.Department;
import com.coqing.order.vo.hosp.DepartmentQueryVo;
import com.coqing.order.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室接口
    @Override
    public void save(Map<String, Object> paramMap) {
        // paramMap 转department对象
        String paramMapString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(paramMapString, Department.class);

        Department departmentExist = departmentRepository
                .getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        if(departmentExist!=null){
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }else{
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }

    //查询科室接口
    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        // 创建Pageable对象
        // 0是第一页
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建Example对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department, matcher);

        Page<Department> all = departmentRepository.findAll(example, pageable);

        return all;
    }

    //删除科室接口
    @Override
    public void remove(String hoscode, String depcode) {
        //根据医院编号和科室编号查询
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department!=null){
            departmentRepository.deleteById(department.getId());
        }

    }

    //根据医院编号,查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        ArrayList<DepartmentVo> result = new ArrayList<>();

        Department department = new Department();
        department.setHoscode(hoscode);
        Example example = Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);

        //根据大科室编号 bitcode分组，获取每个大科室里面下级科室
        Map<String, List<Department>> demartmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        for(Map.Entry<String, List<Department>> entry : demartmentMap.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室编号对应的全局数据
            List<Department> departmentList1 = entry.getValue();

            //封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigcode);
            departmentVo1.setDepname(departmentList1.get(0).getBigname());

            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for(Department department1: departmentList1) {
                DepartmentVo departmentVo2 =  new DepartmentVo();
                departmentVo2.setDepcode(department1.getDepcode());
                departmentVo2.setDepname(department1.getDepname());
                //封装到list集合
                children.add(departmentVo2);
            }

            //把小科室list集合放到大科室children里面
            departmentVo1.setChildren(children);
            //放到最终result里面
            result.add(departmentVo1);
        }

        return result;
    }
}
