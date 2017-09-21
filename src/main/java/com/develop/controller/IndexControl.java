package com.develop.controller;


import com.alibaba.fastjson.JSONObject;
import com.develop.entity.User;
import com.develop.service.Impl.UserServiceImpl;
import com.develop.service.UserService;
import com.develop.util.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Random;

@Controller
@RequestMapping("/index")
public class IndexControl {

    @Autowired
    private UserServiceImpl userService;

    @ResponseBody
    @RequestMapping("/")
    public ResultVo index(HttpServletRequest request,
                                HttpServletResponse response) {
        ResultVo resultVo = new ResultVo();

        return resultVo;
    }

    /**
     * 请求样例代码
     * reqData = {};
     * reqData["userName"] = "***";
     * reqData["password"] = "***";
     *
     * $.ajax({
     *     url:"localhost:8080/index/login",
     *     type:"post",
     *     data:{data:Json.stringify(reqData)},
     *     success:function(data){}
     *
     * })
     *
     * /index/login 登录页
     *
     * @param request
     * 请求数据
     *  data:{
     *     reqData:{
     *        userPhone:用户手机号,
     *        userPassword:用户密码,
     *        checkCode:验证码
     *      }
     * }
     *@param response
     * 返回数据
     * {
     *     status:状态码 1-成功,0-失败
     *     Msg：返回提示消息
     *     code：验证码
     * }
     */
    @ResponseBody
    @RequestMapping(value = "/login",method= RequestMethod.POST)
    public ResultVo login(HttpServletRequest request,
                          HttpServletResponse response) {
        ResultVo resultVo = new ResultVo();
        String data = request.getParameter("data");
        JSONObject paramJSON = JSONObject.parseObject(data);
        JSONObject reqData = paramJSON.getJSONObject("reqData");

        String userPhone = reqData.getString("userPhone");
        String userPsd = reqData.getString("userPassword");
        String checkCode = reqData.getString("checkCode");
        String code = (String) request.getSession().getAttribute("code");

        return resultVo;
    }

    /**
     *
     * @param request
     * 请求数据
     *  data:{
     *        userType:用户类型,
     *        userPhone:用户手机号,
     *        phoneCode:手机验证码,
     *        userPassword:密码,
     *        againPassword:确认密码,
     *        checkCode:验证码
     * }
     * @param response
     * 返回数据
     * {
     *     status:状态码 1-成功,0-失败，
     *     Msg：返回提示消息，
     *     code：验证码
     * }
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/regist",method=RequestMethod.POST)
    public ResultVo regist(HttpServletRequest request,
                          HttpServletResponse response) {
        ResultVo resultVo = new ResultVo();
        String data = request.getParameter("data");
        if(isEmpty(data)){
            resultVo.setStatus(0);
            resultVo.setMsg("参数不能为空");
            return resultVo;
        }
        User user = new User();
        JSONObject reqData = JSONObject.parseObject(data);
        String code = (String) request.getSession().getAttribute("code");
        String phoneCheck = (String) request.getSession().getAttribute("phoneCode");
        if(reqData == null){
            resultVo.setStatus(0);
            resultVo.setMsg("参数不能为空");
            return resultVo;
        }
        String userType = reqData.getString("userType");
        if(isEmpty(userType)){
            resultVo.setStatus(0);
            resultVo.setMsg("用户类型不能为空");
            return resultVo;
        }
        user.setUserType(userType);
        String userPhone = reqData.getString("userPhone");
        if(isEmpty(userPhone)){
            resultVo.setStatus(0);
            resultVo.setMsg("手机号不能为空");
            return resultVo;
        }else if(!userPhone.matches( "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$")){
            resultVo.setStatus(0);
            resultVo.setMsg("手机号格式不正确");
            return resultVo;
        }
//        (".*[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*")
        user.setUserPhone(userPhone);
//        String phoneCode = reqData.getString("phoneCode");
//        if(code.equals(phoneCode)){
//            resultVo.setStatus(0);
//            resultVo.setMsg("手机验证码不正确");
//            return resultVo;
//        }
        String userPsd = reqData.getString("userPassword");
        if(isEmpty(userPsd)){
            resultVo.setStatus(0);
            resultVo.setMsg("密码不能为空");
            return resultVo;
        }else if(!userPsd.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$")){
            resultVo.setStatus(0);
            resultVo.setMsg("请输入6-16位包含字母和数字的密码");
            return resultVo;
        }
        String againPsd = reqData.getString("againPassword");
        if(!againPsd.equals(againPsd)){
            resultVo.setStatus(0);
            resultVo.setMsg("密码不一致");
            return resultVo;
        }
        user.setPassword(userPsd);
        String checkCode = reqData.getString("checkCode");
//        if(phoneCheck.equals(checkCode)){
//            resultVo.setStatus(0);
//            resultVo.setMsg("验证码不正确");
//            return resultVo;
//        }
        boolean flag = userService.insert(user);
        if(!flag){
            resultVo.setStatus(0);
            resultVo.setMsg("注册失败");
            return resultVo;
        }
        resultVo.setStatus(1);
        resultVo.setMsg("注册成功");
        return resultVo;
    }

    /**
     *
     * @param request
     * @param response
     *  返回数据
     * {
     *     status:状态码 1-成功,0-失败，
     *     Msg：返回提示消息，
     *     data:{
     *      code：验证码
     *     }
     * }
     * @return
     */
    @ResponseBody
    @RequestMapping("/requestCode")
    public ResultVo requestCode(HttpServletRequest request,
                           HttpServletResponse response) {
        ResultVo resultVo = new ResultVo();
        char[] codeChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456".toCharArray();
        String captcha = ""; // 存放生成的验证码
        Random random = new Random();
        for(int i = 0; i < 4; i++) {
            int index = random.nextInt(codeChar.length);
            captcha += codeChar[index];
        }
        request.getSession().setAttribute("code", captcha);
        resultVo.setStatus(1);
        HashMap map = new HashMap();
        map.put("code", captcha);
        resultVo.setData(map);
        return resultVo;
    }

    private boolean isEmpty(Object object){
        if(object instanceof String){
            if(((String) object).isEmpty()){
                return true;
            }
        }else if(object instanceof Integer){
            if(((Integer) object) == null){
                return true;

            }
        }
        return false;
    }
}
