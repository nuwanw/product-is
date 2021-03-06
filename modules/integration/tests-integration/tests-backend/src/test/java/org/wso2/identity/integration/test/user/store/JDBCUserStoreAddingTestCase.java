/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.test.user.store;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.test.user.mgt.UserManagementServiceAbstractTest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JDBCUserStoreAddingTestCase extends UserManagementServiceAbstractTest {
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private final String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private final String rwLDAPClass = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    private final String roLDAPClass = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    private final String adLDAPClass = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";
    private final String domainId = "WSO2.COM";
    private final String userStoreDBName = "JDBC_USER_STORE_DB";
    private final String dbUserName = "wso2automation";
    private final String dbUserPassword = "wso2automation";


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        doInit();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, getSessionCookie());
        testAddJDBCUserStore();
    }

    @Test(groups = "wso2.is", description = "Check user store manager implementations")
    public void testAvailableUserStoreClasses() throws Exception {
        String[] classes = userStoreConfigAdminServiceClient.getAvailableUserStoreClasses();
        List<String> classNames = Arrays.asList(classes);
        Assert.assertTrue(classNames.contains(jdbcClass), jdbcClass + " not present in User Store List.");
        Assert.assertTrue(classNames.contains(rwLDAPClass), rwLDAPClass + " not present.");
        Assert.assertTrue(classNames.contains(roLDAPClass), roLDAPClass + " not present.");
        Assert.assertTrue(classNames.contains(adLDAPClass), adLDAPClass + " not present.");

    }

    //    @Test(groups = "wso2.is", description = "Check add user store via DTO")
    private void testAddJDBCUserStore() throws Exception {

//        Property[] properties = (new JDBCUserStoreManager()).getDefaultUserStoreProperties().getMandatoryProperties();
        PropertyDTO[] propertyDTOs = new PropertyDTO[9];
        for (int i = 0; i < 9; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }
        //creating database
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                                                            + "/repository/database/" + userStoreDBName,
                                                            dbUserName, dbUserPassword);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue("jdbc:h2:repository/database/" + userStoreDBName);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(dbUserName);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(dbUserPassword);

        propertyDTOs[4].setName("PasswordJavaRegEx");
        propertyDTOs[4].setValue("^[\\S]{5,30}$");

        propertyDTOs[5].setName("UsernameJavaRegEx");
        propertyDTOs[5].setValue("^[\\S]{5,30}$");

        propertyDTOs[6].setName("Disabled");
        propertyDTOs[6].setValue("false");

        propertyDTOs[7].setName("PasswordDigest");
        propertyDTOs[7].setValue("SHA-256");

        propertyDTOs[8].setName("StoreSaltedPassword");
        propertyDTOs[8].setValue("true");

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(jdbcClass, domainId, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        Assert.assertTrue(waitForUserStoreDeployment(domainId), "Domain addition via DTO has failed.");

    }

/*    @Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore")
    public void addUserIntoJDBCUserStore() throws Exception {
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);


        userMgtClient.addRole(domainId + "/" + newUserRole, null, new String[]{"/permission/admin/login"});
        Assert.assertTrue(userMgtClient.roleNameExists(domainId.toUpperCase() + "/" + newUserRole)
                , "Role name doesn't exists");

        userMgtClient.addUser(domainId.toUpperCase() + "/" + newUserName, newUserPassword, new String[]{newUserRole}, null);
        Assert.assertTrue(userMgtClient.userNameExists(domainId.toUpperCase() + "/" + newUserRole
                , domainId.toUpperCase() + "/" + newUserName), "User name doesn't exists");

        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, automationContext
                .getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();
    }*/

/*    @Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore")
    public void disableUserStore() throws Exception {
        userStoreConfigAdminServiceClient.up

        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, automationContext
                .getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();
    }*/
/*
    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
    public void deleteUserFromJDBCUserStore() throws Exception {
        userMgtClient.deleteUser(domainId.toUpperCase() + "/" + newUserName);
        Assert.assertFalse(Utils.nameExists(userMgtClient.listAllUsers(domainId.toUpperCase() + "/" + newUserName, 10)
                , domainId.toUpperCase() + "/" + newUserName), "User Deletion failed");

        userMgtClient.deleteRole(domainId.toUpperCase() + "/" + newUserRole);
        Assert.assertFalse(Utils.nameExists(userMgtClient.getAllRolesNames(newUserRole, 100), newUserRole), "User Role still exist");
    }*/

    private boolean waitForUserStoreDeployment(String domain) throws Exception {
        long waitTime = System.currentTimeMillis() + 30000; //wait for 45 seconds
        while (System.currentTimeMillis() < waitTime) {
            UserStoreDTO[] userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO != null) {
                    if (userStoreDTO.getDomainId().equalsIgnoreCase(domain)) {
                        return true;
                    }
                }
            }
            Thread.sleep(1000);
        }
        return false;
    }


    @Override
    protected void setUserName() {
        newUserName = domainId + "/userStoreUser";

    }

    @Override
    protected void setUserPassword() {
        newUserPassword = "password";
    }

    @Override
    protected void setUserRole() {
        newUserRole = domainId + "/jdsbUserStoreRole";
    }
}
