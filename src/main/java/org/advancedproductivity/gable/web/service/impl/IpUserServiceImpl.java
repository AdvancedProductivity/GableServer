/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.advancedproductivity.gable.web.service.impl;

import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.web.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzq
 */
@Service
public class IpUserServiceImpl implements UserService {

    @Override
    public String getUserId(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equals(ip)) {
            return ip;
        }
        ip = request.getRemoteAddr();
        return ip;
    }

    @Override
    public String getUserId(Boolean isPublic, HttpServletRequest request) {
        if (isPublic != null && isPublic) {
            return GableConfig.PUBLIC_PATH;
        }
        return getUserId(request);
    }

}
