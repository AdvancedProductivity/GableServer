package org.advancedproductivity.gable.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzq
 */
public interface UserService {

    /**
     * get operation's id
     * @param request
     * @return id
     * */
    String getUserId(HttpServletRequest request);
}
