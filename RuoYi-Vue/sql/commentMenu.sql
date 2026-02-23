-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价', '3', '1', 'comment', 'visa/comment/index', 1, 0, 'C', '0', '0', 'visa:comment:list', '#', 'admin', sysdate(), '', null, '签证评价菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'visa:comment:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'visa:comment:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'visa:comment:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'visa:comment:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('签证评价导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'visa:comment:export',       '#', 'admin', sysdate(), '', null, '');