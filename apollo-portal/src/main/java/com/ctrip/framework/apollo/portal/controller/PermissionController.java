package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.enums.EnvUtils;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceEnvRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.AppRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.PermissionCondition;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
public class PermissionController {

    @Autowired
    private UserInfoHolder userInfoHolder;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleInitializationService roleInitializationService;

    /**
     * @api {POST} /apps/{appId}/initPermission initAppPermission
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     */
    @RequestMapping(value = "/apps/{appId}/initPermission", method = RequestMethod.POST)
    public ResponseEntity<Void> initAppPermission(@PathVariable String appId, @RequestBody String namespaceName) {
        roleInitializationService.initNamespaceEnvRoles(appId, namespaceName, userInfoHolder.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * @api {GET} /apps/{appId}/permissions/{permissionType}
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} permissionType
     */
    @RequestMapping(value = "/apps/{appId}/permissions/{permissionType}", method = RequestMethod.GET)
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(), permissionType, appId));

        return ResponseEntity.ok().body(permissionCondition);
    }

    /**
     * @api {GET} /apps/{appId}/namespaces/{namespaceName}/permissions/{permissionType} hasPermission
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     * @apiParam {String} permissionType
     */
    @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/permissions/{permissionType}", method = RequestMethod.GET)
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String namespaceName,
                                                             @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(), permissionType,
                        RoleUtils.buildNamespaceTargetId(appId, namespaceName)));

        return ResponseEntity.ok().body(permissionCondition);
    }

    /**
     * @api {GET} /apps/{appId}/envs/{env}/namespaces/{namespaceName}/permissions/{permissionType} hasPermission
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} env
     * @apiParam {String} permissionType
     */
    @RequestMapping(value = "/apps/{appId}/envs/{env}/namespaces/{namespaceName}/permissions/{permissionType}", method = RequestMethod.GET)
    public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName,
                                                             @PathVariable String permissionType) {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(
                rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(), permissionType,
                        RoleUtils.buildNamespaceTargetId(appId, namespaceName, env)));

        return ResponseEntity.ok().body(permissionCondition);
    }

    /**
     * @api {GET} /permissions/root hasRootPermission
     * @apiGroup Permission
     */
    @RequestMapping(value = "/permissions/root", method = RequestMethod.GET)
    public ResponseEntity<PermissionCondition> hasRootPermission() {
        PermissionCondition permissionCondition = new PermissionCondition();

        permissionCondition.setHasPermission(rolePermissionService.isSuperAdmin(userInfoHolder.getUser().getUserId()));

        return ResponseEntity.ok().body(permissionCondition);
    }

    /**
     * @api {GET} /apps/{appId}/envs/{env}/namespaces/{namespaceName}/role_users getNamespaceEnvRoles
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} env
     * @apiParam {String} namespaceName
     */
    @RequestMapping(value = "/apps/{appId}/envs/{env}/namespaces/{namespaceName}/role_users", method = RequestMethod.GET)
    public NamespaceEnvRolesAssignedUsers getNamespaceEnvRoles(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName) {

        // validate env parameter
        if (Env.UNKNOWN == EnvUtils.transformEnv(env)) {
            throw new BadRequestException("env is illegal");
        }

        NamespaceEnvRolesAssignedUsers assignedUsers = new NamespaceEnvRolesAssignedUsers();
        assignedUsers.setNamespaceName(namespaceName);
        assignedUsers.setAppId(appId);
        assignedUsers.setEnv(Env.fromString(env));

        Set<UserInfo> releaseNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName, env));
        assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

        Set<UserInfo> modifyNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName, env));
        assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

        return assignedUsers;
    }

    /**
     * @api {GET} /apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType} assignNamespaceEnvRoleToUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} env
     * @apiParam {String} namespaceName
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.POST)
    public ResponseEntity<Void> assignNamespaceEnvRoleToUser(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName,
                                                             @PathVariable String roleType, @RequestBody String user) {
        checkUserExists(user);
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }

        // validate env parameter
        if (Env.UNKNOWN == EnvUtils.transformEnv(env)) {
            throw new BadRequestException("env is illegal");
        }
        Set<String> assignedUser = rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType, env),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        if (CollectionUtils.isEmpty(assignedUser)) {
            throw new BadRequestException(user + "已授权");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * @api {GET} /apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType} removeNamespaceEnvRoleFromUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} env
     * @apiParam {String} roleType
     * @apiParam {String} user
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeNamespaceEnvRoleFromUser(@PathVariable String appId, @PathVariable String env, @PathVariable String namespaceName,
                                                               @PathVariable String roleType, @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        // validate env parameter
        if (Env.UNKNOWN == EnvUtils.transformEnv(env)) {
            throw new BadRequestException("env is illegal");
        }
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType, env),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * @api {GET} /apps/{appId}/namespaces/{namespaceName}/role_users getNamespaceRoles
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     */
    @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/role_users", method = RequestMethod.GET)
    public NamespaceRolesAssignedUsers getNamespaceRoles(@PathVariable String appId, @PathVariable String namespaceName) {

        NamespaceRolesAssignedUsers assignedUsers = new NamespaceRolesAssignedUsers();
        assignedUsers.setNamespaceName(namespaceName);
        assignedUsers.setAppId(appId);

        Set<UserInfo> releaseNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName));
        assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

        Set<UserInfo> modifyNamespaceUsers =
                rolePermissionService.queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName));
        assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

        return assignedUsers;
    }

    /**
     * @api {POST} /apps/{appId}/namespaces/{namespaceName}/roles/{roleType} assignNamespaceRoleToUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     * @apiParam {String} roleType
     * @apiParam {String} user
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.POST)
    public ResponseEntity<Void> assignNamespaceRoleToUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                          @PathVariable String roleType, @RequestBody String user) {
        checkUserExists(user);
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        Set<String> assignedUser = rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        if (CollectionUtils.isEmpty(assignedUser)) {
            throw new BadRequestException(user + "已授权");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * @api {DELETE} /apps/{appId}/namespaces/{namespaceName}/roles/{roleType} removeNamespaceRoleFromUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     * @apiParam {String} user
     * @apiParam {String} roleType
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeNamespaceRoleFromUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                            @PathVariable String roleType, @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * @api {GET} /apps/{appId}/role_users  getAppRoles
     * @apiGroup Permission
     * @apiParam {String} appId
     */
    @RequestMapping(value = "/apps/{appId}/role_users", method = RequestMethod.GET)
    public AppRolesAssignedUsers getAppRoles(@PathVariable String appId) {
        AppRolesAssignedUsers users = new AppRolesAssignedUsers();
        users.setAppId(appId);

        Set<UserInfo> masterUsers = rolePermissionService.queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));
        users.setMasterUsers(masterUsers);

        return users;
    }

    /**
     * @api {DELETE} /apps/{appId}/roles/{roleType} assignAppRoleToUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} user
     * @apiParam {String} roleType
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/roles/{roleType}", method = RequestMethod.POST)
    public ResponseEntity<Void> assignAppRoleToUser(@PathVariable String appId, @PathVariable String roleType,
                                                    @RequestBody String user) {
        checkUserExists(user);
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        Set<String> assignedUsers = rolePermissionService.assignRoleToUsers(RoleUtils.buildAppRoleName(appId, roleType),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        if (CollectionUtils.isEmpty(assignedUsers)) {
            throw new BadRequestException(user + "已授权");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * @api {DELETE} /apps/{appId}/roles/{roleType}  removeAppRoleFromUser
     * @apiGroup Permission
     * @apiParam {String} appId
     * @apiParam {String} user
     * @apiParam {String} roleType
     */
    @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
    @RequestMapping(value = "/apps/{appId}/roles/{roleType}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeAppRoleFromUser(@PathVariable String appId, @PathVariable String roleType,
                                                      @RequestParam String user) {
        RequestPrecondition.checkArgumentsNotEmpty(user);

        if (!RoleType.isValidRoleType(roleType)) {
            throw new BadRequestException("role type is illegal");
        }
        rolePermissionService.removeRoleFromUsers(RoleUtils.buildAppRoleName(appId, roleType),
                Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    private void checkUserExists(String userId) {
        if (userService.findByUserId(userId) == null) {
            throw new BadRequestException(String.format("User %s does not exist!", userId));
        }
    }

}
