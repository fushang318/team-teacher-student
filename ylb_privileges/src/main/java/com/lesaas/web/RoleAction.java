package com.lesaas.web;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lesaas.common.RightsHelper;
import com.lesaas.common.Tools;
import com.lesaas.model.Menu;
import com.lesaas.model.Role;
import com.lesaas.service.MenuService;
import com.lesaas.service.RoleService;



@Controller
@RequestMapping(value="/role")
public class RoleAction {
	
	@Autowired
	private RoleService roleService;
	@Autowired
	private MenuService menuService;
	
	@RequestMapping
	public String list(Map<String,Object> map){
		List<Role> roleList = roleService.listAllRoles();
		map.put("roleList", roleList);
		return "roles";
	}
	
	@RequestMapping(value="/save")
	public void save(PrintWriter out,Role role){
		boolean flag = true;
		if(role.getRoleId()!=null && role.getRoleId().intValue()>0){
			flag = roleService.updateRoleBaseInfo(role);
		}else{
			flag = roleService.insertRole(role);
		}
		if(flag){
			out.write("success");
		}else{
			out.write("failed");
		}
		out.flush();
		out.close();
	}
	
	@RequestMapping(value="/delete")
	public void deleteRoleById(@RequestParam int roleId,PrintWriter out){
		roleService.deleteRoleById(roleId);
		out.write("success");
		out.close();
	}
	
	@RequestMapping(value="/auth")
	public String auth(@RequestParam int roleId,Model model){
		List<Menu> menuList = menuService.listAllMenu();
		Role role = roleService.getRoleById(roleId);
		String roleRights = role.getRights();
		if(Tools.notEmpty(roleRights)){
			for(Menu menu : menuList){
				menu.setHasMenu(RightsHelper.testRights(roleRights, menu.getMenuId()));
				if(menu.isHasMenu()){
					List<Menu> subMenuList = menu.getSubMenu();
					for(Menu sub : subMenuList){
						sub.setHasMenu(RightsHelper.testRights(roleRights, sub.getMenuId()));
					}
				}
			}
		}
		JSONArray arr = JSONArray.fromObject(menuList);
		String json = arr.toString();
		json = json.replaceAll("menuId", "id").replaceAll("menuName", "name").replaceAll("subMenu", "nodes").replaceAll("hasMenu", "checked");
		model.addAttribute("zTreeNodes", json);
		model.addAttribute("roleId", roleId);
		return "authorization";
	}
	
	@RequestMapping(value="/auth/save")
	public void saveAuth(@RequestParam int roleId,@RequestParam String menuIds,PrintWriter out){
		BigInteger rights = RightsHelper.sumRights(Tools.str2StrArray(menuIds));
		Role role = roleService.getRoleById(roleId);
		role.setRights(rights.toString());
		roleService.updateRoleRights(role);
		out.write("success");
		out.close();
	}
}
