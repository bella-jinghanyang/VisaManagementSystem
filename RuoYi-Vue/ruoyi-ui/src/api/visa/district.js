import request from "@/utils/request";

// 查询领区配置列表
export function listDistrict(query) {
  return request({
    url: "/visa/district/list",
    method: "get",
    params: query,
  });
}

// 查询领区配置详细
export function getDistrict(id) {
  return request({
    url: "/visa/district/" + id,
    method: "get",
  });
}

// 新增领区配置
export function addDistrict(data) {
  return request({
    url: "/visa/district",
    method: "post",
    data: data,
  });
}

// 修改领区配置
export function updateDistrict(data) {
  return request({
    url: "/visa/district",
    method: "put",
    data: data,
  });
}

// 删除领区配置
export function delDistrict(id) {
  return request({
    url: "/visa/district/" + id,
    method: "delete",
  });
}

// 修改上架状态
export function changeDistrictStatus(id, status) {
  return request({
    url: `/visa/district/changeStatus/${id}/${status}`,
    method: "put",
  });
}
