package com.coqing.order.hospital.controller.api;


import com.coqing.order.common.exception.OrderException;
import com.coqing.order.common.helper.HttpRequestHelper;
import com.coqing.order.common.result.Result;
import com.coqing.order.common.result.ResultCodeEnum;
import com.coqing.order.common.utils.MD5;
import com.coqing.order.hospital.service.DepartmentService;
import com.coqing.order.hospital.service.HospitalService;
import com.coqing.order.hospital.service.HospitalSetService;
import com.coqing.order.hospital.service.ScheduleService;
import com.coqing.order.model.hospital.Department;
import com.coqing.order.model.hospital.Hospital;
import com.coqing.order.model.hospital.Schedule;
import com.coqing.order.vo.hosp.DepartmentQueryVo;
import com.coqing.order.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
@CrossOrigin
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    //删除排班接口
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //TODO 签名校验
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");

        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

        //查询排班接口
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //TODO 签名校验

        // 根据医院编码
        String hoscode = (String) paramMap.get("hoscode");
        // 获取科室编号
        String depcode = (String) paramMap.get("depcode");

        //当前页和每页记录数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String)paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        Page<Schedule> pageModel = scheduleService.findPageSchedule(page,limit,scheduleQueryVo);

        return Result.ok(pageModel);

    }

    //上传排班接口
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //TODO 签名校验
        scheduleService.save(paramMap);
        return Result.ok();
    }

    //删除科室接口
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 2 根据医院编码，查询数据库里的签名
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        //TODO 签名校验

        departmentService.remove(hoscode,depcode);

        return Result.ok();
    }

    //查询科室接口
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 2 根据医院编码，查询数据库里的签名
        String hoscode = (String) paramMap.get("hoscode");

        //TODO 签名校验

        //当前页和每页记录数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String)paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        Page<Department> pageModel = departmentService.findPageDepartment(page,limit,departmentQueryVo);

        return Result.ok(pageModel);

    }

    //上传科室接口
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);


        // 1 获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        // 2 根据医院编码，查询数据库里的签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);

        // 3 MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        // 4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)){
            throw new OrderException(ResultCodeEnum.SIGN_ERROR);
        }

        departmentService.save(paramMap);
        return Result.ok();
    }

    //查询医院接口
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 1 获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        // 2 根据医院编码，查询数据库里的签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);

        // 3 MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        // 4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)){
            throw new OrderException(ResultCodeEnum.SIGN_ERROR);
        }

        Hospital hospital =  hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }


        //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 1 获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");
        // 2 根据医院编码，查询数据库里的签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 3 MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        // 4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)){
            throw new OrderException(ResultCodeEnum.SIGN_ERROR);
        }

        //传输过程中“+”转化为了“ ”，现在转换回来
        String logoData = (String) paramMap.get("logoData");
        String replace = logoData.replace(" ", "+");
        paramMap.put("logoData",replace);

        //调用service方法
        hospitalService.save(paramMap);
        return Result.ok();


    }
}
