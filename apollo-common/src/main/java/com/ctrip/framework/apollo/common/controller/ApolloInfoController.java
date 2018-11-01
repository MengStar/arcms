package com.ctrip.framework.apollo.common.controller;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.foundation.Foundation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/apollo")
public class ApolloInfoController {
    /**
     * @api {GET} /sys/app getApp
     * @apiGroup Info
     */
    @RequestMapping("app")
    public String getApp() {
        return Foundation.app().toString();
    }
    /**
     * @api {GET} /sys/net getNet
     * @apiGroup Info
     */
    @RequestMapping("net")
    public String getNet() {
        return Foundation.net().toString();
    }
    /**
     * @api {GET} /sys/server getServer
     * @apiGroup Info
     */
    @RequestMapping("server")
    public String getServer() {
        return Foundation.server().toString();
    }
    /**
     * @api {GET} /sys/version getVersion
     * @apiGroup Info
     */
    @RequestMapping("version")
    public String getVersion() {
        return Apollo.VERSION;
    }
}
