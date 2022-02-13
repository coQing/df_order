package com.coqing.order.hospital.service;


import com.coqing.order.model.hospital.Department;
import com.coqing.order.vo.hosp.DepartmentQueryVo;
import com.coqing.order.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //上传科室接口
    void save(Map<String, Object> paramMap);

    //查询科室接口
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    //删除科室接口
    void remove(String hoscode, String depcode);

    //根据医院编号,查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);
}
