package com.ra.boomblog.web.restapi;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ra.boomblog.commons.entity.Feedback;
import com.ra.boomblog.commons.entity.Link;
import com.ra.boomblog.commons.entity.SystemConfig;
import com.ra.boomblog.commons.entity.User;
import com.ra.boomblog.commons.feign.PictureFeignClient;
import com.ra.boomblog.utils.*;
import com.ra.boomblog.web.global.MessageConf;
import com.ra.boomblog.web.global.RedisConf;
import com.ra.boomblog.web.global.SQLConf;
import com.ra.boomblog.web.global.SysConf;
import com.ra.boomblog.xo.service.*;
import com.ra.boomblog.xo.utils.RabbitMqUtil;
import com.ra.boomblog.xo.utils.WebUtil;
import com.ra.boomblog.xo.vo.FeedbackVO;
import com.ra.boomblog.xo.vo.LinkVO;
import com.ra.boomblog.xo.vo.UserVO;
import com.ra.boomblog.base.enums.EGender;
import com.ra.boomblog.base.enums.ELinkStatus;
import com.ra.boomblog.base.enums.EOpenStatus;
import com.ra.boomblog.base.enums.EStatus;
import com.ra.boomblog.base.exception.ThrowableUtils;
import com.ra.boomblog.base.exception.exceptionType.InsertException;
import com.ra.boomblog.base.global.Constants;
import com.ra.boomblog.base.validator.group.Insert;
import com.ra.boomblog.base.vo.FileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ?????????????????????
 *
 * @author ??????
 * @date 2020???10???11???10:25:58
 */
@RestController
@RefreshScope
@RequestMapping("/oauth")
@Api(value = "???????????????????????????", tags = {"???????????????????????????"})
@Slf4j
public class AuthRestApi {
    @Autowired
    private WebUtil webUtil;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private WebConfigService webConfigService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private LinkService linkService;
    @Autowired
    private RabbitMqUtil rabbitMqUtil;
    @Autowired
    private UserService userService;
    @Value(value = "${justAuth.clientId.gitee}")
    private String giteeClienId;
    @Value(value = "${justAuth.clientSecret.gitee}")
    private String giteeClientSecret;
    @Value(value = "${justAuth.clientId.github}")
    private String githubClienId;
    @Value(value = "${justAuth.clientSecret.github}")
    private String githubClientSecret;
    @Value(value = "${justAuth.clientId.qq}")
    private String qqClienId;
    @Value(value = "${justAuth.clientSecret.qq}")
    private String qqClientSecret;
    @Value(value = "${data.webSite.url}")
    private String webSiteUrl;
    @Value(value = "${data.web.url}")
    private String moguWebUrl;
    @Value(value = "${BLOG.USER_TOKEN_SURVIVAL_TIME}")
    private Long userTokenSurvivalTime;
    /**
     * ???????????????
     */
    @Value(value = "${data.web.project_name_en}")
    private String PROJECT_NAME_EN;
    @Value(value = "${DEFAULE_PWD}")
    private String DEFAULE_PWD;
    @Value(value = "${uniapp.qq.appid}")
    private String APP_ID;
    @Value(value = "${uniapp.qq.appid}")
    private String SECRET;
    @Value(value = "${uniapp.qq.grant_type}")
    private String GRANT_TYPE;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "????????????", notes = "????????????")
    @RequestMapping("/render")
    public String renderAuth(String source) {
        // ?????????????????????????????????
        Boolean isOpenLoginType = webConfigService.isOpenLoginType(source.toUpperCase());
        if (!isOpenLoginType) {
            return ResultUtil.result(SysConf.ERROR, "??????????????????????????????!");
        }
        log.info("??????render:" + source);
        AuthRequest authRequest = getAuthRequest(source);
        String token = AuthStateUtils.createState();
        String authorizeUrl = authRequest.authorize(token);
        Map<String, String> map = new HashMap<>();
        map.put(SQLConf.URL, authorizeUrl);
        return ResultUtil.result(SysConf.SUCCESS, map);
    }


    /**
     * oauth?????????????????????????????????????????????????????????????????????gitee???????????????????????????????????????http://127.0.0.1:8603/oauth/callback/gitee
     */
    @RequestMapping("/callback/{source}")
    public void login(@PathVariable("source") String source, AuthCallback callback, HttpServletResponse httpServletResponse) throws IOException {
        log.info("??????callback???" + source + " callback params???" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse response = authRequest.login(callback);
        if (response.getCode() == Constants.NUM_5000) {
            // ?????????500????????????
            httpServletResponse.sendRedirect(webSiteUrl + Constants.STR_500);
            return;
        }
        String result = JSONObject.toJSONString(response);
        Map<String, Object> map = JsonUtils.jsonToMap(result);
        Map<String, Object> data = JsonUtils.jsonToMap(JsonUtils.objectToJson(map.get(SysConf.DATA)));
        Map<String, Object> token = new HashMap<>();
        String accessToken = "";
        if (data == null || data.get(SysConf.TOKEN) == null) {
            // ?????????500????????????
            httpServletResponse.sendRedirect(webSiteUrl + Constants.STR_500);
            return;
        } else {
            token = JsonUtils.jsonToMap(JsonUtils.objectToJson(data.get(SysConf.TOKEN)));
            accessToken = token.get(SysConf.ACCESS_TOKEN).toString();
        }

        Boolean exist = false;
        User user;
        //??????user????????????
        if (data.get(SysConf.UUID) != null && data.get(SysConf.SOURCE) != null) {
            user = userService.getUserBySourceAnduuid(data.get(SysConf.SOURCE).toString(), data.get(SysConf.UUID).toString());
            if (user != null) {
                exist = true;
                if (EStatus.DISABLED ==  user.getStatus()) {
                    throw new InsertException("?????????????????????????????????????????????");
                }
            } else {
                user = new User();
            }
        } else {
            return;
        }

        // ????????????????????????
        if (data.get(SysConf.EMAIL) != null) {
            String email = data.get(SysConf.EMAIL).toString();
            user.setEmail(email);
        }

        // ??????????????????
        if (data.get(SysConf.GENDER) != null && !exist) {
            String gender = data.get(SysConf.GENDER).toString();
            if (SysConf.MALE.equals(gender)) {
                user.setGender(EGender.MALE);
            } else if (SysConf.FEMALE.equals(gender)) {
                user.setGender(EGender.FEMALE);
            } else {
                user.setGender(EGender.UNKNOWN);
            }
        }

        // ????????????uid????????????
        String pictureList = this.pictureFeignClient.getPicture(user.getAvatar(), SysConf.FILE_SEGMENTATION);
        List<String> photoList = webUtil.getPicture(pictureList);
        Map<String, Object> picMap = (Map<String, Object>) JsonUtils.jsonToObject(pictureList, Map.class);

        // ???????????????????????????????????????
        if (SysConf.SUCCESS.equals(picMap.get(SysConf.CODE)) && photoList.size() > 0) {
            List<Map<String, Object>> picData = (List<Map<String, Object>>) picMap.get(SysConf.DATA);
            String fileOldName = picData.get(0).get(SysConf.FILE_OLD_NAME).toString();

            // ???????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????blob??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (fileOldName.equals(data.get(SysConf.AVATAR)) || SysConf.BLOB.equals(fileOldName)) {
                user.setPhotoUrl(photoList.get(0));
            } else {
                updateUserPhoto(data, user);
            }
        } else {
            // ?????????????????????????????????????????????????????????
            updateUserPhoto(data, user);
        }

        if (data.get(SysConf.NICKNAME) != null) {
            user.setNickName(data.get(SysConf.NICKNAME).toString());
        }

        if (user.getLoginCount() == null) {
            user.setLoginCount(1);
        } else {
            user.setLoginCount(user.getLoginCount() + 1);
        }
        // ??????????????????IP???????????????????????????
        user = userService.serRequestInfo(user);
        // ?????????token????????????user?????????????????????????????????redis????????????
        user.setValidCode(accessToken);
        if (exist) {
            user.updateById();
        } else {
            user.setUuid(data.get(SysConf.UUID).toString());
            user.setSource(data.get(SysConf.SOURCE).toString());
            String userName = PROJECT_NAME_EN.concat(Constants.SYMBOL_UNDERLINE).concat(user.getSource()).concat(Constants.SYMBOL_UNDERLINE).concat(user.getUuid());
            user.setUserName(userName);
            // ????????????????????????????????????????????????
            if (StringUtils.isEmpty(user.getNickName())) {
                user.setNickName(userName);
            }
            // ????????????
            user.setPassWord(MD5Utils.string2MD5(DEFAULE_PWD));
            // ????????????????????????????????????????????????
            user.setStartEmailNotification(EOpenStatus.CLOSE_STATUS);
            user.insert();
        }
        // ????????????
        user.setPassWord("");
        if (user != null) {
            //???????????????????????????????????????redis???
            stringRedisTemplate.opsForValue().set(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken, JsonUtils.objectToJson(user), userTokenSurvivalTime, TimeUnit.HOURS);
        }

        httpServletResponse.sendRedirect(webSiteUrl + "?token=" + accessToken);
    }

    /**
     * ??????????????????
     *
     * @param data
     * @param user
     */
    private void updateUserPhoto(Map<String, Object> data, User user) {
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.last(SysConf.LIMIT_ONE);
        SystemConfig systemConfig = systemConfigService.getOne(queryWrapper);
        // ????????????????????????????????????????????????
        FileVO fileVO = new FileVO();
        fileVO.setAdminUid(SysConf.DEFAULT_UID);
        fileVO.setUserUid(SysConf.DEFAULT_UID);
        fileVO.setProjectName(SysConf.BLOG);
        fileVO.setSortName(SysConf.ADMIN);
        fileVO.setSystemConfig(JsonUtils.object2Map(systemConfig));
        List<String> urlList = new ArrayList<>();
        if (data.get(SysConf.AVATAR) != null) {
            urlList.add(data.get(SysConf.AVATAR).toString());
        } else if (data.get(SysConf.AVATAR_URL) != null) {
            urlList.add(data.get(SysConf.AVATAR_URL).toString());
        }
        fileVO.setUrlList(urlList);
        String res = this.pictureFeignClient.uploadPicsByUrl(fileVO);
        Map<String, Object> resultMap = JsonUtils.jsonToMap(res);
        if (resultMap.get(SysConf.CODE) != null && SysConf.SUCCESS.equals(resultMap.get(SysConf.CODE).toString())) {
            if (resultMap.get(SysConf.DATA) != null) {
                List<Map<String, Object>> listMap = (List<Map<String, Object>>) resultMap.get(SysConf.DATA);
                if (listMap != null && listMap.size() > 0) {
                    Map<String, Object> pictureMap = listMap.get(0);

                    String localPictureBaseUrl = systemConfig.getLocalPictureBaseUrl();
                    String qiNiuPictureBaseUrl = systemConfig.getQiNiuPictureBaseUrl();
                    String picturePriority = systemConfig.getPicturePriority();
                    user.setAvatar(pictureMap.get(SysConf.UID).toString());
                    // ????????????????????????
                    if (EOpenStatus.OPEN.equals(picturePriority)) {
                        // ???????????????
                        if (pictureMap.get(SysConf.QI_NIU_URL) != null && pictureMap.get(SysConf.UID) != null) {
                            user.setPhotoUrl(qiNiuPictureBaseUrl + pictureMap.get(SysConf.QI_NIU_URL).toString());
                        }
                    } else {
                        // ???????????????????????????
                        if (pictureMap.get(SysConf.PIC_URL) != null && pictureMap.get(SysConf.UID) != null) {
                            user.setPhotoUrl(localPictureBaseUrl + pictureMap.get(SysConf.PIC_URL).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param map
     * @return
     */
    @ApiOperation(value = "decryptData", notes = "QQ???????????????????????????")
    @PostMapping("/decryptData")
    public String decryptData(@RequestBody Map<String, String> map) throws UnsupportedEncodingException {

        String encryptDataB64 = map.get("encryptDataB64");
        String jsCode = map.get("jsCode");
        String ivB64 = map.get("ivB64");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("appid", APP_ID);
        paramMap.put("secret", SECRET);
        paramMap.put("js_code", jsCode);
        paramMap.put("grant_type", GRANT_TYPE);

        String result = HttpUtil.get("https://api.q.qq.com/sns/jscode2session", paramMap);
        log.info("??????UnionID");
        log.info(result);
        Map<String, Object> resultMap = JsonUtils.jsonToMap(result);

        if (resultMap != null) {
            String sessionKey = resultMap.get("session_key").toString();
            String userInfo = UniappUtils.decryptData(encryptDataB64, sessionKey, ivB64);
            log.info(userInfo);
            Map<String, Object> userInfoMap = JsonUtils.jsonToMap(userInfo);

            Boolean exist = false;
            User user = null;
            //??????user????????????
            if (userInfoMap.get(SysConf.OPEN_ID) != null) {
                user = userService.getUserBySourceAnduuid("QQ", userInfoMap.get(SysConf.OPEN_ID).toString());
                if (user != null) {
                    log.info("???????????????");
                    exist = true;
                } else {
                    log.info("???????????????????????????????????????");
                    user = new User();
                }
            } else {
                log.info("???????????????openId");
                return ResultUtil.result(SysConf.ERROR, MessageConf.PARAM_INCORRECT);
            }

            // ??????????????????
            if (userInfoMap.get(SysConf.GENDER) != null) {
                log.info("??????????????????:{}", userInfoMap.get(SysConf.GENDER));
                String genderStr = userInfoMap.get(SysConf.GENDER).toString();
                String gender = Double.valueOf(genderStr).intValue() + "";
                if (EGender.MALE.equals(gender)) {
                    user.setGender(EGender.MALE);
                } else if (EGender.FEMALE.equals(gender)) {
                    user.setGender(EGender.FEMALE);
                } else {
                    user.setGender(EGender.UNKNOWN);
                }
            }

            // ????????????uid????????????
            String pictureList = this.pictureFeignClient.getPicture(user.getAvatar(), SysConf.FILE_SEGMENTATION);
            List<String> photoList = webUtil.getPicture(pictureList);
            Map<String, Object> picMap = (Map<String, Object>) JsonUtils.jsonToObject(pictureList, Map.class);
            log.info("????????????????????????:{}", JsonUtils.objectToJson(picMap));
            // ???????????????????????????????????????
            if (SysConf.SUCCESS.equals(picMap.get(SysConf.CODE)) && photoList.size() > 0) {
                List<Map<String, Object>> picData = (List<Map<String, Object>>) picMap.get(SysConf.DATA);
                String fileOldName = picData.get(0).get(SysConf.FILE_OLD_NAME).toString();

                // ???????????????????????????????????????????????????????????????????????????????????????
                // ??????????????????blob??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (fileOldName.equals(userInfoMap.get(SysConf.AVATAR_URL)) || SysConf.BLOB.equals(fileOldName)) {
                    user.setPhotoUrl(photoList.get(0));
                } else {
                    updateUserPhoto(userInfoMap, user);
                }
            } else {
                updateUserPhoto(userInfoMap, user);
            }

            if (userInfoMap.get(SysConf.NICK_NAME) != null) {
                user.setNickName(userInfoMap.get(SysConf.NICK_NAME).toString());
            }

            if (user.getLoginCount() == null) {
                user.setLoginCount(0);
            } else {
                user.setLoginCount(user.getLoginCount() + 1);
            }

            // ??????????????????IP???????????????????????????
            user = userService.serRequestInfo(user);

            // ?????????token????????????user?????????????????????????????????redis????????????
            String accessToken = StringUtils.getUUID();
            user.setValidCode(accessToken);

            if (exist) {
                user.updateById();
                log.info("??????????????????????????????");
                log.info(JsonUtils.objectToJson(user));
            } else {
                user.setSummary("");
                user.setUuid(userInfoMap.get(SysConf.OPEN_ID).toString());
                user.setSource("QQ");
                String userName = PROJECT_NAME_EN.concat("_").concat(user.getSource()).concat("_").concat(user.getUuid());
                user.setUserName(userName);
                // ????????????????????????????????????????????????
                if (StringUtils.isEmpty(user.getNickName())) {
                    user.setNickName(userName);
                }
                // ????????????
                user.setPassWord(MD5Utils.string2MD5(DEFAULE_PWD));
                // ????????????????????????????????????????????????
                user.insert();
                log.info("??????????????????: {}", user);
            }
            // ?????????????????????????????????????????????????????????
            user.setPassWord("");
            if (user != null) {
                //???????????????????????????????????????redis???
                stringRedisTemplate.opsForValue().set(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken, JsonUtils.objectToJson(user), userTokenSurvivalTime, TimeUnit.HOURS);
            }
            return ResultUtil.result(SysConf.SUCCESS, user);
        } else {
            return ResultUtil.result(SysConf.ERROR, MessageConf.PARAM_INCORRECT);
        }

    }

    @RequestMapping("/revoke/{source}/{token}")
    public Object revokeAuth(@PathVariable("source") String source, @PathVariable("token") String token) throws IOException {
        AuthRequest authRequest = getAuthRequest(source);
        return authRequest.revoke(AuthToken.builder().accessToken(token).build());
    }

    @RequestMapping("/refresh/{source}")
    public Object refreshAuth(@PathVariable("source") String source, String token) {
        AuthRequest authRequest = getAuthRequest(source);
        return authRequest.refresh(AuthToken.builder().refreshToken(token).build());
    }

    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/verify/{accessToken}")
    public String verifyUser(@PathVariable("accessToken") String accessToken) {
        String userInfo = stringRedisTemplate.opsForValue().get(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken);
        if (StringUtils.isEmpty(userInfo)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        } else {
            Map<String, Object> map = JsonUtils.jsonToMap(userInfo);
            return ResultUtil.result(SysConf.SUCCESS, map);
        }
    }

    @ApiOperation(value = "??????accessToken", notes = "??????accessToken")
    @RequestMapping("/delete/{accessToken}")
    public String deleteUserAccessToken(@PathVariable("accessToken") String accessToken) {
        stringRedisTemplate.delete(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken);
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
    }

    /**
     * ??????token?????????????????????
     *
     * @param token
     * @return
     */
    @GetMapping("/getSystemConfig")
    public String getSystemConfig(@RequestParam("token") String token) {
        String userInfo = stringRedisTemplate.opsForValue().get(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + token);
        if (StringUtils.isEmpty(userInfo)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.last(SysConf.LIMIT_ONE);
        SystemConfig SystemConfig = systemConfigService.getOne(queryWrapper);
        return ResultUtil.result(SysConf.SUCCESS, SystemConfig);
    }

    /**
     * ????????????????????????
     */
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping("/editUser")
    public String editUser(HttpServletRequest request, @RequestBody UserVO userVO) {
        if (request.getAttribute(SysConf.USER_UID) == null || request.getAttribute(SysConf.TOKEN) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();
        String token = request.getAttribute(SysConf.TOKEN).toString();
        User user = userService.getById(userUid);
        if (user == null) {
            return ResultUtil.result(SysConf.ERROR, "????????????, ??????????????????!");
        }
        log.info("??????????????????: {}", user);
        user.setNickName(userVO.getNickName());
        user.setAvatar(userVO.getAvatar());
        user.setBirthday(userVO.getBirthday());
        if (StringUtils.isNotEmpty(userVO.getSummary())) {
            user.setSummary(userVO.getSummary());
        } else {
            user.setSummary("???????????????????????????????????????");
        }
        user.setGender(userVO.getGender());
        user.setQqNumber(userVO.getQqNumber());
        user.setOccupation(userVO.getOccupation());

        // ??????????????????????????????????????????????????????
        if (userVO.getStartEmailNotification() == SysConf.ONE && !StringUtils.isNotEmpty(user.getEmail())) {
            return ResultUtil.result(SysConf.ERROR, "???????????????????????????????????????????????????????????????~");
        }
        user.setStartEmailNotification(userVO.getStartEmailNotification());
        user.updateById();
        user.setPassWord("");
        user.setPhotoUrl(userVO.getPhotoUrl());

        // ?????????????????????????????????
        if (userVO.getEmail() != null && !userVO.getEmail().equals(user.getEmail())) {
            user.setEmail(userVO.getEmail());
            // ??????RabbitMQ????????????
            rabbitMqUtil.sendRegisterEmail(user, token);
            // ????????????????????????Redis??????????????????
            stringRedisTemplate.opsForValue().set(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + token, JsonUtils.objectToJson(user), userTokenSurvivalTime, TimeUnit.HOURS);
            return ResultUtil.result(SysConf.SUCCESS, "??????????????????????????????????????????????????????");
        } else {
            stringRedisTemplate.opsForValue().set(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + token, JsonUtils.objectToJson(user), userTokenSurvivalTime, TimeUnit.HOURS);
            return ResultUtil.result(SysConf.SUCCESS, MessageConf.UPDATE_SUCCESS);
        }
    }

    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping("/updateUserPwd")
    public String updateUserPwd(HttpServletRequest request, @RequestParam(value = "oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd) {
        if (StringUtils.isEmpty(oldPwd)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.PARAM_INCORRECT);
        }
        if (request.getAttribute(SysConf.USER_UID) == null || request.getAttribute(SysConf.TOKEN) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();
        User user = userService.getById(userUid);
        // ???????????????????????????????????????
        if (!user.getSource().equals(SysConf.MOGU)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.CANNOT_CHANGE_THE_PASSWORD_BY_USER);
        }
        // ???????????????????????????
        if (user.getPassWord().equals(MD5Utils.string2MD5(oldPwd))) {
            user.setPassWord(MD5Utils.string2MD5(newPwd));
            user.updateById();
            return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
        }
        return ResultUtil.result(SysConf.ERROR, MessageConf.PASSWORD_IS_ERROR);
    }

    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/replyBlogLink")
    public String replyBlogLink(HttpServletRequest request, @RequestBody LinkVO linkVO) {
        if (request.getAttribute(SysConf.USER_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();

        User user = userService.getById(userUid);

        // ???????????????????????????????????????????????????????????????????????????????????????
        if (user != null && user.getCommentStatus() == SysConf.ZERO) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.YOU_DONT_HAVE_PERMISSION_TO_REPLY);
        }

        // ??????????????????????????????
        SystemConfig systemConfig = systemConfigService.getConfig();
        if (systemConfig != null && EOpenStatus.OPEN.equals(systemConfig.getStartEmailNotification())) {
            if (StringUtils.isNotEmpty(systemConfig.getEmail())) {
                log.info("??????????????????????????????");
                String feedback = "????????????????????????: " + "<br />"
                        + "?????????" + linkVO.getTitle() + "<br />"
                        + "?????????" + linkVO.getSummary() + "<br />"
                        + "?????????" + linkVO.getUrl();
                rabbitMqUtil.sendSimpleEmail(systemConfig.getEmail(), feedback);
            } else {
                log.error("????????????????????????????????????????????????");
            }
        }

        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.USER_UID, userUid);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.TITLE, linkVO.getTitle());
        queryWrapper.last(SysConf.LIMIT_ONE);
        Link existLink = linkService.getOne(queryWrapper);

        if (existLink != null) {
            Integer linkStatus = existLink.getLinkStatus();
            String message = "";
            switch (linkStatus) {
                case 0: {
                    message = MessageConf.BLOG_LINK_IS_EXIST;
                }
                break;
                case 1: {
                    message = MessageConf.BLOG_LINK_IS_PUBLISH;
                }
                break;
                case 2: {
                    message = MessageConf.BLOG_LINK_IS_NO_PUBLISH;
                }
                break;
            }
            return ResultUtil.result(SysConf.ERROR, message);
        }

        Link link = new Link();
        link.setLinkStatus(ELinkStatus.APPLY);
        link.setTitle(linkVO.getTitle());
        link.setSummary(linkVO.getSummary());
        link.setUrl(linkVO.getUrl());
        link.setClickCount(0);
        link.setSort(0);
        link.setFileUid(linkVO.getFileUid());
        link.setEmail(linkVO.getEmail());
        link.setStatus(EStatus.ENABLE);
        link.setUserUid(userUid);
        link.insert();
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);

    }

    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/getFeedbackList")
    public String getFeedbackList(HttpServletRequest request) {
        if (request.getAttribute(SysConf.USER_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.USER_UID, userUid);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        Page<Feedback> page = new Page<>();
        page.setSize(20);
        page.setCurrent(1);
        IPage<Feedback> pageList = feedbackService.page(page, queryWrapper);
        return ResultUtil.result(SysConf.SUCCESS, pageList);
    }

    @ApiOperation(value = "????????????", notes = "????????????", response = String.class)
    @PostMapping("/addFeedback")
    public String edit(HttpServletRequest request, @Validated({Insert.class}) @RequestBody FeedbackVO feedbackVO, BindingResult result) {

        // ????????????
        ThrowableUtils.checkParamArgument(result);

        if (request.getAttribute(SysConf.USER_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }

        String userUid = request.getAttribute(SysConf.USER_UID).toString();
        User user = userService.getById(userUid);

        // ?????????????????????????????????????????????????????????????????????????????????
        if (user != null && user.getCommentStatus() == SysConf.ZERO) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.YOU_DONT_HAVE_PERMISSION_TO_FEEDBACK);
        }

        // ??????????????????????????????
        SystemConfig systemConfig = systemConfigService.getConfig();
        if (systemConfig != null && EOpenStatus.OPEN.equals(systemConfig.getStartEmailNotification())) {
            if (StringUtils.isNotEmpty(systemConfig.getEmail())) {
                log.info("????????????????????????");
                String feedback = "????????????????????????: " + "<br />"
                        + "?????????" + feedbackVO.getTitle() + "<br />" + "<br />"
                        + "??????" + feedbackVO.getContent();
                rabbitMqUtil.sendSimpleEmail(systemConfig.getEmail(), feedback);
            } else {
                log.error("????????????????????????????????????????????????");
            }
        }

        Feedback feedback = new Feedback();
        feedback.setUserUid(userUid);
        feedback.setTitle(feedbackVO.getTitle());
        feedback.setContent(feedbackVO.getContent());

        // ?????????????????????
        feedback.setFeedbackStatus(0);
        feedback.setReply(feedbackVO.getReply());
        feedback.setUpdateTime(new Date());
        feedback.insert();
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.INSERT_SUCCESS);
    }

    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/bindUserEmail/{token}/{code}")
    public String bindUserEmail(@PathVariable("token") String token, @PathVariable("code") String code) {

        String userInfo = stringRedisTemplate.opsForValue().get(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + token);
        if (StringUtils.isEmpty(userInfo)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        User user = JsonUtils.jsonToPojo(userInfo, User.class);
        user.updateById();
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
    }

    /**
     * ??????
     *
     * @param source
     * @return
     */
    private AuthRequest getAuthRequest(String source) {
        AuthRequest authRequest = null;
        switch (source) {
            case SysConf.GITHUB:
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(githubClienId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(moguWebUrl + "/oauth/callback/github")
                        .build());
                break;
            case SysConf.GITEE:
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(giteeClienId)
                        .clientSecret(giteeClientSecret)
                        .redirectUri(moguWebUrl + "/oauth/callback/gitee")
                        .build());
                break;
            case SysConf.QQ:
                authRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(qqClienId)
                        .clientSecret(qqClientSecret)
                        .redirectUri(moguWebUrl + "/oauth/callback/qq")
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException(MessageConf.OPERATION_FAIL);
        }
        return authRequest;
    }
}
