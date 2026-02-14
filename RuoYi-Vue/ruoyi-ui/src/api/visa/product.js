import request from "@/utils/request";

// 查询签证产品列表
export function listProduct(query) {
  return request({
    url: "/visa/product/list",
    method: "get",
    params: query,
  });
}

// 查询签证产品详细
export function getProduct(id) {
  return request({
    url: "/visa/product/" + id,
    method: "get",
  });
}

// 新增签证产品
export function addProduct(data) {
  return request({
    url: "/visa/product",
    method: "post",
    data: data,
  });
}

// 修改签证产品
export function updateProduct(data) {
  return request({
    url: "/visa/product",
    method: "put",
    data: data,
  });
}

// 删除签证产品
export function delProduct(id) {
  return request({
    url: "/visa/product/" + id,
    method: "delete",
  });
}

// 修改上架状态
// export function changeProductStatus(id, status) {
//   return request({
//     url: `/visa/product/changeStatus/${id}/${status}`,
//     method: "put",
//   });
// }

export function changeProductStatus(id, status) {
  const data = {
    id,
    status,
  };
  return request({
    url: "/visa/product/changeStatus", // 对应 Controller 里的路径
    method: "put",
    data: data,
  });
}
