import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

export const EDCRService = {
  create: (data, tenantId) =>
    Request({
      url: Urls.edcr.create,
      // data: data,
      multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { tenantId },
      auth: true,
      multipartFormData: true
    }),

  rgacreate: (data, tenantId) =>
    Request({
      url: Urls.edcr.rgacreate,
      // data: data,
      multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { tenantId },
      auth: true,
      multipartFormData: true
    }),

    createEdcrRule: (edcrRule) =>
    Request({
      url: Urls.edcr.createEdcrRule,
      data: edcrRule,
      // multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { },
      auth: false,
      // multipartFormData: true
    })
};