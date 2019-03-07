package com.ctrip.framework.apollo.metaservice.controller;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private DiscoveryService discoveryService;

    /**
     * @api {GET} /services/meta getMetaService
     * @apiGroup Meta
     */
    @RequestMapping("/meta")
    public List<ServiceDTO> getMetaService() {
        List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
        return getServiceDTOS(instances);
    }

    /**
     * @api {GET} /services/config getConfigService
     * @apiGroup Meta
     * @apiParam {String} appId
     * @apiParam {String} clientIp
     */
    @RequestMapping("/config")
    public List<ServiceDTO> getConfigService(
            @RequestParam(value = "appId", defaultValue = "") String appId,
            @RequestParam(value = "ip", required = false) String clientIp) {
        List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
        List<ServiceDTO> result = instances.stream().map(instance -> {
            ServiceDTO service = new ServiceDTO();
            service.setAppName(instance.getAppName());
            service.setInstanceId(instance.getInstanceId());
          //service.setHomepageUrl(instance.getHomePageUrl());
            service.setHomepageUrl("http://127.0.0.1:8080");
            return service;
        }).collect(Collectors.toList());
        return result;
        //   return getServiceDTOS(instances);
    }

    private List<ServiceDTO> getServiceDTOS(List<InstanceInfo> instances) {
        List<ServiceDTO> result = instances.stream().map(instance -> {
            ServiceDTO service = new ServiceDTO();
            service.setAppName(instance.getAppName());
            service.setInstanceId(instance.getInstanceId());
            service.setHomepageUrl(instance.getHomePageUrl());
            //service.setHomepageUrl("http://127.0.0.1:8080");
            return service;
        }).collect(Collectors.toList());
        return result;
    }

    /**
     * @api {GET} /services/admin getAdminService
     * @apiGroup Meta
     */
    @RequestMapping("/admin")
    public List<ServiceDTO> getAdminService() {
        List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
        return getServiceDTOS(instances);
    }
}
