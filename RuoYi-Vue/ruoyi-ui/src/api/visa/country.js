import request from "@/utils/request";

// 查询国家配置列表
export function listCountry(query) {
  return request({
    url: "/visa/country/list",
    method: "get",
    params: query,
  });
}

// 查询国家配置详细
export function getCountry(id) {
  return request({
    url: "/visa/country/" + id,
    method: "get",
  });
}

// 新增国家配置
export function addCountry(data) {
  return request({
    url: "/visa/country",
    method: "post",
    data: data,
  });
}

// 修改国家配置
export function updateCountry(data) {
  return request({
    url: "/visa/country",
    method: "put",
    data: data,
  });
}

// 删除国家配置
export function delCountry(id) {
  return request({
    url: "/visa/country/" + id,
    method: "delete",
  });
}

// 修改上架状态
export function changeCountryStatus(id, status) {
  return request({
    url: `/visa/country/changeStatus/${id}/${status}`,
    method: "put",
  });
}
