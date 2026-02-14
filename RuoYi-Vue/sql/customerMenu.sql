-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户', '3', '1', 'customer', 'visa/customer/index', 1, 0, 'C', '0', '0', 'visa:customer:list', '#', 'admin', sysdate(), '', null, 'C端客户菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'visa:customer:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'visa:customer:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'visa:customer:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'visa:customer:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('C端客户导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'visa:customer:export',       '#', 'admin', sysdate(), '', null, '');