package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface MenuService {
    String UnitMenuFileName = "unit_menu.json";

    /**
     * get the unit test's menu true by user
     * @param nameSpace maybe userId,Ip...
     * @return menus
     * */
    ArrayNode getUserUnitMenus(String nameSpace);



    /**
     * get the unit test's menu true in public
     * @return menus
     * */
    ArrayNode getPublicUnitMenus();

    /**
     * generate a unit test group
     * @param groupName
     * @return group info
     * */
    ObjectNode addGroup(String groupName);

    /**
     * add unit test to the group
     * @param userUnitMenus menu tree
     * @param unitName new unit test name
     * @param type new test type
     * @param groupUuid new test belong to group
     * @return new test uuid
     */
    String addUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, String type, String nameSpace);

    String pushUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, JsonNode config, String nameSpace, String originUuid);

    String cloneUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, JsonNode config, String nameSpace, String originUuid);

    /**
     * update user menu
     * @param newMenu menu wait for update
     * @param nameSpace maybe userId,Ip...
     * */
    void updateUserMenu(ArrayNode newMenu,String nameSpace);

    void sync(String from, String to, String nameSpace);

    void deleteUnitTest(ArrayNode userUnitMenus, String uuid, String nameSpace);
}
