package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AppNamespaceController {

    @Autowired
    private AppNamespaceService appNamespaceService;
    @Autowired
    private NamespaceService namespaceService;

    /**
     * @api {POST} /apps/{appId}/appnamespaces create
     * @apiGroup AdminNamespace
     * @apiParam {AppNamespaceDTO} appNamespace
     */
    @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
    public AppNamespaceDTO create(@RequestBody AppNamespaceDTO appNamespace) {

        AppNamespace entity = BeanUtils.transfrom(AppNamespace.class, appNamespace);
        AppNamespace managedEntity = appNamespaceService.findOne(entity.getAppId(), entity.getName());

        if (managedEntity != null) {
            throw new BadRequestException("app namespaces already exist.");
        }

        if (StringUtils.isEmpty(entity.getFormat())) {
            entity.setFormat(ConfigFileFormat.Properties.getValue());
        }

        entity = appNamespaceService.createAppNamespace(entity);

        return BeanUtils.transfrom(AppNamespaceDTO.class, entity);
    }

    /**
     * @api {DELETE} /apps/{appId}/appnamespaces/{namespaceName:.+}
     * @apiGroup AdminNamespace
     * @apiParam {String} appId
     * @apiParam {String} namespaceName
     * @apiParam {String} operator
     */
    @RequestMapping(value = "/apps/{appId}/appnamespaces/{namespaceName:.+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("appId") String appId, @PathVariable("namespaceName") String namespaceName,
                       @RequestParam String operator) {
        AppNamespace entity = appNamespaceService.findOne(appId, namespaceName);
        if (entity == null) {
            throw new BadRequestException("app namespace not found for appId: " + appId + " namespace: " + namespaceName);
        }
        appNamespaceService.deleteAppNamespace(entity, operator);
    }

    /**
     * @api {GET} /appnamespaces/{publicNamespaceName}/namespace findPublicAppNamespaceAllNamespaces
     * @apiGroup AdminNamespace
     * @apiParam {String} publicNamespaceName
     * @apiParam {Pageable} pageable
     */
    @RequestMapping(value = "/appnamespaces/{publicNamespaceName}/namespaces", method = RequestMethod.GET)
    public List<NamespaceDTO> findPublicAppNamespaceAllNamespaces(@PathVariable String publicNamespaceName, Pageable pageable) {

        List<Namespace> namespaces = namespaceService.findPublicAppNamespaceAllNamespaces(publicNamespaceName, pageable);

        return BeanUtils.batchTransform(NamespaceDTO.class, namespaces);
    }

    /**
     * @api {GET} /appnamespaces/{publicNamespaceName}/associated-namespaces/count countPublicAppNamespaceAssociatedNamespaces
     * @apiGroup AdminNamespace
     * @apiParam {String} publicNamespaceName
     */
    @RequestMapping(value = "/appnamespaces/{publicNamespaceName}/associated-namespaces/count", method = RequestMethod.GET)
    public int countPublicAppNamespaceAssociatedNamespaces(@PathVariable String publicNamespaceName) {
        return namespaceService.countPublicAppNamespaceAssociatedNamespaces(publicNamespaceName);
    }

}
