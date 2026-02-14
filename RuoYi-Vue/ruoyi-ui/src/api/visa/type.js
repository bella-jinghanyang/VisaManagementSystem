import request from "@/utils/request";

// 查询签证类型列表
export function listType(query) {
  return request({
    url: "/visa/type/list",
    method: "get",
    params: query,
  });
}

// 查询签证类型详细
export function getType(id) {
  return request({
    url: "/visa/type/" + id,
    method: "get",
  });
}

// 新增签证类型
export function addType(data) {
  return request({
    url: "/visa/type",
    method: "post",
    data: data,
  });
}

// 修改签证类型
export function updateType(data) {
  return request({
    url: "/visa/type",
    method: "put",
    data: data,
  });
}

// 删除签证类型
export function delType(id) {
  return request({
    url: "/visa/type/" + id,
    method: "delete",
  });
}

// 修改上架状态
export function changeTypeStatus(id, status) {
  return request({
    url: `/visa/type/changeStatus/${id}/${status}`,
    method: "put",
  });
}
