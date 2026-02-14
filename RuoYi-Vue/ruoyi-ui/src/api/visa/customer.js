import request from "@/utils/request";

// 查询C端客户列表
export function listCustomer(query) {
  return request({
    url: "/visa/customer/list",
    method: "get",
    params: query,
  });
}

// 查询C端客户详细
export function getCustomer(id) {
  return request({
    url: "/visa/customer/" + id,
    method: "get",
  });
}

// 新增C端客户
export function addCustomer(data) {
  return request({
    url: "/visa/customer",
    method: "post",
    data: data,
  });
}

// 修改C端客户
export function updateCustomer(data) {
  return request({
    url: "/visa/customer",
    method: "put",
    data: data,
  });
}

// 删除C端客户
export function delCustomer(id) {
  return request({
    url: "/visa/customer/" + id,
    method: "delete",
  });
}

// 修改上架状态
export function changeCustomerStatus(id, status) {
  return request({
    url: `/visa/customer/changeStatus/${id}/${status}`,
    method: "put",
  });
}
