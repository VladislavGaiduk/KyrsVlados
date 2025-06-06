package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Role;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.RoleService;

import java.util.List;

public class RoleController {
    private final RoleService roleService;

    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
    }

    public RoleController() {
        this.roleService = new RoleService();
    }

    public Response createRole(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);
            if (!(extractedData instanceof Role role)) {
                return new Response(false, "Некорректные данные роли", null);
            }
            
            // Проверяем, существует ли роль с таким именем
            Role existingRole = roleService.findRoleByName(role.getName());
            if (existingRole != null) {
                return new Response(false, "Роль с таким названием уже существует", null);
            }
            
            roleService.saveEntity(role);
            Role createdRole = roleService.findRoleByName(role.getName());
            return new Response(true, "Роль успешно создана", Serializer.toJson(createdRole));
            
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при создании роли: " + e.getMessage(), null);
        }
    }

    public Response getAllRoles() {
        try {
            List<Role> roles = roleService.findAllEntities();
            return new Response(true, "Роли успешно получены", Serializer.toJson(roles));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при получении списка ролей", null);
        }
    }

    public Response updateRole(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);
            if (!(extractedData instanceof Role role)) {
                return new Response(false, "Некорректные данные роли", null);
            }
            
            // Проверяем существование роли
            Role existingRole = roleService.findEntity(role.getId());
            if (existingRole == null) {
                return new Response(false, "Роль не найдена", null);
            }
            
            // Проверяем, не занято ли новое имя роли
            Role roleWithSameName = roleService.findRoleByName(role.getName());
            if (roleWithSameName != null && !roleWithSameName.getId().equals(role.getId())) {
                return new Response(false, "Роль с таким названием уже существует", null);
            }
            
            roleService.updateEntity(role);
            return new Response(true, "Роль успешно обновлена", null);
            
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при обновлении роли: " + e.getMessage(), null);
        }
    }

    public Response deleteRole(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);
            if (!(extractedData instanceof String)) {
                return new Response(false, "Некорректный формат ID роли", null);
            }
            
            int roleId;
            try {
                roleId = Integer.parseInt((String) extractedData);
            } catch (NumberFormatException e) {
                return new Response(false, "Некорректный формат ID роли", null);
            }
            
            // Проверяем существование роли
            Role existingRole = roleService.findEntity(roleId);
            if (existingRole == null) {
                return new Response(false, "Роль не найдена", null);
            }
            
            // TODO: Добавить проверку на использование роли перед удалением
            // if (roleService.isRoleInUse(existingRole)) {
            //     return new Response(false, "Невозможно удалить роль, так как она используется пользователями", null);
            // }
            
            roleService.deleteEntity(existingRole);
            return new Response(true, "Роль успешно удалена", null);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при удалении роли: " + e.getMessage(), null);
        }
    }
}

