-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置', '3', '1', 'district', 'visa/district/index', 1, 0, 'C', '0', '0', 'visa:district:list', '#', 'admin', sysdate(), '', null, '领区配置菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'visa:district:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'visa:district:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'visa:district:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'visa:district:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('领区配置导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'visa:district:export',       '#', 'admin', sysdate(), '', null, '');